package ca.utoronto.tdccbr.mcode.internal.task;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel;

public class CreateClusterNetworkViewTask implements ObservableTask {
	
	@Tunable(
			description = "Result ID",
			longDescription = "The ID of the MCODE analysis result which contains the desired cluster.",
			required = true,
			exampleStringValue = "1",
			context = "nogui"
	)
	public int id;
	
	@Tunable(
			description = "Cluster Rank",
			longDescription = "The rank of the desired cluster.",
			required = true,
			exampleStringValue = "2",
			context = "nogui"
	)
	public int rank;
	
	private CyNetworkView networkView;
	private MCODECluster cluster;
	private MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final MCODEResultsManager resultsMgr;
	private final CyServiceRegistrar registrar;
	
	private CyNetworkView newNetworkView;
	private boolean interrupted;
	
	/**
	 * This constructor requires the tunable fields.
	 */
	public CreateClusterNetworkViewTask(
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.mcodeUtil = mcodeUtil;
		this.resultsMgr = resultsMgr;
		this.registrar = registrar;
	}
	
	public CreateClusterNetworkViewTask(
			MCODECluster cluster,
			CyNetworkView networkView,
			int resultId,
			MCODEAlgorithm alg,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.networkView = networkView;
		this.cluster = cluster;
		this.id = resultId;
		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
		this.resultsMgr = null;
		this.registrar = registrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (cluster == null && resultsMgr != null) {
			// From commands
			cluster = resultsMgr.getCluster(id, rank);
			MCODEParameters params = mcodeUtil.getParameterManager().getResultParams(id);
			MCODEResultsPanel resultsPanel = mcodeUtil.getResultPanel(id);
		
			if (cluster == null || params == null || resultsPanel == null) {
				throw new RuntimeException("Cannot find cluster with result id " + id + " and rank " + rank);
			} else {
				alg = mcodeUtil.getNetworkAlgorithm(params.getNetwork().getSUID());
				networkView = resultsPanel.getNetworkView();
			}
		}
		
		CyNetwork clusterNetwork = cluster.getNetwork();
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		
		String title = id + ": " + cluster.getName() + " (Score: " +
				 nf.format(cluster.getScore()) + ")";

		// Create the child network and view
		CySubNetwork newNetwork = mcodeUtil.createSubNetwork(clusterNetwork, clusterNetwork.getNodeList(),
				alg.getParams().getIncludeLoops(), SavePolicy.SESSION_FILE);
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, title);
		
		VisualStyle vs = mcodeUtil.getNetworkViewStyle(networkView);
		newNetworkView = mcodeUtil.createNetworkView(newNetwork, vs);
	
		newNetworkView.setVisualProperty(NETWORK_CENTER_X_LOCATION, 0.0);
		newNetworkView.setVisualProperty(NETWORK_CENTER_Y_LOCATION, 0.0);
	
		if (interrupted)
			return;
		
		mcodeUtil.displayNetworkView(newNetworkView);
	
		// Layout new cluster and fit it to window.
		// Randomize node positions before layout so that they don't all layout in a line
		// (so they don't fall into a local minimum for the SpringEmbedder)
		// If the SpringEmbedder implementation changes, this code may need to be removed
		boolean layoutNecessary = false;
		CyNetworkView clusterView = cluster.getView();
		
		for (View<CyNode> nv : newNetworkView.getNodeViews()) {
			if (interrupted)
				return;
			
			CyNode node = nv.getModel();
			View<CyNode> cnv = clusterView != null ? clusterView.getNodeView(node) : null;
			
			if (cnv != null) {
				// If it does, then we take the layout position that was already generated for it
				double x = cnv.getVisualProperty(NODE_X_LOCATION);
				double y = cnv.getVisualProperty(NODE_Y_LOCATION);
				nv.setVisualProperty(NODE_X_LOCATION, x);
				nv.setVisualProperty(NODE_Y_LOCATION, y);
			} else {
				// This will likely never occur.
				// Otherwise, randomize node positions before layout so that they don't all layout in a line
				// (so they don't fall into a local minimum for the SpringEmbedder).
				// If the SpringEmbedder implementation changes, this code may need to be removed.
				double w = newNetworkView.getVisualProperty(NETWORK_WIDTH);
				double h = newNetworkView.getVisualProperty(NETWORK_HEIGHT);
	
				nv.setVisualProperty(NODE_X_LOCATION, w * Math.random());
				// height is small for many default drawn graphs, thus +100
				nv.setVisualProperty(NODE_Y_LOCATION, (h + 100) * Math.random());
	
				layoutNecessary = true;
			}
		}

		if (layoutNecessary) {
			SpringEmbeddedLayouter lay = new SpringEmbeddedLayouter(newNetworkView);
			lay.doLayout(0, 0, 0, null);
		}
	
		if (interrupted)
			return;
		
		newNetworkView.fitContent();
		newNetworkView.updateView();
	}

	@Override
	public void cancel() {
		interrupted = true;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type.equals(CyNetworkView.class))
			return newNetworkView;
		
		if (type == String.class)
			return (newNetworkView != null) ? "Created view: " + newNetworkView : "No views were created.";
				
		if (type == JSONResult.class) {
			String view = newNetworkView != null ? registrar.getService(CyJSONUtil.class).toJson(newNetworkView) : null;
			String json = "{ \"view\": " + view + " }";
			
			JSONResult res = () -> { return json; };
			
			return res;
		}
		
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, CyNetworkView.class, JSONResult.class);
	}
}
