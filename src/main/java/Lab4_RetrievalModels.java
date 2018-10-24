package org.novasearch.tutorials.labs2018;

import java.util.List;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.search.similarities.Similarity;

public class Lab4_RetrievalModels extends Lab1_Baseline {

    public static void main(String[] args) {

        // ===================================
        // Default analyzer
        Analyzer analyzer = new StandardAnalyzer();


		// ===================================
        // Select the retrieval model function
        // Similarity similarity = new ClassicSimilarity();
        // Similarity similarity = new BM25Similarity();
        // Similarity similarity = new LMDirichletSimilarity();
        // Similarity similarity = new LMJelinekMercerSimilarity(lambda);
        
        // Para gerar os ficheiros com as metricas do LMJelinekMercer com 
        /*for (int j = 1; j < 12; j++) {
        	float lambda = (float)(j-1)/10;
        	Similarity similarity = new LMJelinekMercerSimilarity(lambda);
        	Lab1_Baseline baseline = new Lab1_Baseline();
        	baseline.indexSearch(analyzer, similarity);
        }*/
        
        /*List<Integer> mus = Arrays.asList(10, 100, 500, 1000, 5000);
        
        for (int miu : mus) {
        	Similarity similarity = new LMDirichletSimilarity(miu);
        	Lab1_Baseline baseline = new Lab1_Baseline();
        	baseline.indexSearch(analyzer, similarity);
        }*/
        
        /*List<Float> bs = Arrays.asList(0.0f, 0.25f, 0.5f, 0.75f, 1.0f);
        List<Float> k1s = Arrays.asList(0.0f, 0.5f, 1.0f, 1.25f, 1.5f, 2.0f);
        
        for (float ib : bs) {
        	for (float ik : k1s) {
        		Similarity similarity = new BM25Similarity(ik,ib);
        		Lab1_Baseline baseline = new Lab1_Baseline();
        		baseline.indexSearch(analyzer, similarity);
        	}
        }*/
        
        // For testing purposes of the decay function
        Similarity similarity = new BM25Similarity();
		Lab1_Baseline baseline = new Lab1_Baseline();
        
        // ===================================
        // The indexing process will use the provided analyzer and ranking function
        // Lab1_Baseline baseline = new Lab1_Baseline();
        // These lines should be commented since we only want to work on the search part
        baseline.openIndex(analyzer, similarity);
        baseline.indexDocuments();
        baseline.close();

        // ===================================
        // The search process will use the provided analyzer and ranking function
        baseline.indexSearch(analyzer, similarity);
    }


}
