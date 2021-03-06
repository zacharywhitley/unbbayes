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
package unbbayes.prs.mebn.kb.powerloom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import unbbayes.io.exception.UBIOException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;
import edu.isi.powerloom.Environment;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;
import edu.isi.stella.Stella_Object;

/**
 * This class uses PowerLoom for building the knowledge base from the MTheory.
 * The KB will be used for evaluating the context nodes. The PowerLoom is used
 * as a reasoner for the first-order logic sentences defined in the context
 * nodes.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 * @author Shou Matsumoto (cardialfly@[yahoo,gmail].com)
 * @version 1.1 (2007/12/26)
 *
 */
public class PowerLoomKB implements KnowledgeBase {
	/*
	 * TODO
	 * Functions
	 *	This illustrates another point: A PowerLoom relation is by default "multi-valued" which in the case of a binary relation means that a single first value can be mapped by the relation to more than one second value. In the present case, our model permits a company entity to have more than one company-name. If a (binary) relation always maps its first argument to exactly one value (i.e., if it it 窶徭ingle-valued窶� we can specify it as a function instead of a relation. For example, we can use a function to indicate the number of employees for a company:
	 *	
	 *	 	
	 *	(deffunction number-of-employees ((?c company)) :-> (?n INTEGER))
	 *	When defining a function, all arguments but the last appear just as they do for a relation. The last argument (and its type) appears by itself following the keyword :->. Defining a single-valued relation as a function allows us to refer to it using a functional syntax within a logical sentence, as in the following:
	 *	
	 *	 	
	 *	(assert (= (number-of-employees ACME-cleaners) 8))
	 *	(assert (= (number-of-employees megasoft) 10000))
	 *	The functional syntax often results in shorter expressions than equivalents that use relational syntax. For example to retrieve all companies with fewer than 50 employees, we can simply write:
	 *	
	 *	 	
	 *	(retrieve all (and (company ?x) (< (number-of-employees ?x) 50)))
	 *	->
	 *	There is 1 solution:
	 *	  #1: ?X=ACME-CLEANERS
	 *	Using the syntax for relations, the same query would require the introduction of an existential quantifier, as in:
	 *	
	 *	 	
	 *	(retrieve all (and (company ?x) 
	 *	                   (exists ?n
	 *	                     (and (number-of-employees ?x ?n)
	 *	                          (< ?n 50)))))
	 *	->
	 *	There is 1 solution:
	 *	  #1: ?X=ACME-CLEANERS
	 *	To repeat ourselves slightly, Powerloom allows users the choice of using either relational or functional syntax when using a function in predicate position. For example, if f is a function, then the expressions (f ?x ?y) and (= (f ?x) ?y) are equivalent.
	 */

	public static final String FILE_SUFIX = "plm"; 
	
	private static int nextId = 1; 
	
	public static String PLI_TOKEN_SEPARATOR = "() \n\t\r";

	private static final String POWER_LOOM_KERNEL_MODULE = "/PL-KERNEL/PL-USER/";
	private static final boolean CASE_SENSITIVE = false;
	private static final String GENERATIVE_MODULE_NAME =  "GENERATIVE_MODULE"; 
	private static final String FINDING_MODULE_NAME =  "FINDINGS_MODULE"; 
	
	private int idInstance; 
	private String moduleGenerativeName;
	private String moduleFindingName;	
	private Module moduleGenerative;
	private Module moduleFinding;
	private String moduleName; 

	private Environment environment = null;

	private int maximumQueryAttemptCount = 3;
	
	private long maximumQueryAttemptWaitTime = 500;
	
	/* 
	 * Modules (hierarchy): 
	 * 
	 * - PL-USER
	 * - GENERATIVE_MODULE
	 * - FINDING_MODULE
	 */

	private static final String POSSIBLE_STATE_SUFIX = "_state";

	private MultiEntityBayesianNetwork mebn = null;

	private String wildCardSymbol = "?";

	/*
	 * Create a new instance of PowerLoomKB with the given id. The names of 
	 * modules of this instance is build using the id.  
	 */
	protected PowerLoomKB(int id) {

		initialize(); 
		idInstance = id; 
		
		moduleGenerativeName =  GENERATIVE_MODULE_NAME + "_" + idInstance; 
		moduleFindingName = FINDING_MODULE_NAME + "_" + idInstance; 
		
		Module fatherModule = PLI.getModule(POWER_LOOM_KERNEL_MODULE, environment);
		moduleGenerative = PLI.createModule(moduleGenerativeName, fatherModule,
                           CASE_SENSITIVE);
		moduleFinding = PLI.createModule(moduleFindingName, moduleGenerative,
				           CASE_SENSITIVE);

		Debug.println("Modules created successfully:");
		Debug.println(moduleGenerative.moduleFullName);
		Debug.println(moduleFinding.moduleFullName);
		
		PLI.sChangeModule(moduleFindingName, environment);

	}

	/**
	 * Initialize the Knowledge Base system
	 */
	public static void initialize(){
		Debug.println("Initializing...");
		PLI.initialize();
		Debug.println("Done.");
	}
	
	/** 
	 * @return One new Knowledge Base.
	 */
	public synchronized static PowerLoomKB getNewInstanceKB() {
		
		return new PowerLoomKB(nextId++); 
	
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Methods for save and load modules                                       */
	/*-------------------------------------------------------------------------*/	
	
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		Debug.println("Saving generative module...");
		PLI.sSaveModule(moduleGenerativeName, file.getAbsolutePath(),
				"REPLACE", environment);
		Debug.println("...File save sucefull");
	}

	/**
	 * @see KnowledgeBase
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		Debug.println("Saving finding module...");
		PLI.sSaveModule(moduleFindingName, file.getAbsolutePath(), "REPLACE",
				environment);
		Debug.println("...File save sucefull");
	}

	/**
	 * @see KnowledgeBase
	 */
	public synchronized void loadModule(File file, boolean findingModule) throws UBIOException{
		Debug.println("Loading module " + file);
		
		File tempFile = preLoad(file, findingModule);
		PLI.load(tempFile.getAbsolutePath(), environment);
		tempFile.delete(); 
		
		
		Debug.println("File load sucefull");
		
		// extract MEBN so that we can load entity instances
		MultiEntityBayesianNetwork mebn = getMEBN();
		if (mebn == null) {
			return;
		}
		
		// load entity instances and insert them to mebn
		for (ObjectEntity entity : mebn.getObjectEntityContainer().getListEntity()) {
			
			boolean hasOrder = entity.isOrdereable();	// use a local variable, so that we don't need to call isOrderable in next loop
			ObjectEntityInstanceOrdereable previousInOrder = null;	// this will be used if we find an entity with ordering
			
			for (String instanceName : getEntityByType(entity.getType().getName())) {
				ObjectEntityInstance instance = entity.getInstanceByName(instanceName);
				try {
					if (instance == null) {
						instance = entity.addInstance(instanceName);
					}
					// special treatment if instance has ordering
					if (hasOrder) {
						((ObjectEntityInstanceOrdereable)instance).setPrev(previousInOrder);
						if (previousInOrder != null) {
							previousInOrder.setProc((ObjectEntityInstanceOrdereable) instance);
						}
						previousInOrder = (ObjectEntityInstanceOrdereable) instance;
					}
					mebn.getObjectEntityContainer().addEntityInstance(instance);
					mebn.getNamesUsed().add(instanceName); 
					Debug.println(getClass(), "Loaded entity instance " + instance);
				} catch (Exception e) {
					Debug.println(getClass(), e.getClass().getName(), e);
				}
			}
		}
		
		Debug.println("Entity instances loaded from file: " + file);
	}

