package com.works.controller;

import com.works.dto.CustomerLoginRequestDto;
import com.works.dto.CustomerRegisterRequestDto;
import com.works.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody CustomerLoginRequestDto dto, HttpServletRequest request) {
        ResponseEntity response = customerService.login(dto);

        // Eğer giriş başarılıysa
        if (response.getStatusCode().is2xxSuccessful()) {
            // Kullanıcı bilgilerini Session'a kaydediyoruz
            request.getSession().setAttribute("customer", response.getBody());
        }

        return response;
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody CustomerRegisterRequestDto dto) {
        return customerService.register(dto);
    }

    @GetMapping("/check-session")
    public ResponseEntity checkSession(HttpServletRequest request) {
        // getSession(false) -> Eğer mevcut bir oturum yoksa yenisini oluşturmaz, null döner
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("customer") != null) {
            // Oturum hala aktif, kullanıcının bilgilerini geri dön
            return ResponseEntity.ok(session.getAttribute("customer"));
        }

        // Oturum düşmüş veya geçersiz
        return ResponseEntity.status(401).body("Oturum süresi dolmuş veya geçersiz.");
    }

    @GetMapping("/logout")
    public ResponseEntity logout() {
        return customerService.logout();
    }
}