package ru.mirea.project.auth.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.stereotype.Component;

import ru.mirea.project.common.exceptions.GlobalExceptionHandler.ApiError;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityErrorHandler {

    private final JsonMapper jsonMapper;

    private final BearerTokenAuthenticationEntryPoint entryPoint =
            new BearerTokenAuthenticationEntryPoint();

    private final BearerTokenAccessDeniedHandler deniedHandler =
            new BearerTokenAccessDeniedHandler();

    public SecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void handleUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        entryPoint.commence(request, response, exception);

        HttpStatus status =
                HttpStatus.valueOf(response.getStatus());

        writeError(
                request,
                response,
                status,
                "Отсутствует или недействителен токен авторизации."
        );
    }

    public void handleForbidden(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {

        deniedHandler.handle(request, response, exception);

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean passwordChangeRequired =
                authentication != null &&
                        authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority.getAuthority().equals(
                                                "PASSWORD_CHANGE_REQUIRED"
                                        )
                                );

        String message = passwordChangeRequired
                ? "Необходимо сменить временный пароль."
                : "Недостаточно прав для выполнения операции.";

        writeError(
                request,
                response,
                HttpStatus.FORBIDDEN,
                message
        );
    }

    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        jsonMapper.writeValue(
                response.getWriter(),
                error
        );
    }
}
