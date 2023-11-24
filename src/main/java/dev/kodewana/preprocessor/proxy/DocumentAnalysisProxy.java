package dev.kodewana.preprocessor.proxy;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentAnalysisProxy {

    private final DocumentAnalysisClient client;

    private final String modelId;

    public DocumentAnalysisProxy(DocumentAnalysisClient client,
                                 @Value("${azure.cognitive.document.service.model.id}") String modelId) {
        this.client = client;
        this.modelId = modelId;
    }

    public SyncPoller<OperationResult, AnalyzeResult> analyzeDocument(BinaryData document) {
        return this.client.beginAnalyzeDocument(modelId, document);
    }
}
