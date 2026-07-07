package kepco.prorject.ictyb.back.ictyb_back.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private String message;
    private JwtUserDto user; //로그인한 사용자정보
}