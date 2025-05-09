package com.example.cms.controller;

import com.example.cms.controller.Dto.ProductDto;
import com.example.cms.controller.Dto.TestResultsResponseDto;
import com.example.cms.controller.exceptions.UserNotFoundException;
import com.example.cms.model.entity.*;
import com.example.cms.model.repository.*;
import com.example.cms.controller.Dto.TestResultsDto;
import com.example.cms.model.service.SkinCareRountineService;
import com.example.cms.model.service.SkinCareRoutineService2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@CrossOrigin
@RestController
@RequestMapping("/test-results") // Base path for consistency
public class TestResultsController {

    private final TestResultsRepository testResultsRepository;
    private final UserRepository userRepository;
    private final SkintypeRepository skintypeRepository;
    private final IngredientRepository ingredientRepository;
    private final SkinCareRountineService skinCareRountineService;

    private final SkinCareRoutineService2 skinCareRoutineService2;

    private final ConcernRepository concernRepository;

    @Autowired
    public TestResultsController(TestResultsRepository testResultsRepository,
                                 UserRepository userRepository,
                                 SkintypeRepository skintypeRepository,
                                 IngredientRepository ingredientRepository,
                                 SkinCareRountineService skinCareRountineService,
                                 SkinCareRoutineService2 skinCareRoutineService2,
                                 ConcernRepository concernRepository) {
        this.testResultsRepository = testResultsRepository;
        this.userRepository = userRepository;
        this.skintypeRepository = skintypeRepository;
        this.ingredientRepository = ingredientRepository;
        this.skinCareRountineService = skinCareRountineService;
        this.concernRepository = concernRepository;
        this.skinCareRoutineService2 = skinCareRoutineService2;
    }

    //-------------------Get Mapping---------------
    @GetMapping("/{id}") //Maybe not needed?
    public TestResults getTestResultById(@PathVariable Long id) {
        return testResultsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TestResult not found with id: " + id));
    }

    @GetMapping("/users/{userId}")
    public TestResults getTestResultsForUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return user.getTestResults();
    }

    @GetMapping("")
    public List<TestResults> getTestResults() {
        List<TestResults> testResults = testResultsRepository.findAll();
        if (testResults.isEmpty()) {
            throw new RuntimeException("No test results found.");
        }
        return testResults;
    }

    //Recommended Products by userId
    @GetMapping("/users/{userId}/recommendations")
    public List<ProductDto> getRecommendedProducts(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // get user's testResult
        TestResults userTestResults = user.getTestResults();
        if (userTestResults == null) {
            throw new RuntimeException("No test results found for user with id: " + userId);
        }

        // Convert Product entities to DTOs using the service method
        return skinCareRoutineService2.getProductDtos(userTestResults.getRecommendedProducts());
    }

    //Skincare concerns by userId
    @GetMapping("/users/{userId}/concerns")
    public List<Concern> getConcernsByUserId(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        TestResults userTestResults = user.getTestResults();
        if (userTestResults == null) {
            throw new RuntimeException("No test results found for user with id: " + userId);
        }
        return userTestResults.getConcerns();
    }

    //Avoid ingredients by userId
    @GetMapping("/users/{userId}/avoid")
    public List<Ingredient> getAvoidIngredientsByUserId(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        TestResults userTestResults = user.getTestResults();
        if (userTestResults == null) {
            throw new RuntimeException("No test results found for user with id: " + userId);
        }
        return userTestResults.getAvoidIngredients();
    }
    //-------------------Post Mapping---------------
    @PostMapping("")
    @Transactional
    public ResponseEntity<TestResultsResponseDto> createTestResult(@RequestBody TestResultsDto testResultsDTO) {
        /* Example body:
        {
            "skinTypes" : 1,
            "avoidIngredients": [1,2],
            "concerns": [1, 2],
            "budget" : 20,
            "user" : "00001"
        }
	    */

        //Fetch user from the database
        User user = userRepository.findById(testResultsDTO.getUser())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + testResultsDTO.getUser()));

        // Check if the user already has a TestResults object
        // Delete existing test results if they exist
        if (user.getTestResults() != null) {
            //Delete test results
            this.deleteTestResult(user.getUserId());
        }

        // Create and set up the new test result
        //-----Set the attributes & relationships-----
        TestResults testResult = new TestResults();
        testResult.setBudget(testResultsDTO.getBudget());  //Set budget

