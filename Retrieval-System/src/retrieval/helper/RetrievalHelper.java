package retrieval.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import indexer.helper.DocumentHelper;
import system.model.DocumentEvaluationModel;
import system.model.DocumentRankModel;
import system.model.DocumentTermModel;
import system.model.EvaluationResultModel;
import system.model.IndexModel;
import system.model.QueryModel;
import system.model.QueryResultModel;
import system.model.SystemEvaluationModel;
import system.model.TermIndexModel;

public class RetrievalHelper extends DocumentHelper {

	// private static Map<Integer, String> docIdMap = new HashMap<>();
	// private static Map<Integer, Integer> docLengthMap = new HashMap<>();
	private static Logger LOGGER = Logger.getLogger(RetrievalHelper.class.getName());
	// private static Map<String, List<IndexModel>> unaryIndexMap = new
	// HashMap<>();
	private static String COMMON_WORDS = ".\\common_words.txt";
	private static String CACM_RAW_QUERIES = ".\\cacm_raw_queries.txt";
	private static String CACM_STEM_QUERIES = ".\\cacm_stem.queries.txt";
	private static double IDF_DEFAULT = 1.5;

	private RetrievalHelper() {
		// do nothing;
	}

	public static void initHelper() {
		try {
			if (docTermFreqMap.size() == 0) {
				docTermFreqMap = getDocTermMap(unaryIndexMap);
			}
		} catch (Exception e) {
			LOGGER.severe("Failed initializing helper");
			e.printStackTrace();
		}
	}

	public static <T> void writeToJsonStream(String fileLoction, String fileName, List<T> tfTable) {
		try {
			Gson gson = new Gson();
			LOGGER.info("Writing json data to file " + fileLoction + "/" + fileName);
			JsonWriter writer = new JsonWriter(
					new OutputStreamWriter(new FileOutputStream(new File(fileLoction + "/" + fileName))));
			writer.setIndent("  ");
			writer.beginArray();
			for (T tfModel : tfTable) {
				gson.toJson(tfModel, tfModel.getClass(), writer);
			}

			writer.endArray();
			writer.close();
		} catch (Exception e) {
			LOGGER.severe("Error writing file");
			e.printStackTrace();
		}

	}

	public static <T> void writeToJsonStream(String fileLoction, String fileName, T data) {
		try {
			Gson gson = new Gson();
			LOGGER.info("Writing json data to file " + fileLoction + "/" + fileName);
			JsonWriter writer = new JsonWriter(
					new OutputStreamWriter(new FileOutputStream(new File(fileLoction + "/" + fileName))));
			writer.setIndent("  ");
			writer.beginArray();
			gson.toJson(data, data.getClass(), writer);
			writer.endArray();
			writer.close();
		} catch (Exception e) {
			LOGGER.severe("Error writing file");
			e.printStackTrace();
		}
	}

	public static List<QueryModel> getStemmedQueryList() throws FileNotFoundException, IOException {
		String fileName = RetrievalHelper.CACM_STEM_QUERIES;
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			List<QueryModel> queryList = new ArrayList<>();
			System.out.println("Reading file: " + fileName);
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(currentLine);
				QueryModel query = new QueryModel();
				if (st.hasMoreTokens()) {
					query.setId(Integer.parseInt(st.nextToken()));
				}
				StringBuilder sb = new StringBuilder();
				while (st.hasMoreTokens()) {
					sb.append(st.nextToken() + " ");
				}
				System.out.println("query obtained is " + sb.toString().trim());
				String parsedQuery = DocumentHelper.parsePunctuation(DocumentHelper.parseText(sb.toString().trim()));
				query.setQuery(parsedQuery);
				String[] queryArray = parseQuery(query);
				sb.delete(0, sb.length());
				for (String word : queryArray) {
					sb.append(word+" ");
				}
				query.setQuery(sb.toString());
				queryList.add(query);
			}

			return queryList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static Integer getDocId(String docName) {
		Integer docId = null;
		for (Map.Entry<Integer, String> entry : docIdMap.entrySet()) {
			if (entry.getValue().equals(docName)) {
				docId = entry.getKey();
				break;
			}
		}
		if (docId == null) {
			LOGGER.warning("doc id for this document not found" + docName);
		}
		return docId;
	}

	public static String getParentfileLocation(String fileLocation) {
		// System.out.println("file location = "+fileLocation);
		File f = new File(fileLocation);
		System.out.println("file path=" + f.getAbsolutePath());
		return f.getParent();
	}

