package ca.utoronto.tdccbr.mcode.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
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
@SuppressWarnings("serial")
public class NewAnalysisPanel extends JPanel {

	private final JLabel scopeLabel = new JLabel("Find Clusters:");
	private final JLabel degreeCutoffLabel = new JLabel("Degree Cutoff:");
	private final JLabel densityCutoffLabel = new JLabel("Node Density Cutoff:");
	private final JLabel scoreCutoffLabel = new JLabel("Node Score Cutoff:");
	private final JLabel kCoreLabel = new JLabel("K-Core:");
	private final JLabel maxDepthLabel = new JLabel("Max. Depth:");
	
	private JPanel scopePnl;
	private JPanel advancedOptionsPnl;
	private JPanel networkScoringPnl;
	private JPanel clusterFindingPnl;
	private JCheckBox includeLoopsCkb;
	private JFormattedTextField degreeCutoffTxt;
	private JFormattedTextField kCoreTxt;
	private JFormattedTextField scoreCutoffTxt;
	private JCheckBox haircutCkb;
	private JCheckBox fluffCkb;
	private JFormattedTextField densityCutoffTxt;
	private JFormattedTextField maxDepthTxt;

	private final MCODEParameters parameters; // stores current parameters - populates panel fields
	private final DecimalFormat decFormat; // used in the formatted text fields
	
