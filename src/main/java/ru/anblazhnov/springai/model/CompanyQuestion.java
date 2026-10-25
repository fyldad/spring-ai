package ru.anblazhnov.springai.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CompanyQuestion(
        @NotBlank(message = "question is required") String question,
        List<MetaDataFilter> metaDataFilter
) {
}
