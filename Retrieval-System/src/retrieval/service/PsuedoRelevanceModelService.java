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
	private final Integer topTermCount = 20;

	public QueryModel performPsuedoRelevance(QueryModel query, List<DocumentRankModel> scoredList) {
		this.query = query;
		this.scoredList = scoredList;

		return performExpansion();
	}

	// GIVEN:
	// RETURNS:
	private QueryModel performExpansion() {
		Map<String, Double> topTerms = getTopTerms(scoredList.get(0).getDocId());
		for (int i = 1; i < k; i++) {
			topTerms = pickTopNFromDocs(topTerms, getTopTerms(scoredList.get(i).getDocId()));
		}
		StringBuffer newQuery = new StringBuffer(query.getQuery());
		for (String term : topTerms.keySet()) {
			newQuery.append(" " + term);
		}
		QueryModel qrm = new QueryModel();
		qrm.setId(query.getId());
		qrm.setQuery(newQuery.toString());
		return qrm;
	}

	// GIVEN:
	// RETURNS:
	private Map<String, Double> pickTopNFromDocs(Map<String, Double> topTermsMap, Map<String, Double> topTermForDoc) {
		for (String t : topTermForDoc.keySet()) {
			Integer checkMap = 0;
			for (String term : topTermsMap.keySet()) {
				if (term.equalsIgnoreCase(t)) {
					Double val = topTermsMap.get(t) > topTermForDoc.get(t) ? topTermsMap.get(t) : topTermForDoc.get(t);
					topTermsMap.put(t, val);
					checkMap = 1;
					break;
				}
			}
			if (checkMap == 0) {
				List<String> keyList = new ArrayList<>();
				keyList.addAll(topTermsMap.keySet());
				String lastKey = keyList.get(keyList.size() - 1);
				if (topTermForDoc.get(t) > topTermsMap.get(lastKey)) {
					topTermsMap.remove(lastKey);
					topTermsMap.put(t, topTermForDoc.get(t));
				}
				topTermsMap = RetrievalHelper.sortByValue(topTermsMap);
			}

		}
		return topTermsMap;
	}

	// GIVEN:
	// RETURNS:
	private Map<String, Double> getTopTerms(Integer docId) {
		Map<String, Double> topTerms = new HashMap<>();
		Map<String, Double> topNTerms = new HashMap<>();
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
			topNTerms.put(term, topTerms.get(term));
			count++;
			if (count == topTermCount)
				break;
		}
		return topNTerms;
	}

}
