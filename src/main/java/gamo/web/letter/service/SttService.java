package gamo.web.letter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SttService {

    public String convertToText(MultipartFile voiceFile) throws IOException {
        try (SpeechClient speechClient = SpeechClient.create()) {
            // 음성 파일 바이트 읽기
            byte[] audioBytes = voiceFile.getBytes();
            ByteString audioData = ByteString.copyFrom(audioBytes);

            // RecognitionAudio
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioData)
                    .build();

            // RecognitionConfig
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) // wav 파일이면 LINEAR16
                    .setLanguageCode("ko-KR") // 한국어
                    .setSampleRateHertz(16000) // 녹음 샘플링레이트
                    .build();

            // 음성 인식 요청
            RecognizeResponse response = speechClient.recognize(config, audio);
            StringBuilder sb = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                sb.append(result.getAlternatives(0).getTranscript());
            }
            return sb.toString();
        }
    }
}
