package system.model;

import java.util.List;

public class SystemEvaluationModel {
private String modelName;
private List<EvaluationResultModel> evaluatedResults;
private Double map;
private Double mrr;
public String getModelName() {
	return modelName;
}
public void setModelName(String modelName) {
	this.modelName = modelName;
}
public List<EvaluationResultModel> getEvaluatedResults() {
	return evaluatedResults;
}
public void setEvaluatedResults(List<EvaluationResultModel> evaluatedResults) {
	this.evaluatedResults = evaluatedResults;
}
public Double getMap() {
	return map;
}
public void setMap(Double map) {
	this.map = map;
}
public Double getMrr() {
	return mrr;
}
public void setMrr(Double mrr) {
	this.mrr = mrr;
}
}
