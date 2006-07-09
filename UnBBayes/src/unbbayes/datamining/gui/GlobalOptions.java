package unbbayes.datamining.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import unbbayes.datamining.datamanipulation.Options;

public class GlobalOptions extends JInternalFrame
{
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  private JPanel contentPane;
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel7 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private JPanel jPanel6 = new JPanel();
  private JLabel jLabel3 = new JLabel();
  private JPanel jPanel5 = new JPanel();
  private JLabel jLabel2 = new JLabel();
  private JPanel jPanel4 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private GridLayout gridLayout1 = new GridLayout();
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel12 = new JPanel();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel14 = new JPanel();
  private JComboBox jComboBox2 = new JComboBox();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JPanel jPanel15 = new JPanel();
  private JPanel jPanel16 = new JPanel();
  private JPanel jPanel17 = new JPanel();
  private JComboBox jComboBox1 = new JComboBox();
  private GridLayout gridLayout4 = new GridLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel jPanel18 = new JPanel();
  private JPanel jPanel19 = new JPanel();
  private JLabel jLabel4 = new JLabel();
  private BorderLayout borderLayout8 = new BorderLayout();
  private GridLayout gridLayout5 = new GridLayout();
  private JTextField jTextField1 = new JTextField();
  private JPanel jPanel9 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private JPanel jPanel20 = new JPanel();
  private JPanel jPanel21 = new JPanel();
  private JPanel jPanel22 = new JPanel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private JTextField jTextField2 = new JTextField();

  public GlobalOptions()
  {   super("Global Options",false,true,true,true);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try
      {   jbInit();
      }
      catch(Exception e)
      {   e.printStackTrace();
      }
  }

  /**Component initialization*/
  private void jbInit() throws Exception
  { contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(400,300);
    jPanel1.setLayout(gridLayout2);
    gridLayout2.setColumns(2);
    gridLayout2.setHgap(5);
    gridLayout2.setRows(4);
    gridLayout2.setVgap(5);
    jPanel7.setLayout(borderLayout7);
    jPanel6.setLayout(gridLayout1);
    jLabel3.setText("Default Language");
    jPanel5.setLayout(borderLayout3);
    jLabel2.setText("Default Look and Feel");
    jPanel4.setLayout(gridLayout3);
    jLabel1.setText("Maximum number of states allowed");
    jPanel3.setLayout(borderLayout5);
    jPanel2.setLayout(gridLayout4);
    jButton1.setMaximumSize(new Dimension(80, 27));
    jButton1.setMinimumSize(new Dimension(80, 27));
    jButton1.setPreferredSize(new Dimension(80, 27));
    jButton1.setMnemonic('C');
    jButton1.setText("Cancel");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton2.setMaximumSize(new Dimension(80, 27));
    jButton2.setMinimumSize(new Dimension(80, 27));
    jButton2.setPreferredSize(new Dimension(80, 27));
    jButton2.setMnemonic('O');
    jButton2.setText("OK");
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    gridLayout1.setRows(3);
    gridLayout3.setRows(3);
    jPanel14.setLayout(borderLayout4);
    gridLayout4.setRows(3);
    jPanel16.setLayout(borderLayout6);
    jLabel4.setText("Minimum amount of instances to show confidence intervals");
    jPanel18.setLayout(borderLayout8);
    jPanel19.setLayout(gridLayout5);
    gridLayout5.setRows(3);
    jTextField1.setText("40");
    jTextField1.setHorizontalAlignment(SwingConstants.RIGHT);
    jPanel11.setLayout(borderLayout2);
    jPanel22.setLayout(borderLayout9);
    jTextField2.setText("100");
    jTextField2.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jPanel7, null);
    jPanel7.add(jLabel1, BorderLayout.CENTER);
    jPanel1.add(jPanel19, null);
    jPanel19.add(jPanel9, null);
    jPanel19.add(jPanel11, null);
    jPanel11.add(jTextField1, BorderLayout.CENTER);
    jPanel19.add(jPanel10, null);
    jPanel1.add(jPanel18, null);
    jPanel18.add(jLabel4, BorderLayout.CENTER);
    jPanel1.add(jPanel6, null);
    jPanel6.add(jPanel20, null);
    jPanel6.add(jPanel22, null);
    jPanel22.add(jTextField2,  BorderLayout.CENTER);
    jPanel6.add(jPanel21, null);
    jPanel1.add(jPanel5, null);
    jPanel5.add(jLabel2, BorderLayout.CENTER);
    jPanel1.add(jPanel4, null);
    jPanel4.add(jPanel13, null);
    jPanel4.add(jPanel14, null);
    jPanel14.add(jComboBox2, BorderLayout.CENTER);
    jPanel4.add(jPanel12, null);
    jPanel1.add(jPanel3, null);
    jPanel3.add(jLabel3, BorderLayout.CENTER);
    jPanel1.add(jPanel2, null);
    jPanel2.add(jPanel17, null);
    jPanel2.add(jPanel16, null);
    jPanel16.add(jComboBox1, BorderLayout.CENTER);
    jPanel2.add(jPanel15, null);
    contentPane.add(jPanel8,  BorderLayout.SOUTH);
    jPanel8.add(jButton2, null);
    jPanel8.add(jButton1, null);
    jComboBox2.addItem("Metal");
    jComboBox2.addItem("Motif");
    jComboBox2.addItem("Windows");
    jComboBox1.addItem("English");
    jComboBox1.addItem("Portuguese");
  }

  public void setDefaultOptions(int states,int confidence,String language,String laf)
  { jTextField1.setText(""+states);
    jTextField2.setText(""+confidence);
    jComboBox2.setSelectedItem(laf);
    jComboBox1.setSelectedItem(language);
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   try
      {   int numStates = Integer.parseInt(jTextField1.getText());
          int confidenceLimit = Integer.parseInt(jTextField2.getText());
          Options.getInstance().setNumberStatesAllowed(numStates);
          Options.getInstance().setConfidenceLimit(confidenceLimit);
          PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("DataMining.ini")),true);
          pw.println("[data mining]");
          pw.println("Maximum states = "+numStates);
          pw.println("Confidence limit = "+confidenceLimit);
          pw.println("Language = "+jComboBox1.getSelectedItem().toString());
          pw.println("Look and Feel = "+jComboBox2.getSelectedItem().toString());
          dispose();
      }
      catch (IOException ioe)
      {   JOptionPane.showConfirmDialog(this,"Error writing file = "+ioe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception ex)
      {   JOptionPane.showConfirmDialog(this,"Error = "+ex.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
      }
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   dispose();
  }
}