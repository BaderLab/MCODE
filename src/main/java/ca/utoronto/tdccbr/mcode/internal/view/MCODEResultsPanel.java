package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil.invokeOnEDT;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.action.MCODEDiscardResultAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.task.CreateClusterNetworkViewTask;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources.ImageName;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

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
 * * User: Vuk Pavlovic
 * * Description: The Results Panel displaying found clusters
 */

/**
 * Reports the results of MCODE cluster finding. This class sets up the UI.
 */
@SuppressWarnings("serial")
public class MCODEResultsPanel extends JPanel implements CytoPanelComponent {

	private static final String SCORE_ATTR = "MCODE_Score";
	private static final String NODE_STATUS_ATTR = "MCODE_Node_Status";
	private static final String CLUSTER_ATTR = "MCODE_Cluster";
	
	private final int resultId;
	private final MCODEAlgorithm alg;
	private final List<MCODECluster> clusters;
	private MCODEParameters currentParamsCopy;
	/** Keep track of selected attribute for enumeration so it stays selected for all cluster explorations */
	private int enumerationSelection;
	
	// Actual cluster data
	private final CyNetwork network;
	private CyNetworkView networkView;
	private final MCODEUtil mcodeUtil;
	private final MCODEDiscardResultAction discardResultAction;

	// Graphical classes
	private JScrollPane clusterBrowserScroll;
	private ClusterBrowserPanel clusterBrowserPnl;
	private BasicCollapsiblePanel explorePnl;
	private JPanel bottomPnl;
	private JButton createSubNetButton;
	private JButton closeBtn;
	private final Map<Integer, ExploreContentPanel> exploreContentPanels = new HashMap<>();
	
