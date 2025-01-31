package com.paulcera.bentabox.core.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseMessage {

    private String message;

    private Object payload;

    public ResponseMessage(String message) {
        this(message, null);
    }

    public ResponseMessage(String message, Object payload) {
        this.message = message;
        this.payload = payload;
    }
}
