package unbbayes.metaphor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class MetaphorFrame extends JFrame{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  MetaphorMain jPanel1 = new MetaphorMain();
  BorderLayout borderLayout1 = new BorderLayout();
  public MetaphorFrame() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(screenSize);
    this.setTitle("Metáfora Médica");

    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
  }

  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      System.exit(0);
    }
  }

  public static void main(String[] args)
  {
    new MetaphorFrame().setVisible(true);
  }
}