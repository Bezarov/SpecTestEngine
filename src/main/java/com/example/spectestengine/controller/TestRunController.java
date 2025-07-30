package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.service.HTTPTestSpecService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/test/run")
@Slf4j
public class TestRunController {
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: '{}'";

    private final HTTPTestSpecService httpTestSpecService;

    public TestRunController(HTTPTestSpecService httpTestSpecService) {
        this.httpTestSpecService = httpTestSpecService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestRunResultDTO>> runAll() {
        log.debug("Received GET request to RUN all tests specification ");
        List<TestRunResultDTO> resultDTOS = httpTestSpecService.runAllTestsSpec();
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok(resultDTOS);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestRunResultDTO> runById(
            @PathVariable
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            @Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$", message = "Spec name can only contain letters, numbers, spaces, hyphens and underscores")
            Long specId) {
        log.debug("Received GET request to RUN test specification with id: '{}'", specId);
        TestRunResultDTO resultDTO = httpTestSpecService.runTestBySpecId(specId);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestRunResultDTO> runByName(
            @PathVariable
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            @Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$", message = "Spec name can only contain letters, numbers, spaces, hyphens and underscores")
            String specName) {
        log.debug("Received GET request to RUN test specification with name: '{}'", specName);
        TestRunResultDTO resultDTO = httpTestSpecService.runTestWithSpecName(specName);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/in-range")
    public ResponseEntity<List<TestRunResultDTO>> runInRange(
            @RequestParam
            @NotNull(message = "From ID cannot be null")
            @Positive(message = "From ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "From ID is too large")
            Long fromId,

            @RequestParam
            @NotNull(message = "To ID cannot be null")
            @Positive(message = "To ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "To ID is too large")
            Long toId) {
        log.debug("Received GET request to RUN in range tests specification from id: '{}', to id: '{}'", fromId, toId);
        if (fromId > toId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'fromId' must be less or equal than 'toId'");
        }

        List<TestRunResultDTO> resultDTOS = httpTestSpecService.runTestsInSpecRangeId(fromId, toId);
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok(resultDTOS);
    }
}
