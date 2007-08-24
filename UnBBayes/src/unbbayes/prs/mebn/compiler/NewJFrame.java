package unbbayes.prs.mebn.compiler;
import java.awt.BorderLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class NewJFrame extends javax.swing.JFrame {
	private JDesktopPane jDesktopPane1;
	private JSplitPane jSplitPane1;
	private JTable jTable1;
	private JTextArea jTextArea1;
	private JLabel jLabel1;
	private ButtonGroup buttonGroup1;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		NewJFrame inst = new NewJFrame();
		inst.setVisible(true);
	}
	
	public NewJFrame() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				{
					buttonGroup1 = new ButtonGroup();
				}
				jDesktopPane1 = new JDesktopPane();
				getContentPane().add(jDesktopPane1, BorderLayout.CENTER);
				{
					jSplitPane1 = new JSplitPane();
					jDesktopPane1.add(jSplitPane1);
					jSplitPane1.setBounds(0, 0, 385, 231);
					{
						jLabel1 = new JLabel();
						jSplitPane1.add(jLabel1, JSplitPane.RIGHT);
						jLabel1.setText("jLabel1");
					}
					{
						jTextArea1 = new JTextArea();
						jSplitPane1.add(jTextArea1, JSplitPane.LEFT);
						jTextArea1.setText("jTextArea1");
						jTextArea1.setPreferredSize(new java.awt.Dimension(142, 154));
					}
				}
				{
					TableModel jTable1Model = new DefaultTableModel(
						new String[][] { { "One", "Two" }, { "Three", "Four" } },
						new String[] { "Column 1", "Column 2" });
					jTable1 = new JTable();
					jDesktopPane1.add(jTable1);
					jTable1.setModel(jTable1Model);
					jTable1.setCellSelectionEnabled(true);
					jTable1.setBounds(0, 0, 147, 231);
				}
			}
			pack();
			setSize(400, 300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
