package kepco.prorject.ictyb.back.ictyb_back.common;

import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder=true)
@Data
public class BaseResponse<T> {
    private StatusEnum status;
    private T data;
    private ResultCodeEnum resultCode;
    private String resultMsg;


    // SUCCESS
    public static <V> BaseResponse<V> actionSuccess() {
        return new BaseResponse<>(StatusEnum.SUCCESS, null, ResultCodeEnum.SUCCESS, "정상적으로 처리되었습니다.");
    }

    // INSERT SUCCESS
    public static BaseResponse<?> actionCreateSuccess() {
        return new BaseResponse<>(StatusEnum.SUCCESS, null, ResultCodeEnum.SUCCESS_CREATE, "정상적으로 등록됐습니다.");
    }

    // DELETE SUCCESS
    public static BaseResponse<?> actionSuccessWithNoContent() {
        return new BaseResponse<>(StatusEnum.SUCCESS, null, ResultCodeEnum.SUCCESS_DELETE, "정상적으로 삭제됐습니다.");
    }

    // NOT AUTHROIZATION
    public static BaseResponse<?> actionBadRequest() {
        return new BaseResponse<>(StatusEnum.FAIL, null, ResultCodeEnum.BAD_REQUEST, "올바른 키가 아닙니다.");
    }

    // NOT FOUND
    public static BaseResponse<?> actionNotFound() {
        return new BaseResponse<>(StatusEnum.FAIL, null, ResultCodeEnum.NOT_FOUND, "데이터를 찾지 못했습니다.");
    }

    // INDEX
    public static BaseResponse<?> actionMain() {
        return new BaseResponse<>(StatusEnum.SUCCESS, "Nice to meet you stranger!", ResultCodeEnum.SUCCESS, null);
    }

    // SERVER_ERROR
    public static BaseResponse<?> actionError() {
        return new BaseResponse<>(StatusEnum.ERROR, null, ResultCodeEnum.INTERNAL_SERVER_ERROR, "서비스를 이용하실 수 없습니다.");
    }

    // UNAUTHORIZED
    public static BaseResponse<?> actionUnAuthorized() {
        return new BaseResponse<>(StatusEnum.FAIL, null, ResultCodeEnum.UNAUTHORIZED, "인증이 필요한 서비스입니다.");
    }
}