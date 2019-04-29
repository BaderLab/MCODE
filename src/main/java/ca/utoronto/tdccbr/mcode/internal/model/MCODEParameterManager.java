package ca.utoronto.tdccbr.mcode.internal.model;

import java.util.HashMap;
import java.util.Map;

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
 ** User: Gary Bader
 ** Date: Jun 25, 2004
 ** Time: 2:15:10 PM
 ** Description: Singleton class to store the current parameters
 **/

/**
 * Stores the current parameters for MCODE.  Parameters are entered in the NewAnalysisPanel and
 * stored in a hash map for the particular network being analyzed by the AnalysisAction
 * if the analysis produced a result.
 */
public class MCODEParameterManager {

	/** Parameters being used by the UI right now. */
	private MCODEParameters liveParams;
	private Map<Long, MCODEParameters> networkParamsMap = new HashMap<>();
	private Map<Integer, MCODEParameters> resultParamsMap = new HashMap<>();

	/**
	 * Current parameters can only be updated using this method.
	 * This method is called by AnalysisAction after comparisons have been conducted
	 * between the last saved version of the parameters and the current user's version.
	 *
	 * @param params The new current parameters to set
	 * @param resultId Id of the result set
	 * @param network The target network
	 */
	public void setParams(MCODEParameters params, int resultId, CyNetwork network) {
		// Cannot simply equate the passed params and new params classes since that creates a permanent reference
		// and prevents us from keeping 2 sets of the class such that the saved version is not altered
		// until this method is called
		MCODEParameters netParams = new MCODEParameters(
				network,
				params.getScope(),
				params.getSelectedNodes(),
				params.getIncludeLoops(),
				params.getDegreeCutoff(),
				params.getKCore(),
				params.getMaxDepthFromStart(),
				params.getNodeScoreCutoff(),
				params.getFluff(),
				params.getHaircut(),
				params.getFluffNodeDensityCutoff());

		networkParamsMap.put(network.getSUID(), netParams);

		MCODEParameters resultParams = new MCODEParameters(
				network,
				params.getScope(),
				params.getSelectedNodes(),
				params.getIncludeLoops(),
				params.getDegreeCutoff(),
				params.getKCore(),
				params.getMaxDepthFromStart(),
				params.getNodeScoreCutoff(),
				params.getFluff(),
				params.getHaircut(),
				params.getFluffNodeDensityCutoff());

		resultParamsMap.put(resultId, resultParams);
	}

	/**
	 * Get a copy of the last parameters for a particular network. Only a copy of the current param object is
	 * returned to avoid side effects.
	 * <p/>
	 * Note: parameters can be changed by the user after you have your own copy,
	 * so if you always need the latest, you should get the updated parameters again.                                                    
	 *
	 * @param networkID Id of the network
	 * @return A copy of the parameters
	 */
	public MCODEParameters getNetworkParams(Long networkID) {
		MCODEParameters params = networkID != null ? networkParamsMap.get(networkID) : null;
		
		return params != null ? params.copy() : new MCODEParameters();
	}
	
	public MCODEParameters getResultParams(int resultId) {
		MCODEParameters params = resultParamsMap.get(resultId);
		
		return params != null ? params.copy() : null;
	}

	public void removeResultParams(int resultId) {
		resultParamsMap.remove(resultId);
	}
	
	public MCODEParameters getLiveParams() {
		return liveParams != null ? liveParams.copy() : new MCODEParameters();
	}
	
	public void setLiveParams(MCODEParameters liveParams) {
		this.liveParams = liveParams;
	}
}
