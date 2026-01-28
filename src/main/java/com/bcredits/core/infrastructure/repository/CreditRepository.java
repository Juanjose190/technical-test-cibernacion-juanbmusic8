package com.bcredits.core.infrastructure.repository;

import com.bcredits.core.domain.model.CreditApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRepository extends JpaRepository<CreditApplication, Long> {
}
