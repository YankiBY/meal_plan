package com.example.mealplan.controller;

import com.example.mealplan.dto.ProgressDto;
import com.example.mealplan.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<List<ProgressDto>> getProgress(Authentication authentication) {
        return ResponseEntity.ok(progressService.getProgress(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ProgressDto> recordProgress(Authentication authentication,
                                                       @RequestBody ProgressDto dto) {
        return ResponseEntity.ok(progressService.recordProgress(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgressDto> updateProgress(Authentication authentication,
                                                       @PathVariable Long id,
                                                       @RequestBody ProgressDto dto) {
        return ResponseEntity.ok(progressService.updateProgress(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(Authentication authentication, @PathVariable Long id) {
        progressService.deleteProgress(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/period")
    public ResponseEntity<List<ProgressDto>> getProgressForPeriod(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(progressService.getProgressForPeriod(authentication.getName(), start, end));
    }
}
