package com.example.mealplan.service;

import com.example.mealplan.dto.HealthProfileDto;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthProfileServiceTest {

    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DiseaseRepository diseaseRepository;
    @Mock
    private AllergenRepository allergenRepository;

    @InjectMocks
    private HealthProfileService healthProfileService;

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
    void getProfile_existingProfile_returnsDto() {
        HealthProfile profile = HealthProfile.builder()
                .id(1L)
                .user(testUser)
                .weight(80.0)
                .height(180.0)
                .age(30)
                .gender("MALE")
                .activityLevel("MODERATE")
                .dailyCalorieTarget(2759.0)
                .dailyProteinTarget(206.93)
                .dailyFatTarget(91.97)
                .dailyCarbTarget(275.9)
                .diseases(new HashSet<>())
                .allergens(new HashSet<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        HealthProfileDto result = healthProfileService.getProfile("testuser");

        assertNotNull(result);
        assertEquals(80.0, result.getWeight());
        assertEquals(180.0, result.getHeight());
        assertEquals(30, result.getAge());
        assertEquals("MALE", result.getGender());
        assertEquals("MODERATE", result.getActivityLevel());
        assertEquals(2759.0, result.getDailyCalorieTarget());
    }

    @Test
    void getProfile_noProfile_returnsEmptyDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        HealthProfileDto result = healthProfileService.getProfile("testuser");

        assertNotNull(result);
        assertNotNull(result.getDiseaseIds());
        assertNotNull(result.getAllergenIds());
        assertTrue(result.getDiseaseIds().isEmpty());
    }

    @Test
    void getProfile_unknownUser_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> healthProfileService.getProfile("unknown"));
    }

    @Test
    void updateProfile_male_moderate_bmrCorrect() {
        HealthProfileDto dto = new HealthProfileDto();
        dto.setWeight(80.0);
        dto.setHeight(180.0);
        dto.setAge(30);
        dto.setGender("MALE");
        dto.setActivityLevel("MODERATE");
        dto.setDiseaseIds(new HashSet<>());
        dto.setAllergenIds(new HashSet<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> {
            HealthProfile saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

        assertNotNull(result);
        // BMR = 10*80 + 6.25*180 - 5*30 + 5 = 1780; TDEE = 1780 * 1.55 = 2759
        assertEquals(2759.0, result.getDailyCalorieTarget());
        // Proteins: 2759 * 0.3 / 4 = 206.925 → rounds to 206.93
        assertEquals(206.93, result.getDailyProteinTarget(), 0.01);
        // Fats: 2759 * 0.3 / 9 = 91.9667 → rounds to 91.97
        assertEquals(91.97, result.getDailyFatTarget(), 0.01);
        // Carbs: 2759 * 0.4 / 4 = 275.9
        assertEquals(275.9, result.getDailyCarbTarget(), 0.01);
    }

    @Test
    void updateProfile_female_sedentary_bmrCorrect() {
        HealthProfileDto dto = new HealthProfileDto();
        dto.setWeight(60.0);
        dto.setHeight(165.0);
        dto.setAge(25);
        dto.setGender("FEMALE");
        dto.setActivityLevel("SEDENTARY");
        dto.setDiseaseIds(new HashSet<>());
        dto.setAllergenIds(new HashSet<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> {
            HealthProfile saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

        // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        // TDEE = 1345.25 * 1.2 = 1614.3
        assertEquals(1614.3, result.getDailyCalorieTarget(), 0.01);
    }

    @Test
    void updateProfile_withDiseaseMultiplier() {
        Disease diabetes = Disease.builder()
                .id(1L)
                .name("Diabetes")
                .recommendedCaloriesMultiplier(0.9)
                .build();

        HealthProfileDto dto = new HealthProfileDto();
        dto.setWeight(80.0);
        dto.setHeight(180.0);
        dto.setAge(30);
        dto.setGender("MALE");
        dto.setActivityLevel("MODERATE");
        dto.setDiseaseIds(Set.of(1L));
        dto.setAllergenIds(new HashSet<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(diseaseRepository.findAllById(Set.of(1L))).thenReturn(List.of(diabetes));
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> {
            HealthProfile saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

        // TDEE = 2759 * 0.9 = 2483.1
        assertEquals(2483.1, result.getDailyCalorieTarget(), 0.01);
    }

    @Test
    void updateProfile_existingProfile_updatesFields() {
        HealthProfile existing = HealthProfile.builder()
                .id(1L)
                .user(testUser)
                .weight(70.0)
                .height(170.0)
                .age(25)
                .gender("MALE")
                .activityLevel("LIGHT")
                .diseases(new HashSet<>())
                .allergens(new HashSet<>())
                .build();

        HealthProfileDto dto = new HealthProfileDto();
        dto.setWeight(80.0);
        dto.setHeight(180.0);
        dto.setAge(30);
        dto.setGender("MALE");
        dto.setActivityLevel("MODERATE");
        dto.setDiseaseIds(new HashSet<>());
        dto.setAllergenIds(new HashSet<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

        assertEquals(80.0, result.getWeight());
        assertEquals(180.0, result.getHeight());
        assertEquals(30, result.getAge());
        verify(healthProfileRepository).save(existing);
    }

    @Test
    void updateProfile_allActivityLevels() {
        Map<String, Double> expectedMultipliers = Map.of(
                "SEDENTARY", 1.2,
                "LIGHT", 1.375,
                "MODERATE", 1.55,
                "ACTIVE", 1.725,
                "VERY_ACTIVE", 1.9
        );

        for (Map.Entry<String, Double> entry : expectedMultipliers.entrySet()) {
            HealthProfileDto dto = new HealthProfileDto();
            dto.setWeight(80.0);
            dto.setHeight(180.0);
            dto.setAge(30);
            dto.setGender("MALE");
            dto.setActivityLevel(entry.getKey());
            dto.setDiseaseIds(new HashSet<>());
            dto.setAllergenIds(new HashSet<>());

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> {
                HealthProfile saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

            double expectedBmr = 10 * 80 + 6.25 * 180 - 5 * 30 + 5; // 1780
            double expectedTdee = Math.round(expectedBmr * entry.getValue() * 100.0) / 100.0;
            assertEquals(expectedTdee, result.getDailyCalorieTarget(), 0.01,
                    "Activity level " + entry.getKey() + " should yield TDEE " + expectedTdee);
        }
    }

    @Test
    void updateProfile_nullWeightHeightAge_noCalculation() {
        HealthProfileDto dto = new HealthProfileDto();
        dto.setGender("MALE");
        dto.setActivityLevel("MODERATE");
        dto.setDiseaseIds(new HashSet<>());
        dto.setAllergenIds(new HashSet<>());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(inv -> {
            HealthProfile saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        HealthProfileDto result = healthProfileService.updateProfile("testuser", dto);

        assertNull(result.getDailyCalorieTarget());
    }
}