	public static Map<String, List<IndexModel>> readJsonStream(String fileLocation) throws IOException {
		try {
			// LOGGER.info("reading file");
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(new File(fileLocation))));
			Map<String, List<IndexModel>> indexMap = new HashMap<>();
			reader.beginArray();
			while (reader.hasNext()) {

				TermIndexModel termIndex = gson.fromJson(reader, TermIndexModel.class);
				// LOGGER.info(""+termIndex.getTerm()+"\t
				// "+termIndex.getInvertedList());
				indexMap.put(termIndex.getTerm(), termIndex.getInvertedList());
			}
			reader.endArray();
			reader.close();
			// LOGGER.info("" + indexMap.size());
			return indexMap;
		} catch (IOException e) {
			LOGGER.severe("\n\n\t!!!...Not able to find index files at the given location :" + fileLocation + "\n\n");
			LOGGER.severe("" + e.getMessage());
			throw e;
		}
	}

	public static void readUnaryIndex(String indexedFileLocation) throws IOException {
		if (unaryIndexMap.size() == 0) {
			unaryIndexMap = readJsonStream(indexedFileLocation);
		}
	}

	public static List<IndexModel> getInvertedIndex(String term) {
		if (unaryIndexMap.containsKey(term))
			return unaryIndexMap.get(term);
		else
			return new ArrayList<>();
	}

	public static Map<String, List<IndexModel>> getIndex() {
		return unaryIndexMap;
	}

	// private static String getFileExtension(File file) {
	// String name = file.getName();
	// try {
	// // System.out.println("extension =" +
	// // name.substring(name.lastIndexOf(".") +
	// // 1));
	// return name.substring(name.lastIndexOf(".") + 1);
	// } catch (Exception e) {
	// return "";
	// }
	// }

	/**
	 * 
	 * @param docId
	 * @return document length You might want to cast it into double for
	 *         caluclations
	 */
	public static int getDocLenth(int docId) {
		return docLengthMap.get(docId);
	}

	public static double getAvgDocLength() {
		int totalDocLength = 0;
		for (Map.Entry<Integer, Integer> entry : docLengthMap.entrySet()) {
			totalDocLength += entry.getValue();
		}
		return (double) totalDocLength / docLengthMap.size();
	}

	public static Integer getCollcetionSizeLength() {
		int totalDocLength = 0;
		for (Map.Entry<Integer, Integer> entry : docLengthMap.entrySet()) {
			totalDocLength += entry.getValue();
		}
		return totalDocLength;
	}

	public static int getCollectionSize() {
		return docLengthMap.size();
	}

	/**
	 * 
	 * @param queryResult
	 * @param filename
	 * @param fileLocation
	 * @param resultType
	 */
	public static void printIndex(List<QueryResultModel> queryResult, String fileLocation, String resultType) {
		printIndex(queryResult, fileLocation, resultType, false);
	}

	/**
	 * 
	 * @param queryResult
	 * @param filename
	 * @param fileLocation
	 * @param resultType
	 * @param printSnippet
	 */
	public static void printIndex(List<QueryResultModel> queryResult, String fileLocation, String resultType,
			boolean printSnippet) {

		// query_id Q0 doc_id rank BM25_score system_name

		for (QueryResultModel model : queryResult) {
			int rank = 1;
			StringBuilder sb = new StringBuilder();
			for (DocumentRankModel docRankModel : model.getResults()) {
				sb.append(model.getQueryId() + " Q0 ");
				sb.append(docIdMap.get(docRankModel.getDocId()) + " ");
				sb.append(rank + " " + docRankModel.getRankScore() + " ");
				sb.append(resultType);
				sb.append("\n");
				if (printSnippet) {
					sb.append(docRankModel.getSnippet());
					sb.append("\n\n");
				}
				rank++;
			}
			writeToFile(sb.toString(), resultType + "_" + model.getQueryId(), fileLocation);
		}

	}

	public static void printEvaluatedFile(SystemEvaluationModel systemEvl, String fileLocation) {
		StringBuilder sb = new StringBuilder();
		double p5 = 0.0;
		double p20 = 0.0;
		for (EvaluationResultModel evlResult : systemEvl.getEvaluatedResults()) {
			int rank = 1;
			for (DocumentEvaluationModel docEval : evlResult.getResults()) {
				sb.append(evlResult.getQueryId() + " Q0 ");
				sb.append(docEval.getDocId() + " " + rank);
				if (docEval.isRelevant())
					sb.append(" R ");
				else
					sb.append(" N ");
				sb.append(" Percision: " + docEval.getPrecision());
				sb.append(" Recal: " + docEval.getRecall() + " ");
				sb.append(systemEvl.getModelName() + "\n");
				rank++;
				if (rank == 5)
					p5 = docEval.getPrecision();
				if (rank == 20)
					p20 = docEval.getPrecision();
			}
		}
		sb.append("MAP: " + systemEvl.getMap() + "\n");
		sb.append("MRR: " + systemEvl.getMrr() + "\n");
		sb.append("P@5: " + p5 + "\n");
		sb.append("P@20: " + p20 + "\n");
		writeToFile(sb.toString(), "Eval_" + systemEvl.getModelName(), fileLocation);
	}

	public static void writeToFile(String doc, String filename, String fileLocation) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileLocation + "/" + filename + ".txt"));
			writer.write(doc);

		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}

	}

	public static double getTermFreqInDoc(String term, int docId) {
		int tf = 0;
		List<IndexModel> invetedIndex = RetrievalHelper.getInvertedIndex(term);
		for (IndexModel model : invetedIndex) {
			if (model.getDocId() == docId) {
				tf = model.getTf();
			}
		}
		return tf;
	}

	public static double getIdf(String word) {
		if (RetrievalHelper.getInvertedIndex(word) != null) {
			double noOfdocs = RetrievalHelper.getInvertedIndex(word).size();
			double totalDocs = RetrievalHelper.getCollectionSize();
			return Math.log((1 + totalDocs) / (1 + noOfdocs));
		} else {
			return IDF_DEFAULT;
		}
	}

	public static double calculateQueryFrequency(String[] queryTerms, String term) {
		double freq = 0;
		for (String nextTerm : queryTerms) {
			if (nextTerm.equals(term))
				freq++;
		}
		return freq;
	}

	private static Map<Integer, List<DocumentTermModel>> getDocTermMap(Map<String, List<IndexModel>> unaryIndexMap) {
		Map<Integer, List<DocumentTermModel>> docTermMap = new HashMap<>();
		for (Map.Entry<String, List<IndexModel>> entry : unaryIndexMap.entrySet()) {
			List<IndexModel> invertedIndex = entry.getValue();
			Iterator<IndexModel> itr = invertedIndex.iterator();

			while (itr.hasNext()) {
				IndexModel indexModel = itr.next();
				if (docTermMap.containsKey(indexModel.getDocId())) {
					List<DocumentTermModel> list = docTermMap.get(indexModel.getDocId());
					DocumentTermModel model = new DocumentTermModel();
					model.setTerm(entry.getKey());
					model.setTf(indexModel.getTf());
					list.add(model);
					docTermMap.put(indexModel.getDocId(), list);
				} else {
					List<DocumentTermModel> list = new ArrayList<>();
					DocumentTermModel model = new DocumentTermModel();
					model.setTerm(entry.getKey());
					model.setTf(indexModel.getTf());
					list.add(model);
					docTermMap.put(indexModel.getDocId(), list);
				}
			}
		}
		// sort the map values
		for (Map.Entry<Integer, List<DocumentTermModel>> entry : docTermMap.entrySet()) {
			docTermMap.put(entry.getKey(), entry.getValue().stream().sorted().collect(Collectors.toList()));
		}
		LOGGER.info("" + docTermMap);
		return docTermMap;

	}

	/*
	 * this method returns the terms in a document in sorted order of their
	 * frequency
	 */
	public static List<DocumentTermModel> getTermFreqMap(int docId) {
		return docTermFreqMap.get(docId);
	}
	/*
	 * Citation : Took this code from World Wide Web, to perform sorting in Map
	 * based on value. URL :
	 * https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-
	 * java
	 */

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	// GIVEN: a QueryModel having a query Id and a query
	// WHERE: tokenizer tokenizes the given query using '//s+' as the delimiter
	// RETURNS: the given query in a tokenized form in an array of tokens
	public static String[] parseQuery(QueryModel query) {
		StringTokenizer tokenizer = new StringTokenizer(query.getQuery(), " ");
		Set<String> tokenList = new HashSet<String>();
		while (tokenizer.hasMoreTokens()) {
			String nextToken = tokenizer.nextToken();
			tokenList.add(nextToken);
		}
		return Arrays.copyOf(tokenList.toArray(), tokenList.toArray().length, String[].class);
	}

	// GIVEN: an absolute path of the file
	// RETURNS: a file object
	public static File getFile(String absPath) {
		File textFile = null;
		try {
			textFile = new File(absPath);
			if (!textFile.exists()) {
				textFile.createNewFile();
			}
			return textFile;
		} catch (IOException io) {
			System.out.println("RetrievalHelper::getFile -- Error Message: IOException.");
		}
		return textFile;
	}

	// GIVEN: an absolute path of the file
	// RETURNS: all the lines of the text file as a Set.
	private static Set<String> parseFileContentToString() {
		Set<String> lineSet = new HashSet<String>();
		if (Files.isDirectory(Paths.get(RetrievalHelper.COMMON_WORDS))) {
			System.out.println("RetrievalHelper::parseFileContentToString -- Error Message: Given path of a directory. "
					+ "Expected path of a file.");
		} else if (FileUtils.getFile(RetrievalHelper.COMMON_WORDS).getName().contains(".txt")) {
			try {
				List<String> lines = Files.readAllLines(Paths.get(RetrievalHelper.COMMON_WORDS));
				lineSet.addAll(lines);
			} catch (IOException io) {
				System.out.println("RetrievalHelper::parseFileContentToString -- Error Message: IOException.");
			}
		} else {
			System.out
					.println("RetrievalHelper::parseFileContentToString -- Error Message:Please provide a text file.");
		}
		return lineSet;
	}

	// GIVEN: the text in a document
	// RETURNS: the same text but stop words excluded
	public static String removeStopWordsFromDoc(String docText) {
		Set<String> commonWords = RetrievalHelper.parseFileContentToString();
		StringTokenizer tokens = new StringTokenizer(docText, " ");
		StringBuffer str = new StringBuffer();

		while (tokens.hasMoreTokens()) {
			String word = tokens.nextToken();
			if (commonWords.contains(word))
				continue;
			else
				str.append(word + " ");
		}
		return str.toString().trim();
	}

	// GIVEN:
	// RETURNS:
	public static List<QueryModel> parseQueriesFromXML() {
		List<QueryModel> queryList = new ArrayList<>();
		try (Stream<String> lines = Files.lines(Paths.get(RetrievalHelper.CACM_RAW_QUERIES),
				Charsets.toCharset("UTF-8"))) {
			StringBuffer sb = new StringBuffer();
			lines.forEach((l) -> {
				sb.append(l + System.lineSeparator());
			});
			Elements docs = Jsoup.parse(sb.toString()).select("DOC");
			for (Element element : docs) {
				int queryId = Integer.parseInt(element.select("DOCNO").text());
				String query = element.ownText().toLowerCase().trim();
				String parsedQuery = DocumentHelper.parsePunctuation(DocumentHelper.parseText(query));
				QueryModel queryModel = new QueryModel();
				queryModel.setId(queryId);
				queryModel.setQuery(parsedQuery);
				queryList.add(queryModel);
			}
		} catch (IOException io) {
			System.out.println("RetrievalHelper::parseQueriesFromXML -- ErrorMessage:IO Exception -- Invalid Path");
		}
		return queryList;
	}

	/**
	 * 
	 * Expects list in a sorted order
	 * 
	 * @param result
	 */
	public static List<DocumentRankModel> getTopNResults(List<DocumentRankModel> results, int n) {
		List<DocumentRankModel> topResults = new ArrayList<>();
		for (DocumentRankModel result : results) {
			if (topResults.size() < n)
				topResults.add(result);
			else
				break;
		}
		return topResults;
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public static Map<Integer,List<String>> getSetOfPairs(QueryModel query) {
		Map<Integer,List<String>> pairMap = new HashMap<>();
		String[] queryTerms = query.getQuery().split("\\s");
		String previousTerm="";
		int count= 1;
		for(String term: queryTerms) {
			List<String> pairList =new ArrayList<>();
			if(!previousTerm.equals("")) {
				pairList.add(previousTerm);
				pairList.add(term);
				pairMap.put(count, pairList);
				count++;
			}
			previousTerm = term;
		}
		return pairMap;
		
	}

	public static String[] removeStopWordsFromQuery(String[] terms) {
		Set<String> commonWords = RetrievalHelper.parseFileContentToString();
		List<String> termList =new ArrayList<>();

		for(String term : terms) {
			if (commonWords.contains(term))
				continue;
			else
				termList.add(term);
		}
		
		return Arrays.copyOf(termList.toArray(), termList.toArray().length, String[].class);
	}


}
