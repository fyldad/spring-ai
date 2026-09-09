package ru.anblazhnov.springai.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
import ru.anblazhnov.springai.Service.impl.SelfCheckQuestionService;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

@RestController
public class QueryController {

    private final QuestionServiceImpl questionService;

    public QueryController(QuestionServiceImpl questionService) {
        this.questionService = questionService;
    }

    @PostMapping(path = "/ask")
    public Answer ask(@RequestBody @Valid Question question) {
        return questionService.askQuestion(question);
    }

}
