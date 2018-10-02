import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class Lab4_RetrievalModels extends Lab1_Baseline {

    public static void main(String[] args) {

        // ===================================
        // Default analyzer
        Analyzer analyzer = new StandardAnalyzer();


        // ===================================
        // Select the retrieval model function
        Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new BM25Similarity();
        // Similarity similarity = new LMDirichletSimilarity();

        // ===================================
        // The indexing process will use the provided analyzer and ranking function
        Lab1_Baseline baseline = new Lab1_Baseline();
        baseline.openIndex(analyzer, similarity);
        baseline.indexDocuments();
        baseline.close();

        // ===================================
        // The search process will use the provided analyzer and ranking function
        baseline.indexSearch(analyzer, similarity);
    }


}
