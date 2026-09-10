package ru.anblazhnov.springai.Service;

import reactor.core.publisher.Flux;
import ru.anblazhnov.springai.model.Question;

public interface QuestionStreamService {
    Flux<String> askQuestion(Question question);
}
