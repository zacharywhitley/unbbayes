/**
 * 
 */
package unbbayes.prs.mebn.compiler.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.gui.table.GUIPotentialTable;
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
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.exception.MEBNException;
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
	
	
	private static final String TABLE_TO_PARSE = "if any STi have (OpSpec = Cardassian & HarmPot = true)  "
		+ " [Un = .90, Hi = (1 - Un) * .8, Me = (1 - Un) * .2, Lo = 0] "
		+ "else if any STl have (OpSpec = Friend & HarmPot = true | OpSpec = Friend & HarmPot = false)  "
		+ " [Un = 0, Hi = 0, Me = .01, Lo = .99] "
		+ "else [Un = 0, Hi = 0, Me = 0, Lo = 1] ";

	
	/**
	 * @param arg0
	 */
	public CompilerTest(String arg0) {
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

		//new GUIPotentialTable(tab).showTable("VAI FUNCIONAR!");
		
		
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

	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#init(java.lang.String)}.
	 */
	public void testInit() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#parse()}.
	 */
	public void testParse() {
		fail("Not yet implemented"); // TODO
		
	}

}
