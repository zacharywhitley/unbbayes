package unbbayes.datamining.gui.c45;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import unbbayes.datamining.datamanipulation.*;

/** 
 * Dialog window where C4.5 options are set.
 * 
 * 	@author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class DecisionTreeOptions extends JInternalFrame
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
	this.setSize(220,310);
	this.setResizable(false);
	JPanel contentPane = (JPanel) this.getContentPane();
	contentPane.setLayout(new BorderLayout());
	
	//customized layouts and borders
	FlowLayout leftFlowLayout = new FlowLayout();
	leftFlowLayout.setAlignment(FlowLayout.LEFT);
	FlowLayout rightFlowLayout = new FlowLayout();
	rightFlowLayout.setAlignment(FlowLayout.RIGHT);
	GridLayout twoColumsLayout = new GridLayout();
	twoColumsLayout.setColumns(2);
	twoColumsLayout.setRows(1);
	GridLayout twoRowsLayout = new GridLayout();
	twoRowsLayout.setColumns(1);
	twoRowsLayout.setRows(2);
	Border border = BorderFactory.createEtchedBorder(Color.white,new Color(165, 163, 151));
			
	//NORTH PANEL: PRUNNING PANEL
	JPanel northPanel = new JPanel();
	northPanel.setLayout(twoRowsLayout);
	northPanel.setBorder(new TitledBorder(border,resource.getString("prunning1")));
	contentPane.add(northPanel,"North");
	
	//prunning check box
	JPanel prunningCheckBoxPanel = new JPanel();
	prunningCheckBoxPanel.setLayout(leftFlowLayout);
	prunningCheckBox = new JCheckBox(resource.getString("prunning2"));
	prunningCheckBox.setSelected(Options.getInstance().getIfUsingPrunning());
	prunningCheckBox.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			confidenceScroll.setEnabled(prunningCheckBox.isSelected());
			confidenceValueLabel.setEnabled(prunningCheckBox.isSelected());
		}
	});
	prunningCheckBoxPanel.add(prunningCheckBox);
	northPanel.add(prunningCheckBoxPanel);
	
	//confidence panel
	JPanel confidencePanel = new JPanel();
	confidencePanel.setLayout(twoColumsLayout);
	northPanel.add(confidencePanel);
		
	//confidence option label
	JPanel confidenceLabelPanel = new JPanel();
	confidenceLabelPanel.setLayout(leftFlowLayout);	
	JLabel confidenceLabel = new JLabel(resource.getString("confidenceLevel"));
	confidenceLabel.setPreferredSize(new Dimension(100,20));
	confidenceLabelPanel.add(confidenceLabel);
	confidencePanel.add(confidenceLabelPanel);
	
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
	confidenceFieldPanel.add(confidenceValueLabel);
	confidenceFieldPanel.add(confidenceScroll);
	confidenceFieldPanel.add(new JLabel("%"));
	confidencePanel.add(confidenceFieldPanel);
	
	//CENTER PANEL: PANEL WITH THE OTHER OPTIONS
	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(twoRowsLayout);
	contentPane.add(centerPanel,"Center");
	
	//gain ratio check box
	JPanel gainRatioPanel = new JPanel();
	gainRatioPanel.setLayout(leftFlowLayout);
	gainRatioPanel.setBorder(new TitledBorder(border,resource.getString("gainRatio1")));
	gainRatioCheckBox = new JCheckBox(resource.getString("gainRatio2"));
	gainRatioCheckBox.setSelected(Options.getInstance().getIfUsingGainRatio());
	gainRatioPanel.add(gainRatioCheckBox);
	centerPanel.add(gainRatioPanel);
	
	//confidence panel
	JPanel verbosityPanel = new JPanel();
	verbosityPanel.setLayout(twoColumsLayout);
	verbosityPanel.setBorder(new TitledBorder(border,resource.getString("verbosity1")));
	centerPanel.add(verbosityPanel);
		
	//verbosity option label
	JPanel verbosityLabelPanel = new JPanel();
	verbosityLabelPanel.setLayout(leftFlowLayout);	
	JLabel verbosityLabel = new JLabel(resource.getString("verbosity2"));
	verbosityLabel.setPreferredSize(new Dimension(100,20));
	verbosityLabelPanel.add(verbosityLabel);
	verbosityPanel.add(verbosityLabelPanel);
	
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
	verbosityFieldPanel.add(verbosityValueLabel);
	verbosityFieldPanel.add(verbosityScroll);
	verbosityPanel.add(verbosityFieldPanel);
	
	//SOUTH PANEL: PANEL WITH BUTTONS 
	JPanel southPanel = new JPanel();
	southPanel.setLayout(new FlowLayout());
	contentPane.add(southPanel,"South");
	
	//button ok
	JButton buttonOK = new JButton("OK");
	buttonOK.setMnemonic('O');
	buttonOK.setPreferredSize(new Dimension(80,25));	
	buttonOK.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		Options.getInstance().setIfUsingGainRatio(gainRatioCheckBox.isSelected());
		Options.getInstance().setIfUsingPrunning(prunningCheckBox.isSelected());
		Options.getInstance().setVerbosityLevel(Integer.parseInt(verbosityValueLabel.getText().trim()));
		Options.getInstance().setConfidenceLevel(Float.parseFloat(confidenceValueLabel.getText().trim())/100f);
		
		dispose();
	  }
	});
	southPanel.add(buttonOK);
	
	//button cancel
	JButton buttonCancel = new JButton(resource.getString("cancel"));
	buttonCancel.setMnemonic('C');
	buttonCancel.setPreferredSize(new Dimension(80,25));
	buttonCancel.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		dispose();
	  }
	});
	southPanel.add(buttonCancel);
	  	
	/*
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
	*/
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
		verbosityValueLabel.setText((5-verbosityScroll.getValue())+"    ");
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
	   	confidenceValueLabel.setText((100-confidenceScroll.getValue())+"    ");
	}
	else 
	{
		int value = Integer.parseInt(text);
		confidenceScroll.setValue(100-value);
	}
  }
}
