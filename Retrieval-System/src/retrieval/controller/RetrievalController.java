package retrieval.controller;

import java.util.ArrayList;
import java.util.List;

import indexer.controller.IndexController;
import retrieval.helper.RetrievalHelper;
import retrieval.service.Bm25RetrievalServiceImpl;
import retrieval.service.LuceneRetrievalServiceImpl;
import retrieval.service.TermWeightIdfServiceImpl;
import system.model.QueryModel;
import system.model.QueryResultModel;

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
		// RetrievalHelper.writeToJsonStream(indexDir,
		// "Lucene_Query_Results.json", luceneQueryResultList);
		RetrievalHelper.printIndex(luceneQueryResultList, indexDir, "lucene_NoStem", true);

		List<QueryResultModel> bm25QueryResultList = new ArrayList<>();
		Bm25RetrievalServiceImpl bm25RetrievalService = new Bm25RetrievalServiceImpl();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = bm25RetrievalService.getQueryResults(query, 100);
			bm25QueryResultList.add(queryResult);
		}
		// RetrievalHelper.writeToJsonStream(indexDir,
		// "Bm25_Query_Results.json", bm25QueryResultList);
		RetrievalHelper.printIndex(bm25QueryResultList, indexDir, "bm25_NoStem");

		TermWeightIdfServiceImpl idfService = new TermWeightIdfServiceImpl();
		List<QueryResultModel> tfIdfQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel tfIdf = idfService.getQueryResults(query, 100);
			tfIdfQueryResultList.add(tfIdf);
		}
		RetrievalHelper.printIndex(tfIdfQueryResultList, indexDir, "tfIdf_NoStem");
		System.out.println("We are good to go !!");

	}
}
