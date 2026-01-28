package com.bcredits.core.api.dto;

import com.bcredits.core.domain.model.CreditStatus;
import com.bcredits.core.domain.model.CreditType;  

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditResponseDTO(
        Long id,
        String customerName,
        BigDecimal amount,
        CreditType type,  
        CreditStatus status,
        LocalDateTime createdAt
) {}