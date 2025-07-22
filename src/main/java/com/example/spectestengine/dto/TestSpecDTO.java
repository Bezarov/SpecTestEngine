package com.example.spectestengine.dto;

import java.time.LocalDateTime;

public record TestSpecDTO(Long id, String name, String spec, LocalDateTime createdAt) {

}