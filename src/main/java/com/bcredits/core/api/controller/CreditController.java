package com.bcredits.core.api.controller;

import com.bcredits.core.api.dto.CreditRequestDTO;
import com.bcredits.core.api.dto.CreditResponseDTO;
import com.bcredits.core.domain.service.CreditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditController {
    
    private final CreditService service;
    
 
    public CreditController(CreditService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<CreditResponseDTO> create(@RequestBody @Valid CreditRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }
    
    @GetMapping
    public ResponseEntity<List<CreditResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CreditResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CreditResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid CreditRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}