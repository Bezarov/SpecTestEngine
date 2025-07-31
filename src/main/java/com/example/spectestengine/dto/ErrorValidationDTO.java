package com.example.spectestengine.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ErrorValidationDTO(LocalDateTime timestamp, String error, Set<String> message, String path) {
}
