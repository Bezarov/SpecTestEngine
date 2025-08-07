package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.model.SpecFormat;
import com.example.spectestengine.service.TestRunService;
import com.example.spectestengine.validation.annotation.ValidSpecId;
import com.example.spectestengine.validation.annotation.ValidSpecName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test/run")
public class TestRunController {
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: '{}'";

    private final TestRunService testRunService;

    public TestRunController(TestRunService testRunService) {
        this.testRunService = testRunService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestRunDTO>> runAll(@RequestParam(defaultValue = "JSON") String format) {
        log.debug("Received GET request to RUN all tests specification ");
        List<TestRunDTO> resultDTOS = testRunService.runAllTestsSpec();
        MediaType requestedMediaType = SpecFormat.getMediaType(format);
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok().contentType(requestedMediaType).body(resultDTOS);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestRunDTO> runById(@PathVariable @ValidSpecId Long specId) {
        log.debug("Received GET request to RUN test specification with id: '{}'", specId);
        TestRunDTO resultDTO = testRunService.runTestBySpecId(specId);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok().contentType(resultDTO.mediaType()).body(resultDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestRunDTO> runByName(@PathVariable @ValidSpecName String specName) {
        log.debug("Received GET request to RUN test specification with name: '{}'", specName);
        TestRunDTO resultDTO = testRunService.runTestWithSpecName(specName);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok().contentType(resultDTO.mediaType()).body(resultDTO);
    }

    @GetMapping("/in-range")
    public ResponseEntity<List<TestRunDTO>> runInRange(@RequestParam @ValidSpecId Long fromId,
                                                       @RequestParam @ValidSpecId Long toId,
                                                       @RequestParam(defaultValue = "JSON") String format) {
        log.debug("Received GET request to RUN in range tests specification from id: '{}', to id: '{}'", fromId, toId);
        if (fromId > toId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'fromId' must be less or equal than 'toId'");
        }

        List<TestRunDTO> resultDTOS = testRunService.runTestsInSpecRangeId(fromId, toId);
        MediaType requestedMediaType = SpecFormat.getMediaType(format);
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok().contentType(requestedMediaType).body(resultDTOS);
    }
}
