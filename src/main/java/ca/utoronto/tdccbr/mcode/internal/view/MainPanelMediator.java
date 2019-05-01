package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.invokeOnEDT;
import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.invokeOnEDTAndWait;
import static org.cytoscape.util.swing.LookAndFeelUtil.isMac;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.action.AnalysisAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.task.CreateClusterNetworkViewTask;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel.ClusterBrowser;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel.ExploreContentPanel;

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
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 7:00:13 PM
 * * Description: Utilities for MCODE
 */

public class MainPanelMediator implements NetworkAboutToBeDestroyedListener, SetCurrentNetworkListener {
	
	private static final String SCORE_ATTR = "MCODE_Score";
	private static final String NODE_STATUS_ATTR = "MCODE_Node_Status";
	private static final String CLUSTER_ATTR = "MCODE_Cluster";
	
	private UpdateParentNetworkTask updateNetTask;
	private boolean ignoreResultSelected;
	private boolean firstTime = true;
	
	private MCODEMainPanel mainPanel;
	private NewAnalysisPanel newAnalysisPanel;
	private JDialog newAnalysisDialog;
	private AboutDialog aboutDialog;
	
	private final AnalysisAction analysisAction;
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	private final Object lock = new Object();
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);
	
	public MainPanelMediator(
			AnalysisAction analysisAction,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.analysisAction = analysisAction;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
		
		resultsMgr.addPropertyChangeListener("resultAdded", evt -> {
			invokeOnEDT(() -> {
				MCODEResult res = (MCODEResult) evt.getNewValue();
				
				if (res != null) {
					addResult(res);
					disposeNewAnalysisDialog();
				}
			});
		});
		resultsMgr.addPropertyChangeListener("resultRemoved", evt -> {
			invokeOnEDT(() -> {
				MCODEResult res = (MCODEResult) evt.getNewValue();
				
				if (res != null) {
					getMainPanel().removeResult(res);
					
					if (getMainPanel().getResultsCount() == 0) {
						// TODO Move out from here
						// Reset the results cache
						resultsMgr.reset();
						mcodeUtil.reset();
					}
				}
			});
		});
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent evt) {
		if (isMainPanelOpen()) {
			CyNetwork network = evt.getNetwork();
			Set<Integer> resultIds = resultsMgr.getNetworkResults(network.getSUID());

			for (int id : resultIds)
				resultsMgr.removeResult(id);
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent evt) {
		if (isMainPanelOpen())
			getMainPanel().updateNewAnalysisButton();
	}

	private void onResultSelected() {
		synchronized (lock) {
			if (ignoreResultSelected)
				return;
			
			getMainPanel().update();
			
			if (updateNetTask != null) {
				updateNetTask.cancel();
				updateNetTask = null;
			}
		
			MCODEResult res = getMainPanel().getSelectedResult();
			
			if (res != null) {
				updateNetTask = new UpdateParentNetworkTask(res, getMainPanel().getSelectedCluster());
				registrar.getService(DialogTaskManager.class).execute(new TaskIterator(updateNetTask));
			}
		}
	}
	
	private void addResult(MCODEResult res) {
		List<MCODECluster> clusters = res != null ? res.getClusters() : null;
		
		if (clusters == null || clusters.isEmpty())
			return;
		
		ignoreResultSelected = true;
		
		try {
			CytoPanel cytoPanel = getControlPanel();
			int index = cytoPanel.indexOfComponent(getMainPanel());
			
			if (index >= 0)
				cytoPanel.setSelectedIndex(index);
			
			getMainPanel().addResult(res);
		} finally {
			ignoreResultSelected = false;
		}
		
		CyNetwork network = res.getNetwork();
		CyNetworkView currView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		CyNetworkView netView = currView != null && currView.getModel().equals(network) ? currView : null;

		if (netView == null) {
			Collection<CyNetworkView> allViews = registrar.getService(CyNetworkViewManager.class)
					.getNetworkViews(network);

			if (allViews.isEmpty())
				netView = allViews.iterator().next();
		}
		
		// Add required listeners to results components
		ClusterBrowser clusterBrowser = getMainPanel().getClusterBrowser(res);
		List<ClusterPanel> clusterPanels = clusterBrowser != null ? clusterBrowser.getAllItems()
				: Collections.emptyList();
		
		for (ClusterPanel p : clusterPanels)
			p.getSizeSlider().addChangeListener(new SizeAction(p));
		
		// Create all the images for the clusters
		int rank = 0;
		
		for (MCODECluster c : clusters) {
			c.setRank(++rank);
			
			if (!c.isTooLargeToVisualize())
				createClusterImage(c, true);
		}
	}

	public MCODEMainPanel getMainPanel() {
		if (mainPanel == null) {
			invokeOnEDTAndWait(() -> {
				mainPanel = new MCODEMainPanel(mcodeUtil, registrar);
				mainPanel.getResultsCombo().addItemListener(evt -> onResultSelected());
				mainPanel.getNewAnalysisButton().addActionListener(evt -> showNewAnalysisDialog());
				mainPanel.getDiscardButton().addActionListener(evt -> discardSelectedResult());
				mainPanel.getOptionsButton().addActionListener(evt -> getOptionsMenu()
						.show(mainPanel.getOptionsButton(), 0, mainPanel.getOptionsButton().getHeight()));
				mainPanel.getAboutButton().addActionListener(evt -> showAboutDialog());
				mainPanel.getClosePanelButton().addActionListener(evt -> closeMainPanel());
			});
		}
		
		return mainPanel;
	}
	
	private NewAnalysisPanel getNewAnalysisPanel() {
		if (newAnalysisPanel == null) {
			invokeOnEDTAndWait(() -> newAnalysisPanel = new NewAnalysisPanel(mcodeUtil));
		}
		
		return newAnalysisPanel;
	}
	
	public void disposeNewAnalysisDialog() {
		if (newAnalysisDialog != null && newAnalysisDialog.isVisible()) {
			newAnalysisDialog.dispose();
			newAnalysisDialog = null;
		}
	}
	
	public void showMainPanel() {
		invokeOnEDT(() -> {
			CytoPanelComponent panel = null;
			
			try {
				panel = registrar.getService(CytoPanelComponent.class, "(id=" + MCODEMainPanel.ID + ")");
			} catch (Exception ex) {
			}
			
			if (panel == null) {
				panel = getMainPanel();
				
				Properties props = new Properties();
				props.setProperty("id", MCODEMainPanel.ID);
				registrar.registerService(panel, CytoPanelComponent.class, props);
				
				if (firstTime && getMainPanel().getResultsCount() == 0
						&& registrar.getService(CyApplicationManager.class).getCurrentNetwork() != null) {
					firstTime = false;
					showNewAnalysisDialog();
					analysisAction.updateEnableState();
				}
			}
			
			selectMainPanel();
		});
	}
	
	public void showAboutDialog() {
		invokeOnEDT(() -> {
			if (aboutDialog == null) {
				aboutDialog = new AboutDialog(SwingUtilities.getWindowAncestor(getMainPanel()), mcodeUtil, registrar);
				
				if (isMac()) // Workaround for bug: https://bugs.openjdk.java.net/browse/JDK-8182638
					aboutDialog.addWindowListener(new WindowAdapter() {
						@Override
						public void windowActivated(WindowEvent evt) {
							evt.getWindow().toFront();
						}
					});
			}
	
			if (!aboutDialog.isVisible()) {
				aboutDialog.setLocationRelativeTo(null);
				aboutDialog.setVisible(true);
			}
		});
	}
	
	public void closeMainPanel() {
		boolean confirmed = true;
		
		if (!resultsMgr.getAllResults().isEmpty())
			confirmed = discardAllResults(true);
		
		if (confirmed && isMainPanelOpen())
			registrar.unregisterService(getMainPanel(), CytoPanelComponent.class);
	}
	
	@SuppressWarnings("serial")
	public void showNewAnalysisDialog() {
		invokeOnEDT(() -> {
			if (newAnalysisDialog == null) {
				Window owner = SwingUtilities.getWindowAncestor(getControlPanel().getThisComponent());
				
				newAnalysisDialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
				newAnalysisDialog.setTitle("MCODE");
				newAnalysisDialog.setResizable(false);
				newAnalysisDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				JButton okBtn = new JButton(analysisAction);
				JButton cancelBtn = new JButton(new AbstractAction("Close") {
					@Override
					public void actionPerformed(ActionEvent evt) {
						disposeNewAnalysisDialog();
					}
				});
				
				makeSmall(okBtn, cancelBtn);
				
				JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
				buttonPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)
				));
				
				newAnalysisDialog.getContentPane().add(getNewAnalysisPanel(), BorderLayout.CENTER);
				newAnalysisDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
				
				LookAndFeelUtil.setDefaultOkCancelKeyStrokes(newAnalysisDialog.getRootPane(), analysisAction,
						cancelBtn.getAction());
				newAnalysisDialog.getRootPane().setDefaultButton(okBtn);
				
				if (isMac()) // Workaround for bug: https://bugs.openjdk.java.net/browse/JDK-8182638
					newAnalysisDialog.addWindowListener(new WindowAdapter() {
						@Override
						public void windowActivated(WindowEvent evt) {
							evt.getWindow().toFront();
						}
					});
			}
			
			newAnalysisDialog.pack();
			newAnalysisDialog.setLocationRelativeTo(newAnalysisDialog.getParent());
			newAnalysisDialog.setVisible(true);
		});
	}
	
	/**
	 * @return true if the Main Panel is open and false otherwise
	 */
	public boolean isMainPanelOpen() {
		CytoPanel cytoPanel = getControlPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			final Component comp = cytoPanel.getComponentAt(i);
			
			if (comp instanceof MCODEMainPanel)
				return true;
		}
		
		return false;
	}

	public CytoPanel getControlPanel() {
		return registrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.WEST);
	}
	
	public void selectMainPanel() {
		invokeOnEDT(() -> {
			if (mainPanel != null) {
				CytoPanel cytoPanel = getControlPanel();
				int index = cytoPanel.indexOfComponent(mainPanel);
				
				if (index >= 0)
					cytoPanel.setSelectedIndex(index);
			}
		});
	}
	
	public boolean discardAllResults(boolean requestUserConfirmation) {
		Integer confirmed = JOptionPane.YES_OPTION;
		
		if (requestUserConfirmation) {
			// Must make sure the user wants to close this results panel
			String message = "You are about to dispose of all Results.\nDo you wish to continue?";
			confirmed = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getMainPanel()),
													 new Object[] { message },
													 "MCODE - Confirm",
													 JOptionPane.YES_NO_OPTION,
													 JOptionPane.QUESTION_MESSAGE,
													 null,
													 null,
													 null);
		}

		if (confirmed == JOptionPane.YES_OPTION) {
			for (MCODEResult res : resultsMgr.getAllResults())
				discardResult(res.getId(), false);
			
			return true;
		}
		
		return false;
	}
	
	private void discardSelectedResult() {
		MCODEResult res = getMainPanel().getSelectedResult();
		
		if (res != null)
			discardResult(res.getId(), true);
	}
	
	private void discardResult(int resultId, boolean requestUserConfirmation) {
		Integer confirmed = JOptionPane.YES_OPTION;
		
		if (requestUserConfirmation) {
			// Must make sure the user wants to close this results panel
			String message = "You are about to dispose of Result " + resultId + ".\nDo you wish to continue?";
			confirmed = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getMainPanel()),
													 new Object[] { message },
													 "MCODE - Confirm",
													 JOptionPane.YES_NO_OPTION,
													 JOptionPane.QUESTION_MESSAGE,
													 null,
													 null,
													 null);
		}

		if (confirmed == JOptionPane.YES_OPTION)
			resultsMgr.removeResult(resultId);
	}
	
	private JPopupMenu getOptionsMenu() {
		JPopupMenu menu = new JPopupMenu();
		MCODEResult res = getMainPanel().getSelectedResult();
		MCODECluster cluster = getMainPanel().getSelectedCluster();
		CyNetwork currentNet = registrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		{
			JMenuItem mi = new JMenuItem("View Source Network");
			mi.setToolTipText(res != null ? "Source Network: " + MCODEUtil.getName(res.getNetwork()) : null);
			mi.setIcon(new TextIcon(
					IconManager.ICON_SHARE_ALT, registrar.getService(IconManager.class).getIconFont(14), 16, 16));
			mi.addActionListener(evt -> {
				registrar.getService(CyApplicationManager.class).setCurrentNetwork(res.getNetwork());
			});
			mi.setEnabled(res != null && !res.getNetwork().equals(currentNet));
			menu.add(mi);
		}
		menu.addSeparator();
		{
			JMenuItem mi = new JMenuItem("Create Cluster Network");
			mi.setToolTipText("Create Network from Selected Cluster");
			mi.setIcon(new TextIcon(
					IconManager.ICON_PLUS_CIRCLE, registrar.getService(IconManager.class).getIconFont(14), 16, 16));
			mi.addActionListener(evt -> {
				MCODEAlgorithm alg = mcodeUtil.getNetworkAlgorithm(res.getNetwork().getSUID());

				CreateClusterNetworkViewTask task = new CreateClusterNetworkViewTask(cluster, res.getId(), alg,
						resultsMgr, mcodeUtil, registrar);
				registrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
			});
			mi.setEnabled(res != null && cluster != null);
			menu.add(mi);
		}
		{
			JMenuItem mi = new JMenuItem("Export Result...");
			mi.setToolTipText("Export Selected Result to File");
			mi.setIcon(new TextIcon(
					IconManager.ICON_SHARE_SQUARE_O, registrar.getService(IconManager.class).getIconFont(13), 16, 16));
			mi.addActionListener(evt -> {
				MCODEAlgorithm alg = mcodeUtil.getNetworkAlgorithm(res.getNetwork().getSUID());
				mcodeUtil.exportMCODEResults(alg, res.getClusters(), res.getNetwork());
			});
			mi.setEnabled(res != null);
			menu.add(mi);
		}
		menu.addSeparator();
		{
			JMenuItem mi = new JCheckBoxMenuItem("Show Analysis Parameters");
			mi.setToolTipText("Show Analysis Parameters");
			mi.setIcon(new TextIcon(
					IconManager.ICON_INFO_CIRCLE, registrar.getService(IconManager.class).getIconFont(14), 16, 16));
			mi.addActionListener(evt -> getMainPanel().getInfoPanel().setVisible(mi.isSelected()));
			mi.setSelected(getMainPanel().getInfoPanel().isVisible());
			mi.setEnabled(res != null);
			menu.add(mi);
		}
		menu.addSeparator();
		{
			JMenuItem mi = new JMenuItem("Discard All");
			mi.setToolTipText("Discard All Results");
			mi.setIcon(new TextIcon(
					IconManager.ICON_TRASH_O, registrar.getService(IconManager.class).getIconFont(14), 16, 16));
			mi.addActionListener(evt -> discardAllResults(true));
			mi.setEnabled(res != null);
			menu.add(mi);
		}
		
		return menu;
	}
	
	/**
	 * Convert a network to an image and set it to the passed cluster.
	 * 
	 * @param cluster Input network to convert to an image
	 * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
	 */
	private void createClusterImage(MCODECluster cluster, boolean layoutNecessary) {
		try {
			final CyNetwork net = cluster.getNetwork();
			final VisualStyle vs = mcodeUtil.getClusterStyle();
			final CyNetworkView clusterView = mcodeUtil.createNetworkView(net, vs);
	
			int width = ClusterPanel.GRAPH_IMG_SIZE;
			int height = ClusterPanel.GRAPH_IMG_SIZE;
			
			clusterView.setVisualProperty(NETWORK_WIDTH, new Double(width));
			clusterView.setVisualProperty(NETWORK_HEIGHT, new Double(height));
	
			for (View<CyNode> nv : clusterView.getNodeViews()) {
				// Node position
				final double x;
				final double y;
	
				// First we check if the MCODECluster already has a node view of this node (posing the more generic condition
				// first prevents the program from throwing a null pointer exception in the second condition)
				if (cluster.getView() != null && cluster.getView().getNodeView(nv.getModel()) != null) {
					// If it does, then we take the layout position that was already generated for it
					x = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_X_LOCATION);
					y = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_Y_LOCATION);
				} else {
					// Otherwise, randomize node positions before layout so that they don't all layout in a line
					// (so they don't fall into a local minimum for the SpringEmbedder)
					// If the SpringEmbedder implementation changes, this code may need to be removed
					// size is small for many default drawn graphs, thus +100
					x = (clusterView.getVisualProperty(NETWORK_WIDTH) + 100) * Math.random();
					y = (clusterView.getVisualProperty(NETWORK_HEIGHT) + 100) * Math.random();
					layoutNecessary = true;
				}
	
				nv.setVisualProperty(NODE_X_LOCATION, x);
				nv.setVisualProperty(NODE_Y_LOCATION, y);
	
				// Node shape
				if (cluster.getSeedNode() == nv.getModel().getSUID())
					nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
				else
					nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
			}
	
			if (layoutNecessary) {
				SpringEmbeddedLayouter layouter = new SpringEmbeddedLayouter(clusterView);
				layouter.doLayout();
			}
			
			final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			final Dimension size = new Dimension(width, height);
	
			invokeOnEDT(() -> {
				try {
					JPanel panel = new JPanel();
					panel.setPreferredSize(size);
					panel.setSize(size);
					panel.setMinimumSize(size);
					panel.setMaximumSize(size);
					panel.setBackground((Color) vs.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		
					JWindow window = new JWindow();
					window.getContentPane().add(panel, BorderLayout.CENTER);
		
					RenderingEngine<CyNetwork> re = mcodeUtil.createRenderingEngine(panel, clusterView);
		
					vs.apply(clusterView);
					
					clusterView.fitContent();
					window.pack();
					window.repaint();
		
					re.createImage(width, height);
					re.printCanvas(g);
					g.dispose();
					
					if (!clusterView.getNodeViews().isEmpty())
						cluster.setView(clusterView);
					
					cluster.setImage(image);
				} catch (Exception ex) {
					logger.error("Cannot update cluster image.", ex);
				}
			});
		} catch (Exception ex) {
			logger.error("Cannot create cluster image.", ex);
		}
	}
	
	/**
	 * Handles the dynamic cluster size manipulation via the JSlider
	 */
	private class SizeAction implements ChangeListener {

		private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(3);
		private ScheduledFuture<?> futureLoader;
		private final ClusterPanel clusterPanel;
		
		SizeAction(ClusterPanel clusterPanel) {
			this.clusterPanel = clusterPanel;
		}

		@Override
		public void stateChanged(ChangeEvent evt) {
			JSlider source = (JSlider) evt.getSource();

			if (source.getValueIsAdjusting())
				return;
			
			// This method as been written so that the slider is responsive to the user's input at all times, despite
			// the computationally challenging drawing, layout out, and selection of large clusters. As such, we only
			// perform real work if the slider produces a different cluster, and furthermore we only perform the quick
			// processes here, while the heavy weights are handled by the drawer's thread.
			final double nodeScoreCutoff = (((double) source.getValue()) / 1000);
			final int index = clusterPanel.getIndex();
			// Store current cluster content for comparison
			final MCODECluster oldCluster = clusterPanel.getCluster();
			
			if (futureLoader != null && !futureLoader.isDone()) {
				futureLoader.cancel(true);
				
				if (!oldCluster.equals(getMainPanel().getCluster(index)))
					oldCluster.dispose();
			}
			
			final MCODEResult res = getMainPanel().getSelectedResult();
			
			if (res == null)
				return;
			
			final CyNetwork network = res.getNetwork();
			final MCODEAlgorithm alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
			
			final Runnable command = (() -> {
				final List<Long> oldNodes = oldCluster.getNodes();
				// Find the new cluster given the node score cutoff
				final MCODECluster newCluster = alg.exploreCluster(oldCluster, nodeScoreCutoff, network, res.getId());
				// We only want to do the following work if the newly found cluster is actually different
				// So we get the new cluster content
				List<Long> newNodes = newCluster.getNodes();

				// And compare the old and new
				if (!newNodes.equals(oldNodes)) {
					// We want to set the loading icon
					oldCluster.setImage(null);
					
					// If the cluster has changed, then we conduct all non-rate-limiting steps...
					// Update the cluster array
					res.replaceCluster(index, newCluster);
					// Update the cluster details
					clusterPanel.setCluster(newCluster);
					
					// Update the node attributes table
					ExploreContentPanel explorePanel = mainPanel.getExploreContentPanel(index);
					
					if (explorePanel != null)
						explorePanel.updateEnumerationsTable(index);
					
					// Draw Graph and select the cluster in the view in a separate thread so that it can be
					// interrupted by the slider movement
					if (!newCluster.isDisposed()) {
						// There is a small difference between expanding and retracting the cluster size.
						// When expanding, new nodes need random position and thus must go through the layout.
						// When retracting, we simply use the layout that was generated and stored.
						// This speeds up the drawing process greatly.
						boolean layoutNecessary = newNodes.size() > oldNodes.size();
						oldCluster.dispose();
						
						// Graph drawing will only occur if the cluster is not too large,
						// otherwise a place holder will be drawn
						if (!newCluster.isTooLargeToVisualize())
							createClusterImage(newCluster, layoutNecessary);
					}
				}
	        });
	        
	        futureLoader = scheduler.schedule(command, 100, TimeUnit.MILLISECONDS);
		}
	}
	
	private class UpdateParentNetworkTask extends AbstractTask {
		
		private final MCODEResult result;
		private final MCODECluster cluster;

		public UpdateParentNetworkTask(MCODEResult result, MCODECluster cluster) {
			this.result = result;
			this.cluster = cluster;
		}
		
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run(TaskMonitor tm) throws Exception {
			tm.setTitle("Update MCODE Attributes");
			tm.setStatusMessage("Updating Node Table values...");
			tm.setProgress(0.0);
			
			// To re-initialize the calculators we need the highest score of this particular result set
			double maxScore = setNodeAttributesAndGetMaxScore();
			tm.setProgress(5.0);
			
			if (cancelled)
				return;
			
			// Get the updated app's style
			VisualStyle appStyle = mcodeUtil.getAppStyle(maxScore);
			
			if (cancelled)
				return;
			
			// Register the app's style but don't make it active by default
			mcodeUtil.registerVisualStyle(appStyle);
			tm.setProgress(7.0);
			
			if (cancelled)
				return;
			
			// Select nodes and edges that also exist in the selected cluster
			tm.setStatusMessage("Selecting nodes and edges from selected cluster...");
			
			if (cluster != null) {
				List elements = new ArrayList<>();
				elements.addAll(cluster.getGraph().getEdgeList());
				elements.addAll(cluster.getGraph().getNodeList());
				
				if (cancelled)
					return;
				
				mcodeUtil.setSelected(elements, result.getNetwork());
			}
			
			tm.setProgress(1.0);
		}
		
		/**
		 * Sets the network node attributes to the current result set's scores and clusters.
		 * @return the maximal score in the network given the parameters that were used for scoring at the time
		 */
		private double setNodeAttributesAndGetMaxScore() {
			int resultId = result.getId();
			CyNetwork network = result.getNetwork();
			List<MCODECluster> clusters = result.getClusters();
			MCODEAlgorithm alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
			
			for (CyNode n : network.getNodeList()) {
				if (cancelled)
					return 0;
				
				Long nId = n.getSUID();
				CyTable nodeTbl = network.getDefaultNodeTable();
				
				try {
					if (nodeTbl.getColumn(CLUSTER_ATTR) == null)
						nodeTbl.createListColumn(CLUSTER_ATTR, String.class, false);
				} catch (IllegalArgumentException e) {
					logger.error("MCODE", e);
				}
				try {
					if (nodeTbl.getColumn(NODE_STATUS_ATTR) == null)
						nodeTbl.createColumn(NODE_STATUS_ATTR, String.class, false);
				} catch (IllegalArgumentException e) {
					logger.error("MCODE", e);
				}
				try {
					if (nodeTbl.getColumn(SCORE_ATTR) == null)
						nodeTbl.createColumn(SCORE_ATTR, Double.class, false);
				} catch (IllegalArgumentException e) {
					logger.error("MCODE", e);
				}

				CyRow nodeRow = network.getRow(n);
				
				if (cancelled)
					return 0;
				
				nodeRow.set(NODE_STATUS_ATTR, "Unclustered");
				
				if (cancelled)
					return 0;
				
				nodeRow.set(SCORE_ATTR, alg.getNodeScore(n.getSUID(), resultId));
	
				for (MCODECluster cluster : clusters) {
					if (cancelled)
						return 0;
					
					if (cluster.getNodes().contains(nId)) {
						Set<String> clusterNameSet = new LinkedHashSet<>();
	
						if (nodeRow.isSet(CLUSTER_ATTR))
							clusterNameSet.addAll(nodeRow.getList(CLUSTER_ATTR, String.class));
	
						clusterNameSet.add(cluster.getName());
						
						if (cancelled)
							return 0;
						
						nodeRow.set(CLUSTER_ATTR, new ArrayList<>(clusterNameSet));
	
						if (cancelled)
							return 0;
						
						if (cluster.getSeedNode() == nId)
							nodeRow.set(NODE_STATUS_ATTR, "Seed");
						else
							nodeRow.set(NODE_STATUS_ATTR, "Clustered");
					}
				}
			}

			return alg.getMaxScore(resultId);
		}
	}
}
