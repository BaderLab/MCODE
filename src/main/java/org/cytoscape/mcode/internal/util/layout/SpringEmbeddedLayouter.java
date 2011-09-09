package org.cytoscape.mcode.internal.util.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cytoscape.mcode.internal.view.MCODELoader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;

/**
 * An implementation of Kamada and Kawai's spring embedded layout algorithm.
 * Note 1: this was copied from giny.util because it is being phased out.  Eventually
 * the layout API will be available to use (TODO: remove layouter when layout API is available)
 * Note 2: this has been modified so that the doLayout method is interruptable and
 * reports progress to the MCODELoader
 *  */
public class SpringEmbeddedLayouter {

	public static final int DEFAULT_NUM_LAYOUT_PASSES = 2;
	public static final double DEFAULT_AVERAGE_ITERATIONS_PER_NODE = 20.0;

	public static final double[] DEFAULT_NODE_DISTANCE_SPRING_SCALARS = new double[] { 1.0, 1.0 };
	public static final double DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT = 15.0;
	public static final double DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT = 200.0;
	public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH = .05;
	public static final double DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH = 2500.0;

	public static final double[] DEFAULT_ANTICOLLISION_SPRING_SCALARS = new double[] { 0.0, 1.0 };
	public static final double DEFAULT_ANTICOLLISION_SPRING_STRENGTH = 100.0;

	protected int numLayoutPasses = DEFAULT_NUM_LAYOUT_PASSES;
	protected double averageIterationsPerNode = DEFAULT_AVERAGE_ITERATIONS_PER_NODE;

	protected double[] nodeDistanceSpringScalars = DEFAULT_NODE_DISTANCE_SPRING_SCALARS;
	protected double nodeDistanceStrengthConstant = DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT;
	protected double nodeDistanceRestLengthConstant = DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT;
	protected double disconnectedNodeDistanceSpringStrength = DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH;
	protected double disconnectedNodeDistanceSpringRestLength = DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH;
	protected double[][] nodeDistanceSpringStrengths;
	protected double[][] nodeDistanceSpringRestLengths;

	protected double[] anticollisionSpringScalars = DEFAULT_ANTICOLLISION_SPRING_SCALARS;
	protected double anticollisionSpringStrength = DEFAULT_ANTICOLLISION_SPRING_STRENGTH;

	protected CyNetworkView graphView;
	protected int nodeCount;
	protected int edgeCount;
	protected int layoutPass;

	//maps a root graph index to a distance matrix index
	protected Map<Integer, Integer> nodeIndexToMatrixIndexMap;
	//maps a distance matrix index to a root graph instance
	protected SortedMap<Integer, Integer> matrixIndexToNodeIndexMap;

	private boolean interrupted;

	public SpringEmbeddedLayouter() {
	}

	public SpringEmbeddedLayouter(CyNetworkView graph_view) {
		setGraphView(graph_view);
		initializeSpringEmbeddedLayouter();
	}

	public void setGraphView(CyNetworkView new_graph_view) {
		graphView = new_graph_view;
	}

	public View<CyNetwork> getGraphView() {
		return graphView;
	}

	protected void initializeSpringEmbeddedLayouter() {
		// Do nothing.
	}

	public void interruptDoLayout() {
		this.interrupted = true;
	}

	public void resetDoLayout() {
		this.interrupted = false;
	}

