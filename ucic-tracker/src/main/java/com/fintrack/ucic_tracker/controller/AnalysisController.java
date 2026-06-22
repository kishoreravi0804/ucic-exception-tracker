package com.fintrack.ucic_tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrack.ucic_tracker.entity.AnalysisRun;
import com.fintrack.ucic_tracker.service.AnalysisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AnalysisController {

    private final AnalysisService analysisService;

    
    @PostMapping("/run")
    public ResponseEntity<AnalysisRun> runAnalysis() {
        log.info("Analysis triggered via API");
        AnalysisRun result = analysisService.runAnalysis();
        return ResponseEntity.ok(result);
    }
}
