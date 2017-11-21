package ca.utoronto.tdccbr.mcode.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.BasicCollapsiblePanel;

import ca.utoronto.tdccbr.mcode.internal.action.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
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
 * * Description: The Main Panel allowing user to choose scope and other parameters
 */

/**
 * The parameter change cytpanel which the user can use to select scope and change the scoring and finding parameters
 */
public class MCODEMainPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -4442491309881609088L;
	
	private final CySwingApplication swingApplication;

	private JPanel scopePnl;
	private BasicCollapsiblePanel advancedOptionsPnl;
	private JPanel networkScoringPnl;
	private JPanel clusterFindingPnl;
	private JLabel densityCutoffLabel;
	private JCheckBox includeLoopsCkb;
	private JFormattedTextField degreeCutoffTxt;
	private JFormattedTextField kCoreTxt;
	private JFormattedTextField scoreCutoffTxt;
	private JCheckBox haircutCkb;
	private JCheckBox fluffCkb;
	private JFormattedTextField densityCutoffTxt;
	private JFormattedTextField maxDepthTxt;

	private MCODEParameters currentParamsCopy; // stores current parameters - populates panel fields
	private DecimalFormat decFormat; // used in the formatted text fields

	/**
	 * The actual parameter change panel that builds the UI
	 * @param analyzeAction 
	 */
	public MCODEMainPanel(CySwingApplication swingApplication, MCODEAnalyzeAction analyzeAction, MCODEUtil mcodeUtil) {
		this.swingApplication = swingApplication;
		
		if (isAquaLAF())
			setOpaque(false);

		setMinimumSize(new Dimension(340, 400));
		setPreferredSize(new Dimension(380, 400));
		
		// get the current parameters
		currentParamsCopy = mcodeUtil.getParameterManager().getParamsCopy(null);
		currentParamsCopy.setDefaultParams();

		decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(true);

		densityCutoffLabel = new JLabel("Node Density Cutoff:");
		densityCutoffLabel.setMinimumSize(getDensityCutoffTxt().getMinimumSize());
		densityCutoffLabel.setToolTipText(getDensityCutoffTxt().getToolTipText());
		
		final JButton analyzeBtn = new JButton(analyzeAction);
		
		// Create the three main panels: scope, advanced options, and bottom
		// Add all the vertically aligned components to the main panel
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getScopePnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedOptionsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(analyzeBtn)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getScopePnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAdvancedOptionsPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 1, Short.MAX_VALUE)
				.addComponent(analyzeBtn)
		);
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
		URL iconURL = MCODEResources.getUrl(ImageName.LOGO_SMALL);
		return new ImageIcon(iconURL);
	}

	@Override
	public String getTitle() {
		return "";
	}

	public MCODEParameters getCurrentParamsCopy() {
		return currentParamsCopy;
	}

	/**
	 * @return panel containing the scope option buttons
	 */
	private JPanel getScopePnl() {
		if (scopePnl == null) {
			scopePnl = new JPanel();
			scopePnl.setBorder(createTitledBorder(""));
			
			if (isAquaLAF())
				scopePnl.setOpaque(false);

			final JRadioButton netScopeBtn = new JRadioButton("in whole network",
					currentParamsCopy.getScope() == MCODEAnalysisScope.NETWORK);
			final JRadioButton selScopeBtn = new JRadioButton("from selection",
					currentParamsCopy.getScope() == MCODEAnalysisScope.SELECTION);
			
			netScopeBtn.setActionCommand(MCODEAnalysisScope.NETWORK.toString());
			selScopeBtn.setActionCommand(MCODEAnalysisScope.SELECTION.toString());

			netScopeBtn.addActionListener(new ScopeAction());
			selScopeBtn.addActionListener(new ScopeAction());

			final ButtonGroup scopeOptions = new ButtonGroup();
			scopeOptions.add(netScopeBtn);
			scopeOptions.add(selScopeBtn);
			
			final JLabel findLabel = new JLabel("Find Clusters:");
			findLabel.setHorizontalAlignment(JLabel.RIGHT);
			findLabel.setMinimumSize(new Dimension(findLabel.getMinimumSize().width, netScopeBtn.getMinimumSize().height));

			final GroupLayout layout = new GroupLayout(scopePnl);
			scopePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(findLabel)
					).addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(netScopeBtn)
							.addComponent(selScopeBtn)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(findLabel)
					).addGroup(layout.createSequentialGroup()
							.addComponent(netScopeBtn)
							.addComponent(selScopeBtn)
					)
			);
		}

		return scopePnl;
	}

	private BasicCollapsiblePanel getAdvancedOptionsPnl() {
		if (advancedOptionsPnl == null) {
			advancedOptionsPnl = new BasicCollapsiblePanel("Advanced Options");
			
			if (isAquaLAF())
				advancedOptionsPnl.setOpaque(false);
			
			advancedOptionsPnl.getContentPane().add(getNetworkScoringPnl());
			advancedOptionsPnl.getContentPane().add(getClusterFindingPnl());
			advancedOptionsPnl.getContentPane().add(Box.createVerticalGlue());
		}

		return advancedOptionsPnl;
	}

	/**
	 * Creates a panel that holds network scoring parameter inputs
	 * @return panel containing the network scoring parameter inputs
	 */
	private JPanel getNetworkScoringPnl() {
		if (networkScoringPnl == null) {
			networkScoringPnl = new JPanel();
			
			if (isAquaLAF())
				networkScoringPnl.setOpaque(false);
			
			networkScoringPnl.setBorder(createTitledBorder("Network Scoring"));
			networkScoringPnl.setMaximumSize(
					new Dimension(Short.MAX_VALUE, networkScoringPnl.getPreferredSize().height));

			final JLabel degreeCutoffLabel = new JLabel("Degree Cutoff:");
			degreeCutoffLabel.setMinimumSize(getDegreeCutoffTxt().getMinimumSize());
			degreeCutoffLabel.setToolTipText(getDegreeCutoffTxt().getToolTipText());

			final GroupLayout layout = new GroupLayout(networkScoringPnl);
			networkScoringPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(degreeCutoffLabel)
					).addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getIncludeLoopsCkb())
							.addComponent(getDegreeCutoffTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.TRAILING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(degreeCutoffLabel)
					).addGroup(layout.createSequentialGroup()
							.addComponent(getIncludeLoopsCkb())
							.addComponent(getDegreeCutoffTxt())
					)
			);
		}

		return networkScoringPnl;
	}

	/**
	 * Creates a panel that holds 2 other panels for either customizing or optimized cluster finding parameters
	 * @return Panel
	 */
	private JPanel getClusterFindingPnl() {
		if (clusterFindingPnl == null) {
			clusterFindingPnl = new JPanel();
			
			if (isAquaLAF())
				clusterFindingPnl.setOpaque(false);
			
			clusterFindingPnl.setBorder(createTitledBorder("Cluster Finding"));
			clusterFindingPnl.setMaximumSize(
					new Dimension(Short.MAX_VALUE, clusterFindingPnl.getPreferredSize().height));

			final JLabel scoreCutoffLabel = new JLabel("Node Score Cutoff:");
			scoreCutoffLabel.setMinimumSize(getScoreCutoffTxt().getMinimumSize());
			scoreCutoffLabel.setToolTipText(getScoreCutoffTxt().getToolTipText());
			
			final JLabel kCoreLabel = new JLabel("K-Core:");
			kCoreLabel.setMinimumSize(getScoreCutoffTxt().getMinimumSize());
			kCoreLabel.setToolTipText(getScoreCutoffTxt().getToolTipText());
			
			final JLabel maxDepthLabel = new JLabel("Max. Depth:");
			maxDepthLabel.setMinimumSize(getMaxDepthTxt().getMinimumSize());
			maxDepthLabel.setToolTipText(getMaxDepthTxt().getToolTipText());
			
			final GroupLayout layout = new GroupLayout(clusterFindingPnl);
			clusterFindingPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(densityCutoffLabel)
							.addComponent(scoreCutoffLabel)
							.addComponent(kCoreLabel)
							.addComponent(maxDepthLabel)
					).addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getHaircutCkb())
							.addComponent(getFluffCkb())
							.addComponent(getDensityCutoffTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getScoreCutoffTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getkCoreTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getMaxDepthTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.TRAILING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(densityCutoffLabel)
							.addComponent(scoreCutoffLabel)
							.addComponent(kCoreLabel)
							.addComponent(maxDepthLabel)
					).addGroup(layout.createSequentialGroup()
							.addComponent(getHaircutCkb())
							.addComponent(getFluffCkb())
							.addComponent(getDensityCutoffTxt())
							.addComponent(getScoreCutoffTxt())
							.addComponent(getkCoreTxt())
							.addComponent(getMaxDepthTxt())
					)
			);
			
			updateClusterFindingPanel();
		}

		return clusterFindingPnl;
	}
	
	private JCheckBox getIncludeLoopsCkb() {
		if (includeLoopsCkb == null) {
			includeLoopsCkb = new JCheckBox("Include Loops");
			includeLoopsCkb.addItemListener(new IncludeLoopsCheckBoxAction());
			includeLoopsCkb.setToolTipText("<html>Self-edges may increase a<br>node's score slightly.</html>");
			includeLoopsCkb.setSelected(currentParamsCopy.getIncludeLoops());
		}
		
		return includeLoopsCkb;
	}
	
	private JFormattedTextField getDegreeCutoffTxt() {
		if (degreeCutoffTxt == null) {
			degreeCutoffTxt = new JFormattedTextField(decFormat);
			degreeCutoffTxt.setColumns(3);
			degreeCutoffTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			degreeCutoffTxt.addPropertyChangeListener("value", new FormattedTextFieldAction());
			degreeCutoffTxt.setToolTipText(
					"<html>Sets the minimum number of<br>edges for a node to be scored.</html>");
			degreeCutoffTxt.setText(String.valueOf(currentParamsCopy.getDegreeCutoff()));
		}
		
		return degreeCutoffTxt;
	}
	
	private JCheckBox getHaircutCkb() {
		if (haircutCkb == null) {
			haircutCkb = new JCheckBox("Haircut");
			haircutCkb.addItemListener(new MCODEMainPanel.HaircutCheckBoxAction());
			haircutCkb.setToolTipText("<html>Remove singly connected<br>nodes from clusters.</html>");
			haircutCkb.setSelected(currentParamsCopy.getHaircut());
		}
		
		return haircutCkb;
	}
	
	private JCheckBox getFluffCkb() {
		if (fluffCkb == null) {
			fluffCkb = new JCheckBox("Fluff");
			fluffCkb.addItemListener(new MCODEMainPanel.FluffCheckBoxAction());
			fluffCkb.setToolTipText(
					"<html>Expand core cluster by one neighbour shell<br>" +
					"(applied after the optional haircut).</html>");
			fluffCkb.setSelected(currentParamsCopy.getFluff());
		}
		
		return fluffCkb;
	}
	
	private JFormattedTextField getDensityCutoffTxt() {
		if (densityCutoffTxt == null) {
			densityCutoffTxt = new JFormattedTextField(new DecimalFormat("0.000"));
			densityCutoffTxt.setColumns(3);
			densityCutoffTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			densityCutoffTxt.addPropertyChangeListener("value", new MCODEMainPanel.FormattedTextFieldAction());
			densityCutoffTxt.setToolTipText(
					"<html>Limits fluffing by setting the acceptable<br>"
					+ "node density deviance from the core cluster<br>"
					+ "density (allows clusters' edges to overlap).</html>");
			densityCutoffTxt.setText((new Double(currentParamsCopy.getFluffNodeDensityCutoff())
					.toString()));
		}
		
		return densityCutoffTxt;
	}
	
	private JFormattedTextField getScoreCutoffTxt() {
		if (scoreCutoffTxt == null) {
			scoreCutoffTxt = new JFormattedTextField(new DecimalFormat("0.000"));
			scoreCutoffTxt.setColumns(3);
			scoreCutoffTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			scoreCutoffTxt.addPropertyChangeListener("value", new MCODEMainPanel.FormattedTextFieldAction());
			scoreCutoffTxt.setToolTipText(
					"<html>Sets the acceptable score deviance from<br>" +
					"the seed node's score for expanding a cluster<br>" +
					"(most influental parameter for cluster size).</html>");
			scoreCutoffTxt.setText((new Double(currentParamsCopy.getNodeScoreCutoff()).toString()));
		}
		
		return scoreCutoffTxt;
	}
	
	private JFormattedTextField getkCoreTxt() {
		if (kCoreTxt == null) {
			kCoreTxt = new JFormattedTextField(decFormat);
			kCoreTxt.setColumns(3);
			kCoreTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			kCoreTxt.addPropertyChangeListener("value", new MCODEMainPanel.FormattedTextFieldAction());
			kCoreTxt.setToolTipText(
					"<html>Filters out clusters lacking a<br>" +
					"maximally inter-connected core<br>" +
					"of at least k edges per node.</html>");
			kCoreTxt.setText(String.valueOf(currentParamsCopy.getKCore()));
		}
		
		return kCoreTxt;
	}
	
	private JFormattedTextField getMaxDepthTxt() {
		if (maxDepthTxt == null) {
			maxDepthTxt = new JFormattedTextField(decFormat);
			maxDepthTxt.setColumns(3);
			maxDepthTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			maxDepthTxt.addPropertyChangeListener("value", new MCODEMainPanel.FormattedTextFieldAction());
			maxDepthTxt.setToolTipText(
					"<html>Limits the cluster size by setting the<br>" +
					"maximum search distance from a seed<br>" +
					"node (100 virtually means no limit).</html>");
			maxDepthTxt.setText(String.valueOf(currentParamsCopy.getMaxDepthFromStart()));
		}
		
		return maxDepthTxt;
	}

	private void updateClusterFindingPanel() {
		if (densityCutoffLabel != null) densityCutoffLabel.setEnabled(currentParamsCopy.getFluff());
		getDensityCutoffTxt().setEnabled(currentParamsCopy.getFluff());
	}

	/**
	 * Handles the press of a scope option. Makes sure that appropriate advanced options
	 * inputs are added and removed depending on which scope is selected
	 */
	@SuppressWarnings("serial")
	private class ScopeAction extends AbstractAction {
		/*
		//TODO: Uncomment this action event handler when benchmarking is implemented, and delete the one below
		public void actionPerformed(ActionEvent e) {
		    String scope = e.getActionCommand();
		    if (scope.equals(MCODEParameterSet.NETWORK)) {
		        //We want to have a layered structure such that when network scope is selected, the cluster finding
		        //content allows the user to choose between optimize and customize.  When the other scopes are selected
		        //the user should only see the customize cluster parameters content.
		        //Here we ensured that these two contents are toggled depending on the scope selection.
		        clusterFindingPnl.getContentPane().remove(customizeClusterFindingContent);
		        //add content with 2 options
		        clusterFindingPnl.getContentPane().add(clusterFindingContent, BorderLayout.NORTH);
		        //need to re-add the customize content to its original container
		        customizeClusterFindingPanel.getContentPane().add(customizeClusterFindingContent, BorderLayout.NORTH);
		    } else {
		        //since only one option will be left, it must be selected so that its content is visible
		        customizeOption.setSelected(true);
		        //remove content with 2 options
		        clusterFindingPnl.getContentPane().remove(clusterFindingContent);
		        //add customize content; this automatically removes it from its original container
		        clusterFindingPnl.getContentPane().add(customizeClusterFindingContent, BorderLayout.NORTH);


		    }
		    currentParamsCopy.setScope(scope);
		}
		*/

		//TODO: Delete this ({...}) when benchmarking is implemented
		// TEMPORARY ACTION EVENT HANDLER {
		@Override
		public void actionPerformed(ActionEvent e) {
			String scope = e.getActionCommand();
			
			if (MCODEAnalysisScope.SELECTION.toString().equalsIgnoreCase(scope))
				currentParamsCopy.setScope(MCODEAnalysisScope.SELECTION);
			else
				currentParamsCopy.setScope(MCODEAnalysisScope.NETWORK);
		}
		// }
	}

