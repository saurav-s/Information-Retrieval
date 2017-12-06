package indexer.model;
/**
 * 
 * This class stores information about the token type and total number of tokens in a document
 * @author sanket saurav
 */
public class DocTokenInfoModel {
	//unary,binary..
	private String tokenType;
	private int tokenCount;
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public int getTokenCount() {
		return tokenCount;
	}
	public void setTokenCount(int tokenCount) {
		this.tokenCount = tokenCount;
	}
	@Override
	public String toString() {
		return "[tokenType=" + tokenType + ", tokenCount=" + tokenCount + "]";
	}
	
}
