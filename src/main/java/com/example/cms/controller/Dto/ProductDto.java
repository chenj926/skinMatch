package com.example.cms.controller.Dto;

import com.example.cms.model.entity.Ingredient;
import com.example.cms.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class ProductDto {
    private long id;
    private String name;
    private String brand;
    private Double price;
    private String imageURL;
    private List<Ingredient> ingredients;
    private Double averageScore;

    public static ProductDto fromEntity(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getName(),
                product.getBrand(),
                product.getPrice(),
                product.getImageURL(),
                product.getIngredients(),
                product.getAverageScore()
        );
    }
}
