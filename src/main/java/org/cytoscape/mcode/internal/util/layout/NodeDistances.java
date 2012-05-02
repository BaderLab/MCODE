package org.cytoscape.mcode.internal.util.layout;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Calculates the all-pairs-shortest-paths (APSP) of a set of
 * <code>org.cytoscape.model.CyNode</code> objects that reside in a
 * <code>org.cytoscape.model.CyNetwork</code>. Note: this was copied from
 * giny.util because it is being phased out. Eventually the layout API will be
 * available to use (TODO: remove when layout API is available)
 * 
 * @see giny.util.IntNodeDistances
 */
public class NodeDistances implements MonitorableTask {

	public static final int INFINITY = Integer.MAX_VALUE;

	protected List<CyNode> nodesList;
	protected CyNetwork network;
	protected int[][] distances;
	protected boolean directed;

	// Keep track of progress for monitoring:
	protected int currentProgress;
	protected int lengthOfTask;
	protected String statusMessage;
	protected boolean done;
	protected boolean canceled;
	protected Map<Long, Integer> nodeIndexToMatrixIndexMap; // a root node index to matrix

	/**
	 * The main constructor
	 * 
	 * @param nodesList List of nodes ordered by the index map
	 * @param network The <code>org.cytoscape.model.CyNetwork</code> in which the nodes reside
	 * @param nodeIndexToMatrixIndexMap An index map that maps your root graph indices to the returned matrix indices
	 */
	public NodeDistances(List<CyNode> nodesList, CyNetwork network, Map<Long, Integer> nodeIndexToMatrixIndexMap) {
		this.nodesList = nodesList;
		this.nodeIndexToMatrixIndexMap = nodeIndexToMatrixIndexMap;
		this.network = network;
		this.distances = new int[nodesList.size()][];
		this.directed = false;
	}

	/**
	 * Calculates the APSP in a separate thread.
	 * 
	 * @param return_when_done
	 *            if <code>true</code>, then this method will return only when
	 *            the task is done, else, it will return immediately after
	 *            spawning the thread that performs the task
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void start(boolean return_when_done) {
		final SwingWorker worker = new SwingWorker() {

			@Override
			protected Object doInBackground() throws Exception {
				return new NodeDistancesTask();
			}
		};

		worker.execute();

		if (return_when_done) {
			try {
				// maybe use finished() instead
				worker.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean wasCanceled() {
		return canceled;
	}

	/**
	 * @return the current progress
	 */
	@Override
	public int getCurrentProgress() {
		return this.currentProgress;
	}

	/**
	 * @return the total length of the task
	 */
	@Override
	public int getLengthOfTask() {
		return this.lengthOfTask;
	}

	/**
	 * @return a <code>String</code> describing the task being performed
	 */
	@Override
	public String getTaskDescription() {
		return "Calculating Node Distances";
	}

	/**
	 * @return a <code>String</code> status message describing what the task is
	 *         currently doing (example: "Completed 23% of total.",
	 *         "Initializing...", etc).
	 */
	@Override
	public String getCurrentStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * @return <code>true</code> if the task is done, false otherwise
	 */
	@Override
	public boolean isDone() {
		return this.done;
	}

	/**
	 * Stops the task if it is currently running.
	 */
	@Override
	public void stop() {
		this.canceled = true;
		this.statusMessage = null;
	}

