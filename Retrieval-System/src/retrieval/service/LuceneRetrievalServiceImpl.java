package retrieval.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.FSDirectory;

import retrieval.helper.RetrievalHelper;
import system.model.DocumentRankModel;
import system.model.QueryModel;
import system.model.QueryResultModel;

public class LuceneRetrievalServiceImpl {
	// private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
	private static Analyzer sAnalyzer = new SimpleAnalyzer();

	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();

	/**
	 * Constructor
	 * 
	 * @param indexDir
	 *            the name of the folder in which the index should be created
	 * @throws java.io.IOException
	 *             when exception creating index.
	 */
	private LuceneRetrievalServiceImpl(String indexDir, String corpusDir, String indexFileLocation) throws IOException {

		FSDirectory dir = FSDirectory.open(Paths.get(indexDir));

		IndexWriterConfig config = new IndexWriterConfig(sAnalyzer);

		writer = new IndexWriter(dir, config);

		RetrievalHelper.initHelper();

	}

	public static LuceneRetrievalServiceImpl getRetrievalService(String indexDir, String corpusDir,
			String indexFileLocation) throws IOException {
		return new LuceneRetrievalServiceImpl(indexDir, corpusDir, indexFileLocation);
	}

	/**
	 * Indexes a file or directory
	 * 
	 * @param fileName
	 *            the name of a text file or a folder we wish to add to the index
	 * @throws java.io.IOException
	 *             when exception
	 */
	public void indexFileOrDirectory(String fileName) throws IOException {
		// ===================================================
		// gets the list of files in a folder (if user has submitted
		// the name of a folder) or gets a single file name (is user
		// has submitted only the file name)
		// ===================================================
		addFiles(new File(fileName));

		int originalNumDocs = writer.numDocs();
		for (File f : queue) {
			FileReader fr = null;
			try {
				Document doc = new Document();

				// ===================================================
				// add contents of file
				// ===================================================
				fr = new FileReader(f);
				doc.add(new TextField("contents", new String(Files.readAllBytes(f.toPath())), Field.Store.YES));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(), Field.Store.YES));

				writer.addDocument(doc);
				//System.out.println("Added: " + f);
			} catch (Exception e) {
				System.out.println("Could not add: " + f);
			} finally {
				fr.close();
			}
		}

		int newNumDocs = writer.numDocs();
		System.out.println("");
		System.out.println("************************");
		System.out.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");

		queue.clear();
	}

	private void addFiles(File file) {

		if (!file.exists()) {
			System.out.println(file + " does not exist.");
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addFiles(f);
			}
		} else {
			String filename = file.getName().toLowerCase();
			// ===================================================
			// Only index text files
			// ===================================================
			if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml")
					|| filename.endsWith(".txt")) {
				queue.add(file);
			} else {
				System.out.println("Skipped " + filename);
			}
		}
	}

	/**
	 * Close the index.
	 * 
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException {
		if (writer != null)
			writer.close();
	}

	public TopDocs getDocumentScores(org.apache.lucene.search.Query q, int resultsSize, IndexSearcher searcher)
			throws ParseException, IOException, org.apache.lucene.queryparser.classic.ParseException {
		TopScoreDocCollector collector = TopScoreDocCollector.create(resultsSize);
		searcher.search(q, collector);
		TopDocs topDocs = collector.topDocs();
		return topDocs;
		//ScoreDoc[] hits = topDocs.scoreDocs;
		
		
		//org.apache.lucene.search.Query q = new QueryParser("contents", sAnalyzer).parse(query.getQuery());

 
//        for(String f : fragments)
//        {
//            System.out.println("Highlight:"+f);
//        }

//		
//		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
//		try {
//			Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(q));
//		   for (int i = 0; i < topDocs.totalHits; i++) {
//		     int id = topDocs.scoreDocs[i].doc;
//		     Document doc = searcher.doc(id);
//
//		     System.out.println("Doc = "+doc.get("path") );
//		     String text = doc.get("contents");
//		     System.out.println("len ------ "+text);
//		     TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "contents", sAnalyzer);
//		     TextFragment[] frag;
//				frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);
//			//highlighter.getBestFragments(tokenStream, text, 3, "...");
//		     for (int j = 0; j < frag.length; j++) {
//		       if ((frag[j] != null) && (frag[j].getScore() > 0)) {
//		         System.out.println("Frag notv = "+(frag[j].toString()));
//		       }
//		     }
////		     //Term vector
////		     text = doc.get("tv");
////		     tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc, "tv", sAnalyzer);
////		     frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);
////		     for (int j = 0; j < frag.length; j++) {
////		       if ((frag[j] != null) && (frag[j].getScore() > 0)) {
////		         System.out.println("Frag tv = "+(frag[j].toString()));
////		       }
////		     }
//		     System.out.println("-------------");
//		   }
//		   } catch (InvalidTokenOffsetsException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		
        
        

	}

	public QueryResultModel search(QueryModel query, String indexLocation, int resultSize)
			throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		org.apache.lucene.search.Query q = new QueryParser("contents", sAnalyzer).parse(query.getQuery());
		TopDocs hits = getDocumentScores(q, resultSize, searcher);
		QueryResultModel queryResults = createQueryResultsfromScoredDocs(hits, searcher, query,q);
		return queryResults;
	}

	public QueryResultModel createQueryResultsfromScoredDocs(TopDocs hits, IndexSearcher searcher, QueryModel query,
			org.apache.lucene.search.Query q) throws IOException {
		QueryResultModel resultModel = new QueryResultModel();
		List<DocumentRankModel> results = new ArrayList<>();
		System.out.println("Found " + hits.scoreDocs.length + " hits.");
		UnifiedHighlighter highlighter = new UnifiedHighlighter(searcher, sAnalyzer);
        String[] fragments = highlighter.highlight("contents", q, hits, 100 );
        System.out.println("fragment size = "+fragments.length);
        if(fragments.length > 0) {
	        for (String fragement : fragments)
	        {
	        		System.out.println("fragments :"+fragement);
	        }
	     }else {
	    	 	System.out.println("No fragment found for query "+query.getQuery()); 
	     }
        int i=0;
		for (ScoreDoc scoredDoc : hits.scoreDocs) {
			int docId = scoredDoc.doc;
			Document d = searcher.doc(docId);
			DocumentRankModel result = new DocumentRankModel();
			result.setDocId(RetrievalHelper.getDocId(d.get("filename")));
			result.setRankScore(scoredDoc.score);
			result.setSnippet(fragments[i++]);
			results.add(result);
			System.out.println("res : "+result);
			
		}
		resultModel.setQueryId(query.getId());
		resultModel.setResults(results);
		return resultModel;
	}

	public void indexFiles(String indexLocation, String corpusLocation) {
		try {
			// try to add file into the index
			this.indexFileOrDirectory(corpusLocation);
			// ===================================================
			// after adding, we always have to call the
			// closeIndex, otherwise the index is not created
			// ===================================================
			this.closeIndex();
		} catch (Exception ex) {
			System.out.println("Cannot create index..." + ex.getMessage());
			System.exit(-1);
		}
	}

}