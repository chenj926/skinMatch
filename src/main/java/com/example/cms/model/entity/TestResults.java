package com.example.cms.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "testResults")
public class TestResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testResultId;

    // One-to-one relationship with User - TestResults owns the relationship
    // Use @JsonIgnore for user to break circular reference
    @OneToOne
    @JsonIgnore
    private User user;

    // Many-to-many relationship with Product
    @ManyToMany
    @JoinTable(
            name = "ProductRecommendations",
            joinColumns = @JoinColumn(name = "testResultId"),
            inverseJoinColumns = @JoinColumn(name = "productId")
    )
    private List<Product> recommendedProducts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "userSkincareConcerns",
            joinColumns = @JoinColumn(name = "testResultId"),
            inverseJoinColumns = @JoinColumn(name = "concernId")
    )
    private List<Concern> concerns = new ArrayList<>();

    // Many-to-Many relationship with Ingredients
    @ManyToMany
    @JoinTable(
            name = "userAvoidList",
            joinColumns = @JoinColumn(name = "testResultId"),
            inverseJoinColumns = @JoinColumn(name = "avoidIngredientId")
    )
    private List<Ingredient> avoidIngredients = new ArrayList<>();

    // User's budget
    @NotNull
    private Float budget;

    // User's skin type (oily, dry, combination)
    @ManyToOne
    @JoinColumn(name="skintypeId")
    private Skintype skinType;

    public TestResults(Long testResultId, User user, List<Product> recommendedProducts, List<Concern> concerns,
                       List<Ingredient> avoidIngredients, Float budget, Skintype skinType) {
        this.testResultId = testResultId;
        this.recommendedProducts = recommendedProducts;
        this.concerns = concerns;
        this.avoidIngredients = avoidIngredients;
        this.budget = budget;
        this.skinType = skinType;
    }

}