	/**
	 * Calculates the node distances.
	 * 
	 * @return the <code>int[][]</code> array of calculated distances or null if
	 *         the task was canceled or there was an error
	 */
	public int[][] calculate() {

		this.currentProgress = 0;
		this.lengthOfTask = distances.length;
		this.done = false;
		this.canceled = false;

		CyNode[] nodes = new CyNode[nodesList.size()];

		// We don't have to make new Integers all the time, so we store the index objects in this array for reuse
		Integer[] integers = new Integer[nodes.length];

		// Fill the nodes array with the nodes in their proper index locations.
		int index;
		CyNode from_node;

		for (int i = 0; i < nodes.length; i++) {
			from_node = nodesList.get(i);

			if (from_node == null) {
				continue;
			}

			index = nodeIndexToMatrixIndexMap.get(from_node.getSUID());

			if ((index < 0) || (index >= nodes.length)) {
				System.err.println("WARNING: GraphNode \"" + from_node +
								   "\" has an index value that is out of range: " + index +
								   ".  Graph indices should be maintained such " + "that no index is unused.");
				return null;
			}
			if (nodes[index] != null) {
				System.err.println("WARNING: GraphNode \"" + from_node + "\" has an index value ( " + index +
								   " ) that is the same as " + "that of another GraphNode ( \"" + nodes[index] +
								   "\" ).  Graph indices should be maintained such " + "that indices are unique.");
				return null;
			}
			nodes[index] = from_node;
			integers[index] = index;
		}

		LinkedList<Integer> queue = new LinkedList<Integer>();
		boolean[] completed_nodes = new boolean[nodes.length];
		Collection<CyNode> neighbors;
		CyNode to_node;

		// Node neighbor;
		int neighbor_index;
		int to_node_distance;
		int neighbor_distance;

		for (int from_node_index = 0; from_node_index < nodes.length; from_node_index++) {
			if (this.canceled) {
				// The task was canceled
				this.distances = null;
				return this.distances;
			}

			from_node = nodes[from_node_index];

			if (from_node == null) {
				// Make the distances in this row all Integer.MAX_VALUE.
				if (distances[from_node_index] == null) {
					distances[from_node_index] = new int[nodes.length];
				}

				Arrays.fill(distances[from_node_index], Integer.MAX_VALUE);
				continue;
			}

			// Make the distances row and initialize it.
			if (distances[from_node_index] == null) {
				distances[from_node_index] = new int[nodes.length];
			}
			Arrays.fill(distances[from_node_index], Integer.MAX_VALUE);
			distances[from_node_index][from_node_index] = 0;

			// Reset the completed nodes array.
			Arrays.fill(completed_nodes, false);

			// Add the start node to the queue.
			queue.add(integers[from_node_index]);

			while (!(queue.isEmpty())) {

				if (this.canceled) {
					// The task was canceled
					this.distances = null;
					return this.distances;
				}

				index = ((Integer) queue.removeFirst()).intValue();
				if (completed_nodes[index]) {
					continue;
				}
				completed_nodes[index] = true;

				to_node = nodes[index];
				to_node_distance = distances[from_node_index][index];

				if (index < from_node_index) {
					// Oh boy. We've already got every distance from/to this
					// node.
					int distance_through_to_node;
					for (int i = 0; i < nodes.length; i++) {
						if (distances[index][i] == Integer.MAX_VALUE) {
							continue;
						}
						distance_through_to_node = to_node_distance + distances[index][i];
						if (distance_through_to_node <= distances[from_node_index][i]) {
							// Any immediate neighbor of a node that's already been
							// calculated for that does not already have a shorter path
							// calculated from from_node never will, and is thus complete.
							if (distances[index][i] == 1) {
								completed_nodes[i] = true;
							}
							distances[from_node_index][i] = distance_through_to_node;
						}
					} // End for every node, update the distance using the distance from to_node.
					// So now we don't need to put any neighbors on the queue or
					// anything, since they've already been taken care of by the previous calculation.
					continue;
				} // End if to_node has already had all of its distances calculated.

				// neighbors = network.neighborsList(to_node).iterator();
				neighbors = getNeighbors(to_node);

				for (CyNode neighbor : neighbors) {

					if (this.canceled) {
						this.distances = null;
						return this.distances;
					}

					neighbor_index = nodeIndexToMatrixIndexMap.get(neighbor.getSUID());

					// If this neighbor was not in the incoming List, we cannot include it in any paths.
					if (nodes[neighbor_index] == null) {
						distances[from_node_index][neighbor_index] = Integer.MAX_VALUE;
						continue;
					}

					if (completed_nodes[neighbor_index]) {
						// We've already done everything we can here.
						continue;
					}

					neighbor_distance = distances[from_node_index][neighbor_index];

					if ((to_node_distance != Integer.MAX_VALUE) && (neighbor_distance > (to_node_distance + 1))) {
						distances[from_node_index][neighbor_index] = (to_node_distance + 1);
						queue.addLast(integers[neighbor_index]);
					}
				} // For each of the next nodes' neighbors
			} // For each to_node, in order of their (present) distances

			this.currentProgress++;
			double percentDone = (this.currentProgress * 100) / (double) this.lengthOfTask;
			this.statusMessage = "Completed " + percentDone + "%.";
		} // For each from_node

		this.done = true;
		this.currentProgress = this.lengthOfTask; // why?
		return distances;
	}

	/**
	 * @return the <code>int[][]</code> 2D array of calculated distances or null
	 *         if not yet calculated
	 */
	public int[][] getDistances() {
		return this.distances;
	}

	private Collection<CyNode> getNeighbors(CyNode node) {
		final Set<CyNode> result = new HashSet<CyNode>();
		final Collection<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);

		if (edges == null || edges.size() == 0) return result;

		Long targetID = node.getSUID();

		for (CyEdge curEdge : edges) {
			if (curEdge.getSource().getSUID() != targetID) {
				result.add(curEdge.getSource());
				continue;
			}

			if (curEdge.getTarget().getSUID() != targetID) result.add(curEdge.getTarget());
		}

		return result;
	}

	class NodeDistancesTask {

		NodeDistancesTask() {
			calculate();
		}
	}
}
