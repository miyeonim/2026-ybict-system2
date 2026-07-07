package kepco.prorject.ictyb.back.ictyb_back.common.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ResultCodeEnum {
    SUCCESS(200),
    SUCCESS_CREATE(201),
    SUCCESS_DELETE(204),

    //4xx Client Errors
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    RUNTIME_ERROR(406),
    CONFLICT(409),

    //5xx Server Error
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    private int code;
    ResultCodeEnum(int code){
        this.code = code;
    }
}
