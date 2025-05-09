package com.example.cms.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.example.cms.model.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "products")
public class Product {

    @Id
    @NotNull
    // not sure if we need it
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private long productId;

    @NotEmpty
    private String name;

    @NotEmpty
    private String brand;

    @NotNull
    private Double price;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    @NotEmpty
    private String imageURL;

    @Transient
    private Double averageScore;

    private String description;

    // Add @JsonIgnore to break the circular reference
    // Many-to-Many relationship with TestResults
    @ManyToMany(mappedBy = "recommendedProducts")
    @JsonIgnore
    private List<TestResults> testResults  = new ArrayList<>();

    //Many-to-Many relationship with Ingredients
    @ManyToMany
    @JoinTable(
            name = "ProductIngredients",
            joinColumns = @JoinColumn(name = "productId"),
            inverseJoinColumns = @JoinColumn(name = "ingredientId")
    )
    private List<Ingredient> ingredients  = new ArrayList<>();

    //Many-to-Many relationship with Concerns
    @ManyToMany
    @JoinTable(
            name = "ProductConcerns",
            joinColumns = @JoinColumn(name = "productId"),
            inverseJoinColumns = @JoinColumn(name = "concernId")
    )
    private List<Concern> concerns;


    //Many-to-Many relationship with Skintype
    @ManyToMany
    @JoinTable(
            name = "userSkintype",
            joinColumns = @JoinColumn(name = "productId"),
            inverseJoinColumns = @JoinColumn(name = "skintypeId")
    )
    private List<Skintype> skintypes;

    @ManyToMany
    @JoinTable(
            name = "product_skintype",
            joinColumns = @JoinColumn(name = "productId"),
            inverseJoinColumns = @JoinColumn(name = "skintypeId")
    )
    private List<Skintype> skinTypesProduct = new ArrayList<>();

    //Many-to-Many relationship with products
    @JsonBackReference
    @ManyToMany(mappedBy = "favourites")
    private Set<User> users = new HashSet<>();

    public Product(long productId, String name, String brand, Double price,
                   Category category, String type, String imageURL, List<TestResults> testResults,
                   List<Ingredient> ingredients, List<Concern> concerns, List<Skintype> skintypes) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.category = category;
        this.imageURL = imageURL;
        this.testResults = testResults;
        this.ingredients = ingredients;
        this.concerns = concerns;
        this.skintypes = skintypes;
        this.averageScore = getAverageScore();

    }

}
