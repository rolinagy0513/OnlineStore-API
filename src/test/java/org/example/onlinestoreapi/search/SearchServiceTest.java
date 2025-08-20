package org.example.onlinestoreapi.search;

import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.SearchByPriceDTO;
import org.example.onlinestoreapi.DTO.SearchDTO;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the Search service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class SearchServiceTest {

    @MockBean
    ProductRepository productRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @Autowired
    SearchService searchService;

    private Category category;
    private Product product;

    @BeforeEach
    void setup(){

        category = Category.builder()
                .id(1L)
                .name("Valid Category")
                .products(new ArrayList<>())
                .build();

        product = Product.builder()
                .id(1L)
                .category(category)
                .name("Valid Product")
                .description("Description")
                .price(new BigDecimal("10.00"))
                .quantity(10)
                .build();

        category.getProducts().add(product);

    }

    @Test
    void searchProductsByName_WithExistingProduct_WithValidData_ASC_ShouldReturnProductResponseDTO() throws ExecutionException, InterruptedException {

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid Product")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(product);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Product", resultPage.getContent().get(0).getName());
        assertEquals("Description", resultPage.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(0).getPrice());

        verify(productRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);


    }

    @Test
    void searchProductsByName_WithExistingProduct_WithValidData_DESC_ShouldReturnProductResponseDTO() throws ExecutionException, InterruptedException {

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid Product")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        List<Product> productList = List.of(product);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Product", resultPage.getContent().get(0).getName());
        assertEquals("Description", resultPage.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(0).getPrice());

        verify(productRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchProductsByName_WithHigherCase_ShouldIgnoreCase() throws ExecutionException, InterruptedException{

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("VALID PRODUCT")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(product);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Product", resultPage.getContent().get(0).getName());
        assertEquals("Description", resultPage.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(0).getPrice());

        verify(productRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchProductsByName_WithPartialMatch_ShouldReturnMultipleMatchingProducts() throws ExecutionException, InterruptedException{

        Product testProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Valid Phone")
                .description("Description")
                .price(new BigDecimal("12.00"))
                .quantity(5)
                .build();

        category.getProducts().add(testProduct);

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(product,testProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Valid Product", resultPage.getContent().get(0).getName());
        assertEquals("Description", resultPage.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Valid Phone", resultPage.getContent().get(1).getName());
        assertEquals("Description", resultPage.getContent().get(1).getDescription());
        assertEquals(new BigDecimal("12.00"),resultPage.getContent().get(1).getPrice());

        verify(productRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchProductsByName_WithPartialMatch_WithDifferentCategory_ShouldReturnMultipleMatchingProducts() throws ExecutionException, InterruptedException{

        Category testCategory = Category.builder()
                .id(2L)
                .name("Test Category")
                .products(new ArrayList<>())
                .build();

        Product testProduct = Product.builder()
                .id(2L)
                .category(testCategory)
                .name("Valid Phone")
                .description("Description")
                .price(new BigDecimal("12.00"))
                .quantity(5)
                .build();

        testCategory.getProducts().add(testProduct);

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(product,testProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Valid Product", resultPage.getContent().get(0).getName());
        assertEquals("Description", resultPage.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Valid Category",resultPage.getContent().get(0).getCategoryName());
        assertEquals("Valid Phone", resultPage.getContent().get(1).getName());
        assertEquals("Description", resultPage.getContent().get(1).getDescription());
        assertEquals(new BigDecimal("12.00"),resultPage.getContent().get(1).getPrice());
        assertEquals("Test Category",resultPage.getContent().get(1).getCategoryName());

        verify(productRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchProductsByName_WithNegativeSize_ShouldThrowException() {

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = -10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    void searchProductsByName_WithTooHighSizeValue_ShouldThrowException(){

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = 110;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository, never()).findByNameContainingIgnoreCase(any(), any());

    }

    @Test
    void searchProductsByName_WithNegativePage_ShouldThrowException() {

        String errorMessage = "The page must be 0 or higher";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = -10;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByName(searchDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository, never()).findByNameContainingIgnoreCase(any(), any());

    }

    @Test
    void searchCategoryByName_WithExistingCategory_WithValidData_ASC_ShouldReturnCategoryResponseDTO() throws ExecutionException, InterruptedException{

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid Category")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Category> categoryList = List.of(category);
        Page<Category> mockCategoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        when(categoryRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockCategoryPage);

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO,page,size,sortBy,direction);
        Page<CategoryResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Category", resultPage.getContent().get(0).getName());

        verify(categoryRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchCategoryByName_WithExistingCategory_WithValidData_DESC_ShouldReturnCategoryResponseDTO() throws ExecutionException, InterruptedException{

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid Category")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        List<Category> categoryList = List.of(category);
        Page<Category> mockCategoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        when(categoryRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockCategoryPage);

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO,page,size,sortBy,direction);
        Page<CategoryResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Category", resultPage.getContent().get(0).getName());

        verify(categoryRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchCategoryByName_WithHigherCase_ShouldIgnoreCase() throws ExecutionException, InterruptedException{

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("VALID CATEGORY")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        List<Category> categoryList = List.of(category);
        Page<Category> mockCategoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        when(categoryRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockCategoryPage);

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO,page,size,sortBy,direction);
        Page<CategoryResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Valid Category", resultPage.getContent().get(0).getName());

        verify(categoryRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchCategoryByName_WithPartialMatch_ShouldReturnMultipleMatchingCategories() throws ExecutionException, InterruptedException{

        Category testCategory = Category.builder()
                .id(2L)
                .name("Valid Electronics")
                .build();

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        List<Category> categoryList = List.of(category,testCategory);
        Page<Category> mockCategoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());

        when(categoryRepository.findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable)).thenReturn(mockCategoryPage);

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO,page,size,sortBy,direction);
        Page<CategoryResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Valid Category", resultPage.getContent().get(0).getName());
        assertEquals("Valid Electronics",resultPage.getContent().get(1).getName());

        verify(categoryRepository).findByNameContainingIgnoreCase(searchDTO.getCredential(),pageable);

    }

    @Test
    void searchCategoryByName_WithNegativeSize_ShouldThrowException(){

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = -10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO, page, size, sortBy, direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(),any());

    }

    @Test
    void searchCategoryByName_WithTooHighSizeValue_ShouldThrowException(){

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = 0;
        Integer size = 110;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO, page, size, sortBy, direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(),any());

    }

    @Test
    void searchCategoryByName_WithNegativePage_ShouldThrowException(){

        String errorMessage = "The page must be 0 or higher";

        SearchDTO searchDTO = SearchDTO.builder()
                .credential("Valid")
                .build();

        Integer page = -10;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<CategoryResponseDTO>> future = searchService.searchCategoryByName(searchDTO, page, size, sortBy, direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(),any());

    }

    @Test
    void searchProductsByPrice_WithExistingProduct_WithValidData_ASC_ShouldReturnProductResponseDTO() throws ExecutionException, InterruptedException{

        Product lowerPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Lower price")
                .price(new BigDecimal("2.00"))
                .build();

        Product higherPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Higher price")
                .price(new BigDecimal("10.00"))
                .build();

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(1.00))
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(lowerPricedProduct,higherPricedProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO, page, size, sortBy, direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Lower price",resultPage.getContent().get(0).getName());
        assertEquals(new BigDecimal("2.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Higher price",resultPage.getContent().get(1).getName());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(1).getPrice());

        verify(productRepository).findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable);

    }

    @Test
    void searchProductsByPrice_WithExistingProduct_WithValidData_DESC_ShouldReturnProductResponseDTO() throws ExecutionException, InterruptedException{

        Product lowerPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Lower price")
                .price(new BigDecimal("2.00"))
                .build();

        Product higherPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Higher price")
                .price(new BigDecimal("10.00"))
                .build();

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(1.00))
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "DESC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        List<Product> productList = List.of(lowerPricedProduct,higherPricedProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable)).thenReturn(mockProductPage);

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO, page, size, sortBy, direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Lower price",resultPage.getContent().get(0).getName());
        assertEquals(new BigDecimal("2.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Higher price",resultPage.getContent().get(1).getName());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(1).getPrice());

        verify(productRepository).findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable);

    }

    @Test
    void searchProductsByPrice_WithTooLowPrice_ShouldReturnOnlyOne() throws ExecutionException, InterruptedException{

        Product tooLowPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Too low price")
                .price(new BigDecimal("1.00"))
                .build();

        Product correctlyPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Correct price")
                .price(new BigDecimal("5.00"))
                .build();

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(5.00))
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        List<Product> productList = List.of(correctlyPricedProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable)).thenReturn(mockProductPage);


        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO, page, size, sortBy, direction);
        Page<ProductResponseDTO> resultPage = future.get();

        assertNotNull(resultPage);
        assertEquals(1,resultPage.getTotalElements());
        assertEquals("Correct price",resultPage.getContent().get(0).getName());
        assertEquals(new BigDecimal("5.00"),resultPage.getContent().get(0).getPrice());

        verify(productRepository).findByPriceBetween(searchByPriceDTO.getMinPrice(),searchByPriceDTO.getMaxPrice(),pageable);

    }

    @Test
    void searchProductsByPrice_WithNullMinPrice_ShouldMakeItZero() throws ExecutionException, InterruptedException{

        Product lowerPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Lower price")
                .price(new BigDecimal("2.00"))
                .build();

        Product higherPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Higher price")
                .price(new BigDecimal("10.00"))
                .build();

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(null)
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        List<Product> productList = List.of(lowerPricedProduct,higherPricedProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByPriceBetween(BigDecimal.ZERO,searchByPriceDTO.getMaxPrice(),pageable)).thenReturn(mockProductPage);


        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO, page, size, sortBy, direction);
        Page<ProductResponseDTO> resultPage = future.get();


        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Lower price",resultPage.getContent().get(0).getName());
        assertEquals(new BigDecimal("2.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Higher price",resultPage.getContent().get(1).getName());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(1).getPrice());

        verify(productRepository).findByPriceBetween(BigDecimal.ZERO,searchByPriceDTO.getMaxPrice(),pageable);

    }

    @Test
    void searchProductsByPrice_WithNullMaxPrice_ShouldMakeItZero() throws ExecutionException, InterruptedException{

        Product lowerPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Lower price")
                .price(new BigDecimal("2.00"))
                .build();

        Product higherPricedProduct = Product.builder()
                .id(2L)
                .category(category)
                .name("Higher price")
                .price(new BigDecimal("10.00"))
                .build();

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(null)
                .minPrice(BigDecimal.valueOf(2.00))
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        List<Product> productList = List.of(lowerPricedProduct,higherPricedProduct);
        Page<Product> mockProductPage = new PageImpl<>(productList, pageable, productList.size());

        when(productRepository.findByPriceBetween(searchByPriceDTO.getMinPrice(),BigDecimal.valueOf(Long.MAX_VALUE),pageable)).thenReturn(mockProductPage);


        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO, page, size, sortBy, direction);
        Page<ProductResponseDTO> resultPage = future.get();


        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Lower price",resultPage.getContent().get(0).getName());
        assertEquals(new BigDecimal("2.00"),resultPage.getContent().get(0).getPrice());
        assertEquals("Higher price",resultPage.getContent().get(1).getName());
        assertEquals(new BigDecimal("10.00"),resultPage.getContent().get(1).getPrice());

        verify(productRepository).findByPriceBetween(searchByPriceDTO.getMinPrice(),BigDecimal.valueOf(Long.MAX_VALUE),pageable);

    }

    @Test
    void searchProductsByPrice_WithMoreMinPriceThanMax_ShouldThrowException(){

        String errorMessage = "Minimum price cannot be greater than maximum price";

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(12.00))
                .build();

        Integer page = 0;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository,never()).findByPriceBetween(any(),any(),any());

    }

    @Test
    void searchProductsByPrice_WithNegativeSize_ShouldThrowException(){

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(2.00))
                .build();

        Integer page = 0;
        Integer size = -10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository,never()).findByPriceBetween(any(),any(),any());

    }

    @Test
    void searchProductsByPrice_WithTooHighSize_ShouldThrowException(){

        String errorMessage = "The size must be higher than 0 and lower than 100";

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(2.00))
                .build();

        Integer page = 0;
        Integer size = 110;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository,never()).findByPriceBetween(any(),any(),any());

    }

    @Test
    void searchProductsByPrice_WithNegativePage_ShouldThrowException(){

        String errorMessage = "The page must be 0 or higher";

        SearchByPriceDTO searchByPriceDTO = SearchByPriceDTO.builder()
                .maxPrice(BigDecimal.valueOf(10.00))
                .minPrice(BigDecimal.valueOf(2.00))
                .build();

        Integer page = -10;
        Integer size = 10;
        String sortBy = "name";
        String direction = "ASC";

        CompletableFuture<Page<ProductResponseDTO>> future = searchService.searchProductsByPrice(searchByPriceDTO,page,size,sortBy,direction);

        Exception exception = assertThrows(ExecutionException.class,()->{
            future.get();
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(errorMessage,exception.getCause().getMessage());

        verify(productRepository,never()).findByPriceBetween(any(),any(),any());

    }


}
