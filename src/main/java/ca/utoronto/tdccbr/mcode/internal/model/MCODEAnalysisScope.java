package ca.utoronto.tdccbr.mcode.internal.model;

public enum MCODEAnalysisScope {
	NETWORK("In Whole Network"),
	SELECTION("From Selection");
	
	private final String label;

	private MCODEAnalysisScope(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
