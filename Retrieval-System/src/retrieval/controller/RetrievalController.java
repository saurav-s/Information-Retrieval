package retrieval.controller;

import java.util.ArrayList;
import java.util.List;

import indexer.controller.IndexController;
import indexer.helper.DocumentHelper;
import retrieval.helper.RetrievalHelper;
import retrieval.service.Bm25RetrievalServiceImpl;
import retrieval.service.LuceneRetrievalServiceImpl;
import retrieval.service.PsuedoExpansionService;
import retrieval.service.QLRMServiceImpl;
import retrieval.service.TermWeightIdfServiceImpl;
import retrieval.system.evaluation.Evaluate;
import system.model.QueryModel;
import system.model.QueryResultModel;
import system.model.SystemEvaluationModel;

public class RetrievalController {
	public static void main(String[] args) throws Exception {

		// indexDir : New Index Will be Generated here.
		// corpusLocation: location of the corpus.
		// indexFileLocation: Lucene's index location
		// rawDocDirectory: directory of raw html documents
		// resultDir : output of the tasks will be printed there.
		String indexDir = args[0];
		String corpusLocation = args[1];
		String indexFileLocation = args[2];
		String rawDocDirectory = args[3];
		String resultDir = args[4];
		String task = args[5];
		List<system.model.QueryModel> queryList;
		if (task.equalsIgnoreCase("base")) {
			String[] indexArgs = { rawDocDirectory };
			IndexController.main(indexArgs);
		} else if (task.equalsIgnoreCase("stem")) {
			DocumentHelper helper = new DocumentHelper();
			helper.createCorpus(rawDocDirectory, corpusLocation, true, true, false);
			helper.indexFiles(corpusLocation, true, true, true, true, false);
		} else if (task.equalsIgnoreCase("stop")) {
			DocumentHelper helper = new DocumentHelper();
			helper.createCorpus(rawDocDirectory, corpusLocation, true, true, true);
			helper.indexFiles(corpusLocation, true, true, true, true, true);
		}
		if (task.equalsIgnoreCase("base")) {
			queryList = RetrievalHelper.parseQueriesFromXML();
		} else if (task.equalsIgnoreCase("stop")) {
			queryList = RetrievalHelper.parseQueriesFromXML();
			List<QueryModel> newQueryList = new ArrayList<>();
			for (QueryModel queryModel : queryList) {
				QueryModel qm = new QueryModel();
				qm.setId(queryModel.getId());
				qm.setQuery(RetrievalHelper.removeStopWordsFromDoc(queryModel.getQuery()));
				newQueryList.add(qm);
			}
			queryList = newQueryList;
		} else {
			queryList = RetrievalHelper.getStemmedQueryList();
		}
		RetrievalHelper.initHelper();
		List<QueryResultModel> bm25QueryResultList = new ArrayList<>();
		Bm25RetrievalServiceImpl bm25RetrievalService = new Bm25RetrievalServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = bm25RetrievalService.getQueryResults(query, 100);
			bm25QueryResultList.add(queryResult);
		}

