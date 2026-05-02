package com.example.mealplan.repository;

import com.example.mealplan.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
    List<UserProgress> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    Optional<UserProgress> findByUserIdAndDate(Long userId, LocalDate date);
}
