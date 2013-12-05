package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import unbbayes.util.CommonDataUtil;

public class MainPropertiesEditionPane {

	private JLabel titulo            = new JLabel();

	private JTextField authorText;
	private JTextField dateText;
	private JTextArea goalText;
	private JTextArea commentsText;
	
	private MaskFormatter maskFormatter;
	
	private JPanel panel; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = 
  			unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
	
	public MainPropertiesEditionPane(JButton btn1, JButton btn2, String _title, String panelName){
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor =  GridBagConstraints.FIRST_LINE_START; 

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints d = new GridBagConstraints();
		d.gridx = 0; d.gridy = 0; d.gridwidth = 3;
		d.fill = GridBagConstraints.BOTH;
		d.insets = new Insets(0, 0, 0, 0);

		titulo.setFont(new Font("Arial", Font.BOLD, 24));
		titulo.setForeground(Color.white);
		titulo.setHorizontalAlignment(JLabel.CENTER);

		JPanel panelTitulo = new JPanel(new FlowLayout()); 
		panelTitulo.add(titulo);
		panelTitulo.setBackground(new Color(0x4169AA)); 

		panel.add( panelTitulo, d);

		
		c.gridx = 0; c.gridy = 2;c.gridwidth=3;
		panel.add( Box.createRigidArea(new Dimension(0,5)), c);

		c.gridx = 0; c.gridy = 3; c.gridwidth=1;
		panel.add( new JLabel("Description: "), c);

		c.gridx = 0; c.gridy = 4;c.gridwidth=1;

		JLabel jlAuthorName = new JLabel("Author Name: "); 
		panel.add(jlAuthorName, c);

		c.gridx = 0; c.gridy = 5;c.gridwidth=1;
		panel.add( new JLabel("Date: "), c);

		c.gridx = 0; c.gridy = 6;c.gridwidth=1;
		panel.add( new JLabel("Comments: "), c);
		
//		if (goalFather!=null){
//			c.gridx = 0; c.gridy = 7;c.gridwidth=1;
//			panel.add( new JLabel("Father Name: "), c);
//			c.gridx = 1; c.gridy = 7;c.gridwidth=2;
//			panel.add( new JLabel(goalFather.getGoalName()), c);
//		}

		goalText = new JTextArea(2,20);
		goalText.setLineWrap(true); 
		goalText.setWrapStyleWord(true);
		goalText.setBorder(BorderFactory.createEtchedBorder());
		goalText.setText(""); 

		commentsText = new JTextArea(4,20);
		commentsText.setLineWrap(true); 
		commentsText.setWrapStyleWord(true);
		commentsText.setBorder(BorderFactory.createEtchedBorder());
		commentsText.setText(""); 

		authorText = new JTextField(20);

		c.gridx = 1; 
		c.gridy = 3;
		c.gridwidth=2;

		panel.add( goalText, c);

		c.gridx = 1; c.gridy = 4;c.gridwidth=2;
		panel.add( authorText, c);c.gridwidth=2;

		try {
			maskFormatter = new MaskFormatter ("##/##/####");
		}
		catch (ParseException pe) { 
			pe.printStackTrace();
		}

		dateText = new JFormattedTextField(maskFormatter);
		dateText.setColumns(20);

		authorText.setText(CommonDataUtil.getInstance().getAuthorName()); 
		dateText.setText(CommonDataUtil.getInstance().getActualDate()); 

		c.gridx = 1; c.gridy = 5;c.gridwidth=2;
		panel.add( dateText, c);c.gridwidth=2;


		c.gridx = 1; c.gridy = 6;c.gridwidth=2;
		JScrollPane scroolComments  = new JScrollPane(commentsText); 
		panel.add( scroolComments, c);

		JPanel jpanelButtons = new JPanel(new GridLayout(1,4));
		jpanelButtons.add(new JPanel(), 0); 
		jpanelButtons.add(btn1, 1); 
		jpanelButtons.add(btn2, 2); 
		jpanelButtons.add(new JPanel(), 3); 

		c.gridx = 0; c.gridy = 9;c.gridwidth=3;
		panel.add( Box.createRigidArea(new Dimension(0,5)), c);

		c.gridx = 0; c.gridy = 10;c.gridwidth=3;
		panel.add( jpanelButtons, c);

		panel.setBorder(BorderFactory.createTitledBorder(panelName));
		
		titulo.setText(_title);
		
		authorText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				authorText.requestFocus();
			}
		});

		dateText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dateText.requestFocus();
			}
		});
		
	}
	
	public JPanel getPanel(){
		return panel; 
	}
	
	public String getAuthorText() {
		return authorText.getText();
	}

	public void setAuthorText(String authorText) {
		this.authorText.setText(authorText);
	}

	public String getDateText() {
		return dateText.getText();
	}

	public void setDateText(String dateText) {
		this.dateText.setText(dateText);
	}

	public String getGoalText() {
		return goalText.getText();
	}

	public void setGoalText(String goalText) {
		this.goalText.setText(goalText);
	}

	public String getCommentsText() {
		return commentsText.getText();
	}

	public void setCommentsText(String commentsText) {
		this.commentsText.setText(commentsText);
	}
	
}
