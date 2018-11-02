import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class Lab5_QueryExpansion extends Lab1_Baseline {

    IndexSearcher searcher = null;

    public void indexSearchQE(Analyzer analyzer, Similarity similarity) {

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);

            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            QueryParser parser = new QueryParser("Body", analyzer);
            while (true) {
                System.out.println("Enter query: ");

                String queryString = in.readLine();

                if (queryString == null || queryString.length() == -1) {
                    break;
                }

                queryString = queryString.trim();
                if (queryString.length() == 0) {
                    break;
                }

                Map<String, Integer> expansionTerms = getExpansionTerms(queryString, 0, 100, analyzer, similarity);

                List ntimes = new ArrayList();
                List words = new ArrayList();
                List temp_ntimes = new ArrayList();

                
                for (Map.Entry<String, Integer> term: expansionTerms.entrySet()) {
                    // This is the minimum frequency
                    if (term.getValue() >= 20) {
                        words.add(term.getKey());
                        ntimes.add(term.getValue());
                        temp_ntimes.add(term.getValue());
                        System.out.println(term.getKey() + " -> " + term.getValue() + " times");
                    }
                }


                // Implement the query expansion by selecting terms from the expansionTerms
                System.out.println("HERE");
                Object maxVal = Collections.max(ntimes);
                System.out.println(maxVal.toString());
                System.out.println(ntimes.indexOf(maxVal));
                //temp_ntimes = ntimes;
                Collections.sort(temp_ntimes);
                List<Integer> top3 = new ArrayList<Integer>(temp_ntimes.subList(temp_ntimes.size() -3,temp_ntimes.size()));
                System.out.println(top3.toString());
                int indexTop1 = ntimes.indexOf(top3.get(2));
                int indexTop2 = ntimes.indexOf(top3.get(1));
                int indexTop3 = ntimes.indexOf(top3.get(0));
                System.out.println(words.get(indexTop1));
                System.out.println(words.get(indexTop2));
                System.out.println(words.get(indexTop3));

                queryString = queryString + " " + words.get(indexTop3).toString() + " " + words.get(indexTop2).toString() + " " + words.get(indexTop1);

                System.out.println(queryString);

                if (queryString.equals("")) {
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


    public Map<String, Integer>  getExpansionTerms(String queryString, int startDoc, int numExpDocs, Analyzer analyzer, Similarity similarity) {

        Map<String, Integer> topTerms = new HashMap<String, Integer>();

        try {
            QueryParser parser = new QueryParser("Body", analyzer);
            Query query;
            try {
                query = parser.parse(queryString);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("Error parsing query string.");
                return null;
            }

            TopDocs results = searcher.search(query, startDoc + numExpDocs);

            ScoreDoc[] hits = results.scoreDocs;

            System.out.println(" baseDoc + numExpDocs = "+(startDoc + numExpDocs));
            System.out.println(" hits.length = "+hits.length);

            long numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            for (int j = startDoc; j < hits.length; j++) {
                Document doc = searcher.doc(hits[j].doc);
                String answer = doc.get("Body");
                Integer AnswerId = doc.getField("AnswerId").numericValue().intValue();

                TokenStream stream = analyzer.tokenStream("field", new StringReader(answer));

                // get the CharTermAttribute from the TokenStream
                CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

                try {
                    stream.reset();

                    // print all tokens until stream is exhausted
                    while (stream.incrementToken()) {
                        String term = termAtt.toString();
                        Integer termCount = topTerms.get(term);
                        if (termCount == null)
                            topTerms.put(term, 1);
                        else
                            topTerms.put(term, ++termCount);
                    }

                    stream.end();
                } finally {
                    stream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(topTerms.size());
        return topTerms;
    }

    public static void main(String[] args) {

        Lab5_QueryExpansion baseline = new Lab5_QueryExpansion();

        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();

        // Search the index
        baseline.indexSearchQE(analyzer, similarity);
    }

}
