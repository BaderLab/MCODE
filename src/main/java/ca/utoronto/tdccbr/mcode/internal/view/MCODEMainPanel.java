package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.LAYERED_MCODE_ICON;
import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.MCODE_ICON_COLORS;
import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.getIconFont;
import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.recursiveDo;
import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.styleHeaderButton;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_BARS;
import static org.cytoscape.util.swing.IconManager.ICON_PLUS;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.CardLayout;
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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
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
public class MCODEMainPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "mcode.view.ControlPanel";
	
	/** Keep track of selected attribute for enumeration so it stays selected for all cluster explorations */
	private int enumerationSelection = -1;
	
	private JComboBox<MCODEResult> resultsCombo;
	private JButton newAnalysisButton;
	private JToggleButton discardButton;
	private JButton optionsButton;
	private InfoPanel infoPanel;
	private JPanel clustersPanel;
	private Map<MCODEResult, JScrollPane> clusterBrowserPanes = new HashMap<>();
	private BasicCollapsiblePanel explorePanel;
	private JButton helpButton;
	private JButton aboutButton;
	private JButton closePanelButton;
	
	private final CardLayout cardLayout = new CardLayout();
	
	private final Map<Integer, ExploreContentPanel> exploreContentPanels = new HashMap<>();
	
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;

	private final Icon compIcon = new TextIcon(LAYERED_MCODE_ICON, getIconFont(14.0f), MCODE_ICON_COLORS, 16, 16);
	
	private final Object lock = new Object();
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.class);

	/**
	 * Constructor for the Results Panel which displays the clusters in a
	 * browser table and explore panels for each cluster.
	 */
	public MCODEMainPanel(MCODEUtil mcodeUtil, CyServiceRegistrar registrar) {
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
		
		if (isAquaLAF())
			setOpaque(false);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(isWinLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getResultsCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
   						.addComponent(getNewAnalysisButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getDiscardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getInfoPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getClustersPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getExplorePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
   						.addComponent(getHelpButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addPreferredGap(ComponentPlacement.RELATED)
   						.addComponent(getAboutButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addGap(0, 0, Short.MAX_VALUE)
   						.addComponent(getClosePanelButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addContainerGap()
   				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(getResultsCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getNewAnalysisButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getDiscardButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(getInfoPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getClustersPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getExplorePanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(CENTER, false)
   						.addComponent(getHelpButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getAboutButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getClosePanelButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
		);
		
		update();
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return compIcon;
	}

	@Override
	public String getTitle() {
		return "MCODE";
	}
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	public MCODEResult getSelectedResult() {
		return (MCODEResult) getResultsCombo().getSelectedItem();
	}

	public int getSelectedClusterRow() {
		ClusterBrowser clusterBrowser = getSelectedClusterBrowser();
		
		return clusterBrowser != null ? clusterBrowser.getSelectedIndex() : -1;
	}
	
	public List<MCODECluster> getClusters() {
		return getSelectedResult() != null ? new ArrayList<>(getSelectedResult().getClusters()) : Collections.emptyList();
	}
	
	public MCODECluster getCluster(int index) {
		return getSelectedResult() != null ? getSelectedResult().getClusters().get(index) : null;
	}
	
	public MCODECluster getSelectedCluster() {
		ClusterPanel p = getSelectedClusterPanel();
		
		return p != null ? p.getCluster() : null;
	}
	
	public int getResultsCount() {
		return getResultsCombo().getItemCount();
	}
	
	public void addResult(MCODEResult res) {
		getResultsCombo().addItem(res);
		getResultsCombo().setSelectedItem(res);
		
		ClusterBrowser clusterBrowser = new ClusterBrowser(res);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportView(clusterBrowser);
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				clusterBrowser.updateScrollableTracksViewportHeight();
			}
		});
		
		getClustersPanel().add(scrollPane, res.toString());
		
		synchronized (lock) {
			clusterBrowserPanes.put(res, scrollPane);
		}

		update();
	}
	
	public void removeResult(MCODEResult res) {
		getResultsCombo().removeItem(res);
		
		synchronized (lock) {
			JScrollPane scrollPane = clusterBrowserPanes.remove(res);
			
			if (scrollPane != null)
				getClustersPanel().remove(scrollPane);
		}
		
		update();
	}
	
	public ExploreContentPanel getExploreContentPanel(int index) {
		return exploreContentPanels.get(index);
	}
	
	ClusterBrowser getClusterBrowser(MCODEResult res) {
		synchronized (lock) {
			JScrollPane scrollPane = clusterBrowserPanes.get(res);
			
			return scrollPane != null ? (ClusterBrowser) scrollPane.getViewport().getView() : null;
		}
	}
	
	ClusterBrowser getSelectedClusterBrowser() {
		MCODEResult res = getSelectedResult();
		
		synchronized (lock) {
			JScrollPane scrollPane = res != null ? clusterBrowserPanes.get(res) : null;
			
			return scrollPane != null ? (ClusterBrowser) scrollPane.getViewport().getView() : null;
		}
	}
	
	ClusterPanel getSelectedClusterPanel() {
		ClusterBrowser clusterBrowser = getSelectedClusterBrowser();
				
		return clusterBrowser != null ? clusterBrowser.getSelectedItem() : null;
	}
	
	JComboBox<MCODEResult> getResultsCombo() {
		if (resultsCombo == null) {
			resultsCombo = new JComboBox<>();
			resultsCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (value instanceof MCODEResult == false) {
						setText("-- Select Analysis Result --");
						setToolTipText(null);
					} else {
						setToolTipText(((MCODEResult) value).toString());
					}
					
					return this;
				}
			});
		}
		
		return resultsCombo;
	}
	
	JButton getNewAnalysisButton() {
		if (newAnalysisButton == null) {
			newAnalysisButton = new JButton(ICON_PLUS);
			newAnalysisButton.setToolTipText("New Analysis...");
			styleHeaderButton(newAnalysisButton, registrar.getService(IconManager.class).getIconFont(16.0f));
		}
		
		return newAnalysisButton;
	}
	
	JToggleButton getDiscardButton() {
		if (discardButton == null) {
			discardButton = new JToggleButton(ICON_TRASH_O);
			discardButton.setToolTipText("Discard Selected Result");
			styleHeaderButton(discardButton, registrar.getService(IconManager.class).getIconFont(16.0f));
		}
		
		return discardButton;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(ICON_BARS);
			optionsButton.setToolTipText("Options...");
			styleHeaderButton(optionsButton, registrar.getService(IconManager.class).getIconFont(18.0f));
		}
		
		return optionsButton;
	}
	
	InfoPanel getInfoPanel() {
		if (infoPanel == null) {
			infoPanel = new InfoPanel();
			infoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			infoPanel.setVisible(false);
		}
		
		return infoPanel;
	}
	
	JPanel getClustersPanel() {
		if (clustersPanel == null) {
			clustersPanel = new JPanel();
			clustersPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			clustersPanel.setLayout(cardLayout);
		}
		
		return clustersPanel;
	}
	
	private BasicCollapsiblePanel getExplorePanel() {
		if (explorePanel == null) {
			explorePanel = new BasicCollapsiblePanel("Explore");
			explorePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			explorePanel.setCollapsed(false);
			explorePanel.setVisible(false);
			
			if (isAquaLAF())
				explorePanel.setOpaque(false);
		}
		
		return explorePanel;
	}
	
	private JButton getHelpButton() {
		if (helpButton == null) {
			helpButton = LookAndFeelUtil.createHelpButton("http://baderlab.org/Software/MCODE/UsersManual/");
			helpButton.setToolTipText("Online Help...");
		}
		
		return helpButton;
	}
	
	JButton getAboutButton() {
		if (aboutButton == null) {
			aboutButton = new JButton("About");
			makeSmall(aboutButton);
		}
		
		return aboutButton;
	}
	
	JButton getClosePanelButton() {
		if (closePanelButton == null) {
			closePanelButton = new JButton("Close");
			makeSmall(closePanelButton);
		}
		
		return closePanelButton;
	}
	
	void updateExploreControlPanel() {
		try {
			ClusterPanel item = getSelectedClusterPanel();
			
			if (item != null) {
				final int index = getSelectedClusterRow();
				final MCODECluster cluster = item.getCluster();
				final CyNetwork net = cluster.getNetwork();
				selectCluster(net);

				// Upon selection of a cluster, we must show the corresponding explore panel content
				// First we test if this cluster has been selected yet and if its content exists.
				// If it does not, we create it.
				ExploreContentPanel explorePanel = getExploreContentPanel(index);
				
				if (explorePanel == null)
					exploreContentPanels.put(index, explorePanel = new ExploreContentPanel(index, net));
				
				getExplorePanel().getContentPane().removeAll();
				getExplorePanel().getContentPane().add(explorePanel);
				getExplorePanel().setVisible(true);

				// Finally the explore panel must be redrawn upon the selection
				// event to display the new content with the name of the cluster, if it exists
				String title = "Explore: " + cluster.getName();
				getExplorePanel().setTitleComponentText(title);
				getExplorePanel().updateUI();

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
			} else {
				getExplorePanel().getContentPane().removeAll();
				getExplorePanel().setVisible(false);
			}
		} catch (Exception ex) {
			logger.error("Unexpected MCODE error", ex);
		}
	}
	
	void update() {
		updateNewAnalysisButton();
		
		getResultsCombo().setEnabled(getResultsCombo().getItemCount() > 0);
		getResultsCombo().setToolTipText(getSelectedResult() != null ? getSelectedResult().toString() : null);
		getDiscardButton().setEnabled(getSelectedResult() != null);
		getOptionsButton().setEnabled(getSelectedResult() != null);
		getInfoPanel().update();
		
		if (getSelectedResult() == null) {
			getInfoPanel().setVisible(false);
			getExplorePanel().setCollapsed(true);
			getExplorePanel().setVisible(false);
		} else {
			cardLayout.show(getClustersPanel(), getSelectedResult().toString());
		}
	}

	void updateNewAnalysisButton() {
		getNewAnalysisButton().setEnabled(registrar.getService(CyApplicationManager.class).getCurrentNetwork() != null);
	}
	
	class InfoPanel extends JPanel {
		
		private static final String TRUE = "Yes";
		private static final String FALE = "No";
		
		private JLabel[] labels = new JLabel[] {
				new JLabel("Find Clusters:"),
				new JLabel("Include Loops:"),
				new JLabel("Degree Cutoff:"),
				new JLabel("Haircut:"),
				new JLabel("Fluff:"),
				new JLabel("Node Density Cutoff:"),
				new JLabel("Node Score Cutoff:"),
				new JLabel("K-Core:"),
				new JLabel("Max. Depth:"),
		};
		private JLabel[] values = new JLabel[labels.length];
		
		InfoPanel() {
			setBackground(UIManager.getColor("TableHeader.background"));
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			ParallelGroup hlGroup1 = null;
			ParallelGroup hvGroup1 = null;
			SequentialGroup vGroup1 = null;
			
			ParallelGroup hlGroup2 = null;
			ParallelGroup hvGroup2 = null;
			SequentialGroup vGroup2 = null;
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addGroup(hlGroup1 = layout.createParallelGroup(Alignment.LEADING, true))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(hvGroup1 = layout.createParallelGroup(Alignment.TRAILING, true))
					)
					.addGap(12, 24, 96)
					.addGroup(layout.createSequentialGroup()
							.addGroup(hlGroup2 = layout.createParallelGroup(Alignment.LEADING, true))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(hvGroup2 = layout.createParallelGroup(Alignment.TRAILING, true))
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(vGroup1 = layout.createSequentialGroup())
					.addGroup(vGroup2 = layout.createSequentialGroup())
			);
			
			float half = (labels.length - 1) / 2.0f;
			
			for (int i = 0; i < labels.length; i++) {
				JLabel l = labels[i];
				JLabel v = values[i] = new JLabel();
				
				Color fg = UIManager.getColor("Label.infoForeground");
				l.setForeground(fg);
				v.setForeground(fg);
				
				ParallelGroup hlg = i <= half ? hlGroup1 : hlGroup2;
				ParallelGroup hvg = i <= half ? hvGroup1 : hvGroup2;
				SequentialGroup vg = i <= half ? vGroup1 : vGroup2;
				
				hlg.addComponent(l);
				hvg.addComponent(v);
				
				vg.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(l)
						.addComponent(v)
				);
			}
			
			makeSmall(labels);
			makeSmall(values);
		}
		
		void update() {
			MCODEResult res = getSelectedResult();
			MCODEParameters params = res != null ? res.getParameters() : null;
			
			if (params == null) {
				for (JLabel v : values)
					v.setText("");
			} else {
				int i = 0;
				values[i++].setText(params.getScope().toString());
				values[i++].setText(params.getIncludeLoops() ? TRUE : FALE);
				values[i++].setText("" + params.getDegreeCutoff());
				values[i++].setText(params.getHaircut() ? TRUE : FALE);
				values[i++].setText(params.getFluff() ? TRUE : FALE);
				values[i++].setText("" + params.getFluffNodeDensityCutoff());
				values[i++].setText("" + params.getNodeScoreCutoff());
				values[i++].setText("" + params.getKCore());
				values[i++].setText("" + params.getMaxDepthFromStart());
			}
			
			updateUI();
		}
	}
	
	/**
	 * Panel that contains the browser table with a scroll bar.
	 */
	class ClusterBrowser extends JPanel implements Scrollable {
		
		private final JPanel filler = new JPanel();
		
		private final LinkedHashMap<MCODECluster, ClusterPanel> items = new LinkedHashMap<>();
		private boolean scrollableTracksViewportHeight;

		private final MCODEResult result;
		
		public ClusterBrowser(MCODEResult result) {
			this.result = result;
			
			setName(result.toString());
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
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
			SequentialGroup vGroup = layout.createSequentialGroup();
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			int index = 0;
			List<MCODECluster> clusters = getClusters();
			
			for (MCODECluster c : clusters) {
				c.setRank(index + 1);
				
				ClusterPanel p = new ClusterPanel(index, c, getSelectedResult().getParameters(), registrar);
				p.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						updateScrollableTracksViewportHeight();
					}
				});
				recursiveDo(p, comp -> addMouseListenersForSelection(p, comp));
				setKeyBindings(p);
				
				items.put(c, p);
				
				hGroup.addComponent(p, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
				vGroup.addComponent(p, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
				
				++index;
			}
			
			hGroup.addComponent(filler, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			vGroup.addComponent(filler, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
			
			updateScrollableTracksViewportHeight();
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
			ClusterPanel previousItem = getSelectedItem();
			
			if (item != previousItem) {
				int idx = previousItem != null ? previousItem.getIndex() : -1;
				ExploreContentPanel expPnl = getExploreContentPanel(idx);
				
				if (expPnl != null)
					enumerationSelection = expPnl.getNodeAttributesComboBox().getSelectedIndex();
				
				for (ClusterPanel p : items.values())
					p.setSelected(p.equals(item));
				
				updateExploreControlPanel();
				updateScrollableTracksViewportHeight();
			}
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
			} else {
				JScrollPane scrollPane = clusterBrowserPanes.get(result);
				
				if (scrollPane != null) {
					int ih = 0; // Total items height
					
					// Watch out for ConcurrentModificationExceptions!
					for (ClusterPanel p : new ArrayList<>(items.values()))
						ih += p.getHeight();
					
					scrollableTracksViewportHeight = ih <= scrollPane.getViewport().getHeight();
				}
			}
			
			if (oldValue != scrollableTracksViewportHeight)
				updateUI();
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
				ClusterBrowser clusterBrowser = getSelectedClusterBrowser();
				
				if (clusterBrowser == null || clusterBrowser.isEmpty())
					return;
				
				final String cmd = e.getActionCommand();
				int idx = clusterBrowser.getSelectedIndex();
				
				if (cmd.equals(VK_UP)) {
					if (idx > 0)  {
						clusterBrowser.setSelectedIndex(idx - 1);
						scrollTo(clusterBrowser.getSelectedItem());
					}
				} else if (cmd.equals(VK_DOWN)) {
					if (idx >= 0 && idx < clusterBrowser.getItemsSize() - 1) {
						clusterBrowser.setSelectedIndex(idx + 1);
						scrollTo(clusterBrowser.getSelectedItem());
					}
				} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
					clusterBrowser.setSelectedItem(null);
				}
			}
		}
	}
	
	class ExploreContentPanel extends JPanel {
		
		private JComboBox<String> nodeAttributesComboBox;
		private ResultsEnumeratorTableModel modelEnumerator;
		
		ExploreContentPanel(int index, CyNetwork clusterNet) {
			if (isAquaLAF())
				setOpaque(false);
			
			Collection<CyColumn> nodeColumns = clusterNet.getDefaultNodeTable().getColumns();
			List<String> attributesList = new ArrayList<>(nodeColumns.size());

			for (CyColumn column : nodeColumns) {
				if (!column.getName().equals(CyNetwork.SUID) &&
						!column.getName().equals(CyNetwork.SELECTED) &&
						!column.getName().endsWith(".SUID"))
					attributesList.add(column.getName());
			}

			// Node attributes enumerator
			JLabel attrEnumLbl = new JLabel("Node Attribute:");
			String ATTR_ENUM_TOOL_TIP = "Node Attribute Enumerator";
			attrEnumLbl.setToolTipText(ATTR_ENUM_TOOL_TIP);
			
			Collator collator = Collator.getInstance(Locale.getDefault());
			Collections.sort(attributesList, collator);

			nodeAttributesComboBox = new JComboBox<>(attributesList.toArray(new String[attributesList.size()]));
			nodeAttributesComboBox.setToolTipText(ATTR_ENUM_TOOL_TIP);
			nodeAttributesComboBox.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (value == null) {
						this.setText("Please select...");
						this.setToolTipText(null);
					} else {
						this.setText(mcodeUtil.abbreviate((String) value, 40));
						this.setToolTipText((String) value);
					}
					
					return this;
				}
			});

			// Create a table listing the node attributes and their enumerations
			modelEnumerator = new ResultsEnumeratorTableModel(new HashMap<>());

			JTable enumerationsTable = new JTable(modelEnumerator);
			JScrollPane tableScrollPane = new JScrollPane(enumerationsTable);
			enumerationsTable.setPreferredScrollableViewportSize(new Dimension(100, ClusterPanel.GRAPH_IMG_SIZE));
			enumerationsTable.setGridColor(new JSeparator().getForeground());
			enumerationsTable.setFont(new Font(enumerationsTable.getFont().getFontName(), Font.PLAIN,
					(int) getSmallFontSize()));
			enumerationsTable.setFocusable(false);
			enumerationsTable.getColumnModel().getColumn(0).setPreferredWidth(180);

			// Create a combo box that lists all the available node attributes for enumeration
			nodeAttributesComboBox.addActionListener(evt -> updateEnumerationsTable(index));
			nodeAttributesComboBox.setSelectedIndex(enumerationSelection);

			if (isAquaLAF()) {
				attrEnumLbl.putClientProperty("JComponent.sizeVariant", "small");
				nodeAttributesComboBox.putClientProperty("JComponent.sizeVariant", "small");
				enumerationsTable.putClientProperty("JComponent.sizeVariant", "small");
			}
			
			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(attrEnumLbl)
							.addComponent(nodeAttributesComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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

		@SuppressWarnings("unchecked")
		void updateEnumerationsTable(int index) {
			// The key is the attribute value and the value is the number of times that value appears in the cluster
			final HashMap<Object, Integer> attributeEnumerations = new HashMap<>();

			// First we want to see which attribute was selected in the combo box
			final String attributeName = (String) nodeAttributesComboBox.getSelectedItem();
			final int selectedIndex = nodeAttributesComboBox.getSelectedIndex();

			// If its the generic 'please select' option then we don't do any enumeration
			if (attributeName != null) {
				final CyNetwork net = getCluster(index).getNetwork();
				
				// Otherwise, we want to get the selected attribute's value for each node in the selected cluster
				for (CyNode node : net.getNodeList()) {
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
			enumerationSelection = selectedIndex;
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
	 * Selects a cluster in the view that is selected by the user in the browser table
	 * 
	 * @param custerNetwork Cluster to be selected
	 */
	public void selectCluster(final CyNetwork custerNetwork) {
		if (getSelectedResult() == null)
			return;
		
		if (custerNetwork != null && getSelectedResult() != null) {
			// Only do this if a view has been created on this network
			// start with no selected nodes
			// mcodeUtil.setSelected(network.getNodeList(), false, networkView);
			mcodeUtil.setSelected(custerNetwork.getNodeList(), getSelectedResult().getNetwork());
		} else {
			mcodeUtil.setSelected(new ArrayList<>(), getSelectedResult().getNetwork()); // deselect all
		}
	}
}
