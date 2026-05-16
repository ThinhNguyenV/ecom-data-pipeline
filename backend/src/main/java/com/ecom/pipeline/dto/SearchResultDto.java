package com.ecom.pipeline.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDto {
    private String productId;
    private String name;
    private String category;
    private String description;
    private Double score;
}
