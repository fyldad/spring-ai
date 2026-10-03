package ru.anblazhnov.springai;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringAiApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringAiApplication.class);

    static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                          VectorStore vectorStore,
                          Neo4jChatMemoryRepository chatMemoryRepository
    ) {

        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentPostProcessors((query,  documents) -> {
                    log.info("retrieved {} documents on query: {}", documents.size(), query);
                    documents.forEach(System.out::println);
                    return documents;
                })
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(20)
                        .build())
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .build())
                .build();

        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMemoryRepository)
                        .build())
                .build();

        return chatClientBuilder
                .defaultAdvisors(ragAdvisor, chatMemoryAdvisor)
                .build();
    }

    @Bean
    public OtlpGrpcSpanExporter otlpHttpSpanExporter(
            @Value("${otlp.tracing.url}") String url) {
        return OtlpGrpcSpanExporter.builder().setEndpoint(url).build();
    }

}
