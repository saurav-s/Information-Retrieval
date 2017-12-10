package indexer.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import retrieval.helper.RetrievalHelper;
import system.model.DocTokenInfoModel;
import system.model.DocTokenModel;
import system.model.DocumentFrequencyModel;
import system.model.DocumentIdMapperModel;
import system.model.DocumentTermModel;
import system.model.IndexModel;
import system.model.TermFrequencyModel;
import system.model.TermIndexModel;

public class DocumentHelper {

	private static final int UNIGRAM_SIZE = 1;
	private static final int BIGRAM_SIZE = 2;
	private static final int TRIGRAM_SIZE = 3;
	// private static final String CORPUS_FOLDER_PATH = "/corpus/";
	private static final String TAGS_TO_BE_PARSED = "p,h1,h2,h3,h4,h5,h6,pre";
	private static final String UNARY_TOKEN_TYPE = "unary";
	private static final String BINARY_TOKEN_TYPE = "binary";
	private static final String TERNARY_TOKEN_TYPE = "ternary";
	private static final Pattern SPACE_PATTERN = Pattern.compile("[\\s]", Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^\\p{L}0-9- ](?!\\d)");
	private static final Pattern DIGIT_PATTERN = Pattern.compile("[^\\p{L}0-9- .,](?=\\d)");
	private static final boolean parseRelevantTextOnly = true;
	private static Logger LOGGER = Logger.getLogger(DocumentHelper.class.getName());
	protected static Map<Integer, String> docIdMap = new HashMap<>();
	protected static Map<Integer, List<DocumentTermModel>> docTermFreqMap = new HashMap<>();
	protected static Map<Integer, Integer> docLengthMap = new HashMap<>();
	protected static Map<String, List<IndexModel>> unaryIndexMap = new HashMap<>();
	private static Map<String, List<IndexModel>> binaryIndexMap = new HashMap<>();
	private static Map<String, List<IndexModel>> ternaryIndexMap = new HashMap<>();
	private static List<DocTokenModel> docTokenList = new ArrayList<>();

	public boolean writeToCorpusFile(String doc, String filename, String fileLocation) {
		try {
			File f = new File(fileLocation + filename + ".txt");
			// LOGGER.info("writing to file " + filename + " at location" +
			// f.getAbsoluteFile());
			FileUtils.writeStringToFile(f, doc, "UTF-8");
			return true;
		} catch (IOException e) {
			LOGGER.warning(e.getMessage());
			return false;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			return false;
		}
	}

	public Document getDocument(String filePath, String baseUrl) {
		Document doc = null;
		try {
			File input = new File(filePath);
			doc = Jsoup.parse(input, "UTF-8", baseUrl);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return doc;
	}

	public Document getDocument(String fileLocation) {
		return getDocument(fileLocation, "");
	}

	// GIVEN:
	// RETURNS:
	public void createCorpus(String docLocation, String corpusLocation, String baseUrl, boolean parsePunctuation,
			boolean parseCaseFolding, boolean parseStopping) {
		LOGGER.info("trying to create corpus, fetching files from " + docLocation + "\ncreating corpus at "
				+ corpusLocation);
		Path path = Paths.get(docLocation);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				File file = entry.toFile();
				if (file.isFile()
						&& ((getFileExtension(file).equals("html")) || (getFileExtension(file).equals("txt")))) {
					String parsedDoc = parse(getDocument(entry.toAbsolutePath().toString(), baseUrl), parsePunctuation,
							parseCaseFolding, parseStopping);
					writeToCorpusFile(parsedDoc, getFileName(file), corpusLocation);
				} else {
					LOGGER.info("this is intresting, i was only told to read txt files :( ");
				}
			}
			// LOGGER.info("Map size = "+docIdMap.size());
		} catch (DirectoryIteratorException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// LOGGER.info("Returning Documents =" + docList.size());
	}

	private String getFileName(File file) {
		String name = file.getName();
		try {
			// System.out.println("extension =" +
			// name.substring(name.lastIndexOf(".") +
			// 1));
			// return name.substring(name.lastIndexOf(".") + 1);
			return name.substring(0, name.lastIndexOf("."));
		} catch (Exception e) {
			return "";
		}
	}

	// GIVEN:
	// WHERE:
	// RETURNS:
	public void createCorpus(String documentLocation, String corpusLocation, boolean parsePunctuation,
			boolean parseCaseFolding, boolean parseStopping) {
		createCorpus(documentLocation, corpusLocation, "", parsePunctuation, parseCaseFolding, parseStopping);
	}

