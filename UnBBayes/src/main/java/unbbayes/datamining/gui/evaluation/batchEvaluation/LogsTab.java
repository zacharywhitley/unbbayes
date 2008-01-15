package unbbayes.datamining.gui.evaluation.batchEvaluation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import unbbayes.datamining.evaluation.batchEvaluation.BatchEvaluation;
import unbbayes.datamining.evaluation.batchEvaluation.model.Logs;
import unbbayes.datamining.gui.evaluation.batchEvaluation.controllers.LogsTabController;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 07/08/2007
 */
public class LogsTab {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5280637854285401090L;
	private String tabTitle;
	private ResourceBundle resource;
	private LogsTabController controller;
	private BatchEvaluationMain mainView;
	private JTextArea textArea;
	private Logs data;

	public LogsTab(BatchEvaluationMain mainView, BatchEvaluation model) {
		this.mainView = mainView;
		resource = mainView.getResourceBundle();
		tabTitle = resource.getString("logTabTitle");
		controller = new LogsTabController(this, model);
		data = controller.getData();
	}

	/**
	 * Return the This tab's title.
	 * @return This tab's title.
	 */
	public String getTabTitle() {
		return tabTitle;
	}
	
	/**
	 * Build and return the panel created by this class.
	 * @return This tab panel.
	 */
	public JPanel getTabPanel() {
		/* Build the principal panel */
		JPanel panel = new JPanel(new MigLayout());

		textArea = new JTextArea();
		textArea.setEditable(false);
		
		/* Build scroll pane for the data table */
		JScrollPane dataTableScroll = new JScrollPane(textArea);
		dataTableScroll.setPreferredSize(new Dimension(1000, 150));
		panel.add(dataTableScroll, BorderLayout.NORTH);
		
		/* Add a separator line */
		panel.add(new JSeparator(), "growx, wrap, gaptop 10");

		/* Add copy button */
		String copyButtonText = resource.getString("copyButtonText");
		JButton copyButton = new JButton(copyButtonText);
		copyButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						copyData();
					}
				}
			);

		/* Add clear button */
		String clearButtonText = resource.getString("clearButtonText");
		JButton clearButton = new JButton(clearButtonText);
		clearButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						controller.clearData();
					}
				}
			);

		/* Add buttons to the panel */
		panel.add(copyButton,   new CC().spanX(4).split(4).tag("other"));
		panel.add(clearButton,  new CC().tag("other"));
		return panel;
	}
	
	
	public void copyData() {
		textArea.selectAll();
		textArea.copy();
	}

	public void clearData() {
		textArea.setText(null);
	}

	public void insertData(String log) {
		textArea.append(log + "\n");
	}

	public LogsTabController getController() {
		return controller;
	}
	
	public BatchEvaluationMain getMainView() {
		return mainView;
	}

}

