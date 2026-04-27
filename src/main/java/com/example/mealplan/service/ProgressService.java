package com.example.mealplan.service;

import com.example.mealplan.dto.ProgressDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.UserProgress;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.UserProgressRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;

    public List<ProgressDto> getProgress(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return progressRepository.findByUserId(user.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgressDto recordProgress(String username, ProgressDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        UserProgress progress = UserProgress.builder()
                .user(user)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .weight(dto.getWeight())
                .caloriesConsumed(dto.getCaloriesConsumed())
                .proteinsConsumed(dto.getProteinsConsumed())
                .fatsConsumed(dto.getFatsConsumed())
                .carbsConsumed(dto.getCarbsConsumed())
                .planComplied(dto.isPlanComplied())
                .build();

        return toDto(progressRepository.save(progress));
    }

    @Transactional
    public ProgressDto updateProgress(String username, Long id, ProgressDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        UserProgress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись прогресса не найдена"));

        if (!progress.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к этой записи");
        }

        progress.setDate(dto.getDate() != null ? dto.getDate() : progress.getDate());
        progress.setWeight(dto.getWeight());
        progress.setCaloriesConsumed(dto.getCaloriesConsumed());
        progress.setProteinsConsumed(dto.getProteinsConsumed());
        progress.setFatsConsumed(dto.getFatsConsumed());
        progress.setCarbsConsumed(dto.getCarbsConsumed());
        progress.setPlanComplied(dto.isPlanComplied());

        return toDto(progressRepository.save(progress));
    }

    @Transactional
    public void deleteProgress(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        UserProgress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись прогресса не найдена"));

        if (!progress.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к этой записи");
        }

        progressRepository.delete(progress);
    }

    public List<ProgressDto> getProgressForPeriod(String username, LocalDate start, LocalDate end) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return progressRepository.findByUserIdAndDateBetween(user.getId(), start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProgressDto toDto(UserProgress progress) {
        ProgressDto dto = new ProgressDto();
        dto.setId(progress.getId());
        dto.setDate(progress.getDate());
        dto.setWeight(progress.getWeight());
        dto.setCaloriesConsumed(progress.getCaloriesConsumed());
        dto.setProteinsConsumed(progress.getProteinsConsumed());
        dto.setFatsConsumed(progress.getFatsConsumed());
        dto.setCarbsConsumed(progress.getCarbsConsumed());
        dto.setPlanComplied(progress.isPlanComplied());
        return dto;
    }
}
