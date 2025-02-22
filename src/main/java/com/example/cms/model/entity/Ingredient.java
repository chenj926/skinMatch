package com.example.cms.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "ingredients")
public class Ingredient{
    @Id
    @NotEmpty
    private Long ingredientId;

    @NotEmpty
    private String ingredientName;

    //Many-to-Many relationship with Products
    @ManyToMany(mappedBy = "ingredients")
    private List<Product> products  = new ArrayList<>();

    //Many-to-Many relationship with TestResults
    @ManyToMany(mappedBy = "avoidIngredients")
    private List<TestResults> testResults  = new ArrayList<>();

}
