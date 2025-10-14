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

    /**
     * 1️⃣ GCS에 음성 파일 업로드
     * - 파일을 지정된 버킷에 업로드
     * - 업로드 후 "gs://bucketName/파일경로" 형태 URI 반환
     */
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

    /**
     * 2️⃣ Google STT 장시간 음성 처리
     * - GCS URI를 이용해 음성을 STT로 변환
     * - WEBM/Opus 파일 지원
     * - 자동 문장 부호 활성화
     * - 결과를 문자열로 합쳐서 반환
     */
    private String longRunningTranscribe(String gcsUri) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create(
                SpeechSettings.newBuilder()
                        .setCredentialsProvider(() -> GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                        .build()
        )) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(gcsUri)
                    .build();

            // 장시간 음성 인식 요청
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, audio);

            LongRunningRecognizeResponse longResponse = response.get();

            // 인식된 텍스트 합치기
            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : longResponse.getResultsList()) {
                transcript.append(result.getAlternatives(0).getTranscript());
            }

            return transcript.toString();
        }
    }

    /**
     * 3️⃣ 외부에서 호출하는 메서드
     * - MultipartFile을 받아서
     *   1) GCS 업로드
     *   2) STT 변환
     * - 최종 텍스트 반환
     */
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