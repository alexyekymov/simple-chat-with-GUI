package com.alex.client;

import com.alex.Connection;
import com.alex.ConsoleHelper;
import com.alex.Message;
import com.alex.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Ошибка во время ожидания.");
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода введите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected) {
            String message = ConsoleHelper.readMessage();
            if (message.equalsIgnoreCase("exit")) {
                clientConnected = false;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readMessage();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите Ваше имя:");
        return ConsoleHelper.readMessage();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение. " + e.getMessage());
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоденился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType messageType = message.getType();
                if (messageType == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    Message newMessage = new Message(MessageType.USER_NAME, userName);
                    connection.send(newMessage);
                } else if (messageType == MessageType.NAME_ACCEPTED) {
                    ConsoleHelper.writeMessage(message.getData());
                    notifyConnectionStatusChanged(true);
                    break;
                } else if (messageType == MessageType.TEXT) {
                    ConsoleHelper.writeMessage(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType messageType = message.getType();
                if (messageType == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (messageType == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (messageType == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
    }
}
