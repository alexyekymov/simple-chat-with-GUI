package com.alex.client;

public class ClientGUIController extends Client {
    private ClientGUIModel model = new ClientGUIModel();
    private ClientGUIView view = new ClientGUIView(this);

    public static void main(String[] args) {
        ClientGUIController client = new ClientGUIController();
        client.run();
    }

    public ClientGUIModel getModel() {
        return model;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GUISocketThread();
    }

    @Override
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.start();
    }

    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    public class GUISocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }
}
