package ru.anblazhnov.springai.Service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.CompanyQuestionService;
import ru.anblazhnov.springai.model.CompanyQuestion;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;

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

        String expression = getFilterExpression(question);

        return chatClient.prompt()
                .system(spec -> spec
                        .text(masterTemplate)
                        .param("company", question.company())
                )
                .user(question.question())
                .advisors(advisorSpec ->
                        advisorSpec.param(FILTER_EXPRESSION, expression)
                )
                .call()
                .content();
    }

    private String getFilterExpression(CompanyQuestion question) {
        String expression = "scope == 'iflex'";
        if (question.module() != null) {
            expression += " && module == '" + question.module() + "'";
        }
        return expression;
    }

}
