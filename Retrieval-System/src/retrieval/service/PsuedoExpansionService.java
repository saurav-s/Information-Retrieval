package retrieval.service;

import java.io.IOException;

import system.model.QueryModel;
import system.model.QueryResultModel;

public class PsuedoExpansionService {
	
	private PsuedoRelevanceModelService prs;
	public PsuedoExpansionService()
	{
		prs = new PsuedoRelevanceModelService();
	}

	public QueryResultModel performQueryExpandsion(QueryModel query, String modelType, int count) throws IOException
	{
		
		if(modelType.equalsIgnoreCase("BM25"))
		{
			Bm25RetrievalServiceImpl bm25 = new Bm25RetrievalServiceImpl();
			QueryResultModel qr =bm25.getQueryResults(query, count);
			QueryModel expandedqr = prs.performPsuedoRelevance(query, qr.getResults());
			return bm25.getQueryResults(expandedqr, count);
		}
		
		else if(modelType.equalsIgnoreCase("QLRM"))
		{
			QLRMService qlr = new QLRMService();
			QueryResultModel qr =qlr.getQueryResults(query, count);
			QueryModel expandedqr = prs.performPsuedoRelevance(query, qr.getResults());
			return qlr.getQueryResults(expandedqr, count);
		}
		else
		{
			TermWeightIdfServiceImpl qlr = new TermWeightIdfServiceImpl();
			QueryResultModel qr =qlr.getQueryResults(query, count);
			QueryModel expandedqr = prs.performPsuedoRelevance(query, qr.getResults());
			return qlr.getQueryResults(expandedqr, count);
		}
	}

}
