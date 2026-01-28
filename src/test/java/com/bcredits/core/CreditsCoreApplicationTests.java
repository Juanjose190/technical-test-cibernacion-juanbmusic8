package com.bcredits.core;

import com.bcredits.core.api.dto.CreditRequestDTO;
import com.bcredits.core.api.dto.CreditResponseDTO;
import com.bcredits.core.domain.mapper.CreditMapper;
import com.bcredits.core.domain.model.CreditApplication;
import com.bcredits.core.domain.model.CreditStatus;
import com.bcredits.core.domain.model.CreditType;
import com.bcredits.core.domain.service.CreditService;
import com.bcredits.core.infrastructure.repository.CreditRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Credit Service Unit Tests")
class CreditServiceTest {

    @Mock
    private CreditRepository repository;

    @Mock
    private CreditMapper mapper;

    @InjectMocks
    private CreditService service;

    private static final BigDecimal MAX_AUTO_AMOUNT = new BigDecimal("50000");

    @BeforeEach
    void setUp() {
       
        ReflectionTestUtils.setField(service, "maxAutoEvalAmount", MAX_AUTO_AMOUNT);
    }

  

    @Test
    @DisplayName("Should auto-approve when amount is within limits")
    void create_ShouldApprove_WhenAmountIsLow() {
   
        CreditRequestDTO request = new CreditRequestDTO("Juan B", new BigDecimal("4500"), CreditType.PERSONAL);
        CreditApplication pendingEntity = buildEntity(request, CreditStatus.PENDING);
        
        when(mapper.toEntity(request)).thenReturn(pendingEntity);
        when(repository.save(any(CreditApplication.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any(CreditApplication.class))).thenAnswer(i -> buildResponse(i.getArgument(0)));


        CreditResponseDTO response = service.create(request);

     
        assertThat(response.status()).isEqualTo(CreditStatus.APPROVED);
        assertThat(response.amount()).isEqualByComparingTo(request.amount());
        
        verify(repository).save(argThat(credit -> credit.getStatus() == CreditStatus.APPROVED));
    }

    @Test
    @DisplayName("Should reject when amount exceeds global limit")
    void create_ShouldReject_WhenAmountExceedsLimit() {
       
        BigDecimal highAmount = MAX_AUTO_AMOUNT.add(BigDecimal.ONE);
        CreditRequestDTO request = new CreditRequestDTO("Empresa Cambridge", highAmount, CreditType.BUSINESS);
        CreditApplication entity = buildEntity(request, CreditStatus.PENDING);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(i -> buildResponse(i.getArgument(0)));

     
        CreditResponseDTO response = service.create(request);

 
        assertThat(response.status()).isEqualTo(CreditStatus.REJECTED);
        verify(repository).save(argThat(c -> c.getStatus() == CreditStatus.REJECTED));
    }

    @Test
    @DisplayName("Should handle exact limit amount correctly")
    void create_ShouldApprove_WhenAmountEqualsLimit() {

        CreditRequestDTO request = new CreditRequestDTO(
            "Edge Case User", 
            MAX_AUTO_AMOUNT,
            CreditType.PERSONAL
        );
        CreditApplication entity = buildEntity(request, CreditStatus.PENDING);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(i -> buildResponse(i.getArgument(0)));


        CreditResponseDTO response = service.create(request);


        assertThat(response.status()).isEqualTo(CreditStatus.APPROVED);
        assertThat(response.amount()).isEqualByComparingTo(MAX_AUTO_AMOUNT);
    }



    @Test
    @DisplayName("Should return all credit applications")
    void findAll_ShouldReturnAllApplications() {
 
        CreditApplication app1 = buildEntityWithId(1L, "User 1", new BigDecimal("30000"), CreditStatus.APPROVED);
        CreditApplication app2 = buildEntityWithId(2L, "User 2", new BigDecimal("60000"), CreditStatus.REJECTED);
        
        when(repository.findAll()).thenReturn(List.of(app1, app2));
        when(mapper.toResponse(any())).thenAnswer(i -> buildResponse(i.getArgument(0)));


        List<CreditResponseDTO> results = service.findAll();


        assertThat(results).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should return credit application by ID")
    void findById_ShouldReturnApplication_WhenIdExists() {
     
        Long existingId = 1L;
        CreditApplication entity = buildEntityWithId(existingId, "Test User", new BigDecimal("25000"), CreditStatus.APPROVED);
        
        when(repository.findById(existingId)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(buildResponse(entity));

       
        CreditResponseDTO response = service.findById(existingId);


        assertThat(response.id()).isEqualTo(existingId);
        assertThat(response.customerName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when ID does not exist")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        
        Long invalidId = 99L;
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

   
        assertThatThrownBy(() -> service.findById(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("ID: " + invalidId);
    }

   

    @Test
    @DisplayName("Should update credit application successfully")
    void update_ShouldUpdateAndReEvaluate_WhenValidRequest() {
     
        Long existingId = 1L;
        CreditRequestDTO updateRequest = new CreditRequestDTO(
            "Updated Name", 
            new BigDecimal("25000"), 
            CreditType.BUSINESS
        );
        
        CreditApplication existing = buildEntityWithId(
            existingId, 
            "Old Name", 
            new BigDecimal("60000"), 
            CreditStatus.REJECTED
        );
        
        when(repository.findById(existingId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toResponse(any())).thenAnswer(i -> buildResponse(i.getArgument(0)));
        
     
        CreditResponseDTO response = service.update(existingId, updateRequest);
        
       
        assertThat(response.customerName()).isEqualTo("Updated Name");
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(response.status()).isEqualTo(CreditStatus.APPROVED); // Re-evaluado
        
        verify(repository).save(argThat(c -> 
            c.getCustomerName().equals("Updated Name") && 
            c.getStatus() == CreditStatus.APPROVED
        ));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent credit")
    void update_ShouldThrowException_WhenIdDoesNotExist() {
      
        Long invalidId = 99L;
        CreditRequestDTO updateRequest = new CreditRequestDTO("Test", new BigDecimal("30000"), CreditType.PERSONAL);
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

    
        assertThatThrownBy(() -> service.update(invalidId, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("ID: " + invalidId);
    }



    @Test
    @DisplayName("Should delete credit application successfully")
    void delete_ShouldRemoveEntity_WhenIdExists() {
       
        Long existingId = 1L;
        when(repository.existsById(existingId)).thenReturn(true);
        
   
        service.delete(existingId);
        

        verify(repository).deleteById(existingId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent credit")
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
 
        Long invalidId = 99L;
        when(repository.existsById(invalidId)).thenReturn(false);
        

        assertThatThrownBy(() -> service.delete(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("ID: " + invalidId);
        
        verify(repository, never()).deleteById(any());
    }

   

    private CreditApplication buildEntity(CreditRequestDTO req, CreditStatus status) {
        return CreditApplication.builder()
                .customerName(req.customerName())
                .amount(req.amount())
                .type(req.type())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreditApplication buildEntityWithId(Long id, String name, BigDecimal amount, CreditStatus status) {
        return CreditApplication.builder()
                .id(id)
                .customerName(name)
                .amount(amount)
                .type(CreditType.PERSONAL)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreditResponseDTO buildResponse(CreditApplication entity) {
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