package ca.utoronto.tdccbr.mcode.internal.rest;

import java.util.ArrayList;
import java.util.List;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;

public class MCODEResults {
	
	private final int id;
	private final List<MCODECluster> clusters = new ArrayList<>();
	
	public MCODEResults(int id, List<MCODECluster> clusters) {
		this.id = id;
		
		if (clusters != null)
			this.clusters.addAll(clusters);
	}
	
	public int getId() {
		return id;
	}
	
	public List<MCODECluster> getClusters() {
		return clusters;
	}
}
