package gamo.web.photo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import gamo.web.common.exception.CustomException;
import gamo.web.common.response.ErrorCode;
import gamo.web.photo.domain.Photo;
import gamo.web.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcpStorageService {
    private final PhotoRepository photoRepository;

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
        String fileName;
        do {
            fileName = UUID.randomUUID().toString();
        } while (photoRepository.findByUrl(fileName) != null);

        // 업로드
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, "photo/"+fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return fileName; //업로드한 파일이름
    }

    // 사진 삭제
    @Transactional
    public void delete(Long photoId) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new CustomException(ErrorCode.PHOTO_NOT_FOUND));
        String fileName = photo.getUrl();

        Storage storage = getStorage();

        storage.delete(bucketName, "photo/" + fileName);
    }

    // GCS에서 사진 바이트 가져오기 (프록시용)
    public byte[] load(String fileName) throws IOException {
        String objectName = "photo/" + fileName;
        Storage storage = getStorage();

        Blob blob = storage.get(bucketName, objectName);
        if (blob == null) {
            throw new CustomException(ErrorCode.PHOTO_NOT_FOUND);
        }
        return blob.getContent();
    }
}
