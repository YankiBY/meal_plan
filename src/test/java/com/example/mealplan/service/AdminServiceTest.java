package com.example.mealplan.service;

import com.example.mealplan.dto.UserDto;
import com.example.mealplan.entity.*;
import com.example.mealplan.exception.ResourceNotFoundException;
import com.example.mealplan.mapper.RecipeMapper;
import com.example.mealplan.mapper.UserMapper;
import com.example.mealplan.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private AllergenRepository allergenRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private MealPlanRepository mealPlanRepository;
    @Mock private UserMapper userMapper;
    @Mock private RecipeMapper recipeMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User testUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("ROLE_USER").build();
        adminRole = Role.builder().id(2L).name("ROLE_ADMIN").build();
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded")
                .blocked(false)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    @Test
    void getAllUsers_returnsMappedDtos() {
        UserDto dto = new UserDto();
        dto.setUsername("testuser");
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(dto);

        List<UserDto> result = adminService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void blockUser_setsBlockedTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.blockUser(1L);

        assertTrue(testUser.isBlocked());
        verify(userRepository).save(testUser);
    }

    @Test
    void unblockUser_setsBlockedFalse() {
        testUser.setBlocked(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.unblockUser(1L);

        assertFalse(testUser.isBlocked());
        verify(userRepository).save(testUser);
    }

    @Test
    void blockUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminService.blockUser(99L));
    }

    @Test
    void changeUserRole_changesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        adminService.changeUserRole(1L, "ROLE_ADMIN");

        assertEquals(1, testUser.getRoles().size());
        assertTrue(testUser.getRoles().contains(adminRole));
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_encodesAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");

        adminService.resetPassword(1L, "newpass");

        assertEquals("encoded_new", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_exists_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);
        adminService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notExists_throws() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteUser(99L));
    }

    @Test
    void moderateRecipe_approve_setsModerated() {
        Recipe recipe = Recipe.builder().id(1L).title("Test").isModerated(false).build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        adminService.moderateRecipe(1L, true);

        assertTrue(recipe.isModerated());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void moderateRecipe_reject_deletesRecipe() {
        Recipe recipe = Recipe.builder().id(1L).title("Test").isModerated(false).build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        adminService.moderateRecipe(1L, false);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void createDisease_saves() {
        Disease disease = Disease.builder().name("Test Disease").build();
        when(diseaseRepository.save(disease)).thenReturn(disease);

        Disease result = adminService.createDisease(disease);

        assertNotNull(result);
        verify(diseaseRepository).save(disease);
    }

    @Test
    void updateDisease_updatesFields() {
        Disease existing = Disease.builder().id(1L).name("Old").description("Old desc").build();
        Disease update = Disease.builder().name("New").description("New desc").build();

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(diseaseRepository.save(existing)).thenReturn(existing);

        Disease result = adminService.updateDisease(1L, update);

        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
    }

    @Test
    void deleteDisease_notFound_throws() {
        when(diseaseRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteDisease(99L));
    }
}
