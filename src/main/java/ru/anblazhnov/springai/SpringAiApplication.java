package ru.anblazhnov.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringAiApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }


    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(20)
                        .build())
//                .queryTransformers(RewriteQueryTransformer.builder()
//                        .chatClientBuilder(chatClientBuilder.clone())
//                        .build())
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .build())
                .build();

        return chatClientBuilder
                .defaultAdvisors(advisor)
                .build();
    }

}
