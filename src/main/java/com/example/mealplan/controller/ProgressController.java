package com.example.mealplan.controller;

import com.example.mealplan.entity.UserProgress;
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
    public ResponseEntity<List<UserProgress>> getProgress(Authentication authentication) {
        return ResponseEntity.ok(progressService.getProgress(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<UserProgress> recordProgress(Authentication authentication, @RequestBody UserProgress progress) {
        return ResponseEntity.ok(progressService.recordProgress(authentication.getName(), progress));
    }

    @GetMapping("/period")
    public ResponseEntity<List<UserProgress>> getProgressForPeriod(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(progressService.getProgressForPeriod(authentication.getName(), start, end));
    }
}
