package ru.anblazhnov.springai.Service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.anblazhnov.springai.Service.QuestionService;
import ru.anblazhnov.springai.exception.AnswerNotRelevantException;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

@Service
public class SelfCheckQuestionService implements QuestionService {

    private final ChatClient chatClient;
    private final RelevancyEvaluator evaluator;

    public SelfCheckQuestionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();

        String prompt = """
                Your task is to evaluate if the response for the query
                is in line with the context information provided.
                You have two options to answer. Either YES or NO.
                Answer YES or NO without any additional text or symbols
                Answer YES, if the response for the query
                is in line with context information otherwise NO.
                Answer only YES or NO with no additional text or symbols.
                
                Query:
                {query}
                
                Response:
                {response}
                
                Context:
                {context}
                """;

        this.evaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(new PromptTemplate(prompt))
                .build();
    }

    @Override
    @Retryable(retryFor = AnswerNotRelevantException.class)
    public Answer askQuestion(Question question) {
        String answerText = chatClient.prompt()
                .user(question.question())
                .call()
                .content();

        evaluateRelevancy(question, answerText);

        return new Answer(question.scope(), answerText);
    }

    @Recover
    public Answer recover(AnswerNotRelevantException e) {
        return new Answer(null, "I'm sorry, I wasn't able to answer the question.");
    }

    private void evaluateRelevancy(Question question, String answer) {
        EvaluationRequest request = new EvaluationRequest(question.question(), answer);
        EvaluationResponse response = evaluator.evaluate(request);
        if (!response.isPass()) {
            throw new AnswerNotRelevantException(question.question(), answer);
        }
    }

}
