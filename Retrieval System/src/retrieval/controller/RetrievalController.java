package retrieval.controller;

import java.util.ArrayList;
import java.util.List;

import retrieval.helper.RetrievalHelper;
import retrieval.model.QueryModel;
import retrieval.model.QueryResultModel;
import retrieval.service.Bm25RetrievalService;
import retrieval.service.RetrievalService;

public class RetrievalController {
	public static void main(String[] args) throws Exception {

		String indexDir = args[0];
		String corpusLocation = args[1];
		String indexFileLocation = args[2];
		String queryFileLocation = args[3];
		RetrievalService retrievalService = RetrievalService.getRetrievalService(indexDir, corpusLocation,indexFileLocation);
		
		retrievalService.indexFiles(indexDir, corpusLocation);
		List<QueryModel> queryList = RetrievalHelper
				.getQueryList(queryFileLocation);
		List<QueryResultModel> luceneQueryResultList = new ArrayList<>();
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = retrievalService.search(query, indexDir, 100);
			luceneQueryResultList.add(queryResult);
		}
		//RetrievalHelper.writeToJsonStream(indexDir, "Lucene_Query_Results.json", luceneQueryResultList);
		RetrievalHelper.printIndex(luceneQueryResultList,indexDir, "lucene_NoStem");
		
		List<QueryResultModel> bm25QueryResultList = new ArrayList<>();
		Bm25RetrievalService bm25RetrievalService =new Bm25RetrievalService(corpusLocation,indexFileLocation);
		for (QueryModel query : queryList) {
			QueryResultModel queryResult = bm25RetrievalService.getQueryResults(query,100);
			bm25QueryResultList.add(queryResult);
		}
		//RetrievalHelper.writeToJsonStream(indexDir, "Bm25_Query_Results.json", bm25QueryResultList);
		RetrievalHelper.printIndex(bm25QueryResultList,indexDir, "bm25_NoStem");
		System.out.println("We are good to go !!");
		
	}
}
