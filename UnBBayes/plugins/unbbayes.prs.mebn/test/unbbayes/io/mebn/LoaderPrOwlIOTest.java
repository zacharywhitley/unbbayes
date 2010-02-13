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
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * FIXME this is not testing properly. Fix it.
 *
 */
public class LoaderPrOwlIOTest extends TestCase {

	public static final String STARSHIP16FILEEXAMPLE = "src/test/resources/testCases/mebn/StarTrek.owl"; 
	public static final String STARSHIP16FILEEXAMPLESAVED = "src/test/resources/testCases/mebn/StarTrek_result.owl"; 
	
	/**
	 * @param arg0
	 */
	public LoaderPrOwlIOTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void traceMebnStructure(MultiEntityBayesianNetwork mebn){
		

		ArrayList<Argument> listArgument = new ArrayList<Argument>(); 
		
		System.out.println("\n\n--------------------");
		System.out.println("Test MEBN Structure "); 
		System.out.println("-------------------- \n");		
		
		/* trace MTheory */
		
		System.out.println("-----> MTheory: " + mebn.getName() + "\n");
		
		System.out.println("-> mFragList: "); 
		List<MFrag> listDomainMFrag = mebn.getDomainMFragList(); 
		for(MFrag mFrag: listDomainMFrag){
			System.out.println(mFrag.getName()); 
		}
		
		/* trace MFrag */
		for(MFrag domainMFrag: listDomainMFrag){
			
			System.out.println("\n\n-----> MFrag: " + domainMFrag.getName() + "\n");
			
			System.out.println("\n-> nodeList: "); 
			ArrayList<Node> nodeList = domainMFrag.getNodeList(); 
			int sizeNodeList = nodeList.size(); 
			for(int i = 0; i < sizeNodeList ; i++){
				System.out.println(nodeList.get(i).getName()); 
			}				
			
			System.out.println("\n-> contextNodeList: "); 
			List<ContextNode> contextNodeList = domainMFrag.getContextNodeList(); 
			for(ContextNode contextNode: contextNodeList){
				System.out.println(contextNode.getName()); 
			}						
			
			System.out.println("\n-> domainResidentNodeList: "); 
			List<ResidentNode> domainResidentNodeList = domainMFrag.getResidentNodeList(); 
			for(ResidentNode residentNode: domainResidentNodeList){
				System.out.println(residentNode.getName()); 
			}				
			
			System.out.println("\n-> GenerativeInputNodeList: "); 
			List<InputNode> generativeInputNodeList = domainMFrag.getInputNodeList(); 
			for(InputNode generativeInputNode: generativeInputNodeList){
				System.out.println(generativeInputNode.getName()); 
			}	
			
			System.out.println("\n-> OrdinaryVariableList: "); 
			List<OrdinaryVariable> ordinaryVariableList = domainMFrag.getOrdinaryVariableList(); 
			for(OrdinaryVariable ordinaryVariable: ordinaryVariableList){
				System.out.println(ordinaryVariable.getName()); 
			}			
		}
	}

	/**
	 * Test method for {@link unbbayes.io.mebn.LoaderPrOwlIO#loadMebn(java.io.File)}.
	 */
	public void testLoadMebn() {
		MultiEntityBayesianNetwork mebn = null; 
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			File file = new File(STARSHIP16FILEEXAMPLE); 
			mebn = prOwlIO.loadMebn(file); 
			System.out.println("Load concluido"); 
		}
		catch (IOMebnException e){
			e.printStackTrace();
			fail("ERROR IO PROWL!!!!!!!!!"); 
		}
		catch (IOException e){
			e.printStackTrace();
			fail("ERROR IO!!!!!!!!!"); 			
		}
		
		System.out.println("Load finished!"); 
		
		try{
			traceMebnStructure(mebn);
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		try{
			File file = new File(STARSHIP16FILEEXAMPLESAVED); 
		    prOwlIO.saveMebn(file, mebn);
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
	}

	
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(LoaderPrOwlIOTest.class);
	}
}
