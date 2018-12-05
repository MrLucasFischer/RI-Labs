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
            //String firstParagraph = body.substring() //FirstParagraph
            doc.add(new TextField("Body", body, Field.Store.YES));

            doc.add(new IntPoint("Length", body.split(" ").length));
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

	private static class PerFieldSimilarity extends PerFieldSimilarityWrapper {

        private Map<String, Similarity> similarityPerField = new HashMap<>();
        private Similarity defaultSim;

		public PerFieldSimilarity(Similarity defaultSim) {
            this.defaultSim = defaultSim;
			similarityPerField.put("Body", new BM25Similarity());
//			similarityPerField.put("FirstSentence", new LMDirichletSimilarity());
		}

		@Override
		public Similarity get(String field) {
		    return similarityPerField.getOrDefault(field, defaultSim);
		}
	}

	public static void main(String[] args) {

		// ===================================
		// The per field retrieval model

//		Similarity similarity = new PerFieldSimilarity(new ClassicSimilarity());
        Similarity similarity = new LMJelinekMercerSimilarity(0.1f);
        ArrayList<Similarity> similarityList = new ArrayList<>();
        similarityList.add(new ClassicSimilarity());
        similarityList.add(new BM25Similarity());
        similarityList.add(new BM25Similarity(1.5f, 0.75f));
        similarityList.add(new BM25Similarity(0.5f, 0.0f));
        similarityList.add(new BM25Similarity(1.5f, 0.0f));
        similarityList.add(new LMJelinekMercerSimilarity(0.9f));
        similarityList.add(new LMJelinekMercerSimilarity(0.7f));
        similarityList.add(new LMJelinekMercerSimilarity(0.1f));
        similarityList.add(new LMJelinekMercerSimilarity(1.0f));
        similarityList.add(new LMJelinekMercerSimilarity(1.0f));
        similarityList.add(new LMDirichletSimilarity(10));
        similarityList.add(new LMDirichletSimilarity(100));
        similarityList.add(new LMDirichletSimilarity(1000));
        similarityList.add(new LMDirichletSimilarity(5000));


		// ===================================

		// The per field parser
		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		analyzerPerField.put("Body", new Lab2_Analyser());
//		analyzerPerField.put("FirstParagraph", new KeywordAnalyzer());
		Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);



		for(Similarity sim : similarityList) {
            // ===================================
            // The indexing process will use the provided analyzer and retrieval model
            Lab6_IndexingMultipleFields baseline = new Lab6_IndexingMultipleFields();
            baseline.openIndex(analyzer, sim);
            baseline.indexDocuments();
            baseline.close();


            // ===================================
            // The search process will use the provided analyzer and retrieval model
            baseline.indexSearch(analyzer, sim);
        }
	}
	//TODO perguntar ao prof sobre o Length

}
