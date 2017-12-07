package system.model;

import java.util.List;

public class DocumentFrequencyModel implements Comparable<DocumentFrequencyModel>{
	
	private String term;
	private List<Integer> docIdList;
	private int df;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public List<Integer> getDocIdList() {
		return docIdList;
	}
	public void setDocIdList(List<Integer> docIdList) {
		this.docIdList = docIdList;
	}
	public int getDf() {
		return df;
	}
	public void setDf(int df) {
		this.df = df;
	}
	
	@Override
	public int compareTo(DocumentFrequencyModel o) {
		
		return term.compareTo(o.term);
	}
	@Override
	public String toString() {
		return "[term=" + term + ", docIdList=" + docIdList + ", df=" + df + "]";
	}
	
	
	
}
