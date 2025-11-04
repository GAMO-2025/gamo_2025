package gamo.web.photo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class GcpStorageService {
    @Value("${gcp.credentials.location}")
    private String keyFileName;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    //공통
    private Storage getStorage() throws IOException {
        InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream();
        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();
    }

    //파일 업로드
    public String upload(MultipartFile file) throws IOException {
        // GCP 인증 키 불러오기
        Storage storage = getStorage();

        // 파일 이름 랜덤으로 생성
        String fileName = UUID.randomUUID().toString();

        // 업로드
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, "photo/"+fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        // 업로드된 파일 URL 반환
        return "https://storage.googleapis.com/photo/" + bucketName + "/" + fileName;
    }

    // 사진 삭제
    public boolean delete(String fileName) throws IOException {
        Storage storage = getStorage();

        boolean deleted = storage.delete(bucketName, "photo/" + fileName);
        if (!deleted) {
            System.err.println("⚠️ 파일이 존재하지 않거나 삭제 실패: " + fileName);
        }

        return deleted;
    }
}