	public static String getRelevantText(String documentText) {
		StringBuilder sb = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(documentText, " ");
		while (tokenizer.hasMoreTokens()) {
			String nextToken = tokenizer.nextToken();
			if (!nextToken.equalsIgnoreCase("CACM")) {
				sb.append(nextToken + " ");
			} else {
				break;
			}
		}
		return sb.toString();
	}

	private static String parseDocumentText(Document doc) {
		String documentText = doc.select(TAGS_TO_BE_PARSED).text();
		Matcher documentMatcher = SPACE_PATTERN.matcher(documentText);
		String whiteSpaceCleanedDocument = documentMatcher.replaceAll(" ");
		if (parseRelevantTextOnly) {
			whiteSpaceCleanedDocument = getRelevantText(whiteSpaceCleanedDocument);
		}
		// LOGGER.info("\n\nDoctext = \t" + whiteSpaceCleanedDocument);
		return whiteSpaceCleanedDocument;

	}

	private static String parsePunctuation(String whiteSpaceCleanedDocument) {
		StringBuilder str = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(whiteSpaceCleanedDocument, " ");
		// Pattern supTagText = Pattern.compile("\\((\\d*?)\\)|\\[(\\d*?)\\]");
		while (tokenizer.hasMoreTokens()) {
			String nextToken = tokenizer.nextToken();
			Matcher punctuationMatcher = PUNCTUATION_PATTERN.matcher(nextToken);
			String punctuationReplacedString = punctuationMatcher.replaceAll("");
			Matcher digitMatcher = DIGIT_PATTERN.matcher(punctuationReplacedString);
			String digitCleanedString = digitMatcher.replaceAll("");
			str.append(digitCleanedString + " ");
		}
		// LOGGER.info("before updating= \n" + whiteSpaceCleanedDocument +
		// "\nupdated
		// string = \n" + str.toString());
		return str.toString();
	}

