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
package unbbayes.controller;

import java.io.File;

import unbbayes.gui.NetworkWindow;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.UbfIoTest;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.util.Debug;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shou
 *
 */
public class MEBNControllerTest extends TestCase {

	private MEBNController controller = null;
	private MultiEntityBayesianNetwork mebn = null;
	
	private String plmFileName = "examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";
	private String owlFileName = "examples/mebn/StarTrek.ubf";
	
	/**
	 * @param arg0
	 */
	public MEBNControllerTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(false);
		UbfIO io = UbfIO.getInstance();
		mebn = io.loadMebn(new File(owlFileName));
		this.controller = new MEBNController(mebn,new NetworkWindow(mebn));
		//PowerLoomKB.getInstanceKB().loadModule(new File(plmFileName));
		Debug.setDebug(true);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Debug.setDebug(false);
	}

//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getResidentNodeActive()}.
//	 */
//	public void testGetResidentNodeActive() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getInputNodeActive()}.
//	 */
//	public void testGetInputNodeActive() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getContextNodeActive()}.
//	 */
//	public void testGetContextNodeActive() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getNodeActive()}.
//	 */
//	public void testGetNodeActive() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#MEBNController(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.gui.NetworkWindow)}.
//	 */
//	public void testMEBNController() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#enableMTheoryEdition()}.
//	 */
//	public void testEnableMTheoryEdition() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setNameMTheory(java.lang.String)}.
//	 */
//	public void testSetNameMTheory() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertEdge(unbbayes.prs.Edge)}.
//	 */
//	public void testInsertEdge() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertDomainMFrag()}.
//	 */
//	public void testInsertDomainMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#removeDomainMFrag(unbbayes.prs.mebn.DomainMFrag)}.
//	 */
//	public void testRemoveDomainMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setCurrentMFrag(unbbayes.prs.mebn.MFrag)}.
//	 */
//	public void testSetCurrentMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameMFrag(unbbayes.prs.mebn.MFrag, java.lang.String)}.
//	 */
//	public void testRenameMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#showGraphMFrag(unbbayes.prs.mebn.MFrag)}.
//	 */
//	public void testShowGraphMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getCurrentMFrag()}.
//	 */
//	public void testGetCurrentMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertOrdinaryVariable(double, double)}.
//	 */
//	public void testInsertOrdinaryVariable() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertDomainResidentNode(double, double)}.
//	 */
//	public void testInsertDomainResidentNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameDomainResidentNode(unbbayes.prs.mebn.DomainResidentNode, java.lang.String)}.
//	 */
//	public void testRenameDomainResidentNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#addPossibleValue(unbbayes.prs.mebn.DomainResidentNode, java.lang.String)}.
//	 */
//	public void testAddPossibleValue() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#addBooleanAsPossibleValue(unbbayes.prs.mebn.DomainResidentNode)}.
//	 */
//	public void testAddBooleanAsPossibleValue() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#removePossibleValue(unbbayes.prs.mebn.DomainResidentNode, java.lang.String)}.
//	 */
//	public void testRemovePossibleValue() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#existsPossibleValue(unbbayes.prs.mebn.DomainResidentNode, java.lang.String)}.
//	 */
//	public void testExistsPossibleValue() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setEnableTableEditionView()}.
//	 */
//	public void testSetEnableTableEditionView() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setUnableTableEditionView()}.
//	 */
//	public void testSetUnableTableEditionView() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#isResidentNodeUsed()}.
//	 */
//	public void testIsResidentNodeUsed() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#isContextNodeUsed()}.
//	 */
//	public void testIsContextNodeUsed() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#isInputNodeUsed()}.
//	 */
//	public void testIsInputNodeUsed() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertGenerativeInputNode(double, double)}.
//	 */
//	public void testInsertGenerativeInputNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setInputInstanceOf(unbbayes.prs.mebn.GenerativeInputNode, unbbayes.prs.mebn.ResidentNode)}.
//	 */
//	public void testSetInputInstanceOf() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#updateArgumentsOfObject(java.lang.Object)}.
//	 */
//	public void testUpdateArgumentsOfObject() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#updateInputInstanceOf(unbbayes.prs.mebn.GenerativeInputNode)}.
//	 */
//	public void testUpdateInputInstanceOf() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#insertContextNode(double, double)}.
//	 */
//	public void testInsertContextNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#deleteSelected(java.lang.Object)}.
//	 */
//	public void testDeleteSelected() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#selectNode(unbbayes.prs.Node)}.
//	 */
//	public void testSelectNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#updateFormulaActiveContextNode()}.
//	 */
//	public void testUpdateFormulaActiveContextNode() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#addNewOrdinaryVariableInMFrag()}.
//	 */
//	public void testAddNewOrdinaryVariableInMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#addNewOrdinaryVariableInResident()}.
//	 */
//	public void testAddNewOrdinaryVariableInResident() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#removeOrdinaryVariableOfMFrag(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testRemoveOrdinaryVariableOfMFrag() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#addOrdinaryVariableInResident(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testAddOrdinaryVariableInResident() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#removeOrdinaryVariableInResident(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testRemoveOrdinaryVariableInResident() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setOVariableSelectedInResidentTree(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testSetOVariableSelectedInResidentTree() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setOVariableSelectedInMFragTree(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testSetOVariableSelectedInMFragTree() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameOVariableOfResidentTree(java.lang.String)}.
//	 */
//	public void testRenameOVariableOfResidentTree() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameOVariableOfMFragTree(java.lang.String)}.
//	 */
//	public void testRenameOVariableOfMFragTree() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameOVariableInArgumentEditionPane(java.lang.String)}.
//	 */
//	public void testRenameOVariableInArgumentEditionPane() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#selectOVariableInEdit(unbbayes.prs.mebn.OrdinaryVariable)}.
//	 */
//	public void testSelectOVariableInEdit() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#createObjectEntity()}.
//	 */
//	public void testAddObjectEntity() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#renameObjectEntity(unbbayes.prs.mebn.entity.ObjectEntity, java.lang.String)}.
//	 */
//	public void testRenameObjectEntity() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#removeObjectEntity(unbbayes.prs.mebn.entity.ObjectEntity)}.
//	 */
//	public void testRemoveObjectEntity() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#loadGenerativeMEBNIntoKB()}.
//	 */
//	public void testPreencherKB() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#executeContext()}.
//	 */
//	public void testExecuteContext() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#makeEntityAssert(java.lang.String)}.
//	 */
//	public void testMakeEntityAssert() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#makeRelationAssert(java.lang.String)}.
//	 */
//	public void testMakeRelationAssert() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#saveDefinitionsFile()}.
//	 */
//	public void testSaveDefinitionsFile() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#linkOrdVariable2Entity(java.lang.String, java.lang.String)}.
//	 */
//	public void testLinkOrdVariable2Entity() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getMultiEntityBayesianNetwork()}.
//	 */
//	public void testGetMultiEntityBayesianNetwork() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setMultiEntityBayesianNetwork(unbbayes.prs.mebn.MultiEntityBayesianNetwork)}.
//	 */
//	public void testSetMultiEntityBayesianNetwork() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#getMebnEditionPane()}.
//	 */
//	public void testGetMebnEditionPane() {
//		fail("Not yet implemented"); // TODO
//	}
//
//	/**
//	 * Test method for {@link unbbayes.controller.MEBNController#setMebnEditionPane(unbbayes.gui.MEBNEditionPane)}.
//	 */
//	public void testSetMebnEditionPane() {
//		fail("Not yet implemented"); // TODO
//	}
//	
//	
	/**
	 * Test method for {@link unbbayes.controller.MEBNController#loadFindingsFile(java.io.File file)}.
	 */
	public void testLoadFindingsFile() {
		try {
			this.controller.loadFindingsFile(new File(plmFileName));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 *  Use this for test suite creation
	 */
	public static Test suite() {
		return new TestSuite(MEBNControllerTest.class);
	}
}
