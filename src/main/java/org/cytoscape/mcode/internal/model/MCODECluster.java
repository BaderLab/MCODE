package org.cytoscape.mcode.internal.model;

import java.util.List;
import java.util.Map;

import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * User: Vuk Pavlovic
 * Date: Nov 29, 2006
 * Time: 5:34:46 PM
 * Description: Stores various cluster information for simple get/set purposes
 */

/**
 * Stores various cluster information for simple get/set purposes.
 */
public class MCODECluster {

	private List<Long> alCluster;
	private CyNetworkView view; // keeps track of layout so that layout process doesn't have to be repeated unnecessarily
	private CySubNetwork network;
	private Long seedNode;
	private Map<Long, Boolean> nodeSeenHashMap; // stores the nodes that have already been included in higher ranking clusters
	private double clusterScore;
	private String clusterName; // pretty much unused so far, but could store name by user's input
	private int rank;
	private int resultId;

	public MCODECluster() {
	}

	public int getResultId() {
		return resultId;
	}

	public void setResultId(int resultId) {
		this.resultId = resultId;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(final String clusterName) {
		this.clusterName = clusterName;
	}

	public CyNetworkView getView() {
		return view;
	}

	public void setView(final CyNetworkView view) {
		if (this.view != null)
			this.view.dispose();
		
		this.view = view;
	}

	public CySubNetwork getNetwork() {
		return network;
	}

	public void setNetwork(final CySubNetwork network) {
		if (view != null && view.getModel().equals(this.network))
			view.dispose();
		
		dispose(this.network);
		this.network = network;
	}

	public double getClusterScore() {
		return clusterScore;
	}

	public void setClusterScore(final double clusterScore) {
		this.clusterScore = clusterScore;
	}

	public List<Long> getALCluster() {
		return alCluster;
	}

	public void setALCluster(final List<Long> alCluster) {
		this.alCluster = alCluster;
	}

	public Long getSeedNode() {
		return seedNode;
	}

	public void setSeedNode(Long seedNode) {
		this.seedNode = seedNode;
	}

	public Map<Long, Boolean> getNodeSeenHashMap() {
		return nodeSeenHashMap;
	}

	public void setNodeSeenHashMap(Map<Long, Boolean> nodeSeenHashMap) {
		this.nodeSeenHashMap = nodeSeenHashMap;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		this.clusterName = "Cluster " + (rank + 1);
	}
	
	public void dispose() {
		if (view != null)
			view.dispose();
		dispose(network);
	}
	
	private static void dispose(final CySubNetwork net) {
		if (net != null) {
			net.getRootNetwork().removeSubNetwork(net);
			net.dispose();
		}
	}
}
