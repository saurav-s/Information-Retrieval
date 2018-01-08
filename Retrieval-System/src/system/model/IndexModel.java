package system.model;

import java.util.List;

public class IndexModel {

	int docId;
	List<Integer> termPositions;


	public int getTf() {
		return termPositions.size();
	}

	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + docId;
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
		IndexModel other = (IndexModel) obj;
		if (docId != other.docId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IndexModel [docId=" + docId + ", termPositions=" + termPositions + "]";
	}

	public List<Integer> getTermPositions() {
		return termPositions;
	}

	public void setTermPositions(List<Integer> termPositions) {
		this.termPositions = termPositions;
	}




}
