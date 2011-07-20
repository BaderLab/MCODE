package org.cytoscape.mcode.internal.view;

import java.awt.Color;

import javax.media.j3d.Interpolator;

import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

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
 * * Date: Dec 27, 2006
 * * Time: 1:20:19 PM
 * * Description: A visual style for MCODE modifying node shape and color
 */

/**
 * A visual style for MCODE modifying node shape and color.
 */
public class MCODEVisualStyle /*extends VisualStyle*/ { // TODO
    private double maxValue = 0.0;

    /**
     * Constructor for MCODE visual style
     *
     * @param name name dipsplayed in the vizmap select box
     */
    public MCODEVisualStyle (String name) {
//        super(name);
//        initCalculators();
    }

    /**
     * Reinitialazes the calculators.  This method is called whenver different results are selected
     * because they may have different node score attributes and may require a redrawing of shapes
     * and colors given the new maximum score.
     */
//    public void initCalculators() {
//        NodeAppearanceCalculator nac = new NodeAppearanceCalculator();
//
//        createNodeShape(nac);
//        createNodeColor(nac);
//
//        this.setNodeAppearanceCalculator(nac);
//    }
//
//    private void createNodeShape(NodeAppearanceCalculator nac) {
//        DiscreteMapping discreteMapping = new DiscreteMapping(RECT, "MCODE_Node_Status", ObjectMapping.NODE_MAPPING);
//        //Node shapes are determined by three discrete classifications
//        discreteMapping.putMapValue("Clustered", ELLIPSE);
//        discreteMapping.putMapValue("Seed", RECT);
//        discreteMapping.putMapValue("Unclustered", DIAMOND);
//
//        Calculator nodeShapeCalculator = new BasicCalculator("Seed and Cluster Status Calculator", discreteMapping, VisualPropertyType.NODE_SHAPE);
//        nac.setCalculator(nodeShapeCalculator);
//    }
//
//    private void createNodeColor(NodeAppearanceCalculator nac) {
//        nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR, Color.WHITE);
//        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
//        continuousMapping.setControllingAttributeName("MCODE_Score", null, false);
//
//        Interpolator fInt = new LinearNumberToColorInterpolator();
//        continuousMapping.setInterpolator(fInt);
//
//        //Node color is based on the score, the lower the score the darker the color
//        Color minColor = Color.BLACK;
//        Color maxColor = Color.RED;
//
//        //Create two boundary conditions
//        //First we state that everything below or equalling 0 (min) will be white, and everything above that will
//        //start from black and fade into the next boundary color
//        BoundaryRangeValues bv0 = new BoundaryRangeValues(Color.WHITE, Color.WHITE, minColor);
//        //Now we state that anything anything below the max score will fade into red from the lower boundary color
//        //and everything equal or greater than the max (never occurs since this is the upper boundary) will be red
//        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);
//
//        //Set Data Points
//        double minValue = 0.0;
//        //the max value is set by MCODEVisualStyleAction based on the current result set's max score
//        continuousMapping.addPoint(minValue, bv0);
//        continuousMapping.addPoint(maxValue, bv2);
//
//        Calculator nodeColorCalculator = new BasicCalculator("MCODE Score Color Calculator", continuousMapping, VisualPropertyType.NODE_FILL_COLOR);
//        nac.setCalculator(nodeColorCalculator);
//    }
//
//    public void setMaxValue(double maxValue) {
//        this.maxValue = maxValue;
//    }
}
