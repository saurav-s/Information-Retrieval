package system.model;

import java.util.List;

public class EvaluationResultModel {

	private int queryId;
	private List<DocumentEvaluationModel> results;
	private double avgPercission;
	private double rr;
	public int getQueryId() {
		return queryId;
	}
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	public List<DocumentEvaluationModel> getResults() {
		return results;
	}
	public void setResults(List<DocumentEvaluationModel> results) {
		this.results = results;
	}
	public double getAvgPercission() {
		return avgPercission;
	}
	public void setAvgPercission(double avgPercission) {
		this.avgPercission = avgPercission;
	}
	public double getRr() {
		return rr;
	}
	public void setRr(double rr) {
		this.rr = rr;
	}

}
