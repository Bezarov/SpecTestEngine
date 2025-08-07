package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.model.SpecFormat;
import com.example.spectestengine.service.TestSpecService;
import com.example.spectestengine.validation.annotation.ValidSpecId;
import com.example.spectestengine.validation.annotation.ValidSpec;
import com.example.spectestengine.validation.annotation.ValidSpecName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

@Slf4j
@RestController
@RequestMapping("/test/spec")
public class TestSpecController {
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: '{}'";

    private final TestSpecService testSpecService;

    public TestSpecController(TestSpecService testSpecService) {
        this.testSpecService = testSpecService;
    }

    @PostMapping("/create")
    public ResponseEntity<TestSpecDTO> createSpec(@RequestParam @ValidSpecName String specName,
                                                  @RequestBody @ValidSpec String rawSpec) {
        log.debug("Received POST request to create test with name: '{}' and specification: '{}'", specName, rawSpec);
        TestSpecDTO testSpecDTO = testSpecService.createSpec(specName, rawSpec);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestSpecDTO> getSpecById(@PathVariable @ValidSpecId Long specId) {
        log.debug("Received GET request to get test specification with id: '{}'", specId);
        TestSpecDTO testSpecDTO = testSpecService.getSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestSpecDTO> getSpecByName(@PathVariable @ValidSpecName String specName) {
        log.debug("Received GET request to get test specification with name: '{}'", specName);
        TestSpecDTO testSpecDTO = testSpecService.getSpecByName(specName);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @GetMapping
    public ResponseEntity<List<TestSpecDTO>> getAll(@RequestParam(defaultValue = "JSON") String format) {
        log.debug("Received GET request to get all test specification");
        List<TestSpecDTO> specDTOList = testSpecService.getAllTestSpec();
        MediaType requestedMediaType = SpecFormat.getMediaType(format);
        log.debug(RESPONSE_LOG, specDTOList);
        return ResponseEntity.ok().contentType(requestedMediaType).body(specDTOList);
    }

    @GetMapping("/with-runs/{specId}")
    public ResponseEntity<TestSpecWithRunsDTO> getSpecWithRunsById(@PathVariable @ValidSpecId Long specId) {
        log.debug("Received GET request to get test specification with run results: '{}'", specId);
        TestSpecWithRunsDTO testSpecWithRunsDTO = testSpecService.getSpecWithRuns(specId);
        log.debug(RESPONSE_LOG, testSpecWithRunsDTO);
        return ResponseEntity.ok().contentType(testSpecWithRunsDTO.mediaType()).body(testSpecWithRunsDTO);
    }

    @PutMapping("/by-id")
    public ResponseEntity<TestSpecDTO> updateSpecById(@RequestParam @ValidSpecId Long specId,
                                                      @RequestBody @ValidSpec String rawSpec) {
        log.debug("Received PUT request to update test specification: '{}' with spec id: '{}'", rawSpec, specId);
        TestSpecDTO testSpecDTO = testSpecService.updateSpecById(specId, rawSpec);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @PutMapping("/by-name")
    public ResponseEntity<TestSpecDTO> updateSpecByName(@RequestParam @ValidSpecName String specName,
                                                        @RequestBody @ValidSpec String rawSpec) {
        log.debug("Received PUT request to update test specification: '{}' with spec name: '{}'", rawSpec, specName);
        TestSpecDTO testSpecDTO = testSpecService.updateSpecByName(specName, rawSpec);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @DeleteMapping("/by-id")
    public ResponseEntity<TestSpecDTO> deleteSpecById(@RequestParam @ValidSpecId Long specId) {
        log.debug("Received DELETE request to remove test specification with spec id: '{}'", specId);
        TestSpecDTO testSpecDTO = testSpecService.deleteSpecById(specId);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<TestSpecDTO> deleteSpecByName(@RequestParam @ValidSpecName String specName) {
        log.debug("Received DELETE request to remove test specification with spec name: '{}'", specName);
        TestSpecDTO testSpecDTO = testSpecService.deleteSpecByName(specName);
        log.debug(RESPONSE_LOG, testSpecDTO);
        return ResponseEntity.ok().contentType(testSpecDTO.mediaType()).body(testSpecDTO);
    }
}