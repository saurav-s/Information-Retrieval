package system.model;

public class TermFrequencyModel implements Comparable<TermFrequencyModel> {
	
	private String term;
	private int tf;
	
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		result = prime * result + tf;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TermFrequencyModel other = (TermFrequencyModel) obj;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		if (tf != other.tf)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(TermFrequencyModel obj) {
		return ((tf < obj.tf) ? 1 : (tf == obj.tf) ? 0 : -1);
	}
	@Override
	public String toString() {
		return "TermFrequencyModel [term=" + term + ", tf=" + tf + "]";
	}
	
	
	
}
