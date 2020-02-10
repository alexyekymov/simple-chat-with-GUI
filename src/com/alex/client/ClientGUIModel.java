package com.alex.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGUIModel {
    private final Set<String> allUserNames = new HashSet<>();
    private String newMessage;

    public void deleteUser(String userName) {
        allUserNames.remove(userName);
    }

    public void addUser(String userName) {
        allUserNames.add(userName);
    }

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }
}
