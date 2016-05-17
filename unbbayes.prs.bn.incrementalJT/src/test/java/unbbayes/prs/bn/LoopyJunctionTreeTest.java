package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.prs.Node;

public class LoopyJunctionTreeTest extends TestCase {
	
	private double marginals[][] = {
			// Indicator 1
			{0.047619048, 0.952380952},
			// Indicator 2
			{0.001642036, 0.998357964},
			// Indicator 3
			{0.049964814,	0.950035186},
			// Indicator 4
			{0.049964814,	0.950035186},
	};
	
	private int numIndicators = 4;
	private double[][][] rcpIndicators = {
		// 1st table (indicators 1&2)
		{
			// Indicator2=Yes; Indicator2=No
			{0.00070373,	0.046915318},	// Indicator1=yes
			{0.000938306,	0.951442646}    // Indicator1=no
		},	
		// 2nd table (indicators 1&3)
		{
			// Indicator3
			{0.003753225,	0.043865822},
			{0.046211588,	0.906169364}    
		},
		// 1&4
		{
			// Indicator4
			{0.001642036,	0.045977011},
			{0.048322777,	0.904058175}    
		},
		// 2&3
		{
			// Indicator3
			{0.000234577,	0.00140746},
			{0.049730237,	0.948627727}    
		},
		// 2&4
		{
			// Indicator4
			{0,				0.001642036},
			{0.049964814,	0.94839315}    
		},
		// 3&4
		{
			// Indicator4
			{0.001642036,	0.048322777},
			{0.048322777,	0.901712409}    
		},
	};


	public LoopyJunctionTreeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		assertEquals(this.rcpIndicators.length, this.numCombinations(numIndicators, 2));
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private long factorial(long n) {
		long ret = 1;
		for (long i = 2; i <= n; i++) {
			ret *= i;
		}
		return ret;
	}
	private long numCombinations(long total, long groupOf) {
		return factorial(total)/(factorial(groupOf)*factorial(total-groupOf));
	}
	
