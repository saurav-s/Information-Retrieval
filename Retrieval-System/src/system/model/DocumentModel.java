package system.model;

import org.jsoup.nodes.Document;

public class DocumentModel {
	private Document document;
	private String name;
	
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[document=" + document + ", name=" + name + "]";
	}
	

}
