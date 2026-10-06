package org.example.firstpetprojectexchanger.exception;

import org.springframework.http.HttpStatus;


public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
/*
ConflictException
+ConflictException(String) «409»
 */