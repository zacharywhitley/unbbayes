/**
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.controller;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import javax.swing.*;


/**
 * Classe auxiliar da classe PrintPreviewer.
 */
public class PrintComponent extends JPanel {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	/**
   * The item to be printed
   */
  protected Printable printable;

  /**
   * PageFormat to use when printing
   */
  protected PageFormat pageFormat;

  /**
   * The page that is currently displayed
   */
  protected int displayPage;

  /**
   * The scale factor (1.0 = 100%)
   */
  protected double scaleFactor;

  public PrintComponent(Printable p, PageFormat pf) {
    setPrintable(p);
    setPageFormat(pf);
    setDisplayPage(0);
    setScaleFactor(100);
    setBackground(Color.white);
  }

  public void setPrintable(Printable p) {
    printable = p;
    revalidate();
  }

  public void setPageFormat(PageFormat pf) {
    pageFormat = pf;
    revalidate();
  }

  public void setDisplayPage(int page) {
    displayPage = page;
    revalidate();
  }

  public void setScaleFactor(double scale) {
    scaleFactor = scale;
    revalidate();
  }

  public double getScaleFactor() {
    return scaleFactor;
  }

  /**
   * Calculate the size of this component with the specified scale factor
   */
  public Dimension getSizeWithScale(double scale) {
    Insets insets = getInsets();
    int width = ((int)(pageFormat.getWidth() *
        scale / 100d)) +
        insets.left + insets.right;
    int height = ((int)(pageFormat.getHeight() *
        scale / 100d)) +
        insets.top + insets.bottom;
    return new Dimension(width, height);
  }

  public Dimension getPreferredSize() {
    return getSizeWithScale(scaleFactor);
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Paint this component, taking the scale factor into account and
   * sizing it appropriately.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    Rectangle clipRect = g2.getClipBounds();
    AffineTransform at = g2.getTransform();
    int x = (int)(pageFormat.getImageableX() *
        scaleFactor / 100d);
    int y = (int)(pageFormat.getImageableY() *
        scaleFactor / 100d);
    int w = (int)(pageFormat.getImageableWidth() *
        scaleFactor / 100d);
    int h = (int)(pageFormat.getImageableHeight() *
        scaleFactor / 100d);
    g2.clipRect(x, y, w, h);
    g2.scale(scaleFactor / 100, scaleFactor / 100);
    try {
      printable.print(g, pageFormat, displayPage);
    } catch (PrinterException pe) {};
    g2.setTransform(at);
    g2.setClip(clipRect);
  }

}
