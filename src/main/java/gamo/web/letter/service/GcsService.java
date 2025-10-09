package gamo.web.letter.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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

    // 초기화 블록에서 credentials 적용
    private void initStorage() throws IOException {
        if (storage == null) {
            storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(
                            com.google.auth.oauth2.ServiceAccountCredentials.fromStream(
                                    new FileInputStream(credentialsPath)
                            )
                    ).build()
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
                    .build();

            storage.create(blobInfo, file.getBytes());

            return "gs://" + bucketName + "/" + objectName;

        } catch (IOException e) {
            throw new RuntimeException("GCS 업로드 실패", e);
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
            throw new RuntimeException("Signed URL 생성 실패", e);
        }
    }
}