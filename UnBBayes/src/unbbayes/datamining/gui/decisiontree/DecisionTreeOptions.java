package unbbayes.datamining.gui.decisiontree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import unbbayes.datamining.datamanipulation.*;

public class DecisionTreeOptions extends JDialog
{ 
  private ResourceBundle resource;
  private JCheckBox gainRatioCheckBox;
  
  public DecisionTreeOptions()
  {   
	resource = ResourceBundle.getBundle("unbbayes.datamining.gui.decisiontree.resources.DecisiontreeResource");
	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	this.setTitle(resource.getString("preferences"));
	this.setSize(250,100);
	this.setModal(true);
	this.setResizable(false);
	
	JPanel contentPane = (JPanel) this.getContentPane();
	contentPane.setLayout(new BorderLayout());
	
	JPanel southPanel = new JPanel();
	southPanel.setLayout(new FlowLayout());
	JButton buttonOK = new JButton("OK");
	buttonOK.setMnemonic('O');	
	buttonOK.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		Options.getInstance().setIfUsingGainRatio(gainRatioCheckBox.isSelected());
		dispose();
	  }
	});
	
	JButton buttonCancel = new JButton(resource.getString("cancel"));
	buttonCancel.setMnemonic('C');
	buttonCancel.addActionListener(new java.awt.event.ActionListener()
	{
	  public void actionPerformed(ActionEvent e)
	  {
		dispose();
	  }
	});
	contentPane.add(southPanel,"South");
	southPanel.add(buttonOK);
	southPanel.add(buttonCancel);
		
	GridLayout northLayout = new GridLayout();
	JPanel northPanel = new JPanel();
	northPanel.setLayout(northLayout);
	northLayout.setColumns(1);
	northLayout.setRows(1);
	JPanel gainRatioPanel = new JPanel();
	gainRatioCheckBox = new JCheckBox(resource.getString("gainRatio"));
	gainRatioCheckBox.setSelected(Options.getInstance().getIfUsingGainRatio());
	contentPane.add(northPanel,"North");
	northPanel.add(gainRatioPanel);
	gainRatioPanel.add(gainRatioCheckBox);
	
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
