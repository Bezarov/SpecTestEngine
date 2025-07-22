package com.example.spectestengine.dto;

import java.time.LocalDateTime;

public record TestRunDTO(Long id, String status, String log, LocalDateTime startedAt, LocalDateTime finishedAt) {

}