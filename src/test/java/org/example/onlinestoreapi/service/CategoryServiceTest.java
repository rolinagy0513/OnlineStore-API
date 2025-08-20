package org.example.onlinestoreapi.service;

import org.example.onlinestoreapi.DTO.CategoryDTO;
import org.example.onlinestoreapi.DTO.CategoryResponseDTO;
import org.example.onlinestoreapi.exception.CategoryNotFoundException;
import org.example.onlinestoreapi.model.Category;
import org.example.onlinestoreapi.repository.CategoryRepository;
import org.example.onlinestoreapi.service.impl.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for the Category service
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {

    @MockBean
    private CategoryRepository repository;

    @Autowired
    private CategoryService service;

    private Category category1;
    private Category category2;
    private Category addedCategory;
    private CategoryDTO categoryDTO;
    private CategoryDTO categoryDTOForDelay;
    private CategoryDTO categoryDTOWithNullValue;
    private List<Category> categoryList;
    private Page<Category> categoryPage;

    @BeforeEach
    void setUp(){

        category1 = Category.builder().id(1L).name("Electronics").build();
        category2 = Category.builder().id(2L).name("Clothing").build();
        categoryDTO = new CategoryDTO("Books");
        categoryDTOWithNullValue = new CategoryDTO(null);
        categoryList = Arrays.asList(category1, category2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        categoryPage = new PageImpl<>(categoryList, pageable, categoryList.size());
    }

    @Test
    void getAllCategory_ShouldReturnPageOfCategoryResponseDTO() throws ExecutionException, InterruptedException{

        when(repository.findAll(any(Pageable.class))).thenReturn(categoryPage);

        CompletableFuture<Page<CategoryResponseDTO>> result = service.getAllCategory(0, 10, "name", "ASC");

        Page<CategoryResponseDTO> resultPage = result.get();

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Electronics", resultPage.getContent().get(0).getName());
        assertEquals("Clothing", resultPage.getContent().get(1).getName());

        verify(repository).findAll(any(Pageable.class));

    }

    @Test
    void getAllCategory_Pagination_ShouldReturnCorrectPages() throws ExecutionException, InterruptedException{

        List<Category> pageOneCategories = Arrays.asList(
                Category.builder().id(8L).name("Category1").build(),
                Category.builder().id(9L).name("Category2").build()
        );

        List<Category> pageTwoCategories = Arrays.asList(
                Category.builder().id(10L).name("Category3").build(),
                Category.builder().id(11L).name("Category4").build()
        );

        Pageable pageableFirst = PageRequest.of(0, 2, Sort.by("name").ascending());
        Page<Category> pageOne = new PageImpl<>(pageOneCategories, pageableFirst, 4);
        when(repository.findAll(pageableFirst)).thenReturn(pageOne);

        Pageable pageableSecond = PageRequest.of(1, 2, Sort.by("name").ascending());
        Page<Category> socondPage = new PageImpl<>(pageTwoCategories, pageableSecond, 4);
        when(repository.findAll(pageableSecond)).thenReturn(socondPage);

        CompletableFuture<Page<CategoryResponseDTO>> futureOfPageOne = service.getAllCategory(0,2,"name","ASC");
        Page<CategoryResponseDTO> resultOfPageOne = futureOfPageOne.get();

        assertNotNull(resultOfPageOne);
        assertEquals(2, resultOfPageOne.getContent().size());
        assertEquals(4, resultOfPageOne.getTotalElements());
        assertEquals("Category1", resultOfPageOne.getContent().get(0).getName());
        assertEquals("Category2", resultOfPageOne.getContent().get(1).getName());

        CompletableFuture<Page<CategoryResponseDTO>> futureOfSecondPage = service.getAllCategory(1,2,"name","ASC");
        Page<CategoryResponseDTO> resultOfSecondPage = futureOfSecondPage.get();

        assertNotNull(resultOfSecondPage);
        assertEquals(2, resultOfSecondPage.getContent().size());
        assertEquals(4, resultOfSecondPage.getTotalElements());
        assertEquals("Category3", resultOfSecondPage.getContent().get(0).getName());
        assertEquals("Category4", resultOfSecondPage.getContent().get(1).getName());

        verify(repository).findAll(pageableFirst);
        verify(repository).findAll(pageableSecond);

    }

    @Test
    void getOneCategory_WithValidId_ShouldReturnCategoryResponseDTO() throws ExecutionException, InterruptedException{

        when(repository.findById(1L)).thenReturn(Optional.of(category1));

        CompletableFuture<CategoryResponseDTO> responseDTO = service.getOneCategory(1L);

        CategoryResponseDTO result = responseDTO.get();

        assertNotNull(result);
        assertEquals(1L,result.getId());
        assertEquals("Electronics", result.getName());

        verify(repository).findById(1L);

    }

    @Test
    void getOneCategory_WithInvalidId_ShouldThrowException() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        CompletableFuture<CategoryResponseDTO> future = service.getOneCategory(99L);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get();
        });

        assertTrue(exception.getCause() instanceof CategoryNotFoundException);
        verify(repository).findById(99L);
    }

    @Test
    void addCategory_WithValidData_ShouldReturnCategory(){

        Category addedCategory = Category.builder()
                .id(3L)
                .name("TheNewCategory")
                .build();
        CategoryDTO newCategory = new CategoryDTO("The new category");

        when(repository.save(any(Category.class))).thenReturn(addedCategory);

        Category result = service.addCategory(newCategory);

        assertNotNull(result);
        assertEquals("TheNewCategory",result.getName());

        verify(repository).save(any(Category.class));

    }

    @Test
    void addCategory_WithInvalidData_ShouldThrowException(){

        assertThrows(Exception.class,()->{
            service.addCategory(categoryDTOWithNullValue);
        });

        verify(repository, never()).save(any(Category.class));

    }

    @Test
    void addCategory_ShouldSucceedAfterRetry() {

        addedCategory = Category.builder().id(3L).name("TheNewCategory").build();

        when(repository.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"))
                .thenReturn(addedCategory);

        Category result = service.addCategory(categoryDTO);

        assertNotNull(result);
        assertEquals("TheNewCategory", result.getName());
        assertEquals(3L, result.getId());

        verify(repository, times(2)).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldRespectBackoffDelay() {

        categoryDTOForDelay = new CategoryDTO("DelayCategory");

        when(repository.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        long startTime = System.currentTimeMillis();

        assertThrows(DataIntegrityViolationException.class, () -> {
            service.addCategory(categoryDTOForDelay);
        });

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        assertTrue(duration >= 180, "Backoff delay not respected, duration: " + duration);

        verify(repository, times(3)).save(any(Category.class));
    }

    @Test
    void editCategory_WithValidIdAndData_ShouldReturnCategoryResponseDTO() {

        Category updatedCategory = Category.builder()
                .id(5L)
                .name("UpdatedElectronics")
                .build();

        CategoryDTO updateDTO = new CategoryDTO("UpdatedCategory");


        when(repository.findById(updatedCategory.getId())).thenReturn(Optional.of(updatedCategory));
        when(repository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponseDTO result = service.editCategory(updatedCategory.getId(), updateDTO);

        assertNotNull(result);
        assertEquals(updatedCategory.getId(), result.getId());
        assertEquals("UpdatedCategory", result.getName());

        verify(repository).findById(updatedCategory.getId());
        verify(repository).save(any(Category.class));
    }

    @Test
    void editCategory_WithEmptyName_ShouldUseOriginal(){

        Category existingCategoryForTest = Category.builder()
                .id(65L)
                .name("Original Category")
                .build();

        CategoryDTO categoryWithNoName = new CategoryDTO("");

        Category savedCategory = Category.builder()
                .id(65L)
                .name(existingCategoryForTest.getName())
                .build();

        when(repository.findById(65L)).thenReturn(Optional.of(existingCategoryForTest));
        when(repository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponseDTO result  = service.editCategory(65L,categoryWithNoName);

        assertNotNull(result);

        assertEquals("Original Category", savedCategory.getName());

        verify(repository).findById(65L);
        verify(repository).save(any(Category.class));

    }

    @Test
    void editCategory_WithInvalidId_ShouldThrowCategoryNotFoundException(){

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()->{
            service.editCategory(99L, categoryDTO);
        });

        verify(repository).findById(99L);

    }

    @Test
    void editCategory_WithInvalidData_ShouldThrowException(){

        assertThrows(Exception.class,()->{
            service.editCategory(1L,categoryDTOWithNullValue);
        });

        verify(repository, never()).save(any(Category.class));

    }

    @Test
    void editCategory_ShouldSucceedAfterRetry(){

        Category updatedCategory = Category.builder()
                .id(6L)
                .name("UpdatedCategory")
                .build();

        CategoryDTO updateDTO = new CategoryDTO("UpdatedCategory");

        when(repository.findById(6L)).thenReturn(Optional.of(updatedCategory));
        when(repository.save(any(Category.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(updatedCategory);

        CategoryResponseDTO result = service.editCategory(6L,updateDTO);

        assertNotNull(result);
        assertEquals(6L,result.getId());
        assertEquals("UpdatedCategory",result.getName());

        verify(repository, times(2)).save(any(Category.class));

    }

    @Test
    void editedCategory_ShouldRespectBackoffDelay(){

        Category updatedCategory = Category.builder()
                .id(6L)
                .name("UpdatedCategory")
                .build();

        CategoryDTO categoryDTOForDelay = new CategoryDTO("DelayedCategory");

        when(repository.findById(6L)).thenReturn(Optional.of(updatedCategory));
        when(repository.save(any(Category.class))).thenThrow(OptimisticLockingFailureException.class);

        Long startTime  = System.currentTimeMillis();

        assertThrows(OptimisticLockingFailureException.class,()->{
            service.editCategory(updatedCategory.getId(),categoryDTOForDelay);
        });

        Long endTime = System.currentTimeMillis();
        Long duration = endTime - startTime;

        assertTrue(duration >= 180, "Backoff delay not respected, duration: " + duration);

        verify(repository, times(3)).save(any(Category.class));

    }

    @Test
    void deleteCategory_ShouldDeleteCategory_thanReturnCategoryName(){

        String finalText  = "The following category was deleted successfully: Deleted Category";

        Category deletedCategory = Category.builder()
                .id(7L)
                .name("Deleted Category")
                .build();

        when(repository.findById(deletedCategory.getId())).thenReturn(Optional.of(deletedCategory));

        String result  = service.deleteCategory(deletedCategory.getId());

        assertNotNull(result);
        assertEquals(finalText,result);

        verify(repository).findById(deletedCategory.getId());

    }

    @Test
    void deleteCategory_WithInvalidId_ShouldThrowException(){

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()->{
            service.deleteCategory(99L);
        });

        verify(repository).findById(99L);
        verify(repository, times(0)).deleteById(99L);
    }
}
