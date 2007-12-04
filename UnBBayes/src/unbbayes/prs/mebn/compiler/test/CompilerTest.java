/**
 * 
 */
package unbbayes.prs.mebn.compiler.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.table.GUIPotentialTable;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.util.Debug;
import junit.framework.TestCase;

/*
 * Just a data type/class used by some tests
 */
class Conjunto {
	public Node[] pais;

	public Conjunto(Node[] pais) {
		this.pais = pais;
	}
}

/*
 * Just a data type used by some tests
 */
class ListaConjunto {
	public List<Conjunto> conjuntos = new ArrayList<Conjunto>();

	public Map<String, Integer> mapa = new HashMap<String, Integer>();

	public int tamanhoConjunto;

	public ListaConjunto(String[] nomes) {
		this.tamanhoConjunto = nomes.length;
		for (int i = 0; i < nomes.length; ++i) {
			mapa.put(nomes[i], i);
		}
	}
}

/**
 * @author user
 *
 */
public class CompilerTest extends TestCase {
	

	private MultiEntityBayesianNetwork mebn = null; 
	private unbbayes.prs.mebn.compiler.Compiler tableParser = null;
	
	public CompilerTest(String arg0) {
		super(arg0);
		Debug.setDebug(true);
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			mebn = prOwlIO.loadMebn(new File("examples/mebn/StarTrek38.owl")); 
			Debug.println("LOAD COMPLETE"); 
		}
		catch (IOMebnException e){
			e.printStackTrace();
			fail("ERROR IO PROWL!!!!!!!!!"); 
			
		}
		catch (IOException e){			
			e.printStackTrace();
			fail("ERROR IO!!!!!!!!!"); 
		}
		
		
		
