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

import java.awt.print.*;
import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Creating an instance of this class and printing it allows it to
 * add a status dialog during printing. The print requests are
 * simply delegated to the Pageable that actually contains the
 * data to be printed, but by intercepting those calls, we can update
 * the page number displayed in our dialog so that it indicates
 * which page is currently being displayed.
 */
public class PrintMonitor implements Pageable {

  protected PrinterJob printerJob;
  protected Pageable pageable;
  protected JOptionPane optionPane;
  protected JDialog statusDialog;

  /** Load resource file from this package */
  private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");

  public PrintMonitor(Pageable p) {
    pageable = p;
    printerJob = PrinterJob.getPrinterJob();
    String[] options = {resource.getString("cancelOption")};
    optionPane = new JOptionPane("",
        JOptionPane.INFORMATION_MESSAGE,
        JOptionPane.CANCEL_OPTION,
        null, options);
    statusDialog = optionPane.createDialog(null,
        resource.getString("printerStatus"));
  }

  /**
   * Create a new thread and have it call the print() method.
   * This ensures that the AWT event thread will be able to handle
   * the Cancel button if it is pressed, and can cancel the print job.
   */
  public void performPrint(boolean showDialog)
      throws PrinterException {
    printerJob.setPageable(this);
    if (showDialog) {
      boolean isOk = printerJob.printDialog();
      if (!isOk) return;
    }
    optionPane.setMessage(resource.getString("initializingPrinter"));
    Thread t = new Thread(new Runnable() {
      public void run() {
        statusDialog.setVisible(true);
        if (optionPane.getValue() !=
            JOptionPane.UNINITIALIZED_VALUE) {
          printerJob.cancel();
        }
      }
    });
    t.start();
    printerJob.print();
    statusDialog.setVisible(false);
  }

  public int getNumberOfPages() {
    return pageable.getNumberOfPages();
  }

  /*
   * Update our dialog message and delegate the getPrintable() call
   */
  public Printable getPrintable(int index) {
    optionPane.setMessage(resource.getString("printingPage") + (index + 1));
    return pageable.getPrintable(index);
  }

  public PageFormat getPageFormat(int index) {
    return pageable.getPageFormat(index);
  }
}
