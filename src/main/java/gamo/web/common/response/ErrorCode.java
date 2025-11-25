package gamo.web.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "요청 파라미터가 올바르지 않습니다."),
    INVALID_DATE(HttpStatus.BAD_REQUEST, "DATE-400", "유효하지 않은 날짜입니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "FORMAT-400", "형식이 올바르지 않습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "COMMON-400", "JSON 파싱에 실패했습니다."),

    // Member 관련한 에러들
    NOT_SAME_FAMILY(HttpStatus.FORBIDDEN, "FAMILY-403", "같은 가족에게만 수행할 수 있는 요청입니다."),
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND,  "FAMILY-404","가족 정보를 찾을 수 없습니다."),
    FAMILY_EXISTS(HttpStatus.CONFLICT, "FAMILY-409", "이미 가족에 가입되어 있습니다."),
    FAMILY_CODE_DUPLICATED(HttpStatus.CONFLICT, "FAMILY-CODE-409", "가족 코드가 중복되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404","회원 정보를 찾을 수 없습니다."),

    // Letter 관련
    LETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LETTER-404", "편지를 찾을 수 없습니다."),
    LETTER_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "LETTER-403", "편지를 삭제할 권한이 없습니다."),
    LETTER_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "LETTER-403", "편지를 조회할 권한이 없습니다."),
    LETTER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "LETTER-400", "이미 삭제된 편지입니다."),
    // Letter Image 관련
    LETTER_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LETTER-500", "편지 이미지 업로드에 실패했습니다."),
    LETTER_IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LETTER-500", "편지 이미지 삭제에 실패했습니다."),
    LETTER_IMAGE_SIGNED_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LETTER-500", "편지 이미지 Signed URL 생성에 실패했습니다."),

    //Photo 관련
    ALBUM_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "PHOTO-403", "앨범를 삭제할 권한이 없습니다."),
    ALBUM_NOT_FOUND(HttpStatus.FORBIDDEN, "PHOTO-404", "앨범을 찾을 수 없습니다."),
    ALBUM_NOT_EMPTY(HttpStatus.FORBIDDEN, "PHOTO-409", "앨범이 비어있지 않습니다."),
    PHOTO_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO-404", "이미지를 찾을 수 없습니다."),
    PHOTO_ACCESS_FORBIDDEN(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO-403", "이미지에 접근할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
