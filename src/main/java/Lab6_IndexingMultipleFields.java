import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.*;

import java.util.HashMap;
import java.util.Map;

public class Lab6_IndexingMultipleFields extends Lab1_Baseline {

	@Override
	public void indexDoc(String rawDocument) {
		// Implement your parser to extract the body and the first sentence
	}

	private static class perFieldSimilarity extends PerFieldSimilarityWrapper {

		public perFieldSimilarity(Similarity defaultSim) {
			Map<String, Similarity> similarityPerField = new HashMap<>();
			similarityPerField.put("Body", new BM25Similarity());
			similarityPerField.put("FirstSentence", new LMDirichletSimilarity());
		}

		@Override
		public Similarity get(String s) {
			return null;
		}
	}

	public static void main(String[] args) {

		// ===================================
		// The per field retrieval model
		Similarity similarity = new perFieldSimilarity(new ClassicSimilarity());

		// ===================================
		// The per field parser
		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		analyzerPerField.put("Body", new KeywordAnalyzer());
		analyzerPerField.put("FirstSentence", new KeywordAnalyzer());
		Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);

		// ===================================
		// The indexing process will use the provided analyzer and retrieval model
		Lab1_Baseline baseline = new Lab1_Baseline();
		baseline.openIndex(analyzer, similarity);
		baseline.indexDocuments();
		baseline.close();

		// ===================================
		// The search process will use the provided analyzer and retrieval model
		baseline.indexSearch(analyzer, similarity);
	}

}
