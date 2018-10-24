import org.apache.lucene.search.similarities.Similarity;
import java.util.List;
import java.util.Arrays;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

public class Lab4_RetrievalModels extends Lab1_Baseline {

    public static void main(String[] args) {


        //Analyzer analyzer = new StandardAnalyzer();
        Lab2_Analyser analyzer = new Lab2_Analyser();

        // Para gerar os ficheiros com as metricas do LMJelinekMercer com
        for (int j = 1; j < 12; j++) {
            float lambda = (float) (j - 1) / 10;
            Similarity similarity = new LMJelinekMercerSimilarity(lambda);
            Lab1_Baseline baseline = new Lab1_Baseline();
//            baseline.openIndex(analyzer, similarity);   //creates an in-disk index
//            baseline.indexDocuments();  //parses the documents and puts them in them index
//            baseline.close();
            baseline.indexSearch(analyzer, similarity);
        }

        List<Integer> mus = Arrays.asList(10, 100, 500, 1000, 5000);

        for (int miu : mus) {
            Similarity similarity = new LMDirichletSimilarity(miu);
            Lab1_Baseline baseline = new Lab1_Baseline();
//            baseline.openIndex(analyzer, similarity);   //creates an in-disk index
//            baseline.indexDocuments();  //parses the documents and puts them in them index
//            baseline.close();
            baseline.indexSearch(analyzer, similarity);
        }

        List<Float> bs = Arrays.asList(0.0f, 0.25f, 0.5f, 0.75f, 1.0f);
        List<Float> k1s = Arrays.asList(0.0f, 0.5f, 1.0f, 1.25f, 1.5f, 2.0f);

        for (float ib : bs) {
            for (float ik : k1s) {
                Similarity similarity = new BM25Similarity(ik, ib);
                Lab1_Baseline baseline = new Lab1_Baseline();
//                baseline.openIndex(analyzer, similarity);   //creates an in-disk index
//                baseline.indexDocuments();  //parses the documents and puts them in them index
//                baseline.close();
                baseline.indexSearch(analyzer, similarity);
            }
        }
    }


}
