package com.works.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.works.entity.Customer}
 */
@Data
public class CustomerLoginRequestDto {
    @NotNull
    @NotEmpty
    @Size(min = 5, max = 100)
    String username;
    @NotNull
    @Size(min = 6, max = 15)
    @NotEmpty
    String password;
}