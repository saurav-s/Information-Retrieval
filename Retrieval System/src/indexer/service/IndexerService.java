package indexer.service;

import java.util.List;
import java.util.Map;

import system.model.IndexModel;

public interface IndexerService {
 Map<String, List<IndexModel>> getUnaryIndex();
}
