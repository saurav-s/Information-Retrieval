package retrieval.service;

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

public class QLRMServiceImpl implements RetrievalService {

	private final Double lambda = 0.35;
	private Map<String, Integer> queryMap = new HashMap<>();
	private double collectionLength = 0;

	public QLRMServiceImpl() {
		collectionLength = RetrievalHelper.getCollcetionSizeLength();
	}

	// GIVEN:
	// RETURNS:
	@Override
	public QueryResultModel getQueryResults(QueryModel query, int size) {
		List<DocumentRankModel> rankedScore = new ArrayList<DocumentRankModel>();
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
		rankedScore = RetrievalHelper.getTopNResults(rankedScore, size);
		QueryResultModel qr = new QueryResultModel();
		qr.setQueryId(query.getId());
		qr.setResults(rankedScore);
		return qr;
	}

	// GIVEN:
	// RETURNS:
	private Double calculateQLRM(Integer doc) {
		Double score = 0.0;
		for (String queryTerm : queryMap.keySet()) {
			double fqi = 0;
			double cqi = 0;
			List<IndexModel> indexList = RetrievalHelper.getInvertedIndex(queryTerm);
			for (IndexModel indexModel : indexList) {
				if (indexModel.getDocId() == doc) {
					fqi = indexModel.getDocId();
				}
				cqi += indexModel.getTf();
			}
			Double firstPartScore = calculateFirstPart(RetrievalHelper.getDocLenth(doc), fqi);
			Double secondPartScore = calculateSecondPart(cqi);
			double finalScore = firstPartScore + secondPartScore;
			if (finalScore != 0) {
				Double queryTermScore = Math.log(finalScore);
				score += queryTermScore;
			}
		}
		return score;
	}

	// GIVEN:
	// RETURNS:
	private Double calculateSecondPart(double cqi) {
		Double score = lambda * (cqi / collectionLength);
		return score;
	}

	// GIVEN:
	// RETURNS:
	private Double calculateFirstPart(double docLength, double fqi) {
		Double score = (1 - lambda) * (fqi / docLength);
		return score;
	}

	// GIVEN:
	// RETURNS:
	private void setQMap(String query) {
		String queryText[] = query.split("\\ ");
		for (String term : queryText) {
			Set<String> qSet = queryMap.keySet();
			if (qSet.contains(term))
				queryMap.put(term, queryMap.get(term) + 1);
			else
				queryMap.put(term, 1);
		}
	}

	// GIVEN:
	// RETURNS:
	private Set<Integer> getAllDocsId() {
		Set<String> qSet = queryMap.keySet();
		Set<Integer> docsSet = new HashSet<>();
		for (String term : qSet) {
			List<IndexModel> docs = RetrievalHelper.getInvertedIndex(term);
			if (docs != null && !docs.isEmpty()) {
				for (IndexModel indexModel : docs) {
					docsSet.add(indexModel.getDocId());
				}
			}
		}
		return docsSet;
	}

}
