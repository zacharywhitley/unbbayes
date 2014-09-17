package unbbayes.gui.umpst;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.MaskFormatter;

import unbbayes.util.CommonDataUtil;

public class MainPropertiesEditionPane {

	private JLabel titleLabel = new JLabel();

	private final JTextField authorText;
	private final JTextField dateText;
	private final JTextArea nameText;
	private final JTextArea commentsText;
	
	private MaskFormatter maskFormatter;
	
	private JPanel panel; 
	
	private final JButton btnCancel; 
	private final JButton btnOK; 
	
	private final int SIZE_COLUMNS_TEXT = 25; 
	private final int NUMBER_LINES_DESCRIPTION = 3; 
	private final int NUMBER_LINES_COMMENTS = 4; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = 
  			unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.umpst.resources.Resources.class.getName());
	
	public MainPropertiesEditionPane(JButton btn1, 
			JButton btn2, 
			String _title, 
			String panelName, 
			JLabel extraLabel,
			JComponent extraPanel,
			boolean nameLimited){
		
		this.btnCancel = btn1; 
		this.btnOK = btn2; 
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor =  GridBagConstraints.FIRST_LINE_START; 

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints d = new GridBagConstraints();
	
		//----------------------------------------------------------------------
		//                      |                   |                          |
		//                            TITLE TEXT                               |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//  LABEL               |   TEXT BOX                                   |
		//                            EXTRA PANEL                              |
		//----------------------------------------------------------------------

		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(Color.white);
		titleLabel.setHorizontalAlignment(JLabel.CENTER);

		JPanel panelTitle = new JPanel(new FlowLayout()); 
		panelTitle.add(titleLabel);
		panelTitle.setBackground(new Color(0x4169AA)); 

		d.gridx = 0; 
		d.gridy = 0; 
		d.gridwidth = 3;
		d.fill = GridBagConstraints.BOTH;
		d.insets = new Insets(0, 0, 0, 0);
		
		panel.add( panelTitle, d);
		
		c.gridx = 0; 
		c.gridy = 2;
		c.gridwidth=3;
		panel.add( Box.createRigidArea(new Dimension(0,5)), c);

		c.gridx    = 0; 
		c.gridy    = 3; 
		c.gridwidth= 1;
		panel.add( new JLabel("Description: "), c);

		c.gridx = 0; 
		c.gridy = 4;
		c.gridwidth=1;
		
		panel.add(new JLabel("Author Name: "), c);

		c.gridx = 0; 
		c.gridy = 5;
		c.gridwidth=1;
		panel.add( new JLabel("Date: "), c);

		c.gridx = 0; 
		c.gridy = 6;
		c.gridwidth=1;
		panel.add( new JLabel("Comments: "), c);

		if (nameLimited){
			nameText = new JTextArea(1,SIZE_COLUMNS_TEXT);
		}else{
			nameText = new JTextArea(NUMBER_LINES_DESCRIPTION, SIZE_COLUMNS_TEXT);
		}
		
		nameText.setLineWrap(true); 
		nameText.setWrapStyleWord(true);
		nameText.setBorder(BorderFactory.createEtchedBorder());
		nameText.setText(""); 
		
		nameText.addKeyListener(new java.awt.event.KeyAdapter() {
			 public void keyPressed(java.awt.event.KeyEvent evt) {
				 if(evt.getKeyCode() == evt.VK_TAB){
					 authorText.requestFocusInWindow(); 
				 }
			 }
		}); 

		JScrollPane scroolName  = new JScrollPane(nameText); 
		
		c.gridx = 1; 
		c.gridy = 3;
		c.gridwidth=2;

		panel.add( scroolName, c);

		authorText = new JTextField(SIZE_COLUMNS_TEXT);
		
		c.gridx = 1; 
		c.gridy = 4;
		c.gridwidth=2;
		
		panel.add( authorText, c);

		try {
			maskFormatter = new MaskFormatter ("##/##/####");
		}
		catch (ParseException pe) { 
			pe.printStackTrace();
		}

		dateText = new JFormattedTextField(maskFormatter);
		dateText.setColumns(SIZE_COLUMNS_TEXT);

		c.gridx    = 1; 
		c.gridy    = 5;
		c.gridwidth= 2;
		panel.add( dateText, c);

		commentsText = new JTextArea(NUMBER_LINES_COMMENTS, SIZE_COLUMNS_TEXT);
		commentsText.setLineWrap(true); 
		commentsText.setWrapStyleWord(true);
		commentsText.setBorder(BorderFactory.createEtchedBorder());
		commentsText.setText(""); 
		
		commentsText.addKeyListener(new java.awt.event.KeyAdapter() {
			 public void keyPressed(java.awt.event.KeyEvent evt) {
				 if(evt.getKeyCode() == evt.VK_TAB){
					 btnOK.requestFocusInWindow(); 
				 }
			 }
		}); 
		
		JScrollPane scroolComments  = new JScrollPane(commentsText); 
		
		c.gridx = 1; 
		c.gridy = 6;
		c.gridwidth=2;
		panel.add( scroolComments, c);

		JPanel jpanelButtons = new JPanel(new FlowLayout());
		
		btn1.setPreferredSize(new Dimension(100,35));
		btn2.setPreferredSize(new Dimension(100,35));
		
		jpanelButtons.add(new JPanel()); 
		jpanelButtons.add(btn1); 
		jpanelButtons.add(btn2); 
		jpanelButtons.add(new JPanel()); 

		if (extraPanel != null){
			c.gridx = 0; 
			c.gridy = 10;
			c.gridwidth=1;
			c.fill = GridBagConstraints.BOTH;
			panel.add( extraLabel, c);
			
			c.gridx = 1; 
			c.gridy = 10;
			c.gridwidth=2;
			c.fill = GridBagConstraints.BOTH;
			panel.add( extraPanel, c);
			
			c.gridx = 0; 
			c.gridy = 11;
			c.gridwidth=3;
			panel.add( Box.createRigidArea(new Dimension(0,5)), c);
			
			c.gridx = 0; 
			c.gridy = 12;
			c.gridwidth=3;
			c.fill = GridBagConstraints.BOTH;
			panel.add( jpanelButtons, c);
			
		}else{
			c.gridx = 0; 
			c.gridy = 10;
			c.gridwidth=3;
			c.fill = GridBagConstraints.BOTH;
			panel.add( jpanelButtons, c);
			
		}
		
		panel.setBorder(BorderFactory.createTitledBorder(panelName));
		
		titleLabel.setText(_title);
		
		authorText.setText(CommonDataUtil.getInstance().getAuthorName()); 
		
		dateText.setText(CommonDataUtil.getInstance().getActualDate()); 
		
		authorText.addCaretListener(new CaretListener(){

			public void caretUpdate(CaretEvent arg0) {
				CommonDataUtil.getInstance().setAuthorName(authorText.getText()); 
				System.out.println("caret update");
			}
			
		}); 
		
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

	public String getTitleText() {
		return nameText.getText();
	}

	public void setTitleText(String goalText) {
		this.nameText.setText(goalText);
	}

	public String getCommentsText() {
		return commentsText.getText();
	}

	public void setCommentsText(String commentsText) {
		this.commentsText.setText(commentsText);
	}
	
}
