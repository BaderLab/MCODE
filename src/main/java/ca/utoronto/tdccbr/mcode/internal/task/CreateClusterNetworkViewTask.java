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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;

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
	
	private MCODECluster cluster;
	private MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final MCODEResultsManager resultsMgr;
	private final CyServiceRegistrar registrar;
	
	private CyNetworkView newNetworkView;
	private boolean interrupted;
	
	private SpringEmbeddedLayouter layouter;
	
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
			int resultId,
			MCODEAlgorithm alg,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.cluster = cluster;
		this.id = resultId;
		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
		this.resultsMgr = resultsMgr;
		this.registrar = registrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Create Cluster Network");
		tm.setStatusMessage("Creating Network...");
		
		CyNetworkView view = null;
		
		if (cluster == null) // From commands
			cluster = resultsMgr.getCluster(id, rank);
		
		var params = mcodeUtil.getParameterManager().getResultParams(id);
		var res = resultsMgr.getResult(id);
		
		if (cluster == null || params == null || res == null) {
			throw new RuntimeException("Cannot find cluster with result id " + id + " and rank " + rank);
		} else {
			alg = mcodeUtil.getNetworkAlgorithm(params.getNetwork().getSUID());
			var currentView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			
			if (currentView != null && currentView.getModel().equals(res.getNetwork())) {
				view = currentView;
			} else {
				var viewSet = registrar.getService(CyNetworkViewManager.class).getNetworkViews(res.getNetwork());

				if (!viewSet.isEmpty())
					view = viewSet.iterator().next();
			}
		}
		
		var clusterNet = cluster.getNetwork();
		
		var nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		
		var title = id + ": " + cluster.getName() + " (Score: " + nf.format(cluster.getScore()) + ")";

		// Create the child network and view
		var newNet = mcodeUtil.createSubNetwork(clusterNet, res, SavePolicy.SESSION_FILE);
		newNet.getRow(newNet).set(CyNetwork.NAME, title);
		
		if (interrupted)
			return;
		
		tm.setStatusMessage("Creating View...");
		
		var vs = mcodeUtil.getNetworkViewStyle(view);
		
		if (vs == null)
			vs = registrar.getService(VisualMappingManager.class).getDefaultVisualStyle();
		
		newNetworkView = mcodeUtil.createNetworkView(newNet, vs);
	
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
		var clusterView = cluster.getView();
		
		for (var nv : newNetworkView.getNodeViews()) {
			if (interrupted)
				return;
			
			var node = nv.getModel();
			var cnv = clusterView != null ? clusterView.getNodeView(node) : null;
			
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
			tm.setStatusMessage("Applying Layout...");
			layouter = new SpringEmbeddedLayouter(newNetworkView);
			layouter.doLayout();
		}
	
		if (interrupted)
			return;
		
		newNetworkView.fitContent();
	}

	@Override
	public void cancel() {
		interrupted = true;
		
		if (layouter != null)
			layouter.cancel();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		if (type.equals(CyNetworkView.class))
			return newNetworkView;
		
		if (type == String.class)
			return (newNetworkView != null) ? "Created view: " + newNetworkView : "No views were created.";
				
		if (type == JSONResult.class) {
			var view = newNetworkView != null ? registrar.getService(CyJSONUtil.class).toJson(newNetworkView) : null;
			var json = "{ \"view\": " + view + " }";
			
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
