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

package unbbayes.controlador;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.List;

/**
 * Classe utilizada para fazer a impressão de tabelas <code>JTable</code>
 */
public class PrintTable implements Printable, Pageable {

  private static final int SIZE = 15;

  /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controlador.resources.ControllerResources");

  protected List tables;
  protected List owners;
  protected PageFormat pageFormat;


  public PrintTable(List tbs, List owners, PageFormat pf) {
    tables = tbs;
    this.owners = owners;
    pageFormat = pf;
  }

  /**
   * Perform the printing here
   */
  public int print(Graphics g, PageFormat pf, int index) {
    Dimension size = null;
    JTable table;
    int positionX;
    int positionY;
    int pageIndex = 0;
    for (int i = 0; i < tables.size(); i++) {
        table = (JTable)tables.get(i);
        //  Get the table's preferred size
        if ((table.getWidth() == 0) || (table.getHeight() == 0)) {
          table.setSize(table.getPreferredSize());
        }
        int tableWidth = table.getWidth();
        int tableHeight = table.getHeight();
        positionX = 0;
        positionY = 0;

        //  Loop until we have printed the entire table
        while (positionY < tableHeight) {
          positionX = 0;
          while (positionX < tableWidth) {
            size = getPrintSize(table, positionX, positionY);
            if (pageIndex == index) {
              //  Paint as much of the table as will fit on a page
//              g.drawString("Nó: " + donos.get(i), positionX + (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY() + SIZE);
              paintTable(resource.getString("nodeName") + owners.get(i), table, g, positionX, positionY, size);
              return Printable.PAGE_EXISTS;
            }
            pageIndex++;
            positionX += size.width;
          }
          positionY += size.height;
        }
    }
    return Printable.NO_SUCH_PAGE;
  }

  /**
   * Calculate how much of the table will fit on a page without
   * causing a row or column to be split across two pages
   */
  protected Dimension getPrintSize(JTable table, int positionX, int positionY) {
    Rectangle rect;
    int printWidth;
    int printHeight;
    int firstCol = table.columnAtPoint(new Point(positionX, positionY));
    int firstRow = table.rowAtPoint(new Point(positionX, positionY));
    int maxWidth = (int)(pageFormat.getImageableWidth());
    int maxHeight = (int)(pageFormat.getImageableHeight()-(SIZE+10));

    int lastCol = table.columnAtPoint(
        new Point(positionX + maxWidth, positionY));
    if (lastCol == -1) {
      printWidth = table.getWidth() - positionX;
    }
    else {
      rect = table.getCellRect(0, lastCol - 1, true);
      printWidth = rect.x + rect.width - positionX;
    }

    int lastRow = table.rowAtPoint(new Point(
        positionX, positionY + maxHeight));
    if (lastRow == -1) {
      printHeight = table.getHeight() - positionY;
    }
    else {
      rect = table.getCellRect(lastRow - 1, 0, true);
      printHeight = rect.y + rect.height - positionY;
    }
    return new Dimension(printWidth, printHeight);
  }

  /**
   * Paint / print a portion of the table
   */
  protected void paintTable(String title, JTable table, Graphics g, int positionX, int positionY, Dimension size) {
    int offsetX = (int)(pageFormat.getImageableX());
    int offsetY = (int)(pageFormat.getImageableY());
    Shape lastClip = g.getClip();
    g.drawString(title, offsetX, offsetY + SIZE);
    g.translate(offsetX - positionX, offsetY - positionY + (SIZE+10));
    g.clipRect(positionX, positionY, size.width, size.height);
    table.paint(g);
    g.setClip(lastClip);
    g.translate(-(offsetX - positionX), -(offsetY - positionY + (SIZE+10)));
  }

  /**
   * Calculate the number of pages it will take to print the entire table
   */
  public int getNumberOfPages() {
    JTable table;
    Dimension size = null;
    int positionX;
    int positionY;
    int pageIndex = 0;

    for (int i = 0; i < tables.size(); i++) {
        table = (JTable)tables.get(i);
        if ((table.getWidth() == 0) || (table.getHeight() == 0)) {
          table.setSize(table.getPreferredSize());
        }
        int tableWidth = table.getWidth();
        int tableHeight = table.getHeight();
        positionX = 0;
        positionY = 0;
        while (positionY < tableHeight) {
          positionX = 0;
          while (positionX < tableWidth) {
            size = getPrintSize(table, positionX, positionY);
            positionX += size.width;
            pageIndex++;
          }
          positionY += size.height;
        }
    }
    return pageIndex;
  }

  public Printable getPrintable(int index) {
    return this;
  }

  public PageFormat getPageFormat(int index) {
    return pageFormat;
  }

}
