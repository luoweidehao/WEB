package com.acc.lab.exception;

import com.acc.lab.dto.MessageResponse;

public class TooManyRequestsException extends RuntimeException {
    private final MessageResponse response;
    
    public TooManyRequestsException(MessageResponse response) {
        super(response.getMessage());
        this.response = response;
    }
    
    public MessageResponse getResponse() {
        return response;
    }
}