	public void indexFiles(String indexedFileLocation, boolean printInvertedIndex, boolean printTermFrequency,
			boolean printDocumentFrequency, boolean printDocTokenInfo, boolean stopping) throws Exception {
		LOGGER.info("trying to index files, fetching corpus from location " + indexedFileLocation);
		initMap(indexedFileLocation);
		Path path = Paths.get(indexedFileLocation);
		int unaryTokenCount;
		int binaryTokenCount;
		int ternaryTokenCount;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				// LOGGER.info("File name found is:" +
				// entry.getFileName().toString());
				if (entry.toFile().isFile() && (getFileExtension(entry.toFile()).equals("txt"))) {
					try (Stream<String> fileStream = Files.lines(entry)) {
						Integer docId = getDocId(entry.getFileName().toString());
						Iterator<String> iterator = fileStream.iterator();
						unaryTokenCount = 0;
						binaryTokenCount = 0;
						ternaryTokenCount = 0;
						while (iterator.hasNext()) {
							String nextLine = iterator.next();
							unaryTokenCount += indexFile(nextLine, unaryIndexMap, docId, UNIGRAM_SIZE);
							binaryTokenCount += indexFile(nextLine, binaryIndexMap, docId, BIGRAM_SIZE);
							ternaryTokenCount += indexFile(nextLine, ternaryIndexMap, docId, TRIGRAM_SIZE);
						}
						updateDocTokenInfo(docId, unaryTokenCount, binaryTokenCount, ternaryTokenCount);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			printData(indexedFileLocation, printInvertedIndex, printTermFrequency, printDocumentFrequency,
					printDocTokenInfo, stopping);
		}
	}

	private void updateDocTokenInfo(int docId, int unaryTokenCount, int binaryTokenCount, int ternaryTokenCount) {
		DocTokenModel docToken = new DocTokenModel();
		docToken.setDocId(docId);

		DocTokenInfoModel unaryTokenInfo = new DocTokenInfoModel();
		unaryTokenInfo.setTokenType(UNARY_TOKEN_TYPE);
		unaryTokenInfo.setTokenCount(unaryTokenCount);

		DocTokenInfoModel binaryTokenInfo = new DocTokenInfoModel();
		binaryTokenInfo.setTokenType(BINARY_TOKEN_TYPE);
		binaryTokenInfo.setTokenCount(binaryTokenCount);

		DocTokenInfoModel terinaryTokenInfo = new DocTokenInfoModel();
		terinaryTokenInfo.setTokenType(TERNARY_TOKEN_TYPE);
		terinaryTokenInfo.setTokenCount(ternaryTokenCount);

		List<DocTokenInfoModel> tokenInfoList = new ArrayList<>();
		tokenInfoList.add(unaryTokenInfo);
		tokenInfoList.add(binaryTokenInfo);
		tokenInfoList.add(terinaryTokenInfo);
		docToken.setTokenInfoList(tokenInfoList);

		docTokenList.add(docToken);
	}

	private void printData(String indexedFileLocation, boolean printInvertedIndex, boolean printTermFrequency,
			boolean printDocumentFrequency, boolean printDocTokenInfo, boolean stopping) {

		if (printInvertedIndex) {
			printInvertedIndex(indexedFileLocation, stopping);
		}
		if (printTermFrequency) {
			printTermFrequency(getParentfileLocation(indexedFileLocation), stopping);
		}
		if (printDocumentFrequency) {
			printDocumentFrequency(getParentfileLocation(indexedFileLocation), stopping);
		}
		if (printDocTokenInfo) {
			printDocTokenInfo(getParentfileLocation(indexedFileLocation), stopping);
		}
	}

	private void printDocTokenInfo(String fileLocation, boolean stopping) {
		if (stopping) {
			PrintTablesToFile(docTokenList, fileLocation, "Stopped_Document_Token_Count.json");
		} else {
			PrintTablesToFile(docTokenList, fileLocation, "Document_Token_Count.json");
		}

	}

	private void printInvertedIndex(String indexedFileLocation, boolean stopping) {
		if (stopping) {
			PrintIndexToFile(unaryIndexMap, getParentfileLocation(indexedFileLocation), "Stopped_Unary_Index.json");
			PrintIndexToFile(binaryIndexMap, getParentfileLocation(indexedFileLocation), "Stopped_Binary_Index.json");
			PrintIndexToFile(ternaryIndexMap, getParentfileLocation(indexedFileLocation), "Stopped_Ternary_Index.json");
		} else {
			PrintIndexToFile(unaryIndexMap, getParentfileLocation(indexedFileLocation), "Unary_Index.json");
			PrintIndexToFile(binaryIndexMap, getParentfileLocation(indexedFileLocation), "Binary_Index.json");
			PrintIndexToFile(ternaryIndexMap, getParentfileLocation(indexedFileLocation), "Ternary_Index.json");
		}

	}

	public void initIndexAndPrintTermTfAndDf(String indexedFileLocation, boolean stopping) throws IOException {
		try {
			LOGGER.info("trying to read index files, fetching files from " + indexedFileLocation);
			unaryIndexMap = readJsonStream(indexedFileLocation, "Unary_Index.json");
			binaryIndexMap = readJsonStream(indexedFileLocation, "Binary_Index.json");
			ternaryIndexMap = readJsonStream(indexedFileLocation, "Ternary_Index.json");
			printTermFrequency(indexedFileLocation, stopping);
			printDocumentFrequency(indexedFileLocation, stopping);
		} catch (IOException e) {
			LOGGER.severe("\nError while reading files: " + e.getMessage());
			throw e;
		}
	}

	private void printTermFrequency(String fileLocation, boolean stopping) {
		if (stopping) {
			PrintTablesToFile(getTermFrequencyTable(unaryIndexMap), fileLocation, "Stopped_Unary_Tf.json");
			PrintTablesToFile(getTermFrequencyTable(binaryIndexMap), fileLocation, "Stopped_Binary_Tf.json");
			PrintTablesToFile(getTermFrequencyTable(ternaryIndexMap), fileLocation, "Stopped_Ternary_Tf.json");
		} else {
			PrintTablesToFile(getTermFrequencyTable(unaryIndexMap), fileLocation, "Unary_Tf.json");
			PrintTablesToFile(getTermFrequencyTable(binaryIndexMap), fileLocation, "Binary_Tf.json");
			PrintTablesToFile(getTermFrequencyTable(ternaryIndexMap), fileLocation, "Ternary_Tf.json");
		}
	}

	private void printDocumentFrequency(String fileLocation, boolean stopping) {
		if (stopping) {
			PrintTablesToFile(getDocumentFrequencyMap(unaryIndexMap), fileLocation, "Stopped_Unary_Df.json");
			PrintTablesToFile(getDocumentFrequencyMap(binaryIndexMap), fileLocation, "Stopped_Binary_Df.json");
			PrintTablesToFile(getDocumentFrequencyMap(ternaryIndexMap), fileLocation, "Stopped_Ternary_Df.json");
		} else {
			PrintTablesToFile(getDocumentFrequencyMap(unaryIndexMap), fileLocation, "Unary_Df.json");
			PrintTablesToFile(getDocumentFrequencyMap(binaryIndexMap), fileLocation, "Binary_Df.json");
			PrintTablesToFile(getDocumentFrequencyMap(ternaryIndexMap), fileLocation, "Ternary_Df.json");
		}
	}

	private String getFileExtension(File file) {
		String name = file.getName();
		try {
			// System.out.println("extension =" +
			// name.substring(name.lastIndexOf(".") +
			// 1));
			return name.substring(name.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return "";
		}
	}

	public static void initMap(String fileLocation) throws Exception {
		Path path = Paths.get(fileLocation);
		int count = 1;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				if (entry.toFile().isFile()) {
					// LOGGER.info("Adding to map, count=" + count + "\t" +
					// entry.getFileName().toString());
					try (Stream<String> fileStream = Files.lines(entry)) {
						// Integer docId =
						// getDocId(entry.getFileName().toString());
						Iterator<String> iterator = fileStream.iterator();
						while (iterator.hasNext()) {
							String nextLine = iterator.next();
							String[] split = nextLine.split("\\s+");
							docLengthMap.put(count, split.length);
						}
					}
					docIdMap.put(count++, entry.getFileName().toString());
				}
			}
		}
		LOGGER.info("Init done...!!!!     Total Documents read = " + docIdMap.size());
		PrintTablesToFile(getDocMapperList(), getParentfileLocation(fileLocation), "docId_Map.json");

	}

