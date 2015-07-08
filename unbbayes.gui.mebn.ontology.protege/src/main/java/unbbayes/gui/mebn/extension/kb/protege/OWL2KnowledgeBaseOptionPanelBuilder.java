/**
 * 
 */
package unbbayes.gui.mebn.extension.kb.protege;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLReasonerInfo;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBaseBuilder;
import unbbayes.util.Debug;

/**
 * This is the option panel related to {@link OWL2KnowledgeBaseBuilder}
 * @author Shou Matsumoto
 *
 */
public class OWL2KnowledgeBaseOptionPanelBuilder extends JScrollPane implements IKBOptionPanelBuilder {

	private static final long serialVersionUID = 472990477185584431L;
	
	private KnowledgeBase kb;
	private ButtonGroup reasonerButtonGroup;
	private JPanel owlAPIReasonerOptionPanel;
	private JPanel protege41ReasonerOptionPanel;
	private JLabel owlAPIReasonerLabel;
	

//	private boolean isToHideEntityButtons = true;
	private boolean isToHideEntityButtons = false;
	
	
//	private JRadioButtonMenuItem previouslySelectedReasonerItem;
	
	/**
	 * Default constructor must be public for plug-in support
	 */
	public OWL2KnowledgeBaseOptionPanelBuilder() {
		try {
			this.setReasonerButtonGroup(new ButtonGroup());
			this.setName("Available OWL2 Reasoners");
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
	
	/**
	 * Creates a panel containing informations and configuration forms to edit 
	 * a OWLAPI-based knowledge base.
	 * @param kb2
	 * @return
	 */
	protected JComponent createOWLAPIReasonerOptionPanel(KnowledgeBase kb2) {
		try {
			// initial assertion
			if (this.getKB() == null
					|| !(this.getKB() instanceof OWL2KnowledgeBase)
					|| ((OWL2KnowledgeBase)this.getKB()).getDefaultOWLReasoner() == null) {
				return new JLabel("No OWL2 reasoner found.", SwingConstants.CENTER);
			}
			
			// extract currently selected reasoner
			OWLReasoner reasoner = ((OWL2KnowledgeBase)this.getKB()).getDefaultOWLReasoner();
			
			// this is the return of this method
			this.setOWLAPIReasonerOptionPanel(new JPanel(new BorderLayout()));
			this.getOWLAPIReasonerOptionPanel().setBorder(new TitledBorder("Current reasoner"));
			this.getOWLAPIReasonerOptionPanel().setBackground(Color.WHITE);
			
			// just create a label telling what is the currently selected reasoner
			this.setOWLAPIReasonerLabel(new JLabel(reasoner.getReasonerName(),IconController.getInstance().getCompileIcon(), SwingConstants.CENTER));
			this.getOWLAPIReasonerLabel().setToolTipText("Select another one and press \"confirm\" to change this reasoner.");
			this.getOWLAPIReasonerOptionPanel().add( this.getOWLAPIReasonerLabel() , BorderLayout.CENTER);
			
			
			// force label update when visibility changes...
			this.getOWLAPIReasonerLabel().addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					if (e.getChangeFlags() == e.SHOWING_CHANGED) {
						Debug.println(this.getClass(), "Showing changed on OWLAPI Reasoner label");
						try {
							getOWLAPIReasonerLabel().setText(((OWL2KnowledgeBase)getKB()).getDefaultOWLReasoner().getReasonerName());
							getOWLAPIReasonerLabel().updateUI();
							getOWLAPIReasonerLabel().repaint();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			});
			
			return this.getOWLAPIReasonerOptionPanel();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// if code reaches here, we could not specify what reasoner was being used
		return this.createDefaultErrorPanel();
//		return null;
	}
	
	/**
	 * This is just an extension of {@link JRadioButtonMenuItem} which also holds an instance of {@link OWLReasonerInfo}.s
	 * @author Shou Matsumoto
	 */
	class JRadioButtonMenuItemWithReasonerInfo extends JRadioButtonMenuItem {
		private static final long serialVersionUID = -2542776227138651025L;
		private OWLReasonerInfo reasonerInfo;
		public JRadioButtonMenuItemWithReasonerInfo(OWLReasonerInfo reasonerInfo, ImageIcon icon, boolean selected) {
			super(reasonerInfo.getReasonerName(), icon, selected);
			this.reasonerInfo = reasonerInfo;
			// the name will store the reasoner ID
			this.setName(reasonerInfo.getReasonerId());
			this.setToolTipText(reasonerInfo.getReasonerId());
		}
		public OWLReasonerInfo getReasonerInfo() { return reasonerInfo; }
	}

	/**
	 * Set up option panel for Protege 4.1 reasoners
	 * @param kb2
	 * @return
	 */
	protected JComponent createProtege41ReasonerOptionPanel(KnowledgeBase kb2) {
		if (this.getKB() == null
				|| !(this.getKB() instanceof OWL2KnowledgeBase)) {
			return this.createDefaultErrorPanel();
		}
		
//		// extract MEBN
//		MultiEntityBayesianNetwork mebn = ((OWL2KnowledgeBase)this.getKB()).getDefaultMEBN();
//		
//		
//		// only create component if mebn is carring a protege storage implementor.
//		if (mebn == null 
//				|| mebn.getStorageImplementor() == null
//				|| !(mebn.getStorageImplementor() instanceof ProtegeStorageImplementorDecorator)) {
//			return null;
//		}
//		
//		// extract implementor (if code reaches here, storage implementor is not null)
//		ProtegeStorageImplementorDecorator protegeStorageImplementor = (ProtegeStorageImplementorDecorator)mebn.getStorageImplementor();
		
		OWL2KnowledgeBase owl2KB = ((OWL2KnowledgeBase)this.getKB());
		
		// extract current OWL reasoner to compare to the loaded reasoners
		OWLReasoner currentReasoner = owl2KB.getDefaultOWLReasoner();
		
		// this is the return of this method
		this.setProtege41ReasonerOptionPanel(new JPanel(new GridLayout(0, 1)));
		this.getProtege41ReasonerOptionPanel().setBackground(Color.WHITE);
		this.getProtege41ReasonerOptionPanel().setBorder(new TitledBorder("Choose another reasoner"));
		
		// create radio boxes to select a reasoner.
		try {
			// reset previously selected reasoner option
//			for (ProtegeOWLReasonerInfo installedReasoner : protegeStorageImplementor.getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getInstalledReasonerFactories()) {
			for (OWLReasonerInfo reasonerInfo : owl2KB.getAvailableOWLReasonersInfo()) {
				JRadioButtonMenuItem radioItem = null;
				try {
					// create radio item representing a reasoner installed in protege
					radioItem = new JRadioButtonMenuItemWithReasonerInfo(
										reasonerInfo, 
										IconController.getInstance().getEntityInstance(),
										reasonerInfo.getReasonerName().equals(currentReasoner.getReasonerName())	// just test name equality
//										|| reasonerInfo.getReasonerFactory().createReasoner(currentReasoner.getRootOntology()).getClass().equals(currentReasoner.getClass())  // test class equality
								);	
					radioItem.setBackground(Color.WHITE);
					// This is strange, but Swing is not "marking" the selected radio Items (even when the item is selected, its radio button is not visually "selected")
//					if (radioItem.isSelected()) {
//						// reinforce GUI to "mark" it as selected by "pressing" it
//						try {
//							radioItem.doClick();
//						}catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
					try {
						Debug.println(this.getClass(), radioItem.getName() + (radioItem.isSelected()?" is selected.":" is not selected."));
					}catch (Throwable e) {
						e.printStackTrace();
					}
				} catch (Throwable e) {
					// OK. This one failed, but let's try the others
					e.printStackTrace();
					continue;
				}
				
				// add item to group
				this.getReasonerButtonGroup().add(radioItem);
				
				// add item to the last created protege4.1 option panel (which is the return of this method)
				this.getProtege41ReasonerOptionPanel().add(radioItem);
				
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return this.createDefaultErrorPanel();
		}
		
		return this.getProtege41ReasonerOptionPanel();
	}

	/**
	 * Sets up default panel (which displays error messages)
	 * @return
	 */
	protected JComponent createDefaultErrorPanel() {
		JTextArea textArea = new JTextArea("Could not create OWL2 Knowledge Base");
		textArea.setEditable(false);
		return textArea;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#commitChanges()
	 */
	public void commitChanges() {
		// force KB to reset (undo) its "clear" commands
		if (this.getKB() instanceof OWL2KnowledgeBase) {
			OWL2KnowledgeBase owl2KnowledgeBase = (OWL2KnowledgeBase) this.getKB();
			owl2KnowledgeBase.undoClearKnowledgeBase();
		}
		
//		System.gc();
		
		// initial assertion
		if (this.getKB() == null
				|| !(this.getKB() instanceof OWL2KnowledgeBase)) {
			Debug.println(this.getClass(), "No KB to commit...");
			return;
		}
		
//		// extract MEBN in order to extract storage implementor
//		MultiEntityBayesianNetwork mebn = ((OWL2KnowledgeBase)this.getKB()).getDefaultMEBN();
//		if (mebn == null 
//				|| mebn.getStorageImplementor() == null
//				|| !(mebn.getStorageImplementor() instanceof ProtegeStorageImplementorDecorator)) {
//			Debug.println(this.getClass(), "No Storage implementor to commit...");
//			return;
//		}
		
		try {
			Enumeration<AbstractButton> buttonGroupElements = this.getReasonerButtonGroup().getElements();
			while (buttonGroupElements.hasMoreElements()) {
				JRadioButtonMenuItemWithReasonerInfo selectedMenu = (JRadioButtonMenuItemWithReasonerInfo)buttonGroupElements.nextElement();
				
				if (selectedMenu.isSelected()) {
					// extract the reasoner ID from the selected radio button (it is stored in its name)
//					final String reasonerID = selectedMenu.getName();
					
//					// create a stub OWLReasoner which its name is a Protege plugin ID (this is similar to a bundle ID in OSGi vocabulary).
//					// This is a workaround in order to send a protege plugin ID as an argument to ProtegeStorageImplementorDecorator#setOWLReasoner() without changing its interface
//					// It was needed because protege seems not to offer enough services to consistently change reasoners that was not previously loaded as a Protege plugin
//					// (so, only the reasoners already loaded by Protege/OSGi can be used).
//					// TODO find out a better solution to update Protege's reasoner 
//					OWLReasoner stubReasonerJustToSendAProtegePluginID = new NoOpReasoner(((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee()) {
//						/** It returns the reasonerID. If reasonerID is null, it just delegates to the superclass */
//						public String getReasonerName() { return (reasonerID==null)?(super.getReasonerName()):reasonerID; }
//					};
//					
//					// update current reasoner using storage implementor (it is deprecated, but it does what we want - delegate to protege plug-ins)...
//					((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).setOWLReasoner(stubReasonerJustToSendAProtegePluginID);
					
//					// because our knowledge base may not use the same reasoner from the storage implementor, we must explicitly make them synchronized.
//					((OWL2KnowledgeBase)this.getKB()).setDefaultOWLReasoner(((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLReasoner());
					
					// let the KB to build the reasoner
					((OWL2KnowledgeBase)this.getKB()).setDefaultOWLReasoner(((OWL2KnowledgeBase)this.getKB()).buildOWLReasoner(selectedMenu.getReasonerInfo()));
					
					// we do need to check if the new reasoner is equal to the last one, because we want that re-selecting the reasoner re-triggers the initialization again.
					// (i.e. the above ProtegeStorageImplementorDecorator#setOWLReasoner() is going to call OWLReasonerManager#classifyAsynchronously too)
					
					// refresh this component, so that the OWLAPI portion of this GUI (i.e. the one generated by createOWLAPIReasonerOptionPanel()) is updated too.
//					try {
//						this.updateUI();
//						this.repaint();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
					
					// just use the first selected reasoner if multiple could be selected (it should be impossible - it is a radio button)
					break;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this, 
					e.getMessage(), 
					UIManager.getString("OptionPane.messageDialogTitle"), 
					JOptionPane.ERROR_MESSAGE);
		}
//		System.gc();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder#discardChanges()
	 */
	public void discardChanges() {
		// do nothing.
		Debug.println(this.getClass(), "Nothing is nedded to undo changes in reasoners.");
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
		// we must hide such panels because they are elements to be editted by Protege 4.1 panel
		if (this.getKB()  instanceof OWL2KnowledgeBase) {
			OWL2KnowledgeBase owl2KnowledgeBase = (OWL2KnowledgeBase) this.getKB();
			// update commands if it exists
			if (owl2KnowledgeBase.getClearKBCommandList() != null) {
				// this will act like a parameter for the commands
				final IMEBNMediator mediator = owl2KnowledgeBase.getDefaultMediator();
				// disable/enable entity panel, individuals panel and finding panel
				owl2KnowledgeBase.getClearKBCommandList().add(new OWL2KnowledgeBase.IClearKBCommand() {
					public void doCommand() {
						if (isToHideEntityButtons()) {
							mediator.getMebnEditionPane().getBtnTabOptionEntity().setEnabled(false);
							mediator.getMebnEditionPane().getBtnTabOptionEntityFinding().setEnabled(false);
							mediator.getMebnEditionPane().getBtnTabOptionNodeFinding().setEnabled(false);
							mediator.getMebnEditionPane().getBtnTabOptionTree().doClick(); // change view to MTheoryTree
						}
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
	@Override
	public void updateUI() {
		
		JPanel newView = new JPanel(new GridLayout(0, 1));
		newView.setBackground(Color.WHITE);
		newView.setBorder(new TitledBorder(this.getName()));
		
		// add reasoners from OWL API
		try {
			JComponent owlAPIOptionPanel = this.createOWLAPIReasonerOptionPanel(this.getKB());
			if (owlAPIOptionPanel != null) {
				newView.add(owlAPIOptionPanel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// add reasoners from protege 4.1 plugins
		try {
			JComponent protege41component = this.createProtege41ReasonerOptionPanel(this.getKB());
			if (protege41component != null) {
				newView.add(protege41component);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.setViewportView(newView);
		
		super.updateUI();
		
		this.repaint();
		
//		System.gc();
	}

	/**
	 * @return the reasonerButtonGroup
	 */
	public ButtonGroup getReasonerButtonGroup() {
		return reasonerButtonGroup;
	}

	/**
	 * @param reasonerButtonGroup the reasonerButtonGroup to set
	 */
	public void setReasonerButtonGroup(ButtonGroup reasonerButtonGroup) {
		this.reasonerButtonGroup = reasonerButtonGroup;
	}

	/**
	 * It holds the last panel generated by {@link #createOWLAPIReasonerOptionPanel(KnowledgeBase)}
	 * @return the oWLAPIReasonerOptionPanel
	 */
	public JPanel getOWLAPIReasonerOptionPanel() {
		return owlAPIReasonerOptionPanel;
	}

	/**
	 * It holds the last panel generated by {@link #createOWLAPIReasonerOptionPanel(KnowledgeBase)}
	 * @param oWLAPIReasonerOptionPanel the oWLAPIReasonerOptionPanel to set
	 */
	public void setOWLAPIReasonerOptionPanel(JPanel oWLAPIReasonerOptionPanel) {
		owlAPIReasonerOptionPanel = oWLAPIReasonerOptionPanel;
	}

	/**
	 * It holds the last panel generated by {@link #createProtege41ReasonerOptionPanel(KnowledgeBase)}
	 * @return the protege41ReasonerOptionPanel
	 */
	protected JPanel getProtege41ReasonerOptionPanel() {
		return protege41ReasonerOptionPanel;
	}

	/**
	 * It holds the last panel generated by {@link #createProtege41ReasonerOptionPanel(KnowledgeBase)}
	 * @param protege41ReasonerOptionPanel the protege41ReasonerOptionPanel to set
	 */
	protected void setProtege41ReasonerOptionPanel(
			JPanel protege41ReasonerOptionPanel) {
		this.protege41ReasonerOptionPanel = protege41ReasonerOptionPanel;
	}

	/**
	 * @return the oWLAPIReasonerLabel
	 */
	public JLabel getOWLAPIReasonerLabel() {
		return owlAPIReasonerLabel;
	}

	/**
	 * @param oWLAPIReasonerLabel the oWLAPIReasonerLabel to set
	 */
	public void setOWLAPIReasonerLabel(JLabel oWLAPIReasonerLabel) {
		owlAPIReasonerLabel = oWLAPIReasonerLabel;
	}

	/**
	 * @return if true, {@link #setKB(KnowledgeBase)} will attempt to hide buttons related to entities (because entities will be editted in protege panel)
	 * 
	 */
	public boolean isToHideEntityButtons() {
		return isToHideEntityButtons;
	}

	/**
	 * @param isToHideEntityButtons: if true, {@link #setKB(KnowledgeBase)} will attempt to hide buttons related to entities (because entities will be editted in protege panel)
	 */
	public void setToHideEntityButtons(boolean isToHideEntityButtons) {
		this.isToHideEntityButtons = isToHideEntityButtons;
	}

	

//	/**
//	 * @return the previouslySelectedReasonerItem
//	 */
//	protected JRadioButtonMenuItem getPreviouslySelectedReasonerItem() {
//		return previouslySelectedReasonerItem;
//	}
//
//	/**
//	 * @param previouslySelectedReasonerItem the previouslySelectedReasonerItem to set
//	 */
//	protected void setPreviouslySelectedReasonerItem(
//			JRadioButtonMenuItem previouslySelectedReasonerItem) {
//		this.previouslySelectedReasonerItem = previouslySelectedReasonerItem;
//	}
	
	

}
