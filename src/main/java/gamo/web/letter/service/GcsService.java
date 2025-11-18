package gamo.web.letter.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsService {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials.location}")
    private String credentialsPath;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    private Storage storage;

    private void initStorage() throws IOException {
        if (storage == null) {
            InputStream credentialsStream;
            if (credentialsPath.startsWith("classpath:")) {
                credentialsStream = new ClassPathResource(
                        credentialsPath.substring("classpath:".length())
                ).getInputStream();
            } else {
                credentialsStream = new FileInputStream(credentialsPath);
            }

            storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
                    .build()
                    .getService();
        }
    }

    // 파일 업로드 후 GCS 경로 반환
    public String uploadFile(MultipartFile file) {
        try {
            initStorage();

            String objectName = "letters/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setCacheControl("public, max-age=604800") // 1주일 캐시
                    .build();

            storage.create(blobInfo, file.getBytes());

            return "gs://" + bucketName + "/" + objectName;

        } catch (IOException e) {
            throw new CustomException(ErrorCode.LETTER_IMAGE_UPLOAD_FAILED);
        }
    }

    // Signed URL 생성 (읽기용)
    public URL generateSignedUrl(String gsPath, long durationMinutes) {
        try {
            initStorage();

            String objectPath = gsPath.replace("gs://" + bucketName + "/", "");
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectPath).build();

            return storage.signUrl(blobInfo, durationMinutes, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.LETTER_IMAGE_SIGNED_URL_FAILED);
        }
    }

    // GCS 객체 삭제
    public void deleteFile(String gsPath) throws IOException {
        initStorage();

        String objectName = gsPath.replace("gs://" + bucketName + "/", "");
        boolean deleted = storage.delete(bucketName, objectName);

        if (!deleted) {
            log.warn("GCS 객체가 존재하지 않거나 삭제 실패: {}", objectName);
        }
    }

}