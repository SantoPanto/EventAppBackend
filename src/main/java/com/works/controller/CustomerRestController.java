package com.works.controller;

import com.works.dto.CustomerLoginRequestDto;
import com.works.dto.CustomerRegisterRequestDto;
import com.works.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody CustomerRegisterRequestDto dto) {
        return customerService.register(dto);
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody CustomerLoginRequestDto dto) {
        return customerService.login(dto);
    }

    @GetMapping("/logout")
    public ResponseEntity logout() {
        return customerService.logout();
    }
}