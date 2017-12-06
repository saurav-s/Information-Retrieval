package system.model;
import java.util.Set;

public class Page implements Comparable<Page>{
	private String url;
	private int depth;
	private Set<String> outLinks;
	
	public Page(String url, int depth, Set<String> outLinks) {
		super();
		this.url = url;	
		this.depth = depth;
		this.outLinks = outLinks;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public Set<String> getOutLinks() {
		return outLinks;
	}
	public void setOutLinks(Set<String> outLinks) {
		this.outLinks = outLinks;
	}
	
	@Override
	public String toString() {
		return "Page [url=" + url + ", depth=" + depth + ", outLinks=" + outLinks + "]";
	}

	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 *  !!!!!!         Important        !!!!!!
	 * Equals method has been overridden to ignore depth while comparing pages
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/*
	 * *(non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * !!!!!!         Important        !!!!!!
	 * Equals method has been overridden to ignore depth and other attributes while comparing pages
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Page other = (Page) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
	@Override
	public int compareTo(Page p) {	
		return this.getUrl().compareTo(p.getUrl());
	}



}
