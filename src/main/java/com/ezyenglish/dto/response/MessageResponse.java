package com.ezyenglish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic message response for API operations.
 */
@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
