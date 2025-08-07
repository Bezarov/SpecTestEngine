package com.example.spectestengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

public record TestSpecDTO(Long id,
                          String name,
                          @JsonIgnore
                          MediaType mediaType,
                          Object spec,
                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                          LocalDateTime createdAt) {
}