package ru.anblazhnov.springai.Service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.QuestionService;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;
import ru.anblazhnov.springai.tools.TimeTools;

import java.sql.Time;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);
    private final ChatClient chatClient;

    public QuestionServiceImpl(ChatClient.Builder chatClientBuilder, TimeTools timeTools) {
        this.chatClient = chatClientBuilder
                .defaultTools(timeTools)
                .build();
    }

    @Value("classpath:/templates/masterTemplate.st")
    Resource masterTemplate;

    public Answer askQuestion(Question question) {

        ResponseEntity<ChatResponse, Answer> responseEntity = chatClient
                .prompt()
//                .system(spec -> spec
//                        .text(question.question())
//                )
                .user(question.question())
                .call()
                .responseEntity(Answer.class);

        Optional.of(responseEntity)
                .map(ResponseEntity::getResponse)
                .map(ChatResponse::getMetadata)
                .map(ChatResponseMetadata::getUsage)
                .ifPresent(this::logUsage);

        Answer answer = responseEntity.getEntity();
        log.info(answer.toString());

        return answer;
    }

    private void logUsage(Usage usage) {
        log.info("Token usage: prompt={}, generation={}, total={}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());

    }

}