//	/**
//	 * Sets the optimization parameter depending on which radio button is selected (cusomize/optimize)
//	 */
//	private class ClusterFindingAction extends AbstractAction {
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			currentParamsCopy.setOptimize(optimizeOption.isSelected());
//		}
//	}

	/**
	 * Handles setting of the include loops parameter
	 */
	private class IncludeLoopsCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			currentParamsCopy.setIncludeLoops(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}

	/**
	 * Handles setting for the text field parameters that are numbers.
	 * Makes sure that the numbers make sense.
	 */
	private class FormattedTextFieldAction implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			final JFormattedTextField source = (JFormattedTextField) e.getSource();

			String message = "The value you have entered is invalid.\n";
			boolean invalid = false;

			if (source == degreeCutoffTxt) {
				Number value = (Number) degreeCutoffTxt.getValue();
				
				if ((value != null) && (value.intValue() > 1)) {
					currentParamsCopy.setDegreeCutoff(value.intValue());
				} else {
					source.setValue(2);
					message += "The degree cutoff must be greater than 1.";
					invalid = true;
				}
			} else if (source == getScoreCutoffTxt()) {
				Number value = (Number) getScoreCutoffTxt().getValue();
				
				if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
					currentParamsCopy.setNodeScoreCutoff(value.doubleValue());
				} else {
					source.setValue(new Double(currentParamsCopy.getNodeScoreCutoff()));
					message += "The node score cutoff must be between 0 and 1.";
					invalid = true;
				}
			} else if (source == kCoreTxt) {
				Number value = (Number) kCoreTxt.getValue();
				
				if ((value != null) && (value.intValue() > 1)) {
					currentParamsCopy.setKCore(value.intValue());
				} else {
					source.setValue(2);
					message += "The K-Core must be greater than 1.";
					invalid = true;
				}
			} else if (source == maxDepthTxt) {
				Number value = (Number) maxDepthTxt.getValue();
				
				if ((value != null) && (value.intValue() > 0)) {
					currentParamsCopy.setMaxDepthFromStart(value.intValue());
				} else {
					source.setValue(1);
					message += "The maximum depth must be greater than 0.";
					invalid = true;
				}
			} else if (source == densityCutoffTxt) {
				Number value = (Number) densityCutoffTxt.getValue();
				
				if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
					currentParamsCopy.setFluffNodeDensityCutoff(value.doubleValue());
				} else {
					source.setValue(new Double(currentParamsCopy.getFluffNodeDensityCutoff()));
					message += "The fluff node density cutoff must be between 0 and 1.";
					invalid = true;
				}
			}
			
			if (invalid) {
				JOptionPane.showMessageDialog(swingApplication.getJFrame(),
											  message,
											  "Parameter out of bounds",
											  JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Handles setting of the haircut parameter
	 */
	private class HaircutCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			currentParamsCopy.setHaircut(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}

	/**
	 * Handles setting of the fluff parameter and showing or hiding of the fluff node density cutoff input
	 */
	private class FluffCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			currentParamsCopy.setFluff(e.getStateChange() != ItemEvent.DESELECTED);
			updateClusterFindingPanel();
		}
	}
}
