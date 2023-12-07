package kr.bb.apigateway.common;

import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import javax.security.sasl.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.bb.apigateway.common.util.ExtractAuthorizationTokenUtil;
import kr.bb.apigateway.common.util.JwtUtil;
import kr.bb.apigateway.common.util.RedisBlackListTokenUtil;
import kr.bb.apigateway.common.valueobject.JWTAuthenticationShouldNotFilterAntMatcher;
import kr.bb.apigateway.common.valueobject.KakaoOAuthURLAntURI;
import kr.bb.apigateway.common.valueobject.SwaggerRequestURI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final RedisBlackListTokenUtil redisBlackListTokenUtil;

  private boolean shouldNotFilterSwaggerURI(HttpServletRequest request)  {
    String requestURI = request.getRequestURI();
    return requestURI.contains(SwaggerRequestURI.UI_URI) || requestURI.contains(SwaggerRequestURI.API_DOCS_URI)
        || requestURI.contains(SwaggerRequestURI.WEB_JARS) || requestURI.contains(SwaggerRequestURI.FAVICON)
        || requestURI.contains(SwaggerRequestURI.RESOURCES);
  }

  private boolean shouldNotFilterKakaoOauth2(HttpServletRequest request)  {

    String requestURI = request.getRequestURI();
    return requestURI.contains(KakaoOAuthURLAntURI.KAPI)
        || requestURI.contains(KakaoOAuthURLAntURI.KAUTH) || requestURI.contains(KakaoOAuthURLAntURI.REDIRECT)
        || requestURI.contains(KakaoOAuthURLAntURI.OAUTH);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request)  {
    String requestURI = request.getRequestURI();
    return shouldNotFilterSwaggerURI(request) || shouldNotFilterKakaoOauth2(request)
        || requestURI.contains(JWTAuthenticationShouldNotFilterAntMatcher.SIGNUP_ANT)
        || requestURI.contains(JWTAuthenticationShouldNotFilterAntMatcher.LOGIN_ANT)
        || requestURI.contains(JWTAuthenticationShouldNotFilterAntMatcher.EMAIL_ANT);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = ExtractAuthorizationTokenUtil.extractToken(request);
    if (redisBlackListTokenUtil.isTokenBlacklisted(token)) {
      throw new AuthenticationException("해당 토큰은 이미 로그아웃 처리된 토큰이라 사용할 수 없는 토큰입니다.");
    }
    try {
      JwtUtil.isTokenValid(token);
    } catch (ExpiredJwtException e) {
      throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "만료된 토큰입니다. Refresh토큰을 확인하세요");
    }
    filterChain.doFilter(request, response);
  }




}