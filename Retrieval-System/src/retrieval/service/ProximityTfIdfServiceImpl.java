package retrieval.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrieval.helper.RetrievalHelper;
import system.model.DocTermPosModel;
import system.model.DocumentRankModel;
import system.model.IndexModel;
import system.model.QueryModel;
import system.model.QueryResultModel;

public class ProximityTfIdfServiceImpl extends TermWeightIdfServiceImpl implements RetrievalService {
	private Map<Integer, List<DocTermPosModel>> map = new HashMap<>();
	private boolean useStopWords =false;

	
	public void init(boolean stopWord) {
		useStopWords = stopWord ;
	}
	
	@Override
	public QueryResultModel getQueryResults(QueryModel query, int size) {
		String[] queryWords = RetrievalHelper.parseQuery(query);
		if(useStopWords) {
			queryWords = RetrievalHelper.removeStopWordsFromQuery(queryWords);
		}
		map =getDocTermPosMap(queryWords); 
		List<IndexModel> docList = fetchRelevantDocsForQuery(queryWords);
		QueryResultModel qr = new QueryResultModel();
		List<DocumentRankModel> drmList = new ArrayList<DocumentRankModel>();
		Map<Integer, List<String>> termPairs = RetrievalHelper.getSetOfPairs(query);
		for (IndexModel indexModel : docList) {
			double proximityScore = getProximityScore(termPairs, indexModel);
			DocumentRankModel drm = new DocumentRankModel();
			double tfIdfScore = fetchTfIdfProduct(query, indexModel, queryWords,termPairs);
			drm.setDocId(indexModel.getDocId());
			double scaledScore=0;
			if(proximityScore !=0) {
				scaledScore = Math.log(proximityScore);
			}
			
			drm.setRankScore(scaledScore);
			//System.out.println(drm.getDocId()+" : "+drm.getRankScore());
			drmList.add(drm);
		}
		Collections.sort(drmList);
		qr.setQueryId(query.getId());
		qr.setResults(drmList);
		return qr;
	}

	private double fetchTfIdfProduct(QueryModel query, IndexModel indexModel,String[] queryWords,Map<Integer, List<String>> termPairs) {
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
	
	private double getProximityScore(Map<Integer, List<String>> termPairs,IndexModel indexModel) {

		int docId = indexModel.getDocId();
		List<DocTermPosModel> DocTermPositions = map.get(docId);
		double score = 0;
		
		for(Map.Entry<Integer, List<String>> entry: termPairs.entrySet()) {
			double pairScore = 0;
			List<String> termPair = entry.getValue();
			String term1 = termPair.get(0);
			String term2 = termPair.get(1);
			List<Integer> term1Position = null;
			List<Integer> term2Position = null;;
			for(DocTermPosModel docTermPosition: DocTermPositions) {
				if (docTermPosition.getTerm().equals(term1)) {
					term1Position = docTermPosition.getTermPositions();
				}else if(docTermPosition.getTerm().equals(term2)) {
					term2Position = docTermPosition.getTermPositions();
				}
				if(term1Position !=null && term2Position!=null) {
					break;
				}
			}
			if(term1Position !=null && term2Position!=null) {
				for(Integer pos2: term2Position){
					for(Integer pos1 :term1Position){
						if((pos2-pos1) <=3 && (pos2-pos1) >0 ) {
							pairScore += ((double)1 / (double)((pos2-pos1) * (pos2-pos1)));
							//System.out.println("pair score ="+pairScore+ "\tpair = "+term1+"\t term2 ="+term2);
						}
					}
				}
			}
			score += pairScore;
		}
		//System.out.println("total score ="+score);
		
		return score;
		
	}

	private Map<Integer, List<DocTermPosModel>> getDocTermPosMap(String[] queryTerms) {
		Map<Integer, List<DocTermPosModel>> docTermPosMap = new HashMap<>();
		for (String term : queryTerms) {
			List<IndexModel> invertedIndex = RetrievalHelper.getInvertedIndex(term);
			for (IndexModel model : invertedIndex) {
				if (docTermPosMap.containsKey(model.getDocId())) {
					List<DocTermPosModel> termPosList = docTermPosMap.get(model.getDocId());
					DocTermPosModel docTermPosModel = new DocTermPosModel();
					docTermPosModel.setTerm(term);
					docTermPosModel.setTermPositions(model.getTermPositions());
					termPosList.add(docTermPosModel);
					docTermPosMap.put(model.getDocId(), termPosList);
				}else {
					DocTermPosModel docTermPosModel = new DocTermPosModel();
					docTermPosModel.setTerm(term);
					docTermPosModel.setTermPositions(model.getTermPositions());
					List<DocTermPosModel> termPosList = new ArrayList<>();
					termPosList.add(docTermPosModel);
					docTermPosMap.put(model.getDocId(), termPosList);
				}
			}
		}
		
		return docTermPosMap;
	}
}
