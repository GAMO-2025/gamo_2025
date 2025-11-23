package gamo.web.letter.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.api.gax.longrunning.OperationFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
public class SttService {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials.location}")
    private Resource credentialsResource;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    /**
     * 1️⃣ GCS에 음성 파일 업로드
     * - 파일을 지정된 버킷에 업로드
     * - 업로드 후 "gs://bucketName/파일경로" 형태 URI 반환
     */
    private String uploadToGcs(MultipartFile file) throws Exception {
        try (InputStream credentialsStream = credentialsResource.getInputStream()) {
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build()
                    .getService();

            String objectName = "stt/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
            storage.create(blobInfo, file.getBytes());

            return "gs://" + bucketName + "/" + objectName;
        }
    }
    /**
     * Google STT 장시간 음성 처리 (공통)
     * @param gcsUri GCS에 업로드된 파일 경로
     * @param audioChannelCount 오디오 채널 수 (0 = 설정 안 함, 1 = 모노, 2 = 스테레오)
     */
    private String longRunningTranscribe(String gcsUri, int audioChannelCount) throws Exception {
        try (InputStream credentialsStream = credentialsResource.getInputStream();
             SpeechClient speechClient = SpeechClient.create(
                     SpeechSettings.newBuilder()
                             .setCredentialsProvider(() -> GoogleCredentials.fromStream(credentialsStream))
                             .build()
             )) {

            RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true);

            if (audioChannelCount > 0) {
                configBuilder.setAudioChannelCount(audioChannelCount);
            }

            RecognitionConfig config = configBuilder.build();

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

    /**
     * 비디오콜 전용 STT 처리 메서드 (2채널 스테레오)
     * - 로컬 오디오 + 원격 오디오가 섞인 2채널 파일 처리
     */
    public String videoCallStt(MultipartFile voiceFile) {
        String gcsUri = null;

        try {
            log.info("비디오콜 STT 요청 들어옴");

            gcsUri = uploadToGcs(voiceFile);
            log.info("비디오콜 STT 업로드 성공: {}", gcsUri);

            String text = longRunningTranscribe(gcsUri, 2);
            log.info("비디오콜 STT 변환 완료: {} chars", text.length());

            deleteFromGcs(gcsUri);
            log.info("비디오콜 STT 음성 파일 삭제 완료: {}", gcsUri);

            return text;

        } catch (Exception e) {
            log.error("비디오콜 STT 변환 중 오류 발생", e);

            if (gcsUri != null) {
                try {
                    deleteFromGcs(gcsUri);
                    log.warn("오류 발생으로 GCS 파일 삭제 완료: {}", gcsUri);
                } catch (Exception inner) {
                    log.error("GCS 파일 삭제 실패: {}", gcsUri, inner);
                }
            }

            throw new RuntimeException("비디오콜 STT 변환 실패: " + e.getMessage(), e);
        }
    }


//    /**
//     * 2️⃣ Google STT 장시간 음성 처리
//     * - GCS URI를 이용해 음성을 STT로 변환
//     * - 결과를 문자열로 합쳐서 반환
//     */
//    private String longRunningTranscribe(String gcsUri) throws Exception {
//        try (InputStream credentialsStream = credentialsResource.getInputStream();
//             SpeechClient speechClient = SpeechClient.create(
//                     SpeechSettings.newBuilder()
//                             .setCredentialsProvider(() -> GoogleCredentials.fromStream(credentialsStream))
//                             .build()
//             )) {
//            RecognitionConfig config = RecognitionConfig.newBuilder()
//                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
//                    .setSampleRateHertz(48000)
//                    .setLanguageCode("ko-KR")
//                    .setEnableAutomaticPunctuation(true)
//                    .build();
//
//            RecognitionAudio audio = RecognitionAudio.newBuilder()
//                    .setUri(gcsUri)
//                    .build();
//
//            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
//                    speechClient.longRunningRecognizeAsync(config, audio);
//
//            LongRunningRecognizeResponse longResponse = response.get();
//
//            StringBuilder transcript = new StringBuilder();
//            for (SpeechRecognitionResult result : longResponse.getResultsList()) {
//                transcript.append(result.getAlternatives(0).getTranscript());
//            }
//
//            return transcript.toString();
//        }
//    }

    /**
     * 3️⃣ 외부에서 호출되는 STT 처리 메서드
     * - MultipartFile 음성 파일을 입력받아,
     *   1) 음성 파일을 GCS에 업로드
     *   2) 업로드된 GCS URI 기반으로 STT 장기 음성 변환 실행
     *   3) 변환이 끝난 후 GCS 음성 파일 삭제
     * - 최종적으로 변환된 텍스트를 반환
     */
    public String transcribe(MultipartFile voiceFile) {
        String gcsUri = null;

        try {
            // 1) GCS 업로드
            gcsUri = uploadToGcs(voiceFile);
            log.info("STT 업로드 성공: {}", gcsUri);

            // 2) STT 변환
            String text = longRunningTranscribe(gcsUri, 1);
            log.info("STT 변환 완료: {} chars", text.length());

            // 3) GCS 음성 파일 삭제
            deleteFromGcs(gcsUri);
            log.info("STT 음성 파일 삭제 완료: {}", gcsUri);

            return text;

        } catch (Exception e) {
            log.error("STT 변환 중 오류 발생", e);

            // 실패 시에도 업로드된 파일 제거 시도
            if (gcsUri != null) {
                try {
                    deleteFromGcs(gcsUri);
                    log.warn("오류 발생으로 GCS 파일 삭제 완료: {}", gcsUri);
                } catch (Exception inner) {
                    log.error("GCS 파일 삭제 실패: {}", gcsUri, inner);
                }
            }

            throw new RuntimeException("STT 변환 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 4️⃣ STT 처리 후 GCS 음성 파일 삭제
     * - "gs://bucket/path" 형태의 URI에서 objectName 추출 후 삭제
     */
    private void deleteFromGcs(String gcsUri) throws Exception {
        try (InputStream credentialsStream = credentialsResource.getInputStream()) {

            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build()
                    .getService();

            String objectName = gcsUri.replace("gs://" + bucketName + "/", "");

            boolean deleted = storage.delete(bucketName, objectName);

            if (!deleted) {
                log.warn("GCS 파일 삭제 실패 또는 파일 없음: {}", objectName);
            }
        }
    }

}