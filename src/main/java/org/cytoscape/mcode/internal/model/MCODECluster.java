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
	private double score;
	private String name; // pretty much unused so far, but could store name by user's input
	private int rank;
	private int resultId;
	private boolean disposed;

	public MCODECluster(final int resultId,
						final Long seedNode,
						final CySubNetwork network,
						final double score,
						final List<Long> alCluster,
						final Map<Long, Boolean> nodeSeenHashMap) {
		assert seedNode != null;
		assert network != null;
		assert alCluster != null;
		assert nodeSeenHashMap != null;
		
		this.resultId = resultId;
		this.seedNode = seedNode;
		this.network = network;
		this.score = score;
		this.alCluster = alCluster;
		this.nodeSeenHashMap = nodeSeenHashMap;
	}

	public int getResultId() {
		return resultId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		throwExceptionIfDisposed();
		this.name = name;
	}

	public synchronized CyNetworkView getView() {
		return view;
	}

	public synchronized void setView(final CyNetworkView view) {
		throwExceptionIfDisposed();
		
		if (this.view != null)
			this.view.dispose();
		
		this.view = view;
	}

	public synchronized CySubNetwork getNetwork() {
		return network;
	}

	public double getScore() {
		return score;
	}

	public List<Long> getALCluster() {
		return alCluster;
	}

	public Long getSeedNode() {
		return seedNode;
	}

	public Map<Long, Boolean> getNodeSeenHashMap() {
		return nodeSeenHashMap;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		this.name = "Cluster " + (rank + 1);
	}
	
	public synchronized boolean isDisposed() {
		return disposed;
	}

	public synchronized void dispose() {
		if (isDisposed()) return;
		
		if (view != null)
			view.dispose();
		
		network.getRootNetwork().removeSubNetwork(network);
		network.dispose();
		
		disposed = true;
	}

	@Override
	public String toString() {
		return "MCODECluster [clusterName=" + name + ", clusterScore=" + score + 
				", rank=" + rank + ", resultId=" + resultId + ", disposed=" + disposed + "]";
	}

	private void throwExceptionIfDisposed() {
		if (isDisposed())
			throw new RuntimeException("MCODECluster has been disposed and cannot be used anymore: ");
	}
}