		tableParser = new Compiler((DomainResidentNode)mebn.getNode("DangerToSelf"));
		
	}

	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
		assertNotNull(tableParser);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse()'
	 */
	public void testNormalConsistencyCheck() {
		
		//		 should go all right
		String tableString =  
			" if any st.t have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st.z.t have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any sr.st have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any st.z have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage());
			
		} 
		
		tableParser.getTempTable();
		
	}
	
	/*
	 * Test if parser detect no default distribution table
	 * or else clause undeclared
	 *
	 */
	public void testConsistencyNoDefDistro() {
		//		 Should fail, no default distro
		String tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = MIN(0;1) , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = MAX(0;.99) ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = CARDINALITY(OpSpec)*0.1 , Medium = .15 , Low = 0 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " ;
		
			
		try  {
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage() + " at " + tableString.substring(tableParser.getIndex()-1,tableParser.getIndex()+10));
			e.printStackTrace();
			System.exit(1);
		} 
		
		
		//		 Should fail, no else clause
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " ;
		
			
		try  {
			Debug.println("=> PARSING A NO-ELSE CLAUSE TABLE");
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage()+ ", " + e.getClass().getName());
		} 
		
	}
	
	/*
	 * Tests if a single node state is between [0,1]
	 */
	public void testConsistencySingleState() {

		//		 Should fail, some state is above 1
		String tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 

		//		 Should fail, some state is below 0
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .60 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Medium = -0.5 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
	}
	
	
	public void testProbDistroSum() {

		
		//		 Should fail sum is above 1 in default distro
		String tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Medium = 0 , Low = 1 ] ";
		
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is above 1
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0.2 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is below 1
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .0 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is below 1 in default distribution
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 0 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		
	}
	
	public void testNormalConsistencyCheckWithFunctions() {
		//		 should go all right
		String tableString =  
			" if any st have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = MIN ( CARDINALITY (OpSpec) * 2 ; .2 ) , Low = 1 - Medium ]  " +
			" else if any st.t have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = (.005 + .005) , Low = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = (((1 - Medium) - Un) - Hi ) ]  " +
			" else if any st.z have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any st.sr.z have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = MAX (.5 ; CARDINALITY (HarmPotential)) , Low = 1 - Medium ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage() + " at index " + tableParser.getIndex());
		} 
		
	}
	
	
	public void testNoIfStatement() {
		//		 should go all right
		String tableString =  
			" [ Un = 0 , Hi = 0 , Medium = MIN ( -3 * -2 ; .2 ) , Low = 1 - Medium ]  ";		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage() + " at index " + tableParser.getIndex());
			
		} 
		
	}
	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#main(java.lang.String[])}.
	 */
	public void testMain() {
		ProbabilisticNetwork rede = new ProbabilisticNetwork("MEBN Table Test");

		ListaConjunto conjuntos = new ListaConjunto(new String[] { "OpSpec",
				"HarmPot" });

		ProbabilisticNode dangerToSelf = new ProbabilisticNode();
		dangerToSelf.setName("DangerToSelf");
		dangerToSelf.setDescription("Danger to self");
		dangerToSelf.appendState("Un");
		dangerToSelf.appendState("Hi");
		dangerToSelf.appendState("Me");
		dangerToSelf.appendState("Lo");
		PotentialTable auxTabPot = dangerToSelf.getPotentialTable();
		auxTabPot.addVariable(dangerToSelf);
		rede.addNode(dangerToSelf);

		ProbabilisticNode opSpec = new ProbabilisticNode();
		opSpec.setName("OpSpec");
		opSpec.setDescription("Operator Specie");
		opSpec.appendState("Cardassian");
		opSpec.appendState("Unknown");
		opSpec.appendState("Friend");
		opSpec.appendState("Klingon");
		opSpec.appendState("Romulan");
		auxTabPot = opSpec.getPotentialTable();
		auxTabPot.addVariable(opSpec);
		rede.addNode(opSpec);

		Edge auxArco = new Edge(opSpec, dangerToSelf);
		rede.addEdge(auxArco);

		ProbabilisticNode harmPotential = new ProbabilisticNode();
		harmPotential.setName("HarmPotential");
		harmPotential.setDescription("Harm Potential");
		harmPotential.appendState("True");
		harmPotential.appendState("False");
		auxTabPot = harmPotential.getPotentialTable();
		auxTabPot.addVariable(harmPotential);
		rede.addNode(harmPotential);

		auxArco = new Edge(harmPotential, dangerToSelf);
		rede.addEdge(auxArco);

		conjuntos.conjuntos.add(new Conjunto(
				new Node[] { opSpec, harmPotential }));

		opSpec = new ProbabilisticNode();
		opSpec.setName("OpSpec2");
		opSpec.setDescription("Operator Specie 2");
		opSpec.appendState("Cardassian");
		opSpec.appendState("Unknown");
		opSpec.appendState("Friend");
		opSpec.appendState("Klingon");
		opSpec.appendState("Romulan");
		auxTabPot = opSpec.getPotentialTable();
		auxTabPot.addVariable(opSpec);
		rede.addNode(opSpec);

		auxArco = new Edge(opSpec, dangerToSelf);
		rede.addEdge(auxArco);

		harmPotential = new ProbabilisticNode();
		harmPotential.setName("HarmPotential2");
		harmPotential.setDescription("Harm Potential 2");
		harmPotential.appendState("True");
		harmPotential.appendState("False");
		auxTabPot = harmPotential.getPotentialTable();
		auxTabPot.addVariable(harmPotential);
		rede.addNode(harmPotential);

		auxArco = new Edge(harmPotential, dangerToSelf);
		rede.addEdge(auxArco);

		conjuntos.conjuntos.add(new Conjunto(
				new Node[] { opSpec, harmPotential }));

		PotentialTable tab = dangerToSelf.getPotentialTable();
		for (int i = 0; i < tab.tableSize();) {
			int[] coord = tab.voltaCoord(i);
			int countSTi = 0;
			int countSTj = 0;
			int countSTk = 0;
			int countSTl = 0;
			int countSTm = 0;

			for (int j = 0; j < conjuntos.conjuntos.size(); ++j) {
				int opSpecIndex = conjuntos.mapa.get("OpSpec");
				int harmPotIndex = conjuntos.mapa.get("HarmPot");
				boolean ehCarda = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ opSpecIndex] == 0; // 0 ? o indice do cardassian
				boolean ehTrue = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ harmPotIndex] == 0; // 0 ? o indice do true
				if (ehCarda && ehTrue) {
					// STi
					countSTi++;
				}

				boolean ehRomu = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ opSpecIndex] == 4; // 4 ? o indice do romulan
				if (ehRomu && ehTrue) {
					// STj
					countSTj++;
				}

				boolean ehUnk = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ opSpecIndex] == 1; // 1 ? o indice do unk

				if (ehUnk && ehTrue) {
					// stk
					countSTk++;
				}

				boolean ehklin = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ opSpecIndex] == 3; // 3 ? o indice do klin

				if (ehklin && ehTrue) {
					// stl
					countSTl++;
				}

				boolean ehfri = coord[1 + (j * conjuntos.tamanhoConjunto)
						+ opSpecIndex] == 2; // 2 ? o indice do fri

				if (ehfri && ehTrue) {
					// stm
					countSTm++;
				}
			}			
			
