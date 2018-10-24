package org.novasearch.tutorials.labs2018;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.DoubleConstValueSource;
import org.apache.lucene.queries.function.valuesource.DoubleFieldSource;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Lab1_Baseline {
    String indexPath = "./index";
    String docPath = "./data/Answers.csv";

    boolean create = true;

    private IndexWriter idx;

    public static void main(String[] args) {

        //Analyzer analyzer = new StandardAnalyzer(); //converts text documents to tokens   For Lab1
        Lab2_Analyser analyzer = new Lab2_Analyser(); //For Lab2

        Similarity similarity = new ClassicSimilarity();    //Compares the query to the documents

        Lab1_Baseline baseline = new Lab1_Baseline();

        // Create a new index
        baseline.openIndex(analyzer, similarity);   //creates an in-disk index
        baseline.indexDocuments();  //parses the documents and puts them in them index
        baseline.close();
        
        // Search the index
        baseline.indexSearch(analyzer, similarity); //Search the index
    }

	public void openIndex(Analyzer analyzer, Similarity similarity) {
		try {
			// ====================================================
			// Configure the index to be created/opened
			//
			// IndexWriterConfig has many options to be set if needed.
			//
			// Example: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setSimilarity(similarity);
			if (create) {
				// Create a new index, removing any
				// previously indexed documents:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			}

			// ====================================================
			// Open/create the index in the specified location
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			idx = new IndexWriter(dir, iwc);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void indexDocuments() {
		if (idx == null)
			return;

		// ====================================================
		// Parse the Answers data
		try (BufferedReader br = new BufferedReader(new FileReader(docPath))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine(); // The first line is dummy
			line = br.readLine();

			// ====================================================
			// Read documents
			while (line != null) {
				int i = line.length();

				// Search for the end of document delimiter
				if (i != 0)
					sb.append(line);
				sb.append(System.lineSeparator());
				if (((i >= 2) && (line.charAt(i - 1) == '"') && (line.charAt(i - 2) != '"'))
						|| ((i == 1) && (line.charAt(i - 1) == '"'))) {
					// Index the document
					indexDoc(sb.toString());

					// Start a new document
					sb = new StringBuilder();
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
				//System.out.println("??????????????????????????????????????????????");
				LocalDate docDate = creationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				
				Date docDate1 = Date.from(docDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
				long diff = now.getTime() - docDate1.getTime();
				int totalDaysElapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				
				// Calculation of total elapsed number of days ends here
				System.out.println(totalDaysElapsed);
				//double decayFactor = 1.0 / Math.pow(2.0, totalDaysElapsed);
				double decayFactor = 1.0 / Math.pow((1.0 + (1.0/(float)totalDaysElapsed)), (float)totalDaysElapsed);
				
				// JOANA DECAY
				doc.add(new DoubleDocValuesField("decayField", decayFactor));
				
				// BAD INSTRUCTIONS
				// not using //doc.add(new DoublePoint("decayField", decayFactor)); // these don't work
				// not using //doc.add(new StoredField("decayField", decayFactor)); // these don't work
				

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
			// I added this line below
			doc.add(new TextField("Body", body, Field.Store.YES)); // Does this mean terms are also stored?
			
			// Add vector of terms for Body
			// Create own FieldType to store Term Vectors
		    

		// ====================================================
		// Add the document to the index
			if (idx.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
				System.out.println("adding " + AnswerId);
				// Don't know how to use this // float doc_boost = doc.getField(body).boost();
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

	// ====================================================
    // Comment and refactor this method yourself
    public void indexSearch(Analyzer analyzer, Similarity similarity) {

        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            // Get number of documents in index
            int nDocsInReader = reader.numDocs();
            
            System.out.println("----------------------------------------------------------------------");
            System.out.println("# docs in index = "+nDocsInReader);
            System.out.println("----------------------------------------------------------------------");
            // end of new part
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(similarity);

            QueryParser parser = new QueryParser("Body", analyzer);
            try (BufferedReader br = new BufferedReader(new FileReader("./eval/queries.offline.txt"))) {

                String line = br.readLine();
                String simString = similarity.toString().replace("(", "_").replace(")", "_").replace(" ","_").replace("=", "").replaceAll("," , "_");
                String filename = "./eval/"+simString+".txt";
                File resultFile = new File(filename);
                
                //File resultFile = new File("./eval/classicSim.txt");
                resultFile.delete();
                resultFile.createNewFile();
                int counter = 0;
                while (line != null) {
                    int questionID = Integer.parseInt(line.substring(0, line.indexOf(":")));

                    if (line == null || line.length() == -1) {
                        break;
                    }

                    line = line.trim();
                    if (line.length() == 0) {
                        break;
                    }

                    Query query;
                    
                    // JOANA DECAY
                    ValueSource val = new DoubleFieldSource("decayField");
                    FunctionQuery myFuncQuery = new FunctionQuery(val);
                    
                    try {
                        query = parser.parse(line);
                    } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                        System.out.println("Error parsing query string.");
                        continue;
                    }
                    
                    // JOANA DECAY
                    query = new CustomScoreQuery(query, myFuncQuery);

                    TopDocs results = searcher.search(query, 100);
                    ScoreDoc[] hits = results.scoreDocs;

                    long numTotalHits = results.totalHits;
                    System.out.println(numTotalHits + " total matching documents");

                    try (FileWriter fw = new FileWriter(resultFile, true);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw)) {
                        if(counter == 0)
                            out.println("QueryID\t\t\tQ0\t\t\tDocID\t\t\tRank\t\t\tScore\t\t\tRunID");
                        for (int j = 0; j < hits.length; j++) {
                            Document doc = searcher.doc(hits[j].doc);
                            long ntotterms = searcher.collectionStatistics("Body").sumTotalTermFreq();
                            
                            //long numTotTermFreq = searcher.collectionStatistics("Body").sumTotalTermFreq();
                            //System.out.println("$$$"+"DocID="+hits[j].doc+"$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                            ////reader.getTermVector(hits[j].doc, "Body").iterator();
                            //System.out.println((float)ntotterms/(float)nDocsInReader);
                            //System.out.println(ntotterms);
                            //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                            //searcher.doc(hits[j].doc).getBinaryValue("Body") // not used
                            System.out.println(searcher.explain(query, hits[j].doc));
                            String answer = doc.get("Body");
                            Integer AnswerId = doc.getField("AnswerId").numericValue().intValue();
                            out.println(questionID + "\t\t\tQ0\t\t\t"+ AnswerId + "\t\t\t" + (j+1) + "\t\t\t" + hits[j].score + "\t\t\trun-1");
                        }
                    } catch (IOException e) {
                        //exception handling left as an exercise for the reader
                    }

                    if (line.equals("")) {
                        break;
                    }
                    counter++;
                    line = br.readLine();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

    public void close() {
        try {
            idx.close();
        } catch (IOException e) {
            System.out.println("Error closing the index.");
        }
    }
}
