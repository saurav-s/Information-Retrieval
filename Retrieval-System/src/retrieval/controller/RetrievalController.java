package retrieval.controller;

import java.util.ArrayList;
import java.util.List;

import indexer.controller.IndexController;
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

		String indexDir = args[0];
		String corpusLocation = args[1];
		String indexFileLocation = args[2];
		String queryFileLocation = args[3];
		String rawDocDirectory = args[4];
		String[] indexArgs = { rawDocDirectory };
		IndexController.main(indexArgs);
		RetrievalHelper.initHelper();
		LuceneRetrievalServiceImpl retrievalService = LuceneRetrievalServiceImpl.getRetrievalService(indexDir,
				corpusLocation, indexFileLocation);

		retrievalService.indexFiles(indexDir, corpusLocation);
		List<system.model.QueryModel> queryList = RetrievalHelper.getQueryList(queryFileLocation);
		List<system.model.QueryResultModel> luceneQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = retrievalService.search(query, indexDir, 100);
			luceneQueryResultList.add(queryResult);
		}
		RetrievalHelper.printIndex(luceneQueryResultList, indexDir, "lucene_NoStem", true);

		List<QueryResultModel> bm25QueryResultList = new ArrayList<>();
		Bm25RetrievalServiceImpl bm25RetrievalService = new Bm25RetrievalServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = bm25RetrievalService.getQueryResults(query, 100);
			bm25QueryResultList.add(queryResult);
		}
		RetrievalHelper.printIndex(bm25QueryResultList, indexDir, "bm25_NoStem");

		TermWeightIdfServiceImpl idfService = new TermWeightIdfServiceImpl();
		List<QueryResultModel> tfIdfQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel tfIdf = idfService.getQueryResults(query, 100);
			tfIdfQueryResultList.add(tfIdf);
		}
		RetrievalHelper.printIndex(tfIdfQueryResultList, indexDir, "tfIdf_NoStem");

		List<QueryResultModel> qlrmQueryList = new ArrayList<>();
		QLRMServiceImpl qlrsr = new QLRMServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = qlrsr.getQueryResults(query, 100);
			qlrmQueryList.add(queryResult);
		}
		RetrievalHelper.printIndex(qlrmQueryList, indexDir, "QLR_NoStem");

		List<QueryResultModel> psuedoExpandedList = new ArrayList<>();
		PsuedoExpansionService psuedoRel = new PsuedoExpansionService();
		for (QueryModel query : queryList) {
			QueryResultModel pusedoQuery = psuedoRel.performQueryExpandsion(query, "TF", 100);
			psuedoExpandedList.add(pusedoQuery);
		}
		RetrievalHelper.printIndex(psuedoExpandedList, indexDir, "PSUEDO_TFIdf_NoStem");

		Evaluate evl = new Evaluate();
		SystemEvaluationModel tfEval = evl.performEvaluation(tfIdfQueryResultList, "tfIdf_NoStem");
		SystemEvaluationModel qlrmEval = evl.performEvaluation(qlrmQueryList, "QLR_NoStem");
		SystemEvaluationModel bm25Eval = evl.performEvaluation(bm25QueryResultList, "bm25_NoStem");
		SystemEvaluationModel luceneEval = evl.performEvaluation(luceneQueryResultList, "lucene_NoStem");
		SystemEvaluationModel pusedoEval = evl.performEvaluation(psuedoExpandedList, "PSUEDO_TFIdf_NoStem");

		RetrievalHelper.printEvaluatedFile(tfEval, indexDir);
		RetrievalHelper.printEvaluatedFile(qlrmEval, indexDir);
		RetrievalHelper.printEvaluatedFile(bm25Eval, indexDir);
		RetrievalHelper.printEvaluatedFile(luceneEval, indexDir);
		RetrievalHelper.printEvaluatedFile(pusedoEval, indexDir);
		System.out.println("We are good to go !!");

	}
}
