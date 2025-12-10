package com.polypadel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PoolRequest(
    @NotBlank String name,
    @Size(min = 6, max = 6, message = "Une poule doit contenir exactement 6 équipes") List<Long> teamIds
) {}
