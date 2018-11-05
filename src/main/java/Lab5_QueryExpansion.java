import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Lab5_QueryExpansion extends Lab1_Baseline {

    IndexSearcher searcher = null;

    public void indexSearchQE(Analyzer analyzer, Similarity similarity) {

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);

            QueryParser parser = new QueryParser("Body", analyzer);

            try (BufferedReader br = new BufferedReader(new FileReader("./eval/queries.offline.txt"))) {
                String queryString = br.readLine();
                String simString = similarity.toString().replace("(", "_").replace(")", "_").replace(" ", "_").replace("=", "").replaceAll(",", "_");
                String filename = "./eval/" + simString + ".txt";
                File resultFile = new File(filename);

                resultFile.delete();
                resultFile.createNewFile();
                int counter = 0;

                while (queryString != null) {
                    int questionID = Integer.parseInt(queryString.substring(0, queryString.indexOf(":")));

                    queryString = queryString.trim();
                    queryString = queryString.substring(queryString.indexOf(":") + 1);
                    if (queryString.length() == 0) {
                        break;
                    }

                    int numberOfTerms = 5;
                    int numberOfDocs = 5; //Using 5 because this is the best value for number of documents
                    Map<String, Integer> negTerms = getExpansionTerms(queryString, 200, 220, analyzer, similarity, null);

                    List<Map.Entry<String, Integer>> topNegative = getTopTerms(negTerms, numberOfTerms);

                    Map<String, Integer> posTerms = getExpansionTerms(queryString, 0, numberOfDocs, analyzer, similarity, topNegative); //for exe1

                    List<Map.Entry<String, Integer>> topTerms = getTopTerms(posTerms, numberOfTerms);
                    // Implement the query expansion by selecting terms from the expansionTerms
                    queryString = expandQuery(queryString, topTerms);
                    System.out.println(queryString);
                    Query query;

                    try {
                        query = parser.parse(queryString); //Parsing the expanded query
                    } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                        System.out.println("Error parsing query string.");
                        continue;
                    }

                    TopDocs results = searcher.search(query, 100);
                    ScoreDoc[] hits = results.scoreDocs;

                    try (FileWriter fw = new FileWriter(resultFile, true);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw)) {
                        if (counter == 0)
                            out.println("QueryID\t\t\tQ0\t\t\tDocID\t\t\tRank\t\t\tScore\t\t\tRunID");
                        for (int j = 0; j < hits.length; j++) {
                            Document doc = searcher.doc(hits[j].doc);
                            int AnswerId = doc.getField("AnswerId").numericValue().intValue();
                            out.println(questionID + "\t\t\tQ0\t\t\t" + AnswerId + "\t\t\t" + (j + 1) + "\t\t\t" + hits[j].score + "\t\t\trun-1");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (queryString.equals("")) {
                        break;
                    }
                    counter++;
                    queryString = br.readLine();
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


    public Map<String, Integer> getExpansionTerms(String queryString, int startDoc, int numExpDocs, Analyzer analyzer, Similarity similarity, List<Map.Entry<String, Integer>> top3Negative) {

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
                        //Filter terms that are in the negative feedback documents
                        if (top3Negative == null || (!top3Negative.get(0).getKey().equals(term) &&
                                !top3Negative.get(1).getKey().equals(term) &&
                                !top3Negative.get(2).getKey().equals(term))) {
                            if (termCount == null)
                                topTerms.put(term, 1);
                            else
                                topTerms.put(term, ++termCount);
                        }
                    }

                    stream.end();
                } finally {
                    stream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topTerms;
    }

    private List<Map.Entry<String, Integer>> getTopTerms(Map<String, Integer> terms, int numberOfTerms) {
        return terms
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("http://"))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(numberOfTerms)
                .collect(Collectors.toList());
    }

    public String expandQuery(String query, List<Map.Entry<String, Integer>> topTerms) {
        return query + String.join(" ", topTerms.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
    }


    public static void main(String[] args) {

        Lab5_QueryExpansion baseline = new Lab5_QueryExpansion();


        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new ClassicSimilarity();

        // Search the index
        baseline.indexSearchQE(analyzer, similarity);
    }

}