//        //Set the user in test results
//        testResult.setUser(user);

        //Set skin type
        Skintype skintype = skintypeRepository.findById(testResultsDTO.getSkinType())
                .orElseThrow(() -> new RuntimeException("Skintype not found with id: " + testResultsDTO.getSkinType()));;
        testResult.setSkinType(skintype);

        //Set avoid ingredients
        testResult.setAvoidIngredients(
                testResultsDTO.getAvoidIngredients().stream()
                        .map(ingredientId -> ingredientRepository.findById(ingredientId)
                                .orElseThrow(() -> new RuntimeException("Ingredient with ID " + ingredientId + " not found")))
                        .collect(Collectors.toList())
        );

        // Set concerns
        testResult.setConcerns(
                testResultsDTO.getConcerns().stream()
                        .map(concernId -> this.concernRepository.findById(concernId)
                                .orElseThrow(() -> new RuntimeException("concern with ID " + concernId + " not found")))
                        .collect(Collectors.toList())
        );

        //-----------Call matching algorithm before saving--------
        // Critical: Establish relationship before saving
        testResult.setUser(user);
        user.setTestResults(testResult);

        // First save the user with the relationship to ensure everything is consistent
        //userRepository.save(user);

        // Now match products for the test result
        //this.skinCareRountineService.matchProducts(testResult);
        this.skinCareRoutineService2.matchProducts(testResult);

        // Now save the test result with matched products
        TestResults savedResult = testResultsRepository.save(testResult);

        //Save user
        userRepository.save(user);

        // Convert to DTO for response
        TestResultsResponseDto responseDto = convertToDto(savedResult);

        return ResponseEntity.ok(responseDto);
    }

    private TestResultsResponseDto convertToDto(TestResults entity) {
        TestResultsResponseDto dto = new TestResultsResponseDto();
        dto.setTestResultId(entity.getTestResultId());
        dto.setBudget(entity.getBudget());

        if (entity.getSkinType() != null) {
            dto.setSkinTypeName(entity.getSkinType().getDescription());
        }

        // Convert recommended products to DTOs
        List<ProductDto> productDtos = skinCareRoutineService2.getProductDtos(entity.getRecommendedProducts());
        dto.setRecommendedProducts(productDtos);

        // Convert avoid ingredients to names
        List<String> ingredientNames = entity.getAvoidIngredients().stream()
                .map(Ingredient::getIngredientName)
                .collect(Collectors.toList());
        dto.setAvoidIngredientsNames(ingredientNames);

        // Convert concerns to names
        List<String> concernNames = entity.getConcerns().stream()
                .map(Concern::getDescription)
                .collect(Collectors.toList());
        dto.setConcernNames(concernNames);

        List<String> categoryNames = new ArrayList<>();
        for (Product product : entity.getRecommendedProducts()) {
            categoryNames.add(product.getCategory().getCategoryName());
        }
        dto.setCategoryNames(categoryNames);

        return dto;
    }

    //Put concerns by userId
    @PostMapping("/users/{userId}/concerns")
    public List<Concern> updateConcernsByUserId(@PathVariable String userId,
                                                @RequestBody TestResultsDto testResultsDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        TestResults userTestResults = user.getTestResults();
        if (userTestResults == null) {
            throw new RuntimeException("No test results found for user with id: " + userId);
        }
        userTestResults.setConcerns(concernRepository.findAllById(testResultsDTO.getConcerns()));
        userRepository.save(user);
        return userTestResults.getConcerns();
    }

    //Put avoidIngredients by userId
    @PostMapping("/users/{userId}/avoid")
    public List<Ingredient> updateAvoidIngredientsByUserId(@PathVariable String userId,
                                                           @RequestBody TestResultsDto testResultsDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        TestResults userTestResults = user.getTestResults();
        if (userTestResults == null) {
            throw new RuntimeException("No test results found for user with id: " + userId);
        }
        userTestResults.setAvoidIngredients(ingredientRepository.findAllById(testResultsDTO.getAvoidIngredients()));
        userRepository.save(user);
        return userTestResults.getAvoidIngredients();
    }

    //-------------------Delete Mapping---------------
    @DeleteMapping("/{id}")
    @Transactional
    public void deleteTestResult(@PathVariable Long id) {
        TestResults testResult = testResultsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TestResult not found with id: " + id));

        // Remove reference from user to avoid foreign key issues
        User user = testResult.getUser();
        if (user != null) {
            user.setTestResults(null);
            userRepository.save(user);
        }

        testResultsRepository.delete(testResult);
    }

    @DeleteMapping("/users/{userId}")
    @Transactional
    public void deleteTestResult(@PathVariable String userId) {
        //Fetch the user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        //Get the TestResults
        TestResults testResult = user.getTestResults();
        if (testResult == null) {
            throw new RuntimeException("No TestResults found for user with id: " + userId);
        }

        //Remove the reference from User
        user.setTestResults(null);
        userRepository.save(user);

        //Delete the TestResults record
        testResultsRepository.delete(testResult);
    }
}
