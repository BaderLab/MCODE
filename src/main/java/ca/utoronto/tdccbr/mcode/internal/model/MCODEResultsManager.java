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
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyNetwork;

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

public class MCODEResultsManager {

	/** Keeps track of networks (id is key) and their respective results (list of result ids). */
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
			Set<Integer> ids = networkResults.get(suid);
			
			return ids != null ? new HashSet<>(ids) : Collections.emptySet();
		}
	}

	public MCODEResult getResult(int resultId) {
		synchronized (lock) {
			return allResults.get(resultId);
		}
	}
	
	public Collection<MCODEResult> getAllResults() {
		return allResults.isEmpty() ? Collections.emptySet() : new LinkedHashSet<>(allResults.values());
	}
	
	/**
	 * @param network Target CyNetwork
	 * @param clusters Clusters created as result of the analysis.
	 */
	public MCODEResult createResult(CyNetwork network, List<MCODECluster> clusters) {
		MCODEResult res = null;
		
		synchronized (lock) {
			res = new MCODEResult(nextResultId, network, clusters);
			nextResultId++; // Increment next available ID
		}
		
		return res;
	}
	
	/**
	 * Fires a {@link PropertyChangeEvent} for property "newResult", where the new value is the added result.
	 */
	public void addResult(MCODEResult res) {
		synchronized (lock) {
			Set<Integer> ids = networkResults.get(res.getNetwork().getSUID());

			if (ids == null) {
				ids = new HashSet<>();
				networkResults.put(res.getNetwork().getSUID(), ids);
			}

			allResults.put(res.getId(), res);
		}
		
		pcs.firePropertyChange("newResult", null, res);
	}
	
	public boolean removeResult(int resultId) {
		boolean removed = false;
		mcodeUtil.getParameterManager().removeResultParams(resultId);

		synchronized (lock) {
			Long networkId = null;
			
			for (Entry<Long, Set<Integer>> entries : networkResults.entrySet()) {
				Set<Integer> ids = entries.getValue();
	
				if (ids.remove(resultId)) {
					if (ids.isEmpty())
						networkId = entries.getKey();
	
					removed = true;
					break;
				}
			}
	
			if (networkId != null)
				networkResults.remove(networkId);
			
			MCODEResult res = allResults.remove(resultId);
			
			if (res != null) {
				for (MCODECluster c : res.getClusters())
					c.dispose();
			}
		}
		
		return removed;
	}
	
	public List<MCODECluster> getClusters(int resultId) {
		synchronized (lock) {
			MCODEResult res = getResult(resultId);
			
			return res != null ? res.getClusters() : Collections.emptyList();
		}
	}
	
	public MCODECluster getCluster(int resultId, int clusterRank) {
		synchronized (lock) {
			List<MCODECluster> clusters = getClusters(resultId);
			
			for (MCODECluster c : clusters) {
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
		}
	}
}
