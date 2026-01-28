package com.bcredits.core.domain.mapper;

import com.bcredits.core.api.dto.CreditRequestDTO;
import com.bcredits.core.api.dto.CreditResponseDTO;
import com.bcredits.core.domain.model.CreditApplication;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditApplication toEntity(CreditRequestDTO dto) {
        if (dto == null) return null;
        
        return CreditApplication.builder()
                .customerName(dto.customerName())
                .amount(dto.amount())
                .type(dto.type())
                .build();
    }

    public CreditResponseDTO toResponse(CreditApplication entity) {
        if (entity == null) return null;

        return new CreditResponseDTO(
                entity.getId(),
                entity.getCustomerName(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}