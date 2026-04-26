package com.example.mealplan.service;

import com.example.mealplan.entity.User;
import com.example.mealplan.entity.UserProgress;
import com.example.mealplan.repository.UserProgressRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;

    public List<UserProgress> getProgress(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return progressRepository.findByUserId(user.getId());
    }

    @Transactional
    public UserProgress recordProgress(String username, UserProgress progress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        progress.setUser(user);
        if (progress.getDate() == null) {
            progress.setDate(LocalDate.now());
        }
        return progressRepository.save(progress);
    }

    public List<UserProgress> getProgressForPeriod(String username, LocalDate start, LocalDate end) {
        // In a real app, this would be a custom query in the repository
        return getProgress(username).stream()
                .filter(p -> !p.getDate().isBefore(start) && !p.getDate().isAfter(end))
                .toList();
    }
}
