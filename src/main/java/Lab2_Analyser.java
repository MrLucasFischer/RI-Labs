/**
 *
 */

import java.util.List;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author jmag
 */
public class Lab2_Analyser extends Analyzer {

    /**
     * An unmodifiable set containing some common English words that are not
     * usually useful for searching.
     */
    static List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
            "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
            "these", "they", "this", "to", "was", "will", "with");
    static CharArraySet stopSet = new CharArraySet(stopWords, false);

    /**
     * Default maximum allowed token length
     */
    private int maxTokenLength = 25;

    /**
     * Builds an analyzer with the default stop words
     */
    public Lab2_Analyser() {

    }


    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {


        // THE FIELD IS IGNORED
        // ___BUT___
        // you can provide different TokenStremComponents according to the fieldName

//        final StandardTokenizer src = new StandardTokenizer();
        final WhitespaceTokenizer src = new WhitespaceTokenizer();
//        final UAX29URLEmailTokenizer src = new UAX29URLEmailTokenizer();

        TokenStream tok = null;

        tok = new StandardFilter(src);                    // text into non punctuated text
        tok = new LowerCaseFilter(tok);                    // changes all text into lowercase
        tok = new StopFilter(tok, stopSet);                // removes stop words
//        tok = new ShingleFilter(tok, 2, 3);                // creates word-grams with neighboring words
        tok = new SnowballFilter(tok, "English");        // stems words according to the specified language
//        tok = new CommonGramsFilter(tok, stopSet);    // creates word-grams with stopwords
//        tok = new EdgeNGramTokenFilter(tok, 2, 5);        // creates word-bounded n-grams
//        tok = new NGramTokenFilter(tok, 2, 5);            // creates unbounded n-grams

        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) {
//                src.setMaxTokenLength(Lab2_Analyser.this.maxTokenLength);
//                super.setReader(reader);
                super.setReader(new HTMLStripCharFilter(reader));
            }
        };
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in);
        result = new LowerCaseFilter(result);
        return result;
    }

    // ===============================================
    // Test the different filters
    public static void main(String[] args) throws IOException {

//        final String text = "This is a demonstration, of the TokenStream Lucene-API,";
        final String text = "<p><a href=\"\"http://www.r-project.org/\"\">http://www.r-project.org/</a></p>\n" +
                "56,NA,2010-07-19T19:42:28Z,22,116,\"<p>Here is how I would explain the basic difference to my grandma:</p>";

//        Document doc = Jsoup.parse(text);
//        String textNoHtml = doc.text();
//        System.out.println(textNoHtml);

        Lab2_Analyser analyzer = new Lab2_Analyser();
        TokenStream stream = analyzer.tokenStream("field", new StringReader(text));

        // get the CharTermAttribute from the TokenStream
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        try {
            stream.reset();

            // print all tokens until stream is exhausted
            while (stream.incrementToken()) {
                System.out.println(termAtt.toString());
            }

            stream.end();
        } finally {
            stream.close();
        }
    }
}
