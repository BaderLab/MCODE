package org.cytoscape.mcode.internal.util;

import javax.swing.*;

import org.cytoscape.mcode.internal.model.MCODEAlgorithm;
import org.cytoscape.mcode.internal.model.MCODECluster;
import org.cytoscape.mcode.internal.view.MCODELoader;
import org.cytoscape.model.CyNetwork;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

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

/**
 * Utilities for MCODE
 */
public class MCODEUtil {

    private static boolean INTERRUPTED = false;
    private static Image placeHolderImage = null;

    /**
     * Convert a network to an image.  This is used by the MCODEResultsPanel.
     *
     * @param loader Graphic loader displaying progress and process
     * @param cluster Input network to convert to an image
     * @param height  Height that the resulting image should be
     * @param width   Width that the resulting image should be
     * @param layouter Reference to the layout algorithm
     * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
     * @return The resulting image
     */
//    public static ImageName convertNetworkToImage(MCODELoader loader, MCODECluster cluster, int height, int width, SpringEmbeddedLayouter layouter, boolean layoutNecessary) {
//        DGraphView view;
//        ImageName image;
//
//        //Progress reporters.  There are three basic tasks, the progress of each is calculated and then combined
//        //using the respective weighting to get an overall progress
//        //global progress
//        int weightSetupNodes = 20;  // setting up the nodes and edges is deemed as 25% of the whole task
//        int weightSetupEdges = 5;
//        int weightLayout = 75;      // layout it is 70%
//        int goalTotal = weightSetupNodes + weightSetupEdges;
//        if (layoutNecessary) {
//            goalTotal += weightLayout;
//        }
//        double progress = 0;        // keeps track of progress as a percent of the totalGoal
//
//        view = generateGraphView(cluster.getGPCluster());
//
//        for (Iterator in = view.getNodeViewsIterator(); in.hasNext();) {
//            if (INTERRUPTED) {
//                System.err.println("Interrupted: Node Setup");
//                //before we shortcircuit the method we reset the interruption so that the method can run without
//                //problems the next time around
//                if (layouter != null) layouter.resetDoLayout();
//                resetLoading();
//                return null;
//            }
//            
//            NodeView nv = (NodeView) in.next();
//
//            //Otherwise we give it new generic data
//            String label = nv.getNode().getIdentifier();
//            nv.getLabel().setText(label);
//            nv.setWidth(40);
//            nv.setHeight(40);
//            if (cluster.getSeedNode().intValue() == nv.getRootGraphIndex()) {
//                nv.setShape(NodeView.RECTANGLE);
//            } else {
//                nv.setShape(NodeView.ELLIPSE);
//            }
//            nv.setUnselectedPaint(Color.RED);
//            nv.setBorderPaint(Color.BLACK);
//
//            //First we check if the MCODECluster already has a node view of this node (posing the more generic condition
//            //first prevents the program from throwing a null pointer exception in the second condition)
//            if (cluster.getView() != null && cluster.getView().getNodeView(nv.getNode().getRootGraphIndex()) != null) {
//                //If it does, then we take the layout position that was already generated for it
//                nv.setXPosition(cluster.getView().getNodeView(nv.getNode().getRootGraphIndex()).getXPosition());
//                nv.setYPosition(cluster.getView().getNodeView(nv.getNode().getRootGraphIndex()).getYPosition());
//            } else {
//                //Otherwise, randomize node positions before layout so that they don't all layout in a line
//                //(so they don't fall into a local minimum for the SpringEmbedder)
//                //If the SpringEmbedder implementation changes, this code may need to be removed
//                //size is small for many default drawn graphs, thus +100
//                nv.setXPosition((view.getCanvas().getWidth() + 100) * Math.random());
//                nv.setYPosition((view.getCanvas().getHeight() + 100) * Math.random());
//                if (!layoutNecessary) {
//                    goalTotal += weightLayout;
//                    progress /= (goalTotal / (goalTotal - weightLayout));
//                    layoutNecessary = true;
//                }
//            }
//            if (loader != null) {
//                progress += 100.0 * (1.0 / (double) view.nodeCount()) * ((double) weightSetupNodes / (double) goalTotal);
//                loader.setProgress((int) progress, "Setup: nodes");
//            }
//        }
//
//        for (Iterator ie = view.getEdgeViewsIterator(); ie.hasNext();) {
//            if (INTERRUPTED) {
//                System.err.println("Interrupted: Edge Setup");
//                if (layouter != null) layouter.resetDoLayout();
//                resetLoading();
//                return null;
//            }
//            EdgeView ev = (EdgeView) ie.next();
//            ev.setUnselectedPaint(Color.BLUE);
//            ev.setTargetEdgeEnd(EdgeView.BLACK_ARROW);
//            ev.setTargetEdgeEndPaint(Color.CYAN);
//            ev.setSourceEdgeEndPaint(Color.CYAN);
//            ev.setStroke(new BasicStroke(5f));
//
//            if (loader != null) {
//                progress += 100.0 * (1.0 / (double) view.edgeCount()) * ((double) weightSetupEdges / (double) goalTotal);
//                loader.setProgress((int) progress, "Setup: edges");
//            }
//        }
//        if (layoutNecessary) {
//            if (layouter == null) {
//                layouter = new SpringEmbeddedLayouter();
//            }
//            layouter.setGraphView(view);
//            //The doLayout method should return true if the process completes without interruption
//            if (!layouter.doLayout(weightLayout, goalTotal, progress, loader)) {
//                //Otherwise, if layout is not completed, set the interruption to false, and return null, not an image
//                resetLoading();
//                return null;
//            }
//        }
//
//        view.getCanvas().setSize(width, height);
//        view.fitContent();
//        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        final Graphics2D g = (Graphics2D) image.getGraphics();
//        view.getCanvas().paint(g);
//
//        image = view.getCanvas(DGraphView.Canvas.NETWORK_CANVAS).getImage();
//
//        double largestSide = view.getCanvas().getWidth();
//        if (view.getCanvas().getHeight() > largestSide) {
//            largestSide = view.getCanvas().getHeight();
//        }
//        if (view.getNodeViewCount() >= 1) {
//            cluster.setView(view);
//        }
//        layouter.resetDoLayout();
//        resetLoading();
//        return (image);
//    }
//
//	private static DGraphView generateGraphView(GraphPerspective gp) {
//        DGraphView view = new DGraphView(gp);
//
//        final int[] nodes = gp.getNodeIndicesArray();
//        for (int i = 0; i < nodes.length; i++) {
//            view.addNodeView(nodes[i]);
//        }
//
//        final int[] edges = gp.getEdgeIndicesArray();
//        for (int i = 0; i < edges.length; i++) {
//            view.addEdgeView(edges[i]);
//        }
//
//		return view;
//	}
//
//    public static void interruptLoading() {
//        INTERRUPTED = true;
//    }
//
//    public static void resetLoading() {
//        INTERRUPTED = false;
//    }
//
//    /**
//     * Generates an image of a place holder saying "Too big to show".
//     *
//     * @param width width of the image
//     * @param height height of the image
//     * @return place holder
//     */
//    public static ImageName getPlaceHolderImage(int width, int height) {
//        //We only want to generate a place holder image once so that memory is not eaten up
//        if (placeHolderImage == null) {
//            ImageName image;
//            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g2 = (Graphics2D) image.getGraphics();
//            int fontSize = 10;
//            g2.setFont(new Font("Arial", Font.PLAIN, fontSize));
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//            Font f = g2.getFont();
//            FontMetrics fm = g2.getFontMetrics(f);
//
//            //Place Holder text
//            String placeHolderText = "Too big to show";
//            //We want to center the text vertically in the top 20 pixels
//            height = 20;
//            //White outline
//            g2.setColor(Color.WHITE);
//            g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) - 1, (height / 2) + (fontSize / 2) - 1);
//            g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) - 1, (height / 2) + (fontSize / 2) + 1);
//            g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) + 1, (height / 2) + (fontSize / 2) - 1);
//            g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) + 1, (height / 2) + (fontSize / 2) + 1);
//            //Red text
//            g2.setColor(Color.RED);
//            g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2), (height / 2) + (fontSize / 2));
//
//            placeHolderImage = image;
//        }
//        return placeHolderImage;
//    }
//
//    /**
//     * Convert a cluster generated by MCODE to a Cytoscape network.
//     *
//     * @param cluster       The MCODE cluster
//     * @param sourceNetwork The original network that contained the cluster
//     * @return The network representing the cluster
//     */
//    public static GraphPerspective convertClusterToNetwork(ArrayList cluster, CyNetwork sourceNetwork) {
//        GraphPerspective gpCluster;
//        int[] clusterArray = convertIntArrayList2array(cluster);
//        gpCluster = sourceNetwork.createGraphPerspective(clusterArray);
//        return gpCluster;
//    }
//
//    /**
//     * Converts a list of MCODE generated clusters to a list of networks that is sorted by the score of the cluster
//     *
//     * @param clusters   List of MCODE generated clusters
//     * @return A sorted array of cluster objects based on cluster score.
//     */
//    public static MCODECluster[] sortClusters(MCODECluster[] clusters) {
//        Arrays.sort(clusters, new Comparator() {
//            //sorting clusters by decreasing score
//            public int compare(Object o1, Object o2) {
//                double d1 = ((MCODECluster) o1).getClusterScore();
//                double d2 = ((MCODECluster) o2).getClusterScore();
//                if (d1 == d2) {
//                    return 0;
//                } else if (d1 < d2) {
//                    return 1;
//                } else {
//                    return -1;
//                }
//            }
//        });
//        return clusters;
//    }
//
//    /**
//     * A utility method to convert ArrayList to int[]
//     *
//     * @param alInput ArrayList input
//     * @return int array
//     */
//    private static int[] convertIntArrayList2array(ArrayList alInput) {
//        int[] outputNodeIndices = new int[alInput.size()];
//        int j = 0;
//        for (Iterator i = alInput.iterator(); i.hasNext(); j++) {
//            outputNodeIndices[j] = ((Integer) i.next()).intValue();
//        }
//        return (outputNodeIndices);
//    }
//
//    /**
//     * Utility method to get the names of all the nodes in a GraphPerspective
//     *
//     * @param gpInput The input graph perspective to get the names from
//     * @return A concatenated set of all node names (separated by a comma)
//     */
//    public static StringBuffer getNodeNameList(GraphPerspective gpInput) {
//        Iterator i = gpInput.nodesIterator();
//        StringBuffer sb = new StringBuffer();
//        while (i.hasNext()) {
//            Node node = (Node) i.next();
//            sb.append(node.getIdentifier());
//            if (i.hasNext()) {
//                sb.append(", ");
//            }
//        }
//        return (sb);
//    }
//
//    /**
//     * Save MCODE results to a file
//     *
//     * @param alg       The algorithm instance containing parameters, etc.
//     * @param clusters  The list of clusters
//     * @param network   The network source of the clusters
//     * @param fileName  The file name to write to
//     * @return True if the file was written, false otherwise
//     */
//	public static boolean exportMCODEResults(MCODEAlgorithm alg,
//											 MCODECluster[] clusters,
//											 CyNetwork network,
//											 String fileName) {
//        if (alg == null || clusters == null || network == null || fileName == null) {
//            return false;
//        }
//        String lineSep = System.getProperty("line.separator");
//        try {
//            File file = new File(fileName);
//            FileWriter fout = new FileWriter(file);
//            //write header
//            fout.write("MCODE Plugin Results" + lineSep);
//            fout.write("Date: " + DateFormat.getDateTimeInstance().format(new Date()) + lineSep + lineSep);
//            fout.write("Parameters:" + lineSep + alg.getParams().toString() + lineSep);
//            fout.write("Cluster	Score (Density*#Nodes)\tNodes\tEdges\tNode IDs" + lineSep);
//            //get GraphPerspectives for all clusters, score and rank them
//            //convert the ArrayList to an array of GraphPerspectives and sort it by cluster score
//            //GraphPerspective[] gpClusterArray = MCODEUtil.convertClusterListToSortedNetworkList(clusters, network, alg);
//            for (int i = 0; i < clusters.length; i++) {
//                GraphPerspective gpCluster = clusters[i].getGPCluster();
//                fout.write((i + 1) + "\t"); //rank
//                NumberFormat nf = NumberFormat.getInstance();
//                nf.setMaximumFractionDigits(3);
//                fout.write(nf.format(clusters[i].getClusterScore()) + "\t");
//                //cluster size - format: (# prot, # intx)
//                fout.write(gpCluster.getNodeCount() + "\t");
//                fout.write(gpCluster.getEdgeCount() + "\t");
//                //create a string of node names - this can be long
//                fout.write(getNodeNameList(gpCluster).toString() + lineSep);
//            }
//            fout.close();
//            return true;
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null, e.toString(),
//                    "Error Writing to \"" + fileName + "\"",
//                    JOptionPane.ERROR_MESSAGE);
//            return false;
//        }
//    }
}
