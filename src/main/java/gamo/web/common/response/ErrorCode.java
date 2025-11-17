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
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404","회원 정보를 찾을 수 없습니다."),

    // Letter 관련
    LETTER_NOT_FOUND(HttpStatus.NOT_FOUND, "LETTER-404", "편지를 찾을 수 없습니다."),
    LETTER_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "LETTER-403", "편지를 삭제할 권한이 없습니다."),
    LETTER_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "LETTER-403", "편지를 조회할 권한이 없습니다."),
    LETTER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "LETTER-4001", "이미 삭제된 편지입니다."),
    FILE_SIGNED_URL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-500", "Signed URL 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
