package indexer.controller;

import java.util.logging.Logger;

import indexer.helper.DocumentHelper;

public class IndexController {
	private static Logger LOGGER = Logger.getLogger(IndexController.class.getName());
	private static String documentLocation = "";
	private static String corpusLocation = "";
	private static boolean parseStopping = false;
	private static boolean parsePunctuation = true;
	private static boolean parseCaseFolding = true;
	private static boolean printInvertedIndex = true;
	private static boolean printTf = true;
	private static boolean printDf = true;
	private static boolean printTokenInfo = true;
	private static String taskType = "all";

	// documentLocation: raw html documents
	// C:\Users\rahul\Desktop\Information-Retrieval\cacm
	// corpusLocation is made at the root location in a directory called corpus

	public static void main(String args[]) {
		try {

			initArgs(args);
			DocumentHelper helper = new DocumentHelper();
			LOGGER.info("Selected task type is : " + taskType);
			switch (taskType) {
			case "task1":
				helper.createCorpus(documentLocation, corpusLocation, parsePunctuation, parseCaseFolding,
						parseStopping);
				break;
			case "task2":
				printTf = false;
				printDf = false;
				helper.indexFiles(corpusLocation, printInvertedIndex, printTf, printDf, printTokenInfo, parseStopping);
				break;
			case "task3":
				helper.initIndexAndPrintTermTfAndDf(DocumentHelper.getParentfileLocation(corpusLocation),
						parseStopping);
				break;
			case "all":
				helper.createCorpus(documentLocation, corpusLocation, parsePunctuation, parseCaseFolding,
						parseStopping);
				helper.indexFiles(corpusLocation, printInvertedIndex, printTf, printDf, printTokenInfo, parseStopping);
				break;
			case "stop":
				System.out.println("Building Stopped Corpus...");
				parseStopping = true;
				helper.createCorpus(documentLocation, corpusLocation, parsePunctuation, parseCaseFolding,
						parseStopping);
				helper.indexFiles(corpusLocation, printInvertedIndex, printTf, printDf, printTokenInfo, parseStopping);
				break;
			case "stem":
				System.out.println("Reading Stemmed documents...");
				helper.createStemmedCorpus(documentLocation, corpusLocation);
				helper.indexFiles(corpusLocation, printInvertedIndex, printTf, printDf, printTokenInfo, parseStopping);
				break;
			default:
				helper.createCorpus(documentLocation, corpusLocation, parsePunctuation, parseCaseFolding,
						parseStopping);
				helper.indexFiles(corpusLocation, printInvertedIndex, printTf, printDf, printTokenInfo, parseStopping);
				break;
			}
			LOGGER.info("I am done");
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void initArgs(String args[]) {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("Need document location to be passed as parameter");
		} else {
			documentLocation = args[0];
			corpusLocation = getCorpusDirectory(documentLocation);
		}
		if (args.length >= 2) {
			taskType = args[1];
			System.out.println("Task Type: ");
			LOGGER.info("task type is set to:" + taskType);
		}
		if (args.length >= 3) {
			parsePunctuation = Boolean.parseBoolean(args[2]);
			LOGGER.info("Punctuation handling is set to:" + parsePunctuation);
		}
		if (args.length >= 4) {
			parseCaseFolding = Boolean.parseBoolean(args[3]);
			LOGGER.info("Case folding is set to:" + parseCaseFolding);
		}
		if (args.length >= 5) {
			corpusLocation = args[4] + "/";
			LOGGER.info("Corpus location is set to:" + corpusLocation);
			if (documentLocation.equals(corpusLocation)) {
				throw new IllegalArgumentException("Document and corpus directory can not be same.");
			}
		}
	}

	private static String getCorpusDirectory(String arg0) {
		return DocumentHelper.getParentfileLocation(arg0) + "/corpus/";
	}

}
