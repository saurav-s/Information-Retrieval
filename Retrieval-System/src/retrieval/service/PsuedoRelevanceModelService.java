package retrieval.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrieval.helper.RetrievalHelper;
import system.model.DocumentRankModel;
import system.model.DocumentTermModel;
import system.model.QueryModel;

public class PsuedoRelevanceModelService {

	private QueryModel query;
	private List<DocumentRankModel> scoredList;
	private final Integer k = 10;
	private final Integer toptermCount = 20;

	public QueryModel performPsuedoRelevance(QueryModel query, List<DocumentRankModel> scoredList) {
		this.query = query;
		this.scoredList = scoredList;

		return performExpansion();
	}

	private QueryModel  performExpansion() {
		Map<String, Double> mainMap = getTopterms(scoredList.get(0).getDocId());
		for (int i = 1; i < k; i++) {
			mainMap = addtoMap(mainMap, getTopterms(scoredList.get(i).getDocId()));
		}
		StringBuffer newQuery = new StringBuffer(query.getQuery());
		for (String term : mainMap.keySet()) {
			newQuery.append(" " + term);
		}
		QueryModel qrm = new QueryModel();
		qrm.setId(query.getId());
		qrm.setQuery(newQuery.toString());
		return qrm;
	}

	private Map<String, Double> addtoMap(Map<String, Double> mainMap, Map<String, Double> topterms) {
		for (String t : topterms.keySet()) {
			Integer checkMap = 0;
			for (String term : mainMap.keySet()) {
				if (term.equalsIgnoreCase(t)) {
					Double val = mainMap.get(t) > topterms.get(t) ? mainMap.get(t) : topterms.get(t);
					mainMap.put(t, val);
					checkMap = 1;
					break;
				}
			}
			if (checkMap == 0) {
				List<String> keyList = new ArrayList<>();
				keyList.addAll(mainMap.keySet());
				String lastKey = keyList.get(keyList.size()-1);  
				if (topterms.get(t) > mainMap.get(lastKey)) {
					mainMap.remove(lastKey);
					//mainMap.get(lastKey);
					mainMap.put(t, topterms.get(t));
				}
				mainMap=RetrievalHelper.sortByValue(mainMap); 
			}

		}
		return mainMap;
	}

	private Map<String, Double> getTopterms(Integer docId) {
		Map<String, Double> topTerms = new HashMap<>();
		Map<String, Double> topTerm20 = new HashMap<>();
		List<DocumentTermModel> docIndex = RetrievalHelper.getTermFreqMap(docId);
		Integer docLength = RetrievalHelper.getDocLenth(docId);
		for (DocumentTermModel termObj : docIndex) {
			double tf = (double) termObj.getTf() / (double) docLength;
			double idf = Math.log((double) RetrievalHelper.getCollectionSize()
					/ (double) RetrievalHelper.getInvertedIndex(termObj.getTerm()).size());
			topTerms.put(termObj.getTerm(), tf * idf);
		}
		topTerms = RetrievalHelper.sortByValue(topTerms);
		int count = 0;
		for (String term : topTerms.keySet()) {
			topTerm20.put(term, topTerms.get(term));
			count++;
			if (count == toptermCount)
				break;
		}
		return topTerm20;
	}

}
