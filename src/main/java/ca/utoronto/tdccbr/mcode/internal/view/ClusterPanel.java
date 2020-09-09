package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.createSpinnerIcon;
import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.invokeOnEDT;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;

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
 * * User: vukpavlovic
 * * Date: Dec 17, 2006
 * * Time: 12:33:12 PM
 * * Description: Generates a loading picture with a progress bar for the cluster browser table
 * * TODO: Make the loader more general so that it can be used in different situations as well (for example take in an object and painting bounds not table and selected row)
 */

@SuppressWarnings("serial")
public class ClusterPanel extends JPanel {

	public static final int GRAPH_IMG_SIZE = 80;
	
	private JLabel rankLabel;
	private JLabel imageLabel;
	private JLabel scoreLabel;
	private JLabel nodesLabel;
	private JLabel edgesLabel;
	private JSlider sizeSlider;
	
	private final TextIcon warnIcon;
	
	private boolean selected;
	
	private final int index;
	private MCODECluster cluster;
	private final MCODEParameters params;
	private final CyServiceRegistrar registrar;
	
	public ClusterPanel(int index, MCODECluster cluster, MCODEParameters params, CyServiceRegistrar registrar) {
		this.index = index;
		this.cluster = cluster;
		this.params = params;
		this.registrar = registrar;
		
		var font = registrar.getService(IconManager.class).getIconFont(36.0f);
		var fg = UIManager.getColor("Label.disabledForeground");
		fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60);
		warnIcon = new TextIcon(IconManager.ICON_EXCLAMATION_TRIANGLE, font, fg, GRAPH_IMG_SIZE, GRAPH_IMG_SIZE);
		
