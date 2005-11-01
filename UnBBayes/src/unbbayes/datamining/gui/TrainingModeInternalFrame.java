package unbbayes.datamining.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import unbbayes.datamining.evaluation.*;

/**
 * Dialog window where Training Mode options are set.
 *
 * 	@author Mário Hernique Paes Vieira (mhpv@hotmail.com)
 */
public class TrainingModeInternalFrame extends JInternalFrame {

    /** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	private JRadioButton trainingSetRadioButton = new JRadioButton();
	private JRadioButton crossValidationRadioButton = new JRadioButton();
	private JComboBox crossValidationComboBox = new JComboBox();
	private IUnBMinerInternalFrame reference;
	
	  /** Default constructor. */
	  public TrainingModeInternalFrame(IUnBMinerInternalFrame reference) {
		  this.reference = reference;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

   /**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.setTitle("Training Mode");
        this.setSize(220,150);
        this.setResizable(false);
        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        //customized layouts and borders
        GridLayout twoRowsLayout = new GridLayout();
        twoRowsLayout.setColumns(1);
        twoRowsLayout.setRows(2);
        Border border = BorderFactory.createEtchedBorder(Color.white,new Color(165, 163, 151));

        //CENTER PANEL: PANEL WITH THE OTHER OPTIONS
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(twoRowsLayout);
        contentPane.add(centerPanel,"Center");
        centerPanel.setBorder(new TitledBorder(border,"Available Modes"));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        trainingSetRadioButton.setText("Use Training Set");
        trainingSetRadioButton.setSelected(true);
        buttonGroup.add(trainingSetRadioButton);
        for (int i=2;i<31;i++) {
            crossValidationComboBox.addItem(i);        	
        }
        crossValidationComboBox.setSelectedIndex(8);
        centerPanel.add(trainingSetRadioButton);
        
        JPanel crossValidationPanel = new JPanel();
        crossValidationPanel.setLayout(new BorderLayout());
        crossValidationRadioButton.setText("Cross-Validation");
        buttonGroup.add(crossValidationRadioButton);
        crossValidationPanel.add(crossValidationRadioButton,BorderLayout.WEST);
        crossValidationPanel.add(crossValidationComboBox,BorderLayout.CENTER);
        centerPanel.add(crossValidationPanel);

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
        	  if (trainingSetRadioButton.isSelected()) {
            	  reference.setTrainingMode(new TrainingSet());        		  
        	  } else {
        		  reference.setTrainingMode(new CrossValidation((Integer)crossValidationComboBox.getSelectedItem()));
        	  }
                dispose();
          }
        });
        southPanel.add(buttonOK);

        //button cancel
        JButton buttonCancel = new JButton("Cancel"/*resource.getString("cancel")*/);
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
    }

    BorderLayout borderLayout1 = new BorderLayout();
}
