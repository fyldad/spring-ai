package ru.anblazhnov.springai.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import ru.anblazhnov.springai.Service.impl.QuestionServiceImpl;
import ru.anblazhnov.springai.model.Answer;
import ru.anblazhnov.springai.model.Question;

import java.io.IOException;
import java.nio.charset.Charset;

@EnableWireMock(
        @ConfigureWireMock(baseUrlProperties = "gigachat.base.url")
)
@SpringBootTest(
        properties = "spring.ai.gigachat.base-url=${gigachat.base.url}"
)
class QuestionServiceImplTest {

    @Value("classpath:/test-openai-response.json")
    Resource resource;
    @Autowired
    ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() throws IOException {
        String changedResource = resource.getContentAsString(Charset.defaultCharset());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(changedResource);
        WireMock.stubFor(WireMock.post("/chat/completions")
                .willReturn(ResponseDefinitionBuilder.okForJson(node)));
    }

    @Test
    void askQuestion() {
        QuestionServiceImpl questionServiceImpl = new QuestionServiceImpl(chatClientBuilder);
        Answer answer = questionServiceImpl.askQuestion(new Question("hello"));

        Assertions.assertNotNull(answer);
        Assertions.assertEquals("hello", answer.answer());

    }
}