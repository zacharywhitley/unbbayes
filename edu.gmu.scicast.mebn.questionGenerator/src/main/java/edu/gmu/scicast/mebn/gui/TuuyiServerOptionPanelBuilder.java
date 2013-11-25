/**
 * 
 */
package edu.gmu.scicast.mebn.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.util.Debug;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;

import edu.gmu.scicast.mebn.kb.TuuyiKnowledgeBase;

/**
 * This is the option panel related to 
 * @author Shou Matsumoto
 *
 */
public class TuuyiServerOptionPanelBuilder extends JScrollPane implements IKBOptionPanelBuilder {

	private static final long serialVersionUID = 962294717143477235L;
	
	private KnowledgeBase kb;
	private JTextField urlTextField;
	
	
	/**
	 * Default constructor must be public for plug-in support
	 */
	public TuuyiServerOptionPanelBuilder() {
		try {
			this.setName("Tuuyi Server URL");
		}catch (Throwable e) {
			// the constructor must not fail in initializing fields accessible by public methods, because callers may initialize them afterwards
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.IPanelBuilder#getPanel()
	 */
	public JComponent getPanel() {
		return this;
	}
	


	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#commitChanges()
	 */
	public void commitChanges() {
		
		try {
			// force KB to reset (undo) its "clear" commands
			if (this.getKB() instanceof TuuyiKnowledgeBase) {
				TuuyiKnowledgeBase owl2KnowledgeBase = (TuuyiKnowledgeBase) this.getKB();
				owl2KnowledgeBase.undoClearKnowledgeBase();
			}
			
			// initial assertion
			if (this.getKB() == null
					|| !(this.getKB() instanceof TuuyiKnowledgeBase)) {
				Debug.println(this.getClass(), "No KB to commit...");
				return;
			}
			
			// extract MEBN in order to extract storage implementor
			OntologyClient client = ((TuuyiKnowledgeBase)this.getKB()).getOntologyClient();
			if (client == null ) {
				Debug.println(this.getClass(), "No Storage implementor to commit...");
				return;
			}
			
			client.setServerURL(new URL(getURLTextField().getText()));
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					this, 
					e.getMessage(), 
					UIManager.getString("OptionPane.messageDialogTitle"), 
					JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#discardChanges()
	 */
	public void discardChanges() {
		// reset text field to the currently selected URL
		this.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#setKB(unbbayes.prs.mebn.kb.KnowledgeBase)
	 */
	public void setKB(KnowledgeBase kb) {
		if (this.kb == kb) {
			// no change. Do nothing
			return;
		}
		this.kb = kb;
		// initialize set of routines to hide EntityPanel, EntityInstancePanel and FindingPanel when this KB is selected
		// usually, this KB is selected when its "clear" method is called
		// TODO find out another way KB can detect what KB is currently selected
		// we must hide such panels because they are elements to be edited by Protege 4.1 panel
		if (this.getKB()  instanceof TuuyiKnowledgeBase) {
			TuuyiKnowledgeBase owl2KnowledgeBase = (TuuyiKnowledgeBase) this.getKB();
			// update commands if it exists
			if (owl2KnowledgeBase.getClearKBCommandList() != null) {
				// this will act like a parameter for the commands
				final IMEBNMediator mediator = owl2KnowledgeBase.getDefaultMediator();
				// disable/enable entity panel, individuals panel and finding panel
				owl2KnowledgeBase.getClearKBCommandList().add(new TuuyiKnowledgeBase.IClearKBCommand() {
					public void doCommand() {
						mediator.getMebnEditionPane().getBtnTabOptionEntity().setEnabled(false);
						mediator.getMebnEditionPane().getBtnTabOptionEntityFinding().setEnabled(false);
						mediator.getMebnEditionPane().getBtnTabOptionNodeFinding().setEnabled(false);
						mediator.getMebnEditionPane().getBtnTabOptionTree().doClick(); // change view to MTheoryTree
					}
					public void undoCommand() {
						mediator.getMebnEditionPane().getBtnTabOptionEntity().setEnabled(true);
						mediator.getMebnEditionPane().getBtnTabOptionEntityFinding().setEnabled(true);
						mediator.getMebnEditionPane().getBtnTabOptionNodeFinding().setEnabled(true);
					}
				});
			}
		}
		
		this.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#getKB()
	 */
	public KnowledgeBase getKB() {
		return kb;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JScrollPane#updateUI()
	 */
	public void updateUI() {
		
		JPanel newView = new JPanel(new GridLayout(0, 1));
		newView.setBackground(Color.WHITE);
		newView.setBorder(new TitledBorder(this.getName()));
		
		setURLTextField(new JTextField(60));
		
		// initialize text field with current server URL
		try {
			getURLTextField().setText( ((TuuyiKnowledgeBase)getKB()).getOntologyClient().getServerURL().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		newView.add(urlTextField);
		
		this.setViewportView(newView);
		super.updateUI();
		this.repaint();
	}

	/**
	 * @return the urlTextField
	 */
	public JTextField getURLTextField() {
		return urlTextField;
	}

	/**
	 * @param urlTextField the urlTextField to set
	 */
	public void setURLTextField(JTextField urlTextField) {
		this.urlTextField = urlTextField;
	}


}
