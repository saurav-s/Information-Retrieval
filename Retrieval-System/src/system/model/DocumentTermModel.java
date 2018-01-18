package system.model;

public class DocumentTermModel implements Comparable<DocumentTermModel>{
	
	String term;
	int tf;
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	
	@Override
	public String toString() {
		return "[term=" + term + ", tf=" + tf + "]";
	}
	
	@Override
	public int compareTo(DocumentTermModel o) {
		return tf > o.getTf() ? -1 : tf == o.getTf() ? 0 : 1; 
	}

}
