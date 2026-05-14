package com.works.controller;

import com.works.dto.CustomerLoginRequestDto;
import com.works.dto.CustomerRegisterRequestDto;
import com.works.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/logout")
    public ResponseEntity logout() {
        return customerService.logout();
    }
}