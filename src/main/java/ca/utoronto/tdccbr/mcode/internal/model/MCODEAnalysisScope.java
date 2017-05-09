package ca.utoronto.tdccbr.mcode.internal.model;

public enum MCODEAnalysisScope {
	NETWORK("network"),
	SELECTION("selection");

	private String name;

	private MCODEAnalysisScope(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
