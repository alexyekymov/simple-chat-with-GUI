package com.alex.client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }

    @Override
    protected String getUserName() {
        int number = (int) (Math.random() * 100);
        return "date_bot_" + number;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (message.contains(": ")) {
                Map<String, String> map = new TreeMap<>();
                map.put("дата", "d.MM.yyyy");
                map.put("день", "d");
                map.put("месяц", "MMMM");
                map.put("год", "yyyy");
                map.put("время", "H:mm:ss");
                map.put("час", "H");
                map.put("минуты", "m");
                map.put("секунды", "s");

                String userName = message.split(": ")[0];
                String command = message.substring(userName.length() + 2)
                        .trim();
                String answer = String.format("Информация для %s: ", userName);
                LocalDateTime currentTime = LocalDateTime.now();
                DateTimeFormatter formatter;
                for (Map.Entry<String, String> pair : map.entrySet()) {
                    if (pair.getKey().equalsIgnoreCase(command)) {
                        formatter = DateTimeFormatter.ofPattern(pair.getValue());
                        sendTextMessage(answer + currentTime.format(formatter));
                    }
                }
            }
        }
    }
}
