package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.onlinestoreapi.model.Category;

/**
 * DTO for category responses containing basic category information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDTO {

    private Long id;
    private String name;

    /**
     * Converts Category entity to CategoryResponseDTO.
     */
    public static CategoryResponseDTO fromCategory(Category category){
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
