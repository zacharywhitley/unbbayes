/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/**
 * Implements the interface MebnIO for the Pr-OWL format.
 * It's like a wrapper and facade for 
 * unbbayes.io.mebn.SaverPrOwlIO and unbbayes.io.mebn.LoaderPrOwlIO
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 2006/10/25
 * @see unbbayes.io.mebn.SaverPrOwlIO 
 * @see unbbayes.io.mebn.LoaderPrOwlIO
 */

public class PrOwlIO extends PROWLModelUser implements MebnIO {
	
	//public static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 
	public static final String PROWLMODELURI = "http://www.pr-owl.org/pr-owl.owl";
	
	public static final String FILEEXTENSION = "owl";
	
	/** Array with a single element {@link #FILEEXTENSION} */
	public static final String SUPPORTED_EXTENSIONS[] = {FILEEXTENSION};
	
	private OWLModel lastOwlModel = null;
	
	private LoaderPrOwlIO loader = new LoaderPrOwlIO(); 
	
	// list of instance names which are native to pr-owl definition
	//private Collection<String> untouchableInstanceNames = null;
	
	private String name = "Probabilistic Web Ontology Language";

	private SaverPrOwlIO saver = new SaverPrOwlIO();
	
	
	public PrOwlIO() {
		super();
		//this.untouchableInstanceNames = new ArrayList<String>();
	}




