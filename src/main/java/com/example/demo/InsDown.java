package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class InsDown {
    Object stageSendVideo(Message message) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://instagram-post-and-reels-downloader.p.rapidapi.com/?url="+message.getText()))
                .header("X-RapidAPI-Key", "ffdaef41eemsh19658dbc3cd6d1fp12e96ajsn6cddc4828a8e")
                .header("X-RapidAPI-Host", "instagram-post-and-reels-downloader.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {


                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(String.valueOf(message.getChatId()));

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(response.body());

                String videoUrl = responseJson.get(0).get("link").asText();

                sendVideo.setVideo(new InputFile(videoUrl));
                sendVideo.setCaption(message.getText()+" \n \n Join us ⏬ \n  @tolliq_skachatbot");

                return sendVideo;
            } else {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(String.valueOf(message.getChatId()));
                errorMessage.setText("The link you sent is wrong ! \n Please, send correct link from Instagram, TikTok, YouTube.");
                errorMessage.setChatId(String.valueOf(message.getChatId()));
                return errorMessage;
            }
        } catch (InterruptedException e) {
            SendMessage errorMessage = new SendMessage();
            errorMessage.setChatId(String.valueOf(message.getChatId()));
            errorMessage.setText("Failed to download video. Please try again later.");
            errorMessage.setChatId(String.valueOf(message.getChatId()));
            return errorMessage;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
