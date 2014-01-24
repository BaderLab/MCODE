package ca.utoronto.tdccbr.mcode.internal.model;

import java.util.HashMap;
import java.util.Map;

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
 * Stores the current parameters for MCODE.  Parameters are entered in the MCODEMainPanel and
 * stored in a hash map for the particular network being analyzed by the MCODEAnalyzeAction
 * if the analysis produced a result.
 */
public class MCODECurrentParameters {

	private Map<Long, MCODEParameterSet> currentParams = new HashMap<Long, MCODEParameterSet>();
	private Map<Integer, MCODEParameterSet> resultParams = new HashMap<Integer, MCODEParameterSet>();

	/**
	 * Get a copy of the current parameters for a particular network. Only a copy of the current param object is
	 * returned to avoid side effects.  The user should use the following code to get their
	 * own copy of the current parameters:
	 * MCODECurrentParameters.getInstance().getParamsCopy();
	 * <p/>
	 * Note: parameters can be changed by the user after you have your own copy,
	 * so if you always need the latest, you should get the updated parameters again.                                                    
	 *
	 * @param networkID Id of the network
	 * @return A copy of the parameters
	 */
	public MCODEParameterSet getParamsCopy(Long networkID) {
		if (networkID != null) {
			return currentParams.get(networkID).copy();
		} else {
			MCODEParameterSet newParams = new MCODEParameterSet();
			return newParams.copy();
		}
	}

	/**
	 * Current parameters can only be updated using this method.
	 * This method is called by MCODEAnalyzeAction after comparisons have been conducted
	 * between the last saved version of the parameters and the current user's version.
	 *
	 * @param newParams The new current parameters to set
	 * @param resultId Id of the result set
	 * @param networkID Id of the network
	 */
	public void setParams(MCODEParameterSet newParams, int resultId, Long networkID) {
		//cannot simply equate the params and newParams classes since that creates a permanent reference
		//and prevents us from keeping 2 sets of the class such that the saved version is not altered
		//until this method is called
		MCODEParameterSet currentParamSet = new MCODEParameterSet(newParams.getScope(), newParams.getSelectedNodes(),
																  newParams.isIncludeLoops(), newParams
																		  .getDegreeCutoff(), newParams.getKCore(),
																  newParams.isOptimize(), newParams
																		  .getMaxDepthFromStart(), newParams
																		  .getNodeScoreCutoff(), newParams.isFluff(),
																  newParams.isHaircut(), newParams
																		  .getFluffNodeDensityCutoff());

		currentParams.put(networkID, currentParamSet);

		MCODEParameterSet resultParamSet = new MCODEParameterSet(newParams.getScope(), newParams.getSelectedNodes(),
																 newParams.isIncludeLoops(), newParams
																		 .getDegreeCutoff(), newParams.getKCore(),
																 newParams.isOptimize(), newParams
																		 .getMaxDepthFromStart(), newParams
																		 .getNodeScoreCutoff(), newParams.isFluff(),
																 newParams.isHaircut(), newParams
																		 .getFluffNodeDensityCutoff());

		resultParams.put(resultId, resultParamSet);
	}

	public MCODEParameterSet getResultParams(int resultId) {
		return resultParams.get(resultId).copy();
	}

	public void removeResultParams(int resultId) {
		resultParams.remove(resultId);
	}
}
