package retrieval.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrieval.helper.RetrievalHelper;
import system.model.DocumentRankModel;
import system.model.IndexModel;
import system.model.QueryModel;
import system.model.QueryResultModel;

public class TermWeightIdfServiceImpl implements RetrievalService {

	@Override
	public QueryResultModel getQueryResults(QueryModel query, int size) {
		String[] queryWords = RetrievalHelper.parseQuery(query);
		List<IndexModel> docList = fetchRelevantDocsForQuery(queryWords);
		QueryResultModel qr = new QueryResultModel();
		List<DocumentRankModel> drmList = new ArrayList<DocumentRankModel>();
		for (IndexModel indexModel : docList) {
			DocumentRankModel drm = new DocumentRankModel();
			drm.setDocId(indexModel.getDocId());
			drm.setRankScore(fetchTfIdfProduct(queryWords, indexModel));
			System.out.println(drm.getDocId()+" : "+drm.getRankScore());
			drmList.add(drm);
		}
		Collections.sort(drmList);
		qr.setQueryId(query.getId());
		qr.setResults(RetrievalHelper.getTopNResults(drmList, size));
		return qr;
	}

	private double fetchTfIdfProduct(String[] queryWords, IndexModel indexModel) {
		double tfIdfProduct = 0;
		for (String word : queryWords) {
			double tf = RetrievalHelper.getTermFreqInDoc(word, indexModel.getDocId());
			double totalTerms = RetrievalHelper.getDocLenth(indexModel.getDocId());
			double idf = RetrievalHelper.getIdf(word);
			double tfIdfForWord = (tf / totalTerms) * idf;
			tfIdfProduct += tfIdfForWord;
		}
		return tfIdfProduct;
	}

	private List<IndexModel> fetchRelevantDocsForQuery(String[] queryWords) {
		List<IndexModel> docList = new ArrayList<>();
		for (String word : queryWords) {
			List<IndexModel> docsForWord = RetrievalHelper.getInvertedIndex(word);
			docList.addAll(docsForWord);
		}
		return docList;
	}
}
