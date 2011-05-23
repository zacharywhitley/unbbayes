package unbbayes.gui.umpst;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;



import unbbayes.model.umpst.requirements.GoalModel;







public class TableRequirements extends IUMPSTPanel{
	
	private JTable table;
	private JScrollPane scrollpanePergunta;
	

	private static TableRequirements instance;

	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"Goal","","",""};

	Object[][] data = {
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},
			{"Determinar fraudes em concurso público","","",""},	
	};
	
	
    DefaultTableModel model = new DefaultTableModel(data, columnNames);  

    
    public TableRequirements(UmpstModule janelaPai){
    	super(janelaPai);
    	this.setLayout(new GridLayout(1,0));
    	
    	this.add(getScrollPanePergunta());
    
    	
    	
    }
    
    
    public static TableRequirements getInstance(UmpstModule janelaPai) {
		if(instance == null){
			instance = new TableRequirements(janelaPai);
		}
		return instance;
	}
    
   
	
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
			
			TableButton buttonEdit = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column)
				{
					button.setIcon(new ImageIcon("images/edit.gif") );

				}
			});

			TableColumn buttonColumn1 = table.getColumnModel().getColumn(1);
			buttonColumn1.setMaxWidth(28);
			buttonColumn1.setCellRenderer(buttonEdit);
			buttonColumn1.setCellEditor(buttonEdit);
			
			buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					//alterarJanelaAtual(new GoalsMainPanel(getJanelaPai()));
				}
			});
			
			
			TableButton buttonAdd = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column)
				{
					button.setIcon(new ImageIcon("images/add.gif") );

				}
			});

			TableColumn buttonColumn2 = table.getColumnModel().getColumn(2);
			buttonColumn2.setMaxWidth(28);
			buttonColumn2.setCellRenderer(buttonAdd);
			buttonColumn2.setCellEditor(buttonAdd);
			
			buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					alterarJanelaAtual(new SubgoalMainPanel(getJanelaPai()));
				}
			});
			
			
			TableButton buttonDel = new TableButton( new TableButton.TableButtonCustomizer()
			{
				public void customize(JButton button, int row, int column)
				{
					button.setIcon(new ImageIcon("images/del.gif") );

				}
			});

			TableColumn buttonColumn3 = table.getColumnModel().getColumn(3);
			buttonColumn3.setMaxWidth(28);
			buttonColumn3.setCellRenderer(buttonDel);
			buttonColumn3.setCellEditor(buttonDel);
			
			buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
				public void onButtonPress(int row, int column) {
					//alterarJanelaAtual(new GoalsMainPanel(getJanelaPai()));
				}
			});
			
			
	       /** ButtonColumn buttonColumnEdit = new ButtonColumn(table, 3,iconEdit); 	        
	        ButtonColumn buttonColumnAdd = new ButtonColumn(table, 4,iconAdd);  	        
	        ButtonColumn buttonColumnDel = new ButtonColumn(table, 5,iconDel);  

	        buttonColumnAdd.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {					
					alterarJanelaAtual(new SubgoalMainPanel(getJanelaPai()));
					System.out.println("clicou no botao");
					
				}
			}); */
	        
		}
		return scrollpanePergunta; 
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
    
    public void updateGoalsTable(GoalModel goal){
    	String[] columnNames = {"Goal","","",""};

    	Object[][] data ={ 
    			{goal.getGoalName(),"","",""},
    	};
    	
    	
    }
	

}
