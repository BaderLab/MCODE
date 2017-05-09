package ca.utoronto.tdccbr.mcode.internal.task;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
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
	private final int analyze;
	private final int resultId;

	private boolean interrupted;
	private CyNetwork network;
	
	private int count;

	private List<MCODECluster> clusters;
	
	private static final Logger logger = LoggerFactory.getLogger(MCODEAnalyzeTask.class);

	/**
	 * Scores and finds clusters in a given network
	 *
	 * @param network The network to cluster
	 * @param analyze Tells the task if we need to rescore and/or refind
	 * @param resultId Identifier of the current result set
	 * @param alg reference to the algorithm for this network
	 */
	public MCODEAnalyzeTask(
			final CyNetwork network,
			final int analyze,
			final int resultId,
			final MCODEAlgorithm alg,
			final MCODEUtil mcodeUtil
	) {
		this.network = network;
		this.analyze = analyze;
		this.resultId = resultId;
		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
	}

	/**
	 * Run MCODE (Both score and find steps).
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (taskMonitor == null)
			throw new IllegalStateException("Task Monitor is not set.");

		taskMonitor.setTitle("MCODE Analysis");
		
		mcodeUtil.resetLoading();

		try {
			// Run MCODE scoring algorithm - node scores are saved in the alg object
			alg.setTaskMonitor(taskMonitor, network.getSUID());

			// Only (re)score the graph if the scoring parameters have been changed
			if (analyze == MCODEAnalyzeAction.RESCORE) {
				taskMonitor.setProgress(0.001);
				taskMonitor.setStatusMessage("Scoring Network (Step 1 of 3)");
				
				alg.scoreGraph(network, resultId);

				if (interrupted)
					return;

				logger.info("Network was scored in " + alg.getLastScoreTime() + " ms.");
			}

			taskMonitor.setProgress(0.001);
			taskMonitor.setStatusMessage("Finding Clusters (Step 2 of 3)");

			clusters = alg.findClusters(network, resultId);

			if (interrupted)
				return;

			taskMonitor.setProgress(0.001);
			taskMonitor.setStatusMessage("Drawing Results (Step 3 of 3)");

			// Also create all the images here for the clusters, since it can be a time consuming operation
			mcodeUtil.sortClusters(clusters);
			count = 0;
			final double total = clusters.size();
			
			final int cores = Runtime.getRuntime().availableProcessors();
			final ExecutorService exec = Executors.newFixedThreadPool(cores);
			final List<Callable<MCODECluster>> tasks = new ArrayList<>();

			for (final MCODECluster c : clusters) {
				final Callable<MCODECluster> callable = () -> {
		        	if (interrupted)
		        		return null;
		        	
					final Image img = mcodeUtil.createClusterImage(c, IMAGE_SIZE, IMAGE_SIZE, null, true, null);
					c.setImage(img);
					taskMonitor.setProgress((++count) / total);
		        	
					return c;
			    };
				tasks.add(callable);
			}
			
			if (interrupted)
				return;
			
			try {
				final List<Future<MCODECluster>> results = exec.invokeAll(tasks);
				
				for (final Future<MCODECluster> future : results) {
					if (interrupted)
						break;
					
					future.get();
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
		interrupted = true;
		alg.setCancelled(true);
		mcodeUtil.removeResult(resultId);
		mcodeUtil.removeNetworkAlgorithm(network.getSUID());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		return clusters;
	}
}
