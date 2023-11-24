package dev.kodewana.preprocessor.controller;


import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import dev.kodewana.preprocessor.proxy.BlobStorageProxy;
import dev.kodewana.preprocessor.proxy.DocumentAnalysisProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/content")
@Slf4j
public class ContentController {

    @Autowired
    BlobStorageProxy proxy;

    @Autowired
    DocumentAnalysisProxy documentAnalysisProxy;

    private static String tableToHtml(DocumentTable table) {
        StringBuilder tableHtml = new StringBuilder("<table>");

        Map<Integer, Map<Integer, DocumentTableCell>> tableRows = new TreeMap<>();
        table.getCells()
             .forEach(cell -> {
                 tableRows.putIfAbsent(cell.getRowIndex(), new TreeMap<>());
                 tableRows.get(cell.getRowIndex())
                          .putIfAbsent(cell.getColumnIndex(), cell);
             });

        tableRows.forEach((rowIdx, row) -> {
            tableHtml.append("<tr>");
            row.forEach((colIdx, col) -> {
                var tag = (col.getKind() == DocumentTableCellKind.COLUMN_HEADER || col.getKind() == DocumentTableCellKind.ROW_HEADER) ? "th" : "td";
                String spanText = "";
                if (Optional.ofNullable(col.getRowSpan())
                            .orElse(0) > 1) {
                    spanText += " rowSpan=%d".formatted(col.getRowSpan());
                }
                if (Optional.ofNullable(col.getColumnSpan())
                            .orElse(0) > 1) {
                    spanText += " colSpan=%d".formatted(col.getColumnSpan());
                }
                tableHtml.append("<%s%s>%s</%s>".formatted(tag, spanText, HtmlUtils.htmlEscape(col.getContent()), tag));
            });
            tableHtml.append("</tr>");
        });


        return tableHtml.append("</table>")
                        .toString();
    }

    @GetMapping("/{name}")
    public ResponseEntity<String> helloWorld(@PathVariable String name) {

        BinaryData data = proxy.getBlob(name);
        SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutResultPoller = documentAnalysisProxy.analyzeDocument(data);
        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();
        var offset = 0;
        var pageMap = new HashMap<String, String>();
        for (int index = 0; index < analyzeLayoutResult.getPages()
                                                       .size(); index++) {
            DocumentPage page = analyzeLayoutResult.getPages()
                                                   .get(index);
            int pageNum = index + 1;

            List<DocumentTable> tableOnPage = Optional.ofNullable(analyzeLayoutResult.getTables())
                                                      .orElse(new ArrayList<>())
                                                      .stream()
                                                      .filter(tb -> tb.getBoundingRegions()
                                                                      .getFirst()
                                                                      .getPageNumber() == pageNum)
                                                      .toList();

            var pageOffset = page.getSpans()
                                 .getFirst()
                                 .getOffset();
            var pageLength = page.getSpans()
                                 .getFirst()
                                 .getLength();
            var tableChars = new int[pageLength];
            Arrays.fill(tableChars, -1);

            for (int tableId = 0; tableId < tableOnPage.size(); tableId++) {
                var table = tableOnPage.get(tableId);

                for (var span : table.getSpans()) {
                    int finalTableId = tableId;
                    IntStream.range(0, span.getLength())
                             .forEachOrdered(i -> {
                                 var idx = span.getOffset() - pageOffset + i;
                                 if (idx >= 0 && idx < pageLength) {
                                     tableChars[idx] = finalTableId;
                                 }
                             });
                }
            }

            StringBuilder pageText = new StringBuilder();
            Set<Integer> addedTables = new HashSet<>();
            for (int idx = 0; idx < tableChars.length; idx++) {
                var tableId = tableChars[idx];
                if (tableId == -1) {
                    pageText.append(analyzeLayoutResult.getContent()
                                                       .charAt(pageOffset + idx));
                } else if (!addedTables.contains(tableId)) {
                    pageText.append(tableToHtml(tableOnPage.get(tableId)));
                    addedTables.add(tableId);
                }
            }

            pageText.append(" ");
            pageMap.put("%d-%d".formatted(pageNum, offset), pageText.toString());
            offset += pageText.length();
        }

        return null;
    }
}