	/**
	 * Performs the layout of nodes.
	 *
	 * @param weightLayout Weighting of this process as calculated by MCODEUtil.convertNetworkToImage
	 * @param goalTotal Numerical aim as calculated by MCODEUtil.convertNetworkToImage based on number of processes required
	 * @param progress Amount of work completed in finding the cluster before this process started
	 * @param loader Loading animation which displays the progress of this process
	 * @return true if the layout was completed without interruption, false otherwise
	 */
	public boolean doLayout(double weightLayout, double goalTotal, double progress, MCODELoader loader) {
		// Initialize the layouting.
		nodeCount = graphView.getModel().getNodeCount();
		edgeCount = graphView.getModel().getEdgeCount();

		// Initialize the index map
		nodeIndexToMatrixIndexMap = new HashMap<Integer, Integer>();
		matrixIndexToNodeIndexMap = new TreeMap<Integer, Integer>();

		Iterator<CyNode> nodes = graphView.getModel().getNodeList().iterator();
		int count = 0;

		while (nodes.hasNext()) {
			CyNode n = nodes.next();
			nodeIndexToMatrixIndexMap.put(n.getIndex(), count);
			matrixIndexToNodeIndexMap.put(count, n.getIndex());
			count++;
		}

		// Stop if all nodes are closer together than this euclidean distance.
		// TODO: Why is this an appropriate threshold?
		double euclidean_distance_threshold = (0.5 * (nodeCount + edgeCount));
		int num_iterations = (int) ((nodeCount * averageIterationsPerNode) / numLayoutPasses);

		List<PartialDerivatives> partials_list = createPartialsList();
		PotentialEnergy potential_energy = new PotentialEnergy();
		PartialDerivatives partials;
		PartialDerivatives furthest_node_partials = null;

		for (layoutPass = 0; layoutPass < numLayoutPasses; layoutPass++) {
			setupForLayoutPass();

			// Initialize this layout pass
			potential_energy.reset();
			partials_list.clear();

			// Calculate all node distances.  Keep track of the furthest.
			for (View<CyNode> node_view : graphView.getNodeViews()) {
				partials = new PartialDerivatives(node_view);
				calculatePartials(partials, null, potential_energy, false);
				partials_list.add(partials);

				if ((furthest_node_partials == null) ||
					(partials.euclideanDistance > furthest_node_partials.euclideanDistance)) {
					furthest_node_partials = partials;
				}
			}

			// Until num_iterations, or the furthest node is not-so-far, move the
			// furthest node towards where it wants to be.
			for (int iterations_i = 0; ((iterations_i < num_iterations) && (furthest_node_partials.euclideanDistance >= euclidean_distance_threshold)); iterations_i++) {
				if (interrupted) {
					System.err.println("Interrupted: Layouter");
					// Before we shortcircuit the method, we reset the interruption so that the method can run without
					// problems for the next cluster
					resetDoLayout();
					return false;
				}

				furthest_node_partials = moveNode(furthest_node_partials, partials_list, potential_energy);

				progress += 100.0 * (((double) 1 / (double) (num_iterations * numLayoutPasses))) *
							((double) weightLayout / (double) goalTotal);

				if (loader != null) {
					loader.setProgress((int) progress, "Laying out");
				}

			} // End for each iteration, attempt to minimize the total potential
			// energy by moving the node that is furthest from where it should be.
		} // End for each layout pass

		// Just in case an interruption occured right before we exit the method, we reset it, such an interruption
		// will be dealt with in the MCODEUtil class
		resetDoLayout();

		return true;
	}

	/**
	 * Called at the beginning of each layoutPass iteration.
	 */
	protected void setupForLayoutPass() {
		setupNodeDistanceSprings();
	}

	protected void setupNodeDistanceSprings() {
		// We only have to do this once.
		if (layoutPass != 0) {
			return;
		}

		nodeDistanceSpringRestLengths = new double[nodeCount][nodeCount];
		nodeDistanceSpringStrengths = new double[nodeCount][nodeCount];

		if (nodeDistanceSpringScalars[layoutPass] == 0.0) {
			return;
		}

		//create a list of nodes that has the same indices as the nodeIndexToMatrixIndexMap
		List<CyNode> nodeList = new ArrayList<CyNode>();
		Collection<Integer> matrixIndices = matrixIndexToNodeIndexMap.values();
		int i = 0;

		for (int nodeIndex : matrixIndices) {
			nodeList.add(i, graphView.getModel().getNode(nodeIndex));
			i++;
		}

		NodeDistances ind = new NodeDistances(nodeList, graphView.getModel(), nodeIndexToMatrixIndexMap);
		int[][] node_distances = ind.calculate();

		if (node_distances == null) {
			return;
		}

		// TODO: A good strength_constant is the characteristic path length of the graph.
		// For now we'll just use nodeDistanceStrengthConstant.
		double node_distance_strength_constant = nodeDistanceStrengthConstant;

		// TODO: rest_length_constant can be chosen to scale the whole graph.
		// To make it the size of the current view, try
		// rest_length_constant = Math.sqrt( ( ( graphView.getViewRect().width / graphView.getViewRect.height() ) / 4 ) / graphView.getGraphDiameter() );
		// To make it bigger, try
		// rest_length_constant = graphView.averageEdgeLength();
		// To make it smaller, try
		// rest_length_constant = Math.sqrt( ( graphView.getViewRect().width * graphView.getViewRect.height() ) / graphView.getGraphDiameter() );
		// For now we'll just use nodeDistanceRestLengthConstant.
		double node_distance_rest_length_constant = nodeDistanceRestLengthConstant;

		// Calculate the rest lengths and strengths based on the node distance data
		for (int node_i = 0; node_i < nodeCount; node_i++) {
			for (int node_j = (node_i + 1); node_j < nodeCount; node_j++) {
				if (node_distances[node_i][node_j] == Integer.MAX_VALUE) {
					nodeDistanceSpringRestLengths[node_i][node_j] = disconnectedNodeDistanceSpringRestLength;
				} else {
					nodeDistanceSpringRestLengths[node_i][node_j] = (node_distance_rest_length_constant * node_distances[node_i][node_j]);
				}

				// Mirror over the diagonal.
				nodeDistanceSpringRestLengths[node_j][node_i] = nodeDistanceSpringRestLengths[node_i][node_j];

				if (node_distances[node_i][node_j] == Integer.MAX_VALUE) {
					nodeDistanceSpringStrengths[node_i][node_j] = disconnectedNodeDistanceSpringStrength;
				} else {
					nodeDistanceSpringStrengths[node_i][node_j] = (node_distance_strength_constant / (node_distances[node_i][node_j] * node_distances[node_i][node_j]));
				}

				// Mirror over the diagonal.
				nodeDistanceSpringStrengths[node_j][node_i] = nodeDistanceSpringStrengths[node_i][node_j];
			}

		} // currentProgress has been increased by ( nodeCount * nodeCount )
	}

