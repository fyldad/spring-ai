package ru.anblazhnov.springai.Service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.QuestionService;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final ChatClient chatClient;

    public QuestionServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Value("classpath:/templates/masterTemplate.st")
    Resource masterTemplate;

    public Answer askQuestion(Question question) {

        String answerText = chatClient.prompt()
                .system(spec -> spec
                        .text(masterTemplate)
                        .param("scope", question.scope())
                )
                .user(question.question())
                .call()
                .content();

        return new Answer(question.scope(), answerText);
    }

}
