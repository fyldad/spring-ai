package ru.anblazhnov.springai.Service;

import jakarta.validation.Valid;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.CompanyQuestion;
import ru.anblazhnov.springai.model.Question;

public interface CompanyQuestionService {
    String askQuestion(@Valid CompanyQuestion question);
}