	public NewAnalysisPanel(MCODEUtil mcodeUtil) {
		if (isAquaLAF())
			setOpaque(false);

		// get the current parameters
		parameters = mcodeUtil.getParameterManager().getLiveParams();

		decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(true);

		makeSmall(scopeLabel, degreeCutoffLabel, densityCutoffLabel, scoreCutoffLabel, kCoreLabel, maxDepthLabel);
		equalizeSize(scopeLabel, degreeCutoffLabel, densityCutoffLabel, scoreCutoffLabel, kCoreLabel, maxDepthLabel);
		
		// Create the three main panels: scope, advanced options, and bottom
		// Add all the vertically aligned components to the main panel
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getScopePnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAdvancedOptionsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getScopePnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAdvancedOptionsPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		scopeLabel.setHorizontalAlignment(JLabel.RIGHT);
		degreeCutoffLabel.setHorizontalAlignment(JLabel.RIGHT);
		densityCutoffLabel.setHorizontalAlignment(JLabel.RIGHT);
		scoreCutoffLabel.setHorizontalAlignment(JLabel.RIGHT);
		kCoreLabel.setHorizontalAlignment(JLabel.RIGHT);
		maxDepthLabel.setHorizontalAlignment(JLabel.RIGHT);
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

			JRadioButton netScopeBtn = new JRadioButton(MCODEAnalysisScope.NETWORK.toString(),
					parameters.getScope() == MCODEAnalysisScope.NETWORK);
			JRadioButton selScopeBtn = new JRadioButton(MCODEAnalysisScope.SELECTION.toString(),
					parameters.getScope() == MCODEAnalysisScope.SELECTION);
			
			makeSmall(netScopeBtn, selScopeBtn);
			
			netScopeBtn.setActionCommand(MCODEAnalysisScope.NETWORK.name());
			selScopeBtn.setActionCommand(MCODEAnalysisScope.SELECTION.name());

			netScopeBtn.addActionListener(new ScopeAction());
			selScopeBtn.addActionListener(new ScopeAction());

			ButtonGroup scopeOptions = new ButtonGroup();
			scopeOptions.add(netScopeBtn);
			scopeOptions.add(selScopeBtn);
			
			scopeLabel.setMinimumSize(netScopeBtn.getMinimumSize());
			
			GroupLayout layout = new GroupLayout(scopePnl);
			scopePnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(scopeLabel)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(netScopeBtn)
							.addComponent(selScopeBtn)
					)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(scopeLabel)
					).addGroup(layout.createSequentialGroup()
							.addComponent(netScopeBtn)
							.addComponent(selScopeBtn)
					)
			);
		}

		return scopePnl;
	}

	private JPanel getAdvancedOptionsPnl() {
		if (advancedOptionsPnl == null) {
			advancedOptionsPnl = new JPanel();
			advancedOptionsPnl.setBorder(createTitledBorder("Advanced Options"));
			
			if (isAquaLAF())
				advancedOptionsPnl.setOpaque(false);
			
			GroupLayout layout = new GroupLayout(advancedOptionsPnl);
			advancedOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getNetworkScoringPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getClusterFindingPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getNetworkScoringPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getClusterFindingPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
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
			
			degreeCutoffLabel.setMinimumSize(getDegreeCutoffTxt().getMinimumSize());
			degreeCutoffLabel.setToolTipText(getDegreeCutoffTxt().getToolTipText());

			GroupLayout layout = new GroupLayout(networkScoringPnl);
			networkScoringPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
							.addComponent(degreeCutoffLabel)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
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
			
			densityCutoffLabel.setMinimumSize(getDensityCutoffTxt().getMinimumSize());
			densityCutoffLabel.setToolTipText(getDensityCutoffTxt().getToolTipText());
			
			scoreCutoffLabel.setMinimumSize(getScoreCutoffTxt().getMinimumSize());
			scoreCutoffLabel.setToolTipText(getScoreCutoffTxt().getToolTipText());
			
			kCoreLabel.setMinimumSize(getScoreCutoffTxt().getMinimumSize());
			kCoreLabel.setToolTipText(getScoreCutoffTxt().getToolTipText());
			
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
			includeLoopsCkb.setSelected(parameters.getIncludeLoops());
			makeSmall(includeLoopsCkb);
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
			degreeCutoffTxt.setText(String.valueOf(parameters.getDegreeCutoff()));
			makeSmall(degreeCutoffTxt);
		}
		
		return degreeCutoffTxt;
	}
	
	private JCheckBox getHaircutCkb() {
		if (haircutCkb == null) {
			haircutCkb = new JCheckBox("Haircut");
			haircutCkb.addItemListener(new NewAnalysisPanel.HaircutCheckBoxAction());
			haircutCkb.setToolTipText("<html>Remove singly connected<br>nodes from clusters.</html>");
			haircutCkb.setSelected(parameters.getHaircut());
			makeSmall(haircutCkb);
		}
		
		return haircutCkb;
	}
	
	private JCheckBox getFluffCkb() {
		if (fluffCkb == null) {
			fluffCkb = new JCheckBox("Fluff");
			fluffCkb.addItemListener(new NewAnalysisPanel.FluffCheckBoxAction());
			fluffCkb.setToolTipText(
					"<html>Expand core cluster by one neighbour shell<br>" +
					"(applied after the optional haircut).</html>");
			fluffCkb.setSelected(parameters.getFluff());
			makeSmall(fluffCkb);
		}
		
		return fluffCkb;
	}
	
	private JFormattedTextField getDensityCutoffTxt() {
		if (densityCutoffTxt == null) {
			densityCutoffTxt = new JFormattedTextField(new DecimalFormat("0.000"));
			densityCutoffTxt.setColumns(3);
			densityCutoffTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			densityCutoffTxt.addPropertyChangeListener("value", new NewAnalysisPanel.FormattedTextFieldAction());
			densityCutoffTxt.setToolTipText(
					"<html>Limits fluffing by setting the acceptable<br>"
					+ "node density deviance from the core cluster<br>"
					+ "density (allows clusters' edges to overlap).</html>");
			densityCutoffTxt.setText((new Double(parameters.getFluffNodeDensityCutoff())
					.toString()));
			makeSmall(densityCutoffTxt);
		}
		
		return densityCutoffTxt;
	}
	
	private JFormattedTextField getScoreCutoffTxt() {
		if (scoreCutoffTxt == null) {
			scoreCutoffTxt = new JFormattedTextField(new DecimalFormat("0.000"));
			scoreCutoffTxt.setColumns(3);
			scoreCutoffTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			scoreCutoffTxt.addPropertyChangeListener("value", new NewAnalysisPanel.FormattedTextFieldAction());
			scoreCutoffTxt.setToolTipText(
					"<html>Sets the acceptable score deviance from<br>" +
					"the seed node's score for expanding a cluster<br>" +
					"(most influental parameter for cluster size).</html>");
			scoreCutoffTxt.setText((new Double(parameters.getNodeScoreCutoff()).toString()));
			makeSmall(scoreCutoffTxt);
		}
		
		return scoreCutoffTxt;
	}
	
	private JFormattedTextField getkCoreTxt() {
		if (kCoreTxt == null) {
			kCoreTxt = new JFormattedTextField(decFormat);
			kCoreTxt.setColumns(3);
			kCoreTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			kCoreTxt.addPropertyChangeListener("value", new NewAnalysisPanel.FormattedTextFieldAction());
			kCoreTxt.setToolTipText(
					"<html>Filters out clusters lacking a<br>" +
					"maximally inter-connected core<br>" +
					"of at least k edges per node.</html>");
			kCoreTxt.setText(String.valueOf(parameters.getKCore()));
			makeSmall(kCoreTxt);
		}
		
		return kCoreTxt;
	}
	
	private JFormattedTextField getMaxDepthTxt() {
		if (maxDepthTxt == null) {
			maxDepthTxt = new JFormattedTextField(decFormat);
			maxDepthTxt.setColumns(3);
			maxDepthTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			maxDepthTxt.addPropertyChangeListener("value", new NewAnalysisPanel.FormattedTextFieldAction());
			maxDepthTxt.setToolTipText(
					"<html>Limits the cluster size by setting the<br>" +
					"maximum search distance from a seed<br>" +
					"node (100 virtually means no limit).</html>");
			maxDepthTxt.setText(String.valueOf(parameters.getMaxDepthFromStart()));
			makeSmall(maxDepthTxt);
		}
		
		return maxDepthTxt;
	}

	private void updateClusterFindingPanel() {
		if (densityCutoffLabel != null)
			densityCutoffLabel.setEnabled(parameters.getFluff());
		
		getDensityCutoffTxt().setEnabled(parameters.getFluff());
	}

	/**
	 * Handles the press of a scope option. Makes sure that appropriate advanced options
	 * inputs are added and removed depending on which scope is selected
	 */
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
		    parameters.setScope(scope);
		}
		*/

		//TODO: Delete this ({...}) when benchmarking is implemented
		// TEMPORARY ACTION EVENT HANDLER {
		@Override
		public void actionPerformed(ActionEvent e) {
			String scope = e.getActionCommand();
			
			if (MCODEAnalysisScope.SELECTION.name().equalsIgnoreCase(scope))
				parameters.setScope(MCODEAnalysisScope.SELECTION);
			else
				parameters.setScope(MCODEAnalysisScope.NETWORK);
		}
		// }
	}