		TermWeightIdfServiceImpl idfService = new TermWeightIdfServiceImpl();
		List<QueryResultModel> tfIdfQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel tfIdf = idfService.getQueryResults(query, 100);
			tfIdfQueryResultList.add(tfIdf);
		}

		List<QueryResultModel> qlrmQueryList = new ArrayList<>();
		QLRMServiceImpl qlrsr = new QLRMServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = qlrsr.getQueryResults(query, 100);
			qlrmQueryList.add(queryResult);
		}

		List<QueryResultModel> psuedoExpandedList = new ArrayList<>();
		PsuedoExpansionService psuedoRel = new PsuedoExpansionService();
		for (QueryModel query : queryList) {
			QueryResultModel pusedoQuery = psuedoRel.performQueryExpandsion(query, "BM25", 100);
			psuedoExpandedList.add(pusedoQuery);
		}

		Evaluate evl = new Evaluate();
		if (task.equalsIgnoreCase("stem")) {

			RetrievalHelper.printIndex(qlrmQueryList, resultDir, "stem_QLRM");
			RetrievalHelper.printIndex(tfIdfQueryResultList, resultDir, "stem_tfIdf");
			RetrievalHelper.printIndex(bm25QueryResultList, resultDir, "stem_BM25");

		} else if (task.equalsIgnoreCase("base")) {

			LuceneRetrievalServiceImpl retrievalService = LuceneRetrievalServiceImpl.getRetrievalService(indexDir,
					corpusLocation, indexFileLocation);

			retrievalService.indexFiles(indexDir, corpusLocation);

			List<system.model.QueryResultModel> luceneQueryResultList = new ArrayList<>();
			for (QueryModel query : queryList) {
				QueryResultModel queryResult = retrievalService.search(query, indexDir, 100);
				luceneQueryResultList.add(queryResult);
			}
			RetrievalHelper.printIndex(luceneQueryResultList, resultDir, "lucene", false);
			RetrievalHelper.printIndex(luceneQueryResultList, resultDir, "luceneWithSnippet", true);

			RetrievalHelper.printIndex(qlrmQueryList, resultDir, "QLRM");
			RetrievalHelper.printIndex(psuedoExpandedList, resultDir, "PSUEDO_BM25");

			RetrievalHelper.printIndex(tfIdfQueryResultList, resultDir, "tfIdf");
			RetrievalHelper.printIndex(bm25QueryResultList, resultDir, "BM25");

			SystemEvaluationModel tfEval = evl.performEvaluation(tfIdfQueryResultList, "tfIdf");
			SystemEvaluationModel qlrmEval = evl.performEvaluation(qlrmQueryList, "QLRM");
			SystemEvaluationModel bm25Eval = evl.performEvaluation(bm25QueryResultList, "BM25");
			SystemEvaluationModel luceneEval = evl.performEvaluation(luceneQueryResultList, "lucene");
			SystemEvaluationModel pusedoEval = evl.performEvaluation(psuedoExpandedList, "PSUEDO_TFIdf");

			RetrievalHelper.printEvaluatedFile(tfEval, indexDir);
			RetrievalHelper.printEvaluatedFile(qlrmEval, indexDir);
			RetrievalHelper.printEvaluatedFile(bm25Eval, indexDir);
			RetrievalHelper.printEvaluatedFile(luceneEval, indexDir);
			RetrievalHelper.printEvaluatedFile(pusedoEval, indexDir);

		} else if (task.equalsIgnoreCase("stop")) {

			RetrievalHelper.printIndex(qlrmQueryList, resultDir, "stop_QLRM");
			RetrievalHelper.printIndex(psuedoExpandedList, resultDir, "stop_PSUEDO_BM25");

			RetrievalHelper.printIndex(tfIdfQueryResultList, resultDir, "stop_tfIdf");
			RetrievalHelper.printIndex(bm25QueryResultList, resultDir, "stop_BM25");

			SystemEvaluationModel tfEval = evl.performEvaluation(tfIdfQueryResultList, "stop_tfIdf");
			SystemEvaluationModel qlrmEval = evl.performEvaluation(qlrmQueryList, "stop_QLRM");
			SystemEvaluationModel bm25Eval = evl.performEvaluation(bm25QueryResultList, "stop_BM25");
			SystemEvaluationModel pusedoEval = evl.performEvaluation(psuedoExpandedList, "stop_PSUEDO_TFIdf");

			RetrievalHelper.printEvaluatedFile(tfEval, indexDir);
			RetrievalHelper.printEvaluatedFile(qlrmEval, indexDir);
			RetrievalHelper.printEvaluatedFile(bm25Eval, indexDir);
			RetrievalHelper.printEvaluatedFile(pusedoEval, indexDir);
		}

		System.out.println("We are good to go !!");

	}
}