	public final void testTripleLoopyCase() throws Exception {
		LoopyJunctionTree jt = new LoopyJunctionTree();
		jt.setLoopy(true);
		jt.setMaxLoopyBPIteration(Integer.MAX_VALUE);
		jt.setMaxLoopyBPTimeMillis(3000);	// 3 sec max
		
		int indicatorNumbers[] = {2,3,4};
		
		// creating the variables (necessary for creating cliques)
		List<ProbabilisticNode> nodes = new ArrayList<ProbabilisticNode>(3);
		for (int indicator : indicatorNumbers) {
			ProbabilisticNode node = new ProbabilisticNode();
			node.setName(""+indicator);
			node.appendState("Yes");
			node.appendState("No");
			nodes.add(node);
		}
		
		// creating cliques of pairs of variables
		for (int i = 0; i < nodes.size()-1; i++) {
			for (int j = i+1; j <  nodes.size(); j++) {
				ProbabilisticNode node1 = nodes.get(i);
				ProbabilisticNode node2 = nodes.get(j);
				Clique clique = new Clique();
				clique.getNodesList().add(node1);
				clique.getNodesList().add(node2);
				jt.getCliques().add(clique);
				
				if (node1.getAssociatedClique() == null) {
					node1.setAssociatedClique(clique);
					clique.getAssociatedProbabilisticNodesList().add(node1);
				}
				if (node2.getAssociatedClique() == null) {
					node2.setAssociatedClique(clique);
					clique.getAssociatedProbabilisticNodesList().add(node2);
				}
			}
		}
		
		// creating separators between pairs of cliques
		for (int i = 0; i < jt.getCliques().size()-1; i++) {
			for (int j = i+1; j < jt.getCliques().size(); j++) {
				Clique clique1 = jt.getCliques().get(i);
				Clique clique2 = jt.getCliques().get(j);
				
				List<Node> intersection = new ArrayList<Node>(clique1.getNodesList());
				intersection.retainAll(clique2.getNodesList());
				
				Separator sep = new Separator(clique1, clique2, false);
				sep.setNodes(intersection);
				jt.addSeparator(sep);
				jt.addParent(clique1, clique2);
				clique1.addChild(clique2);
			}
		}
		
		// synchronizing the variables present in cliques/separators with the variables in their respective potential tables
		new JunctionTreeAlgorithm().addVariablesToCliqueAndSeparatorTables(null, jt);
		
		// fill clique potentials
		
		// clique {indicator1, indicator2}
		for (Clique clique : jt.getCliques()) {
			assertEquals(clique.toString(), 2 , clique.getNodesList().size());
			
			Node node1 = clique.getNodesList().get(0);
			Node node2 = clique.getNodesList().get(1);
			int indicatorTableIndex = getIndicatorTableIndexFromNodes(node1 , node2, numIndicators);
			
			PotentialTable table = clique.getProbabilityFunction();
			assertEquals(clique.toString() + " ; " + node1 + " ; " + table.getVariableAt(0), node1 , table.getVariableAt(0));
			assertEquals(clique.toString() + " ; " + node2 + " ; " + table.getVariableAt(1), node2 , table.getVariableAt(1));
			
			for (int i = 0; i < table.tableSize(); i++) {
				int[] coord = table.getMultidimensionalCoord(i);
				float value = (float) this.rcpIndicators[indicatorTableIndex][coord[0]][coord[1]];
				table.setValue(i, value );
			}
		}
		
		// Check that marginals of cliques match expected
		for (Clique clique : jt.getCliques()) {
			assertEquals(clique.toString(), 2, clique.getNodesList().size());
			for (Node n : clique.getNodesList()) {
				assertTrue(clique.toString() + " ; " + n.toString(), n instanceof ProbabilisticNode); 
				ProbabilisticNode node = (ProbabilisticNode) n;
				assertEquals(clique.toString() + " ; " + n.toString(), 2, node.getStatesSize());
				
				// marginalize out from clique the variables other than the current node
				PotentialTable dummyTable = (PotentialTable) clique.getProbabilityFunction().getTemporaryClone();
				for (int i = 0; i < dummyTable.getVariablesSize(); i++) {
					if (!dummyTable.getVariableAt(i).equals(node)) {
						dummyTable.removeVariable(dummyTable.getVariableAt(i));
						i--;
					}
				}
				assertEquals(clique.toString() + " ; " + n.toString(), 1, dummyTable.getVariablesSize());
				
				// compare the marginalized values from clique with the values we expected
				for (int i = 0; i < node.getStatesSize(); i++) {
					assertEquals(clique.toString() + " ; " + n.toString() + "[" + i + "]", marginals[getExpectedMarginalIndexFromNode(node)][i], dummyTable.getValue(i), 0.00005);
				}
			}
		}
		
		
		// fill separator potentials 
		for (Separator sep : jt.getSeparators()) {
			assertEquals(sep.toString(), 1, sep.getNodesList().size());
			PotentialTable table = sep.getProbabilityFunction();
			assertEquals(sep.toString(), 1, table.getVariablesSize());
			assertEquals(sep.toString(), 2, table.tableSize());
			for (int i = 0; i < table.tableSize(); i++) {
//				table.setValue(i, 1f);
				int[] coord = table.getMultidimensionalCoord(i);
				table.setValue(i, (float) marginals[getExpectedMarginalIndexFromNode((ProbabilisticNode) sep.getNodesList().get(0))][i]);
			}
		}
		
		// just update internal IDs
		jt.updateCliqueAndSeparatorInternalIdentificators();
		

		// print clique potentials
//		System.out.println("Clique potentials before global consistency");
//		for (Clique clique : jt.getCliques()) {
//			System.out.println("Clique = " + clique);
//			PotentialTable table = clique.getProbabilityFunction();
//			System.out.println(table.getVariableAt(0) + "\t,\t" + table.getVariableAt(1)+ "\t,\tProb");
//			for (int i = 0; i < table.tableSize(); i++) {
//				int[] coord = table.getMultidimensionalCoord(i);
//				System.out.println(coord[0] + "\t,\t" + coord[1]+ "\t,\t" + table.getValue(i));
//			}
//		}
		
		// do loopy propagation for global consistency
		jt.initConsistency();
		
		
		// fill marginal of nodes and print marginals
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).updateMarginal();
			System.out.print(nodes.get(i).toString() + " = ");
			for (int j = 0; j < nodes.get(i).getStatesSize(); j++) {
				System.out.print(" " + nodes.get(i).getMarginalAt(j));
			}
			System.out.println();
		}
		
		// print clique potentials
		System.out.println("Clique potentials after consistency");
		for (Clique clique : jt.getCliques()) {
			System.out.println("Clique = " + clique);
			PotentialTable table = clique.getProbabilityFunction();
			System.out.println(table.getVariableAt(0) + "\t,\t" + table.getVariableAt(1)+ "\t,\tProb");
			for (int i = 0; i < table.tableSize(); i++) {
				int[] coord = table.getMultidimensionalCoord(i);
				System.out.println(coord[0] + "\t,\t" + coord[1]+ "\t,\t" + table.getValue(i));
			}
		}
		
		// Check that marginals of cliques match expected
		for (Clique clique : jt.getCliques()) {
			assertEquals(clique.toString(), 2, clique.getNodesList().size());
			for (Node n : clique.getNodesList()) {
				assertTrue(clique.toString() + " ; " + n.toString(), n instanceof ProbabilisticNode); 
				ProbabilisticNode node = (ProbabilisticNode) n;
				assertEquals(clique.toString() + " ; " + n.toString(), 2, node.getStatesSize());
				
				// marginalize out from clique the variables other than the current node
				PotentialTable dummyTable = (PotentialTable) clique.getProbabilityFunction().getTemporaryClone();
				for (int i = 0; i < dummyTable.getVariablesSize(); i++) {
					if (!dummyTable.getVariableAt(i).equals(node)) {
						dummyTable.removeVariable(dummyTable.getVariableAt(i));
						i--;
					}
				}
				assertEquals(clique.toString() + " ; " + n.toString(), 1, dummyTable.getVariablesSize());
				
				// compare the marginalized values from clique with the values we expected
				for (int i = 0; i < node.getStatesSize(); i++) {
					double marginalFromClique = dummyTable.getValue(i);
					System.out.println(clique.toString() + " ; " + n.toString() + "[" + i + "] Expected=" +  marginals[getExpectedMarginalIndexFromNode(node)][i] + "; Actual = " + marginalFromClique);
					assertEquals(clique.toString() + " ; " + n.toString(), marginals[getExpectedMarginalIndexFromNode(node)][i], marginalFromClique, 0.00005);
				}
			}
		}
		
		
		
		// assert propagation has converged
		assertFalse(jt.isModified());
		
		// backup clique and separator potentials
		for (Clique clique : jt.getCliques()) {
			clique.getProbabilityFunction().copyData();
		}
		for (Separator sep : jt.getSeparators()) {
			sep.getProbabilityFunction().copyData();
		}
		
		// calculate joint probabilities
		PotentialTable jointProbs = new ProbabilisticTable();
		jointProbs.addVariable(nodes.get(0));
		jointProbs.addVariable(nodes.get(1));
		jointProbs.addVariable(nodes.get(2));
		
		for (int node1Index = 0; node1Index < nodes.get(0).getStatesSize(); node1Index++) {
			for (int node2Index = 0; node2Index < nodes.get(1).getStatesSize(); node2Index++) {
				for (int node3Index = 0; node3Index < nodes.get(2).getStatesSize(); node3Index++) {
					
					
					int nodeIndexes[] = {node1Index, node2Index, node3Index}; 
					assertEquals(nodes.size(), nodeIndexes.length);
					float jointProb = 1;
					for (int nodeIndexToAddEvidence = 0; nodeIndexToAddEvidence < nodes.size(); nodeIndexToAddEvidence++) {
						ProbabilisticNode node = nodes.get(nodeIndexToAddEvidence);
						jointProb *= node.getMarginalAt(nodeIndexes[nodeIndexToAddEvidence]);
						System.out.println("Joint at this point = " + jointProb);
						node.addFinding(nodeIndexes[nodeIndexToAddEvidence]);
						node.updateEvidences();
						
						if (nodeIndexToAddEvidence + 1 >= nodes.size()) {
							break;
						}
						
						// propagate
						jt.consistency();
						
						System.out.println(jt.isModified()?"Loopy BP didn't converge":"Loopy BP converged.");
						
						// fill marginal of nodes and print marginals
						for (int i = 0; i < nodes.size(); i++) {
							nodes.get(i).updateMarginal();
							System.out.print(nodes.get(i).toString() + " = ");
							for (int j = 0; j < nodes.get(i).getStatesSize(); j++) {
								System.out.print(" " + nodes.get(i).getMarginalAt(j));
							}
							System.out.println();
						}
						// print clique potentials
						System.out.println("Clique potentials after consistency");
						for (Clique clique : jt.getCliques()) {
							System.out.println("Clique = " + clique);
							PotentialTable table = clique.getProbabilityFunction();
							System.out.println(table.getVariableAt(0) + "\t,\t" + table.getVariableAt(1)+ "\t,\tProb");
							for (int j = 0; j < table.tableSize(); j++) {
								int[] coord = table.getMultidimensionalCoord(j);
								System.out.println(coord[0] + "\t,\t" + coord[1]+ "\t,\t" + table.getValue(j));
							}
						}
					}
				
					System.out.println("Joint ( " + nodes.get(node1Index) + " = " + node1Index + " , " 
												+ nodes.get(node2Index) + " = " + node2Index + " , " 
												+ nodes.get(node3Index) + " = " + node3Index+ " ) = " + jointProb );
					
					jointProbs.setValue(nodeIndexes, (float) jointProb);

					// restore backup of clique and separator potentials
					for (Clique clique : jt.getCliques()) {
						clique.getProbabilityFunction().restoreData();
					}
					for (Separator sep : jt.getSeparators()) {
						sep.getProbabilityFunction().restoreData();
					}
					
					// fill marginal of nodes and print marginals
					for (int i = 0; i < nodes.size(); i++) {
						nodes.get(i).updateMarginal();
						System.out.print(nodes.get(i).toString() + " = ");
						for (int j = 0; j < nodes.get(i).getStatesSize(); j++) {
							System.out.print(" " + nodes.get(i).getMarginalAt(j));
						}
						System.out.println();
					}
					
				}
			}
		}
		
		System.out.println();
		System.out.println("Joint probability: ");
		System.out.println();
		System.out.println( "Indicator" + nodes.get(0).getName() + "\t,\t" 
						  + "Indicator" + nodes.get(1).getName() + "\t,\t" 
						  + "Indicator" + nodes.get(2).getName() + "\t,\tProb");
		float sum = 0;
		for (int node1Index = 0, i = 0; node1Index < nodes.get(0).getStatesSize(); node1Index++) {
			for (int node2Index = 0; node2Index < nodes.get(1).getStatesSize(); node2Index++) {
				for (int node3Index = 0; node3Index < nodes.get(2).getStatesSize(); node3Index++) {
					int nodeIndexes[] = {node1Index, node2Index, node3Index}; 
					System.out.println( ((node1Index%2==0)?"Yes":"No") + "\t,\t" 
									  + ((node2Index%2==0)?"Yes":"No") + "\t,\t" 
									  + ((node3Index%2==0)?"Yes":"No") + "\t,\t" 
									  + jointProbs.getValue(nodeIndexes) );
					sum += jointProbs.getValue(nodeIndexes);
					i++;
				}
			}
		}
		assertEquals(1f, sum, 0.0005);
		
		
		System.out.println();
		System.out.println();
		System.out.println("Conditional mutual information");
		System.out.println();
		for (int indexOfCondition = 0; indexOfCondition < jointProbs.variableCount(); indexOfCondition++) {
			System.out.print("I(");
			for (int indexOfOtherVars = 0; indexOfOtherVars < jointProbs.variableCount(); indexOfOtherVars++) {
				if (indexOfCondition != indexOfOtherVars) {
					System.out.print("Indicator"+jointProbs.getVariableAt(indexOfOtherVars).getName());
					if (indexOfOtherVars + 1 < jointProbs.variableCount()) {
						System.out.print(",");
					}
				}
			}
			System.out.println("|Indicator" + jointProbs.getVariableAt(indexOfCondition).getName()  + ") \t,\t " + getConditionalMutualInformation(jointProbs, indexOfCondition));
		}
	}
	
	/**
	 * calculates I(X;Y|Z)
	 * @param jointProbs : P(X,Y,Z)
	 * @param zIndex : index of Z in jointProbs
	 * @return I(X;Y|Z)
	 */
	private float getConditionalMutualInformation(PotentialTable jointProbs, int zIndex) {
		assertEquals("This version only allows 3 vars.", 3, jointProbs.getVariablesSize());
		
		// decide who is X and who is Y
		int xIndex = 0;
		while ( xIndex < jointProbs.getVariablesSize() ) {
			if (xIndex != zIndex) {
				break;
			} else {
				xIndex++;
			}
		}
		
		int yIndex = 0;
		while ( yIndex < jointProbs.getVariablesSize() ) {
			if ((yIndex != zIndex) && (yIndex != xIndex)) {
				break;
			} else {
				yIndex++;
			}
		}
		
		assertTrue(xIndex != zIndex && xIndex != yIndex && yIndex != zIndex);
		
		// SUM[p(x,y,z)log(p(x,y|z) / p(x|z)p(y|z))]
		float sum = 0;
		for (int i = 0; i < jointProbs.tableSize(); i++) {
			int[] coord = jointProbs.getMultidimensionalCoord(i);
			
			// P(X,Y,Z)
			float joint = jointProbs.getValue(i);
			if (joint == 0f) {
				continue;	// nothing to sum
			}
			
			// P(Z)
			// marginalize out other vars
			PotentialTable clone = jointProbs.getTemporaryClone();
			assertEquals(3, clone.getVariablesSize());
			clone.removeVariable(jointProbs.getVariableAt(xIndex));
			clone.removeVariable(jointProbs.getVariableAt(yIndex));
			assertEquals(1, clone.getVariablesSize());
			float pZ = clone.getValue(coord[zIndex]);
			
			// P(x,y|z) = p(x,y,z)/p(z)
			float pXYZ = joint/pZ;
			
			//P(x,z)
			// marginalize out Y
			clone = jointProbs.getTemporaryClone();
			assertEquals(3, clone.getVariablesSize());
			clone.removeVariable(jointProbs.getVariableAt(yIndex));
			assertEquals(2, clone.getVariablesSize());
			
			// P(x|z) = P(x,z)/P(z)
			int[] bivariateCoord = clone.getMultidimensionalCoord(0);
			bivariateCoord[clone.indexOfVariable((Node) jointProbs.getVariableAt(xIndex))] = coord[xIndex];
			bivariateCoord[clone.indexOfVariable((Node) jointProbs.getVariableAt(zIndex))] = coord[zIndex];
			float pXZ = clone.getValue(bivariateCoord)/pZ;

			//P(y,z)
			// marginalize out x
			clone = jointProbs.getTemporaryClone();
			assertEquals(3, clone.getVariablesSize());
			clone.removeVariable(jointProbs.getVariableAt(xIndex));
			assertEquals(2, clone.getVariablesSize());
			
			// P(y|z) = P(y,z)/P(z)
			bivariateCoord = clone.getMultidimensionalCoord(0);
			bivariateCoord[clone.indexOfVariable((Node) jointProbs.getVariableAt(yIndex))] = coord[yIndex];
			bivariateCoord[clone.indexOfVariable((Node) jointProbs.getVariableAt(zIndex))] = coord[zIndex];
			float pYZ = clone.getValue(bivariateCoord)/pZ;
			
			// p(x,y,z)log(p(x,y|z) / p(x|z)p(y|z))
			float weightedInfo = (float) (joint * Math.log(pXYZ / (pXZ*pYZ) ));
			if (Float.isNaN(weightedInfo)) {
				System.err.println("Found NaN");
				continue;
			}
			sum += weightedInfo;
		}
		
		return sum;
	}
	
	private int getExpectedMarginalIndexFromNode(ProbabilisticNode node) {
		return Integer.parseInt(node.getName()) - 1;
	}

	private int getIndicatorTableIndexFromNodes(Node node1, Node node2, int numIndicators) {
		int index1 = Integer.parseInt(node1.getName());
		int index2 = Integer.parseInt(node2.getName());
		if (index1 == index2) {
			throw new IllegalArgumentException(node1 + " = " + node2);
		}
		if (index1 > index2) {
			int aux = index1;
			index1 = index2;
			index2 = aux;
		}
		int base = 0;
		for (int i = 1; i < index1; i++) {
			base += (numIndicators-i);
		}
		
		int offset = index2 - index1 - 1;
		
		return base + offset;
	}
	

//	/**
//	 * Adds nodes in {@link Clique#getNodesList()} and {@link Separator#getNodesList()} to 
//	 * their respective clique/separator tables by using {@link PotentialTable#addVariable(INode)}.
//	 *	@param net : net whose junctionTree was generated from
//	 * @param junctionTree : junction tree where cliques and separators belong.
//	 */
//	private void addVariablesToCliqueAndSeparatorTables(IJunctionTree junctionTree) {
//	
//		for (int i = junctionTree.getCliques().size() - 1; i >= 0; i--) {
//			Clique auxClique = (Clique) junctionTree.getCliques().get(i);
//			PotentialTable auxTable = auxClique.getProbabilityFunction();
//	
//			int numNodes = auxClique.getNodesList().size();
//			for (int c = 0; c < numNodes; c++) {
//				auxTable.addVariable(auxClique.getNodesList().get(c));
//			}
//		}
//	
//		for (Separator auxSep : junctionTree.getSeparators()) {
//			PotentialTable auxTable = auxSep.getProbabilityFunction();
//			int numNodes = auxSep.getNodesList().size();
//			for (int c = 0; c < numNodes; c++) {
//				auxTable.addVariable(auxSep.getNodesList().get(c));
//			}
//		}
//	}

}
