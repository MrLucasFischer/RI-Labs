import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Lab6_IndexingMultipleFields extends Lab1_Baseline {

    @Override
    public void indexDoc(String rawDocument) {

        Document doc = new Document();

        // ====================================================
        // Each document is organized as:
        // Id,OwnerUserId,CreationDate,ParentId,Score,Body
        // Now for Lab they will also have an extra field FirstParagraph
        Integer AnswerId = 0;
        try {

            // Extract field Id
            int start = 0;
            int end = rawDocument.indexOf(',');
            String aux = rawDocument.substring(start, end);
            AnswerId = Integer.decode(aux);

            // Index _and_ store the AnswerId field
            doc.add(new IntPoint("AnswerId", AnswerId));
            doc.add(new StoredField("AnswerId", AnswerId));

            // Extract field OwnerUserId
            start = end + 1;
            end = rawDocument.indexOf(',', start);
            aux = rawDocument.substring(start, end);
            Integer OwnerUserId = Integer.decode(aux);
            doc.add(new IntPoint("OwnerUserId", OwnerUserId));

            // Extract field CreationDate
            try {
                start = end + 1;
                end = rawDocument.indexOf(',', start);
                aux = rawDocument.substring(start, end);
                Date creationDate;
                creationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(aux);
                doc.add(new LongPoint("CreationDate", creationDate.getTime()));

            } catch (ParseException e1) {
                System.out.println("Error parsing date for document " + AnswerId);
            }

            // Extract field ParentId
            start = end + 1;
            end = rawDocument.indexOf(',', start);
            aux = rawDocument.substring(start, end);
            Integer ParentId = Integer.decode(aux);
            doc.add(new IntPoint("ParentId", ParentId));

            // Extract field Score
            start = end + 1;
            end = rawDocument.indexOf(',', start);
            aux = rawDocument.substring(start, end);
            Integer Score = Integer.decode(aux);
            doc.add(new IntPoint("Score", Score));

            // Extract field Body
            String body = rawDocument.substring(end + 1);
            doc.add(new TextField("Body", body, Field.Store.YES));

            // Extracting length of document
            doc.add(new IntPoint("Length", body.split(" ").length));

            // Extracting the first sentence of a document
            doc.add(new TextField("FirstSentence", getFirstSentence(body), Field.Store.YES));


            // ====================================================
            // Add the document to the index
            if (idx.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                System.out.println("adding " + AnswerId);
                idx.addDocument(doc);
            } else {
                idx.updateDocument(new Term("AnswerId", AnswerId.toString()), doc);
            }
        } catch (IOException e) {
            System.out.println("Error adding document " + AnswerId);
        } catch (Exception e) {
            System.out.println("Error parsing document " + AnswerId);
        }
    }

    private String getFirstSentence(String body) {
        int periodIdx = body.indexOf(".");

        if (periodIdx > -1 && (body.charAt(periodIdx + 1) == '<' || body.charAt(periodIdx + 1) == ' '))
            return body.substring(0, periodIdx);
        else {
            int questionIdx = body.indexOf("?");
            if (questionIdx > -1 && (body.charAt(questionIdx + 1) == '<' || body.charAt(questionIdx + 1) == ' '))
                return body.substring(0, questionIdx);

            else {
                int exclamationIdx = body.indexOf("!");
                if (exclamationIdx > -1 && (body.charAt(exclamationIdx + 1) == '<' || body.charAt(exclamationIdx + 1) == ' '))
                    return body.substring(0, exclamationIdx);
                else
                    return body;
            }
        }
    }

    private static class PerFieldSimilarity extends PerFieldSimilarityWrapper {

        private Map<String, Similarity> similarityPerField = new HashMap<>();
        private Similarity defaultSim;

        public PerFieldSimilarity(Similarity defaultSim) {
            this.defaultSim = defaultSim;
            similarityPerField.put("Body", new LMJelinekMercerSimilarity(0.9f));
			similarityPerField.put("FirstSentence", new BM25Similarity(1.2f, 0.75f));
        }

        @Override
        public Similarity get(String field) {
            return similarityPerField.getOrDefault(field, defaultSim);
        }
    }
//
//    BM25Similarity(0.5f, 0.0f)
//        LMDirichletSimilarity(1000)
//            LMJelinekMercerSimilarity(0.9f)
//                ClassicSimilarity()

    public static void main(String[] args) {

        // ===================================
        // The per field retrieval model

        Similarity similarity = new PerFieldSimilarity(new ClassicSimilarity());
//        Similarity similarity = new LMJelinekMercerSimilarity(0.1f);

////         ===================================
//
//         The per field parser
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("Body", new Lab2_Analyser());
		analyzerPerField.put("FirstSentence", new Lab2_Analyser());
        Analyzer analyzer = new PerFieldAnalyzerWrapper(new Lab2_Analyser(), analyzerPerField);

//        // ===================================
//        // The indexing process will use the provided analyzer and retrieval model
        Lab6_IndexingMultipleFields baseline = new Lab6_IndexingMultipleFields();
        baseline.openIndex(analyzer, similarity);
        baseline.indexDocuments();
        baseline.close();

        // ===================================
        // The search process will use the provided analyzer and retrieval model
        baseline.indexSearch(analyzer, similarity);
    }
}
