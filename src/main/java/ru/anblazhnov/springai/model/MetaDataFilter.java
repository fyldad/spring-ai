package ru.anblazhnov.springai.model;

public record MetaDataFilter(String field, String value, CompareType type) {
    public enum CompareType {
        OR,
        AND
    }
}
