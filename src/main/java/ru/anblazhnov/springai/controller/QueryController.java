package ru.anblazhnov.springai.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.anblazhnov.springai.Service.CompanyQuestionService;
import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
import ru.anblazhnov.springai.Service.impl.QuestionStreamServiceImpl;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.CompanyQuestion;
import ru.anblazhnov.springai.model.Question;

@RestController
public class QueryController {

    private final QuestionServiceImpl questionService;
    private final QuestionStreamServiceImpl streamService;
    private final CompanyQuestionService companyQuestionService;

    public QueryController(QuestionServiceImpl questionService,
                           QuestionStreamServiceImpl streamService,
                           CompanyQuestionService companyQuestionService) {
        this.questionService = questionService;
        this.streamService = streamService;
        this.companyQuestionService = companyQuestionService;
    }

    @PostMapping(path = "/ask", produces = "application/json")
    public Answer ask(@RequestBody @Valid Question question) {
        return questionService.askQuestion(question);
    }

    @PostMapping(path = "ask/company", produces = "application/json")
    public String companyAsk(@RequestBody @Valid CompanyQuestion question) {
        return companyQuestionService.askQuestion(question);
    }

    @PostMapping(path = "/ask/stream", produces = "text/event-stream")
    public Flux<String> askStream(@RequestBody @Valid Question question) {
        return streamService.askQuestion(question);
    }

}
