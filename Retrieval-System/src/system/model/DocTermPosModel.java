package system.model;

import java.util.List;

public class DocTermPosModel {
	String term;
	List<Integer> termPositions;
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public List<Integer> getTermPositions() {
		return termPositions;
	}
	public void setTermPositions(List<Integer> termPositions) {
		this.termPositions = termPositions;
	}
	@Override
	public String toString() {
		return "DocTermPosModel [term=" + term + ", termPositions=" + termPositions + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		DocTermPosModel other = (DocTermPosModel) obj;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	
	

}
