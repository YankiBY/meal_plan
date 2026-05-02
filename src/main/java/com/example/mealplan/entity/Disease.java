package com.example.mealplan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diseases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Recommendations for this disease
    private Double recommendedCaloriesMultiplier;
}
