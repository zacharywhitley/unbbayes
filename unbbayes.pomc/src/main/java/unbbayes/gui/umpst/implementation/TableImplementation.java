package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.TableButton;
import unbbayes.gui.umpst.TableObject;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Show list of rules and its description. First panel when user access Implementation panel.
 * @author Diego Marques
 */
public class TableImplementation extends TableObject{
	
	private JScrollPane scrollPanePergunta;
	private ImplementationEditPanel implementationEditPanel;
	
	private IconController iconController = IconController.getInstance();	
	
	String[] columnNames = {"ID", "Rules", ""};	

	public TableImplementation(UmpstModule janelaPai, UMPSTProject umpstProject) {
		super(janelaPai, umpstProject);
		this.setLayout(new GridLayout(1, 2));	
		
//		this.add(createDescriptionText(), BorderLayout.CENTER);
		this.add(createScrollTableRules(), BorderLayout.CENTER);
	}

	/**
	 * 
	 * @return the scroolPanePergunta
	 */
	public JScrollPane createScrollTableRules() {
		if(scrollPanePergunta == null) {
			scrollPanePergunta = new JScrollPane(createTable());
			scrollPanePergunta.setSize(new Dimension(300, 150));
		}
		return scrollPanePergunta;
	}	

	/**
	 * Pass rule data to an object
	 * @return the data
	 */
	public JTable createTable() {
		
		Object[][] data = new Object[getUmpstProject().getMapRules().size()][3];
		Set<Integer> keysInteger = new TreeSet<Integer>();
		
		for (String key : getUmpstProject().getMapRules().keySet()) {
			keysInteger.add(new Integer(key));
		}
		
		int i = 0;
		for (Integer keyInteger : keysInteger) {
			String key = keyInteger.toString();			
			data[i][0] = getUmpstProject().getMapRules().get(key).getId();
			data[i][1] = getUmpstProject().getMapRules().get(key).getName();
			data[i][2] = "";
			i++;
		}
		return createTable(data);
	}

	/**
	 * Populate table row with data object.
	 * @param data
	 * @return the table
	 */
	public JTable createTable(final Object[][] data) {
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		final JTable table = new JTable(tableModel);
		
		TableButton buttonEdit = new TableButton(new TableButton.TableButtonCustomizer() {			
			public void customize(JButton button, int row, int column) {
				button.setIcon(iconController.getEditUMPIcon());				
			}
		});
		
		TableColumn bottomColumn1 = table.getColumnModel().getColumn(columnNames.length-1);
		bottomColumn1.setMaxWidth(SIZE_COLUMN_BUTTON);
		bottomColumn1.setCellRenderer(buttonEdit);
		bottomColumn1.setCellEditor(buttonEdit);
		
		buttonEdit.addHandler(new TableButton.TableButtonPressedHandler() {			
			public void onButtonPress(int row, int column) {
				String key = data[row][0].toString();
				createEditPanel(key);
			}		
		});
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (true) { // true by default
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
 
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        System.out.println("No rows are selected.");
                    } else {
                    	// This listener starts counting from zero.
                        int selectedRow = lsm.getMinSelectionIndex() + 1;
                        
                        Set<Integer> keysInteger = new TreeSet<Integer>();                		
                		for (String key : getUmpstProject().getMapRules().keySet()) {
                			keysInteger.add(new Integer(key));
                		}
                		
                		for (Integer keyInteger : keysInteger) {
                			String key = keyInteger.toString();
                			String ruleID = getUmpstProject().getMapRules().get(key).getId();
                			if (ruleID.equals(Integer.toString(selectedRow))) {
                				String ruleDescriptionText = getUmpstProject().getMapRules().get(key).getName();
                				UmpstModule ipanel = getFatherPanel();
                				ipanel.getMenuPanel().getImplementationPane().setRuleDescriptionText(ruleDescriptionText);
                				ipanel.getMenuPanel().getImplementationPane().updateDescriptionArea();
							}                			
                		}                       
                    }
                }
            });
        }
        
        
		TableColumn indexColumn = table.getColumnModel().getColumn(0);
		indexColumn.setMaxWidth(SIZE_COLUMN_INDEX);
		
		return table;
	}
	
	/**
	 * Open panel to define rule selected.
	 * @param key
	 */
	public void createEditPanel(String key) {
		RuleModel ruleAux = getUmpstProject().getMapRules().get(key);		
		implementationEditPanel = new ImplementationEditPanel(getFatherPanel(), getUmpstProject(), 
				ruleAux);
		UmpstModule janelaPai = getFatherPanel();
		changePanel(janelaPai.getMenuPanel().getImplementationPane().getImplementationTable().
				getImplementationEditPanel());
	}

	/**
	 * @return the scrollPanePergunta
	 */
	public JScrollPane getScrollPanePergunta() {
		return scrollPanePergunta;
	}

	/**
	 * @param scrollPanePergunta the scrollPanePergunta to set
	 */
	public void setScrollPanePergunta(JScrollPane scrollPanePergunta) {
		this.scrollPanePergunta = scrollPanePergunta;
	}

	/**
	 * @return the implementationEditPanel
	 */
	public ImplementationEditPanel getImplementationEditPanel() {
		return implementationEditPanel;
	}

	/**
	 * @param implementationEditPanel the implementationEditPanel to set
	 */
	public void setImplementationEditPanel(ImplementationEditPanel implementationEditPanel) {
		this.implementationEditPanel = implementationEditPanel;
	}

}
