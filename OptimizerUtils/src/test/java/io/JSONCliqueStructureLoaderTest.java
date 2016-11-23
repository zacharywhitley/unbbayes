package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * This is a test case for {@link JSONCliqueStructureLoader}.
 * The public final void methods with names starting with "test" are examples of how to use {@link JSONCliqueStructureLoader}.
 * @author Shou Matsumoto
 *
 */
public class JSONCliqueStructureLoaderTest extends TestCase {

	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public final void testLoadSimple() throws FileNotFoundException, IOException {
		
		String fileName = "src/test/resources/cliquesSimple.json";
		
		// instantiate the loader
		JSONCliqueStructureLoader loader = new JSONCliqueStructureLoader();
		
		// load (parse) the file
		loader.load(new FileInputStream(new File(fileName)));
		
		// start printing the content we just parsed
		
		
		// extract the names and sizes
		List<String> varNames = loader.getVariableNames();
		List<Integer> varSizes = loader.getVariablesSizes();
		// basic assertions
		assertEquals(varNames.size(),  varSizes.size());
		assertEquals(4,  varSizes.size());
		// names and sizes
		for (int i = 0; i < varNames.size(); i++) {
			assertEquals("D" + (i+1), varNames.get(i));
			assertEquals(varSizes.get(i).intValue(), loader.getVariableSize(varNames.get(i)).intValue());
			assertEquals((i+2), varSizes.get(i).intValue());
		}

		// check cliques
		assertEquals(3, loader.getCliqueNames().size());
		
		assertEquals("C0", loader.getCliqueNames().get(0));
		assertEquals(2, loader.getVariablesInClique(loader.getCliqueNames().get(0)).size());
		assertEquals("D1", loader.getVariablesInClique(loader.getCliqueNames().get(0)).get(0));
		assertEquals("D2", loader.getVariablesInClique(loader.getCliqueNames().get(0)).get(1));
		
		assertEquals("C1", loader.getCliqueNames().get(1));
		assertEquals(2, loader.getVariablesInClique(loader.getCliqueNames().get(1)).size());
		assertEquals("D1", loader.getVariablesInClique(loader.getCliqueNames().get(1)).get(0));
		assertEquals("D3", loader.getVariablesInClique(loader.getCliqueNames().get(1)).get(1));
		
		assertEquals("C2", loader.getCliqueNames().get(2));
		assertEquals(2, loader.getVariablesInClique(loader.getCliqueNames().get(2)).size());
		assertEquals("D3", loader.getVariablesInClique(loader.getCliqueNames().get(2)).get(0));
		assertEquals("D4", loader.getVariablesInClique(loader.getCliqueNames().get(2)).get(1));
		
		// check separators
		assertEquals(2, loader.getSeparators().size());
		
		assertEquals("C0", loader.getSeparators().get(0).getKey());
		assertEquals("C1", loader.getSeparators().get(0).getValue());
		assertEquals(1, loader.getVariablesInSeparator(loader.getSeparators().get(0)).size());
		assertEquals("D1", loader.getVariablesInSeparator(loader.getSeparators().get(0)).get(0));
		
		assertEquals("C1", loader.getSeparators().get(1).getKey());
		assertEquals("C2", loader.getSeparators().get(1).getValue());
		assertEquals(1, loader.getVariablesInSeparator(loader.getSeparators().get(1)).size());
		assertEquals("D3", loader.getVariablesInSeparator(loader.getSeparators().get(1)).get(0));
		
		
	}
	
	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public final void testLoad() throws FileNotFoundException, IOException {
		
		String fileName = "src/test/resources/cliques.json";
		
		// instantiate the loader
		JSONCliqueStructureLoader loader = new JSONCliqueStructureLoader();
		
		// load (parse) the file
		loader.load(new FileInputStream(new File(fileName)));
		
		// start printing the content we just parsed
		
		
		// extract the names and sizes
		List<String> varNames = loader.getVariableNames();
		List<Integer> varSizes = loader.getVariablesSizes();
		// basic assertions
		assertEquals(varNames.size(),  varSizes.size());
		assertEquals(24,  varSizes.size());
		// names and sizes
		for (int i = 0; i < varNames.size(); i++) {
			assertEquals("D" + (i+1), varNames.get(i));
			assertEquals(2, loader.getVariableSize(varNames.get(i)).intValue());
			assertEquals(2, varSizes.get(i).intValue());
		}
		
		// check cliques
		assertEquals(10, loader.getCliqueNames().size());
		
		Set<String> cliqueNames = new HashSet<String>();
		
		for (int i = 0; i < loader.getCliqueNames().size(); i++) {
			cliqueNames.add(loader.getCliqueNames().get(i));
			assertEquals("C" + i, loader.getCliqueNames().get(i));
			assertTrue(loader.getVariablesInClique(loader.getCliqueNames().get(i)).size() >= 7);
			assertTrue(loader.getVariablesInClique(loader.getCliqueNames().get(i)).size() <= 15);
			// check that variables in clique were declared in the list of variables
			for (String varName : loader.getVariablesInClique(loader.getCliqueNames().get(i))) {
				assertTrue(varName + " not found in " + varNames, varNames.contains(varName));
			}
		}
		
		
		// check separators
		assertEquals(9, loader.getSeparators().size());
		
		for (int i = 0; i < loader.getSeparators().size(); i++) {
			// check if clique1 (key) and clique2 (value) are present in the list of cliques
			assertTrue(loader.getSeparators().get(i).getKey() + " not found in " + cliqueNames, cliqueNames.contains(loader.getSeparators().get(i).getKey()));
			assertTrue(loader.getSeparators().get(i).getValue() + " not found in " + cliqueNames, cliqueNames.contains(loader.getSeparators().get(i).getValue()));
			
			assertTrue(loader.getVariablesInSeparator(loader.getSeparators().get(i)).size() >= 6);
			assertTrue(loader.getVariablesInSeparator(loader.getSeparators().get(i)).size() <= 14);
			for (String varName : loader.getVariablesInSeparator(loader.getSeparators().get(i))) {
				assertTrue(varName + " not found in " + varNames, varNames.contains(varName));
			}
		}
		
		
	}

}
