package ca.utoronto.tdccbr.mcode.internal.model;

import static ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope.NETWORK;

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
	private Long networkSUID;
	private MCODEAnalysisScope scope;
	private Long[] selectedNodes;

	// used in scoring stage
	private boolean includeLoops;
	private int degreeCutoff;
	private int kCore;

	// used in cluster finding stage
	private boolean optimize;
	private int maxDepthFromStart;
	private double nodeScoreCutoff;
	private boolean fluff;
	private boolean haircut;
	private double fluffNodeDensityCutoff;

	/**
	 * Constructor for the parameter set object. Default parameters are:
	 * networkViewSUID = null, ('use the current view')
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
	 * @param networkSUID CyNetwork to be analyzed
	 * @param scope scope of the search (equal to one of the two fields NETWORK or SELECTION)
	 * @param selectedNodes Node selection for selection-based scope
	 * @param includeLoops include loops
	 * @param degreeCutoff degree cutoff
	 * @param kCore K-core
	 * @param optimize determines if parameters are customized by user/default or optimized
	 * @param maxDepthFromStart max depth from start
	 * @param nodeScoreCutoff node score cutoff
	 * @param fluff fluff
	 * @param haircut haircut
	 * @param fluffNodeDensityCutoff fluff node density cutoff
	 */
	public MCODEParameters(
			Long networkSUID,
			MCODEAnalysisScope scope,
			Long[] selectedNodes,
			boolean includeLoops,
			int degreeCutoff,
			int kCore,
			boolean optimize,
			int maxDepthFromStart,
			double nodeScoreCutoff,
			boolean fluff,
			boolean haircut,
			double fluffNodeDensityCutoff
	) {
		setAllAlgorithmParams(networkSUID, scope, selectedNodes, includeLoops, degreeCutoff, kCore, optimize,
				maxDepthFromStart, nodeScoreCutoff, fluff, haircut, fluffNodeDensityCutoff);
	}

	/**
	 * Method for setting all parameters to their default values
	 */
	public void setDefaultParams() {
		setAllAlgorithmParams(null, NETWORK, new Long[0], false, 2, 2, false, 100, 0.2, false, true, 0.1);
	}

	/**
	 * Convenience method to set all the main algorithm parameters
	 *
	 * @param networkSUID CyNetwork to be analyzed
	 * @param scope scope
	 * @param selectedNodes Node selection for selection-based scopes
	 * @param includeLoops include loops
	 * @param degreeCutoff degree cutoff
	 * @param kCore K-core
	 * @param optimize determines if parameters are customized by user/default or optimized
	 * @param maxDepthFromStart max depth from start
	 * @param nodeScoreCutoff node score cutoff
	 * @param fluff fluff
	 * @param haircut haircut
	 * @param fluffNodeDensityCutoff fluff node density cutoff
	 */
	public void setAllAlgorithmParams(
			Long networkSUID,
			MCODEAnalysisScope scope,
			Long[] selectedNodes,
			boolean includeLoops,
			int degreeCutoff,
			int kCore,
			boolean optimize,
			int maxDepthFromStart,
			double nodeScoreCutoff,
			boolean fluff,
			boolean haircut,
			double fluffNodeDensityCutoff
	) {
		this.networkSUID = networkSUID;
		this.scope = scope;
		this.selectedNodes = selectedNodes;
		this.includeLoops = includeLoops;
		this.degreeCutoff = degreeCutoff;
		this.kCore = kCore;
		this.optimize = optimize;
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
		newParam.setNetworkSUID(networkSUID);
		newParam.setScope(scope);
		newParam.setSelectedNodes(selectedNodes);
		newParam.setIncludeLoops(includeLoops);
		newParam.setDegreeCutoff(degreeCutoff);
		newParam.setKCore(kCore);
		newParam.setOptimize(optimize);
		newParam.setMaxDepthFromStart(maxDepthFromStart);
		newParam.setNodeScoreCutoff(nodeScoreCutoff);
		newParam.setFluff(fluff);
		newParam.setHaircut(haircut);
		newParam.setFluffNodeDensityCutoff(fluffNodeDensityCutoff);
		
		return newParam;
	}
	
	public Long getNetworkSUID() {
		return networkSUID;
	}
	
	public void setNetworkSUID(Long networkSUID) {
		this.networkSUID = networkSUID;
	}

	public MCODEAnalysisScope getScope() {
		return scope;
	}

	public void setScope(MCODEAnalysisScope scope) {
		this.scope = scope;
	}

	public Long[] getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(Long[] selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	public boolean isIncludeLoops() {
		return includeLoops;
	}

	public void setIncludeLoops(boolean includeLoops) {
		this.includeLoops = includeLoops;
	}

	public int getDegreeCutoff() {
		return degreeCutoff;
	}

	public void setDegreeCutoff(int degreeCutoff) {
		this.degreeCutoff = degreeCutoff;
	}

	public int getKCore() {
		return kCore;
	}

	public void setKCore(int kCore) {
		this.kCore = kCore;
	}

	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	public boolean isOptimize() {
		return optimize;
	}

	public int getMaxDepthFromStart() {
		return maxDepthFromStart;
	}

	public void setMaxDepthFromStart(int maxDepthFromStart) {
		this.maxDepthFromStart = maxDepthFromStart;
	}

	public double getNodeScoreCutoff() {
		return nodeScoreCutoff;
	}

	public void setNodeScoreCutoff(double nodeScoreCutoff) {
		this.nodeScoreCutoff = nodeScoreCutoff;
	}

	public boolean isFluff() {
		return fluff;
	}

	public void setFluff(boolean fluff) {
		this.fluff = fluff;
	}

	public boolean isHaircut() {
		return haircut;
	}

	public void setHaircut(boolean haircut) {
		this.haircut = haircut;
	}

	public double getFluffNodeDensityCutoff() {
		return fluffNodeDensityCutoff;
	}

	public void setFluffNodeDensityCutoff(double fluffNodeDensityCutoff) {
		this.fluffNodeDensityCutoff = fluffNodeDensityCutoff;
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
