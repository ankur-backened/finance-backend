package com.zorvyn.finance_backend.interceptor;

import com.zorvyn.finance_backend.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // allow browser preflight requests to pass through
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        String role = request.getHeader("X-User-Role");
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (role == null || role.isBlank()) {
            throw new AccessDeniedException("X-User-Role header is missing");
        }

        role = role.toUpperCase().trim();

        switch (role) {
            case "VIEWER":
                if (!method.equalsIgnoreCase("GET")) {
                    throw new AccessDeniedException(
                            "VIEWER role is not allowed to create, update or delete records"
                    );
                }
                break;

            case "ANALYST":
                boolean isGet = method.equalsIgnoreCase("GET");
                boolean isCreateRecord = method.equalsIgnoreCase("POST")
                        && path.startsWith("/api/records");

                if (!isGet && !isCreateRecord) {
                    throw new AccessDeniedException(
                            "ANALYST role can only view data and create financial records"
                    );
                }
                break;

            case "ADMIN":
                break;

            default:
                throw new AccessDeniedException(
                        "Invalid role: " + role + ". Accepted roles are ADMIN, ANALYST, VIEWER"
                );
        }

        return true;
    }
}