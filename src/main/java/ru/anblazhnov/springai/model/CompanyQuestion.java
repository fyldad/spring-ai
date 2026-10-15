package ru.anblazhnov.springai.model;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CompanyQuestion (
        @NotBlank(message = "company of the question is required") String company,
        @NotBlank(message = "question is required") String question,
        Map<String, String> metaDataFilter
) { }
