package com.example.mealplan.service;

import com.example.mealplan.dto.ProgressDto;
import com.example.mealplan.entity.User;
import com.example.mealplan.entity.UserProgress;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.UserProgressRepository;
import com.example.mealplan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserProgressRepository progressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressService progressService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded")
                .roles(new HashSet<>())
                .build();
    }

    @Test
    void getProgress_returnsEntries() {
        UserProgress entry = UserProgress.builder()
                .id(1L).user(testUser).date(LocalDate.now())
                .weight(80.0).caloriesConsumed(2000.0)
                .proteinsConsumed(150.0).fatsConsumed(70.0).carbsConsumed(250.0)
                .planComplied(true).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findByUserId(1L)).thenReturn(List.of(entry));

        List<ProgressDto> result = progressService.getProgress("testuser");

        assertEquals(1, result.size());
        assertEquals(80.0, result.get(0).getWeight());
        assertEquals(2000.0, result.get(0).getCaloriesConsumed());
        assertTrue(result.get(0).isPlanComplied());
    }

    @Test
    void recordProgress_createsEntry() {
        ProgressDto dto = new ProgressDto();
        dto.setDate(LocalDate.of(2025, 1, 15));
        dto.setWeight(79.5);
        dto.setCaloriesConsumed(2100.0);
        dto.setProteinsConsumed(160.0);
        dto.setFatsConsumed(75.0);
        dto.setCarbsConsumed(260.0);
        dto.setPlanComplied(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(inv -> {
            UserProgress up = inv.getArgument(0);
            up.setId(1L);
            return up;
        });

        ProgressDto result = progressService.recordProgress("testuser", dto);

        assertNotNull(result);
        assertEquals(79.5, result.getWeight());
        assertEquals(LocalDate.of(2025, 1, 15), result.getDate());
        verify(progressRepository).save(any(UserProgress.class));
    }

    @Test
    void recordProgress_noDate_usesToday() {
        ProgressDto dto = new ProgressDto();
        dto.setWeight(80.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(inv -> {
            UserProgress up = inv.getArgument(0);
            up.setId(1L);
            return up;
        });

        ProgressDto result = progressService.recordProgress("testuser", dto);

        assertEquals(LocalDate.now(), result.getDate());
    }

    @Test
    void updateProgress_ownerCanUpdate() {
        UserProgress existing = UserProgress.builder()
                .id(1L).user(testUser).date(LocalDate.now())
                .weight(80.0).planComplied(false).build();

        ProgressDto dto = new ProgressDto();
        dto.setWeight(79.0);
        dto.setPlanComplied(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any(UserProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressDto result = progressService.updateProgress("testuser", 1L, dto);

        assertEquals(79.0, result.getWeight());
        assertTrue(result.isPlanComplied());
    }

    @Test
    void updateProgress_notOwner_throws() {
        User otherUser = User.builder().id(2L).username("other").build();
        UserProgress existing = UserProgress.builder()
                .id(1L).user(otherUser).date(LocalDate.now())
                .weight(80.0).build();

        ProgressDto dto = new ProgressDto();
        dto.setWeight(79.0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> progressService.updateProgress("testuser", 1L, dto));
    }

    @Test
    void deleteProgress_ownerCanDelete() {
        UserProgress existing = UserProgress.builder()
                .id(1L).user(testUser).date(LocalDate.now()).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findById(1L)).thenReturn(Optional.of(existing));

        progressService.deleteProgress("testuser", 1L);

        verify(progressRepository).delete(existing);
    }

    @Test
    void deleteProgress_notOwner_throws() {
        User otherUser = User.builder().id(2L).username("other").build();
        UserProgress existing = UserProgress.builder()
                .id(1L).user(otherUser).date(LocalDate.now()).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> progressService.deleteProgress("testuser", 1L));
        verify(progressRepository, never()).delete(any());
    }

    @Test
    void getProgressForPeriod_filtersCorrectly() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(progressRepository.findByUserIdAndDateBetween(1L, start, end)).thenReturn(List.of());

        List<ProgressDto> result = progressService.getProgressForPeriod("testuser", start, end);

        assertTrue(result.isEmpty());
        verify(progressRepository).findByUserIdAndDateBetween(1L, start, end);
    }

    @Test
    void getProgress_unknownUser_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> progressService.getProgress("nobody"));
    }
}
