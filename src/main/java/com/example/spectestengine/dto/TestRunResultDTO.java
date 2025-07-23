package com.example.spectestengine.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record TestRunResultDTO(Long runId, Long specId, String overalTestStatus, JsonNode log, LocalDateTime startedAt, LocalDateTime finishedAt) {

}