package com.example.spectestengine.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record TestSpecDTO(Long id, String name, JsonNode spec, LocalDateTime createdAt) {

}