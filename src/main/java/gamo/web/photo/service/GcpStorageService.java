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
    @Value("src\\main\\resources\\photo_api.json")
//    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileName;

    @Value("gamo_photo_buket")
//    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) throws IOException {
        System.out.println("keyFileName: " + keyFileName);
        System.out.println("bucketName: " + bucketName);
        // GCP 인증 키 불러오기
        InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream();
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build()
                .getService();

        // 파일 이름 랜덤으로 생성
        String fileName = UUID.randomUUID().toString();

        // 업로드
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        // 업로드된 파일 URL 반환
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }
}
