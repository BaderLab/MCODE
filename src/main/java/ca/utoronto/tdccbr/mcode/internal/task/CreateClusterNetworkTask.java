package ca.utoronto.tdccbr.mcode.internal.task;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.text.NumberFormat;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;

public class CreateClusterNetworkTask implements ObservableTask {

	private final CyNetworkView networkView;
	private final MCODECluster cluster;
	private final MCODEAlgorithm alg;
	private final MCODEUtil mcodeUtil;
	private final int resultId;
	
	private CyNetworkView newNetworkView;
	private boolean interrupted;
	
	public CreateClusterNetworkTask(
			final MCODECluster cluster,
			final CyNetworkView networkView,
			final int resultId,
			final MCODEAlgorithm alg,
			final MCODEUtil mcodeUtil
	) {
		this.networkView = networkView;
		this.cluster = cluster;
		this.resultId = resultId;
		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final CyNetwork clusterNetwork = cluster.getNetwork();
		
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		
		String title = resultId + ": " + cluster.getName() + " (Score: " +
				 nf.format(cluster.getScore()) + ")";

		// Create the child network and view
		CySubNetwork newNetwork = mcodeUtil.createSubNetwork(clusterNetwork, clusterNetwork.getNodeList(),
				alg.getParams().isIncludeLoops(), SavePolicy.SESSION_FILE);
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
		return newNetworkView;
	}
}
