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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YtDown {
    Object stageSendYTVideo(Message message) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://youtube-media-downloader.p.rapidapi.com/v2/video/details?videoId="+extractVideoId(message.getText())))
                .header("X-RapidAPI-Key", "ffdaef41eemsh19658dbc3cd6d1fp12e96ajsn6cddc4828a8e")
                .header("X-RapidAPI-Host", "youtube-media-downloader.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {


                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(String.valueOf(message.getChatId()));

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(response.body());
                String videoUrl = responseJson.get("videos").get("items").get(0).get("url").asText();
                sendVideo.setVideo(new InputFile(videoUrl));
                sendVideo.setCaption(message.getText()+" \n \n Join us ⏬ \n  @tolliq_skachatbot");


                return sendVideo;
            } else {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setText("The link you sent is wrong ! \n Please, send correct link from Instagram, TikTok, YouTube.");

                errorMessage.setChatId(String.valueOf(message.getChatId()));
                return errorMessage;
            }
        } catch (InterruptedException e) {
            SendMessage errorMessage = new SendMessage();
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

    private static String extractVideoId(String url) {
        String videoId = null;
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v=|\\/v\\/|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|embed\\?v=)([^#\\&\\?\\n]*[^\\?\\n])";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            videoId = matcher.group();
        }
        return videoId;
    }
}
