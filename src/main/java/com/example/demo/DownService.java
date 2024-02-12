package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Component
@RequiredArgsConstructor
public class DownService extends TelegramLongPollingBot {

    private final UserRepository repository;

    private final InsDown insDown;
    private final TtDown ttDown;
    private final YtDown yt;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        SendVideo sendVideo = null;
        Message message = update.getMessage();
        String text = message.getText();
        SendMessage sendMessage = new SendMessage();

        if (message.hasText()) {
            if (text.equals("/start")) {
                sendMessage.setText("Assalomu aleykum botga hush kelibsiz.\n Instagram, Tiktok, Youtube dan link jonating");
                sendMessage.setChatId(String.valueOf(message.getChatId()));
                execute(sendMessage);

                Optional<User> optional = this.repository.findById(message.getChatId());
                if (optional.isEmpty()){
                    User user = new User();
                    user.setChatId(update.getMessage().getChatId());
                    user.setUserName(update.getMessage().getChat().getUserName());
                    user.setLastName(update.getMessage().getChat().getLastName());
                    user.setFirstName(update.getMessage().getChat().getFirstName());
                    this.repository.save(user);
                }
            }else if (text.startsWith("https://www.instagram.com/")) {
                sendAndDelete(message);
                Object response = this.insDown.stageSendVideo(message);

                if (response instanceof SendVideo) {
                    sendVideo= (SendVideo) response;
                    execute(sendVideo);
                } else if (response instanceof SendMessage) {
                    sendMessage= (SendMessage) response;
                    execute(sendMessage);
                }
            } else if (text.startsWith("https://vt.tiktok.com/")) {
                sendAndDelete(message);
                Object response = this.ttDown.stageSendTTVideo(message);

                if (response instanceof SendVideo) {
                    sendVideo= (SendVideo) response;
                    execute(sendVideo);
                } else if (response instanceof SendMessage) {
                    sendMessage= (SendMessage) response;
                    execute(sendMessage);
                }
            } else if (text.startsWith("https://youtu.be/")) {
                sendAndDelete(message);
                Object response = this.yt.stageSendYTVideo(message);

                if (response instanceof SendVideo) {
                    sendVideo= (SendVideo) response;
                    execute(sendVideo);
                } else if (response instanceof SendMessage) {
                    sendMessage= (SendMessage) response;
                    execute(sendMessage);
                }
            }else {
                sendMessage.setText("The link you sent is wrong.\n" +
                        "Please, send correct link from Instagram, TikTok, YouTube.");
                sendMessage.setChatId(String.valueOf(message.getChatId()));
                execute(sendMessage);

            }
        }


    }






    @Override
    public String getBotUsername() {
        return "@tolliq_skachatbot";
    }

    public String getBotToken() {
        return "6606131007:AAF4TBYJjyynr7W2M9OCtzJdDa6t4rY9Rho";
    }
    private void sendAndDelete(Message message) throws TelegramApiException {
        SendMessage pleaseWaitMessage = new SendMessage();
        pleaseWaitMessage.setChatId(String.valueOf(message.getChatId()));
        pleaseWaitMessage.setText("Iltimos, kuting...");

        Message sentMessage = execute(pleaseWaitMessage);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(String.valueOf(sentMessage.getChatId()));
            deleteMessage.setMessageId(sentMessage.getMessageId());
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }, 2, TimeUnit.SECONDS);

        executor.shutdown();
    }
}