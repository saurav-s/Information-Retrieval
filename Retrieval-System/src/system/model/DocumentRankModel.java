package system.model;

public class DocumentRankModel  implements Comparable<DocumentRankModel>{
	
	private int docId;
	private double rankScore;
	private String snippet;
	
	public int getDocId() {
		return docId;
	}


	public void setDocId(int docId) {
		this.docId = docId;
	}


	public double getRankScore() {
		return rankScore;
	}


	public void setRankScore(double rankScore) {
		this.rankScore = rankScore;
	}




	@Override
	public int compareTo(DocumentRankModel o) {
		return (this.rankScore < o.getRankScore()) ? 1 : (this.rankScore == o.getRankScore()) ? 0 : -1 ;
	}


	public String getSnippet() {
		return snippet;
	}


	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}


	@Override
	public String toString() {
		return "DocumentRankModel [docId=" + docId + ", rankScore=" + rankScore + ", snippet=" + snippet + "]";
	}


}
