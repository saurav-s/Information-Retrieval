package system.model;

public class DocumentIdMapperModel {

	private int docId;
	private String docName;
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	
	@Override
	public String toString() {
		return "[docId=" + docId + ", docName=" + docName + "]";
	}
	
}
