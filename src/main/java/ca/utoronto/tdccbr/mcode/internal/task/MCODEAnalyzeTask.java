package ca.utoronto.tdccbr.mcode.internal.task;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ca.utoronto.tdccbr.mcode.internal.action.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

/**
 * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
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
 * * User: GaryBader
 * * Date: Jan 25, 2005
 * * Time: 8:41:53 PM
 * * Description: MCODE Score network and find cluster task
 */

/**
 * MCODE Score network and find cluster task.
 */
public class MCODEAnalyzeTask implements ObservableTask {

	private final MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final MCODEResultsManager resultsMgr;
	private final int mode;
	private final int resultId;

	private boolean cancelled;
	private CyNetwork network;
	
	private MCODEResult result;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);

	/**
	 * Scores and finds clusters in a given network
	 *
	 * @param network The network to cluster
	 * @param mode Tells the task if we need to rescore and/or refind
	 * @param resultId Identifier of the current result set
	 * @param alg reference to the algorithm for this network
	 */
	public MCODEAnalyzeTask(
			CyNetwork network,
			int mode,
			int resultId,
			MCODEAlgorithm alg,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil
	) {
		this.network = network;
		this.mode = mode;
		this.resultId = resultId;
		this.alg = alg;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
	}

	/**
	 * Run MCODE (Both score and find steps).
	 */
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (tm == null)
			throw new IllegalStateException("Task Monitor is not set.");

		tm.setTitle("MCODE Analysis");
		
		try {
			// Run MCODE scoring algorithm - node scores are saved in the alg object
			alg.setTaskMonitor(tm, network.getSUID());

			// Only (re)score the graph if the scoring parameters have been changed
			if (mode == MCODEAnalyzeAction.RESCORE) {
				tm.setProgress(0.001);
				tm.setStatusMessage("Scoring Network (Step 1 of 3)");
				
				alg.scoreGraph(network, resultId);

				if (cancelled)
					return;

				logger.info("Network was scored in " + alg.getLastScoreTime() + " ms.");
			}

			tm.setProgress(0.001);
			tm.setStatusMessage("Finding Clusters (Step 2 of 3)");

			List<MCODECluster> clusters = alg.findClusters(network, resultId);

			if (cancelled || clusters.isEmpty())
				return;

			mcodeUtil.sortClusters(clusters);
			
			tm.setStatusMessage("Drawing Results (Step 3 of 3)");
			result = resultsMgr.createResult(network, clusters);
			
			new Thread(() -> {
				resultsMgr.addResult(result);
				mcodeUtil.disposeUnusedNetworks(resultsMgr.getAllResults());
			}).start();
			
			tm.setProgress(1.0);
		} catch (Exception e) {
			throw new Exception("Error while executing the MCODE analysis", e);
		}
	}

	@Override
	public void cancel() {
		cancelled = true;
		alg.setCancelled(true);
		resultsMgr.removeResult(resultId);
		mcodeUtil.removeNetworkAlgorithm(network.getSUID());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type == MCODEResult.class)
			return result;
		
		if (type == String.class) {
			Color color = LookAndFeelUtil.getSuccessColor();
			
			if (color == null)
				color = Color.DARK_GRAY;
			
			StringBuilder sb = new StringBuilder();
			
			if (result == null) {
				sb.append("No clusters were found.\n"
						+ "You can try changing the MCODE parameters or modifying your node selection"
						+ " if you are using a selection-specific scope."
				);
			} else {
				List<MCODECluster> clusters = result.getClusters();
				
				sb.append(String.format(
						"<html><body>"
						+ "<span style='font-family: monospace; color: %1$s;'>Result #" + resultId + ":</span><br /> <br />"
						+ "<table style='font-family: monospace; color: %1$s;'>"
						+ "<tr style='font-weight: bold; border-width: 0px 0px 1px 0px; border-style: dotted;'>"
						+ "<th style='text-align: left;'>Rank</th>"
						+ "<th style='text-align: left;'>Score</th>"
						+ "<th style='text-align: left;'>Nodes</th>"
						+ "<th style='text-align: left;'>Edges</th>"
						+ "</tr>",
						("#" + Integer.toHexString(color.getRGB()).substring(2))
				));
				
				for (MCODECluster c : clusters)
					sb.append(String.format(
							"<tr>"
							+ "<td style='text-align: right;'>%d</td>"
							+ "<td style='text-align: right;'>%f</td>"
							+ "<td style='text-align: right;'>%d</td>"
							+ "<td style='text-align: right;'>%d</td></tr>",
							c.getRank(),
							c.getScore(),
							c.getGraph().getNodeCount(),
							c.getGraph().getEdgeCount()
					));
				
				sb.append("</table></body></html>");
			}
			
			return sb.toString();
		}
		
		if (type == JSONResult.class) {
			Gson gson = new Gson();
			JSONResult res = () -> { return gson.toJson(result); };
			
			return res;
		}
		
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(MCODEResult.class, String.class, JSONResult.class);
	}
}