	private static List<DocumentIdMapperModel> getDocMapperList() {
		List<DocumentIdMapperModel> docIdMapper = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : docIdMap.entrySet()) {
			DocumentIdMapperModel model = new DocumentIdMapperModel();
			model.setDocId(entry.getKey());
			model.setDocName(entry.getValue());
			docIdMapper.add(model);
		}
		return docIdMapper;
	}

	public static String getDocName(Integer docid) {
		return docIdMap.get(docid);
	}

	public static String getParentfileLocation(String fileLocation) {
		// System.out.println("file location = "+fileLocation);
		File f = new File(fileLocation);
		System.out.println("file path=" + f.getAbsolutePath());
		return f.getParent();
	}

	private Integer getDocId(String uniquefileName) {
		Integer docId = null;
		for (Map.Entry<Integer, String> entry : docIdMap.entrySet()) {
			if (uniquefileName.equals(entry.getValue())) {
				docId = entry.getKey();
				break;
			}
		}
		return docId;
	}

	private void PrintIndexToFile(Map<String, List<IndexModel>> indexMap, String fileLocation, String fileName) {
		try {
			// LOGGER.info("Total number of unique words = " + indexMap.size());
			writeJsonStream(fileLocation, fileName, indexMap);
		} catch (Exception e) {
			LOGGER.severe("Error writing file");
		}

	}

	public void writeJsonStream(String fileLoction, String fileName, Map<String, List<IndexModel>> indexMap) {
		try {
			Gson gson = new Gson();
			LOGGER.info("Writing index to file " + fileLoction + "/" + fileName);
			JsonWriter writer = new JsonWriter(
					new OutputStreamWriter(new FileOutputStream(new File(fileLoction + "/" + fileName))));
			writer.setIndent("  ");
			writer.beginArray();
			for (Map.Entry<String, List<IndexModel>> entry : indexMap.entrySet()) {
				TermIndexModel indexObject = new TermIndexModel();
				indexObject.setTerm(entry.getKey());
				indexObject.setInvertedList(entry.getValue());
				gson.toJson(indexObject, TermIndexModel.class, writer);
			}

			writer.endArray();
			writer.close();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private static <T> void PrintTablesToFile(List<T> table, String fileLocation, String fileName) {
		try {
			// LOGGER.info("Printing table" + fileName + "\tTotal number of
			// unique words = "
			// + table.size());
			writeJsonStream(fileLocation, fileName, table);
		} catch (Exception e) {
			LOGGER.severe("Error writing file" + e.getMessage());
			throw e;
		}

	}

	public static <T> void writeJsonStream(String fileLoction, String fileName, List<T> tfTable) {
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

	public Map<String, List<IndexModel>> readJsonStream(String fileLocation, String fileName) throws IOException {
		try {
			// LOGGER.info("reading file");
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(
					new InputStreamReader(new FileInputStream(new File(fileLocation + "/" + fileName))));
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

	private static int indexFile(String line, Map<String, List<IndexModel>> indexMap, final int docId, int ngramSize) {
		try {
			// all whitespace is already replaced with single ' ' character
			String[] tokens = line.split("\\s");
			String lastToken = null;
			String secondLastToken = null;
			int tokenCount = 0;
			for (String token : tokens) {
				if (!token.isEmpty()) {
					// LOGGER.info("token = "+token);
					if (ngramSize == 1) {
						addTokenToIndex(token, indexMap, docId);
						tokenCount++;
					} else if ((ngramSize == 2) && (lastToken != null)) {
						addTokenToIndex(lastToken + " " + token, indexMap, docId);
						tokenCount++;
					} else if ((ngramSize == 3) && (secondLastToken != null)) {
						addTokenToIndex(secondLastToken + " " + lastToken + " " + token, indexMap, docId);
						tokenCount++;
					} else if (ngramSize != 1 && ngramSize != 2 && ngramSize != 3) {
						throw new UnsupportedOperationException(
								"The ngram size '" + ngramSize + "' is not supported currently");
					}
					secondLastToken = lastToken;
					lastToken = token;
				}
			}
			return tokenCount;
		} catch (Exception e) {
			LOGGER.warning("Error occured " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	private static void addTokenToIndex(String token, Map<String, List<IndexModel>> indexMap, final int docId) {

		IndexModel dummyModel = new IndexModel();
		if (indexMap.containsKey(token)) {
			// token already present updating list
			List<IndexModel> docIdList = indexMap.get(token);
			dummyModel.setDocId(docId);
			if (docIdList.contains(dummyModel)) {
				// doc already present updating list
				Iterator<IndexModel> docItr = docIdList.iterator();
				while (docItr.hasNext()) {
					IndexModel indexModel = docItr.next();
					if (indexModel.getDocId() == docId) {
						int tf = indexModel.getTf();
						indexModel.setTf(++tf);
						break;
					}
				}
			} else {
				// LOGGER.warning("doc not present adding new and updating
				// list");
				IndexModel indexModel = new IndexModel();
				indexModel.setDocId(docId);
				indexModel.setTf(1);
				docIdList.add(indexModel);
			}
		} else {
			IndexModel indexModel = new IndexModel();
			indexModel.setDocId(docId);
			indexModel.setTf(1);
			List<IndexModel> docIdList = new ArrayList<>();
			docIdList.add(indexModel);
			indexMap.put(token, docIdList);
		}
	}

	private static String parseCaseFolding(String documentText) {
		return documentText.toLowerCase();
	}

	// GIVEN:
	// RETURNS:
	public String parse(Document doc, boolean parsePunctuation, boolean parseCaseFolding, boolean parseStopping) {
		String documentText = parseDocumentText(doc);
		if (parsePunctuation) {
			documentText = parsePunctuation(documentText);
		}
		if (parseCaseFolding) {
			documentText = parseCaseFolding(documentText);
		}
		if (parseStopping) {
			documentText = parseStopping(documentText);
		}
		return documentText;
	}

	// GIVEN:
	// RETURNS:
	private String parseStopping(String documentText) {
		String stopWords = RetrievalHelper.removeStopWordsFromDoc(documentText);
		return stopWords;
	}

	private List<TermFrequencyModel> getTermFrequencyTable(Map<String, List<IndexModel>> indexMap) {
		List<TermFrequencyModel> tfTable = new ArrayList<>();
		int frequency;
		for (Map.Entry<String, List<IndexModel>> entry : indexMap.entrySet()) {
			TermFrequencyModel tfModel = new TermFrequencyModel();
			frequency = 0;
			tfModel.setTerm(entry.getKey());
			for (IndexModel model : entry.getValue()) {
				frequency += model.getTf();
			}
			tfModel.setTf(frequency);
			tfTable.add(tfModel);
		}
		Collections.sort(tfTable);
		return tfTable;
	}

	private List<DocumentFrequencyModel> getDocumentFrequencyMap(Map<String, List<IndexModel>> indexMap) {
		List<DocumentFrequencyModel> dfTable = new ArrayList<>();
		for (Map.Entry<String, List<IndexModel>> entry : indexMap.entrySet()) {
			DocumentFrequencyModel dfModel = new DocumentFrequencyModel();
			dfModel.setTerm(entry.getKey());
			List<Integer> docIdList = new ArrayList<>();
			for (IndexModel model : entry.getValue()) {
				docIdList.add(model.getDocId());
			}
			dfModel.setDocIdList(docIdList);
			dfModel.setDf(entry.getValue().size());
			dfTable.add(dfModel);
		}
		Collections.sort(dfTable);
		return dfTable;
	}

}
