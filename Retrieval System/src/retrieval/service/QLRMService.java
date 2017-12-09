package retrieval.service;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrieval.helper.RetrievalHelper;
import system.model.IndexModel;

public class QLRMService {

	//private Map<String, List<IndexModel>> uniGram = new HashMap<>();
	private Map<Integer, Integer> docStat = new HashMap<>();
	private final Double lambda=0.35;
	private  Map<String, Integer> queryMap = new HashMap<>();
	private Integer collectionLength=0;

	
	public QLRMService(Map<String, List<IndexModel>> uniGram, Map<Integer, Integer> docStat) {
	//		this.uniGram=uniGram;
			this.docStat= docStat;
			// collectionLength= RetrievalHelper.getTotalLength;
			
			collectionLength= calculateCollectionLength();
	}
	
	public Map<Integer, Double> processQuery(String query)
	{
		Map<Integer, Double> qLRcoresForDocs= new HashMap<>();
		setQMap(query);
		Set<Integer> docsSet = getAllDocsId();
		for (Integer doc : docsSet) {
			Double score = calculateQLRM(doc);
			qLRcoresForDocs.put(doc, score); 
		}
		return qLRcoresForDocs;
	}
	
	private Double calculateQLRM(Integer doc) {
		Double score=0.0;
		for (String queryTerm : queryMap.keySet()) 
		{
			int fqi=0;
			int cqi=0;
			List<IndexModel> indexList=
					RetrievalHelper.getInvetedIndex(queryTerm);
			for (IndexModel indexModel : indexList) {
				if(indexModel.getDocId()==doc)
				{
					fqi=indexModel.getDocId();
				}
				cqi+=indexModel.getTf();
			}
			Double firstPartScore= calculateFirstPart(docStat.get(doc),fqi);
			Double secondPartScore= calculatesecondPart(cqi);
			Double queryTermScore=Math.log(firstPartScore+secondPartScore);
			score+=queryTermScore;
		}
		
		
		return score;
	}

	private Double calculatesecondPart(int cqi) {
		Double score = lambda*(cqi/collectionLength);
		return score;
	}

	private Double calculateFirstPart(Integer docLength,Integer fqi) {
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
				List<IndexModel> docs=uniGram.get(term);
				if(docs!=null && !docs.isEmpty())
				{
					for (IndexModel indexModel : docs) {
						docsSet.add(indexModel.getDocId());
					}
				}	
			}
			return docsSet;
		}
		
		
		private  Integer calculateCollectionLength() {
			Integer toalTokens=0;
			for (int doc : docStat.keySet()) 
			{
				toalTokens+=docStat.get(doc);
			}
			return toalTokens;
		}

}
