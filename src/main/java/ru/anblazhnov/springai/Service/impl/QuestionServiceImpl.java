package ru.anblazhnov.springai.Service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.QuestionService;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public QuestionServiceImpl(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @Value("classpath:/templates/masterTemplate.st")
    Resource masterTemplate;

    public Answer askQuestion(Question question) {

        ResponseEntity<ChatResponse, Answer> responseEntity = chatClient.prompt()
                .system(spec -> spec
                        .text(masterTemplate)
                        .param("scope", question.scope())
                        .param("vectorData", getVectorStorageData(question))
                )
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

    private String getVectorStorageData(Question question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question.question())
                .filterExpression(
                        new FilterExpressionBuilder()
                                .eq("scope", question.scope()).build())
                .build();

        log.info("search request: {}", searchRequest);

        List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);

        log.info("found {} similar documents", similarDocs.size());

        return similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private void logUsage(Usage usage) {
        log.info("Token usage: prompt={}, generation={}, total={}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());

    }

}
