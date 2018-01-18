package system.model;

public class DocumentEvaluationModel {
	private String docId;
	private boolean isRelevant;
	private Double precision;
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public boolean isRelevant() {
		return isRelevant;
	}
	public void setRelevant(boolean isRelevant) {
		this.isRelevant = isRelevant;
	}
	public Double getPrecision() {
		return precision;
	}
	public void setPrecision(Double precision) {
		this.precision = precision;
	}
	public Double getRecall() {
		return recall;
	}
	public void setRecall(Double recall) {
		this.recall = recall;
	}
	private Double recall;
	

}
