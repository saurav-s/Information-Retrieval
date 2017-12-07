package retrieval.model;

public class IndexModel {

	int docId;
	int tf;


	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
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
		return "[docId=" + docId + ", tf=" + tf + "]";
	}


}
