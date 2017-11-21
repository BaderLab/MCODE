package ca.utoronto.tdccbr.mcode.internal.model;

import static ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope.NETWORK;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;

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
 ** User: Gary Bader
 ** Date: Jan 26, 2004
 ** Time: 2:44:30 PM
 ** Description: Stores an MCODE parameter set
 **/

/**
 * Contains the MCODE analysis parameters.
 */
public class MCODEParameters {

	// scope
	private CyNetwork network;
	private MCODEAnalysisScope scope;

	// used in scoring stage
	private boolean includeLoops;
	private int degreeCutoff;
	private int kCore;

	// used in cluster finding stage
	private int maxDepthFromStart;
	private double nodeScoreCutoff;
	private boolean haircut;
	private boolean fluff;
	private double fluffNodeDensityCutoff;
	
	/** Caches the SUID of selected nodes. */
	private Long[] selectedNodes;

	/**
	 * Constructor for the parameter set object. Default parameters are:
	 * network = null, ('use the current network')
	 * scope = NETWORK,
	 * selectedNodes = new Integer[0],
	 * loops = false,
	 * degree cutoff = 2,
	 * max depth = 100,
	 * k-core = 2,
	 * node score cutoff = 0.2,
	 * fluff = false,
	 * haircut = true,
	 * fluff node density cutoff = 0.1,
	 */
	public MCODEParameters() {
		setDefaultParams();
	}

	/**
	 * Constructor for non-default algorithm parameters.
	 * Once an alalysis is conducted, new parameters must be saved so that they can be retrieved in the result panel
	 * for exploration and export purposes.
	 *
	 * @param network {@link CyNetwork} to be analyzed
	 * @param scope scope of the search (equal to one of the two fields NETWORK or SELECTION)
	 * @param selectedNodes Node selection for selection-based scope
	 * @param includeLoops include loops
	 * @param degreeCutoff degree cutoff
	 * @param kCore K-core
	 * @param maxDepthFromStart max depth from start
	 * @param nodeScoreCutoff node score cutoff
	 * @param fluff fluff
	 * @param haircut haircut
	 * @param fluffNodeDensityCutoff fluff node density cutoff
	 */
	public MCODEParameters(
			CyNetwork network,
			MCODEAnalysisScope scope,
			Long[] selectedNodes,
			boolean includeLoops,
			int degreeCutoff,
			int kCore,
			int maxDepthFromStart,
			double nodeScoreCutoff,
			boolean fluff,
			boolean haircut,
			double fluffNodeDensityCutoff
	) {
		setAllAlgorithmParams(network, scope, selectedNodes, includeLoops, degreeCutoff, kCore,
				maxDepthFromStart, nodeScoreCutoff, fluff, haircut, fluffNodeDensityCutoff);
	}

	/**
	 * Method for setting all parameters to their default values
	 */
	public void setDefaultParams() {
		setAllAlgorithmParams(null, NETWORK, new Long[0], false, 2, 2, 100, 0.2, false, true, 0.1);
	}

	/**
	 * Convenience method to set all the main algorithm parameters
	 *
	 * @param network {@link CyNetwork} to be analyzed
	 * @param scope scope
	 * @param selectedNodes Node selection for selection-based scopes
	 * @param includeLoops include loops
	 * @param degreeCutoff degree cutoff
	 * @param kCore K-core
	 * @param maxDepthFromStart max depth from start
	 * @param nodeScoreCutoff node score cutoff
	 * @param fluff fluff
	 * @param haircut haircut
	 * @param fluffNodeDensityCutoff fluff node density cutoff
	 */
	public void setAllAlgorithmParams(
			CyNetwork network,
			MCODEAnalysisScope scope,
			Long[] selectedNodes,
			boolean includeLoops,
			int degreeCutoff,
			int kCore,
			int maxDepthFromStart,
			double nodeScoreCutoff,
			boolean fluff,
			boolean haircut,
			double fluffNodeDensityCutoff
	) {
		this.network = network;
		this.scope = scope;
		this.selectedNodes = selectedNodes;
		this.includeLoops = includeLoops;
		this.degreeCutoff = degreeCutoff;
		this.kCore = kCore;
		this.maxDepthFromStart = maxDepthFromStart;
		this.nodeScoreCutoff = nodeScoreCutoff;
		this.fluff = fluff;
		this.haircut = haircut;
		this.fluffNodeDensityCutoff = fluffNodeDensityCutoff;
	}

	/**
	 * Copies a parameter set object
	 *
	 * @return A copy of the parameter set
	 */
	public MCODEParameters copy() {
		MCODEParameters newParam = new MCODEParameters();
		newParam.setNetwork(network);
		newParam.setScope(scope);
		newParam.setSelectedNodes(selectedNodes);
		newParam.setIncludeLoops(includeLoops);
		newParam.setDegreeCutoff(degreeCutoff);
		newParam.setKCore(kCore);
		newParam.setMaxDepthFromStart(maxDepthFromStart);
		newParam.setNodeScoreCutoff(nodeScoreCutoff);
		newParam.setFluff(fluff);
		newParam.setHaircut(haircut);
		newParam.setFluffNodeDensityCutoff(fluffNodeDensityCutoff);
		
		return newParam;
	}
	
