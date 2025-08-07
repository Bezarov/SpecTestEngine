package com.example.spectestengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

public record TestRunDTO(Long runId,
                         Long specId,
                         @JsonIgnore
                         MediaType mediaType,
                         String overallTestStatus,
                         JsonNode testResultLog,
                         @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                         LocalDateTime startedAt,
                         @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                         LocalDateTime finishedAt) {
}