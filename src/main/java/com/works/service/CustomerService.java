package com.works.service;

import com.works.dto.CustomerLoginRequestDto;
import com.works.dto.CustomerRegisterRequestDto;
import com.works.dto.CustomerResponseDto;
import com.works.entity.Customer;
import com.works.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CustomerService {

    final CustomerRepository customerRepository;
    final HttpServletRequest request;
    final ModelMapper model;

    public ResponseEntity register(CustomerRegisterRequestDto customerRegisterRequestDto){
        List<Customer> customerList = customerRepository.findByEmailEqualsOrPhoneEqualsAllIgnoreCase(customerRegisterRequestDto.getEmail(), customerRegisterRequestDto.getPhone());
        if(customerList.size() > 0){
            // daha önceden bu email veya phone kullanılmış demektir.
            Map<String, Object> hm = Map.of("success", false, "message", "This email or phone number is already in use.");
            return ResponseEntity.badRequest().body(hm);
        }
        Customer customer = model.map(customerRegisterRequestDto, Customer.class);
        String hashPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt());
        customer.setPassword(hashPassword);
        customerRepository.save(customer);
        return ResponseEntity.ok().body(customer);
    }

    // login
    public ResponseEntity login(CustomerLoginRequestDto customerLoginRequestDto){
        System.out.println("CustomerService - " +  model.hashCode());
        Optional<Customer> optionalCustomer = customerRepository.findByEnabledTrueAndEmailIgnoreCaseOrEnabledTrueAndPhoneIgnoreCase(customerLoginRequestDto.getUsername(), customerLoginRequestDto.getUsername());
        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();
            boolean isMatch = BCrypt.checkpw(customerLoginRequestDto.getPassword(), customer.getPassword());
            if(isMatch){
                CustomerResponseDto customerResponseDto = model.map(customer, CustomerResponseDto.class);
                request.getSession().setAttribute("customer", customerResponseDto);
                return ResponseEntity.ok().body(customerResponseDto);
            }
        }
        Map<String, Object> hm = Map.of("success", false, "message", "Username or password is incorrect.");
        return ResponseEntity.badRequest().body(hm);
    }

    // customer logout
    public ResponseEntity logout(){
        request.getSession().invalidate();
        return ResponseEntity.ok().body("Logout successfully.");
    }


}
