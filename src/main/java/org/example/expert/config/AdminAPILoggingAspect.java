package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AdminAPILoggingAspect {

    private final ObjectMapper objectMapper;

    public AdminAPILoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    public Object logAdminApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        // 요청 및 응답 정보 로깅
        HttpServletRequest request = getHttpServletRequest();
        HttpServletResponse response = getHttpServletResponse();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 요청 본문 로깅
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        log.info("Admin API Request: Time={}, URL={}, UserId={}, Body={}",
                LocalDateTime.now(), request.getRequestURI(), request.getHeader("User-Id"), requestBody);

        Object result = joinPoint.proceed(); // 실제 메서드 실행

        // 응답 본문 로깅
        String responseBody = new String(wrappedResponse.getContentAsByteArray());
        log.info("Admin API Response: Time={}, URL={}, UserId={}, Body={}",
                LocalDateTime.now(), request.getRequestURI(), request.getHeader("User-Id"), responseBody);

        wrappedResponse.copyBodyToResponse(); // 응답 본문을 클라이언트에 전달

        return result;
    }

    private HttpServletRequest getHttpServletRequest() {
        // HttpServletRequest 가져오는 로직
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private HttpServletResponse getHttpServletResponse() {
        // HttpServletResponse 가져오는 로직
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }
}