		cluster.addPropertyChangeListener("image", evt -> updateImage());
		init();
	}
	
	public int getIndex() {
		return index;
	}
	
	public MCODECluster getCluster() {
		return cluster;
	}
	
	public void setCluster(MCODECluster cluster) {
		if (!cluster.equals(this.cluster)) {
			this.cluster = cluster;
			cluster.addPropertyChangeListener("image", evt -> updateImage());
			update();
		}
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean newValue) {
		if (selected != newValue) {
			selected = newValue;
			updateSelection();
			firePropertyChange("selected", !newValue, newValue);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(getRankLabel())
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(getImageLabel())
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addGroup(layout.createSequentialGroup()
								.addGap(0, 0, Short.MAX_VALUE)
								.addComponent(getScoreLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(getSizeSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(getNodesLabel())
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(getEdgesLabel())
						)
				)
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGap(2)
				.addComponent(getRankLabel())
				.addComponent(getImageLabel())
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(getScoreLabel())
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(getSizeSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
								.addComponent(getNodesLabel())
								.addComponent(getEdgesLabel())
						)
						.addContainerGap()
				)
				.addGap(2)
		);
		
		// Change the slider's label sizes -- only works if it's done after the slider has been added to
		// its parent container and had its UI assigned
		var tickFont = getSizeSlider().getFont().deriveFont(getSmallFontSize());
		Dictionary<Integer, JLabel> labelTable = getSizeSlider().getLabelTable();
		
		for (var enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
			int k = enumeration.nextElement();
			var label = labelTable.get(k);
			label.setFont(tickFont); // Updates the font size
			label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
		}
		
		revalidate();
		update();
	}
	
	protected JLabel getRankLabel() {
		if (rankLabel == null) {
			rankLabel = createLabel("" + cluster.getRank());
			rankLabel.setToolTipText("Rank");
			rankLabel.setFont(rankLabel.getFont().deriveFont(Font.BOLD));
			rankLabel.setForeground(UIManager.getColor("Label.infoForeground"));
			rankLabel.setHorizontalAlignment(JLabel.RIGHT);
			rankLabel.setPreferredSize(new Dimension(32, rankLabel.getPreferredSize().height));
			rankLabel.setMinimumSize(rankLabel.getPreferredSize());
			rankLabel.setMaximumSize(rankLabel.getPreferredSize());
		}
		
		return rankLabel;
	}
	
	protected JLabel getImageLabel() {
		if (imageLabel == null) {
			imageLabel = new JLabel();
			imageLabel.setHorizontalAlignment(JLabel.CENTER);
			imageLabel.setOpaque(true);
			imageLabel.setBackground(UIManager.getColor("Table.background"));
			
			final int bw = 3; // Border width
			
			var d = new Dimension(GRAPH_IMG_SIZE + 2 * bw, GRAPH_IMG_SIZE + 2 * bw);
			imageLabel.setMinimumSize(d);
			imageLabel.setPreferredSize(d);
			imageLabel.setMaximumSize(d);
			imageLabel.setSize(d);
			
			imageLabel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createDashedBorder(UIManager.getColor("Separator.foreground"), 1, 1, 1, true),
					BorderFactory.createLineBorder(UIManager.getColor("Table.background"), bw - 1)
			));
		}
		
		return imageLabel;
	}

	protected JLabel getScoreLabel() {
		if (scoreLabel == null) {
			scoreLabel = createLabel("");
			scoreLabel.setToolTipText("Score");
			scoreLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
		
		return scoreLabel;
	}
	
	protected JLabel getNodesLabel() {
		if (nodesLabel == null) {
			nodesLabel = createLabel("");
			nodesLabel.setHorizontalAlignment(JLabel.RIGHT);
			nodesLabel.setForeground(UIManager.getColor("Label.infoForeground"));
		}
		
		return nodesLabel;
	}
	
	protected JLabel getEdgesLabel() {
		if (edgesLabel == null) {
			edgesLabel = createLabel("");
			edgesLabel.setHorizontalAlignment(JLabel.RIGHT);
			edgesLabel.setForeground(UIManager.getColor("Label.infoForeground"));
		}
		
		return edgesLabel;
	}
	
	/**
	 * Create a slider to manipulate node score cutoff.
	 */
	protected JSlider getSizeSlider() {
		if (sizeSlider == null) {
			// (goes to 1000 so that we get a more precise double variable out of it)
			sizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) (params.getNodeScoreCutoff() * 1000)) {
				@Override
				public void addNotify() {
					super.addNotify();
					// We have to do this here (after the slider is added to its parent container),
					// otherwise the cluster panel will show a glitch on macOS,
					// where it's resized when selected for the first time
					makeSmall(this);
					
					if (isAquaLAF())
						putClientProperty("JComponent.sizeVariant", "mini");
				}
			};
			sizeSlider.setOpaque(false);
			
			// Turn on ticks and labels at major and minor intervals.
			sizeSlider.setMajorTickSpacing(200);
			sizeSlider.setMinorTickSpacing(50);
			sizeSlider.setPaintTicks(true);
			sizeSlider.setPaintLabels(true);
			
			// Set labels ranging from 0 to 100
			var labelTable = new Hashtable<Integer, JLabel>();
			// Make a special label for the initial position
			labelTable.put((int) (params.getNodeScoreCutoff() * 1000), new JLabel("*"));

			sizeSlider.setLabelTable(labelTable);
			sizeSlider.setFont(
					sizeSlider.getFont() != null ? 
					sizeSlider.getFont().deriveFont(getSmallFontSize()) : 
					new Font("Arial", Font.PLAIN, (int) getSmallFontSize())
			);
			sizeSlider.setToolTipText("Size Threshold (Node Score Cutoff)");
		}
		
		return sizeSlider;
	}
	
	private void update() {
		updateSelection();
		updateLabels();
		updateImage();
	}

	private void updateSelection() {
		var c = UIManager.getColor(isSelected() ? "Table.selectionBackground" : "Table.background");
		setBackground(c);
		getSizeSlider().setEnabled(isSelected());
		
		revalidate();
	}
	
	private void updateLabels() {
		var nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);

		getRankLabel().setText("" + cluster.getRank());
		getScoreLabel().setText(nf.format(cluster.getScore()));
		getNodesLabel().setText(cluster.getGraph().getNodeCount() + " nodes");
		getEdgesLabel().setText(cluster.getGraph().getEdgeCount() + " edges");
		
		revalidate();
	}
	
	private void updateImage() {
		if (cluster.getImage() == null) {
			if (cluster.isTooLargeToVisualize()) {
				// If the new cluster is too large to draw within a reasonable time
				// and won't look understandable in the table cell, then we just show a warning
				showIcon(warnIcon, false);
				getImageLabel().setToolTipText("Cluster is too big to show");
			} else {
				showIcon(createSpinnerIcon(), true);
				getImageLabel().setToolTipText("Loading...");
			}
		} else {
			showIcon(new ImageIcon(cluster.getImage()), false);
			getImageLabel().setToolTipText("Cluster");
		}
	}
	
	private void showIcon(Icon icon, boolean animated) {
		invokeOnEDT(() -> {
			getImageLabel().setIcon(icon);
			
			if (animated && icon instanceof ImageIcon)
				((ImageIcon) icon).setImageObserver(getImageLabel());
		});
	}
	
	private JLabel createLabel(String txt) {
		var lbl = new JLabel(txt);
		
		if (LookAndFeelUtil.isAquaLAF())
			lbl.putClientProperty("JComponent.sizeVariant", "small");
		else
			lbl.setFont(lbl.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		return lbl;
	}
}
