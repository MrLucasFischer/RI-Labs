import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class Lab7_SearchingMultipleFields extends Lab6_IndexingMultipleFields {

    // ====================================================
    // Comment and refactor this method yourself
    public void indexSearch(Analyzer analyzer, Similarity similarity) {

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            String[] fields = {"Body", "FirstSentence"};
            Map<String,Float> model = new HashMap<>();
            model.put("Body", 0.8f);
            model.put("FirstSentence", 0.2f);

            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, model);
            while (true) {
                System.out.println("Enter query: ");

                String line = in.readLine();

                if (line == null || line.length() == -1) {
                    break;
                }

                line = line.trim();
                if (line.length() == 0) {
                    break;
                }

                Query query;
                try {
                    query = parser.parse(line);
                } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                    System.out.println("Error parsing query string.");
                    continue;
                }

                TopDocs results = searcher.search(query, 5);
                ScoreDoc[] hits = results.scoreDocs;

                long numTotalHits = results.totalHits;
                System.out.println(numTotalHits + " total matching documents");

                for (int j = 0; j < hits.length; j++) {
                    Document doc = searcher.doc(hits[j].doc);
                    String answer = doc.get("Body");
                    Integer Id = doc.getField("AnswerId").numericValue().intValue();
                    System.out.println("DocId: " + Id);
                    System.out.println("DocAnswer: " + answer);
                    System.out.println();
                }

                if (line.equals("")) {
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            try {
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();

        Lab7_SearchingMultipleFields baseline = new Lab7_SearchingMultipleFields();

        // Search the index
        baseline.indexSearch(analyzer, similarity);
    }


}