	/**
	 * If partials_list is given, adjust all partials (bidirectional) for the
	 * current location of the given partials and return the new furthest node's
	 * partials.  Otherwise, just adjust the given partials (using the
	 * graphView's nodeViewsIterator), and return it.  If reversed is true then
	 * partials_list must be provided and all adjustments made by a non-reversed
	 * call (with the same partials with the same graphNodeView at the same location) will be undone.
	 * Complexity is O( #Nodes ).
	 */
	@SuppressWarnings("unchecked")
	protected PartialDerivatives calculatePartials(PartialDerivatives partials,
												   List<PartialDerivatives> partials_list,
												   PotentialEnergy potential_energy,
												   boolean reversed) {
		partials.reset();

		View<CyNode> nodeView = partials.getNodeView();

		int node_view_index = nodeIndexToMatrixIndexMap.get(nodeView.getModel().getIndex());
		double node_view_radius = nodeView.getVisualProperty(MinimalVisualLexicon.NODE_WIDTH);
		double node_view_x = nodeView.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION);
		double node_view_y = nodeView.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION);

		PartialDerivatives other_node_partials = null;
		View<CyNode> other_node_view = null;
		int other_node_view_index;
		double other_node_view_radius;

		PartialDerivatives furthest_partials = null;
		final Iterator<?> iterator;

		if (partials_list == null) {
			iterator = graphView.getNodeViews().iterator();
		} else {
			iterator = partials_list.iterator();
		}

		double delta_x;
		double delta_y;
		double euclidean_distance;
		double euclidean_distance_cubed;
		double distance_from_rest;
		double distance_from_touching;
		double incremental_change;

		while (iterator.hasNext()) {
			if (partials_list == null) {
				other_node_view = (View<CyNode>) iterator.next();
			} else {
				other_node_partials = (PartialDerivatives) iterator.next();
				other_node_view = other_node_partials.getNodeView();
			}

			if (nodeView.getModel().getIndex() == other_node_view.getModel().getIndex()) {
				continue;
			}

			other_node_view_index = nodeIndexToMatrixIndexMap.get(other_node_view.getModel().getIndex());
			other_node_view_radius = Math.max(other_node_view.getVisualProperty(MinimalVisualLexicon.NODE_WIDTH),
											  other_node_view.getVisualProperty(MinimalVisualLexicon.NODE_HEIGHT));

			delta_x = (node_view_x - other_node_view.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION));
			delta_y = (node_view_y - other_node_view.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION));

			euclidean_distance = Math.sqrt((delta_x * delta_x) + (delta_y * delta_y));
			euclidean_distance_cubed = Math.pow(euclidean_distance, 3);

			distance_from_touching = (euclidean_distance - (node_view_radius + other_node_view_radius));

			incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (delta_x - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * delta_x) / euclidean_distance))));

			if (!reversed) {
				partials.x += incremental_change;
			}

			if (other_node_partials != null) {
				incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[other_node_view_index][node_view_index] * (-delta_x - ((nodeDistanceSpringRestLengths[other_node_view_index][node_view_index] * -delta_x) / euclidean_distance))));
				if (reversed) {
					other_node_partials.x -= incremental_change;
				} else {
					other_node_partials.x += incremental_change;
				}
			}

			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (delta_x - (((node_view_radius + other_node_view_radius) * delta_x) / euclidean_distance))));

				if (!reversed) {
					partials.x += incremental_change;
				}

				if (other_node_partials != null) {
					incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (-delta_x - (((node_view_radius + other_node_view_radius) * -delta_x) / euclidean_distance))));

					if (reversed) {
						other_node_partials.x -= incremental_change;
						//System.out.println( "Other_Node_Partials (-): "+other_node_partials.x );
					} else {
						other_node_partials.x += incremental_change;
						//System.out.println( "Other_Node_Partials (+): "+other_node_partials.x );
					}
				}
			}
			incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (delta_y - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * delta_y) / euclidean_distance))));

			if (!reversed) {
				partials.y += incremental_change;
			}

			if (other_node_partials != null) {
				incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[other_node_view_index][node_view_index] * (-delta_y - ((nodeDistanceSpringRestLengths[other_node_view_index][node_view_index] * -delta_y) / euclidean_distance))));
				if (reversed) {
					other_node_partials.y -= incremental_change;
				} else {
					other_node_partials.y += incremental_change;
				}
			}

			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (delta_y - (((node_view_radius + other_node_view_radius) * delta_y) / euclidean_distance))));

				if (!reversed) {
					partials.y += incremental_change;
				}

				if (other_node_partials != null) {
					incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (-delta_y - (((node_view_radius + other_node_view_radius) * -delta_y) / euclidean_distance))));
					if (reversed) {
						other_node_partials.y -= incremental_change;
					} else {
						other_node_partials.y += incremental_change;
					}
				}
			}

			incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (1.0 - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_y * delta_y)) / euclidean_distance_cubed))));

			if (reversed) {
				if (other_node_partials != null) {
					other_node_partials.xx -= incremental_change;
				}
			} else {
				partials.xx += incremental_change;

				if (other_node_partials != null) {
					other_node_partials.xx += incremental_change;
				}
			}
			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (1.0 - (((node_view_radius + other_node_view_radius) * (delta_y * delta_y)) / euclidean_distance_cubed))));

				if (reversed) {
					if (other_node_partials != null) {
						other_node_partials.xx -= incremental_change;
					}
				} else {
					partials.xx += incremental_change;
					if (other_node_partials != null) {
						other_node_partials.xx += incremental_change;
					}
				}
			}

			incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (1.0 - ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_x * delta_x)) / euclidean_distance_cubed))));

			if (reversed) {
				if (other_node_partials != null) {
					other_node_partials.yy -= incremental_change;
				}
			} else {
				partials.yy += incremental_change;
				if (other_node_partials != null) {
					other_node_partials.yy += incremental_change;
				}
			}

			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (1.0 - (((node_view_radius + other_node_view_radius) * (delta_x * delta_x)) / euclidean_distance_cubed))));
				if (reversed) {
					if (other_node_partials != null) {
						other_node_partials.yy -= incremental_change;
					}
				} else {
					partials.yy += incremental_change;
					if (other_node_partials != null) {
						other_node_partials.yy += incremental_change;
					}
				}
			}

			incremental_change = (nodeDistanceSpringScalars[layoutPass] * (nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * ((nodeDistanceSpringRestLengths[node_view_index][other_node_view_index] * (delta_x * delta_y)) / euclidean_distance_cubed)));

			if (reversed) {
				if (other_node_partials != null) {
					other_node_partials.xy -= incremental_change;
				}
			} else {
				partials.xy += incremental_change;
				if (other_node_partials != null) {
					other_node_partials.xy += incremental_change;
				}
			}

			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * (anticollisionSpringStrength * (((node_view_radius + other_node_view_radius) * (delta_x * delta_y)) / euclidean_distance_cubed)));

				if (reversed) {
					if (other_node_partials != null) {
						other_node_partials.xy -= incremental_change;
					}
				} else {
					partials.xy += incremental_change;
					if (other_node_partials != null) {
						other_node_partials.xy += incremental_change;
					}
				}
			}

			distance_from_rest = (euclidean_distance - nodeDistanceSpringRestLengths[node_view_index][other_node_view_index]);
			incremental_change = (nodeDistanceSpringScalars[layoutPass] * ((nodeDistanceSpringStrengths[node_view_index][other_node_view_index] * (distance_from_rest * distance_from_rest)) / 2));

			//System.out.println( "Distance_From_Rest: "+distance_from_rest+" Incremental_Change: "+incremental_change );

			if (reversed) {
				if (other_node_partials != null) {
					potential_energy.totalEnergy -= incremental_change;
				}
			} else {
				potential_energy.totalEnergy += incremental_change;

				if (other_node_partials != null) {
					potential_energy.totalEnergy += incremental_change;
				}
			}

			if (distance_from_touching < 0.0) {
				incremental_change = (anticollisionSpringScalars[layoutPass] * ((anticollisionSpringStrength * (distance_from_touching * distance_from_touching)) / 2));
				if (reversed) {
					if (other_node_partials != null) {
						potential_energy.totalEnergy -= incremental_change;
					}
				} else {
					potential_energy.totalEnergy += incremental_change;
					if (other_node_partials != null) {
						potential_energy.totalEnergy += incremental_change;
					}
				}
			}
			if (other_node_partials != null) {
				other_node_partials.euclideanDistance = Math.sqrt((other_node_partials.x * other_node_partials.x) +
																  (other_node_partials.y * other_node_partials.y));
				if ((furthest_partials == null) ||
					(other_node_partials.euclideanDistance > furthest_partials.euclideanDistance)) {
					furthest_partials = other_node_partials;
				}
			}

		}

		if (!reversed) {
			partials.euclideanDistance = Math.sqrt((partials.x * partials.x) + (partials.y * partials.y));
		}

		if ((furthest_partials == null) || (partials.euclideanDistance > furthest_partials.euclideanDistance)) {
			furthest_partials = partials;
		}

		return furthest_partials;
	}

	/**
	 * Move the node with the given partials and adjust all partials in the given
	 * List to reflect that move, and adjust the potential energy too.
	 *
	 * @return the PartialDerivatives of the furthest node after the move.
	 */
	protected PartialDerivatives moveNode(PartialDerivatives partials,
										  List<PartialDerivatives> partials_list,
										  PotentialEnergy potential_energy) {
		PartialDerivatives starting_partials = new PartialDerivatives(partials);
		calculatePartials(partials, partials_list, potential_energy, true);
		simpleMoveNode(starting_partials);

		return calculatePartials(partials, partials_list, potential_energy, false);
	}

	protected void simpleMoveNode(PartialDerivatives partials) {
		View<CyNode> node_view = partials.getNodeView();
		double denomenator = ((partials.xx * partials.yy) - (partials.xy * partials.xy));
		double delta_x = (((-partials.x * partials.yy) - (-partials.y * partials.xy)) / denomenator);
		double delta_y = (((-partials.y * partials.xx) - (-partials.x * partials.xy)) / denomenator);

		// figure out movement
		double x = node_view.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION);
		double y = node_view.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION);
		
		node_view.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION, x + delta_x);
		node_view.setVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION, y + delta_y);

		// TODO