	/**
	 * @see KnowledgeBase#clearKnowledgeBase()
	 * @see #createGenerativeKnowledgeBase(MultiEntityBayesianNetwork)
	 */
	public void clearKnowledgeBase() {
		this.clearFindings();
		PLI.clearModule(moduleGenerative);
		
		
	}
	
	/**
	 * @see KnowledgeBase
	 */
	public void clearFindings() {
		PLI.clearModule(moduleFinding);
		
		
	}
		
	
	
	/*
	 * This method should be called before the load of a powerloom file. It is 
	 * necessary to correct the names of the modules loaded. 
	 * 
	 * @throws IOException 
	 */
	private File preLoad(File file, boolean findingModule) throws UBIOException{
		
		boolean continueReading = true;
		boolean endOfFile = false; 
		
		//The file will be read line per line and rewrite in a new file
		//with the modifications. 
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			throw new UBIOException(UBIOException.ED_CREATE_FILE, file.getName()); 
		}
		File newFile = new File("Temp.plm"); 
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(newFile));
		} catch (IOException e) {
			throw new UBIOException(UBIOException.ED_CREATE_FILE, newFile.getName()); 
		} 
	
		/*
		 * The lines before the first assert command will be removed and 
		 * substitute for the definitions of modules with the "id" for the
		 * instance of KB assigned to this findings set. 
		 */
		
		try{

			while(continueReading && !endOfFile){
				String line = reader.readLine();

				if(line == null){
					endOfFile = true; 
					break; 
				}

				Pattern defModulePattern = Pattern.compile(".*ASSERT.*");
				Matcher matcher = defModulePattern.matcher(line); 
				if(matcher.matches()){
					if(findingModule){
						writeModuleFindingDefinition(writer);
					}else{
						writeModuleGenerativeDefinition(writer);
					}
					writer.write(line); 
					writer.newLine(); 
					continueReading = false; 
				}
			}

			//Read the rest of the file
			if(!endOfFile){
				String line = null; 
				while((line = reader.readLine()) != null){
					writer.write(line);
					writer.newLine(); 
				}
			}

			writer.flush(); 
			writer.close(); 

		}
		catch(IOException e){
		     throw new UBIOException(UBIOException.ED_READWRITE_FILE);   
		}
	    
	    return newFile; 
		
	}
	
	/*
	 * write: 
	 * (CL:IN-PACKAGE "STELLA")
	 * (DEFMODULE "moduleFindingName": CASE-SENSITIVE? FALSE)
	 * (IN-MODULE "moduleFindingName")
	 * (IN-DIALECT :KIF)
	 */
	private void writeModuleFindingDefinition(BufferedWriter writer) throws IOException{
	    writer.write("(CL:IN-PACKAGE \"STELLA\")"); 
	    writer.newLine();
		writer.write("(DEFMODULE \"" + moduleFinding.moduleFullName + "\" :CASE-SENSITIVE? FALSE)"); 
    	writer.newLine(); 
	    writer.write("(IN-MODULE \""+ moduleFinding.moduleFullName + "\")"); 
	    writer.newLine();
	    writer.write("(IN-DIALECT :KIF)"); 
	    writer.newLine();
	}
	
	/*
	 * write:
	 * (CL:IN-PACKAGE "STELLA")
	 * (DEFMODULE "moduleGenerativeName": CASE-SENSITIVE? FALSE)
	 * (IN-MODULE "moduleGenerativeName")
	 * (IN-DIALECT :KIF)
	 */
	private void writeModuleGenerativeDefinition(BufferedWriter writer) throws IOException{
	    writer.write("(CL:IN-PACKAGE \"STELLA\")"); 
	    writer.newLine();
		writer.write("(DEFMODULE \"" + moduleGenerative.moduleFullName + "\" :CASE-SENSITIVE? FALSE)"); 
    	writer.newLine(); 
	    writer.write("(IN-MODULE \""+ moduleGenerative.moduleFullName + "\")"); 
	    writer.newLine();
	    writer.write("(IN-DIALECT :KIF)"); 
	    writer.newLine();
	}
	

	/*-------------------------------------------------------------------------*/
	/*Methods for insert elements in the KB                                    */
	/*-------------------------------------------------------------------------*/	
	
	/**
	 * <p>Syntax example: 
	 * (DEFCONCEPT CATEGORY_LABEL)
	 * <p>
	 * Note: The object entities are saved by its type (label) instead of its
	 * name because its instances reference only its type.
	 * 
	 * @see KnowledgeBase
	 */
	public void createEntityDefinition(ObjectEntity entity) {

		LogicObject lo = PLI.sCreateConcept(entity.getType().getName(), null,
				moduleGenerativeName, environment);
		Debug.println(lo.toString());

		// PLI.sEvaluate("(assert ( closed " + entity.getType() +" ) )",
		// moduleName, environment);
	}

	/**
	 * Syntax example: 
	 * <p>
	 * States definition 
	 * <p>
	 * (DEFCONCEPT SRDISTANCE_STATE (?Z) :<=>
	 * (MEMBER-OF ?Z (SETOF PHASER1RANGE PULSECANONRANGE))) 
	 * 
	 * ;;Arguments definition 
	 * 
	 * (DEFFUNCTION SRDISTANCE ( (?ARG_0 SENSORREPORT_LABEL) (?ARG_1
	 * TIMESTEP_LABEL) (?RANGE SRDISTANCE_STATE)))
	 * 
	 * @see KnowledgeBase
	 */
	public void createRandomVariableDefinition(ResidentNode resident){
		
		List<StateLink> links = resident.getPossibleValueLinkList(); 
		
		boolean debugSetted = Debug.isDebugMode(); 
		
		String range = ""; 
		
		Debug.println("Analising node " + resident);
		
		switch(resident.getTypeOfStates()){
		
		// this is now a default case
//		case IResidentNode.OBJECT_ENTITY:
//
//			if (!resident.getPossibleValueLinkList().isEmpty()) {
//				String type = resident.getPossibleValueLinkList().get(0)
//						.getState().getType().getName();
//				range = "(" + "?range " + type + ")";
//			}
//
//			break;

		case IResidentNode.CATEGORY_RV_STATES:

			String setofList = "";
			for (StateLink state : links) {
				setofList += state.getState().getName() + " ";
			}

			// definition of the function image
			String residentStateListName = resident.getName()
					+ POSSIBLE_STATE_SUFIX;
			Stella_Object result = null;
			for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
				try {
					result=	PLI.sEvaluate("(defconcept " + residentStateListName
							+ "(?z) :<=> (member-of ?z ( setof " + setofList + ")))",
							moduleGenerativeName, environment);
					break;
				} catch (Exception e) {
					Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
					try {
						Thread.sleep(this.getMaximumQueryAttemptWaitTime());
					} catch (Throwable t) {
						Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
					}
					continue;
				}
			}

			if(result == null){
				Debug.println("Powerloom problem in evaluation of the possible states of the resident node "  + resident);
			}else{
				Debug.println("Result 1 " + result.toString());
			}
			
			range = "(" + "?range " + residentStateListName + ")";

			break;

		// case ResidentNode.BOOLEAN_RV_STATES:
		//			
		// range = "(" + "?range " + "Boolean" + ")";
		//			
		// break;
			default: //case IResidentNode.OBJECT_ENTITY or anything else:
				if (!resident.getPossibleValueLinkList().isEmpty()) {
					String type = resident.getPossibleValueLinkList().get(0)
							.getState().getType().getName();
					range = "(" + "?range " + type + ")";
				}
			break;
		}
		
		/* Step 2: define the resident node with its arguments */
		String arguments = ""; 
		List<OrdinaryVariable> listVariables = resident.getOrdinaryVariableList(); 
		
		int i = 0; 
		for(OrdinaryVariable variable: listVariables){
			arguments+= "("; 
			arguments+= "?arg_" + i + " " + variable.getValueType().getName(); 
			arguments+= ")"; 
			i++; 
		}

		if (resident.getTypeOfStates() == IResidentNode.BOOLEAN_RV_STATES
				&& !arguments.isEmpty()) {
			Stella_Object result = null;
			for (int j = 0; j < this.getMaximumQueryAttemptCount(); j++) {
				try {
					result = PLI.sEvaluate("(defrelation "
							+ resident.getName() + " (" + arguments + "))",
							moduleGenerativeName, null);
					break;
				} catch (Exception e) {
					Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
					try {
						Thread.sleep(this.getMaximumQueryAttemptWaitTime());
					} catch (Throwable t) {
						Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
					}
					continue;
				}
			}
			if (result != null) {
				Debug.println(result.toString());
			}else{
				Debug.println("Powerloom problem in evaluation of the types of the resident node: " + resident);
			}
		} else {
			Stella_Object result = null;
			for (int j = 0; j < this.getMaximumQueryAttemptCount(); j++) {
				try {
					result = PLI.sEvaluate("(deffunction "
							+ resident.getName() + " (" + arguments + range + "))",
							moduleGenerativeName, null);
					break;
				} catch (Exception e) {
					Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
					try {
						Thread.sleep(this.getMaximumQueryAttemptWaitTime());
					} catch (Throwable t) {
						Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
					}
					continue;
				}
			}
			if(result != null){
				Debug.println(result.toString());
			}else{
				Debug.println("Powerloom problem in evaluation of the types of the resident node: " + resident);
			}
		}
		// TODO closed or open world ?
		// PLI.sEvaluate("(assert (closed " + resident.getName() + "))",
		// moduleName, null);

		
	}

	public void createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn) {
		
		this.mebn = mebn;
		
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			if (entity != null) {
				createEntityDefinition(entity);
			}
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				if (resident != null) {
					createRandomVariableDefinition(resident);
				}
			}
		}
		
	}
	
	/**
	 * Syntax example: 
	 * (ASSERT (STARSHIP_LABEL ST0))
	 * 
	 * @see KnowledgeBase
	 */
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		Debug.println("Entity finding: " + entityInstance.getName());

		// (STARSHIP_LABEL ST0)
		String assertCommand = "(";
		assertCommand += entityInstance.getInstanceOf().getType().getName()
				+ " ";
		assertCommand += entityInstance.getName();
		assertCommand += ")";

		PlIterator iterator = PLI.sAssertProposition(assertCommand,
				moduleFindingName, null);
		while (iterator.nextP()) {
			Debug.println(iterator.value.toString());
		}
	}

	/**
	 * Syntax example: 
	 * (ASSERT (not (ISOWNSTARSHIP ST4))) for boolean states 
	 * or
	 * (ASSERT (= (STARSHIPZONE ST4) Z2)) for other kind of states
	 * 
	 * @see KnowledgeBase
	 */
	public void insertRandomVariableFinding(
	        RandomVariableFinding randonVariableFinding){
		
		String finding = "";
		
		// stores wildcards found in arguments
		List<ObjectEntityInstance> wildCardArgs = new ArrayList<ObjectEntityInstance>();
		
		if(randonVariableFinding.getNode().getTypeOfStates() == IResidentNode.BOOLEAN_RV_STATES
				&& (randonVariableFinding.getArguments().length > 0)){
		
			finding+= "(";
			if(randonVariableFinding.getState().getName().equals("false")){
			    finding+= "NOT";
				finding+= "("; 
			}
			   finding+= randonVariableFinding.getNode().getName(); 
			      finding+=" "; 
			         for(ObjectEntityInstance argument: randonVariableFinding.getArguments()){
			        	 finding+=argument.getName() + " ";
			        	 if (argument.getName().contains(getWildCardSymbol())) {
			        		 wildCardArgs.add(argument);
			        	 }
			         }
			if(randonVariableFinding.getState().getName().equals("false")){
			    finding+= ") ";
			} 
			
			finding+= ")";	
		
		}else{ 
			finding+= "(=";
			finding+= "("; 
			finding+= randonVariableFinding.getNode().getName(); 
			finding+=" "; 
			
			for(ObjectEntityInstance argument: randonVariableFinding.getArguments()){
			   	 finding+=argument.getName() + " ";
			   	 if (argument.getName().contains(getWildCardSymbol())) {
	        		 wildCardArgs.add(argument);
	        	 }
			}
			finding+= ") "; 
			finding+= randonVariableFinding.getState().getName();  
			finding+= ")";	
		}

		if (wildCardArgs.isEmpty()) {
			// there is no wildcard. This is a propositional finding
			PlIterator iterator = PLI.sAssertProposition(finding,
					moduleFindingName, null);
			
			while (iterator.nextP()) {
				Debug.println(iterator.value.toString());
			}
		} else {
			// needs to handle wildcards (this is not a propositional finding)
			// create a "presume" command instead of assertion.
			// (PRESUME (FORALL (?S1) (=> (SHIP_LABEL ?S1) (not (ISOWNSTARSHIP ?S1)))))
			String command = "(PRESUME (FORALL (";
			// list of variables
			for (ObjectEntityInstance arg : wildCardArgs) {
				command += arg.getName().toUpperCase() + " ";
			}
			// end of list of variables
			command += ") (=> (";	
			// print type of 1st arg
			command += wildCardArgs.get(0).getType().getName().toUpperCase() + " ";
			// print 1st wildcard arg
			command += wildCardArgs.get(0).getName();
			command += ")  ";
			
			// the rest is same of finding
			command += finding;
			
			command += ") ) ) ";	// end of "(PRESUME (FORALL ("
			
			String ret = this.executeCommand(command);
			Debug.println(ret);
		}
	}

	/**
	 * Syntax example: 
	 * ( NOT IsOwnStarship(st) ) for UnBBaes syntax 
	 * (NOT( IsOwnStarship ST4 ) ) for PowerLoom syntax
	 * 
	 * @see KnowledgeBase
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {

		String formula = "";

		NodeFormulaTree formulaTree = (NodeFormulaTree) context
				.getFormulaTree();

		formula += "(";
		if ((formulaTree.getTypeNode() == EnumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {
			formula += makeOperandString(formulaTree, ovInstances);
		} else {
			formula += makeOperatorString(formulaTree, ovInstances);
		}
		formula += ")";

		Debug.println("Original formula: " + context.getLabel());
		Debug.println("PowerLoom formula: " + formula);

		TruthValue answer = PLI.sAsk(formula, moduleFindingName, null);

		if (PLI.isTrue(answer)) {
			Debug.println("Result: true");
			return true;
		} else if (PLI.isFalse(answer)) {
			Debug.println("Result: false");
			return false;
		} else if (PLI.isUnknown(answer)) {
			Debug.println("Result: unknown");
			return false;
		} else {
			return false;
		}
	}
    
	/**
	 * @see KnowledgeBase
	 */
    public List<String> evaluateSingleSearchContextNodeFormula(ContextNode context, List<OVInstance> ovInstances)
                                     throws OVInstanceFaultException{
    	
    	String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		List<OrdinaryVariable> ovFaultList = context.getOVFaultForOVInstanceSet(ovInstances); 
		
		//This implementation treat only the case where have only one search variable
		if(ovFaultList.size()>1){
			throw new OVInstanceFaultException(ovFaultList); 
		}
		
		//The search isn't necessary. 
		if(ovFaultList.size() == 0){
			return null; 
		}
		
		//The list have only one element
		OrdinaryVariable ovFault = ovFaultList.get(0); 
		
		//Build the retrieve statement. 
		formula+=" all ";
		
		//List of variables of retrieve. Only one ordinary variable fault. 
		formula+="(" + "?" + ovFault.getName() + " " + ovFault.getValueType().getName() + ")"; 
		
		//Formula
		formula+= "(";  
		formula+= makeOperatorString(formulaTree, ovInstances); 		
		formula+= ")"; 
		
		Debug.println("PowerLoom Formula: " + formula); 
		
		for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
			try {
				PlIterator iterator = PLI.sRetrieve(formula, moduleFindingName, null);
				List result = new ArrayList<String>(); 
				
				while(iterator.nextP()){
					result.add(PLI.getNameInModule(iterator.value, moduleFinding, environment)); 
				}
				
				return result;
				
			} catch (Exception e) {
				Debug.println(this.getClass(), "Could not retrieve from powerloom. Attempting again... Attempt " + i, e);
				try {
					Thread.sleep(this.getMaximumQueryAttemptWaitTime());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * @see KnowledgeBase
	 */
	public SearchResult evaluateSearchContextNodeFormula(
			ContextNode context, List<OVInstance> ovInstances) {
		
    	String formula = ""; 
		
		NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
		
		OrdinaryVariable ovFaultArray[] = context.getOVFaultForOVInstanceSet(ovInstances).toArray(
		                                		   new OrdinaryVariable[context.getOVFaultForOVInstanceSet(ovInstances).size()]); 
		
		//The search isn't necessary. 
		if(ovFaultArray.length == 0){
			return null; 
		}
		
		//Build the retrieve statement. 
		
		//List of variables of retrieve
		//Sample: (all ((?x Person)(?Y Person) (Likes ?x ?y)))
		//That will be: (retrieve (all ((?x Person)(?Y Person) (Likes ?x ?y))))
        
		formula+=" all ";
		
		formula+="("; 
        for(OrdinaryVariable ov: ovFaultArray){
        	formula+= "(" + " ?" + ov.getName() + " " + ov.getValueType().getName() + ")"; 
        }
        
        formula+=")"; 
		
		//Formula
		formula+= "(";  
		formula+= makeOperatorString(formulaTree, ovInstances); 		
		formula+= ")"; 
		
		Debug.println("Evaluate PowerLoom Formula: " + formula); 
		
		PlIterator iterator = null;
		
		for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
			try {
				iterator = PLI.sRetrieve(formula, moduleFindingName, null);
				break;
			} catch (Exception e) {
				Debug.println(this.getClass(), "Could not retrieve from powerloom. Attempting again... Attempt " + i, e);
				try {
					Thread.sleep(this.getMaximumQueryAttemptWaitTime());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
		}
		
		SearchResult searchResult; 
		try {
			Debug.println("Result Set Size = " + ((iterator==null)?0:iterator.length()));
		}catch (Exception e) {
//			e.printStackTrace();
			Debug.println(this.getClass(), "", e);
		}
		if(iterator != null && iterator.length() != 0){
			
			//Create the SearchResult object. 
			searchResult = new SearchResult(ovFaultArray);
			while(iterator.nextP()){
				String[] resultN = new String[ovFaultArray.length];
				Debug.print("   >"); 
				for(int i = 0; i < ovFaultArray.length; i++){ 
//					resultN[i] = PLI.getNthString(iterator, i, moduleFinding, environment);
					// FIXME This is just a quick fix to avoid weird names like /PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE_1/FINDINGS_MODULE_1/PERSON1
					resultN[i] = PLI.getNthString(iterator, i, moduleFinding, environment).replace("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE_1/FINDINGS_MODULE_1/", "").toLowerCase();
					Debug.print(" " + resultN[i]);
				}
				Debug.println(" <   "); 
				searchResult.addResult(resultN); 
			}
			
		}else{

			//No result for this evaluation
			searchResult = null; 
			
		}
		
		return searchResult;
	}

    
	/**
	 * @see KnowledgeBase
	 */
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(List<ContextNode> contextList, List<OVInstance> ovInstances){
	
		
		HashMap<OrdinaryVariable, List<String>> values = new HashMap<OrdinaryVariable, List<String>>(); 
		
		//List of the ov for what don't have one OVInstance
		List<OrdinaryVariable> ovFaultList = new ArrayList<OrdinaryVariable>(); 
    	String formula = ""; 
		
    	//Fill the ovFaultList
		for(ContextNode contextNode: contextList){
			for(OrdinaryVariable ov: contextNode.getOVFaultForOVInstanceSet(ovInstances)){
				if(!ovFaultList.contains(ov)){
					ovFaultList.add(ov); 
					Debug.println("OVFault = " + ov);
				}
			}
			break; 
		}
		
		//The search isn't necessary. 
		if(ovFaultList.size() == 0){
			return null; 
		}
		
		//EXAMPLES SEARCHS...
        formula+=" all ";
		
        formula+="("; 
        for(OrdinaryVariable ov: ovFaultList){
        	formula+= " ?" + ov.getName();
        	values.put(ov, new ArrayList<String>()); 
        }
        
        Debug.println("Formula = " + formula);
        formula+=")"; 
        
        
        //FORMULA
        formula+="("; 
        
        formula+=" AND "; 
        
        for(OrdinaryVariable ov: ovFaultList){
        	formula+="("; 
        	formula+= ov.getValueType().getName() + " ";
        	formula+= " ?" + ov.getName();
    		formula+=")"; 
        }
        
        for(ContextNode context: contextList){
        	
        	Debug.println("Context = " + context);
        	Debug.println("Formula = " + formula);	
        	formula+="("; 
        	NodeFormulaTree formulaTree = (NodeFormulaTree)context.getFormulaTree(); 
    		formula+= makeOperatorString(formulaTree, ovInstances);
    		formula+=")"; 
    		
        }
        
        formula+=")";

		Debug.println("PowerLoom Formula: " + formula); 
		
		for (int j = 0; j < this.getMaximumQueryAttemptCount(); j++) {
			try {
				PlIterator iterator = PLI.sRetrieve(formula, moduleFindingName, null);
				
				iterator.length();	// what is this?
				
				while(iterator.nextP()){
					for(int i = 0; i < ovFaultList.size(); i++){	
						values.get(ovFaultList.get(0)).add(PLI.getNthString(iterator, i, moduleFinding, environment)); 
					}
				}
				
				return values;
				
			} catch (Exception e) {
				Debug.println(this.getClass(), "Could not retrieve from powerloom. Attempting again... Attempt " + j, e);
				try {
					Thread.sleep(this.getMaximumQueryAttemptWaitTime());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
		}
        
		return values;
	}
    
    
    
    
    /*-------------------------------------------------------------------------*/
    /* Auxiliary Methods (Interface)                                           */
    /*-------------------------------------------------------------------------*/    

	/**
	 * @see KnowledgeBase
	 */
	public boolean existEntity(String name) {
		
		Stella_Object so = PLI.sGetObject(name, moduleFindingName, environment); 
		
		if (so != null){
			return true;
		}else{
			return false; 
		}
	}

	/**
	 * @see KnowledgeBase
	 */
    public StateLink searchFinding(ResidentNode randomVariable, Collection<OVInstance> listArguments) {
		
		String finding = "";
		
		if(randomVariable.getTypeOfStates() == IResidentNode.BOOLEAN_RV_STATES
				&& !listArguments.isEmpty()){
			finding+= randomVariable.getName() + " ";
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			TruthValue value = PLI.sAsk(finding, moduleFindingName, environment);
			
			StateLink exactValue = null; 
			//TODO throw a exception when the node dont have the argument... kb inconsistency. 
			if(PLI.isTrue(value)){
				exactValue = randomVariable.getPossibleValueByName("true"); 
			}else{
				if(PLI.isFalse(value)){
					exactValue = randomVariable.getPossibleValueByName("false"); 
				}else{
					if(PLI.isUnknown(value)){
						exactValue = null; 
					}
				}
			}

			return exactValue; 
			
		}else{
			finding+="all ?x"; 
			finding+="("; 
			finding+= randomVariable.getName();
			
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			
			finding += " ?x"; 
			finding+= ")";
			
			for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
				try {
					PlIterator iterator = PLI.sRetrieve(finding, moduleFindingName, environment); 
					
					//TODO throw a exception when the search return more than one result...
					if(iterator.nextP()){
						String state = PLI.getNthString(iterator, 0, moduleFinding, environment);
						return randomVariable.getPossibleValueByName(state); 
					}	
					return null; 
				} catch (Exception e) {
					Debug.println(this.getClass(), "Could not retrieve from powerloom. Attempting again... Attempt " + i, e);
					try {
						Thread.sleep(this.getMaximumQueryAttemptWaitTime());
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see KnowledgeBase
	 */
	public List<String> getEntityByType(String type) {
		
		List<String> list = new ArrayList<String>(); 
		
		PlIterator iterator = PLI.sGetConceptInstances(type, moduleFindingName, environment); 
		while (iterator.nextP()){
			list.add(PLI.getNthString(iterator, 0, moduleFinding, environment)); 
		}
			
		return list; 
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Auxiliary Methods                                                       */
	/*-------------------------------------------------------------------------*/
	
	/**
	 * It makes an operator string from the NodeFormulaTree. (an operator and
	 * its operands).
	 * 
	 * @param operatorNode
	 * @param ovInstances
	 * @return
	 */
	private String makeOperatorString(NodeFormulaTree operatorNode,
			List<OVInstance> ovInstances) {

		String operator = "";

		switch (operatorNode.getTypeNode()) {

		case SIMPLE_OPERATOR:

			BuiltInRV builtIn = (BuiltInRV) operatorNode.getNodeVariable();
			
			if(builtIn instanceof BuiltInRVAnd){
				operator+= makeBynaryStatement(operatorNode, "AND", ovInstances);
			}else
				if(builtIn instanceof BuiltInRVOr){
					operator+= makeBynaryStatement(operatorNode, "OR", ovInstances);	
				}else
					if(builtIn instanceof BuiltInRVEqualTo){
						operator+= makeEqualStatement(operatorNode, " = ", ovInstances); 	
					}else
						if(builtIn instanceof BuiltInRVIff){
							operator+= makeBynaryStatement(operatorNode, "<=>", ovInstances);
						}else
							if(builtIn instanceof BuiltInRVImplies){
								operator+= makeBynaryStatement(operatorNode, "=>", ovInstances);
							}else
								if(builtIn instanceof BuiltInRVNot){
									operator+= makeUnaryStatement(operatorNode, "NOT", ovInstances) ; 
								}	    
			                    break;
			
		case QUANTIFIER_OPERATOR:

			builtIn = (BuiltInRV) operatorNode.getNodeVariable();
			
			if (builtIn instanceof BuiltInRVExists) {
				operator += makeQuantifier(operatorNode, "EXISTS", ovInstances);
			} else if (builtIn instanceof BuiltInRVForAll) {
				operator += makeQuantifier(operatorNode, "FORALL", ovInstances);
			}

			break;

		case OPERAND:
			if(operatorNode.getSubTypeNode() == EnumSubType.NODE){
				   operator += makeNodeOperandCase(operatorNode, ovInstances);
			}
			break; 
			
		default:
			Debug.println("ERROR! type of operator don't found");

		}

		return operator;

	}

	/**
	 * The case of a operator that consists in only a node. 
	 * ex: IsOwnStarship(st)
	 * 
	 * This case is valid only if the node is a boolean node. 
	 */
	private String makeNodeOperandCase(NodeFormulaTree operatorNode,
			List<OVInstance> ovInstances) {
		
		String operator = ""; 
		
		ResidentNodePointer node = (ResidentNodePointer)operatorNode.getNodeVariable(); 
		   operator+= node.getResidentNode().getName(); 

		   for(OrdinaryVariable ordVariable: node.getOrdinaryVariableList()){
			   operator += " "; 
			   OVInstance ovInstance = getOVInstanceForOV(ordVariable, ovInstances); 
			   if(ovInstance != null){
			       operator += ovInstance.getEntity().getInstanceName(); 
			   }
			   else{
				   operator += "?" + ordVariable.getName(); 
			   } 
		   }
		   
		return operator;
	}
	
	/**
	 * It makes an operand string from the NodeFormulaTree.
	 * 
	 * @param operator
	 * @param ovInstances
	 * @return
	 */
	private String makeOperandString(NodeFormulaTree operator, List<OVInstance> ovInstances){
		
		String operand = ""; 
		
		switch(operator.getTypeNode()){
		
		case SIMPLE_OPERATOR:
			operand += makeOperatorString(operator, ovInstances);
			break;

		case QUANTIFIER_OPERATOR:
			operand += makeOperatorString(operator, ovInstances); 
		break; 	
		
		case OPERAND: 
			
		   switch(operator.getSubTypeNode()){
		   
		   case OVARIABLE: 
			   OrdinaryVariable ov = (OrdinaryVariable)operator.getNodeVariable(); 
			   OVInstance ovInstance = getOVInstanceForOV(ov, ovInstances); 
			   if(ovInstance != null){
			       operand+= ovInstance.getEntity().getInstanceName(); 
			   }
			   else{ 
				   operand+= "?" + ov.getName(); 
			   }
			   break; 
		   
		   case NODE:
			   
			   ResidentNodePointer node = (ResidentNodePointer)operator.getNodeVariable(); 
			   operand+= node.getResidentNode().getName(); 
			   
			   operand+= " "; 
			   
			   for(OrdinaryVariable ordVariable: node.getOrdinaryVariableList()){
				   ovInstance = getOVInstanceForOV(ordVariable, ovInstances); 
				   if(ovInstance != null){
				       operand+= ovInstance.getEntity().getInstanceName(); 
				   }
				   else{
					   operand+= "?" + ordVariable.getName(); 
				   }
				   operand+=" "; 
			   }
			   
			   break;   
			   
		   case ENTITY:
			   Entity entity = (Entity) operator.getNodeVariable(); 
			   operand+= entity.getName(); 
		   }
		   
		break; 
		
		}

		return operand;
	}
	
	/*
	 * Used for: 
	 * AND
	 * OR
	 * => (implies)
	 * <=> (iff)
	 */
	private String makeBynaryStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		
		retorno+= conectiveName; 
		
    	ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperando = listChildren.get(0);
    	
    	retorno+= "( "; 
		retorno+= makeOperandString(leftOperando, ovInstances);   		
    	retorno+=" ) "; 
    	
    	retorno+= "( "; 
    	NodeFormulaTree rightOperando = listChildren.get(1); 
		retorno+= makeOperandString(rightOperando, ovInstances);   
    	retorno+=" ) "; 
    	
    	return retorno; 
    	
	}
	
	/* 
	 * Used for:
	 * = (equal)
	 */
	private String makeEqualStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 
    	
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	NodeFormulaTree leftOperand = listChildren.get(0);
    	
    	// Brackets aren't necessary when the operand is a ordinary variable. 
    	// ex: StarshipZone(st) = ov -> (= (StarshipZone ST) ?ov)
    	// ex: ov = StarshipZone(st) -> (= ?ov (StarshipZone ST))
    	if((leftOperand.getTypeNode() == EnumType.OPERAND)&&(leftOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
    		retorno+= " ";
    		retorno+= this.makeOperandString(leftOperand, ovInstances);
    		retorno+= " ";
    	}else{
    		retorno+= " ( ";
    		retorno+= this.makeOperandString(leftOperand, ovInstances);
    		retorno+= " ) ";
    	}
    	
    	NodeFormulaTree rightOperand = listChildren.get(1); 
    	
    	if((rightOperand.getTypeNode() == EnumType.OPERAND)&&(rightOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
    		retorno+= " ";
    		retorno+= this.makeOperandString(rightOperand, ovInstances);
    		retorno+= " ";
    	}else{
    		retorno+= " ( ";
    		retorno+= this.makeOperandString(rightOperand, ovInstances);
    		retorno+= " ) ";
    	}
		
		return retorno; 
	}
	
	/*
	 * Used for:
	 * NOT
	 */
	private String makeUnaryStatement(NodeFormulaTree node, String conectiveName, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= conectiveName; 	
		
		ArrayList<NodeFormulaTree> listChildren = (ArrayList<NodeFormulaTree>)node.getChildren(); 
    	
    	retorno+= "( "; 
		retorno+= this.makeOperandString(listChildren.get(0), ovInstances);   		
    	retorno+=" ) "; 
		
		return retorno; 
	
	}
	
	/* 
	 * Used for:
	 * ALL
	 * EXISTS
	 */
	private String makeQuantifier(NodeFormulaTree node, String name, List<OVInstance> ovInstances){
    	
		String retorno = ""; 
		retorno+= name; 
		
		List<NodeFormulaTree> listChildren = node.getChildren(); 
    	
		/*--------------------- Exemplar variables --------------------------*/
    	NodeFormulaTree listExemplares = listChildren.get(0);  //Var...
    	
    	retorno+="("; 
    	for(NodeFormulaTree exemplar : listExemplares.getChildren()){
    		OrdinaryVariable ov = (OrdinaryVariable)exemplar.getNodeVariable(); 
    		retorno+= "?" + ov.getName() + ",";
    	}
    	
    	//delete the virgle
    	if(listExemplares.getChildren().size() > 0){
    	   retorno = retorno.substring(0, retorno.length() - 2); 
    	}
    	
    	retorno+=")"; 
    	
    	/*-------------------------- Formula ----------------------------------*/
    	NodeFormulaTree formula = listChildren.get(1); 
    	retorno+="("; 
		retorno+= makeOperatorString(formula, ovInstances);   		
		retorno+=")"; 
		
		return retorno; 
	}

	private OVInstance getOVInstanceForOV(OrdinaryVariable ov,
			List<OVInstance> list) {

		for (OVInstance ovi : list) {
			if (ovi.getOv().equals(ov)) {
				return ovi;
			}
		}

		return null;
	}

	/*
	 * Save the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void saveDefinitionsFile(String name){
		PLI.sSaveModule(moduleGenerativeName, name, "REPLACE", environment); 
	}
	
	/*
	 * Load the definitions file (content of current KB)
	 * 
	 * @param name Name of the file
	 */
	private void loadDefinitionsFile(String name){
		PLI.load(name, environment); 
	}
	
	
	
	
	
	/*-------------------------------------------------------------------------*/
	/* Other public methods                                                    */
	/*-------------------------------------------------------------------------*/
	
	/**
	 * Debug method used for execute a command directely in the Powerloom. 
	 * 
	 * @param command A valid statement of powerloom (in PowerLoom syntaxe SURROUNDED by parentesis)
	 * @return the result of execution (console text of PowerLoom)
	 */
	public String executeCommand(String command){
		for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
			try {
				return PLI.sEvaluate(command, moduleFindingName, null).toString();
			} catch (Exception e) {
				Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
				try {
					Thread.sleep(this.getMaximumQueryAttemptWaitTime());
				} catch (Throwable t) {
					Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
				}
				continue;
			}
		}
		return "";
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#fillFindings(unbbayes.prs.mebn.ResidentNode)
	 */
	public void fillFindings(ResidentNode resident) {
		final boolean NONBOOLEAN = false;
		final boolean ISBOOLEAN = true;
		boolean booleanValue = false;
		
		/*
		 * Retrieve from kb using the format below:
		 *   boolean true:
		 *   		retrieve all ( RESIDENT ?x0 )
		 *   		retrieve all ( RESIDENT ?x0 ?x1 )
		 *   boolean false:
		 *   		retrieve all ( not ( RESIDENT ?x0 ) )
		 *   		retrieve all ( not ( RESIDENT ?x0 ?x1 ) )
		 *   else:
		 *   		retrieve all ( = ( RESIDENT ?x0 ) ?x1 ) 
		 *   		retrieve all ( = ( RESIDENT ?x0 ?x1  ) ?x2 ) 
		 */
		
		String queryString = "";
		int argcount = 0;
		if(resident.getTypeOfStates() != IResidentNode.BOOLEAN_RV_STATES){
			
			// filling ordinary
			
			queryString = "( retrieve all ( = ( " + resident.getName().toUpperCase(); 
			List<ObjectEntityInstance> argumentList = new ArrayList<ObjectEntityInstance>();
			for(Argument argument: resident.getArgumentList()){
				queryString += " ?x" + argcount;
				argcount++;
			}
			queryString += " ) ?x" + argcount + ") ) ";
			argcount++;
			
			Debug.println(this.getClass(), "Quering to PLI: " + queryString);
			
			Stella_Object sobj = null;
			for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
				try {
					sobj = PLI.sEvaluate(queryString, moduleFindingName, environment); 
					break;
				} catch (Exception e) {
					Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
					try {
						Thread.sleep(this.getMaximumQueryAttemptWaitTime());
					} catch (Throwable t) {
						Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
					}
					continue;
				}
			}
			
			String result = PLI.getNthString(sobj, 0, this.moduleFinding, this.environment);
			Debug.println(this.getClass(), "result..." + result + "...count = " + argcount);
			
			this.parsePLIStringAndFillBooleanFinding(resident, result, NONBOOLEAN, NONBOOLEAN);
		
		} else {
			
			// filling false findings
			booleanValue = false;
			
			queryString = "( retrieve all ( not ( " + resident.getName(); 
			List<ObjectEntityInstance> argumentList = new ArrayList<ObjectEntityInstance>();
			for(Argument argument: resident.getArgumentList()){
				queryString += " ?x" + argcount++; 
			}
			queryString += " ) ) ) ";
			
			Stella_Object sobj = null;
			String result = null;
			
			try{
				for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
					try {
						sobj = PLI.sEvaluate(queryString, moduleFindingName, environment); 
						break;
					} catch (Exception e) {
						Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
						try {
							Thread.sleep(this.getMaximumQueryAttemptWaitTime());
						} catch (Throwable t) {
							Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
						}
						continue;
					}
				}
				if (!sobj.deletedP()) {
					result = PLI.getNthString(sobj, 0, this.moduleFinding, this.environment);
				}
			} catch ( Exception e) {
//				e.printStackTrace();
				Debug.println(this.getClass(), "", e);
				return;
			}
			
			Debug.println(this.getClass(), "result..." + result + "...count = " + argcount);
			
			this.parsePLIStringAndFillBooleanFinding(resident, result, ISBOOLEAN  , booleanValue);
				
			
			// filling true findings
			booleanValue = true;			
			
			queryString = "( retrieve all ( " + resident.getName(); 
			argumentList = new ArrayList<ObjectEntityInstance>();
			for(Argument argument: resident.getArgumentList()){
				queryString += " ?x" + argcount++; 
			}
			queryString += " ) ) ";

			try{
				for (int i = 0; i < this.getMaximumQueryAttemptCount(); i++) {
					try {
						sobj = PLI.sEvaluate(queryString, moduleFindingName, environment); 
						break;
					} catch (Exception e) {
						Debug.println(this.getClass(), "Failed to query powerloom. Attempt " + i, e);
						try {
							Thread.sleep(this.getMaximumQueryAttemptWaitTime());
						} catch (Throwable t) {
							Debug.println(this.getClass(), "Failed to sleep thread until next powerloom query attemtp.", t);
						}
						continue;
					}
				}
				if (!sobj.deletedP()) {
					result = PLI.getNthString(sobj, 0, this.moduleFinding, this.environment);
				}
			} catch ( Exception e) {
//				e.printStackTrace();
				Debug.println(this.getClass(), "", e);
				return;
			}
			Debug.println(this.getClass(), "result..." + result + "...count = " + argcount);
			
			this.parsePLIStringAndFillBooleanFinding(resident, result, ISBOOLEAN, booleanValue);
			
		}
		
	}

	/**
	 * Parses the string describing the findings and fills the resident node
	 * with them.
	 * The string evaluated by this method should be in the format sampled below:
	 * 		(ST4 ST3 ST2 ST1) 
	 * for boolean findings with 1 argument
	 * 		((ST4 ST3) 
	 * 		 (ST2 ST1)) 
	 * for boolean findings with two arguments
	 * 		((ST4 CARDASSIAN) 
	 * 		 (ST2 UNKNOWN)) 
	 * for non-boolean findings with one argument
	 * 		((ST4 T3 CARDASSIAN) 
	 * 		 (ST2 T1 UNKNOWN))
	 * for non-boolean findings with two arguments
	 * @param resident the node to insert the finding
	 * @param strPLI the special format string to be evaluated. Can be obtained by using PLI.sEvaluate and PLI.getNthString.
	 * @param boolValue if the evaluated boolean finding should be "true" or "false". It is ignored when the finding should not
	 * be boolean
	 * @param isBool if the finding should be a boolean value (false if non-boolean finding)
	 * @return resident.getRandomVariableFindingList()
	 */
	public List<RandomVariableFinding> parsePLIStringAndFillBooleanFinding(ResidentNode resident , String strPLI, boolean isBool , boolean boolValue )  {
		
		StringTokenizer tokenizer =  new StringTokenizer(strPLI , PLI_TOKEN_SEPARATOR);
		
		List<ObjectEntityInstance> argumentInstances = null;
		List<Argument> arguments = resident.getArgumentList();
		
		Debug.println(this.getClass(), "input: " + strPLI);
		
		if (strPLI.equalsIgnoreCase(PLI_TOKEN_SEPARATOR)) {
			return null;
		}
		
		while (tokenizer.hasMoreTokens()) {
			argumentInstances = new ArrayList<ObjectEntityInstance>();
			String token = null;
			Debug.println("Arguments:");
			for (int i = 0 ;  i < arguments.size() ; i++) {
				// Extract arguments
				token = tokenizer.nextToken();
				try {
					ObjectEntityInstance instance = resident.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer().getEntityInstanceByName(token);
					Debug.print(" | " + token);
					if (instance == null) {
						// the KB is using an instance which is not represented in memory yet. Create new instance in memory!
						instance = createEntityInstanceByArgIndex(resident, i, token);
					}
					argumentInstances.add(instance);
				} catch (Exception e) {
//					e.printStackTrace();
					Debug.println(this.getClass(), e.getMessage(), e);
					continue;
				}
			}
			Debug.println("");
			// now, argumentInstances should contain all arguments of this finding
			RandomVariableFinding finding = null;
			String possibleValue = null;
			// let's extract the possible value, if necessary
			if (!isBool) {
				possibleValue = tokenizer.nextToken();
				Debug.println(this.getClass(), "Possible Value = " + possibleValue);
			}
			try {
				if (isBool) {
					finding = new RandomVariableFinding(resident , 
							  argumentInstances.toArray(new ObjectEntityInstance[argumentInstances.size()]) , 
							  resident.getPossibleValueByName(String.valueOf(boolValue)).getState() ,
							  resident.getMFrag().getMultiEntityBayesianNetwork());
				} else {
					StateLink possibleValueStateLink = resident.getPossibleValueByName(possibleValue);
					Entity possibleValueEntity = null;
					if (possibleValueStateLink != null) {
						possibleValueEntity = possibleValueStateLink.getState();
					} else {
						// KB contains a possible value which is not present in memory yet. Create in-memory representation
						possibleValueEntity = createPossibleValueEntityInstanceOfNode(resident, possibleValue);
					}
					finding = new RandomVariableFinding(resident , 
							 argumentInstances.toArray(new ObjectEntityInstance[argumentInstances.size()]) , 
							 possibleValueEntity ,
							 resident.getMFrag().getMultiEntityBayesianNetwork());
				}
			} catch (Exception e) {
//				e.printStackTrace();
				Debug.println(this.getClass(), "", e);
				System.err.println("Error: " + strPLI + " at " + token);
				continue;
			}
			
			Debug.println("Finding = " + finding.toString());
			resident.addRandomVariableFinding(finding);
		}
		return resident.getRandomVariableFindingList();
	}

	/**
	 * Create an entity instance of name "name" compatible with the possible value of "resident".
	 * Caution: it does not check whether the instance already exists.
	 * @param resident : the type must be an object entity
	 * @param name
	 * @return a new instance of object entity ({@link ObjectEntityInstance}). 
	 */
	protected Entity createPossibleValueEntityInstanceOfNode(
			ResidentNode resident, String name) {
		if (resident.getTypeOfStates() != resident.OBJECT_ENTITY) {
			throw new IllegalArgumentException("Expected type of node " + resident + " is Object Entity.");
		}
		if (name == null) {
			name = "NULL";
		}
		try {
			MultiEntityBayesianNetwork mebn = resident.getMFrag().getMultiEntityBayesianNetwork();
			// extract entity
			ObjectEntity entity = (ObjectEntity)resident.getPossibleValueLinkList().get(0).getState();
			if (entity.isValidInstanceName(name)) {
				ObjectEntityInstance instance = entity.addInstance(name);
				mebn.getObjectEntityContainer().addEntityInstance(instance);
				if (!mebn.getNamesUsed().contains(name)) {
					mebn.getNamesUsed().add(name); 
				}
				return instance;
			} else {
				Debug.println(getClass(), name + " is not valid as a possible state of " + resident);
			}
		} catch(Exception e){
			Debug.println(getClass(), e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Create an entity instance of name "name" compatible with the "index"'th argument of "resident".
	 * Caution: it does not check whether the instance already exists.
	 * @param resident
	 * @param index
	 * @param name
	 * @return a new instance of object entity.
	 */
	protected ObjectEntityInstance createEntityInstanceByArgIndex(
			ResidentNode resident, int index, String name) {
		if (name == null) {
			name = "NULL";
		}
		MultiEntityBayesianNetwork mebn = resident.getMFrag().getMultiEntityBayesianNetwork();
		// extract entity
		ObjectEntity entity = mebn.getObjectEntityContainer().getObjectEntityByType(
				resident.getOrdinaryVariableByIndex(index).getValueType());
		try {
			ObjectEntityInstance instance = entity.addInstance(name);
			mebn.getObjectEntityContainer().addEntityInstance(instance);
			if (!mebn.getNamesUsed().contains(name)) {
				mebn.getNamesUsed().add(name); 
			}
			return instance;
		} catch (TypeException e1) {
			e1.printStackTrace();
		} catch(EntityInstanceAlreadyExistsException e){
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#supportsLocalFile(boolean)
	 */
	public boolean supportsLocalFile(boolean isLoad) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileDescription(boolean)
	 */
	public String getSupportedLocalFileDescription(boolean isLoad) {
		return "Power Loom (.plm)";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileExtension(boolean)
	 */
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		String[] ret = {".plm"};
		return ret;
	}

	/**
	 * This is the amount of times a query to powerloom will be attempted.
	 * If this is set to a value greater than 1, then queries will be repeated
	 * in case of an exception is thrown.
	 * Do not set this value to 0 or lower, because such values mean no query
	 * will be performed at all.
	 * @return the maximumQueryAttemptCount
	 */
	public int getMaximumQueryAttemptCount() {
		return maximumQueryAttemptCount;
	}

	/**
	 * This is the amount of times a query to powerloom will be attempted.
	 * If this is set to a value greater than 1, then queries will be repeated
	 * in case of an exception is thrown.
	 * Do not set this value to 0 or lower, because such values mean no query
	 * will be performed at all.
	 * @param maximumQueryAttemptCount the maximumQueryAttemptCount to set
	 */
	public void setMaximumQueryAttemptCount(int maximumQueryAttemptCount) {
		this.maximumQueryAttemptCount = maximumQueryAttemptCount;
	}

	/**
	 * This is the amount of time in milliseconds to wait when a query
	 * to powerloom fails and it is going to be tried again.
	 * @return the maximumQueryAttemptWaitTime
	 * @see #getMaximumQueryAttemptCount()
	 * @see Thread#sleep(long)
	 */
	public long getMaximumQueryAttemptWaitTime() {
		return maximumQueryAttemptWaitTime;
	}

	/**
	 * This is the amount of time in milliseconds to wait when a query
	 * to powerloom fails and it is going to be tried again.
	 * @param maximumQueryAttemptWaitTime the maximumQueryAttemptWaitTime to set
	 * @see #getMaximumQueryAttemptCount()
	 * @see Thread#sleep(long)
	 */
	public void setMaximumQueryAttemptWaitTime(long maximumQueryAttemptWaitTime) {
		this.maximumQueryAttemptWaitTime = maximumQueryAttemptWaitTime;
	}

	/**
	 * @return the mebn
	 * @see #createGenerativeKnowledgeBase(MultiEntityBayesianNetwork)
	 * @see #loadModule(File, boolean)
	 * @see #clearKnowledgeBase()
	 */
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}

	/**
	 * @param mebn the mebn to set
	 * @see #createGenerativeKnowledgeBase(MultiEntityBayesianNetwork)
	 * @see #loadModule(File, boolean)
	 * @see #clearKnowledgeBase()
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}

	/**
	 * @return the wildCardSymbol : symbol to trigger wildcard arguments. Use "?" as prefix by default
	 */
	public String getWildCardSymbol() {
		return wildCardSymbol;
	}

	/**
	 * @param wildCardSymbol : symbol to trigger wildcard arguments. Use "?" as prefix by default
	 */
	public void setWildCardSymbol(String wildCardSymbol) {
		this.wildCardSymbol = wildCardSymbol;
	}
	
	
	
}
