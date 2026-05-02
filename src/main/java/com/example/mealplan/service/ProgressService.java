package com.example.mealplan.service;

import com.example.mealplan.dto.ProgressDto;
import com.example.mealplan.dto.ProgressCalendarDayDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.UserProgress;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.HealthProfileRepository;
import com.example.mealplan.repository.UserProgressRepository;
import com.example.mealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mealplan.entity.Recipe;
import com.example.mealplan.repository.RecipeRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final HealthProfileRepository healthProfileRepository;

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

    public List<ProgressCalendarDayDto> getCalendar(String username, LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new RuntimeException("Некорректный период");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Map<LocalDate, UserProgress> byDate = progressRepository.findByUserIdAndDateBetween(user.getId(), start, end)
                .stream()
                .collect(Collectors.toMap(UserProgress::getDate, Function.identity(), (a, b) -> a));

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        return java.util.stream.LongStream.range(0, days)
                .mapToObj(i -> start.plusDays(i))
                .map(date -> {
                    UserProgress p = byDate.get(date);
                    if (p == null) {
                        return new ProgressCalendarDayDto(date, false, false, null);
                    }
                    return new ProgressCalendarDayDto(date, true, p.isPlanComplied(), p.getWeight());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgressDto addRecipeToProgress(String username, Long recipeId, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт не найден"));

        Double currentWeight = healthProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getWeight())
                .orElse(null);

        UserProgress progress = progressRepository.findByUserIdAndDate(user.getId(), date)
                .orElseGet(() -> UserProgress.builder()
                        .user(user)
                        .date(date)
                        .caloriesConsumed(0.0)
                        .proteinsConsumed(0.0)
                        .fatsConsumed(0.0)
                        .carbsConsumed(0.0)
                        .weight(currentWeight)
                        .build());

        progress.setCaloriesConsumed(Optional.ofNullable(progress.getCaloriesConsumed()).orElse(0.0) + recipe.getTotalCalories());
        progress.setProteinsConsumed(Optional.ofNullable(progress.getProteinsConsumed()).orElse(0.0) + recipe.getTotalProteins());
        progress.setFatsConsumed(Optional.ofNullable(progress.getFatsConsumed()).orElse(0.0) + recipe.getTotalFats());
        progress.setCarbsConsumed(Optional.ofNullable(progress.getCarbsConsumed()).orElse(0.0) + recipe.getTotalCarbohydrates());

        return toDto(progressRepository.save(progress));
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
