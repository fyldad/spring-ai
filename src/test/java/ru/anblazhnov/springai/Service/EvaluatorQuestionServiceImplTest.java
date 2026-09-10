//package ru.anblazhnov.springai.Service;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
//import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
//import org.springframework.ai.evaluation.EvaluationRequest;
//import org.springframework.ai.evaluation.EvaluationResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
//import ru.anblazhnov.springai.model.Answer;
//import ru.anblazhnov.springai.model.Question;
//
//import java.io.IOException;
//
//@SpringBootTest
//class EvaluatorQuestionServiceImplTest {
//
//    @Autowired
//    private QuestionServiceImpl questionServiceImpl;
//    @Autowired
//    private ChatClient.Builder chatClientBuilder;
//
//    private RelevancyEvaluator relevancyEvaluator;
//    private FactCheckingEvaluator factCheckingEvaluator;
//
//    @BeforeEach
//    void setUp() throws IOException {
//
//        final String prompt = """
//				Evaluate whether or not the following claim is supported by the provided document.
//				Respond with "yes" if the claim is supported, or "no" if it is not.
//				Answer only yes or no without any additional text or symbols.
//
//				Document:
//				{document}
//
//				Claim:
//				{claim}
//			""";
//
//        this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
//        this.factCheckingEvaluator = FactCheckingEvaluator.builder(chatClientBuilder).evaluationPrompt(prompt).build();
//    }
//
//    @Test
//    void evaluateRelevancy() {
//        String userText = "Why is the sky blue?";
//        Question question = new Question("sky", userText);
//        Answer answer = questionServiceImpl.askQuestion(question);
//
//        EvaluationRequest request = new EvaluationRequest(userText, answer.answer());
//        EvaluationResponse response = relevancyEvaluator.evaluate(request);
//
//        Assertions.assertTrue(response.isPass(),
//                String.format("""
//                                ========================================
//                                The answer "%s"
//                                is not considered relevant to the question
//                                "%s".
//                                ========================================
//                                """,
//                        answer.answer(), userText));
//
//
//    }
//
//    @Test
//    public void evaluateFact() {
//        String userText = "Why is the sky blue?";
//        Question question = new Question("sky", userText);
//        Answer answer = questionServiceImpl.askQuestion(question);
//
//        EvaluationRequest request = new EvaluationRequest(userText, answer.answer());
//
//        EvaluationResponse response = factCheckingEvaluator.evaluate(request);
//
//        Assertions.assertTrue(response.isPass(),
//                String.format("""
//                                ========================================
//                                The answer "%s"
//                                is not correct to the question
//                                "%s".
//                                ========================================
//                                """,
//                        answer.answer(), userText));
//    }
//
//}