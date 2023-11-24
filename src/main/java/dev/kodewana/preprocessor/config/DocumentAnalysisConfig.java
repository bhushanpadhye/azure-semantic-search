package dev.kodewana.preprocessor.config;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentAnalysisConfig {

    private final String endpoint;

    private final AzureKeyCredential azureKeyCredential;

    public DocumentAnalysisConfig(@Value("${azure.cognitive.document.service.name}") String serviceName, @Value("${azure.cognitive.document.service.credentials}") String accessKey) {
        this.endpoint = "https://%s.cognitiveservices.azure.com".formatted(serviceName);
        this.azureKeyCredential = new AzureKeyCredential(accessKey);
    }

    @Bean
    @ConditionalOnProperty(name = "azure.cognitive.document.tracing.enabled", havingValue = "false")
    public DocumentAnalysisClient documentRecognizerDefaultClient() {
        return new DocumentAnalysisClientBuilder().endpoint(endpoint)
                                                  .credential(azureKeyCredential)
                                                  .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.cognitive.document.tracing.enabled", havingValue = "true")
    public DocumentAnalysisClient tracingDocumentRecognizerDefaultClient() {

        var httpLogOption = new HttpLogOptions();
        httpLogOption.setPrettyPrintBody(true);
        httpLogOption.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        return new DocumentAnalysisClientBuilder().endpoint(endpoint)
                                                  .credential(azureKeyCredential)
                                                  .httpLogOptions(httpLogOption)
                                                  .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.cognitive.document.tracing.enabled", havingValue = "false")
    public DocumentAnalysisAsyncClient documentRecognizerAsyncClient() {
        return new DocumentAnalysisClientBuilder().endpoint(endpoint)
                                                  .credential(azureKeyCredential)
                                                  .buildAsyncClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.cognitive.document.tracing.enabled", havingValue = "true")
    public DocumentAnalysisAsyncClient tracingDocumentRecognizerAsyncClient() {

        var httpLogOption = new HttpLogOptions();
        httpLogOption.setPrettyPrintBody(true);
        httpLogOption.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        return new DocumentAnalysisClientBuilder().endpoint(endpoint)
                                                  .credential(azureKeyCredential)
                                                  .httpLogOptions(httpLogOption)
                                                  .buildAsyncClient();
    }


}