	private final CyServiceRegistrar registrar;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);

	/**
	 * Constructor for the Results Panel which displays the clusters in a
	 * browser table and explore panels for each cluster.
	 * 
	 * @param clusters Found clusters from the MCODEAnalyzeTask
	 * @param alg A reference to the alg for this particular network
	 * @param network Network were these clusters were found
	 * @param clusterImages A list of images of the found clusters
	 * @param resultId Title of this result as determined by MCODESCoreAndFindAction
	 */
	public MCODEResultsPanel(
			final List<MCODECluster> clusters,
			final MCODEUtil mcodeUtil,
			final CyNetwork network,
			final CyNetworkView networkView,
			final int resultId,
			final MCODEDiscardResultAction discardResultAction,
			final CyServiceRegistrar registrar
	) {
		this.registrar = registrar;
		
		if (isAquaLAF())
			setOpaque(false);
		
		setLayout(new BorderLayout());
		
		this.alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
		this.mcodeUtil = mcodeUtil;
		this.resultId = resultId;
		this.clusters = Collections.synchronizedList(clusters);
		this.network = network;
		// The view may not exist, but we only test for that when we need to (in the TableRowSelectionHandler below)
		this.networkView = networkView;
		this.discardResultAction = discardResultAction;
		this.currentParamsCopy = mcodeUtil.getParameterManager().getResultParams(resultId);
		
		clusterBrowserScroll = new JScrollPane();
		clusterBrowserPnl = new ClusterBrowserPanel(clusters);
		
		clusterBrowserScroll.setViewportView(clusterBrowserPnl);
		clusterBrowserScroll.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				clusterBrowserPnl.updateScrollableTracksViewportHeight();
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(clusterBrowserScroll, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(clusterBrowserScroll, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getBottomPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public Icon getIcon() {
		final URL iconURL = MCODEResources.getUrl(ImageName.LOGO_SMALL);
		return new ImageIcon(iconURL);
	}

	@Override
	public String getTitle() {
		return "Result " + getResultId();
	}

	public int getResultId() {
		return this.resultId;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public CyNetwork getNetwork() {
		return network;
	}

	public int getSelectedClusterRow() {
		return clusterBrowserPnl.getSelectedIndex();
	}
	
	public List<MCODECluster> getClusters() {
		return new ArrayList<>(clusters);
	}
	
	public MCODECluster getSelectedCluster() {
		ClusterPanel p = clusterBrowserPnl.getSelectedItem();
		
		return p != null ? p.getCluster() : null;
	}

	public void discard(final boolean requestUserConfirmation) {
		invokeOnEDT(() -> {
			boolean oldRequestUserConfirmation = Boolean.valueOf(discardResultAction
					.getValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND).toString());

			discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND,
										 requestUserConfirmation);
			getCloseBtn().doClick();
			discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND,
										 oldRequestUserConfirmation);
		});
	}
	
	ClusterBrowserPanel getClusterBrowserPnl() {
		return clusterBrowserPnl;
	}
	
	/**
	 * Creates a panel containing the explore collapsable panel and result set
	 * specific buttons
	 * 
	 * @return Panel containing the explore cluster collapsable panel and button panel
	 */
	private JPanel getBottomPnl() {
		if (bottomPnl == null) {
			bottomPnl = new JPanel();
			
			if (isAquaLAF())
				bottomPnl.setOpaque(false);
			
			final JButton exportButton = new JButton("Export");
			exportButton.addActionListener(new ExportAction());
			exportButton.setToolTipText("Export result set to a text file");
			
			if (isAquaLAF()) {
				getCreateSubNetButton().putClientProperty("JComponent.sizeVariant", "small");
				exportButton.putClientProperty("JComponent.sizeVariant", "small");
				getCloseBtn().putClientProperty("JComponent.sizeVariant", "small");
			}
			
			final GroupLayout layout = new GroupLayout(bottomPnl);
			bottomPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getExplorePnl(), Alignment.CENTER, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(Alignment.CENTER, layout.createSequentialGroup()
						.addComponent(getCreateSubNetButton())
						.addComponent(exportButton)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(getCloseBtn())
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getExplorePnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(getCreateSubNetButton())
						.addComponent(exportButton)
						.addComponent(getCloseBtn())
					)
			);
		}

		return bottomPnl;
	}
	
	private BasicCollapsiblePanel getExplorePnl() {
		if (explorePnl == null) {
			explorePnl = new BasicCollapsiblePanel("Explore");
			explorePnl.setCollapsed(false);
			explorePnl.setVisible(false);
			
			if (isAquaLAF())
				explorePnl.setOpaque(false);
		}
		
		return explorePnl;
	}
	
	private JButton getCreateSubNetButton() {
		if (createSubNetButton == null) {
			createSubNetButton = new JButton("Create Cluster Network");
			createSubNetButton.addActionListener(new CreateSubNetworkAction(MCODEResultsPanel.this));
			
			createSubNetButton.setEnabled(clusterBrowserPnl.getSelectedIndex() >= 0);
		}
		
		return createSubNetButton;
	}
	
	private JButton getCloseBtn() {
		if (closeBtn == null) {
			closeBtn = new JButton(discardResultAction);
			discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND, true);
		}
		
		return closeBtn;
	}

	private void updateExploreControlPanel() {
		try {
			ClusterPanel item = clusterBrowserPnl.getSelectedItem();
			getCreateSubNetButton().setEnabled(item != null);
			
			if (item != null) {
				final int selectedRow = clusterBrowserPnl.getSelectedIndex();
				final MCODECluster cluster = item.getCluster();
				final CyNetwork net = cluster.getNetwork();
				selectCluster(net);

				// Upon selection of a cluster, we must show the corresponding explore panel content
				// First we test if this cluster has been selected yet and if its content exists.
				// If it does not, we create it.
				ExploreContentPanel explorePanel = exploreContentPanels.get(selectedRow);
				
				if (explorePanel == null)
					exploreContentPanels.put(selectedRow, explorePanel = createExploreContent(selectedRow));
				
				// Next, if this is the first time explore panel content is being displayed, then the
				// explore panel is not visible yet, and there is no content in
				// it yet, so we do not have to remove it, otherwise,
				// if the panel is visible, then it must have content which needs to be removed
				if (getExplorePnl().isVisible())
					getExplorePnl().getContentPane().removeAll();

				// Now we add the currently selected cluster's explore panel content
				getExplorePnl().getContentPane().add(explorePanel);

				// and set the explore panel to visible so that it can be seen
				// (this should only happen once after the first time the user selects a cluster
				if (!getExplorePnl().isVisible())
					getExplorePnl().setVisible(true);

				// Finally the explore panel must be redrawn upon the selection
				// event to display the new content with the name of the cluster, if it exists
				String title = "Explore: " + cluster.getName();
				getExplorePnl().setTitleComponentText(title);
				getExplorePnl().updateUI();

				// In order for the enumeration to be conducted for this cluster
				// on the same attribute that might already have been selected
				// we get a reference to the combo box within the explore content...
				final JComboBox<String> nodeAttributesComboBox = explorePanel.getNodeAttributesComboBox();
				// ...and fire the enumeration action
				nodeAttributesComboBox.setSelectedIndex(enumerationSelection);

				// TODO: it doesn't work from here
				// This needs to be called when the explore panel shows up 
				// so that any selection in the cluster browser is centered in the visible portion of the table
				// table.scrollRectToVisible(table.getCellRect(selectedRow, 0, true));
			}
		} catch (Exception ex) {
			logger.error("Unexpected MCODE error", ex);
		}
	}
	
	/**
	 * This method creates a JPanel containing a node score cutoff slider and a
	 * node attribute enumeration viewer
	 * 
	 * @param selectedRow
	 *            The cluster that is selected in the cluster browser
	 * @return panel A JPanel with the contents of the explore panel, get's
	 *         added to the explore collapsable panel's content pane
	 */
	private ExploreContentPanel createExploreContent(int selectedRow) {
		final ExploreContentPanel panel = new ExploreContentPanel(selectedRow);

		return panel;
	}

	/**
	 * Sets the network node attributes to the current result set's scores and clusters.
	 * This method is accessed from MCODEVisualStyleAction only when a results panel is selected in the east cytopanel.
	 * 
	 * @return the maximal score in the network given the parameters that were
	 *         used for scoring at the time
	 */
	public double setNodeAttributesAndGetMaxScore() {
		for (CyNode n : network.getNodeList()) {
			Long nId = n.getSUID();
			CyTable netNodeTbl = network.getDefaultNodeTable();
			
			if (netNodeTbl.getColumn(CLUSTER_ATTR) == null)
				netNodeTbl.createListColumn(CLUSTER_ATTR, String.class, false);
			if (netNodeTbl.getColumn(NODE_STATUS_ATTR) == null)
				netNodeTbl.createColumn(NODE_STATUS_ATTR, String.class, false);
			if (netNodeTbl.getColumn(SCORE_ATTR) == null)
				netNodeTbl.createColumn(SCORE_ATTR, Double.class, false);

			CyRow nodeRow = network.getRow(n);
			nodeRow.set(NODE_STATUS_ATTR, "Unclustered");
			nodeRow.set(SCORE_ATTR, alg.getNodeScore(n.getSUID(), resultId));

			for (MCODECluster cluster : clusters) {
				if (cluster.getNodes().contains(nId)) {
					Set<String> clusterNameSet = new LinkedHashSet<>();

					if (nodeRow.isSet(CLUSTER_ATTR))
						clusterNameSet.addAll(nodeRow.getList(CLUSTER_ATTR, String.class));

					clusterNameSet.add(cluster.getName());
					nodeRow.set(CLUSTER_ATTR, new ArrayList<>(clusterNameSet));

					if (cluster.getSeedNode() == nId)
						nodeRow.set(NODE_STATUS_ATTR, "Seed");
					else
						nodeRow.set(NODE_STATUS_ATTR, "Clustered");
				}
			}
		}

		return alg.getMaxScore(resultId);
	}
	
	/**
	 * Handles the create child network press in the cluster exploration panel
	 */
	private class CreateSubNetworkAction extends AbstractAction {

		MCODEResultsPanel trigger;

		CreateSubNetworkAction(MCODEResultsPanel trigger) {
			this.trigger = trigger;
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {
			if (clusterBrowserPnl.getSelectedIndex() < 0)
				return;
			
			final MCODECluster cluster = getSelectedCluster();
			
			final CreateClusterNetworkViewTask task = new CreateClusterNetworkViewTask(cluster, networkView,
					trigger.getResultId(), alg, mcodeUtil, registrar);
			registrar.getService(DialogTaskManager.class).execute(new TaskIterator(task));
		}
	}

	/**
	 * Panel that contains the browser table with a scroll bar.
	 */
	class ClusterBrowserPanel extends JPanel implements Scrollable {
		
		private final JPanel filler = new JPanel();
		
		private final LinkedHashMap<MCODECluster, ClusterPanel> items = new LinkedHashMap<>();
		private boolean scrollableTracksViewportHeight;
		
		public ClusterBrowserPanel(List<MCODECluster> clusters) {
			setBackground(UIManager.getColor("Table.background"));
			
			Color fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60);
			
			filler.setBorder(null);
			filler.setAlignmentX(LEFT_ALIGNMENT);
			filler.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
			filler.setBackground(getBackground());
			filler.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					if (!e.isPopupTrigger())
						setSelectedItem(null);
				}
			});
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			addItems(clusters);
			add(filler);
			
			updateScrollableTracksViewportHeight();
		}

		List<MCODECluster> getClusters() {
			return new ArrayList<>(items.keySet());
		}

		ClusterPanel getSelectedItem() {
			for (ClusterPanel p : items.values()) {
				if (p.isSelected())
					return p;
			}
			
			return null;
		}
		
		int getSelectedIndex() {
			int idx = -1;
			
			for (ClusterPanel p : items.values()) {
				idx++;
				
				if (p.isSelected())
					break;
			}
			
			return idx;
		}
		
		void setSelectedIndex(int index) {
			setSelectedItem(getItem(index));
		}
		
		void setSelectedItem(ClusterPanel item) {
			for (ClusterPanel p : items.values())
				p.setSelected(p.equals(item));
			
			updateExploreControlPanel();
		}
		
		public void scrollTo(ClusterPanel item) {
			if (item != null)
				((JComponent) item.getParent()).scrollRectToVisible(item.getBounds());
		}
		
		int getIndex(ClusterPanel item) {
			return item == null ? -1 : new ArrayList<>(items.values()).indexOf(item);
		}
		
		ClusterPanel getItem(int index) {
			return index < 0 ? null : new ArrayList<>(items.values()).get(index);
		}
		
		List<ClusterPanel> getAllItems() {
			return new ArrayList<>(items.values());
		}
		
		public int getItemsSize() {
			return items.size();
		}
		
		boolean isEmpty() {
			return items.isEmpty();
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return scrollableTracksViewportHeight;
		}
		
		void updateScrollableTracksViewportHeight() {
			final boolean oldValue = scrollableTracksViewportHeight;
			
			if (items == null || items.isEmpty()) {
				scrollableTracksViewportHeight = true;
			} else if (clusterBrowserScroll.getViewport() != null) {
				int ih = 0; // Total items height
				
				for (ClusterPanel p : items.values())
					ih += p.getHeight();
				
				scrollableTracksViewportHeight = ih <= clusterBrowserScroll.getViewport().getHeight();
			}
			
			if (oldValue != scrollableTracksViewportHeight)
				updateUI();
		}
		
		private void addItems(List<MCODECluster> clusters) {
			int rank = 0;
			
			for (MCODECluster c : clusters) {
				c.setRank(++rank);
				
				ClusterPanel p = new ClusterPanel(c, currentParamsCopy, mcodeUtil, registrar);
				p.setAlignmentX(LEFT_ALIGNMENT);
				p.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						updateScrollableTracksViewportHeight();
					}
				});
				addMouseListenersForSelection(p);
				setKeyBindings(p);
				
				p.getSizeSlider().addChangeListener(new SizeAction(rank));
				
				items.put(c, p);
				add(p);
			}
		}
		
		private void addMouseListenersForSelection(ClusterPanel item, JComponent... components) {
			// This mouse listener listens for mouse pressed events to select the list items
			final MouseListener selectionListener = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					item.requestFocusInWindow();
					final boolean isMac = LookAndFeelUtil.isMac();
					
					if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
						// COMMAND button down on MacOS or CONTROL button down on another OS.
						// Toggle this item's selection state
						setSelectedItem(item.isSelected() ? null : item);
					} else {
						setSelectedItem(item);
					}
				}
			};
			
			item.addMouseListener(selectionListener);
			
			for (JComponent c : components)
				c.addMouseListener(selectionListener);
		}
		
		private void setKeyBindings(final JComponent comp) {
			final ActionMap actionMap = comp.getActionMap();
			final InputMap inputMap = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			final int CTRL = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK :  InputEvent.CTRL_DOWN_MASK;

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
			
			actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
			actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
			actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
		}
		
		private class KeyAction extends AbstractAction {

			final static String VK_UP = "VK_UP";
			final static String VK_DOWN = "VK_DOWN";
			final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (clusterBrowserPnl.isEmpty())
					return;
				
				final String cmd = e.getActionCommand();
				int idx = clusterBrowserPnl.getSelectedIndex();
				
				if (cmd.equals(VK_UP)) {
					if (idx > 0)  {
						clusterBrowserPnl.setSelectedIndex(idx - 1);
						scrollTo(clusterBrowserPnl.getSelectedItem());
					}
				} else if (cmd.equals(VK_DOWN)) {
					if (idx >= 0 && idx < clusterBrowserPnl.getItemsSize() - 1) {
						clusterBrowserPnl.setSelectedIndex(idx + 1);
						scrollTo(clusterBrowserPnl.getSelectedItem());
					}
				} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
					clusterBrowserPnl.setSelectedItem(null);
				}
			}
		}
	}
	
	private class ExploreContentPanel extends JPanel {
		
		private JComboBox<String> nodeAttributesComboBox;
		
		ExploreContentPanel(int selectedRow) {
			if (isAquaLAF())
				setOpaque(false);
			
			// Node attributes enumerator
			final JLabel attrEnumLbl = new JLabel("Node Attribute:");
			final String ATTR_ENUM_TOOL_TIP = "Node Attribute Enumerator";
			attrEnumLbl.setToolTipText(ATTR_ENUM_TOOL_TIP);

			final ClusterPanel clusterPanel = clusterBrowserPnl.getItem(selectedRow);
			final CyNetwork net = clusterPanel.getCluster().getNetwork();
			
			final Collection<CyColumn> nodeColumns = net.getDefaultNodeTable().getColumns();
			final List<String> attributesList = new ArrayList<>(nodeColumns.size());

			for (CyColumn column : nodeColumns) {
				if (!column.getName().equals(CyNetwork.SUID) &&
						!column.getName().equals(CyNetwork.SELECTED) &&
						!column.getName().endsWith(".SUID"))
					attributesList.add(column.getName());
			}

			final Collator collator = Collator.getInstance(Locale.getDefault());
			Collections.sort(attributesList, collator);

			nodeAttributesComboBox = new JComboBox<>(attributesList.toArray(new String[attributesList.size()]));
			nodeAttributesComboBox.setToolTipText(ATTR_ENUM_TOOL_TIP);
			nodeAttributesComboBox.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value == null) this.setText("Please select...");
					
					return this;
				}
			});

			// Create a table listing the node attributes and their enumerations
			final ResultsEnumeratorTableModel modelEnumerator;
			modelEnumerator = new ResultsEnumeratorTableModel(new HashMap<>());

			final JTable enumerationsTable = new JTable(modelEnumerator);
			final JScrollPane tableScrollPane = new JScrollPane(enumerationsTable);
			enumerationsTable.setPreferredScrollableViewportSize(new Dimension(100, ClusterPanel.GRAPH_IMG_SIZE));
			enumerationsTable.setGridColor(new JSeparator().getForeground());
			enumerationsTable.setFont(new Font(enumerationsTable.getFont().getFontName(), Font.PLAIN,
					(int) getSmallFontSize()));
			enumerationsTable.setFocusable(false);
			enumerationsTable.getColumnModel().getColumn(0).setPreferredWidth(180);

			// Create a combo box that lists all the available node attributes for enumeration
			nodeAttributesComboBox.addActionListener(new EnumerateAction(modelEnumerator, selectedRow));
			nodeAttributesComboBox.setSelectedItem(null);

			if (isAquaLAF()) {
				attrEnumLbl.putClientProperty("JComponent.sizeVariant", "small");
				nodeAttributesComboBox.putClientProperty("JComponent.sizeVariant", "small");
				enumerationsTable.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(attrEnumLbl)
							.addComponent(nodeAttributesComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(tableScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(attrEnumLbl)
							.addComponent(nodeAttributesComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(tableScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}

		JComboBox<String> getNodeAttributesComboBox() {
			return nodeAttributesComboBox;
		}
	}
	
	/**
	 * Handles the data to be displayed in the node attribute enumeration table
	 */
	private class ResultsEnumeratorTableModel extends AbstractTableModel {

		String[] columnNames = { "Value", "Occurrence" };
		Object[][] data = new Object[0][columnNames.length]; // the actual table

		public ResultsEnumeratorTableModel(HashMap<Object, Integer> enumerations) {
			listIt(enumerations);
		}

		public void listIt(final HashMap<Object, Integer> enumerations) {
			// First we sort the hash map of attributes values and their occurrences
			final List<Entry<Object, Integer>> enumerationsSorted = sortMap(enumerations);
			// Then we put it into the data array in reverse order so that the
			// most frequent attribute value is on top
			final Object[][] newData = new Object[enumerationsSorted.size()][columnNames.length];
			int c = enumerationsSorted.size() - 1;

			for (Iterator<Map.Entry<Object, Integer>> i = enumerationsSorted.iterator(); i.hasNext();) {
				final Map.Entry<Object, Integer> mp = i.next();
				
				newData[c][0] = mp.getKey();
				newData[c][1] = mp.getValue();
				c--;
			}

			// Finally we redraw the table, however, in order to prevent constant flickering
			// we only fire the data change if the number or rows is altered.
			// That way, when the number of rows stays the same, which is most of the
			// time, there is no flicker.
			if (getRowCount() == newData.length) {
				data = new Object[newData.length][columnNames.length];
				System.arraycopy(newData, 0, data, 0, data.length);
				fireTableRowsUpdated(0, getRowCount());
			} else {
				data = new Object[newData.length][columnNames.length];
				System.arraycopy(newData, 0, data, 0, data.length);
				fireTableDataChanged();
			}
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		@Override
		public void setValueAt(Object object, int row, int col) {
			data[row][col] = object;
			fireTableCellUpdated(row, col);
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	/**
	 * This method uses Arrays.sort for sorting a Map by the entries' values
	 * 
	 * @param map
	 *            Has values mapped to keys
	 * @return outputList of Map.Entries
	 */
	private List<Map.Entry<Object, Integer>> sortMap(final Map<Object, Integer> map) {
		final List<Map.Entry<Object, Integer>> outputList = new ArrayList<>(map.entrySet());

		// Sort the entries with own comparator for the values:
		Collections.sort(outputList, (o1, o2) -> {
			return o1.getValue().compareTo(o2.getValue());
		});

		return outputList;
	}

	/**
	 * Handles the selection of all available node attributes for the enumeration within the cluster
	 */
	private class EnumerateAction extends AbstractAction {

		int selectedRow;
		MCODEResultsPanel.ResultsEnumeratorTableModel modelEnumerator;

		EnumerateAction(MCODEResultsPanel.ResultsEnumeratorTableModel modelEnumerator, int selectedRow) {
			this.selectedRow = selectedRow;
			this.modelEnumerator = modelEnumerator;
		}

		
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(final ActionEvent e) {
			// The key is the attribute value and the value is the number of times that value appears in the cluster
			final HashMap<Object, Integer> attributeEnumerations = new HashMap<>();

			// First we want to see which attribute was selected in the combo box
			final String attributeName = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
			final int selectionIndex = ((JComboBox<String>) e.getSource()).getSelectedIndex();

			// If its the generic 'please select' option then we don't do any enumeration
			if (attributeName != null) {
				final ClusterPanel clusterPanel = clusterBrowserPnl.getItem(selectedRow);
				final CyNetwork net = clusterPanel.getCluster().getNetwork();
				
				// Otherwise, we want to get the selected attribute's value for each node in the selected cluster
				for (final CyNode node : net.getNodeList()) {
					// The attribute value will be stored as a string no matter
					// what it is but we need an array list because some attributes are maps or lists of any size
					final ArrayList<Object> attributeValues = new ArrayList<>();
					final CyRow row = net.getRow(node);
					final CyColumn column = row.getTable().getColumn(attributeName);
					
					if (column == null) // This should never happen!
						continue;
					
					final Class<?> type = column.getType();

					if (Collection.class.isAssignableFrom(type)) {
						final Collection<Object> valueList = (Collection<Object>) row.get(attributeName, type);

						if (valueList != null) {
							for (Object value : valueList)
								attributeValues.add(value);
						}
					} else {
						attributeValues.add(row.get(attributeName, type));
					}

					// Next we must make a non-repeating list with the attribute values and enumerate the repetitions
					for (final Object aviElement : attributeValues) {
						if (aviElement != null) {
							final Object value = aviElement instanceof Number ? aviElement : aviElement.toString();

							if (!attributeEnumerations.containsKey(value)) {
								// If the attribute value appears for the first
								// time, we give it an enumeration of 1 and add it to the enumerations
								attributeEnumerations.put(value, 1);
							} else {
								// If it already appeared before, we want to add to the enumeration of the value
								Integer enumeration = (Integer) attributeEnumerations.get(value);
								enumeration = enumeration.intValue() + 1;
								attributeEnumerations.put(value, enumeration);
							}
						}
					}
				}
			}

			modelEnumerator.listIt(attributeEnumerations);
			// Finally we make sure that the selection is stored so that all the
			// cluster explorations are looking at the already selected attribute
			enumerationSelection = selectionIndex;
		}
	}

	/**
	 * Handles the Export press for this panel (export results to a text file)
	 */
	private class ExportAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			mcodeUtil.exportMCODEResults(alg, getClusters(), network);
		}
	}

	/**
	 * Selects a cluster in the view that is selected by the user in the browser table
	 * 
	 * @param custerNetwork Cluster to be selected
	 */
	public void selectCluster(final CyNetwork custerNetwork) {
		if (custerNetwork != null) {
			// Only do this if a view has been created on this network
			// start with no selected nodes
			//				mcodeUtil.setSelected(network.getNodeList(), false, networkView);
			mcodeUtil.setSelected(custerNetwork.getNodeList(), network);

			// TODO: is it still necessary?
			// We want the focus to switch to the appropriate network view but only if the cytopanel is docked
			// If it is not docked then it is best if the focus stays on the panel
			//				if (swingApplication.getCytoPanel(CytoPanelName.EAST).getState() == CytoPanelState.DOCK) {
			//					
			//					 Cytoscape.getDesktop().setFocus(networkView.getSUID());
			//				}
		} else {
			mcodeUtil.setSelected(new ArrayList<>(), network); // deselect all
		}
	}
	
	/**
	 * Handles the dynamic cluster size manipulation via the JSlider
	 */
	private class SizeAction implements ChangeListener {

		private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(3);
		private ScheduledFuture<?> futureLoader;
		
		private final int row;
		
		/**
		 * @param row The selected cluster row
		 * @param nodeAttributesComboBox Reference to the attribute enumeration picker
		 */
		SizeAction(int row) {
			this.row = row;
		}

		@Override
		public void stateChanged(ChangeEvent evt) {
			// This method as been written so that the slider is responsive to the user's input at all times, despite
			// the computationally challenging drawing, layout out, and selection of large clusters. As such, we only
			// perform real work if the slider produces a different cluster, and furthermore we only perform the quick
			// processes here, while the heavy weights are handled by the drawer's thread.
			final JSlider source = (JSlider) evt.getSource();
			final double nodeScoreCutoff = (((double) source.getValue()) / 1000);
			final int clusterRow = row;
			// Store current cluster content for comparison
			final MCODECluster oldCluster = clusters.get(clusterRow);
			
			if (futureLoader != null && !futureLoader.isDone()) {
				futureLoader.cancel(true);
				
				if (!oldCluster.equals(clusters.get(clusterRow)))
					oldCluster.dispose();
			}
			
			final Runnable command = (() -> {
	            	final List<Long> oldNodes = oldCluster.getNodes();
				// Find the new cluster given the node score cutoff
				final MCODECluster newCluster = alg.exploreCluster(oldCluster, nodeScoreCutoff, network, resultId);
				
				// We only want to do the following work if the newly found cluster is actually different
				// So we get the new cluster content
				List<Long> newNodes = newCluster.getNodes();

				// TODO
//				if (newNodes.size() <= 300 && !newNodes.equals(oldNodes)) { // And compare the old and new
//					// If the cluster has changed, then we conduct all non-rate-limiting steps:
//					// Update the cluster array
//					clusters.set(clusterRow, newCluster);
//					// Update the cluster details
//					clusterBrowserPnl.update(newCluster, clusterRow);
//					// Fire the enumeration action
//					nodeAttributesComboBox.setSelectedIndex(nodeAttributesComboBox.getSelectedIndex());
//	
//					// There is a small difference between expanding and retracting the cluster size.
//					// When expanding, new nodes need random position and thus must go through the layout.
//					// When retracting, we simply use the layout that was generated and stored.
//					// This speeds up the drawing process greatly.
//					boolean layoutNecessary = newNodes.size() > oldNodes.size();
//					
//					// Draw Graph and select the cluster in the view in a separate thread so that it can be
//					// interrupted by the slider movement
//					if (!newCluster.isDisposed()) {
//						// TODO draw cluster image
//						drawGraph(row, newCluster, layoutNecessary);
//						oldCluster.dispose();
//					}
//				}
	        });
	        
	        futureLoader = scheduler.schedule(command, 100, TimeUnit.MILLISECONDS);
		}
	}

//	/**
//	 * Threaded method for drawing exploration graphs which allows the slider to
//	 * move uninterruptedly despite MCODE's drawing efforts
//	 */
//	class GraphDrawer implements Runnable {
//
//		private boolean drawGraph; // run switch
//		private boolean placeHolderDrawn;
//		private boolean drawPlaceHolder;
//		MCODECluster cluster;
//		SpringEmbeddedLayouter layouter;
//		boolean layoutNecessary;
//		boolean clusterSelected;
//		private Thread t;
//		private final MCODELoadIcon loadIcon;
//		private boolean interrupted;
//
//		GraphDrawer(MCODELoadIcon loadIcon) {
//			this.loadIcon = loadIcon;
//			layouter = new SpringEmbeddedLayouter();
//		}
//
//		/**
//		 * @param cluster Cluster to be drawn
//		 * @param layoutNecessary True only if the cluster is expanding in size or lacks a DGView
//		 * @param drawPlaceHolder Determines if the cluster should be drawn or a place
//		 *                        holder in the case of big clusters
//		 */
//		public void drawGraph(MCODECluster cluster, boolean layoutNecessary, boolean drawPlaceHolder) {
//			this.cluster = cluster;
//			this.layoutNecessary = layoutNecessary;
//
//			// Graph drawing will only occur if the cluster is not too large,
//			// otherwise a place holder will be drawn
//			drawGraph = !drawPlaceHolder;
//			this.drawPlaceHolder = drawPlaceHolder;
//			clusterSelected = false;
//			t = new Thread(this);
//			t.start();
//		}
//
//		@Override
//		public void run() {
//			try {
//				// We want to set the loader only once during continuous exploration
//				// It is only set again when a graph is fully loaded and placed in the table
//				if (!drawPlaceHolder) {
//					// internally, the loader is only drawn into the appropriate cell after a short sleep period
//					// to ensure that quick loads are not displayed unnecessarily
//					loadIcon.start();
//				}
//				
//				final Thread currentThread = Thread.currentThread(); 
//				
//				while (t == currentThread) {
//					// This ensures that the drawing of this cluster is only attempted once
//					// if it is unsuccessful it is because the setup or layout
//					// process was interrupted by the slider movement
//					// In that case the drawing must occur for a new cluster using the drawGraph method
//					if (drawGraph && !drawPlaceHolder) {
//						PropertyChangeListener pcl = new PropertyChangeListener() {
//							@Override
//							public void propertyChange(PropertyChangeEvent evt) {
//								Image image = cluster.getImage();
//								
//								// If the drawing process was interrupted, a new cluster must have been found and
//								// this will have returned null, the drawing will be recalled (with the new cluster)
//								// However, if the graphing was successful, we update
//								// the table, stop the loader from animating and select the new cluster
//								if (image != null && drawGraph) {
//									cluster.removePropertyChangeListener(this);
//									// Select the new cluster (surprisingly a time consuming step)
//									loadIcon.setProgress(100, "Selecting Nodes");
//									selectCluster(cluster.getNetwork());
//									clusterSelected = true;
//									// Update the table
//									clusterBrowserPnl.update(new ImageIcon(image), cluster.getRank() - 1);
//									drawGraph = false;
//								}
//	
//								// This is here just in case to reset the variable
//								placeHolderDrawn = false;
//							}
//						};
//						
//						cluster.addPropertyChangeListener("image", pcl);
//						createClusterImage(cluster, GRAPH_IMG_SIZE, GRAPH_IMG_SIZE, layouter, layoutNecessary, loadIcon);
//					} else if (drawPlaceHolder && !placeHolderDrawn) {
//						// draw place holder, only once though (as per the if statement)
//						Image image = mcodeUtil.getPlaceHolderImage(GRAPH_IMG_SIZE, GRAPH_IMG_SIZE);
//						cluster.setImage(image);
//						// Update the table
//						clusterBrowserPnl.update(new ImageIcon(image), cluster.getRank() - 1);
//						// select the cluster
//						selectCluster(cluster.getNetwork());
//						drawGraph = false;
//						// Make sure this block is not run again unless if we need to reload the image
//						placeHolderDrawn = true;
//					} else if (!drawGraph && drawPlaceHolder && !clusterSelected) {
//						selectCluster(cluster.getNetwork());
//						clusterSelected = true;
//					}
//
//					if ((!drawGraph && !drawPlaceHolder) || placeHolderDrawn)
//						stop();
//					
//					// This sleep time produces the drawing response time of 1 20th of a second
//					Thread.sleep(100);
//				}
//			} catch (Exception e) {
//				logger.error("Error while drawing cluster image", e);
//			}
//		}
//		
//		void interruptLoading() {
//			interrupted = true;
//		}
//
//		void resetLoading() {
//			interrupted = false;
//		}
//
//		void stop() {
//			// stop loader from animating and taking up computer processing power
//			loadIcon.stop();
//			layouter.interruptDoLayout();
//			interruptLoading();
//			drawGraph = false;
//			t = null;
//		}
//	}
}
