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

    
   /* public TableRequirements(UmpstModule janelaPai){
    	super(janelaPai);
    	this.setLayout(new GridLayout(1,0));
    	
    	this.add(getScrollPanePergunta());
    
    	
    	
    }*/
 
    
    	  /**private constructors make class extension almost impossible,
    	that's why this is protected*/
    	  protected TableRequirements(UmpstModule janelaPai) {
    		  
    		    super(janelaPai);
    	    	this.setLayout(new GridLayout(1,0));
    	    	this.add(createScrolltableGoals(model));
    		    
    	  }
    	  /**
    	   * SingletonHolder is loaded on the first execution of
    	TableRequirements.getInstance()
    	   * or the first access to SingletonHolder.INSTANCE, not before.
    	   */
    	  private static class SingletonHolder {
    		  	public static final TableRequirements INSTANCE = new TableRequirements(null);
    	  }
    	  
    	  
    	  public static TableRequirements getInstance(UmpstModule janelaPai,DefaultTableModel tableModel) {
    	    
    		System.out.println("entrou no get instance");  
    		TableRequirements ret = SingletonHolder.INSTANCE;
    	    ret.setJanelaPai(janelaPai,tableModel);
    	    return ret;
    	  }
    	  
    	  public void setJanelaPai(UmpstModule janelaPai,DefaultTableModel tableModel){
    		// super(janelaPai);
  	    	
    		  System.out.println("entrou no setJanelaPai");
    		  this.setLayout(new GridLayout(1,0));
  	          this.add(createScrolltableGoals(tableModel));
    		  
    	  }
    
   
	
	/**
	 * @return the table
	 */
	public JTable createTable(DefaultTableModel tableModel) {
		
		table = new JTable(tableModel){  
			
            //  Returning the Class of each column will allow different  
            //  renderers to be used based on Class  
            public Class getColumnClass(int column)  {  
                return getValueAt(0, column).getClass();  
            }  
		};
		
		
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
		buttonColumn2.setMaxWidth(22);
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
		buttonColumn3.setMaxWidth(25);
		buttonColumn3.setCellRenderer(buttonDel);
		buttonColumn3.setCellEditor(buttonDel);
		
		buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {	
			public void onButtonPress(int row, int column) {
				//alterarJanelaAtual(new GoalsMainPanel(getJanelaPai()));
			}
		});
		
		return table;
       }
	
	
	public JScrollPane createScrolltableGoals(DefaultTableModel tableModel){
		if(scrollpanePergunta == null){
			scrollpanePergunta = new JScrollPane(createTable(tableModel));
			scrollpanePergunta.setMinimumSize(new Dimension(300,150));
		}
		
		return scrollpanePergunta;
	}
	
	
	
	public JScrollPane getScrollPanePergunta(){
		
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
