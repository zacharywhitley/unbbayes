/**
 * 
 */
package unbbayes.gui.featuremodel.extension;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.swing.JOptionPane;

import org.java.plugin.registry.PluginRegistry;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.ontology.IOWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.UnBBayesModuleBuilder;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This is a test application that simulates a 
 * automatic data provider for feature models described in OWL language
 * @author Shou Matsumoto
 *
 */
public class FeatureModelPanelBuilder extends UnBBayesModule implements UnBBayesModuleBuilder {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7172804402177124337L;

	private static int invocationCount = 0;
	
	private PrefixManager prefixManager;
	private IPROWL2ModelUser nameExtractor;

	private PrefixManager transitionModelPrefixManager;

	public FeatureModelPanelBuilder() {
		try {
			this.setFeatureModelPrefixManager(new DefaultPrefixManager("http://www.cic.unb.br/TES/2010/02/shou/unbbayesFeatureModel.owl"));
			this.setTransitionModelPrefixManager(new DefaultPrefixManager("http://www.cic.unb.br/TES/2010/02/shou/unbbayesConfigurationTransition.owl"));
			this.setNameExtractor(DefaultPROWL2ModelUser.getInstance());
		} catch (Throwable t) {
			Debug.println(getClass(), "Error during constructor", t);
		}
		this.setVisible(false);
	}

	public void doMagic() {
		try {
			// extract ontology and factory from the last created one
			OWLEditorKit kit =  (OWLEditorKit)ProtegeManager.getInstance().getEditorKitManager().getEditorKits().get(ProtegeManager.getInstance().getEditorKitManager().getEditorKitCount() - 1);
			OWLModelManager manager = kit.getOWLModelManager();
			OWLOntology ontology= manager.getActiveOntology();
			OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
			
			// this parser creates owl class expressions from string
			IOWLClassExpressionParserFacade expressionParser = OWLClassExpressionParserFacade.getInstance(manager);
			
			Debug.println("Current ontology is " + ontology);
			
			// superclass of any config
			OWLClass configuration = factory.getOWLClass("#Configuration", this.getFeatureModelPrefixManager());
			
			// new config to add
			String newConfigurationName = "#UnBBayes_" + new Date().toString();
			newConfigurationName = newConfigurationName.replace(' ', '_');
			newConfigurationName = newConfigurationName.replace(':', '_');
//			OWLClass newConfiguration = factory.getOWLClass(newConfigurationName, this.getPrefixManager());
			
			// new config is subclass of config
//			ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLSubClassOfAxiom(newConfiguration, configuration));
			
			String expression = "(has_unbbayes.core some unbbayes.core) \n";
			PluginRegistry registry = UnBBayesPluginContextHolder.newInstance().getPluginManager().getRegistry();
			
			// we must explicitly say all available and non available plugins
			String pluginID = "edu.gmu.seor.prognos.unbbayesplugin.cps";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.datamining";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			 pluginID = "unbbayes.gui.mebn.ontology.protege.2";
			expression += "and (" + ((registry.isPluginDescriptorAvailable("unbbayes.gui.mebn.ontology.protege"))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			 
			 pluginID = "unbbayes.io.NetIO";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.io.XMLBIFIO";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.io.mebn.UbfIO";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.io.mebn.UbfIO2";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.io.xmlbif.version7";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.learning";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.learning.incrementalLearning";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.metaphor";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.metaphor.afin";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.oobn.resources.ja";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.mebn.kb.powerloom";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.mebn.ssbn.ssmsbn";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.msbn";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.oobn";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.prm";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.simulation.likelihoodweighting.inference";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.simulation.montecarlo.sampling";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.simulation.sampling.GibbsSampling";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.io.DneIO";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			pluginID = "unbbayes.prs.mebn";
			expression += "and (" + ((registry.isPluginDescriptorAvailable(pluginID))?"(has_":"not (has_") + pluginID + " some " + pluginID + ")) \n";
			
			 Debug.println("Expression: \n" + expression);
			 
//			 ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(newConfiguration, expressionParser.parseExpression(expression)));
			 
			 // create individuals, because the ontology can only reason with some individual
			 OWLNamedIndividual individual = factory.getOWLNamedIndividual(newConfigurationName, this.getTransitionModelPrefixManager());
//			 ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLClassAssertionAxiom(newConfiguration, individual));
			 ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLClassAssertionAxiom(configuration, individual));
			 ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLClassAssertionAxiom(expressionParser.parseExpression(expression), individual));
			 
			 // add invocation count
			 OWLDataProperty hasInvocationCount = factory.getOWLDataProperty("#hasInvocationCount", this.getTransitionModelPrefixManager());
			 ontology.getOWLOntologyManager().addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(hasInvocationCount, individual, getInvocationCount()));
			 
			 manager.getOWLReasonerManager().classifyAsynchronously(manager.getOWLReasonerManager().getReasonerPreferences().getPrecomputedInferences());
			 
 			 // maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
 			 for (long i = 0; i < 100; i++) {
 				// TODO Stop using buzy waiting!!!
 				if (ReasonerStatus.NO_REASONER_FACTORY_CHOSEN.equals(manager.getOWLReasonerManager().getReasonerStatus())) {
 					// reasoner is not chosen...
 					Debug.println(this.getClass(), "No reasoner is chosen.");
 					break;
 				}
 				if (ReasonerStatus.INITIALIZED.equals(manager.getOWLReasonerManager().getReasonerStatus())) {
 					// reasoner is ready now
 					break;
 				}
 				Debug.println(this.getClass(), "Waiting for reasoner initialization...");
 				try {
 					// sleep and try reasoner status after
 					Thread.sleep(1000);
 				} catch (Throwable t) {
 					// a thread sleep should not break normal program flow...
 					Debug.println(getClass(), "Thread sleep has been interrupted", t);
 				}
 			 }
			 
