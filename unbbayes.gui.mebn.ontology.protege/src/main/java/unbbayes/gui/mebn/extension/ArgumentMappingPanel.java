/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import edu.stanford.smi.protegex.owl.model.OWLOntology;

/**
 * This is a panel for mapping arguments of RVs to domains and ranges of
 * owl properties
 * @author Shou Matsumoto
 *
 */
public class ArgumentMappingPanel extends JPanel {

	private  MultiEntityBayesianNetwork mebn;
	private  IMEBNMediator mediator;
	private  INode selectedNode;
	private  OWLOntology ontology;
	
	/**
	 * Default constructor is not private in order to allow inheritance.
	 * @deprecated use {@link #newInstance(MultiEntityBayesianNetwork, IMEBNMediator, ResidentNode, OWLOntology)} instead
	 */
	protected ArgumentMappingPanel() {
		super();
		// TODO Auto-generated constructor stub
	}


//	protected ArgumentMappingPanel(boolean isDoubleBuffered) {
//		super(isDoubleBuffered);
//		// TODO Auto-generated constructor stub
//	}
//
//
//	protected ArgumentMappingPanel(LayoutManager layout, boolean isDoubleBuffered) {
//		super(layout, isDoubleBuffered);
//		// TODO Auto-generated constructor stub
//	}
//
//
//	protected ArgumentMappingPanel(LayoutManager layout) {
//		super(layout);
//		// TODO Auto-generated constructor stub
//	}


	/**
	 * Default constructor method
	 * @param mebn
	 * @param mediator
	 * @param selectedNode
	 * @param ontology
	 * @return
	 */
	public static ArgumentMappingPanel newInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator, INode selectedNode) {
		ArgumentMappingPanel ret =  new ArgumentMappingPanel();
		ret.setMebn(mebn);
		ret.setMediator(mediator);
		ret.setSelectedNode(selectedNode);
		// TODO extract ontology
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret.initComponents();
		ret.initListeners();
		return ret;
	}


	/**
	 * Initialize content of this panel
	 */
	protected void initComponents() {
		this.setBackground(Color.WHITE);
		this.add(new JScrollPane(new JLabel("TODO: list arguments, mappings, and button to edit mappings.")));
		// TODO use contents in IRIAwareMEBN
	}

	/**
	 * Initialize listeners of components created in {@link #initComponents()}
	 */
	protected void initListeners() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * @return the mebn
	 */
	public MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}

	/**
	 * @param mebn the mebn to set
	 */
	public void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return mediator;
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(IMEBNMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @return the selectedNode
	 */
	public INode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * @param selectedNode the selectedNode to set
	 */
	public void setSelectedNode(INode selectedNode) {
		this.selectedNode = selectedNode;
	}

	/**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * @param ontology the ontology to set
	 */
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}

}
