package com.bcredits.core.domain.service;

import com.bcredits.core.api.dto.CreditRequestDTO;
import com.bcredits.core.api.dto.CreditResponseDTO;
import com.bcredits.core.domain.mapper.CreditMapper;
import com.bcredits.core.domain.model.CreditApplication;
import com.bcredits.core.domain.model.CreditStatus;
import com.bcredits.core.infrastructure.repository.CreditRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {
    
    private final CreditRepository repository;
    private final CreditMapper mapper;
    
    @Value("${credit.auto-eval.max-amount}")
    private BigDecimal maxAutoEvalAmount;
    
    @Transactional
    public CreditResponseDTO create(CreditRequestDTO dto) {
        log.info("Processing credit application for: {}", dto.customerName());
        
        CreditApplication entity = mapper.toEntity(dto);
        CreditStatus evaluatedStatus = evaluateCreditEligibility(dto.amount());
        entity.setStatus(evaluatedStatus);
        
        CreditApplication saved = repository.save(entity);
        
        log.info("Credit application processed - ID: {}, Amount: ${}, Status: {}", 
                 saved.getId(), dto.amount(), evaluatedStatus);
        
        return mapper.toResponse(saved);
    }
    
    public List<CreditResponseDTO> findAll() {
        log.debug("Fetching all credit applications");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    public CreditResponseDTO findById(Long id) {
        log.debug("Fetching credit application with ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> {
                    log.error("Credit application not found with ID: {}", id);
                    return new EntityNotFoundException("Credit application not found with ID: " + id);
                });
    }
    
    @Transactional
    public CreditResponseDTO update(Long id, CreditRequestDTO dto) {
        log.info("Updating credit application ID: {}", id);
        
        CreditApplication existing = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Credit application not found with ID: {}", id);
                    return new EntityNotFoundException("Credit application not found with ID: " + id);
                });
        
        // Actualizar campos
        existing.setCustomerName(dto.customerName());
        existing.setAmount(dto.amount());
        existing.setType(dto.type());
        
        // Re-evaluar si cambió el monto
        CreditStatus newStatus = evaluateCreditEligibility(dto.amount());
        existing.setStatus(newStatus);
        
        CreditApplication updated = repository.save(existing);
        
        log.info("Credit application updated - ID: {}, New Status: {}", id, newStatus);
        
        return mapper.toResponse(updated);
    }
    
    @Transactional
    public void delete(Long id) {
        log.info("Deleting credit application ID: {}", id);
        
        if (!repository.existsById(id)) {
            log.error("Credit application not found with ID: {}", id);
            throw new EntityNotFoundException("Credit application not found with ID: " + id);
        }
        
        repository.deleteById(id);
        log.info("Credit application deleted successfully - ID: {}", id);
    }
    
    private CreditStatus evaluateCreditEligibility(BigDecimal amount) {
        boolean isEligible = amount.compareTo(maxAutoEvalAmount) <= 0;
        log.debug("Credit evaluation - Amount: ${}, Limit: ${}, Eligible: {}", 
                  amount, maxAutoEvalAmount, isEligible);
        return isEligible ? CreditStatus.APPROVED : CreditStatus.REJECTED;
    }
}