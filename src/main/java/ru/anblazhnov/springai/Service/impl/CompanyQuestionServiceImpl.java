package ru.anblazhnov.springai.Service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.CompanyQuestionService;
import ru.anblazhnov.springai.Service.QuestionService;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.CompanyQuestion;
import ru.anblazhnov.springai.model.Question;

import java.util.Optional;

@Service
public class CompanyQuestionServiceImpl implements CompanyQuestionService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    public CompanyQuestionServiceImpl(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClientBuilder = chatClientBuilder;
        this.vectorStore = vectorStore;
    }

    @Value("classpath:/templates/companyMasterTemplate.st")
    Resource masterTemplate;

    @Override
    public String askQuestion(CompanyQuestion question) {

        ChatClient chatClient = getChatClient(question);

        return chatClient.prompt()
                .system(spec -> spec
                        .text(masterTemplate)
                        .param("company", question.company())
                )
                .user(question.question())
                .call()
                .content();
    }

    private ChatClient getChatClient(CompanyQuestion question) {

        String expression = "scope == 'iflex'";
        if (question.module() != null) {
            expression += " && module == '" + question.module() + "'";
        }

        return chatClientBuilder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(20)
                                .query(question.question())
                                .filterExpression(expression)
                                .build())
                        .build())
                .build();

    }

}
