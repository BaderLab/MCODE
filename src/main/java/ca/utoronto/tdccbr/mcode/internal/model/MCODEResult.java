package ca.utoronto.tdccbr.mcode.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;

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
 * *
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 7:00:13 PM
 * * Description: Utilities for MCODE
 */

public class MCODEResult {
	
	private final int id;
	private final transient CyNetwork network;
	private final List<MCODECluster> clusters = new ArrayList<>();
	
	private final Object lock = new Object();
	
	public MCODEResult(int id, CyNetwork network, List<MCODECluster> clusters) {
		this.id = id;
		this.network = network;
		
		if (clusters != null)
			this.clusters.addAll(clusters);
	}
	
	public int getId() {
		return id;
	}
	
	public CyNetwork getNetwork() {
		return network;
	}
	
	public List<MCODECluster> getClusters() {
		synchronized (lock) {
			return new ArrayList<>(clusters);
		}
	}
	
	public void replaceCluster(int index, MCODECluster newCluster) {
		synchronized (lock) {
			clusters.set(index, newCluster);
		}
	}
}
