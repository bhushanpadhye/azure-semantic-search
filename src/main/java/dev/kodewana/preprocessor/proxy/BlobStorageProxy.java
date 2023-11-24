package dev.kodewana.preprocessor.proxy;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import org.springframework.stereotype.Component;

@Component
public class BlobStorageProxy {

    private final BlobContainerClient client;


    public BlobStorageProxy(BlobContainerClient client) {
        this.client = client;
    }

    public BinaryData getBlob(String blobName) {
        return this.client.getBlobClient(blobName)
                .downloadContent();
    }

    public void uploadBlobData(String blobName, BinaryData document) {
        this.client.getBlobClient(blobName)
                .upload(document);
    }
}
