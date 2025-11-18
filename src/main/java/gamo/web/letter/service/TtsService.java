package gamo.web.letter.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TtsService {

    @Value("${gcp.credentials.location}")
    private Resource credentialsResource;

    public String synthesizeSpeech(String text) {

        try (InputStream credentialsStream = credentialsResource.getInputStream()) {

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {

                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(text)
                        .build();

                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("ko-KR")
                        .setSsmlGender(SsmlVoiceGender.FEMALE)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3)
                        .build();

                SynthesizeSpeechResponse response =
                        textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                return Base64.getEncoder()
                        .encodeToString(response.getAudioContent().toByteArray());
            }

        } catch (Exception e) {
            System.out.println("[TTS ERROR] " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("음성 생성 중 오류 발생");
        }
    }
}