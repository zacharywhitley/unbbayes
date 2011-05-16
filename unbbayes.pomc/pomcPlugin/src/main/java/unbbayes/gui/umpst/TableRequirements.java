package unbbayes.gui.umpst;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;



public class TableRequirements extends IUMPSTPanel{
	
	private JTable table;
	private JScrollPane scrollpanePergunta;
	
	ImageIcon iconAdd = createImageIcon("images/add.gif");
	ImageIcon iconDel = createImageIcon("images/del.gif");
	ImageIcon iconEdit = createImageIcon("images/edit.gif");

	
	String[] columnNames = {"ID","Goal","Botao","Edit","Add sub-Goal","Remove"};

	Object[][] data = {
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},
			{"0001","Determinar fraudes em concurso público","xxx","","",""},

			

	};
	
	
    DefaultTableModel model = new DefaultTableModel(data, columnNames);  

    
    public TableRequirements(UmpstModule janelaPai){
    	super(janelaPai);
    	this.setLayout(new GridLayout(1,0));
    	
    	this.add(getScrollPanePergunta());
    
    	
    	
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
			
	        ButtonColumn buttonColumnEdit = new ButtonColumn(table, 3,iconEdit); 	        
	        ButtonColumn buttonColumnAdd = new ButtonColumn(table, 4,iconAdd);  	        
	        ButtonColumn buttonColumnDel = new ButtonColumn(table, 5,iconDel);  

	        buttonColumnAdd.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {					
					alterarJanelaAtual(new GoalsMainPanel(getJanelaPai()));
					System.out.println("clicou no botao");
					
				}
			});
	        
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
	

}
