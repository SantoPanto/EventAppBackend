package com.works.configs;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SessionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SessionFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String urlPath = request.getRequestURI();

        // 🔹 Projemize Özgü İzin Verilen (Session İstemeyen) Yollar
        String[] freeUrls = {
                "/customer/login",
                "/customer/register",
                "/h2-console",       // Veritabanı yönetim ekranı
                "/v3/api-docs",      // Swagger JSON verisi
                "/swagger-ui"        // Swagger Arayüzü
        };

        boolean isAuth = true;
        for (String freeUrl : freeUrls) {
            if (urlPath.startsWith(freeUrl)) {
                isAuth = false;
                break;
            }
        }

        // 🔹 CLIENT BİLGİLERİ (Güvenlik Loglaması için Harika Bir Pratik)
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        HttpSession session = request.getSession(false);
        Object customer = (session != null) ? session.getAttribute("customer") : null;

        // ✅ INFO LOG (Her gelen isteği konsola yazdırır, testlerde çok işe yarar)
        logger.info("""
                ====== REQUEST ======
                Time      : {}
                IP        : {}
                Method    : {}
                URL       : {}
                Session   : {}
                =====================
                """, time, ipAddress, method, urlPath,
                (session != null ? session.getId() : "No Session")
        );

        // 🔐 AUTH KONTROL (Eğer serbest bir yol değilse ve kullanıcı yoksa)
        if (isAuth && customer == null) {
            logger.warn("Unauthorized API access blocked -> IP: {}, URL: {}", ipAddress, urlPath);

            // Projemiz REST API olduğu için doğrudan JSON hatası dönüyoruz
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String jsonResponse = """
                    {
                      "success": false,
                      "message": "Yetkisiz işlem! Lütfen önce giriş yapınız."
                    }
                    """;
            response.getWriter().write(jsonResponse);

            return; // İstediği Controller'a gitmesini ENGELLE ve işlemi kes!
        }

        // Her şey yolundaysa zincirin (isteğin) devam etmesine izin ver
        filterChain.doFilter(request, response);
    }

    // IP tespit metodu (Proxy arkasından geliyorsa gerçek IP'yi bulur)
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}