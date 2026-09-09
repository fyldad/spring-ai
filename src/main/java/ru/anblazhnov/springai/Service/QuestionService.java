package ru.anblazhnov.springai.Service;

import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

public interface QuestionService {
    Answer askQuestion(Question question);
}
