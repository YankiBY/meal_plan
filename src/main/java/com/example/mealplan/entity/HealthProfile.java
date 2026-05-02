package com.example.mealplan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private String activityLevel;

    private Double dailyCalorieTarget;
    private Double dailyProteinTarget;
    private Double dailyFatTarget;
    private Double dailyCarbTarget;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
        name = "profile_diseases",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    @Builder.Default
    private java.util.Set<Disease> diseases = new java.util.HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "profile_allergens",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    @Builder.Default
    private java.util.Set<Allergen> allergens = new java.util.HashSet<>();
}
