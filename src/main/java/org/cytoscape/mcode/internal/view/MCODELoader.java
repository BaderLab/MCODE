package org.cytoscape.mcode.internal.view;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

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

/**
 * Generates a loading picture with a progress bar for the cluster browser table
 */
public class MCODELoader extends ImageIcon implements Runnable {
    
	private static final long serialVersionUID = 3709336791661338561L;
	
	JTable table; //cluster browser table reference
    int selectedRow; //row of cluster
    ImageIcon graphImage; //picture of the graph as is before loading
    BufferedImage onScreenLoader; //picture of loader
    BufferedImage offScreenLoader;
    Graphics2D onScreenG2; //graphics context of loader
    Graphics2D offScreenG2;
    Color bgColor; //background of selected cluster browser table cell
    Color fgColor; //color of foreground in the selected cluster browser table cell
    double fadeInAlpha; //global variable that allows the loader to fade in when it's first displayed
    int degreesForDisk; //global variable keeping track of the loading disk rotation
    int fontSize;

    int progress; //progress bar progress value
    String process; //current process being computed

    Thread t;
    boolean loading; // run switch
    boolean loaderDisplayed; //allows mcode to display a continous loading animation during continous exploration

    /**
     * Constructer for the loader.
     *
     * @param table Reference to the cluster browser table
     * @param width cell width
     * @param height cell height
     */
    MCODELoader (JTable table, int width, int height) {
        this.table = table;
        //First we get the tables colors and set the font
        bgColor = table.getSelectionBackground();
        fgColor = table.getSelectionForeground();
        fontSize = 8;

        //We create the on screen image which will always contain a completed picture for the table to paint whenever it can
        onScreenLoader = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onScreenG2 = (Graphics2D) onScreenLoader.getGraphics();

        //We also create the off screen image which we will use to compute the image and eventually transfer the completed image to the onscreen image
        offScreenLoader = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offScreenG2 = (Graphics2D) offScreenLoader.getGraphics();

        offScreenG2.setFont(new Font("Arial", Font.PLAIN, fontSize));
        offScreenG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //the on screen image is the one that this ImageIcon will display        
        this.setImage(onScreenLoader);

        loading = false;

        t = new Thread(this);
        
    }

