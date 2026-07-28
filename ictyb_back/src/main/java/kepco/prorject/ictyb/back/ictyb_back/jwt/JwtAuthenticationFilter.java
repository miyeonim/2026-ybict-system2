package kepco.prorject.ictyb.back.ictyb_back.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kepco.prorject.ictyb.back.ictyb_back.jwt.JwtTokenProvider;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
         
        System.out.println("doFilterInternal 호출 ========================");
        String token = resolveToken(request);

        if (token != null) {
            try {
                // 토큰이 유효하지 않으면 여기서 예외 발생 (만료/변조 등)
                JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userInfo, null, Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 유효하지 않은 토큰 → 인증 세팅 없이 그냥 넘어감
                // (SecurityConfig에서 anyRequest().authenticated()로 막혀있으면 자동으로 401/403 처리됨)
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}