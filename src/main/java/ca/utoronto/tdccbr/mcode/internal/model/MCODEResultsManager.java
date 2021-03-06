package ca.utoronto.tdccbr.mcode.internal.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;

import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

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

public class MCODEResultsManager
		implements AddedNodesListener, AddedEdgesListener, RemovedNodesListener, RemovedEdgesListener {

	/** Indexes all results in the session by their ids. */
	private Map<Integer, MCODEResult> allResults = new HashMap<>();
	/** Keeps track of analyzed networks (network SUID is key) and their respective results (list of result ids). */
	private Map<Long, Set<Integer>> networkResults = new HashMap<>();

	private int nextResultId = 1;
	
	private final MCODEUtil mcodeUtil;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final Object lock = new Object();
	
	public MCODEResultsManager(MCODEUtil mcodeUtil) {
		this.mcodeUtil = mcodeUtil;
	}
	
	@Override
	public void handleEvent(RemovedEdgesEvent evt) {
		staleResults(evt.getSource());
	}

	@Override
	public void handleEvent(RemovedNodesEvent evt) {
		staleResults(evt.getSource());
	}

	@Override
	public void handleEvent(AddedEdgesEvent evt) {
		staleResults(evt.getSource());
	}

	@Override
	public void handleEvent(AddedNodesEvent evt) {
		staleResults(evt.getSource());
	}
	
	public int getNextResultId() {
		return nextResultId;
	}
	
	public boolean containsNetworkResult(Long suid) {
		synchronized (lock) {
			return networkResults.containsKey(suid);
		}
	}
	
	public Set<Integer> getNetworkResults(Long suid) {
		synchronized (lock) {
			var ids = networkResults.get(suid);
			
			return ids != null ? new HashSet<>(ids) : Collections.emptySet();
		}
	}

	public MCODEResult getResult(int resultId) {
		synchronized (lock) {
			return allResults.get(resultId);
		}
	}
	
	/**
	 * Search for a non-stale result that has been registered for a network and which has an specific set of parameters.
	 * @param suid {@link CyNetwork} SUID
	 * @param params
	 * @return the first result found for the passed network that has the same parameters or null if it cannot find any
	 */
	public MCODEResult getFreshResult(Long suid, MCODEParameters params) {
		synchronized (lock) {
			var results = getNetworkResults(suid);
			
			for (var id : results) {
				var res = getResult(id);
				
				if (res != null && !res.isStale() && params.equals(res.getParameters()))
					return res;
			}
			
			return null;
		}
	}
	
	public Collection<MCODEResult> getAllResults() {
		return allResults.isEmpty() ? Collections.emptySet() : new LinkedHashSet<>(allResults.values());
	}
	
	public int getResultsCount() {
		return allResults.size();
	}
	
	public MCODEResult createResult(CyNetwork network, MCODEParameters params, List<MCODECluster> clusters) {
		MCODEResult res = null;
		
		synchronized (lock) {
			res = new MCODEResult(nextResultId, network, params, clusters);
			nextResultId++; // Increment next available ID
		}
		
		return res;
	}
	
	/**
	 * Fires a {@link PropertyChangeEvent} for property "resultAdded", where the new value is the added result.
	 */
	public void addResult(MCODEResult res) {
		synchronized (lock) {
			var ids = networkResults.get(res.getNetwork().getSUID());

			if (ids == null) {
				ids = new HashSet<>();
				networkResults.put(res.getNetwork().getSUID(), ids);
			}
			
			ids.add(res.getId());
			allResults.put(res.getId(), res);
		}
		
		pcs.firePropertyChange("resultAdded", null, res);
	}
	
	/**
	 * Fires a {@link PropertyChangeEvent} for property "resultRemoved", where the new value is the removed result.
	 */
	public boolean removeResult(int resultId) {
		boolean removed = false;
		mcodeUtil.getParameterManager().removeResultParams(resultId);

		synchronized (lock) {
			Long networkId = null;
			
			for (var entries : networkResults.entrySet()) {
				var ids = entries.getValue();
	
				if (ids.remove(resultId)) {
					if (ids.isEmpty())
						networkId = entries.getKey();
	
					removed = true;
					break;
				}
			}
	
			if (networkId != null)
				networkResults.remove(networkId);
			
			var res = allResults.remove(resultId);
			
			if (res != null) {
				// Delete related columns
				mcodeUtil.removeMCODEColumns(res);
				
				// Dispose clusters
				for (var c : res.getClusters())
					c.dispose();
				
				pcs.firePropertyChange("resultRemoved", null, res);
			}
		}
		
		return removed;
	}
	
	public List<MCODECluster> getClusters(int resultId) {
		synchronized (lock) {
			var res = getResult(resultId);
			
			return res != null ? res.getClusters() : Collections.emptyList();
		}
	}
	
	public MCODECluster getCluster(int resultId, int clusterRank) {
		synchronized (lock) {
			var clusters = getClusters(resultId);
			
			for (var c : clusters) {
				if (clusterRank == c.getRank())
					return c;
			}
		}
		
		return null;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	public void reset() {
		synchronized (lock) {
			nextResultId = 1;
			networkResults.clear();
			allResults.clear();
			pcs.firePropertyChange("reset", false, true);
		}
	}
	
	private void staleResults(CyNetwork net) {
		synchronized (lock) {
			for (var res : allResults.values()) {
				if (net.equals(res.getNetwork()))
					res.setStale();
			}
		}
	}
}
