package unbbayes.datamining.gui.c45;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;

/** 
 * Dialog window where C4.5 options are set.
 * 
 * 	@author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class DecisionTreeOptions extends JDialog
{ 
  /** Internacionalization resource */
  private ResourceBundle resource;
  /** Check box to set if gain ratio is used or not */
  private JCheckBox gainRatioCheckBox;
  /** Check box to set if prunning is used or not */
  private JCheckBox prunningCheckBox;
  /** Text field where verbosity value is typed  */
  private JTextField verbosityValueLabel;
  /** buttons to change verbosity value */
  private JScrollBar verbosityScroll;
  /** Text field where confidence value is typed */
  private JTextField confidenceValueLabel;
  /** buttons to change confidence value */ 
  private JScrollBar confidenceScroll;
  
  //----------------------------------------------------------------------//
  
  /** Default constructor. */
  public DecisionTreeOptions()
  {
  	resource = ResourceBundle.getBundle("unbbayes.datamining.gui.c45.resources.DecisiontreeResource");
	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	this.setTitle(resource.getString("preferences"));
	this.setSize(220,170);
	this.setResizable(false);
	JPanel contentPane = (JPanel) this.getContentPane();
	contentPane.setLayout(new BorderLayout());
	
	//customized layouts
	FlowLayout leftFlowLayout = new FlowLayout();
	leftFlowLayout.setAlignment(FlowLayout.LEFT);
	FlowLayout rightFlowLayout = new FlowLayout();
	rightFlowLayout.setAlignment(FlowLayout.RIGHT);
			
	//NORTH PANEL: PANEL WITH NUMERIC OPTIONS
	GridLayout northLayout = new GridLayout();
	JPanel northPanel = new JPanel();
	northPanel.setLayout(northLayout);
	northLayout.setColumns(2);
	northLayout.setRows(2);
	contentPane.add(northPanel,"North");
	
	//verbosity option label
	JPanel verbosityLabelPanel = new JPanel();
	verbosityLabelPanel.setLayout(leftFlowLayout);	
	JLabel verbosityLabel = new JLabel(resource.getString("verbosityLevel"));
	verbosityLabel.setPreferredSize(new Dimension(100,20));
	verbosityLabelPanel.add(verbosityLabel);
	northPanel.add(verbosityLabelPanel);
	
	//verbosity option text field
	int verbosityValue = Options.getInstance().getVerbosityLevel();
	JPanel verbosityFieldPanel = new JPanel();
	verbosityFieldPanel.setLayout(leftFlowLayout);
	verbosityValueLabel = new JTextField(verbosityValue+"    ");
	verbosityValueLabel.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		fixVerbosityValues();
	  }
	});
	verbosityValueLabel.addFocusListener(new java.awt.event.FocusAdapter()
	{
	  public void focusLost(FocusEvent e)
	  {
		fixVerbosityValues();
	  }
	});
	
	//verbosity scroll
	verbosityScroll = new JScrollBar();
	verbosityScroll.setMinimum(1);
	verbosityScroll.setMaximum(4);
	verbosityScroll.setVisibleAmount(0);
	verbosityScroll.setValue(5-verbosityValue);
	verbosityScroll.setPreferredSize(new Dimension(16, 20));
	verbosityScroll.addAdjustmentListener(new AdjustmentListener()
	{
		public void adjustmentValueChanged(AdjustmentEvent e)
		{
			verbosityValueLabel.setText((5-e.getValue())+"");
		}
	});
	northPanel.add(verbosityFieldPanel);
	verbosityFieldPanel.add(verbosityValueLabel);
	verbosityFieldPanel.add(verbosityScroll);
	
	//confidence option label
	JPanel confidenceLabelPanel = new JPanel();
	confidenceLabelPanel.setLayout(leftFlowLayout);	
	JLabel confidenceLabel = new JLabel(resource.getString("confidenceLevel"));
	confidenceLabel.setPreferredSize(new Dimension(100,20));
	confidenceLabelPanel.add(confidenceLabel);
	northPanel.add(confidenceLabelPanel);
	
	//confidence option text field
	int confidenceValue = (int)(Options.getInstance().getConfidenceLevel()*100);
	JPanel confidenceFieldPanel = new JPanel();
	confidenceFieldPanel.setLayout(leftFlowLayout);
	confidenceValueLabel = new JTextField(confidenceValue+"    ");
	confidenceValueLabel.setEnabled(Options.getInstance().getIfUsingPrunning());
	confidenceValueLabel.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		fixConfidenceValues();
	  }
	});
	confidenceValueLabel.addFocusListener(new java.awt.event.FocusAdapter()
	{
	  public void focusLost(FocusEvent e)
	  {
		fixConfidenceValues();
	  }
	});
	
	//confidence scroll
	confidenceScroll = new JScrollBar();
	confidenceScroll.setMinimum(1);
	confidenceScroll.setMaximum(99);
	confidenceScroll.setVisibleAmount(0);
	confidenceScroll.setValue(100-confidenceValue);
	confidenceScroll.setPreferredSize(new Dimension(16, 20));
	confidenceScroll.setEnabled(Options.getInstance().getIfUsingPrunning());
	confidenceScroll.addAdjustmentListener(new AdjustmentListener()
	{
		public void adjustmentValueChanged(AdjustmentEvent e)
		{
			confidenceValueLabel.setText((100-e.getValue())+"");
		}
	});
	northPanel.add(confidenceFieldPanel);
	confidenceFieldPanel.add(confidenceValueLabel);
	confidenceFieldPanel.add(confidenceScroll);
	confidenceFieldPanel.add(new JLabel("%"));
		
	//CENTER PANEL: PANEL WITH CHECKED OPTIONS
	GridLayout centerLayout = new GridLayout();
	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(centerLayout);
	centerLayout.setColumns(1);
	centerLayout.setRows(2);
	contentPane.add(centerPanel,"Center");
	
	//gain ratio check box
	gainRatioCheckBox = new JCheckBox(resource.getString("gainRatio"));
	gainRatioCheckBox.setSelected(Options.getInstance().getIfUsingGainRatio());
	centerPanel.add(gainRatioCheckBox);
	
	//prunning check box
	prunningCheckBox = new JCheckBox(resource.getString("prunning"));
	prunningCheckBox.setSelected(Options.getInstance().getIfUsingPrunning());
	prunningCheckBox.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			confidenceScroll.setEnabled(prunningCheckBox.isSelected());
			confidenceValueLabel.setEnabled(prunningCheckBox.isSelected());
			
		}
	});
	centerPanel.add(prunningCheckBox);
	
	//SOUTH PANEL: PANEL WITH BUTTONS 
	JPanel southPanel = new JPanel();
	southPanel.setLayout(new FlowLayout());
	contentPane.add(southPanel,"South");
	
	//button ok
	JButton buttonOK = new JButton("OK");
	buttonOK.setMnemonic('O');	
	buttonOK.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		Options.getInstance().setIfUsingGainRatio(gainRatioCheckBox.isSelected());
		Options.getInstance().setIfUsingPrunning(prunningCheckBox.isSelected());
		Options.getInstance().setVerbosityLevel(Integer.parseInt(verbosityValueLabel.getText()));
		Options.getInstance().setConfidenceLevel(Float.parseFloat(confidenceValueLabel.getText())/100f);
		
		dispose();
	  }
	});
	southPanel.add(buttonOK);
	
	//button cancel
	JButton buttonCancel = new JButton(resource.getString("cancel"));
	buttonCancel.setMnemonic('C');
	buttonCancel.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		dispose();
	  }
	});
	southPanel.add(buttonCancel);
	  	
	//center screen
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension frameSize = this.getSize();
	if (frameSize.height > screenSize.height) 
	{
		frameSize.height = screenSize.height;
	}
	if (frameSize.width > screenSize.width) 
	{
		 frameSize.width = screenSize.width;
	}
	this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
  }
  
  //-----------------------------------------------------------------------------//
  
  /** updates verbosity scroll when verbosity text field is set */
  private void fixVerbosityValues()
  {
	String text = verbosityValueLabel.getText();
	int i;
	for(i=0;i<text.length();i++)
	{
		if (!Character.isDigit(text.charAt(i)))
		{
			break;
		}
	}
	
	//if text field was set incorrectly, it is rewritten
	if((text.length()!=i)||(text.length()==0))
	{
		verbosityValueLabel.setText((5-verbosityScroll.getValue())+"");
	}
	else 
	{
		int value = Integer.parseInt(text);
		verbosityScroll.setValue(5-value);
	}
  }

  //-----------------------------------------------------------------------------//
	
  /** updates confidence scroll when confidence text field is set */
  private void fixConfidenceValues()
  {
	String text = confidenceValueLabel.getText();
	int i;
	for(i=0;i<text.length();i++)
	{
		if (!Character.isDigit(text.charAt(i)))
	   	{
		   break;
		}
	}
	
	//if text field was set incorrectly, it is rewritten	
	if((text.length()!=i)||(text.length()==0))
	{
	   	confidenceValueLabel.setText((100-confidenceScroll.getValue())+"");
	}
	else 
	{
		int value = Integer.parseInt(text);
		confidenceScroll.setValue(100-value);
	}
  }
}
