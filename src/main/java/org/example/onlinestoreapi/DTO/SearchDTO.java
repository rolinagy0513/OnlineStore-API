package org.example.onlinestoreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for general search operations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchDTO {
    String credential;
}
