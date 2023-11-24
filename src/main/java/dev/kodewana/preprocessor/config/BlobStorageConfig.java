package dev.kodewana.preprocessor.config;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlobStorageConfig {

    private final String endpoint;

    private final StorageSharedKeyCredential storageSharedKeyCredential;

    private final String containerName;

    public BlobStorageConfig(@Value("${azure.storage.account.name}") String serviceName, @Value("${azure.storage.account.key}") String accessKey, @Value("${azure.storage.container.name}") String containerName) {
        this.endpoint = "https://%s.blob.core.windows.net".formatted(serviceName);
        this.storageSharedKeyCredential = new StorageSharedKeyCredential(serviceName, accessKey);
        this.containerName = containerName;
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.tracing.enabled", havingValue = "false")
    public BlobContainerClient blobContainerDefaultClient() {
        return new BlobContainerClientBuilder().endpoint(endpoint)
                                               .credential(storageSharedKeyCredential)
                                               .containerName(containerName)
                                               .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.tracing.enabled", havingValue = "true")
    public BlobContainerClient tracingBlobContainerDefaultClient() {

        var httpLogOption = new HttpLogOptions();
        httpLogOption.setPrettyPrintBody(true);
        httpLogOption.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        return new BlobContainerClientBuilder().endpoint(endpoint)
                                               .credential(storageSharedKeyCredential)
                                               .httpLogOptions(httpLogOption)
                                               .containerName(containerName)
                                               .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.tracing.enabled", havingValue = "false")
    public BlobContainerAsyncClient blobContainerAsyncClient() {
        return new BlobContainerClientBuilder().endpoint(endpoint)
                                               .credential(storageSharedKeyCredential)
                                               .containerName(containerName)
                                               .buildAsyncClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.tracing.enabled", havingValue = "true")
    public BlobContainerAsyncClient tracingBlobContainerAsyncClient() {

        var httpLogOption = new HttpLogOptions();
        httpLogOption.setPrettyPrintBody(true);
        httpLogOption.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);

        return new BlobContainerClientBuilder().endpoint(endpoint)
                                               .credential(storageSharedKeyCredential)
                                               .httpLogOptions(httpLogOption)
                                               .containerName(containerName)
                                               .buildAsyncClient();
    }
}
