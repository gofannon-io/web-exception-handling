package io.gofannon.webexceptionhandling;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessVerifierFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getRequestURI().toLowerCase().contains("secret2")) {
            throw new UnauthorizedAccessException("Stop ! This access is forbidden");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
