package com.example.spectestengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

public record TestSpecWithRunsDTO(Long id,
                                  String name,
                                  Object spec,
                                  @JsonIgnore
                                  MediaType mediaType,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                                  LocalDateTime createdAt,
                                  List<TestRunDTO> runs) {
}