/**
 * 
 */
package unbbayes.io.medg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import unbbayes.io.mebn.LoaderPrOwlIO;
import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.medg.MultiEntityDecisionNode;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.util.Debug;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

/**
 * This is an IO class for storing and loading PR-OWL 1 with continuous LPD scripts.
 * @author Shou Matsumoto
 *
 */
public class PROWLDecisionIO extends PrOwlIO implements IPROWLDecisionModelUser {
	
	public static final String MEDG_DECISION_NODE = "Domain_Decision";
	public static final String MEDG_UTILITY_NODE = "Domain_Utility";
	
	/** This is the default location of PR-OWL Decision model/definition file. */
	public static final String DEFAULT_PROWL_DECISION_MODELFILE = "pr-owl/pr-owl-decision.owl";

	private String prowlDecisionModelFile = DEFAULT_PROWL_DECISION_MODELFILE;
	
	/**
	 * @deprecated default constructor is protected only to allow inheritance.
	 * Use {@link #getInstance()} instead.
	 */
	protected PROWLDecisionIO() {
		try {
			this.setLoader(new PROWLDecisionIOLoader());
		} catch (Exception e) {
			try {
				Debug.println(getClass(), e.getMessage() ,e);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		try {
			this.setSaver(new PROWLDecisionIOSaver());
		} catch (Exception e) {
			try {
				Debug.println(getClass(), e.getMessage() ,e);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * Default constructor method
	 * @return
	 */
	public static MebnIO getInstance() {
		// TODO Auto-generated method stub
		return new PROWLDecisionIO();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.PrOwlIO#getNamesOfAllModifiedPROWLClasses()
	 */
	public Collection<String> getNamesOfAllModifiedPROWLClasses() {
		// by overriding this method, we can make sure that individuals of decision/utility nodes
		// are cleared (so that old instances do not appear after saving ontology again)
		Collection<String> ret = super.getNamesOfAllModifiedPROWLClasses();
		ret.add(MEDG_DECISION_NODE);
		return ret;
	}
	
	/**
	 * @return the prowlDecisionModelFile : this is the location of the pr-owl-decision.owl, file which will be
	 *  used by {@link PROWLDecisionIOSaver#loadPrOwlModel(JenaOWLModel)}
	 * in order to load the PR-OWL Decision definitions/schemes.
	 */
	public String getPROWLDecisionModelFile() {
		return prowlDecisionModelFile;
	}

	/**
	 * @param prowlDecisionModelFile the prowlDecisionModelFile to set : this is the location of the pr-owl-decision.owl, file which will be
	 *  used by {@link PROWLDecisionIOSaver#loadPrOwlModel(JenaOWLModel)}
	 * in order to load the PR-OWL Decision definitions/schemes.
	 */
	public void setPROWLDecisionModelFile(String prowlDecisionModelFile) {
		this.prowlDecisionModelFile = prowlDecisionModelFile;
	}

	/**
	 * This is a customization of {@link LoaderPrOwlIO} which
	 * handles MEDG.
	 * @author Shou Matsumoto
	 * @see PROWLDecisionIOSaver
	 */
	public class PROWLDecisionIOLoader extends LoaderPrOwlIO {
		
		/*
		 * (non-Javadoc)
		 * @see unbbayes.io.mebn.LoaderPrOwlIO#loadDomainResidentNode()
		 */
		protected void loadDomainResidentNode() throws IOMebnException {

			MFrag domainMFrag;
			ResidentNode domainResidentNode;
			InputNode generativeInputNode;
			Argument argument;
			MultiEntityNode multiEntityNode;

			RDFIndividual individualOne;
			RDFIndividual individualTwo;
			OWLObjectProperty objectProperty;

			OWLNamedClass domainResidentNodePr = this.getOwlModel().getOWLNamedClass(DOMAIN_RESIDENT);
			Collection instances = domainResidentNodePr.getInstances(true);
			MFrag mFragOfNode = null;

			for (Iterator it = instances.iterator(); it.hasNext();) {

				individualOne = (RDFIndividual) it.next();
				domainResidentNode = getMapDomainResidentNode().get(individualOne.getBrowserText());
				if (domainResidentNode == null) {
					throw new IOMebnException(
							getResource().getString("DomainResidentNotExistsInMTheory"),
							individualOne.getBrowserText());
				}

				Debug.println("Domain Resident loaded: "
						+ individualOne.getBrowserText());

				domainResidentNode.setDescription(getDescription(individualOne));

				/* -> isResidentNodeIn */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("isResidentNodeIn");
				instances = individualOne.getPropertyValues(objectProperty);
				Iterator itAux = instances.iterator();
				individualTwo = (RDFIndividual) itAux.next();
				domainMFrag = getMapDomainMFrag().get(individualTwo.getBrowserText());
				if (domainMFrag.containsDomainResidentNode(domainResidentNode) == false) {
					throw new IOMebnException(
							getResource().getString("DomainResidentNotExistsInDomainMFrag"));
				}
				mFragOfNode = domainMFrag;
				Debug.println("-> " + individualOne.getBrowserText() + ": "
						+ objectProperty.getBrowserText() + " = "
						+ individualTwo.getBrowserText());

				/* -> hasArgument */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasArgument");
				instances = individualOne.getPropertyValues(objectProperty);
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					argument = new Argument(individualTwo.getBrowserText(),	domainResidentNode);
					domainResidentNode.addArgument(argument);
					getMapArgument().put(individualTwo.getBrowserText(), argument);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasParent */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasParent");
				instances = individualOne.getPropertyValues(objectProperty);
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					if (getMapDomainResidentNode().containsKey(individualTwo.getBrowserText())) {
						ResidentNode aux = getMapDomainResidentNode().get(individualTwo.getBrowserText());

						Edge auxEdge = new Edge(aux, domainResidentNode);
						try {
							mFragOfNode.addEdge(auxEdge);
						} catch (Exception e) {
							Debug.println("Erro: arco invalido!!!");
						}
					} else {
						if (getMapGenerativeInputNode().containsKey(individualTwo.getBrowserText())) {
							InputNode aux = getMapGenerativeInputNode().get(individualTwo.getBrowserText());

							Edge auxEdge = new Edge(aux, domainResidentNode);
							try {
								mFragOfNode.addEdge(auxEdge);
							} catch (Exception e) {
								Debug.println("Erro: arco invalido!!!");
							}

						} else {
							throw new IOMebnException(
									getResource().getString("NodeNotFound"),
									individualTwo.getBrowserText());
						}
					}
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasInputInstance */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasInputInstance");
				instances = individualOne.getPropertyValues(objectProperty);

				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					generativeInputNode = getMapGenerativeInputNode().get(individualTwo.getBrowserText());
					try {
						generativeInputNode.setInputInstanceOf(domainResidentNode);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> isInnerTermOf */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("isInnerTermOf");
				instances = individualOne.getPropertyValues(objectProperty);
				itAux = instances.iterator();
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					multiEntityNode = getMapMultiEntityNode().get(individualTwo.getBrowserText());
					domainResidentNode.addInnerTermFromList(multiEntityNode);
					multiEntityNode.addInnerTermOfList(domainResidentNode);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasPossibleValues */
				{
					CategoricalStateEntity state = null;
					objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasPossibleValues");
					instances = individualOne.getPropertyValues(objectProperty);
					itAux = instances.iterator();
					for (Object instance : instances) {
						individualTwo = (RDFIndividual) instance;
						String stateName = individualTwo.getBrowserText();
						/* case 1: booleans states */
						if (stateName.equals("true")) {
							StateLink link = domainResidentNode.addPossibleValueLink(
									getMebn().getBooleanStatesEntityContainer().getTrueStateEntity());
							List<String> globallyObjects = getMapBooleanStateGloballyObjects().get("true");
							if (globallyObjects.contains(domainResidentNode.getName())) {
								link.setGloballyExclusive(true);
							} else {
								link.setGloballyExclusive(false);
							}
							domainResidentNode.setTypeOfStates(IResidentNode.BOOLEAN_RV_STATES);
						} else {
							if (stateName.equals("false")) {
								StateLink link = domainResidentNode.addPossibleValueLink(
										getMebn().getBooleanStatesEntityContainer().getFalseStateEntity());
								List<String> globallyObjects = getMapBooleanStateGloballyObjects().get("false");
								if (globallyObjects.contains(domainResidentNode.getName())) {
									link.setGloballyExclusive(true);
								} else {
									link.setGloballyExclusive(false);
								}
								domainResidentNode.setTypeOfStates(IResidentNode.BOOLEAN_RV_STATES);
							} else {
								if (stateName.equals("absurd")) {
									StateLink link = domainResidentNode.addPossibleValueLink(
											getMebn().getBooleanStatesEntityContainer().getAbsurdStateEntity());
									List<String> globallyObjects = getMapBooleanStateGloballyObjects().get("absurd");
									if (globallyObjects.contains(domainResidentNode.getName())) {
										link.setGloballyExclusive(true);
									} else {
										link.setGloballyExclusive(false);
									}
									domainResidentNode.setTypeOfStates(IResidentNode.BOOLEAN_RV_STATES);
								} else {
									if (getMapTypes().get(stateName) != null) {

										/* case 2:object entities */

										StateLink link = domainResidentNode.addPossibleValueLink(getMapTypes().get(stateName));
										domainResidentNode.setTypeOfStates(IResidentNode.OBJECT_ENTITY);

									} else {
										/* case 3: categorical states */
										try {
											state = getMebn().getCategoricalStatesEntityContainer().getCategoricalState(
															individualTwo.getBrowserText());
											StateLink link = domainResidentNode.addPossibleValueLink(state);

											List<String> globallyObjects = getMapCategoricalStateGloballyObjects().get(state.getName());
											if (globallyObjects.contains(domainResidentNode.getName())) {
												link.setGloballyExclusive(true);
											} else {
												link.setGloballyExclusive(false);
											}
											domainResidentNode.setTypeOfStates(IResidentNode.CATEGORY_RV_STATES);
										} catch (CategoricalStateDoesNotExistException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

								}
							}
						}
					} /* for */

				}

				/* hasProbDist */

				OWLObjectProperty hasProbDist = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasProbDist");
				OWLDatatypeProperty hasDeclaration = getOwlModel().getOWLDatatypeProperty("hasDeclaration");
				String cpt = null;
				for (Iterator iter = individualOne.getPropertyValues(hasProbDist)
						.iterator(); iter.hasNext();) {
					RDFIndividual element = (RDFIndividual) iter.next();
					try {
						cpt = (String) element.getPropertyValue(hasDeclaration);
					} catch (Exception e) {
						cpt = "";
					}
					domainResidentNode.setTableFunction(cpt);
				}

				/* isArgTermIn is not checked */

			}

		} // end loadDomainResidentNode
		

		/*
		 * (non-Javadoc)
		 * @see unbbayes.io.mebn.LoaderPrOwlIO#loadDomainMFrag()
		 */
		protected void loadDomainMFrag() throws IOMebnException {

			MFrag domainMFrag;
			OrdinaryVariable oVariable;
			ContextNode contextNode;
			ResidentNode domainResidentNode;
			InputNode generativeInputNode;
			BuiltInRV builtInRV;

			RDFIndividual individualOne;
			RDFIndividual individualTwo;
			OWLNamedClass owlNamedClass;
			OWLObjectProperty objectProperty;

			owlNamedClass = getOwlModel().getOWLNamedClass(DOMAIN_MFRAG);
			Collection instances = owlNamedClass.getInstances(false);

			for (Iterator it = instances.iterator(); it.hasNext();) {
				individualOne = (RDFIndividual) it.next();
				domainMFrag = getMapDomainMFrag().get(individualOne.getBrowserText());
				if (domainMFrag == null) {
					throw new IOMebnException(
							getResource().getString("DomainMFragNotExistsInMTheory"),
							individualOne.getBrowserText());
				}

				Debug.println("DomainMFrag loaded: "
						+ individualOne.getBrowserText());

				domainMFrag.setDescription(getDescription(individualOne));

				/* -> hasResidentNode */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasResidentNode");
				instances = individualOne.getPropertyValues(objectProperty);
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					Object itInNext = itIn.next();
					if (!(itInNext instanceof RDFIndividual)) {
						try {
							System.err.println(itInNext + " != RDFIndividual");
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;
					}
					individualTwo = (RDFIndividual) itInNext;

					// remove prefixes from the name
					String name = individualTwo.getBrowserText();
					if (name.startsWith(SaverPrOwlIO.RESIDENT_NAME_PREFIX)) {
						try {
							name = name.substring(SaverPrOwlIO.RESIDENT_NAME_PREFIX
									.length());
						} catch (Exception e) {
							// ignore, because we can still try the original name
							e.printStackTrace();
						}
					}

					if (individualTwo.hasRDFType(getOwlModel().getOWLNamedClass(MEDG_DECISION_NODE), true)) {
						// Instantiate a decision resident node instead of resident node, if individual is assignable to a decision node
						domainResidentNode = new MultiEntityDecisionNode(name, domainMFrag);
//						try {
//							((MultiEntityDecisionNode)domainResidentNode).onAddToMFrag(domainMFrag);
//						} catch (MFragDoesNotExistException e) {
//							e.printStackTrace();
//						}
					} else if (individualTwo.hasRDFType(getOwlModel().getOWLNamedClass(MEDG_UTILITY_NODE), true)) {
						// Instantiate an utility resident node instead of resident node, if individual is assignable to a utility node
						domainResidentNode = new MultiEntityUtilityNode(name, domainMFrag);
					} else {
						// or else, by default it is a probabilistic resident node
						domainResidentNode = new ResidentNode(name, domainMFrag);
					}
					getMebn().getNamesUsed().add(name);

					domainMFrag.addResidentNode(domainResidentNode);

					// the mappings uses the original names (no prefix removal)
					getMapDomainResidentNode().put(individualTwo.getBrowserText(),
							domainResidentNode);
					getMapMultiEntityNode().put(individualTwo.getBrowserText(),
							domainResidentNode);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasInputNode */
				objectProperty = (OWLObjectProperty) getOwlModel().getOWLObjectProperty("hasInputNode");
				instances = individualOne.getPropertyValues(objectProperty);
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					generativeInputNode = new InputNode(
							individualTwo.getBrowserText(), domainMFrag);
					getMebn().getNamesUsed().add(individualTwo.getBrowserText());
					domainMFrag.addInputNode(generativeInputNode);
					getMapGenerativeInputNode().put(individualTwo.getBrowserText(),
							generativeInputNode);
					getMapMultiEntityNode().put(individualTwo.getBrowserText(),
							generativeInputNode);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasContextNode */
				objectProperty = (OWLObjectProperty) getOwlModel()
						.getOWLObjectProperty("hasContextNode");
				instances = individualOne.getPropertyValues(objectProperty);
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					contextNode = new ContextNode(individualTwo.getBrowserText(),
							domainMFrag);
					getMebn().getNamesUsed().add(individualTwo.getBrowserText());
					domainMFrag.addContextNode(contextNode);
					getMapContextNode().put(individualTwo.getBrowserText(), contextNode);
					getMapMultiEntityNode().put(individualTwo.getBrowserText(),
							contextNode);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}

				/* -> hasOVariable */
				objectProperty = (OWLObjectProperty) getOwlModel()
						.getOWLObjectProperty("hasOVariable");
				instances = individualOne.getPropertyValues(objectProperty);
				String ovName = null;
				for (Iterator itIn = instances.iterator(); itIn.hasNext();) {
					individualTwo = (RDFIndividual) itIn.next();
					ovName = individualTwo.getBrowserText(); // Name of the OV
																// individual
					// Remove MFrag name from ovName. MFrag name is a scope
					// identifier
					try {
						ovName = ovName.split(domainMFrag.getName()
								+ this.getOrdinaryVarScopeSeparator())[1];
					} catch (java.lang.ArrayIndexOutOfBoundsException e) {
						// Use the original name...
						ovName = ovName; // If its impossible to split, then no
											// Scope id was found
					}
					// Debug.println("> Internal OV name is : " + ovName);
					// Create instance of OV w/o scope identifier
					oVariable = new OrdinaryVariable(ovName, getMebn()
							.getTypeContainer().getDefaultType(), domainMFrag);
					domainMFrag.addOrdinaryVariable(oVariable);
					// let's map objects w/ scope identifier included
					getMapOVariable().put(individualTwo.getBrowserText(), oVariable);
					Debug.println("-> " + individualOne.getBrowserText() + ": "
							+ objectProperty.getBrowserText() + " = "
							+ individualTwo.getBrowserText());
				}
			}
		}
		
	}
	
	/**
	 * 
	 * This is a customization of {@link SaverPrOwlIO} which handles MEDG.
	 * @author Shou Matsumoto
	 * @see PROWLDecisionIOLoader
	 */
	public class PROWLDecisionIOSaver extends SaverPrOwlIO {
		
		

		/**
		 * We are extending the {@link unbbayes.io.mebn.SaverPrOwlIO#saveMTheory()} so that decision nodes
		 * and utility nodes are properly stored.
		 * There are some copy-pasted code here, but that's because the superclass did not make template methods
		 * available.
		 * 
		 * @see unbbayes.io.mebn.SaverPrOwlIO#saveMTheory()
		 */
		protected void saveMTheory(){

				OWLNamedClass mTheoryClass = getOwlModel().getOWLNamedClass(MTHEORY); 
				// check if individual exists
				OWLIndividual mTheoryIndividual = getOwlModel().getOWLIndividual(getMebn().getName());
				if (mTheoryIndividual == null) {
					// if this individual is new, create it
					mTheoryIndividual = mTheoryClass.createOWLIndividual(getMebn().getName()); 
				}
				Debug.println("MTheory = " + getMebn().getName());
				
				if(getMebn().getDescription() != null){
					mTheoryIndividual.addComment(getMebn().getDescription()); 
				}
				
				/* hasMFrag */
				// TODO remove magic strings from SaverPrOwlIO (the following code was copied/pasted from SaverPrOwlIO)
				OWLObjectProperty hasMFragProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("hasMFrag"); 	
				List<MFrag> listDomainMFrag = getMebn().getDomainMFragList(); 
				
				for(MFrag domainMFrag: listDomainMFrag){
					OWLNamedClass domainMFragClass = getOwlModel().getOWLNamedClass(DOMAIN_MFRAG); 
					Debug.println("Domain_MFrag = " + domainMFrag.getName());
					// check if individual exists.
					OWLIndividual domainMFragIndividual = getOwlModel().getOWLIndividual(this.MFRAG_NAME_PREFIX + domainMFrag.getName());
					if (domainMFragIndividual == null) {
						// if new, create it
						domainMFragIndividual = domainMFragClass.createOWLIndividual(this.MFRAG_NAME_PREFIX + domainMFrag.getName());
					}
					getMapMFrag().put(domainMFrag, domainMFragIndividual); 
					mTheoryIndividual.addPropertyValue(hasMFragProperty, domainMFragIndividual); 
					
					if(domainMFrag.getDescription()!=null){
						domainMFragIndividual.addComment(domainMFrag.getDescription()); 
					}
					
					/* hasResidentNode */
					OWLObjectProperty hasResidentNodeProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("hasResidentNode"); 	
					
					for(Node node: domainMFrag.getNodeList()){
						if (node instanceof ResidentNode) {
							ResidentNode residentNode = (ResidentNode) node;
							
							Debug.println("Domain_Res = " + residentNode.getName());	
							// check if individual exists
							OWLIndividual domainResIndividual = getOwlModel().getOWLIndividual(this.RESIDENT_NAME_PREFIX + residentNode.getName());
							if (domainResIndividual == null) {
								// if new, create it
								OWLNamedClass domainResClass = null;
								if (residentNode instanceof MultiEntityDecisionNode) {
									// get owl class for decision nodes
									domainResClass = getOwlModel().getOWLNamedClass(MEDG_DECISION_NODE);
									if (domainResClass == null) {
										// if MEDG_DECISION_NODE does not exist, create it
										domainResClass = getOwlModel().createOWLNamedSubclass(MEDG_DECISION_NODE, getOwlModel().getOWLNamedClass(DOMAIN_RESIDENT));
									}
								} else if (residentNode instanceof MultiEntityUtilityNode) {
									// get owl class for utility nodes
									domainResClass = getOwlModel().getOWLNamedClass(MEDG_UTILITY_NODE);
									if (domainResClass == null) {
										// if MEDG_UTILITY_NODE does not exist, create it
										domainResClass = getOwlModel().createOWLNamedSubclass(MEDG_UTILITY_NODE, getOwlModel().getOWLNamedClass(DOMAIN_RESIDENT));
									}
								} else {
									// get owl class for resident nodes
									domainResClass = getOwlModel().getOWLNamedClass(DOMAIN_RESIDENT); 
								}
								
								domainResIndividual = domainResClass.createOWLIndividual(RESIDENT_NAME_PREFIX + residentNode.getName());
							}
							domainMFragIndividual.addPropertyValue(hasResidentNodeProperty, domainResIndividual); 	
							getMapDomainResident().put(residentNode, domainResIndividual); 
						}
						
					}	
					
					/* hasInputNode */
					OWLObjectProperty hasInputNodeProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("hasInputNode"); 	
					OWLNamedClass generativeInputClass = getOwlModel().getOWLNamedClass(GENERATIVE_INPUT); 
					for(InputNode inputNode: domainMFrag.getInputNodeList()){
						Debug.println("Generative_input = " + inputNode.getName());
						// check if individuals exists
						OWLIndividual generativeInputIndividual = getOwlModel().getOWLIndividual(inputNode.getName());
						if (generativeInputIndividual == null) {
							// if new, create it
							generativeInputIndividual = generativeInputClass.createOWLIndividual(inputNode.getName());
						}
						domainMFragIndividual.addPropertyValue(hasInputNodeProperty, generativeInputIndividual); 		
						getMapGenerativeInput().put(inputNode, generativeInputIndividual); 		
					}				
					
					/* hasContextNode */
					OWLObjectProperty hasContextNodeProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("hasContextNode"); 	
					OWLNamedClass contextClass = getOwlModel().getOWLNamedClass(CONTEXT_NODE); 
					for(ContextNode contextNode: domainMFrag.getContextNodeList()){
						// check if individuals exist
						OWLIndividual contextIndividual = getOwlModel().getOWLIndividual(contextNode.getName());
						if (contextIndividual == null) {
							// if new, create it
							contextIndividual = contextClass.createOWLIndividual(contextNode.getName());
						}
						domainMFragIndividual.addPropertyValue(hasContextNodeProperty, contextIndividual); 									
						getMapContext().put(contextNode, contextIndividual); 	
					}				
					
					/* hasOVariable */
					OWLObjectProperty hasOVariableProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("hasOVariable"); 	
				    OWLNamedClass oVariableClass = getOwlModel().getOWLNamedClass(ORDINARY_VARIABLE); 
					OWLObjectProperty isSubsByProperty = (OWLObjectProperty)getOwlModel().getOWLObjectProperty("isSubsBy"); 	
					
					for(OrdinaryVariable oVariable: domainMFrag.getOrdinaryVariableList()){
						// Set variable name as "MFragName.OVName"
						OWLIndividual oVariableIndividual = getOwlModel().getOWLIndividual(
								  oVariable.getMFrag().getName() + SCOPE_SEPARATOR
									+ oVariable.getName() );
						if (oVariableIndividual == null) {
							oVariableIndividual = oVariableClass.createOWLIndividual(
										  oVariable.getMFrag().getName() + SCOPE_SEPARATOR
										+ oVariable.getName() );
						}
						domainMFragIndividual.addPropertyValue(hasOVariableProperty, oVariableIndividual); 		
						
						if (oVariable.getValueType() != null){
							oVariableIndividual.addPropertyValue(isSubsByProperty, getMapMetaEntity().get(oVariable.getValueType().getName())); 
						}
						
						if(oVariable.getDescription() != null){
							oVariableIndividual.addComment(oVariable.getDescription()); 	
						}
						
						getMapOrdinaryVariable().put(oVariable, oVariableIndividual); 				
					}				
				}    	
		    }

		/* (non-Javadoc)
		 * @see unbbayes.io.mebn.PROWLModelUser#loadPrOwlModel(edu.stanford.smi.protegex.owl.jena.JenaOWLModel)
		 */
		public JenaOWLModel loadPrOwlModel(JenaOWLModel owlModel) throws IOException {
			// first, load pr-owl model and keep it in memory, because pr-owl model is imported by pr-owl decision profile model.
			super.loadPrOwlModel(owlModel);
			File filePrOwl = null;
			try {
				filePrOwl = new File(this.getClass().getClassLoader().getResource(getPROWLDecisionModelFile()).toURI());
			} catch (Exception e1) {
				Debug.println(this.getClass(), "Could not load pr-owl-decision definitions from resource. Retry...", e1);
				try {
					// retrying using file on root instead
					filePrOwl = new File(getPROWLDecisionModelFile());
					if (!filePrOwl.exists()) {
						filePrOwl = null;
					}
				} catch (Exception e) {
					e1.printStackTrace();
					e.printStackTrace();
					throw new IOException(e.toString()); 
				}
			}
			
			owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, true));
			
			InputStream inputStreamOwl; 
			try{
				inputStreamOwl = this.getClass().getClassLoader().getResourceAsStream(getPROWLDecisionModelFile());
				if (inputStreamOwl == null) {
					inputStreamOwl = new FileInputStream(getPROWLDecisionModelFile());
				}
				owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
			} catch (Exception e){
				Debug.println(this.getClass(), "Could not load pr-owl-decision definitions from resource. Retry...", e);
				try {
					// retrying using file on root instead
					inputStreamOwl = new FileInputStream(getPROWLDecisionModelFile());
					owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
				} catch (Exception e2) {
					e2.printStackTrace();
					throw new IOException(e2.toString()); 
				}
			}			
			return owlModel;
		}

	}	// end public class PROWLDecisionIOSaver

	

}
