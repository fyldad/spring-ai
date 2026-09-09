package ru.anblazhnov.springai.model;

import jakarta.validation.constraints.NotBlank;

public record Question(
        @NotBlank(message = "scope of the question is required") String scope,
        @NotBlank(message = "question is required") String question
) { }
