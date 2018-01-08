package retrieval.system.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import indexer.helper.DocumentHelper;
import system.model.DocumentEvaluationModel;
import system.model.DocumentRankModel;
import system.model.EvaluationResultModel;
import system.model.QueryResultModel;
import system.model.SystemEvaluationModel;

public class Evaluate {

	
	
	private Map<Integer, List<String>> relevanceJudgement;
	private int relevantCount=0;
	
 public Evaluate() {
	 relevanceJudgement=DocumentHelper.getQueryRelevanceMap(); 
	 }
	
	public SystemEvaluationModel performEvaluation(List<QueryResultModel> querryResultList, String modelType) {
		SystemEvaluationModel systemEvaluation= new SystemEvaluationModel();
		systemEvaluation.setModelName(modelType);
		List<EvaluationResultModel> evaluationList = new ArrayList<>();
		for (QueryResultModel qrm : querryResultList) {
			// As Relevance judgment is for 54 queries and total queries are 64, so breaking early.
			if(relevanceJudgement.keySet().contains(qrm.getQueryId())) 
			evaluationList.add(performEvaluationResult(qrm));
		}
		systemEvaluation.setEvaluatedResults(evaluationList);
		systemEvaluation.setMap(calculateMapOrMrr(evaluationList, "MAP"));
		systemEvaluation.setMrr(calculateMapOrMrr(evaluationList, "MRR"));
		return systemEvaluation;
	}

	private Double calculateMapOrMrr(List<EvaluationResultModel> evaluationList, String type) {
		double score=0.0;
		int count=0;
		for (EvaluationResultModel erm : evaluationList) {
			count++;
			if(type.equalsIgnoreCase("MAP"))
			 score+=erm.getAvgPercission();
			else
			 score+=erm.getRr();
		}
		return score/(double)count;
	}

	private  EvaluationResultModel performEvaluationResult(QueryResultModel qrm) {
		EvaluationResultModel erm = new EvaluationResultModel();
		List<DocumentEvaluationModel> docs = new ArrayList<>();
		erm.setQueryId(qrm.getQueryId());
		int resultCount=1;
		relevantCount=0;
		for (DocumentRankModel result : qrm.getResults()) {
			docs.add(performDocumentEvaluation(result, relevanceJudgement.get(qrm.getQueryId()), resultCount));
			resultCount++;
		}
		erm.setResults(docs);
		erm.setAvgPercission(calculateAvgPercision(docs));
		erm.setRr(calculateRR(docs));
		return erm;
	}

	private double calculateRR(List<DocumentEvaluationModel> docs) {
		int firstRel=0;
		for(int i=0; i<docs.size();i++)
		{
			if (docs.get(i).isRelevant()) 
			{
				firstRel=i+1;
				break;
			}
		}
		if(firstRel !=0)
		return (double) 1/(double)firstRel;
		else return 0;
	}

	private Double calculateAvgPercision(List<DocumentEvaluationModel> docs) {
		int percisionCount=0;
		double percisionSum=0.0;
		for (DocumentEvaluationModel doc : docs) {
			if(doc.isRelevant())
			{
				percisionSum+=doc.getPrecision();
				percisionCount++;
			}
		}
		if(percisionCount!=0)
		return percisionSum/(double)percisionCount;
		else return 0.0;
	}

	private DocumentEvaluationModel performDocumentEvaluation(DocumentRankModel result, List<String> relvanceDocs,
			int resultCount) {
		DocumentEvaluationModel doc = new DocumentEvaluationModel();
		String docName= DocumentHelper.getDocName(result.getDocId());
		doc.setDocId(docName);
		doc.setRelevant(relvanceDocs.contains(docName));
		if(relvanceDocs.contains(docName)) relevantCount++;
		doc.setPrecision((double)relevantCount/(double)resultCount);
		doc.setRecall((double)relevantCount/(double)relvanceDocs.size());
		return doc;
	}

}
