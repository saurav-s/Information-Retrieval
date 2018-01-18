package retrieval.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrieval.helper.RetrievalHelper;
import system.model.DocumentRankModel;
import system.model.IndexModel;
import system.model.QueryModel;
import system.model.QueryResultModel;

public class Bm25RetrievalServiceImpl {

	// /**
	// * @param corpusLocation
	// * :corpusLocation
	// */
	// public Bm25RetrievalServiceImpl(String corpusLocation, String
	// indexFileLocation) {
	// RetrievalHelper.initHelper(corpusLocation, indexFileLocation);
	// }

	// k1, k2 are constants,
	// q is the wqf, the within query frequency,
	// f is the wdf, the within document frequency,
	// n is the number of documents in the collection indexed by this term,
	// N is the total number of documents in the collection,
	// r is the number of relevant documents indexed by this term, !!!----- can
	// be
	// assumed zero
	// R is the total number of relevant documents, !!!----- can be assumed zero
	// L is the normalised document length (i.e. the length of this document
	// divided
	// by the average length of documents in the collection).
	/***
	 * 
	 * 
	 * ((k2 + 1)q) / ((k2 + q)) * ((k1 + 1) f) / ((K + f)) * log( (r + 0.5) (N
	 * − n − R + r + 0.5) ) / ((n − r + 0.5)(R − r + 0.5))
	 * 
	 */

	private double computeBm25ScoreForTerm(String[] queryTerms, String term, int docId) {
		// int totalRelevantDoc=0;
		// int termRelevantDoc=0;
		// int normalizedDocLength=0;
		// int totalDocsInCollection = 0;
		// int tf =0;
		// int termQueryFrequency=1; //term frequency within the query itself
		// int termDocFrequency=1; //tf inside a doc

		double k2 = 100; // constant
		double b = 0.75; // constant
		double k1 = 1.2; // constant
		double r = 0; // constant
		double R = 0; // constant

		double q = RetrievalHelper.calculateQueryFrequency(queryTerms, term);
		double f = RetrievalHelper.getTermFreqInDoc(term, docId);
		double K = computeK(k1, b, docId);

		double N = RetrievalHelper.getCollectionSize();
		double n = RetrievalHelper.getInvertedIndex(term).size();

		////// !!!!!!!!!!!..............................check for double

		double res1 = (((k2 + 1) * q) / (k2 + q));
		double res2 = ((k1 + 1) * f) / (K + f);
		double resLog = ((r + 0.5) * (N - n - R + r + 0.5)) / ((n - r + 0.5) * (R - r + 0.5));
		double result = (Math.log(resLog) * res1 * res2);
		// System.out.println("term: " + term + "\ttf :" + f + "\tterm occurance
		// in no of collection docs:" + n
		// + "\tdocId : " + docId + "\tScore : " + result);
		return result;
	}

	/**
	 * K = k1 * ((1-b) + b. dl/avdl)
	 * 
	 */
	private double computeK(double k1, double b, int docId) {
		double dl = (double) RetrievalHelper.getDocLenth(docId);
		// System.out.println("docId= "+docId+"\tlength = "+dl);
		double avdl = RetrievalHelper.getAvgDocLength();
		return (k1 * ((1 - b) + (b * dl / avdl)));
	}

	private List<DocumentRankModel> computeBm25Score(final QueryModel query) throws IOException {
		String[] searchTerms = RetrievalHelper.parseQuery(query);
		List<IndexModel> relevantIndexList = new ArrayList<>();

		for (String term : searchTerms) {
			List<IndexModel> invetedIndex = RetrievalHelper.getInvertedIndex(term);
			relevantIndexList.addAll(invetedIndex);
			// int totalTf = totalTf(invetedIndex);
		}
		List<DocumentRankModel> bm25ScoreList = computeBm25ForRelevantDocs(relevantIndexList, searchTerms);
		return bm25ScoreList;

	}

	/**
	 * 
	 * @param query
	 * @param resultSize
	 * @return
	 * @throws IOException
	 */
	public QueryResultModel getQueryResults(final QueryModel query, Integer resultSize) throws IOException {
		QueryResultModel resultModel = new QueryResultModel();
		List<DocumentRankModel> scoreList = computeBm25Score(query);
		Collections.sort(scoreList);
		resultModel.setQueryId(query.getId());
		if (resultSize != null) {
			List<DocumentRankModel> topRankedDocs = RetrievalHelper.getTopNResults(scoreList, resultSize);
			resultModel.setResults(topRankedDocs);
		} else {
			resultModel.setResults(scoreList);
		}
		return resultModel;
	}

	public QueryResultModel getQueryResults(final QueryModel query) throws IOException {
		return getQueryResults(query, null);
	}

	/**
	 * 
	 * @param relevantIndex
	 * @param serchTerms
	 * @return
	 */
	private List<DocumentRankModel> computeBm25ForRelevantDocs(List<IndexModel> relevantIndex, String[] serchTerms) {
		Set<Integer> docIdSet = getDocIdSet(relevantIndex);
		List<DocumentRankModel> docScoreList = new ArrayList<>();
		for (int docId : docIdSet) {
			double bm25Score = computeBm25ScoreForDocument(serchTerms, docId);
			DocumentRankModel docScore = new DocumentRankModel();
			docScore.setDocId(docId);
			docScore.setRankScore(bm25Score);
			docScoreList.add(docScore);
		}
		return docScoreList;

	}

	private Set<Integer> getDocIdSet(List<IndexModel> relevantIndex) {
		Set<Integer> docIdList = new HashSet<Integer>();
		for (IndexModel index : relevantIndex) {
			docIdList.add(index.getDocId());
		}
		return docIdList;
	}

	private double computeBm25ScoreForDocument(String[] queryTerms, int docId) {
		double score = 0;
		for (String term : queryTerms) {
			score += computeBm25ScoreForTerm(queryTerms, term, docId);
		}
		return score;
	}

}
