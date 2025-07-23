package com.example.spectestengine.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public record TestSpecWithRunsDTO(Long id, String name, JsonNode spec, LocalDateTime createdAt, List<TestRunDTO> runs) {

}