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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;


/**
 * Tests for KBFacade module. 
 * 
 * @author Laecio
 *
 */
public class KBTest  extends TestCase {
	
	KBTest(String args) {
		super(args);
	}

	private static final String KNOWLEDGE_BASE = "KnowledgeBaseWithStarshipZoneST4.ubf"; 
	private static final String FILE_STARTREK = "examples/mebn/StarTrek47.ubf"; 
	
	public static final String KB_GENERATIVE_FILE = "testeGenerativeStarship.plm";
	public static final String KB_FINDING_FILE = "KnowledgeBaseWithStarshipZoneST4.plm";  	
	
	private KnowledgeBase kb; 
	private MultiEntityBayesianNetwork mebn; 
	private ContextNodeAvaliator avaliator; 
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		init(); 
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void init(){
		
		kb = PowerLoomKB.getInstanceKB();
		
		UbfIO io = UbfIO.getInstance(); 
		try {
			mebn = io.loadMebn(new File(KBTest.FILE_STARTREK));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		kb.loadModule(new File(KBTest.KB_GENERATIVE_FILE)); 
		kb.loadModule(new File(KBTest.KB_FINDING_FILE)); 
			
		avaliator = new ContextNodeAvaliator(PowerLoomKB.getInstanceKB()); 
		
	}
	
	private void evaluateStarshipZone_Case(){
		ContextNode contextNode = mebn.getContextNode("CX11"); 
		
		OrdinaryVariable ovS = mebn.getMFragByName("Starship_MFrag").getOrdinaryVariableByName("st"); 
		OVInstance ovInstance = OVInstance.getInstance(ovS, "ST4", ovS.getValueType()); 
		
		List<OVInstance> args = new ArrayList<OVInstance>(); 
		
		args.add(ovInstance);
		
		try {
			List<String> results = avaliator.evalutateSearchContextNode(contextNode, args);
			for(String result: results){
				System.out.println(result);
			}
		} catch (InvalidContextNodeFormulaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OVInstanceFaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void evaluateEqual_S_ST_Case(){
		ContextNode contextNode = mebn.getContextNode("CX4"); 
		
		OrdinaryVariable ovS = mebn.getMFragByName("DangerToSelf_MFrag").getOrdinaryVariableByName("s"); 
		OVInstance ovInstance = OVInstance.getInstance(ovS, "ST0", ovS.getValueType()); 
		
		List<OVInstance> args = new ArrayList<OVInstance>(); 
		
		args.add(ovInstance);
		
		try {
			List<String> results = avaliator.evalutateSearchContextNode(contextNode, args);
			for(String result: results){
				System.out.println(result);
			}
		} catch (InvalidContextNodeFormulaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OVInstanceFaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void evaluateEqual_S_ST_WhithoutRetrieve_Case(){
		List<OVInstance> args = new ArrayList<OVInstance>(); 
		
		ContextNode contextNode = mebn.getContextNode("CX4"); 
		
		OrdinaryVariable ovS = mebn.getMFragByName("DangerToSelf_MFrag").getOrdinaryVariableByName("s"); 
		OVInstance ovInstance = OVInstance.getInstance(ovS, "ST0", ovS.getValueType()); 
		args.add(ovInstance);
		
		ovS = mebn.getMFragByName("DangerToSelf_MFrag").getOrdinaryVariableByName("st"); 
		ovInstance = OVInstance.getInstance(ovS, "ST1", ovS.getValueType()); 
		args.add(ovInstance);
		
		try {
			boolean result = avaliator.evaluateContextNode(contextNode, args);
			System.out.println("evaluateEqual_S_ST_WhithoutRetrieve_Case=" + result);
		} catch (OVInstanceFaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void evaluateEqual_S_ST_Case_Directelly(){
//		String result = ((PowerLoomKB)kb).executeCommand("(retrieve all (and (STARSHIP_LABEL ?s) (not (= ?s st1))))");
//		System.out.println("Result" + result);
		
		PlIterator it = PLI.sRetrieve("all ( ?s STARSHIP_LABEL) (not (= ?s st1))", "/PL-KERNEL/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE", null);
	    while(it.nextP()){
	    	System.out.println(""+it.value.toString());
	    }
	}
	
	public static void main(String args[]){
		KBTest test = new KBTest(""); 
		test.init(); 
		
//		test.evaluateEqual_S_ST_Case();
		test.evaluateStarshipZone_Case();
	}
	
}
