package com.alex;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен.");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage(e.getMessage());
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }
    }

    private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
        Message reqMessage = new Message(MessageType.NAME_REQUEST);
        while (true) {
            connection.send(reqMessage);
            Message recMessage = connection.receive();
            String username = recMessage.getData();
            if (recMessage.getType() != MessageType.USER_NAME) {
                if (username.isEmpty()) {
                    connection.send(new Message(MessageType.TEXT, "Имя не может быть пустым. Повторите попытку снова:"));
                } else if (connectionMap.containsKey(username)) {
                    connection.send(new Message(MessageType.TEXT, "Имя '" + username + "' уже используется другим пользователем. Введите другое имя:"));
                } else {
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "Вы вошли в чат как '" + username + "'. Добро пожаловать!"));
                    return recMessage.getData();
                }
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((k, v) -> {
            try {
                v.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не удалось отправить сообщение пользователю " + k);
            }
        });
    }
}
