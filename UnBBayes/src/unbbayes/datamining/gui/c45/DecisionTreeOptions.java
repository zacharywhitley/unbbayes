package unbbayes.datamining.gui.c45;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;

public class DecisionTreeOptions extends JDialog
{ 
  private ResourceBundle resource;
  private JCheckBox gainRatioCheckBox;
  private JLabel verbosityValueLabel;
  private JScrollBar verbosityScroll;
  
  public DecisionTreeOptions()
  {   
	resource = ResourceBundle.getBundle("unbbayes.datamining.gui.c45.resources.DecisiontreeResource");
	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	this.setTitle(resource.getString("preferences"));
	this.setSize(220,130);
	this.setResizable(false);
	JPanel contentPane = (JPanel) this.getContentPane();
	contentPane.setLayout(new BorderLayout());
	
	//customized layouts
	FlowLayout leftFlowLayout = new FlowLayout();
	leftFlowLayout.setAlignment(FlowLayout.LEFT);
	FlowLayout rightFlowLayout = new FlowLayout();
	rightFlowLayout.setAlignment(FlowLayout.RIGHT);
			
	//north panel: panel with the options	
	GridLayout northLayout = new GridLayout();
	JPanel northPanel = new JPanel();
	northPanel.setLayout(northLayout);
	northLayout.setColumns(2);
	northLayout.setRows(1);
	contentPane.add(northPanel,"North");
	//verbosity option label
	JPanel verbosityLabelPanel = new JPanel();
	verbosityLabelPanel.setLayout(leftFlowLayout);	
	JLabel verbosityLabel = new JLabel("verbosity level:");
	verbosityLabel.setPreferredSize(new Dimension(100,20));
	verbosityLabelPanel.add(verbosityLabel);
	northPanel.add(verbosityLabelPanel);
	//verbosity option text field
	int verbosityValue = Options.getInstance().getVerbosityLevel();
	JPanel verbosityFieldPanel = new JPanel();
	verbosityFieldPanel.setLayout(leftFlowLayout);
	verbosityValueLabel = new JLabel(verbosityValue+"");
	verbosityScroll = new JScrollBar();
	verbosityScroll.setMinimum(1);
	verbosityScroll.setMaximum(4);
	verbosityScroll.setVisibleAmount(0);
	verbosityScroll.setValue(5-verbosityValue);
	verbosityScroll.setPreferredSize(new Dimension(16, 20));
	verbosityScroll.addAdjustmentListener(
		new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				verbosityValueLabel.setText((5-e.getValue())+"");
			}
		});
	northPanel.add(verbosityFieldPanel);
	verbosityFieldPanel.add(verbosityValueLabel);
	verbosityFieldPanel.add(verbosityScroll);
	
	//center panel: panel with gain ratio option
	JPanel centerPanel = new JPanel();
	contentPane.add(centerPanel,"Center");
	gainRatioCheckBox = new JCheckBox(resource.getString("gainRatio"));
	gainRatioCheckBox.setSelected(Options.getInstance().getIfUsingGainRatio());
	centerPanel.add(gainRatioCheckBox);
	
	//south panel: panel with the buttons 
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
		Options.getInstance().setVerbosityLevel(Integer.parseInt(verbosityValueLabel.getText()));
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
}
