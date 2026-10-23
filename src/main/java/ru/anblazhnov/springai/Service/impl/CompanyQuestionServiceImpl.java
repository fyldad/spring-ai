package ru.anblazhnov.springai.Service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.CompanyQuestionService;
import ru.anblazhnov.springai.model.CompanyQuestion;

import java.util.UUID;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever.FILTER_EXPRESSION;


@Service
public class CompanyQuestionServiceImpl implements CompanyQuestionService {

    private final ChatClient chatClient;

    public CompanyQuestionServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("classpath:/templates/companyMasterTemplate.st")
    Resource masterTemplate;

    @Override
    public String askQuestion(CompanyQuestion question) {

        Filter.Expression expression = getFilterExpression(question);
        String userId = UUID.randomUUID().toString();

        return chatClient.prompt()
//                .system(spec -> spec
//                        .text(masterTemplate)
//                        .param("company", question.company())
//                )
                .user(question.question())
                .advisors(advisorSpec -> {
                            advisorSpec.param(CONVERSATION_ID, userId);
                            if (expression != null) {
                                advisorSpec.param(FILTER_EXPRESSION, expression);
                            }
                        }
                )
                .call()
                .content();
    }

    private Filter.Expression getFilterExpression(CompanyQuestion question) {
        if (question.metaDataFilter() == null) return null;

        var builder = new FilterExpressionBuilder();
        return question.metaDataFilter().entrySet().stream()
                .map(e -> builder.eq(e.getKey(), e.getValue()))
                .reduce(builder::and)
                .map(FilterExpressionBuilder.Op::build)
                .orElse(null);

    }

}
