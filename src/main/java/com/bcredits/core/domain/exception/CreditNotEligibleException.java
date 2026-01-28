package com.bcredits.core.domain.exception;

public class CreditNotEligibleException extends RuntimeException {

    public CreditNotEligibleException(String message) {
        super(message);
    }
}
