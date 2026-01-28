package com.bcredits.core.api.dto;

import com.bcredits.core.domain.model.CreditType;  
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditRequestDTO(
        
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String customerName,
        
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "100.00", message = "El monto mínimo es $100.00")
        @Positive(message = "El monto debe ser positivo")
        BigDecimal amount,
        
        @NotNull(message = "El tipo de crédito es obligatorio")
        CreditType type 
) {}