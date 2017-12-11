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

		// indexDir : New Index Will be Generated here.
		// corpusLocation: location of the corpus.
		// indexFileLocation: Lucene's index location
		// rawDocDirectory: directory of raw documents
		// resultDir : output of the tasks will be printed there.
		String indexDir = args[0];
		String corpusLocation = args[1];
		String indexFileLocation = args[2];
		String rawDocDirectory = args[3];
		String resultDir = args[4];
		String[] indexArgs = { rawDocDirectory };
		IndexController.main(indexArgs);
		RetrievalHelper.initHelper();
		LuceneRetrievalServiceImpl retrievalService = LuceneRetrievalServiceImpl.getRetrievalService(indexDir,
				corpusLocation, indexFileLocation);

		retrievalService.indexFiles(indexDir, corpusLocation);
		List<system.model.QueryModel> queryList = RetrievalHelper.parseQueriesFromXML();
		List<system.model.QueryResultModel> luceneQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = retrievalService.search(query, indexDir, 100);
			luceneQueryResultList.add(queryResult);
		}
		RetrievalHelper.printIndex(luceneQueryResultList, resultDir, "lucene", false);
		RetrievalHelper.printIndex(luceneQueryResultList, resultDir, "luceneWithSnippet", true);

		List<QueryResultModel> bm25QueryResultList = new ArrayList<>();
		Bm25RetrievalServiceImpl bm25RetrievalService = new Bm25RetrievalServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = bm25RetrievalService.getQueryResults(query, 100);
			bm25QueryResultList.add(queryResult);
		}
		RetrievalHelper.printIndex(bm25QueryResultList, resultDir, "BM25");

		TermWeightIdfServiceImpl idfService = new TermWeightIdfServiceImpl();
		List<QueryResultModel> tfIdfQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel tfIdf = idfService.getQueryResults(query, 100);
			tfIdfQueryResultList.add(tfIdf);
		}
		RetrievalHelper.printIndex(tfIdfQueryResultList, resultDir, "tfIdf");

		List<QueryResultModel> qlrmQueryList = new ArrayList<>();
		QLRMServiceImpl qlrsr = new QLRMServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = qlrsr.getQueryResults(query, 100);
			qlrmQueryList.add(queryResult);
		}
		RetrievalHelper.printIndex(qlrmQueryList, resultDir, "QLRM");

		List<QueryResultModel> psuedoExpandedList = new ArrayList<>();
		PsuedoExpansionService psuedoRel = new PsuedoExpansionService();
		for (QueryModel query : queryList) {
			QueryResultModel pusedoQuery = psuedoRel.performQueryExpandsion(query, "BM25", 100);
			psuedoExpandedList.add(pusedoQuery);
		}
		RetrievalHelper.printIndex(psuedoExpandedList, resultDir, "PSUEDO_BM25");

		Evaluate evl = new Evaluate();
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
		System.out.println("We are good to go !!");

	}
}
