//package ru.anblazhnov.springai.Service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
//import com.github.tomakehurst.wiremock.client.WireMock;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.Resource;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.wiremock.spring.ConfigureWireMock;
//import org.wiremock.spring.EnableWireMock;
//import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
//import ru.anblazhnov.springai.model.Answer;
//import ru.anblazhnov.springai.model.Question;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@EnableWireMock(
//        @ConfigureWireMock(baseUrlProperties = "ollama.base.url")
//)
//@SpringBootTest(
//        properties = "spring.ai.ollama.base-url=${ollama.base.url}"
//)
//class QuestionServiceImplTest {
//
//    @Value("classpath:/test-openai-response.json")
//    Resource resource;
//    @Autowired
//    ChatClient.Builder chatClientBuilder;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        String changedResource = resource.getContentAsString(Charset.defaultCharset());
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode node = objectMapper.readTree(changedResource);
//        WireMock.stubFor(WireMock.post("/api/chat")
//                .willReturn(ResponseDefinitionBuilder.okForJson(node)));
//    }
//
//    @Test
//    void askQuestion() throws IOException {
//        Resource masterTemplate = mock(Resource.class);
//        when(masterTemplate.getContentAsString(Charset.defaultCharset()))
//                .thenReturn("Answer questions about {scope}.");
//        QuestionServiceImpl questionServiceImpl = new QuestionServiceImpl(chatClientBuilder);
//        ReflectionTestUtils.setField(questionServiceImpl, "masterTemplate", masterTemplate);
//        Answer answer = questionServiceImpl.askQuestion(new Question("test", "hello"));
//
//        Assertions.assertNotNull(answer);
//        Assertions.assertEquals("hello", answer.answer());
//
//    }
//}