package com.example.spectestengine.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TestSpecWithRunsDTO(Long id, String name, String spec, LocalDateTime createdAt, List<TestRunDTO> runs) {

}