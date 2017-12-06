package retrieval.model;

public class QueryModel {
	private String query;
	private int id;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "QueryModel [query=" + query + ", id=" + id + "]";
	}

}
