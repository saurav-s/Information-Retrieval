package retrieval.model;

import java.util.List;

public class TermIndexModel {
	private String term;
	private List<IndexModel> invertedList;
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public List<IndexModel> getInvertedList() {
		return invertedList;
	}
	public void setInvertedList(List<IndexModel> invertedList) {
		this.invertedList = invertedList;
	}
	@Override
	public String toString() {
		return "TermIndexModel [term=" + term + ", invertedList=" + invertedList + "]";
	}
}
