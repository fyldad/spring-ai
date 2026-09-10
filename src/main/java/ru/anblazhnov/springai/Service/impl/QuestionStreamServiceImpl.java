package ru.anblazhnov.springai.Service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.anblazhnov.springai.Service.QuestionStreamService;
import ru.anblazhnov.springai.model.Question;

@Service
public class QuestionStreamServiceImpl implements QuestionStreamService {

    private final ChatClient chatClient;

    public QuestionStreamServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Value("classpath:/templates/masterTemplate.st")
    Resource masterTemplate;

    public Flux<String> askQuestion(Question question) {

        return chatClient.prompt()
                .system(spec -> spec
                        .text(masterTemplate)
                        .param("scope", question.scope())
                )
                .user(question.question())
                .stream()
                .content();
    }

}
