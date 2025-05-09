package com.example.cms.model.service;

import com.example.cms.controller.Dto.ProductDto;
import com.example.cms.model.entity.Product;
import com.example.cms.model.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductRepository reviewRepository;

    //------------Product search method I: Java filter-----------
    //Return list of filtered product DTOs
    public List<ProductDto> getFilteredProductsI(
            Double maxPrice, String sortBy, List<Integer> categories,
            List<String> brands, List<Integer> types, List<Long> avoidIngredients, List<Integer> concerns) {

        //Fetch all products from the database
        List<Product> products = productRepository.findAllProductsSorted(sortBy);

        return products.stream()

                 //Filter products that are <= maximum price
                .filter(product -> (maxPrice == null || product.getPrice() <= maxPrice))

                //Filter products that have matching category
                .filter(product -> (categories == null || categories.isEmpty() || categories.contains(product.getCategory().getCategoryId())))

                //Filter products that belong to any of the brands on the list
                .filter(product -> (brands == null || brands.isEmpty() || brands.contains(product.getBrand())))

                //Filter products that belong to any of the specified types
                .filter(product -> (types == null || types.isEmpty() || product.getSkintypes().stream()
                        .anyMatch(type -> types.contains(type.getSkintypeId()))))

                //Filter products that match at least one/any of the concerns
                .filter(product -> (concerns == null || concerns.isEmpty() || product.getConcerns().stream()
                        .anyMatch(concern -> concerns.contains(concern.getConcernId()))))

                //Exclude products that contain any of the specified avoid ingredients
                .filter(product -> (avoidIngredients == null || avoidIngredients.isEmpty() || product.getIngredients().stream()
                        .noneMatch(ingredient -> avoidIngredients.contains(ingredient.getIngredientId()))))

                //Convert each Product entity to a ProductDto before returning the response
                .map(product -> {
                    product.setAverageScore(productRepository.findAverageScoreByProductId(product.getProductId()));

                    return new ProductDto(
                            product.getProductId(),
                            product.getName(),
                            product.getBrand(),
                            product.getPrice(),
                            product.getImageURL(),
                            product.getIngredients(),
                            product.getAverageScore()
                    );
                })

                //Collect the results into a list
                .collect(Collectors.toList());
    }
    //------------Product search method II: SQL filter-----------
//    public List<ProductDto> getFilteredProductsII(
//            Double maxPrice, String sortBy, Integer category,
//            List<String> brands, List<String> types, List<Long> avoidIngredients, List<Integer> concerns) {
//
//        //Call repository method to filter via SQL
//        List<Product> products = productRepository.findFilteredProducts(
//                maxPrice, brands, category, types, concerns, avoidIngredients, sortBy
//        );
//
//        //Convert each Product entity to a ProductDto
//        return products.stream()
//                .map(product -> {
//                    product.setAverageScore(productRepository.findAverageScoreByProductId(product.getProductId()));
//                    return new ProductDto(
//                            product.getProductId(),
//                            product.getName(),
//                            product.getBrand(),
//                            product.getPrice(),
//                            product.getImageURL(),
//                            product.getIngredients(),
//                            product.getAverageScore()
//                    );
//                })
//                //Create list
//                .collect(Collectors.toList());
//    }

}
