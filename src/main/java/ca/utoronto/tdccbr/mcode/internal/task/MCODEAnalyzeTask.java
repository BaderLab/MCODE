package ca.utoronto.tdccbr.mcode.internal.task;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
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

	private static final int IMAGE_SIZE = 80;
	
	private final MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final MCODEResultsManager resultsMgr;
	private final int mode;
	private final int resultId;

	private boolean cancelled;
	private CyNetwork network;
	
	private int count;

	private List<MCODECluster> clusters;
	
	private static final Logger logger = LoggerFactory.getLogger(MCODEAnalyzeTask.class);

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
		
		mcodeUtil.resetLoading();

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

			clusters = alg.findClusters(network, resultId);

			if (cancelled)
				return;

			tm.setProgress(0.001);
			tm.setStatusMessage("Drawing Results (Step 3 of 3)");

			// Also create all the images here for the clusters, since it can be a time consuming operation
			mcodeUtil.sortClusters(clusters);
			count = 0;
			final double total = clusters.size();
			
			final int cores = Runtime.getRuntime().availableProcessors();
			final ExecutorService exec = Executors.newFixedThreadPool(cores);
			final List<Callable<MCODECluster>> tasks = new ArrayList<>();
			int rank = 0;
			
			for (final MCODECluster c : clusters) {
				c.setRank(++rank);
				
				final Callable<MCODECluster> callable = () -> {
					if (cancelled)
						return null;

					mcodeUtil.createClusterImage(c, IMAGE_SIZE, IMAGE_SIZE, null, true, null);
					tm.setProgress((++count) / total);
					
					return c;
				};
				tasks.add(callable);
			}
			
			if (cancelled)
				return;
			
			try {
				final List<Future<MCODECluster>> results = exec.invokeAll(tasks);
				
				for (final Future<MCODECluster> future : results) {
					if (cancelled)
						break;
					
					MCODECluster c = future.get();
				}
			} finally {
	            exec.shutdown();
	        }
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
		if (Set.class.isAssignableFrom(type))
			return new LinkedHashSet<>(clusters);
		else if (List.class.isAssignableFrom(type))
			return new ArrayList<>(clusters);
		else if (Collection.class.isAssignableFrom(type))
			return new ArrayList<>(clusters);
		
		if (type == String.class) {
			Color color = LookAndFeelUtil.getSuccessColor();
			
			if (color == null)
				color = Color.DARK_GRAY;
			
			StringBuilder sb = new StringBuilder(String.format(
					"<html><body>"
					+ "<p style='font-family: monospace; color: %1$s;'>Result ID: " + resultId + ".</p>"
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
			
			return sb.toString();
		}
		
		if (type == JSONResult.class) {
			Gson gson = new Gson();
			JSONResult res = () -> { return gson.toJson(clusters); };
			return res;
		}
		
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, Set.class, Collection.class, JSONResult.class);
	}
}
