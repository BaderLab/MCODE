package org.cytoscape.mcode.internal.event;

import java.awt.Image;

import org.cytoscape.mcode.internal.model.MCODECluster;

public class AnalysisCompletedEvent {

	private final boolean successful;
	private final MCODECluster[] clusters;
	private final Image[] imageList;

	public AnalysisCompletedEvent(final boolean successful, final MCODECluster[] clusters, final Image[] imageList) {
		this.successful = successful;
		this.clusters = clusters;
		this.imageList = imageList;
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
	public MCODECluster[] getClusters() {
		return clusters;
	}

	/**
	 * Get image list of computed clusters to be used for display.
	 * @return
	 */
	public Image[] getImageList() {
		return imageList;
	}
}
