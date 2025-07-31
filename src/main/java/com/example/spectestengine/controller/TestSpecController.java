package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.service.HTTPTestSpecService;
import com.example.spectestengine.validation.annotation.ValidSpecId;
import com.example.spectestengine.validation.annotation.ValidSpecJson;
import com.example.spectestengine.validation.annotation.ValidSpecName;
import com.example.spectestengine.validation.validator.SpecValidator;
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
    public ResponseEntity<TestSpecDTO> createSpec(@RequestParam @ValidSpecName String specName,
                                                  @RequestBody @ValidSpecJson String specJson) {
        log.debug("Received POST request to create test with name: '{}' and specification: '{}'", specName, specJson);
        TestSpecDTO testSpecDTO = httpTestSpecService.createSpec(specName, specJson);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestSpecDTO> getSpecById(@PathVariable @ValidSpecId Long specId) {
        log.debug("Received GET request to get test specification with id: '{}'", specId);
        TestSpecDTO testSpecDTO = httpTestSpecService.getSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestSpecDTO> getSpecByName(@PathVariable @ValidSpecName String specName) {
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
    public ResponseEntity<TestSpecWithRunsDTO> getSpecWithRunsById(@PathVariable @ValidSpecId Long specId) {
        log.debug("Received GET request to get test specification with run results: '{}'", specId);
        TestSpecWithRunsDTO testSpecWithRunsDTO = httpTestSpecService.getSpecWithRuns(specId);
        log.debug(RESPONSE_LOG, testSpecWithRunsDTO);
        return ResponseEntity.ok(testSpecWithRunsDTO);
    }

    @PutMapping("/by-id")
    public ResponseEntity<TestSpecDTO> updateSpecById(@RequestParam @ValidSpecId Long specId,
                                                      @RequestBody @ValidSpecJson String specJson) {
        log.debug("Received PUT request to update test specification: '{}' with spec id: '{}'", specJson, specId);
        TestSpecDTO testSpecEntity = httpTestSpecService.updateSpecById(specId, specJson);
        log.debug(RESPONSE_LOG, testSpecEntity);
        return ResponseEntity.ok(testSpecEntity);
    }

    @PutMapping("/by-name")
    public ResponseEntity<TestSpecDTO> updateSpecByName(@RequestParam @ValidSpecName String specName,
                                                        @RequestBody @ValidSpecJson String specJson) {
        log.debug("Received PUT request to update test specification: '{}' with spec name: '{}'", specJson, specName);
        TestSpecDTO testSpecEntity = httpTestSpecService.updateSpecByName(specName, specJson);
        log.debug(RESPONSE_LOG, testSpecEntity);
        return ResponseEntity.ok(testSpecEntity);
    }

    @DeleteMapping("/by-id")
    public ResponseEntity<TestSpecDTO> deleteSpecById(@RequestParam @ValidSpecId Long specId) {
        log.debug("Received DELETE request to remove test specification with spec id: '{}'", specId);
        TestSpecDTO testSpecDTO = httpTestSpecService.deleteSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<TestSpecDTO> deleteSpecByName(@RequestParam @ValidSpecName String specName) {
        log.debug("Received DELETE request to remove test specification with spec name: '{}'", specName);
        TestSpecDTO testSpecDTO = httpTestSpecService.deleteSpecByName(specName);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok(testSpecDTO);
    }
}