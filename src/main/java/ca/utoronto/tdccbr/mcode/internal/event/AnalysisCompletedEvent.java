package ca.utoronto.tdccbr.mcode.internal.event;

import java.util.List;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;

public class AnalysisCompletedEvent {

	private final boolean successful;
	private final List<MCODECluster> clusters;

	public AnalysisCompletedEvent(final boolean successful, final List<MCODECluster> clusters) {
		this.successful = successful;
		this.clusters = clusters;
	}

	/**
	 * @return true if the task has completed successfully.
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * Get computed clusters once MCODE has been run.  Will be null if not computed.
	 * @return
	 */
	public List<MCODECluster> getClusters() {
		return clusters;
	}
}