	@Tunable(
			description = "Network",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
			context = "nogui"
	)
	public CyNetwork getNetwork() {
		return network;
	}
	
	public void setNetwork(CyNetwork network) {
		this.network = network;
	}

	@Tunable(
			description = "Scope",
			longDescription = "The scope of the analysis may be ```SELECTION``` or ```NETWORK``` (the default).",
			exampleStringValue = "NETWORK",
			context = "nogui"
	)
	public MCODEAnalysisScope getScope() {
		return scope;
	}

	public void setScope(MCODEAnalysisScope scope) {
		this.scope = scope;
	}

	@Tunable(
			description = "Include Loops",
			longDescription = "If ```true```, self-edges may increase a node's score slightly.",
			exampleStringValue = "false",
			context = "nogui"
	)
	// Don't name this method isIncludeLoops(), because Cytoscape Tunables only accept the prefix "get".
	public boolean getIncludeLoops() {
		return includeLoops;
	}

	public void setIncludeLoops(boolean includeLoops) {
		this.includeLoops = includeLoops;
	}

	@Tunable(
			description = "Degree Cutoff",
			longDescription = "Sets the minimum number of edges for a node to be scored.",
			exampleStringValue = "2",
			context = "nogui"
	)
	public int getDegreeCutoff() {
		return degreeCutoff;
	}

	public void setDegreeCutoff(int degreeCutoff) {
		this.degreeCutoff = degreeCutoff;
	}

	@Tunable(
			description = "K-Core",
			longDescription = "Filters out clusters lacking a maximally inter-connected core of at least k edges per node.",
			exampleStringValue = "2",
			context = "nogui"
	)
	public int getKCore() {
		return kCore;
	}

	public void setKCore(int kCore) {
		this.kCore = kCore;
	}

	@Tunable(
			description = "Max. Depth",
			longDescription = "Limits the cluster size by setting the maximum search distance from a seed node (100 virtually means no limit).",
			exampleStringValue = "100",
			context = "nogui"
	)
	public int getMaxDepthFromStart() {
		return maxDepthFromStart;
	}

	public void setMaxDepthFromStart(int maxDepthFromStart) {
		this.maxDepthFromStart = maxDepthFromStart;
	}

	@Tunable(
			description = "Node Score Cutoff",
			longDescription = "Sets the acceptable score deviance from the seed node's score for expanding a cluster (most influental parameter for cluster size).",
			exampleStringValue = "0.2",
			context = "nogui"
	)
	public double getNodeScoreCutoff() {
		return nodeScoreCutoff;
	}

	public void setNodeScoreCutoff(double nodeScoreCutoff) {
		this.nodeScoreCutoff = nodeScoreCutoff;
	}

	@Tunable(
			description = "Haircut",
			longDescription = "Remove singly connected nodes from clusters.",
			exampleStringValue = "true",
			context = "nogui"
	)
	// Don't name this method isHaircut(), because Cytoscape Tunables only accept the prefix "get".
	public boolean getHaircut() {
		return haircut;
	}

	public void setHaircut(boolean haircut) {
		this.haircut = haircut;
	}
	
	@Tunable(
			description = "Fluff",
			longDescription = "Expand core cluster by one neighbour shell (applied after the optional haircut).",
			exampleStringValue = "true",
			context = "nogui"
	)
	// Don't name this method isFluff(), because Cytoscape Tunables only accept the prefix "get".
	public boolean getFluff() {
		return fluff;
	}

	public void setFluff(boolean fluff) {
		this.fluff = fluff;
	}
	
	@Tunable(
			description = "Fluff Node Density Cutoff",
			longDescription = "Limits fluffing by setting the acceptable node density deviance from the core cluster density (allows clusters' edges to overlap).",
			exampleStringValue = "0.1",
			context = "nogui"
	)
	public double getFluffNodeDensityCutoff() {
		return fluffNodeDensityCutoff;
	}

	public void setFluffNodeDensityCutoff(double fluffNodeDensityCutoff) {
		this.fluffNodeDensityCutoff = fluffNodeDensityCutoff;
	}
	
	public Long[] getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(Long[] selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	/**
	 * Generates a summary of the parameters. Only parameters that are necessary are included.
	 * For example, if fluff is not turned on, the fluff density cutoff will not be included.
	 * 
	 * @return Buffered string summarizing the parameters
	 */
	@Override
	public String toString() {
		String lineSep = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		sb.append("   Network Scoring:" + lineSep + "      Include Loops: " + includeLoops + "  Degree Cutoff: " +
				  degreeCutoff + lineSep);
		sb.append("   Cluster Finding:" + lineSep + "      Node Score Cutoff: " + nodeScoreCutoff + "  Haircut: " +
				  haircut + "  Fluff: " + fluff +
				  ((fluff) ? ("  Fluff Density Cutoff " + fluffNodeDensityCutoff) : "") + "  K-Core: " + kCore +
				  "  Max. Depth from Seed: " + maxDepthFromStart + lineSep);
		return sb.toString();
	}
}
