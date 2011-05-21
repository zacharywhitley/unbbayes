package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ScrollPanePeer;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;



import unbbayes.controller.umpst.RequirementsController;
import unbbayes.model.umpst.requirements.GoalModel;


public class GoalsPanel extends IUMPSTPanel {
	
	
	private JLabel labelGoal;
	private JLabel labelDetailedGoal;
	private JLabel labelComments;
	private JLabel labelQueries;
	
	private JTextArea textGoal;
	private JTextArea textDetailedGoal;
	private JTextArea textComments;
	
	
	private JButton buttonAddGoal;
	private JButton buttonAddHipotesis;
	private JButton buttonSave;
	
	/**
	 * @return the buttonSave
	 */
	public JButton getButtonSave() {
		
		buttonSave.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				//RequirementsController c = new RequirementsController();
				//c.createGoal(getTextGoal(),getTextComments(),null);
				
				UmpstModule pai = getJanelaPai();
				alterarJanelaAtual(pai.getMenuPanel());	
			}
		});
		
		return buttonSave;
	}

	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"ID","Hipotesis","Botao","Edit","Add sub-Hyphotesis","Remove"};

	Object[][] data = {
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},			
			{getLabelGoal(),getLabelDetailedGoal(),"xxx","testestes","testestes","testestes"},

	};
	
	
    DefaultTableModel model = new DefaultTableModel(data, columnNames);  

	
	/**
	 * @return the table
	 */
	public JTable getTable() {
		
		table = new JTable(model){  
            //  Returning the Class of each column will allow different  
            //  renderers to be used based on Class  
            public Class getColumnClass(int column)  {  
                return getValueAt(0, column).getClass();  
            }  
		};
		return table;
       }
	
	public JScrollPane getScrollPanePergunta(){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(getTable());
			scrollpanePergunta.setMinimumSize(new Dimension(300,150));
			
			
			
	        ButtonColumn buttonColumnEdit = new ButtonColumn(table, 3,iconEdit); 	        
	        ButtonColumn buttonColumnAdd = new ButtonColumn(table, 4,iconAdd);  
	        ButtonColumn buttonColumnDel = new ButtonColumn(table, 5,iconDel);  

		}
		return scrollpanePergunta;
	}



	public GoalsPanel(UmpstModule janelaPai){
		super(janelaPai);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextGoal());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getLabelComments());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextComments());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getLabelQueries());
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BoxLayout(tPanel, BoxLayout.LINE_AXIS));

		TrackingPanel trackingPanel = new TrackingPanel(janelaPai);
		tPanel.add(trackingPanel.getTrackingPanel());
		
		/*JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonSave());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));*/
				
		add(panel,BorderLayout.PAGE_START);
		add(tPanel,BorderLayout.CENTER);
		//add(buttonPane,BorderLayout.PAGE_END);

	}

	/**
	 * @return the labelGoal
	 */
	public JLabel getLabelGoal() {
		if(labelGoal == null){
			labelGoal = new JLabel("Goal: ") {
				public String toString() {
					// TODO Auto-generated method stub
					return this.getText();
				}
			};
			labelGoal.setForeground(Color.white);
		}
		return labelGoal;
	}

	/**
	 * @return the labelDetailedGoal
	 */
	public JLabel getLabelDetailedGoal() {
		if(labelDetailedGoal == null){
			labelDetailedGoal = new JLabel("Detailed Goal"){
				public String toString() {
					// TODO Auto-generated method stub
					return this.getText();
				}
			};
			}
		return labelDetailedGoal;
	}

	/**
	 * @return the labelComments
	 */
	public JLabel getLabelComments() {
		if(labelComments == null){
			labelComments = new JLabel("Comments: ");
			labelComments.setForeground(Color.white);
		}
		return labelComments;
	}

	/**
	 * @return the labelQueries
	 */
	public JLabel getLabelQueries() {
		if(labelQueries == null){
			labelQueries = new JLabel("Add Backtracking:");
			labelQueries.setForeground(Color.white);
		}
		return labelQueries;
	}

	/**
	 * @return the textGoal
	 */
	public JTextArea getTextGoal() {
		if(textGoal == null){
			textGoal = new JTextArea(5,20){
			public String toString() {
				// TODO Auto-generated method stub
				return this.getText();
			}
			};
		}
		return textGoal;
	}

	/**
	 * @return the textDetailedGoal
	 */
	public JTextArea getTextDetailedGoal() {
		if(textDetailedGoal == null){
			textDetailedGoal = new JTextArea(5,20);
		}
		return textDetailedGoal;
	}

	/**
	 * @return the textComments
	 */
	public JTextArea getTextComments() {
		if(textComments == null){
			textComments = new JTextArea(5,20){
				public String toString() {
					// TODO Auto-generated method stub
					return this.getText();
				}
				};
		}
		return textComments;
	}
	
	
	 
    /**
	 * @return the buttonAddGoal
	 */
	public JButton getButtonAddGoal() {
		
		if (buttonAddGoal==null){
			buttonAddGoal = new JButton("Add goal");
			buttonAddGoal.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
				
					
					UmpstModule pai = getJanelaPai();
					alterarJanelaAtual(pai.getMenuPanel());
				}
			});			
		}
		
		return buttonAddGoal;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MenuPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    

	 
}