	/**
	 * clears pr-owl specific individuals.
	 * non-prowl ontology remains kept.
	 * @param model
	 */
	private void clearAllPROWLOntologyIndividuals(JenaOWLModel model) {
		Collection<OWLIndividual> individuals = null;
		Collection<String> untouchableIndividuals = this.getNamesOfAllModifiedLabels();
		
		for (String classNames : this.getNamesOfAllModifiedPROWLClasses()) {
			 try{
				 individuals = model.getOWLNamedClass(classNames).getInstances(false);
				 for (OWLIndividual individual : individuals) {
					try {
						if (!untouchableIndividuals.contains(individual.getName())) {
							//individual.delete();
							Debug.println(this.getClass(), "Removing all prowl individuals: " + individual.getName());
							model.deleteInstance(individual);
						}						
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			 } catch (Exception e2) {
				 e2.printStackTrace();
				 continue;
			 }			
			 
		}

	}
	
	/**
	 * removes ObjectEntity's individuals and classes
	 * @param model
	 */
	private void clearAllObjectEntity(JenaOWLModel model) {
		Collection<OWLNamedClass> subclasses = null;
		OWLNamedClass objectEntity;
		
		try{
			objectEntity = model.getOWLNamedClass(OBJECT_ENTITY);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// delete individuals
		Collection<OWLIndividual> individuals = objectEntity.getInstances(true);
		for (OWLIndividual individual : individuals) {
			try {
				Debug.println(this.getClass(), "Clearing object entity individual: " + individual.getName());
				model.deleteInstance(individual);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		// delete classes
		try {
			subclasses = objectEntity.getSubclasses(true);
			for (OWLNamedClass namedClass : subclasses) {
				try {
					 Debug.println(this.getClass(), "Clearing object entity: " + namedClass.getName());
					 model.deleteCls(namedClass);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
	
	/**
	 * Load MEBN from a pr-owl file and stores it in the mebn structure. 
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException{
		MultiEntityBayesianNetwork mebn = loader.loadMebn(file);
		
		OWLModel lastOWLModel = loader.getLastOWLModel();
		
		// minimize on-memory ontology
		// the below code was postponed until we call routine to save the ontology
//		this.clearAllPROWLModel(lastOWLModel);
	
		mebn.setStorageImplementor(new MEBNStorageImplementorDecorator(lastOWLModel));
		
		/*try{
			Collection<String> errors = new ArrayList<String>();
			((JenaOWLModel)lastOWLModel).save((new File("PROWLIODEBUG.OWL")).toURI(), FileUtils.langXMLAbbrev, errors);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		this.setOWLModelToUse(lastOWLModel);	// not necessary, but just to stay consistent to the interface
 		return  mebn;
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		try {
			return this.loadMebn(input);
		} catch (IOMebnException e) {
			throw new UBIOException(e);
		}
	}




	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws IOException {
		this.saveMebn(output, (MultiEntityBayesianNetwork)net);
	}




	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */
	
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException{
	   
	   JenaOWLModel jenaOWLModel = null;
	   try{
		   MEBNStorageImplementorDecorator decorator = (MEBNStorageImplementorDecorator)mebn.getStorageImplementor();
		   if (decorator != null) {
			   jenaOWLModel = (JenaOWLModel)(decorator.getAdaptee());
			   // clear the old pr-owl model, so that we can save it without repeating elements and not to re-insert deleted elements
			   this.clearAllPROWLModel(((OWLModel)decorator.getAdaptee()));
		   }
	   } catch (Exception e) {
		   e.printStackTrace();
		   jenaOWLModel = null;
	   }
	   
	   
	   saver.saveMebn(file, mebn, jenaOWLModel); 
	   
	   // update reference to last edited ontology
	   try{
		   OWLModel model = saver.getLastOWLModel();
		   // minimize on-memory ontology
//		   this.clearAllPROWLModel(model);
		   this.setOWLModelToUse(model);
		   mebn.setStorageImplementor(new MEBNStorageImplementorDecorator(model));
	   } catch (Exception e) {
		   e.printStackTrace();
	   }
	   
	   
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#getFileExtension()
	 */
	public String getFileExtension() {
		return this.getFileExtension();
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IProtegeOWLModelUser#getLastOWLModel()
	 */
	public OWLModel getLastOWLModel() {
		return this.lastOwlModel;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IProtegeOWLModelUser#setOWLModelToUse(edu.stanford.smi.protegex.owl.model.OWLModel)
	 */
	public void setOWLModelToUse(OWLModel model) throws IOMebnException {
		this.lastOwlModel = model;
	}




	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.PROWLModelUser#getNamesOfAllModifiedPROWLClasses()
	 */
	@Override
	public Collection<String> getNamesOfAllModifiedPROWLClasses() {
		Collection<String> ret = super.getNamesOfAllModifiedPROWLClasses();
		// remove classes that we should not touch (they are coupled with pr-owl definition)
		ret.remove(BUILTIN_RV);
		ret.remove(BOOLEAN_STATE);
		
		return ret;
	}


	/**
	 * Clears all PR-OWL ontology components within a JenaOWLModel.
	 * Currently, only local OWL ontology (using JenaOWLModel) is supported
	 * @param model OWLModel which some PR-OWL ontology is present
	 */
	public void clearAllPROWLModel(OWLModel model){
		 // minimize memory-stored ontology
		try{
			this.clearAllPROWLOntologyIndividuals((JenaOWLModel)model);
			this.clearAllObjectEntity((JenaOWLModel)model);
		} catch (Exception e) {
			// This is not a major problem yet. Lets go
			e.printStackTrace();
		}
	   
	}




	public LoaderPrOwlIO getLoader() {
		return loader;
	}



	/**
	 * Cheks file extension compatibility.
	 * @see #supports(File, boolean)
	 * @param extension
	 * @param isLoadOnly
	 * @return
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		return FILEEXTENSION.equalsIgnoreCase(extension);
	}




	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS;
	}




	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return "PR-OWL (.owl)";
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		String fileExtension = null;
		try {
			int index = file.getName().lastIndexOf(".");
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return this.supports(fileExtension, isLoadOnly);
	}




	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}




	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}




	/**
	 * @return the lastOwlModel
	 */
	public OWLModel getLastOwlModel() {
		return lastOwlModel;
	}




	/**
	 * @param lastOwlModel the lastOwlModel to set
	 */
	public void setLastOwlModel(OWLModel lastOwlModel) {
		this.lastOwlModel = lastOwlModel;
	}




	/**
	 * @param loader the loader to set
	 */
	public void setLoader(LoaderPrOwlIO loader) {
		this.loader = loader;
	}




	/**
	 * @return the saver
	 */
	public SaverPrOwlIO getSaver() {
		return saver;
	}




	/**
	 * @param saver the saver to set
	 */
	public void setSaver(SaverPrOwlIO saver) {
		this.saver = saver;
	}
	
	
	
}