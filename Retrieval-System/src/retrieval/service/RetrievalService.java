/*
 * This RetrievalService is an object of any class that implements 
 * the interface RetrievalService defined below.
 * 
 * A RetrievalService depicts a type of mathematical retrieval model.
 * 
 * A RetrievalService must fetch K top documents relevant to the given 
 * query.
 */

package retrieval.service;

import system.model.QueryModel;
import system.model.QueryResultModel;

public interface RetrievalService {
	
	// GIVEN: a query that has an id assosciated with it, the query itself 
	//        and the needed number of top documents
	// RETURNS: a QueryResultModel that has atleast K top documents. 
	QueryResultModel getQueryResults(QueryModel query, int size);
}
