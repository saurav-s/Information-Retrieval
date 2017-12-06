package indexer.model;

import java.util.List;

/**
 * This class stores information about tokens of a document 
 * @author sanket saurav
 * 
 */
public class DocTokenModel {
	private int docId;
	private List<DocTokenInfoModel> tokenInfoList;

	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public List<DocTokenInfoModel> getTokenInfoList() {
		return tokenInfoList;
	}
	public void setTokenInfoList(List<DocTokenInfoModel> tokenInfoList) {
		this.tokenInfoList = tokenInfoList;
	}
	@Override
	public String toString() {
		return "DocTokenModel [docId=" + docId + ", tokenInfoList=" + tokenInfoList + "]";
	}

}
