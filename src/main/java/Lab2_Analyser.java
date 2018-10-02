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
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
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

    private String filterToUse = "N/A";

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
     *
     */
    public Lab2_Analyser() {

    }


    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {


        // THE FIELD IS IGNORED
        // ___BUT___
        // you can provide different TokenStremComponents according to the fieldName

        final StandardTokenizer src = new StandardTokenizer();

        TokenStream tok = null;

        tok = new StandardFilter(src);                    // text into non punctuated text
//        tok = new LowerCaseFilter(tok);                    // changes all text into lowercase
//        tok = new StopFilter(tok, stopSet);                // removes stop words
//        tok = new ShingleFilter(tok, 2, 3);                // creates word-grams with neighboring works
//        tok = new CommonGramsFilter(tok, stopSet);    // creates word-grams with stopwords
//        tok = new NGramTokenFilter(tok, 2, 5);            // creates unbounded n-grams
//        tok = new EdgeNGramTokenFilter(tok, 2, 5);        // creates word-bounded n-grams
//        tok = new SnowballFilter(tok, "English");        // stems workds according to the specified language

        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) {
                src.setMaxTokenLength(Lab2_Analyser.this.maxTokenLength);
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
                "\n" +
                "<p>R is valuable and significant because it was the first widely-accepted Open-Source alternative to big-box packages.  It's mature, well supported, and a standard within many scientific communities.</p>\n" +
                "\n" +
                "<ul>\n" +
                "<li><a href=\"\"http://www.inside-r.org/why-use-r\"\">Some reasons why it is useful and valuable</a> </li>\n" +
                "<li>There are some nice tutorials <a href=\"\"http://gettinggeneticsdone.blogspot.com/search/label/ggplot2\"\">here</a>.</li>\n" +
                "</ul>\n" +
                "\"\n" +
                "9,50,2010-07-19T19:16:27Z,3,13,\"<p><a href=\"\"http://incanter.org/\"\">Incanter</a> is a Clojure-based, R-like platform (environment + libraries) for statistical computing and graphics. </p>\n" +
                "\"\n" +
                "12,5,2010-07-19T19:18:41Z,7,21,\"<p>See my response to <a href=\"\"http://stackoverflow.com/questions/2252144/datasets-for-running-statistical-analysis-on/2252450#2252450\"\">\"\"Datasets for Running Statistical Analysis on\"\"</a> in reference to datasets in R.</p>\n" +
                "\"\n" +
                "13,23,2010-07-19T19:18:56Z,6,18,\"<p>Machine Learning seems to have its basis in the pragmatic - a Practical observation or simulation of reality.  Even within statistics, mindless \"\"checking of models and assumptions\"\" can lead to discarding methods that are useful.</p>\n" +
                "\n" +
                "<p>For example, years ago, the very first commercially available (and working) Bankruptcy model implemented by the credit bureaus was created through a plain old linear regression model targeting a 0-1 outcome.  Technically, that's a bad approach, but practically, it worked.</p>\n" +
                "\"\n" +
                "14,36,2010-07-19T19:19:03Z,3,5,\"<p>I second that Jay. Why is R valuable? Here's a short list of reasons. <a href=\"\"http://www.inside-r.org/why-use-r\"\" rel=\"\"nofollow\"\">http://www.inside-r.org/why-use-r</a>. Also check out <a href=\"\"http://had.co.nz/ggplot2/\"\" rel=\"\"nofollow\"\">ggplot2</a> - a very nice graphics package for R. Some nice tutorials <a href=\"\"http://gettinggeneticsdone.blogspot.com/search/label/ggplot2\"\" rel=\"\"nofollow\"\">here</a>.</p>\n" +
                "\"\n" +
                "15,6,2010-07-19T19:19:46Z,1,16,\"<p>John Cook gives some interesting recommendations. Basically, get percentiles/quantiles (not means or obscure scale parameters!) from the experts, and fit them with the appropriate distribution.</p>\n" +
                "\n" +
                "<p><a href=\"\"http://www.johndcook.com/blog/2010/01/31/parameters-from-percentiles/\"\">http://www.johndcook.com/blog/2010/01/31/parameters-from-percentiles/</a></p>\n" +
                "\"\n" +
                "16,8,2010-07-19T19:22:31Z,3,16,\"<p>Two projects spring to mind:</p>\n" +
                "\n" +
                "<ol>\n" +
                "<li><a href=\"\"http://www.mrc-bsu.cam.ac.uk/bugs/\"\">Bugs</a> - taking (some of) the pain out of Bayesian statistics. It allows the user to focus more on the model and a bit less on MCMC.</li>\n" +
                "<li><a href=\"\"http://www.bioconductor.org/\"\">Bioconductor</a> - perhaps the most popular statistical tool in Bioinformatics. I know it's a R repository, but there are a large number of people who want to learn R, just for Bioconductor. The number of packages available for cutting edge analysis, make it second to none.</li>\n" +
                "</ol>\n" +
                "\"\n" +
                "18,36,2010-07-19T19:24:18Z,7,39,\"<p>Also see the UCI machine learning Data Repository.</p>\n" +
                "\n" +
                "<p><a href=\"\"http://archive.ics.uci.edu/ml/\"\">http://archive.ics.uci.edu/ml/</a></p>\n" +
                "\"\n" +
                "19,55,2010-07-19T19:24:21Z,7,13,\"<p><a href=\"\"http://www.gapminder.org/data/\"\">Gapminder</a> has a number (430 at the last look) of datasets, which may or may not be of use to you.</p>\n" +
                "\"\n" +
                "20,37,2010-07-19T19:24:35Z,2,4,\"<p>The assumption of normality assumes your data is normally distributed (the bell curve, or gaussian distribution). You can check this by plotting the data or checking the measures for kurtosis (how sharp the peak is) and skewdness (?) (if more than half the data is on one side of the peak).</p>\n" +
                "\"\n" +
                "24,61,2010-07-19T19:26:13Z,3,19,\"<p>For doing a variety of MCMC tasks in Python, there's <a href=\"\"http://code.google.com/p/pymc/\"\">PyMC</a>, which I've gotten quite a bit of use out of.  I haven't run across anything that I can do in BUGS that I can't do in PyMC, and the way you specify models and bring in data seems to be a lot more intuitive to me.</p>\n" +
                "\"\n" +
                "27,68,2010-07-19T19:28:12Z,7,4,\"<p><a href=\"\"http://mathforum.org/workshops/sum96/data.collections/datalibrary/data.set6.html\"\" rel=\"\"nofollow\"\">http://mathforum.org/workshops/sum96/data.collections/datalibrary/data.set6.html</a></p>\n" +
                "\"\n" +
                "28,NA,2010-07-19T19:28:12Z,3,6,\"<p><a href=\"\"http://www.gnu.org/software/gsl/\"\" rel=\"\"nofollow\"\">GSL</a> for those of you who wish to program in C / C++ is a valuable resource as it provides several routines for random generators, linear algebra etc. While GSL is primarily available for Linux there are also ports for Windows. (See: <a href=\"\"http://gladman.plushost.co.uk/oldsite/computing/gnu_scientific_library.php\"\" rel=\"\"nofollow\"\">http://gladman.plushost.co.uk/oldsite/computing/gnu_scientific_library.php</a> and <a href=\"\"http://david.geldreich.free.fr/dev.html\"\" rel=\"\"nofollow\"\">http://david.geldreich.free.fr/dev.html</a>)</p>\n" +
                "\"\n" +
                "29,36,2010-07-19T19:28:15Z,17,6,\"<p>Contingency table (chi-square). Also Logistic Regression is your friend - use dummy variables. </p>\n" +
                "\"\n" +
                "32,5,2010-07-19T19:29:06Z,25,14,\"<p>I recommend R (see <a href=\"\"http://cran.r-project.org/web/views/TimeSeries.html\"\">the time series view on CRAN</a>).  </p>\n" +
                "\n" +
                "<p>Some useful references:</p>\n" +
                "\n" +
                "<ul>\n" +
                "<li><a href=\"\"http://cran.r-project.org/doc/contrib/Farnsworth-EconometricsInR.pdf\"\">Econometrics in R</a>, by Grant Farnsworth</li>\n" +
                "<li><a href=\"\"http://stackoverflow.com/questions/1714280/multivariate-time-series-modelling-in-r/1715488#1715488\"\">Multivariate time series modelling in R</a></li>\n" +
                "</ul>\n" +
                "\"\n" +
                "38,61,2010-07-19T19:32:28Z,35,2,\"<p>If your mean value for the Poisson is 1500, then you're very close to a normal distribution; you might try using that as an approximation and then modelling the mean and variance separately.</p>\n" +
                "\"\n" +
                "41,83,2010-07-19T19:33:13Z,26,9,\"<p>A quote from <a href=\"\"http://en.wikipedia.org/wiki/Standard_deviation\"\" rel=\"\"nofollow\"\">Wikipedia</a>.</p>\n" +
                "\n" +
                "<blockquote>\n" +
                "  <p>It shows how much variation there is from the \"\"average\"\" (mean, or expected/budgeted value). A low standard deviation indicates that the data points tend to be very close to the mean, whereas high standard deviation indicates that the data is spread out over a large range of values.</p>\n" +
                "</blockquote>\n" +
                "\"\n" +
                "42,80,2010-07-19T19:33:19Z,3,14,\"<p><a href=\"\"http://www.cs.waikato.ac.nz/ml/weka\"\">Weka</a> for data mining - contains many classification and clustering algorithms in Java.</p>\n" +
                "\"\n" +
                "43,74,2010-07-19T19:33:37Z,25,7,\"<p>R is great, but I wouldn't really call it \"\"windows based\"\" :) That's like saying the cmd prompt is windows based. I guess it is technically in a window...</p>\n" +
                "\n" +
                "<p>RapidMiner is far easier to use [1]. It's a free, open-source, multi-platform, GUI. Here's a video on time series forecasting:</p>\n" +
                "\n" +
                "<p><a href=\"\"http://rapidminerresources.com/index.php?page=financial-time-series-modelling---part-1\"\">http://rapidminerresources.com/index.php?page=financial-time-series-modelling---part-1</a></p>\n" +
                "\n" +
                "<p>Also, don't forget to read:</p>\n" +
                "\n" +
                "<p><a href=\"\"http://www.forecastingprinciples.com/\"\">http://www.forecastingprinciples.com/</a></p>\n" +
                "\n" +
                "<p>[1] No, I don't work for them. </p>\n" +
                "\"\n" +
                "45,55,2010-07-19T19:34:44Z,40,5,\"<p>The <a href=\"\"http://en.wikipedia.org/wiki/Mersenne_twister\"\" rel=\"\"nofollow\"\">Mersenne Twister</a> is one I've come across and used before now.</p>\n" +
                "\"\n" +
                "46,62,2010-07-19T19:35:04Z,26,0,\"<p>A standard deviation is the square root of the second central moment of a distribution. A central moment is the expected difference from the expected value of the distribution. A first central moment would usually be 0, so we define a second central moment as the expected value of the squared distance of a random variable from its expected value. </p>\n" +
                "\n" +
                "<p>To put it on a scale that is more in line with the original observations, we take the square root of that second central moment and call it the standard deviation. </p>\n" +
                "\n" +
                "<p>Standard deviation is a property of a population. It measures how much average \"\"dispersion\"\" there is to that population. Are all the obsrvations clustered around the mean, or are they widely spread out? </p>\n" +
                "\n" +
                "<p>To estimate the standard deviation of a population, we often calculate the standard deviation of a \"\"sample\"\" from that population. To do this, you take observations from that population, calculate a mean of those observations, and then calculate the square root of the average squared deviation from that \"\"sample mean\"\". </p>\n" +
                "\n" +
                "<p>To get an unbiased estimator of the variance, you don't actually calculate the average squared deviation from the sample mean, but instead, you divide by (N-1) where N is the number of observations in your sample. Note that this \"\"sample standard deviation\"\" is not an unbiased estimator of the standard deviation, but the square of the \"\"sample standard deviation\"\" is an unbiased estimator of the variance of the population. </p>\n" +
                "\"\n" +
                "49,5,2010-07-19T19:36:52Z,33,6,\"<p>You don't need to install any packages because this is possible with base-R functions.  Have a look at <a href=\"\"http://www.stat.ucl.ac.be/ISdidactique/Rhelp/library/ts/html/arima.html\"\">the arima function</a>.  </p>\n" +
                "\n" +
                "<p>This is a basic function of <a href=\"\"http://en.wikipedia.org/wiki/Box%E2%80%93Jenkins\"\">Box-Jenkins analysis</a>, so you should consider reading one of the R time series text-books for an overview; my favorite is Shumway and Stoffer. \"\"<a href=\"\"http://rads.stackoverflow.com/amzn/click/0387293175\"\">Time Series Analysis and Its Applications: With R Examples</a>\"\".</p>\n" +
                "\"\n" +
                "55,56,2010-07-19T19:41:39Z,30,9,\"<p>The <a href=\"\"http://en.wikipedia.org/wiki/Diehard_tests\"\" rel=\"\"nofollow\"\">Diehard Test Suite</a> is something close to a Golden Standard for testing random number generators. It includes a number of tests where a good random number generator should produce result distributed according to some know distribution against which the outcome using the tested generator can then be compared.</p>\n" +
                "\n" +
                "<p><strong>EDIT</strong></p>\n" +
                "\n" +
                "<p>I have to update this since I was not exactly right:\n" +
                "Diehard might still be used a lot, but it is no longer maintained and not state-of-the-art anymore. NIST has come up with <a href=\"\"http://csrc.nist.gov/groups/ST/toolkit/rng/index.html\"\" rel=\"\"nofollow\"\">a set of improved tests</a> since.</p>\n" +
                "\"\n" +
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
