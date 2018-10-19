/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.owlapi.DefaultNonPROWL2ClassExtractor;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO;
import unbbayes.io.mebn.protege.Protege41CompatiblePROWL2IO;
import unbbayes.io.mebn.protege.ProtegeStorageImplementorDecorator;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityContainer;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLReasonerInfo;
import unbbayes.prs.mebn.entity.ontology.owlapi.ProtegeOWLReasonerInfoAdapter;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.util.Debug;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * PR-OWL2 specific reasoning requirements.
 * @author Shou Matsumoto
 *
 */
public class PROWL2KnowledgeBase extends OWL2KnowledgeBase {
	
	private boolean isToUseSameOntology = true;
	
	private OWLAPICompatiblePROWLIO prowlIO = (OWLAPICompatiblePROWLIO)Protege41CompatiblePROWL2IO.newInstance();

	private MultiEntityBayesianNetwork lastLoadedOntologyAsMEBNInstance;

	/**
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	public PROWL2KnowledgeBase() {
		super();
		// change prowl class extractor so that it uses PROWL2-specific routines
		this.setNonPROWLClassExtractor(DefaultNonPROWL2ClassExtractor.getInstance());
	}
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(null, mebn, mediator);
	}

	/**
	 * Constructor method initializing fields
	 * @param reasoner : explicitly sets the value of {@link #getDefaultOWLReasoner()}; If null, {@link #getDefaultOWLReasoner()} will be extracted from {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * of {@link #getDefaultMEBN()}.
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(OWLReasoner reasoner, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		OWL2KnowledgeBase ret = new PROWL2KnowledgeBase();
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
	
//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getDefaultOWLReasoner()
//	 */
//	public OWLReasoner getDefaultOWLReasoner() {
//		// TODO Auto-generated method stub
//		return super.getDefaultOWLReasoner();
//	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getOWLModelManager()
	 */
	public OWLModelManager getOWLModelManager() {
		// use the MEBN object loaded by #loadModule, rather than using the current MTheory
		MultiEntityBayesianNetwork mebn = getLastLoadedOntology();
		
		try {
			if (mebn != null
					&& mebn.getStorageImplementor() != null 
					&& mebn.getStorageImplementor() instanceof ProtegeStorageImplementorDecorator) {
				return ((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLEditorKit().getModelManager();
			}
		} catch (Throwable t) {
			// it is OK, because we can try extracting the reasoner when KB methods are called and MEBN is passed as arguments
			Debug.println(this.getClass(), "Could not extract reasoner from mebn " + mebn, t);
		}
		return null;
	}
	
//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getProwlModelUserDelegator()
//	 */
//	public IPROWL2ModelUser getProwlModelUserDelegator() {
//		// TODO Auto-generated method stub
//		return super.getProwlModelUserDelegator();
//	}
	
	
//	/* (non-Javadoc)
//	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#setDefaultOWLReasoner(org.semanticweb.owlapi.reasoner.OWLReasoner)
//	 */
//	public void setDefaultOWLReasoner(OWLReasoner defaultOWLReasoner) {
//		// TODO Auto-generated method stub
//		super.setDefaultOWLReasoner(defaultOWLReasoner);
//	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getAvailableOWLReasonersInfo()
	 */
	public List<OWLReasonerInfo> getAvailableOWLReasonersInfo() {

		// simply use the ontology we have loaded previously from #loadModule, instead of using the default MEBN object managed by current program
		MultiEntityBayesianNetwork mebn = getLastLoadedOntology();
		
		
		// only create component if mebn is carring a protege storage implementor.
		if (mebn == null 
				|| mebn.getStorageImplementor() == null
				|| !(mebn.getStorageImplementor() instanceof ProtegeStorageImplementorDecorator)) {
			return null;
		}
		
		// extract implementor (if code reaches here, storage implementor is not null)
		ProtegeStorageImplementorDecorator protegeStorageImplementor = (ProtegeStorageImplementorDecorator)mebn.getStorageImplementor();
		
		// obtain reasoner info
		Set<ProtegeOWLReasonerInfo> installedReasonerFactories = protegeStorageImplementor.getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getInstalledReasonerFactories();
		if (installedReasonerFactories == null) {
			return Collections.emptyList();
		}
		
		// prepare the list to be returned
		List<OWLReasonerInfo> ret = new ArrayList<OWLReasonerInfo>(installedReasonerFactories.size());
		
		// fill the list to be returned
		for (ProtegeOWLReasonerInfo installedReasoner : installedReasonerFactories) {
			// use an adapter to convert protege's interface to the desired interface 
			ret.add(ProtegeOWLReasonerInfoAdapter.getInstance(installedReasoner));
		}
		
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#buildOWLReasoner(unbbayes.prs.mebn.entity.ontology.owlapi.OWLReasonerInfo)
	 */
	public OWLReasoner buildOWLReasoner(OWLReasonerInfo reasonerInfo) {
		if (reasonerInfo == null || reasonerInfo.getReasonerId() == null) {
			return null;
		}

		// extract MEBN in order to extract storage implementor. Use the one we loaded at #loadModule
		MultiEntityBayesianNetwork mebn = getLastLoadedOntology();
		if (mebn == null 
				|| mebn.getStorageImplementor() == null
				|| !(mebn.getStorageImplementor() instanceof ProtegeStorageImplementorDecorator)) {
			Debug.println(this.getClass(), "No Storage implementor to commit...");
			return null;
		}
		
		final String reasonerID = reasonerInfo.getReasonerId();

		// create a stub OWLReasoner which its name is a Protege plugin ID (this is similar to a bundle ID in OSGi vocabulary).
		// This is a workaround in order to send a protege plugin ID as an argument to ProtegeStorageImplementorDecorator#setOWLReasoner() without changing its interface
		// It was needed because protege seems not to offer enough services to consistently change reasoners that was not previously loaded as a Protege plugin
		// (so, only the reasoners already loaded by Protege/OSGi can be used).
		// TODO find out a better solution to update Protege's reasoner 
		OWLReasoner stubReasonerJustToSendAProtegePluginID = new NoOpReasoner(((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee()) {
			/** It returns the reasonerID. If reasonerID is null, it just delegates to the superclass */
			public String getReasonerName() { return (reasonerID==null)?(super.getReasonerName()):reasonerID; }
		};
		
		// update current reasoner using storage implementor (it is deprecated, but it does what we want - delegate to protege plug-ins)...
		((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).setOWLReasoner(stubReasonerJustToSendAProtegePluginID);
		
		// now that the reasoner managed by the storage implementor is up to date, return it
		return ((ProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLReasoner();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#supportsLocalFile(boolean)
	 */
	public boolean supportsLocalFile(boolean isLoad) {
		if (isToUseSameOntology()) {
			// just delegate to superclass
			return super.supportsLocalFile(isLoad);
		}
//		return isLoad;	// if loading, then return true. If saving, then return false.
		return true;	// allow saving as separate file
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getSupportedLocalFileExtension(boolean)
	 */
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		if (isToUseSameOntology() || !isLoad) {
			// if we are saving an ontology, or if we should re-use ontology, then just delegate to superclass
			return super.getSupportedLocalFileExtension(isLoad);
		}
		// this knowledge base supports OWL files
		String[] ret = {"owl"};
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getSupportedLocalFileDescription(boolean)
	 */
	public String getSupportedLocalFileDescription(boolean isLoad) {
		if (isToUseSameOntology()  || !isLoad) {
			// if we are saving an ontology, or if we should re-use ontology, then just delegate to superclass
			return super.getSupportedLocalFileDescription(isLoad);
		}
		// this knowledge base supports OWL files
		return "OWL ontology (.owl)";
	}
	
	/**
	 * @return the isToUseSameOntology : if true, then the ontology in {@link unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator#getAdaptee()} 
	 * at {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getStorageImplementor()} will be re-used in order to load or save findings and entity instances.
	 * If false, then a new ontology must be selected for loading or saving findings and entity instances.
	 */
	public boolean isToUseSameOntology() {
		return isToUseSameOntology;
	}
	
	/**
	 * @param isToUseSameOntology : if true, then the ontology in {@link unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator#getAdaptee()} 
	 * at {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork#getStorageImplementor()} will be re-used in order to load or save findings and entity instances.
	 * If false, then a new ontology must be selected for loading or saving findings and entity instances.
	 */
	public void setToUseSameOntology(boolean isToUseSameOntology) {
		this.isToUseSameOntology = isToUseSameOntology;
	}
	
	/**
	 * This method loads an ontology file and stores it to {@link #setLastLoadedOntology(MultiEntityBayesianNetwork)}.
	 * All other methods in this class will then potentially reference the loaded MEBN instance 
	 * (i.e. the object representing a PR-OWL 2 ontology -- which can also represent an OWL 2 ontology).
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#loadModule(java.io.File, boolean)
	 * @see #getLastLoadedOntology()
	 * @see #setLastLoadedOntology(MultiEntityBayesianNetwork)
	 */
	public void loadModule(File file, boolean findingModule) throws UBIOException {
		if (file == null) {
			super.loadModule(file, findingModule);
			return;
		}
		if (!file.exists() || !file.isFile()) {
			throw new UBIOException(file.getName() + " either does not exist or it is not a valid file.");
		}
		// extract the IO class to be used to load the file
		OWLAPICompatiblePROWLIO io = getPROWLIO();
		if (io == null) {
			throw new NullPointerException("No I/O object was specified. File = " + file.getName());
		} else if (file.getName().lastIndexOf('.') >= 0 && file.getName().length() > 1) {
			// extract the file extension
			String fileExtension = file.getName().substring(file.getName().lastIndexOf('.')+1, file.getName().length());
			if (fileExtension.isEmpty()) {
				throw new IllegalArgumentException(file.getName() + " is not a valid file name.");
			}
			// check if this I/O class can handle this file extension.
			boolean found = false;
			for (String extension : io.getSupportedFileExtensions(true)) {
				if (fileExtension.equalsIgnoreCase(extension)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException(file.getName() + " cannot be handled by the I/O class " + io.getClass().getName() );
			}
		}
		
		
		// load the ontology as a MEBN project (it may not be actually a correct/complete MEBN theory, but we consider a PR-OWL 2 project as an instance of MultiEntityBayesianNetwork class);
		try {
			// before we make any further change,
			// extract the ID of the reasoner we are currently using, so that we can use the same reasoner for the loaded ontology
			ProtegeOWLReasonerInfo currentReasonerInfo = getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
			
			// other methods in this class will start referencing the new MEBN object instead of the MTheory currently being edited
			MultiEntityBayesianNetwork findingMEBN = io.loadMebn(file);
			this.setLastLoadedOntology(findingMEBN);
			
			// make sure the finding ontology (the one we just loaded) imports the current MTheory (the ontology being edited by the user now);
			
			// this is where we will look for imports
			OWLOntology findingOntology = ((ProtegeStorageImplementorDecorator)findingMEBN.getStorageImplementor()).getAdaptee();
			
			// this is the ontology we will be looking for
			OWLOntology baseOntology = getDefaultOWLReasoner().getRootOntology();
			
			// search the import closure to see if the finding ontology imports the current base ontology
			boolean hasImport = false;
			for (OWLOntology importedOntology : findingOntology.getImportsClosure()) {
				// compare the IRI to check if they are equal
				if (importedOntology.getOntologyID().getOntologyIRI().equals(baseOntology.getOntologyID().getOntologyIRI())) {
					hasImport = true;
					break;
				}
			}
			if (!hasImport) {
				// force the finding ontology (the one we just loaded frojm I/O class) to import the base ontology (the one currently edited by the user)
				OWLImportsDeclaration importDeclaration = findingOntology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(baseOntology.getOntologyID().getOntologyIRI());
				findingOntology.getOWLOntologyManager().applyChange(new AddImport(findingOntology, importDeclaration));
			}
			
			
			// load entity instances (from findingMEBN to current MTheory), because caller doesn't seem to be loading them.
			
			// extract the object entity container of the finding ontology (the one we just loaded)
			ObjectEntityContainer entityContainerInFinding = findingMEBN.getObjectEntityContainer();
			
			// extract the object entity container of the MTheory (the one that is being edited by the user now) and iterate on object entities
			ObjectEntityContainer entityContainerInTBOX = getDefaultMEBN().getObjectEntityContainer();
//			for (ObjectEntity entityInMTheory : entityContainerInTBOX.getListEntity()) {
//				// check if there is an equivalent entity in the finding ontology we just loaded
//				ObjectEntity entityInFinding = entityContainerInFinding.getObjectEntityByName(entityInMTheory.getName());
//				if (entityInFinding == null) {
//					continue; // ignore entities not common to both
//				}
//				// add object entity instances of finding to MTheory 
//				for (ObjectEntityInstance findingInstance : entityInFinding.getInstanceList()) {
//					if (entityInMTheory.getInstanceByName(findingInstance.getName()) != null) {
//						Debug.println(getClass(), "Avoiding to add instance " + findingInstance + " more than 1 time to entity: " + entityInMTheory.getName());
//						continue;  // but avoid duplicate inclusion to same entity
//					}
//					try {
//						entityContainerInTBOX.addEntityInstance(entityInMTheory.addInstance(findingInstance.getName()));
//					} catch (Exception e) {
//						Debug.println(getClass(), "Unable to create instance " + findingInstance.getName() 
//								+ " for class " + entityInMTheory.getName(), e);
//						continue;
//					}
//				}
//			}
			

			final OWLReasoner findingReasoner = ((ProtegeStorageImplementorDecorator)findingMEBN.getStorageImplementor()).getOWLReasoner();
			for (ObjectEntityInstance instanceInFinding : entityContainerInFinding.getListEntityInstances()) {
				// look for object entity with same name in tbox
				ObjectEntity entityInFinding = instanceInFinding.getInstanceOf();
				ObjectEntity respectiveEntityInTBOX = entityContainerInTBOX.getObjectEntityByName(entityInFinding.getName());
				if (respectiveEntityInTBOX == null) {
					// did not find equivalent class
					System.err.println("Could not find object entity of " + instanceInFinding + " in TBox. Ignoring.");
					continue;
				}
				
				Collection<ObjectEntity> compatibleEntitiesInTBox = this.getAncestorObjectEntities(entityContainerInTBOX,respectiveEntityInTBOX);
				compatibleEntitiesInTBox.add(respectiveEntityInTBOX);	// also consider self
				
				// register individuals to all compatible classes in TBox
				for (ObjectEntity entityInTBOX : compatibleEntitiesInTBox) {
					ObjectEntityInstance instanceInTBox = entityContainerInTBOX.getEntityInstanceByName(instanceInFinding.getName());
					if (instanceInTBox != null) {
						Debug.println(getClass(), "Instance was already inserted for another entity: " + instanceInTBox + ". Avoiding duplicates.");
						// make sure instance can be accessed from entity
						entityInTBOX.getInstanceList().add(instanceInTBox);
					} else {
						try {
							instanceInTBox = entityInTBOX.addInstance(instanceInFinding.getInstanceName());
							entityContainerInTBOX.addEntityInstance(instanceInTBox);
						} catch (TypeException | EntityInstanceAlreadyExistsException e) {
							Debug.println(getClass(),"Could not add instance" + instanceInFinding, e);
						}
					}
				}
				
				
			}
			
			
			// init reasoner
//			this.setDefaultOWLReasoner(buildOWLReasoner(ProtegeOWLReasonerInfoAdapter.getInstance(currentReasonerInfo)));
			this.setDefaultOWLReasoner(findingReasoner);
			
			// force this KB to use a class expression parser which is linked to finding ontology
			this.setOwlClassExpressionParserDelegator(OWLClassExpressionParserFacade.getInstance(((ProtegeStorageImplementorDecorator)findingMEBN.getStorageImplementor()).getOWLEditorKit().getModelManager()));
			
		} catch (IOException e) {
			throw new UBIOException(e);
		}
		
		
		
		// call superclass.
		super.loadModule(file, findingModule);
	}
	
	/**
	 * Recursively obtains parents of given object entity
	 * @param container
	 * @param entity
	 * @return
	 */
	protected Collection<ObjectEntity> getAncestorObjectEntities(ObjectEntityContainer container, ObjectEntity entity) {
		Collection<ObjectEntity> ret = new HashSet<ObjectEntity>();
		
		for (ObjectEntity parent : container.getParentsOfObjectEntity(entity)) {
			ret.add(parent);
			ret.addAll(getAncestorObjectEntities(container, parent));
		}
		
		return ret;
	}
	/**
	 * @return the prowlIO : this is the I/O class used to load OWL entities.
	 * @see #loadModule(File, boolean).
	 */
	public OWLAPICompatiblePROWLIO getPROWLIO() {
		return prowlIO;
	}
	
	/**
	 * @param prowlIO :  this is the I/O class used to load OWL entities.
	 * @see #loadModule(File, boolean).
	 */
	public void setPROWLIO(OWLAPICompatiblePROWLIO io) {
		this.prowlIO = io;
	}
	
	/**
	 * @return the ontology loaded at {@link #loadModule(File, boolean)}.
	 * This is an instance of {@link MultiEntityBayesianNetwork}, but it may not be necessarily a valid/complete MTheory.
	 * A PR-OWL 2 ontology (which is an extension of OWL 2 ontology) is generally represented as an instance of {@link MultiEntityBayesianNetwork} here.
	 * All other methods in this class will potentially reference the content of this object.
	 * This will return {@link #getDefaultMEBN()} if {@link #setLastLoadedOntology(MultiEntityBayesianNetwork)} was never called.
	 */
	public MultiEntityBayesianNetwork getLastLoadedOntology() {
		if (lastLoadedOntologyAsMEBNInstance == null) {
			return getDefaultMEBN();
		}
		return lastLoadedOntologyAsMEBNInstance;
	}
	
	/**
	 * @param lastLoadedOntologyAsMEBNInstance :  the ontology loaded at {@link #loadModule(File, boolean)}.
	 * This is an instance of {@link MultiEntityBayesianNetwork}, but it may not be necessarily a valid/complete MTheory.
	 * A PR-OWL 2 ontology (which is an extension of OWL 2 ontology) is generally represented as an instance of {@link MultiEntityBayesianNetwork} here.
	 * All other methods in this class will potentially reference the content of this object.
	 */
	public void setLastLoadedOntology(MultiEntityBayesianNetwork lastLoadedOntologyAsMEBNInstance) {
		this.lastLoadedOntologyAsMEBNInstance = lastLoadedOntologyAsMEBNInstance;
	}
	
	
	
	
}
