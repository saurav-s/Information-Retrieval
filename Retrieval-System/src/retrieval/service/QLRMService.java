package retrieval.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrieval.helper.RetrievalHelper;
import system.model.DocumentRankModel;
import system.model.IndexModel;
import system.model.QueryModel;
import system.model.QueryResultModel;

public class QLRMService implements RetrievalService{

	
	//private Map<Integer, Integer> docStat = new HashMap<>();
	private final Double lambda=0.35;
	private  Map<String, Integer> queryMap = new HashMap<>();
	private double collectionLength=0;

	
	public QLRMService() {
			//.docStat= docStat;
		collectionLength=RetrievalHelper.getCollcetionSizeLength();
	}
	
	public QueryResultModel getQueryResults(final QueryModel query) throws IOException {
		return getQueryResults(query, 0);
	}

	@Override
	public QueryResultModel getQueryResults(QueryModel query, int size) {
		List<DocumentRankModel> rankedScore= new ArrayList<DocumentRankModel>();
		setQMap(query.getQuery());
		Set<Integer> docsSet = getAllDocsId();
		for (Integer doc : docsSet) {
			Double score = calculateQLRM(doc);
			DocumentRankModel docScore = new DocumentRankModel();
			docScore.setDocId(doc);
			docScore.setRankScore(score);
			rankedScore.add(docScore);
		}
		
		Collections.sort(rankedScore);
		if(size != 0)
		{
			rankedScore=getTopNResults(rankedScore, size);
		}
		QueryResultModel qr = new QueryResultModel();
		qr.setQueryId(query.getId());
		qr.setResults(rankedScore);
		return qr;
	}
	
	
	private List<DocumentRankModel> getTopNResults(List<DocumentRankModel> results, int n) {
		List<DocumentRankModel> topResults = new ArrayList<>();
		for (DocumentRankModel result : results) {
			if (topResults.size() < n)
				topResults.add(result);
			else
				break;
		}
		return topResults;
	}

	
	private Double calculateQLRM(Integer doc) {
		Double score=0.0;
		for (String queryTerm : queryMap.keySet()) 
		{
			double fqi=0;
			double cqi=0;
			List<IndexModel> indexList=RetrievalHelper.getInvertedIndex(queryTerm);
			for (IndexModel indexModel : indexList) {
				if(indexModel.getDocId()==doc)
				{
					fqi=indexModel.getDocId();
				}
				cqi+=indexModel.getTf();
			}
			Double firstPartScore= calculateFirstPart(RetrievalHelper.getDocLenth(doc),fqi);
			Double secondPartScore= calculatesecondPart(cqi);
			double finalScore =firstPartScore+secondPartScore;
			if(finalScore != 0) {
				Double queryTermScore=Math.log(finalScore);
				score+=queryTermScore;
			}
		}
		return score;
	}

	private Double calculatesecondPart(double cqi) {
		Double score = lambda*(cqi/collectionLength);
		return score;
	}

	private Double calculateFirstPart(double docLength,double fqi) {
		Double score = (1-lambda)*(fqi/docLength);
		return score;
	}

		private  void setQMap(String query)
		{
			String queryText[] = query.split("\\ ");
			for (String term : queryText)
			{
				Set<String>qSet = queryMap.keySet();
				if(qSet.contains(term))
					queryMap.put(term, queryMap.get(term)+1);
				else
					queryMap.put(term, 1);
			}
		}
		private Set<Integer> getAllDocsId()
		{
			Set<String>qSet = queryMap.keySet();
			Set<Integer> docsSet = new HashSet<>();
			for (String term : qSet) 
			{
				List<IndexModel> docs=RetrievalHelper.getInvertedIndex(term);
				if(docs!=null && !docs.isEmpty())
				{
					for (IndexModel indexModel : docs) {
						docsSet.add(indexModel.getDocId());
					}
				}	
			}
			return docsSet;
		}

		

}