//	/**
//	 * Sets the optimization parameter depending on which radio button is selected (cusomize/optimize)
//	 */
//	private class ClusterFindingAction extends AbstractAction {
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			parameters.setOptimize(optimizeOption.isSelected());
//		}
//	}

	/**
	 * Handles setting of the include loops parameter
	 */
	private class IncludeLoopsCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			parameters.setIncludeLoops(e.getStateChange() != ItemEvent.DESELECTED);
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
					parameters.setDegreeCutoff(value.intValue());
				} else {
					source.setValue(2);
					message += "The degree cutoff must be greater than 1.";
					invalid = true;
				}
			} else if (source == getScoreCutoffTxt()) {
				Number value = (Number) getScoreCutoffTxt().getValue();
				
				if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
					parameters.setNodeScoreCutoff(value.doubleValue());
				} else {
					source.setValue(new Double(parameters.getNodeScoreCutoff()));
					message += "The node score cutoff must be between 0 and 1.";
					invalid = true;
				}
			} else if (source == kCoreTxt) {
				Number value = (Number) kCoreTxt.getValue();
				
				if ((value != null) && (value.intValue() > 1)) {
					parameters.setKCore(value.intValue());
				} else {
					source.setValue(2);
					message += "The K-Core must be greater than 1.";
					invalid = true;
				}
			} else if (source == maxDepthTxt) {
				Number value = (Number) maxDepthTxt.getValue();
				
				if ((value != null) && (value.intValue() > 0)) {
					parameters.setMaxDepthFromStart(value.intValue());
				} else {
					source.setValue(1);
					message += "The maximum depth must be greater than 0.";
					invalid = true;
				}
			} else if (source == densityCutoffTxt) {
				Number value = (Number) densityCutoffTxt.getValue();
				
				if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
					parameters.setFluffNodeDensityCutoff(value.doubleValue());
				} else {
					source.setValue(new Double(parameters.getFluffNodeDensityCutoff()));
					message += "The fluff node density cutoff must be between 0 and 1.";
					invalid = true;
				}
			}
			
			if (invalid)
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(NewAnalysisPanel.this),
											  message,
											  "Parameter out of bounds",
											  JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Handles setting of the haircut parameter
	 */
	private class HaircutCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			parameters.setHaircut(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}

	/**
	 * Handles setting of the fluff parameter and showing or hiding of the fluff node density cutoff input
	 */
	private class FluffCheckBoxAction implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			parameters.setFluff(e.getStateChange() != ItemEvent.DESELECTED);
			updateClusterFindingPanel();
		}
	}
}
