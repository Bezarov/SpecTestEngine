package com.example.spectestengine.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(LocalDateTime timestamp,
                               String error,
                               String message,
                               String path) {
}