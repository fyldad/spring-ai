package ru.anblazhnov.springai.model;

import jakarta.validation.constraints.NotBlank;

public record CompanyQuestion (
        @NotBlank(message = "company of the question is required") String company,
        @NotBlank(message = "question is required") String question,
        String module
) { }
