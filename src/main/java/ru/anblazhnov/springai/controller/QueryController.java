package ru.anblazhnov.springai.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
import ru.anblazhnov.springai.Service.impl.QuestionStreamServiceImpl;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

@RestController
public class QueryController {

    private final QuestionServiceImpl questionService;
    private final QuestionStreamServiceImpl streamService;

    public QueryController(QuestionServiceImpl questionService,
                           QuestionStreamServiceImpl streamService) {
        this.questionService = questionService;
        this.streamService = streamService;
    }

    @PostMapping(path = "/ask", produces = "application/json")
    public Answer ask(@RequestBody @Valid Question question) {
        return questionService.askQuestion(question);
    }

    @PostMapping(path = "/ask/stream", produces = "text/event-stream")
    public Flux<String> askStream(@RequestBody @Valid Question question) {
        return streamService.askQuestion(question);
    }

}
