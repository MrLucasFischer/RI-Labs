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
        Integer AnswerId = 0;
        try {

            // Extract field Id
            Integer start = 0;
            Integer end = rawDocument.indexOf(',');
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

                // Add decay weight field

                LocalDate docDate = creationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                //Calculate the number of days this document has
                Date docDate1 = Date.from(docDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                long diff = now.getTime() - docDate1.getTime();
                int totalDaysElapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                // DECAY
                // Implementation of the decay function
                double decayFactor = 1.0 / Math.pow((1.0 + (1.0/(float)totalDaysElapsed)), (float)totalDaysElapsed);

                // DECAY
                //doc.add(new DoubleDocValuesField("decayField", decayFactor)); //Add the decayField to the document


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

		public PerFieldSimilarity(Similarity defaultSim) {
			similarityPerField.put("Body", new BM25Similarity());
//			similarityPerField.put("FirstSentence", new LMDirichletSimilarity());
		}

		@Override
		public Similarity get(String field) {
			return similarityPerField.get(field);
		}
	}

	public static void main(String[] args) {

		// ===================================
		// The per field retrieval model
//		Similarity similarity = new PerFieldSimilarity(new ClassicSimilarity());
        Similarity similarity = new ClassicSimilarity();
		// ===================================
		// The per field parser
		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		analyzerPerField.put("Body", new StandardAnalyzer());
//		analyzerPerField.put("FirstParagraph", new KeywordAnalyzer());
		Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);

		// ===================================
		// The indexing process will use the provided analyzer and retrieval model
		Lab6_IndexingMultipleFields baseline = new Lab6_IndexingMultipleFields(); //Substitui aqui o lab1 por lab6
		baseline.openIndex(analyzer, similarity);
		baseline.indexDocuments();
		baseline.close();

		// ===================================
		// The search process will use the provided analyzer and retrieval model
		baseline.indexSearch(analyzer, similarity);
	}

}
