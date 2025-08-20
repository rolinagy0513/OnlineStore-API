package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.ProductDTO;
import org.example.onlinestoreapi.DTO.ProductResponseDTO;
import org.example.onlinestoreapi.DTO.ProductWithQuantityDTO;
import org.example.onlinestoreapi.DTO.ProductWithStockDTO;
import org.example.onlinestoreapi.exception.CategoryNotFoundException;
import org.example.onlinestoreapi.exception.ProductNotFoundException;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.model.Product;
import org.example.onlinestoreapi.model.Stock;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.repository.ProductRepository;
import org.example.onlinestoreapi.service.impl.ProductService;
import org.example.onlinestoreapi.service.impl.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Service class for the Product service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ProductServiceTest {

    @MockBean
    ProductRepository productRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    StockService stockService;

    @Autowired
    private ProductService service;

    private Category category;
    private Stock stock;
    private Product product1;
    private Product product2;
    private List<Product> productList;
    private Page<Product> productPage;
    @Autowired
    private RepositoryMethodInvocationListener repositoryMethodInvocationListener;


    @BeforeEach
    void setUp() {

        category = Category.builder()
                .id(1L)
                .name("Category")
                .build();


        product1 = Product.builder()
                .id(1L)
                .category(category)
                .price(new BigDecimal(5))
                .name("Product1")
                .description("Description for product1")
                .quantity(20)
                .build();

        product2 = Product.builder()
                .id(2L)
                .category(category)
                .price(new BigDecimal(5))
                .name("Product2")
                .description("Description for product2")
                .quantity(20)
                .build();


        stock = Stock.builder()
                .id(1L)
                .quantity(20)
                .product(product1)
                .build();


        product1.setStock(stock);
        product2.setStock(stock);

        productList = Arrays.asList(product1, product2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        productPage = new PageImpl<>(productList, pageable, productList.size());
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProductResponseDTO() throws ExecutionException, InterruptedException {

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        CompletableFuture<Page<ProductResponseDTO>> result = service.getAllProduct(0,10,"name","ASC");

        Page<ProductResponseDTO> resultPage = result.get();

        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Product1",resultPage.getContent().get(0).getName());
        assertEquals("Product2",resultPage.getContent().get(1).getName());

        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllProducts_Pagination_ShouldReturnCorrectPages() throws ExecutionException, InterruptedException{

        List<Product> pageOneProducts = Arrays.asList(
                Product.builder().id(10L).name("Product A").category(category).price(new BigDecimal("10.99")).build(),
                Product.builder().id(11L).name("Product B").category(category).price(new BigDecimal("11.99")).build()
        );

        List<Product> secondPageProducts =Arrays.asList(
                Product.builder().id(12L).name("Product C").category(category).price(new BigDecimal("12.99")).build(),
                Product.builder().id(13L).name("Product D").category(category).price(new BigDecimal("13.99")).build()
        );


        Pageable pageableFirst = PageRequest.of(0, 2, Sort.by("name").ascending());
        Page<Product> pageOne = new PageImpl<>(pageOneProducts, pageableFirst, 4);
        when(productRepository.findAll(pageableFirst)).thenReturn(pageOne);

        Pageable pageableSecond = PageRequest.of(1, 2, Sort.by("name").ascending());
        Page<Product> secondPage = new PageImpl<>(secondPageProducts, pageableSecond, 4);
        when(productRepository.findAll(pageableSecond)).thenReturn(secondPage);

        CompletableFuture<Page<ProductResponseDTO>> futureOfPageOne = service.getAllProduct(0,2,"name","ASC");
        Page<ProductResponseDTO> resultOfPageOne = futureOfPageOne.get();

        assertNotNull(resultOfPageOne);
        assertEquals(2, resultOfPageOne.getContent().size());
        assertEquals(4, resultOfPageOne.getTotalElements());
        assertEquals("Product A",resultOfPageOne.getContent().get(0).getName());
        assertEquals("Product B", resultOfPageOne.getContent().get(1).getName());

        CompletableFuture<Page<ProductResponseDTO>> futureOfSecondPage = service.getAllProduct(1,2,"name","ASC");
        Page<ProductResponseDTO> resultOfSecondPage = futureOfSecondPage.get();

        assertNotNull(resultOfSecondPage);
        assertEquals(2, resultOfSecondPage.getContent().size());
        assertEquals(4, resultOfPageOne.getTotalElements());
        assertEquals("Product C",resultOfSecondPage.getContent().get(0).getName());
        assertEquals("Product D", resultOfSecondPage.getContent().get(1).getName());

        verify(productRepository).findAll(pageableFirst);
        verify(productRepository).findAll(pageableSecond);

    }

    @Test
    void getAllProductsByCategoryId_ShouldReturnPageOfProductResponseDTO() throws ExecutionException, InterruptedException{

        String expectedCategoryName = "Category";

        when(productRepository.findByCategoryId(1L,productPage.getPageable())).thenReturn(productPage);

        CompletableFuture<Page<ProductResponseDTO>> result = service.getAllProductByCategory(1L,0,10,"name","ASC");
        Page<ProductResponseDTO> resultPage = result.get();

        assertNotNull(resultPage);
        assertEquals(2,resultPage.getTotalElements());
        assertEquals("Product1",resultPage.getContent().get(0).getName());
        assertEquals("Product2",resultPage.getContent().get(1).getName());

        resultPage.getContent().forEach(ProductResponseDTO->{
            assertEquals(expectedCategoryName,ProductResponseDTO.getCategoryName());
        });

        verify(productRepository).findByCategoryId(1L,productPage.getPageable());
    }

    @Test
    void getAllProductByCategoryId_WithInvalidId_ShouldThrowException(){

        when(productRepository.findByCategoryId(99L,productPage.getPageable())).thenReturn(Page.empty());

        CompletableFuture<Page<ProductResponseDTO>> productFuture = service.getAllProductByCategory(99L,0,10,"name","ASC");

        Exception exception = assertThrows(ExecutionException.class,()->{
            productFuture.get();
        });

        assertTrue(exception.getCause() instanceof CategoryNotFoundException);
        verify(productRepository).findByCategoryId(99L,productPage.getPageable());

    }

    @Test
    void getAllProductsByCategoryId_Pagination_ShouldReturnCorrectPages() throws ExecutionException, InterruptedException {

        Category testCategory = Category.builder()
                .id(5L)
                .name("PaginationCategory")
                .build();

        List<Product> pageOneProducts = Arrays.asList(
                Product.builder().id(10L).name("Product A").category(testCategory).price(new BigDecimal("10.99")).build(),
                Product.builder().id(11L).name("Product B").category(testCategory).price(new BigDecimal("11.99")).build()
        );

        List<Product> pageTwoProducts = Arrays.asList(
                Product.builder().id(12L).name("Product C").category(testCategory).price(new BigDecimal("12.99")).build(),
                Product.builder().id(13L).name("Product D").category(testCategory).price(new BigDecimal("13.99")).build()
        );

        Pageable pageableFirst = PageRequest.of(0, 2, Sort.by("name").ascending());
        Page<Product> pageOne = new PageImpl<>(pageOneProducts, pageableFirst, 4);
        when(productRepository.findByCategoryId(5L, pageableFirst)).thenReturn(pageOne);

        Pageable pageableSecond = PageRequest.of(1, 2, Sort.by("name").ascending());
        Page<Product> pageTwo = new PageImpl<>(pageTwoProducts, pageableSecond, 4);
        when(productRepository.findByCategoryId(5L, pageableSecond)).thenReturn(pageTwo);

        CompletableFuture<Page<ProductResponseDTO>> resultPageOne = service.getAllProductByCategory(5L, 0, 2, "name", "ASC");
        Page<ProductResponseDTO> pageOneResult = resultPageOne.get();

        assertNotNull(pageOneResult);
        assertEquals(2, pageOneResult.getContent().size());
        assertEquals(4, pageOneResult.getTotalElements());
        assertEquals(0, pageOneResult.getNumber());
        assertEquals("Product A", pageOneResult.getContent().get(0).getName());
        assertEquals("Product B", pageOneResult.getContent().get(1).getName());

        CompletableFuture<Page<ProductResponseDTO>> resultPageTwo = service.getAllProductByCategory(5L, 1, 2, "name", "ASC");
        Page<ProductResponseDTO> pageTwoResult = resultPageTwo.get();

        assertNotNull(pageTwoResult);
        assertEquals(2, pageTwoResult.getContent().size());
        assertEquals(4, pageTwoResult.getTotalElements());
        assertEquals(1, pageTwoResult.getNumber());
        assertEquals("Product C", pageTwoResult.getContent().get(0).getName());
        assertEquals("Product D", pageTwoResult.getContent().get(1).getName());

        verify(productRepository).findByCategoryId(5L,pageableFirst);
        verify(productRepository).findByCategoryId(5L, pageableSecond);
    }

    @Test
    void addProduct_WhenNewProduct_ShouldCreateProductAndStock() {

        Long categoryId = 1L;
        ProductDTO productDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .quantity(10)
                .price(new BigDecimal("19.99"))
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));


        when(productRepository.findByName("Not yet existing Product")).thenReturn(Optional.empty());


        Product newProduct = Product.builder()
                .id(3L)
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("19.99"))
                .category(category)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Stock newStock = Stock.builder()
                .id(2L)
                .quantity(10)
                .product(newProduct)
                .build();

        when(stockService.createNewStock(any(Product.class), eq(10))).thenReturn(newStock);


        ProductWithStockDTO result = service.addProduct(categoryId, productDTO);

        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(10, result.getStockQuantity());
        assertEquals(categoryId, result.getCategoryId());

        verify(categoryRepository).findById(categoryId);
        verify(productRepository).findByName("New Product");
        verify(productRepository).save(any(Product.class));
        verify(stockService).createNewStock(any(Product.class), eq(10));
    }

    @Test
    void addProduct_WhenExistingProduct_ShouldAddToExistingProductsStock(){

        Category existingCategory = Category.builder()
                .id(5L)
                .name("Existing Category")
                .build();

        Product existingProduct = Product.builder()
                .id(5L)
                .name("Existing Product")
                .description("Existing Description")
                .price(new BigDecimal("19.99"))
                .category(existingCategory)
                .build();

        ProductDTO productDTO = ProductDTO.builder()
                .name("Existing Product")
                .description("Existing Description")
                .price(new BigDecimal("19.99"))
                .quantity(20)
                .build();


        existingProduct.setStock(stock);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(existingCategory));

        when(productRepository.findByName(productDTO.getName())).thenReturn(Optional.of(existingProduct));

        when(stockService.increaseStock(existingProduct.getId(),productDTO.getQuantity())).thenReturn(stock);

        ProductWithStockDTO result = service.addProduct(existingCategory.getId(),productDTO);

        assertNotNull(result);
        assertEquals("Existing Product", result.getName());
        assertEquals("Existing Description", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(20, result.getStockQuantity());
        assertEquals(existingCategory.getId(), result.getCategoryId());

        verify(categoryRepository).findById(existingCategory.getId());
        verify(productRepository).findByName(existingProduct.getName());
        verify(stockService).increaseStock(existingProduct.getId(),productDTO.getQuantity());

    }

    @Test
    void addProduct_WithInvalidCategoryId_ShouldThrowException(){

        ProductDTO wandetProductDTO = ProductDTO.builder()
                .name("Wanted Product")
                .description("Wanted Description")
                .price(new BigDecimal("19.99"))
                .quantity(20)
                .build();

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()->{
            service.addProduct(99L,wandetProductDTO);
        });

        verify(categoryRepository).findById(99L);

    }

    @Test
    void addProduct_ShouldSucceedAfterRetry(){
        Long categoryId = 1L;
        ProductDTO productDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .quantity(10)
                .price(new BigDecimal("19.99"))
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        when(productRepository.findByName("New Product")).thenReturn(Optional.empty());

        Product newProduct = Product.builder()
                .id(3L)
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("19.99"))
                .category(category)
                .build();

        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"))
                .thenReturn(newProduct);

        Stock newStock = Stock.builder()
                .id(2L)
                .quantity(10)
                .product(newProduct)
                .build();

        when(stockService.createNewStock(any(Product.class), eq(10))).thenReturn(newStock);

        ProductWithStockDTO result = service.addProduct(categoryId, productDTO);

        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(10, result.getStockQuantity());
        assertEquals(categoryId, result.getCategoryId());

        verify(categoryRepository,times(2)).findById(categoryId);
        verify(productRepository,times(2)).findByName("New Product");
        verify(productRepository, times(2)).save(any(Product.class));
        verify(stockService).createNewStock(any(Product.class), eq(10));
    }

    @Test
    void addProduct_ShouldRespectBackoffDelay() {
        Long categoryId = 1L;
        ProductDTO productDTO = ProductDTO.builder()
                .name("DelayProduct")
                .description("Product for testing delay")
                .quantity(10)
                .price(new BigDecimal("19.99"))
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByName("DelayProduct")).thenReturn(Optional.empty());

        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        long startTime = System.currentTimeMillis();

        assertThrows(DataIntegrityViolationException.class, () -> {
            service.addProduct(categoryId, productDTO);
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration >= 180, "Backoff delay not respected, duration: " + duration);

        verify(categoryRepository, times(3)).findById(categoryId);
        verify(productRepository, times(3)).findByName("DelayProduct");
        verify(productRepository, times(3)).save(any(Product.class));
    }

    @Test
    void editProduct_WithValidDAta_ShouldReturn_ProductResponseDTO(){

        Category categoryForEdit = Category.builder()
                .id(20L)
                .name("Category For Edit")
                .build();

        Product productForEdit = Product.builder()
                .id(20L)
                .name("Product For Edit")
                .description("Description For The Product")
                .quantity(20)
                .price(new BigDecimal(5))
                .category(categoryForEdit)
                .build();

        Stock stockForEdit = Stock.builder()
                .id(20L)
                .quantity(20)
                .product(productForEdit)
                .build();

        productForEdit.setStock(stockForEdit);

        ProductWithQuantityDTO productWithQuantityDTO = ProductWithQuantityDTO.builder()
                .name("New name for the product")
                .description("New description for the product")
                .price(new BigDecimal(5))
                .build();

        when(productRepository.findById(20L)).thenReturn(Optional.of(productForEdit));

        productForEdit.setName(productWithQuantityDTO.getName());
        productForEdit.setDescription(productWithQuantityDTO.getDescription());
        productForEdit.setPrice(productWithQuantityDTO.getPrice());

        when(productRepository.save(any(Product.class))).thenReturn(productForEdit);

        ProductResponseDTO result = service.editProduct(productForEdit.getId(),productWithQuantityDTO);

        assertNotNull(result);
        assertEquals("New name for the product",result.getName());
        assertEquals("New description for the product",result.getDescription());
        assertEquals(new BigDecimal(5),result.getPrice());

        verify(productRepository).findById(productForEdit.getId());
        verify(productRepository).save(any(Product.class));

    }

    @Test
    void editProduct_WithEmptyName_ShouldUseOriginal(){

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        ProductWithQuantityDTO productWithQuantityDTO = ProductWithQuantityDTO.builder()
                .name("")
                .description("New Description")
                .price(new BigDecimal("15.00"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        existingProduct.setDescription(productWithQuantityDTO.getDescription());
        existingProduct.setPrice(productWithQuantityDTO.getPrice());

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponseDTO result = service.editProduct(1L,productWithQuantityDTO);

        assertNotNull(result);
        assertEquals("Original Name",result.getName());
        assertEquals("New Description",result.getDescription());
        assertEquals(new BigDecimal("15.00"),result.getPrice());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));

    }

    @Test
    void editProduct_WithEmptyDescription_ShouldUSeOriginal(){

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        ProductWithQuantityDTO productWithQuantityDTO = ProductWithQuantityDTO.builder()
                .name("New name")
                .description("")
                .price(new BigDecimal("15.00"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        existingProduct.setName(productWithQuantityDTO.getName());
        existingProduct.setPrice(productWithQuantityDTO.getPrice());

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponseDTO result = service.editProduct(1L,productWithQuantityDTO);

        assertNotNull(result);
        assertEquals("New name",result.getName());
        assertEquals("Original Description",result.getDescription());
        assertEquals(new BigDecimal("15.00"),result.getPrice());

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));

    }

    @Test
    void editProduct_WithNegativePrice_ShouldThrowException(){

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        ProductWithQuantityDTO dtoWithNegativePrice = ProductWithQuantityDTO.builder()
                .name("The new name")
                .description("The new description")
                .price(new BigDecimal("-5.00"))
                .build();

        existingProduct.setName(dtoWithNegativePrice.getName());
        existingProduct.setDescription(dtoWithNegativePrice.getDescription());

        existingProduct.setPrice(dtoWithNegativePrice.getPrice());

        jakarta.validation.ConstraintViolationException exception = assertThrows(
                jakarta.validation.ConstraintViolationException.class,()->{
                    service.editProduct(existingProduct.getId(),dtoWithNegativePrice);
                }
        );

        assertTrue(exception.getMessage().contains("price"),
                "Exception message should mention the 'price' field");

        verify(productRepository, never()).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void editProduct_WithInvalidId_ShouldThrowException(){

        ProductWithQuantityDTO productWithQuantityDTO = ProductWithQuantityDTO.builder()
                .name("New name for the product")
                .description("New description for the product")
                .price(new BigDecimal(5))
                .build();

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,()->{
            service.editProduct(99L,productWithQuantityDTO);
        });

        verify(productRepository).findById(99L);

    }

    @Test
    void editProduct_ShouldSucceedAfterRetry(){

        Product existingProduct = Product.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        ProductWithQuantityDTO editedProductDTO = ProductWithQuantityDTO.builder()
                .name("The edited name")
                .description("The edited Description")
                .price(new BigDecimal("10.00"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        existingProduct.setName(editedProductDTO.getName());
        existingProduct.setDescription(editedProductDTO.getDescription());
        existingProduct.setPrice(editedProductDTO.getPrice());



        when(productRepository.save(any(Product.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(existingProduct);

        ProductResponseDTO result = service.editProduct(1L,editedProductDTO);

        assertNotNull(result);
        assertEquals("The edited name", result.getName());
        assertEquals("The edited Description", result.getDescription());
        assertEquals(new BigDecimal("10.00"), result.getPrice());

        verify(productRepository, times(2)).findById(1L);
        verify(productRepository,times(2)).save(any(Product.class));
    }

    @Test
    void editProduct_ShouldRespectBackoffDelay(){

        Product delayedProduct = Product.builder()
                .id(1L)
                .name("Delayed Product's Name")
                .description("Delayed Product's Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        ProductWithQuantityDTO delayedProductDTO = ProductWithQuantityDTO.builder()
                .name("Delayed Product's Name DTO")
                .description("Delayed Product's Description DTO")
                .price(new BigDecimal("10.00"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(delayedProduct));

        delayedProduct.setName(delayedProductDTO.getName());
        delayedProduct.setDescription(delayedProductDTO.getDescription());
        delayedProduct.setPrice(delayedProductDTO.getPrice());

        when(productRepository.save(any(Product.class))).thenThrow(OptimisticLockingFailureException.class);

        long startTime = System.currentTimeMillis();

        assertThrows(OptimisticLockingFailureException.class,()->{
            service.editProduct(1L,delayedProductDTO);
        });

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        assertTrue(duration >= 180,"Backoff Delay was not respected" + duration);

        verify(productRepository,times(3)).findById(1L);
        verify(productRepository,times(3)).save(any(Product.class));

    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnMessage(){

        String finalMessage = "The following product was deleted: Deleted Product";

        Product productForDeletion = Product.builder()
                .id(1L)
                .name("Deleted Product")
                .description("Deleted Description")
                .price(new BigDecimal("10.00"))
                .category(category)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productForDeletion));

        String result  = service.deleteProduct(1L);

        assertNotNull(result);

        assertEquals(finalMessage,result);

        verify(productRepository).findById(1L);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException(){

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,()->{
            service.deleteProduct(99L);
        });

        verify(productRepository).findById(99L);
        verify(productRepository,times(0)).deleteById(99L);

    }

}
