package org.example.onlinestoreapi.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CategoryDTO.java
 * Fields:
 * - name: The name of the category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    @NotNull
    private String name;

}