//			dangerToSelf.appendState("Un");
//			dangerToSelf.appendState("Hi");
//			dangerToSelf.appendState("Me");
//			dangerToSelf.appendState("Lo");			
			
			if (countSTi > 0) {
				double unValue = 0.9 + Math.min(0.1, 0.025 * countSTi);
				tab.setValue(i, (float)unValue);
				
				double hiValue = (1-unValue) * 0.8;
				tab.setValue(i+1, (float)hiValue);
				
				double meValue = (1-unValue) * 0.2;
				tab.setValue(i+2, (float) meValue);
				
				tab.setValue(i+3, 0);
			} else if (countSTj > 0) {
				
			}
			
			i += dangerToSelf.getStatesSize();
		}

		
		GUIPotentialTable guiCPT = new GUIPotentialTable(tab);
		guiCPT.showTable("VAI FUNCIONAR!");
		
		// while (true);
		
		/*
		Compiler c = new Compiler(null);
		c.init(TABLE_TO_PARSE); 
		try  {
			c.parse();
		} catch (MEBNException e) {
			fail(e.getMessage() + ": "+ e.getClass().getName());
		}
		*/
		 
	}
	
	
	
	public void testGenerateCPT() {
		
		DomainResidentNode harmPotential = this.mebn.getDomainResidentNode("HarmPotential");
		DomainResidentNode starshipClass = this.mebn.getDomainResidentNode("StarshipClass");
		DomainResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
		
		OrdinaryVariable st = harmPotential.getOrdinaryVariableByName("st");
		OrdinaryVariable t = harmPotential.getOrdinaryVariableByName("t");
		
		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
		OVInstance t0 = OVInstance.getInstance(st, "T0", st.getValueType());
		
		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
		
		SSBNNode harmPotential_ST0_T0 = SSBNNode.getInstance(net, harmPotential);
		try {
			harmPotential_ST0_T0.addArgument(st0);
			harmPotential_ST0_T0.addArgument(t0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
		try {
			distFromOwn_ST0_T0.addArgument(st0);
			distFromOwn_ST0_T0.addArgument(t0);
			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T0, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		//net.addEdge(new Edge(distFromOwn_ST0_T0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
		
		SSBNNode starshipClass_ST0 = SSBNNode.getInstance(net, starshipClass);
		try {
			starshipClass_ST0.addArgument(st0);
			harmPotential_ST0_T0.addParent(starshipClass_ST0, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		//net.addEdge(new Edge(starshipClass_ST0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
		
		
		Compiler compiler = new Compiler(harmPotential,harmPotential_ST0_T0);
		
		compiler.init(harmPotential_ST0_T0);
		
		PotentialTable table = null;
		try {
			compiler.parse();		
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			table = compiler.getCPT();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
		guiCPT.showTable("VAI FUNCIONAR???");
		
		while(true);
		
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#init(java.lang.String)}.
	 */
	/*
	public void testInit() {
		fail("Not yet implemented"); // TODO
	}
	*/
	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#parse()}.
	 */
	/*
	public void testParse() {
		fail("Not yet implemented"); // TODO
		
	}
	*/
}
