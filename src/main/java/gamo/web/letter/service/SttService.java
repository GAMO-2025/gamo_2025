package gamo.web.letter.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.api.gax.longrunning.OperationFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;

@Service
public class SttService {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials.location}")
    private String credentialsPath;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    // GCS 업로드
    private String uploadToGcs(MultipartFile file) throws Exception {
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                .build()
                .getService();

        String objectName = "stt/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
        storage.create(blobInfo, file.getBytes());

        return "gs://" + bucketName + "/" + objectName;
    }

    // 장기간 음성 처리
    private String longRunningTranscribe(String gcsUri) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create(
                SpeechSettings.newBuilder()
                        .setCredentialsProvider(() -> GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                        .build()
        )) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(gcsUri)
                    .build();

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, audio);

            LongRunningRecognizeResponse longResponse = response.get();

            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : longResponse.getResultsList()) {
                transcript.append(result.getAlternatives(0).getTranscript());
            }

            return transcript.toString();
        }
    }

    public String transcribe(MultipartFile voiceFile) {
        try {
            String gcsUri = uploadToGcs(voiceFile);

            return longRunningTranscribe(gcsUri);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("STT 변환 실패: " + e.getMessage());
        }
    }
}
