package com.example.mealplan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String unit;

    private Double calories; // per 100g/unit
    private Double proteins;
    private Double fats;
    private Double carbohydrates;

    private String barcode; // Use Case 15
}
