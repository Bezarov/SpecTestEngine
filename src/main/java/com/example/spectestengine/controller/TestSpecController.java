package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.service.HTTPTestSpecService;
import com.example.spectestengine.validation.SpecValidator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test/spec")
@Slf4j
public class TestSpecController {
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: '{}'";

    private final HTTPTestSpecService httpTestSpecService;

    public TestSpecController(HTTPTestSpecService httpTestSpecService) {
        this.httpTestSpecService = httpTestSpecService;
    }

    @PostMapping("/create")
    public ResponseEntity<TestSpecDTO> createSpec(
            @RequestParam
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            @Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$", message = "Spec name can only contain letters, numbers, spaces, hyphens and underscores")
            String specName,

            @RequestBody
            @NotBlank(message = "Specification JSON cannot be blank")
            @Size(min = 10, max = 10000, message = "Specification JSON must be between 10 and 10000 characters")
            String specJson) {
        log.debug("Received POST request to create test with name: '{}' and specification: '{}'", specName, specJson);
        SpecValidator.validate(specJson);

        TestSpecDTO testSpecDTO = httpTestSpecService.createSpec(specName, specJson);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestSpecDTO> getSpecById(
            @PathVariable
            @NotNull(message = "Spec ID cannot be null")
            @Positive(message = "Spec ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "Spec ID is too large")
            Long specId) {
        log.debug("Received GET request to get test specification with id: '{}'", specId);
        TestSpecDTO testSpecDTO = httpTestSpecService.getSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestSpecDTO> getSpecByName(
            @PathVariable
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            String specName) {
        log.debug("Received GET request to get test specification with name: '{}'", specName);
        TestSpecDTO testSpecDTO = httpTestSpecService.getSpecByName(specName);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @GetMapping
    public ResponseEntity<List<TestSpecDTO>> getAll() {
        log.debug("Received GET request to get all test specification");
        List<TestSpecDTO> specDTOList = httpTestSpecService.getAllTestSpec();
        log.debug(RESPONSE_LOG, specDTOList);
        return ResponseEntity.ok(specDTOList);
    }

    @GetMapping("/with-runs/{specId}")
    public ResponseEntity<TestSpecWithRunsDTO> getSpecWithRunsById(
            @PathVariable
            @NotNull(message = "Spec ID cannot be null")
            @Positive(message = "Spec ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "Spec ID is too large")
            Long specId) {
        log.debug("Received GET request to get test specification with run results: '{}'", specId);
        TestSpecWithRunsDTO testSpecWithRunsDTO = httpTestSpecService.getSpecWithRuns(specId);
        log.debug(RESPONSE_LOG, testSpecWithRunsDTO);
        return ResponseEntity.ok(testSpecWithRunsDTO);
    }

    @PutMapping("/by-id")
    public ResponseEntity<TestSpecDTO> updateSpecById(
            @RequestParam
            @NotNull(message = "Spec ID cannot be null")
            @Positive(message = "Spec ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "Spec ID is too large")
            Long specId,

            @RequestBody
            @NotBlank(message = "Specification JSON cannot be blank")
            @Size(min = 10, max = 10000, message = "Specification JSON must be between 10 and 10000 characters")
            String specJson) {
        log.debug("Received PUT request to update test specification: '{}' with spec id: '{}'", specJson, specId);
        SpecValidator.validate(specJson);

        TestSpecDTO testSpecEntity = httpTestSpecService.updateSpecById(specId, specJson);
        log.debug(RESPONSE_LOG, testSpecEntity);
        return ResponseEntity.ok(testSpecEntity);
    }

    @PutMapping("/by-name")
    public ResponseEntity<TestSpecDTO> updateSpecByName(
            @RequestParam
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            String specName,

            @RequestBody
            @NotBlank(message = "Specification JSON cannot be blank")
            @Size(min = 10, max = 10000, message = "Specification JSON must be between 10 and 10000 characters")
            String specJson) {
        log.debug("Received PUT request to update test specification: '{}' with spec name: '{}'", specJson, specName);
        SpecValidator.validate(specJson);

        TestSpecDTO testSpecEntity = httpTestSpecService.updateSpecByName(specName, specJson);
        log.debug(RESPONSE_LOG, testSpecEntity);
        return ResponseEntity.ok(testSpecEntity);
    }

    @DeleteMapping("/by-id")
    public ResponseEntity<TestSpecDTO> deleteSpecById(
            @RequestParam
            @NotNull(message = "Spec ID cannot be null")
            @Positive(message = "Spec ID must be positive")
            @Max(value = Long.MAX_VALUE, message = "Spec ID is too large")
            Long specId) {
        log.debug("Received DELETE request to remove test specification with spec id: '{}'", specId);
        TestSpecDTO testSpecDTO = httpTestSpecService.deleteSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<TestSpecDTO> deleteSpecByName(
            @RequestParam
            @NotBlank(message = "Spec name cannot be blank")
            @Size(min = 1, max = 255, message = "Spec name must be between 1 and 255 characters")
            String specName) {
        log.debug("Received DELETE request to remove test specification with spec name: '{}'", specName);
        TestSpecDTO testSpecDTO = httpTestSpecService.deleteSpecByName(specName);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }
}