//		Point2D p = node_view.getOffset();
//		node_view.setOffset(p.getX() + delta_x, p.getY() + delta_y);
	}

	protected List<PartialDerivatives> createPartialsList() {
		return new ArrayList<PartialDerivatives>();
	}

	static class PartialDerivatives {

		protected View<CyNode> nodeView;
		public double x;
		public double y;
		public double xx;
		public double yy;
		public double xy;
		public double euclideanDistance;

		public PartialDerivatives(View<CyNode> nodeView) {
			this.nodeView = nodeView;
		}

		public PartialDerivatives(PartialDerivatives copy_from) {
			this.nodeView = copy_from.getNodeView();
			copyFrom(copy_from);
		}

		public View<CyNode> getNodeView() {
			return nodeView;
		}

		public void copyFrom(PartialDerivatives other_partial_derivatives) {
			x = other_partial_derivatives.x;
			y = other_partial_derivatives.y;
			xx = other_partial_derivatives.xx;
			yy = other_partial_derivatives.yy;
			xy = other_partial_derivatives.xy;
			euclideanDistance = other_partial_derivatives.euclideanDistance;
		}

		public void reset() {
			x = 0.0;
			y = 0.0;
			xx = 0.0;
			yy = 0.0;
			xy = 0.0;
			euclideanDistance = 0.0;
		}

		@Override
		public String toString() {
			return "PartialDerivatives( \"" + getNodeView() + "\", x=" + x + ", y=" + y + ", xx=" + xx + ", yy=" + yy +
				   ", xy=" + xy + ", euclideanDistance=" + euclideanDistance + " )";
		}
	}

	static class PotentialEnergy {

		public double totalEnergy = 0.0;

		public void reset() {
			totalEnergy = 0.0;
		}
	}

}