    /**
     * Sets the row and network image on top of which the loader will be drawn as well as
     * progress to 0 and process to "Starting".
     *
     * @param selectedRow the row in a table where the loader is to be drawn
     * @param table an image of the cluster before the loader is drawn on top
     */
    public void setLoader(int selectedRow, JTable table) {
        this.selectedRow = selectedRow;
        graphImage = (ImageIcon) table.getValueAt(selectedRow, 0);
        fadeInAlpha = 0.0;
        degreesForDisk = 0;
        progress = 0;
        process = "Starting";

        loaderDisplayed = false;
        loading = true;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (loading) {
                    //First we make sure that if the loader is being displayed for the first time in this exploration
                    //session, then we wait half a second to make sure it isn't displayed for process that are really
                    //quick
                    if (loading && !loaderDisplayed) {
                        Thread.sleep(500);
                    }
                    //Then we compute the loading picture if loading is still taking place
                    if (loading) {
                        drawLoader();
                        //If the loader has not been displayed yet and loading is still taking place,
                        //we put it in the table
                        if (loading && !loaderDisplayed) {
                            table.setValueAt(this, selectedRow, 0);
                            loaderDisplayed = true;
                        }

                        //Since the table consolidates paint updates, the animation would not show up unless
                        //we implicitly force it to repaint
                        //This paintImmediately function causes problems in windows (paints on every component sometimes)
                        //Rectangle bounds = table.getCellRect(selectedRow, 0, false);
                        //if (loading) table.paintImmediately(bounds);
                        //This fireTableCellUpdated function seems to consolidate some painting too so it is not very responsive in windows
                        if (loading) ((AbstractTableModel) table.getModel()).fireTableCellUpdated(selectedRow, 0);
                        //Both work on the mac just fine
                    }
                }
                //This sleep time generates a ~30 fps animation
                Thread.sleep(30);
            }
        } catch (Exception e) {}
    }

    /**
     * Sets the run switch to false to stop the drawing process
     */
    public void loaded() {
        loading = false;
        loaderDisplayed = false;
    }

    /**
     * Initially, fades the graph into the background color, draws the Loading string and an animated disk to indicate
     * responsiveness as well as a progress bar and process status.
     */
    private void drawLoader() {
        //We want to compute this image off screen so that partially completed images are never displayed
        Graphics2D g2 = offScreenG2;
        BufferedImage loader = offScreenLoader;

        //Get font info for centering
        Font f = g2.getFont();
        FontMetrics fm = g2.getFontMetrics(f);

        //Clear the image
        g2.setColor(bgColor);
        g2.fillRect(0, 0, loader.getWidth(), loader.getHeight());

        //draw graph as is, centered in the table cell
        int graphX = Math.round((((float) loader.getWidth() - graphImage.getIconWidth()) / 2));
        int graphY = Math.round((((float) loader.getHeight() - graphImage.getIconHeight()) / 2));

        g2.drawImage(graphImage.getImage(), graphX, graphY, this.getImageObserver());

        //Fade background on top to make it look like the graph is fading away while the loading text fades in
        if ((int) (100.0 * fadeInAlpha) < 100) {
            fadeInAlpha += ((1.0 - fadeInAlpha) / 10.0);
        } else {
            fadeInAlpha = 1.0;
        }
        g2.setColor(bgColor);
        g2.setColor(new Color(
                g2.getColor().getRed(),
                g2.getColor().getGreen(),
                g2.getColor().getBlue(),
                (int) (200.0 * fadeInAlpha)
        ));
        g2.fillRect(0, 0, loader.getWidth(), loader.getHeight());

        //Loading animation
        int r = 20;//radius of the disk
        //To draw the animated rotating disk, we must create the Mask, or drawable area, in which we will
        //rotate a triangular polygon around a common center
        //the inner circle is subracted from the outter to create the disk area
        Ellipse2D circOutter = new Ellipse2D.Double((loader.getWidth() / 2) - r, (loader.getHeight() / 2) - r, 2 * r, 2 * r);
        Ellipse2D circInner = new Ellipse2D.Double((loader.getWidth() / 2) - (r / 2), (loader.getHeight() / 2) - (r / 2), r, r);

        Area circInnerArea = new Area(circInner);
        Area diskMask = new Area(circOutter);
        diskMask.subtract(circInnerArea);
        //with consecutive frames the disk must move around the circle
        //this variable keeps track of the overall disk
        if(degreesForDisk >= 360) {
            degreesForDisk = 0;
        }
        //To produce the fading effect we must draw consecutively more transparent polygons around the disk
        //this vriable keeps track of the fading polygons
        //at the start of each frame we want to first draw the least transparent polygon in the same position on the rotating kisk
        double degreesForTrail = degreesForDisk;
        //Here we find the center of the table cell
        Point2D center = new Point2D.Double(loader.getWidth() / 2.0, loader.getHeight() / 2.0);
        //these two points will represent the outer ends of the polygon, spinning around the center
        Point2D pointOnCircumference1 = new Point2D.Double();
        Point2D pointOnCircumference2 = new Point2D.Double();

        //these are the colors of the spinning disk
        Color markerLeadColor = Color.WHITE;
        Color markerTrailColor = Color.CYAN;
        //In order for the leading portion of the rotating disk to be one color and the trailing portion another we use
        //this weighting variable to slowly change the weighting of the two colors in finding the average between the two
        double markerLeadColorWeighting = 1.0;
        //this is the transparency of the initial polygon which is exponantially decremented to fade away the polygons
        double markerAlpha = 255 * fadeInAlpha;

        //In order for the polygons to cover the disk entirely, the outter polygon points must circle in an orbit that is further
        //than the disk itself, otherwise we would get a flat line between the two points and the resulting circle would not be smooth
        r = r + 10;
        //We will only draw the polygons as long as they are visible
        while (((int) markerAlpha) > 0) {
            //these are the radians of rotation of each of the polygon edges
            double theta1 = 2 * Math.PI * (degreesForTrail / 360);
            double theta2 = 2 * Math.PI * ((degreesForTrail - 6.0) / 360);//offset by 5 degrees
            //the outter spinning point locations can be determined by the circle equations here
            pointOnCircumference1.setLocation(((r * Math.cos(theta1))) + center.getX(), ((r * Math.sin(theta1))) + center.getY());
            pointOnCircumference2.setLocation(((r * Math.cos(theta2))) + center.getX(), ((r * Math.sin(theta2))) + center.getY());
            //this is the color with the decrementing alpha
            g2.setColor(new Color(
                    (int) ((markerLeadColor.getRed() * markerLeadColorWeighting) + (markerTrailColor.getRed() * (1.0 - markerLeadColorWeighting))),
                    (int) ((markerLeadColor.getGreen() * markerLeadColorWeighting) + (markerTrailColor.getGreen() * (1.0 - markerLeadColorWeighting))),
                    (int) ((markerLeadColor.getBlue() * markerLeadColorWeighting) + (markerTrailColor.getBlue() * (1.0 - markerLeadColorWeighting))),
                    (int) Math.rint(markerAlpha)
            ));
            if ((int) (markerLeadColorWeighting * 100) > 0) {
                markerLeadColorWeighting = markerLeadColorWeighting - (markerLeadColorWeighting / 40);
            }
            //polygons need arrays of x and y values to be drawn, so these are triangles consisting of a center point and two outter points
            int[] xs = {(int) center.getX(), (int) pointOnCircumference1.getX(), (int) pointOnCircumference2.getX()};
            int[] ys = {(int) center.getY(), (int) pointOnCircumference1.getY(), (int) pointOnCircumference2.getY()};
            //the triangle is intersected with the disk first
            Polygon marker = new Polygon(xs, ys, 3);
            Area markerMask = new Area(marker);
            markerMask.intersect(diskMask);
            //and drawn
            g2.fill(markerMask);
            //the alpha of the marker polygon is exponentially decreased
            markerAlpha = markerAlpha - (markerAlpha / 20);
            //the successive, more transparent marker is offset by 2 degrees backward to give the appearance of motion blur
            degreesForTrail -= 2;
        }
        //The successive disk is rotated 15 degrees for an optimal speed given the fps
        degreesForDisk += 15;

        //Loading text
        String loadingText = "LOADING";
        //White outline
        g2.setColor(Color.WHITE);
        g2.setColor(new Color(
                g2.getColor().getRed(),
                g2.getColor().getGreen(),
                g2.getColor().getBlue(),
                (int) (255.0 * fadeInAlpha)
        ));
        g2.drawString(loadingText, (loader.getWidth() / 2) - (fm.stringWidth(loadingText) / 2) - 1, (loader.getHeight() / 2) + (fontSize / 2) - 1);
        g2.drawString(loadingText, (loader.getWidth() / 2) - (fm.stringWidth(loadingText) / 2) - 1, (loader.getHeight() / 2) + (fontSize / 2) + 1);
        g2.drawString(loadingText, (loader.getWidth() / 2) - (fm.stringWidth(loadingText) / 2) + 1, (loader.getHeight() / 2) + (fontSize / 2) - 1);
        g2.drawString(loadingText, (loader.getWidth() / 2) - (fm.stringWidth(loadingText) / 2) + 1, (loader.getHeight() / 2) + (fontSize / 2) + 1);
        //Red text
        g2.setColor(Color.RED);
        g2.setColor(new Color(
                g2.getColor().getRed(),
                g2.getColor().getGreen(),
                g2.getColor().getBlue(),
                (int) (255.0 * fadeInAlpha)
        ));
        g2.drawString(loadingText, (loader.getWidth() / 2) - (fm.stringWidth(loadingText) / 2), (loader.getHeight() / 2) + (fontSize / 2));

        //Draw process text and progress bar and text
        //Process
        g2.setColor(fgColor);
        g2.drawString(process, 10, loader.getHeight() - 2);

        //Progress Bar Fill
        g2.setColor(Color.BLUE);
        //The transparency of the bar occilates between 100 and 150 using the sin function and the degrees of the rotating disk
        //completing one period with every turn of the disk
        int progressBarAlpha = 150 - (int) (50.0 * Math.abs(Math.sin(2.0 * Math.PI * (((double) degreesForDisk / 2) / 360.0))));
        g2.setColor(new Color(
                g2.getColor().getRed(),
                g2.getColor().getGreen(),
                g2.getColor().getBlue(),
                progressBarAlpha
        ));
        g2.fillRect(10, loader.getHeight() - 20, (int) ((((double) progress) / 100) * (loader.getWidth() - 20)), 10);

        //Progress Bar Outline
        g2.setColor(fgColor);
        g2.setColor(new Color(
                g2.getColor().getRed(),
                g2.getColor().getGreen(),
                g2.getColor().getBlue(),
                100
        ));
        g2.drawRect(10, loader.getHeight() - 20, loader.getWidth() - 20, 10);

        //Progress Text
        String progressText = progress + "%";
        //We add a black shadow to make it easier to read on top of any background
        g2.setColor(bgColor);
        g2.drawString(progressText, (loader.getWidth() / 2) - (fm.stringWidth(progressText) / 2) + 1, loader.getHeight() - 11);

        g2.setColor(fgColor);
        g2.drawString(progressText, (loader.getWidth() / 2) - (fm.stringWidth(progressText) / 2), loader.getHeight() - 12);

        //Lastly we paint the computed image onto the onscreen picture for display
        onScreenG2.drawImage(offScreenLoader, 0, 0, null);
    }

    /**
     * Sets progress and current process which are displayed in the loader.
     *
     * @param progress An integer from 0 - 100
     * @param process a short description of the process being conducted (initially set to "Starting")
     */
    public void setProgress(int progress, String process) {
        this.progress = progress;
        this.process = process;
    }

	public void start() {
		t.start();
	}
}
