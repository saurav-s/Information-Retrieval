package system.model;

import java.util.List;

public class QueryResultModel {
	private int queryId;
	private List<DocumentRankModel> results;
	
	public int getQueryId() {
		return queryId;
	}
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	public List<DocumentRankModel> getResults() {
		return results;
	}
	public void setResults(List<DocumentRankModel> results) {
		this.results = results;
	}
	
	@Override
	public String toString() {
		return "[queryId=" + queryId + ", results=" + results + "]";
	}
	
}
