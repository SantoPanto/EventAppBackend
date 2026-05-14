package com.works.dto;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.works.entity.Customer}
 */
@Data
public class CustomerResponseDto {
    Integer cid;
    String name;
    String surname;
    String email;
    String phone;
}