			 if (!manager.getOWLReasonerManager().getCurrentReasoner().isConsistent()) {
//				 JOptionPane.showMessageDialog(FeatureModelPanelBuilder.this, "Inconsistent configuration");
				 return;
			 }
			 
			 // query suggestions
			 Set<OWLNamedIndividual> suggestions = manager.getOWLReasonerManager().getCurrentReasoner().getInstances(
					 expressionParser.parseExpression("inverse hasSuggestion value " + this.getNameExtractor().extractName(individual)), 
					 false
			 ).getFlattened();
			 
			 // get the property to extract URL
			 OWLDataProperty hasURL = factory.getOWLDataProperty("#hasPluginURL", this.getFeatureModelPrefixManager());
			 
			 // extract suggestion
			 String suggestionMessage = null;
			 if (suggestions != null && !suggestions.isEmpty()) {
				 suggestionMessage = "The following plugins are going to be downloaded: \n";
				 for (OWLNamedIndividual suggestion : suggestions) {
					 Set<OWLLiteral> literals = manager.getOWLReasonerManager().getCurrentReasoner().getDataPropertyValues(suggestion, hasURL);
					 if (!literals.isEmpty()) {
						 suggestionMessage += "\t" + literals.iterator().next().getLiteral() + " \n";
					 }
				 }
			 }
			 
			 // show suggestions
			 if (suggestionMessage != null && suggestionMessage.length() > 0) {
				 int option = JOptionPane.showConfirmDialog(FeatureModelPanelBuilder.this, suggestionMessage);
				 // show that suggestions are going to download if user has chosen it
				 if (option == JOptionPane.OK_OPTION) {
					 try {
						 Thread.sleep(2000);
					 } catch (Exception e) {
						 Debug.println(getClass(), "Thread sleep has been interrupted", e);
					 }
					 JOptionPane.showMessageDialog(FeatureModelPanelBuilder.this, "Download Completed!");
				 }
			 } else {
				 JOptionPane.showMessageDialog(FeatureModelPanelBuilder.this, "No suggestions");
			 }
			 
		} catch (NullPointerException n) {
			Debug.println(getClass(), "No reasoner instance found. Please, initialize some instance of unbbayes.gui.ontology.protege", n);
			JOptionPane.showMessageDialog(FeatureModelPanelBuilder.this, "No reasoner instance found. Please, initialize some instance of unbbayes.gui.ontology.protege");
		} catch (Throwable t) {
			Debug.println(getClass(), "Error when instantiating reasoner", t);
			JOptionPane.showMessageDialog(FeatureModelPanelBuilder.this, t.getMessage());
		}
	}

	public BaseIO getIO() {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getPersistingGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModuleName() {
		return this.getName();
	}

	@Override
	public UnBBayesModule openFile(File file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the prefixManager
	 */
	public PrefixManager getFeatureModelPrefixManager() {
		return prefixManager;
	}

	/**
	 * @param prefixManager the prefixManager to set
	 */
	public void setFeatureModelPrefixManager(PrefixManager prefixManager) {
		this.prefixManager = prefixManager;
	}

	/**
	 * @return the nameExtractor
	 */
	public IPROWL2ModelUser getNameExtractor() {
		return nameExtractor;
	}

	/**
	 * @param nameExtractor the nameExtractor to set
	 */
	public void setNameExtractor(IPROWL2ModelUser nameExtractor) {
		this.nameExtractor = nameExtractor;
	}

	public UnBBayesModule buildUnBBayesModule() {
		this.setVisible(false);
		return this;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see unbbayes.util.extension.UnBBayesModule#setUnbbayesFrame(unbbayes.gui.UnBBayesFrame)
	 */
	public void setUnbbayesFrame(UnBBayesFrame unbbayesFrame) {
		super.setUnbbayesFrame(unbbayesFrame);
		try {
			// this is a workaround to trigger file choosers only when we know
			// UnBBayesFrame is available.
			if (this.getUnbbayesFrame() != null) {
				// if openFile was called once, getNetFileFromUnBBayesFrame will
				// return non-null value.
				this.doMagic();
			}
		} catch (Throwable t) {
			Debug.println(getClass(), "Error when setting UnBBayes frame", t);
			JOptionPane.showMessageDialog(getUnbbayesFrame(), 
					t.getMessage(), 
					"Unknown Error", 
					JOptionPane.ERROR_MESSAGE); 
		}
		
		// free
		unbbayesFrame.getDesktop().remove(this);
		this.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(false);
	}

	/**
	 * @return the invocationCount
	 */
	public static int getInvocationCount() {
		return invocationCount++;
	}

	/**
	 * @param invocationCount the invocationCount to set
	 */
	public static void setInvocationCount(int invocationCount) {
		FeatureModelPanelBuilder.invocationCount = invocationCount;
	}

	/**
	 * @return the transitionModelPrefixManager
	 */
	public PrefixManager getTransitionModelPrefixManager() {
		return transitionModelPrefixManager;
	}

	/**
	 * @param transitionModelPrefixManager the transitionModelPrefixManager to set
	 */
	public void setTransitionModelPrefixManager(
			PrefixManager transitionModelPrefixManager) {
		this.transitionModelPrefixManager = transitionModelPrefixManager;
	}

}
