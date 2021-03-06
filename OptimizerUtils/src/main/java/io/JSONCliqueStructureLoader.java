/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.IJunctionTreeBuilder;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import utils.UniformNameConverter;

/**
 * This is a default implementation of {@link ICliqueStructureLoader}
 * which assumes the input file is in JSON format.
 * It uses {@link org.json.JSONObject} to represent the clique structure.
 * <br/>
 * <br/>
 * An input file supported by this class should be in JSON (RFC7159) format. Such input file describes a tree of cliques (i.e. a junction tree).
 * It basically declares a set of variables, their sizes, set of cliques (with variables in it) and separators (i.e. intersection of cliques).
 * <br/><br/>
 * The following is an example of a JSON file supported by this class.
 * <pre>
{
	"variables": [ "D1", "D2", "D3", "D4"],
	"sizes" : [ 2, 2, 2, 3 ],
	"cliques" : [
		{
			"name" : "C0",
			"variables" : ["D1","D2"]
		},
		{
			"name" : "C1",
			"variables" : ["D1","D3"]
		},
		{
			"name" : "C2",
			"variables" : ["D3","D4"]
		}
	],
	"separators" : [
		{
			"clique1" : "C0",
			"clique2" : "C1",
			"variables" : ["D1"]
		},
		{
			"clique1" : "C1",
			"clique2" : "C2",
			"variables" : ["D3"]
		}
	]
}
 * </pre>
 * 
 * Note: this example only contains Detectors. A model only with detectors is enough for most of cases,
 * because once we get conditional probabilities P(Indicator|Detectors) and P(Threat|Indicators)
 * we can simulate (i.e. generate samples of) Detectors, then Indicators given Detectors, and Threat given Indicators to get a full set.
 * <br/><br/>
 * The *variables* block declares the names of random variables in this domain.
 * <br/><br/>
 * The *sizes* block declares how many states (i.e. possible values) each random variable has. We are considering 2 states in this simpler example.
 * The *cliques* block is a list of cliques. A clique has a name field and a list of variables that belongs to the clique.
 * Stochastic optimization can be performed in each clique separately (in parallel if we can guarantee that optimal values of intersections are unique).
 * <br/><br/>
 * The *separators* block is a list of separators (intersection of cliques).
 * A separator has a pair of cliques it separates, and a set of variables in the intersection.
 * Separators may look redundant, but they are useful to guarantee a unique tree structure of cliques.
 * <br/><br/>
 * The stochastic optimization component should consider the *separators* block only if solver cannot guarantee that optimal values of intersections are unique.
 * This block may be ignored if optimal values of intersections are unique.
 * When such values are not unique, optimization must be performed in sequence for each clique (given optimal values of cliques solved already),
 * accordingly to tree structure (separators are helpful in retrieving such tree structure).
 * <br/><br/>
 * The tree structure (thus the separators) is necessary for generating samples from the joint distribution, though.
 * 
 * 
 * @author Shou Matsumoto
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSONCliqueStructureLoader implements ICliqueStructureLoader {
	
	/** JSON key of name of variables*/
	public static final String VARIABLES_KEY =  "variables";
	/** JSON key of size of variables*/
	public static final String SIZES_KEY =  "sizes";
	/** JSON key of list of cliques*/
	public static final String CLIQUES_KEY =  "cliques";
	/** JSON key of names of cliques*/
	public static final String CLIQUES_NAME_KEY =  "name";
	/** JSON key of variables in cliques*/
	public static final String CLIQUES_VARIABLES_KEY =  VARIABLES_KEY;
	/** JSON key of list of separators*/
	public static final String SEPARATORS_KEY =  "separators";
	/** JSON key of a clique separated by a separator*/
	public static final String SEPARATORS_CLIQUE1_KEY =  "clique1";
	/** JSON key of another clique separated by a separator*/
	public static final String SEPARATORS_CLIQUE2_KEY =  "clique2";
	/** JSON key of variables in separator*/
	public static final String SEPARATORS_VARIABLES_KEY =  CLIQUES_VARIABLES_KEY;
	
	private JSONObject jsonObject = null;
	
	private IJunctionTreeBuilder junctionTreeBuilder = null;
	
	private UniformNameConverter nameConverter = null;
	
	/**
	 * Default constructor
	 */
	public JSONCliqueStructureLoader() {}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#load(java.io.InputStream)
	 */
	public void load(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String source = "";
		for (String line = ""; (line = reader.readLine()) != null;) {
			source += line + "\n";
		}
		setJSONObject(new JSONObject(source));
	}
	

	/**
	 * This is just a helper method which converts a json array to list
	 */
	protected List convertJSONArrayToList(JSONArray jsonArray) {
		if (jsonArray == null) {
			return Collections.EMPTY_LIST;
		}
		List ret = new ArrayList(jsonArray.length());
		for (Object object : jsonArray) {
			if (object != null) {
				ret.add(object);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getVariableNames()
	 */
	public List<String> getVariableNames() {
		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		// just access the field directly
		return convertJSONArrayToList(jsonObject.getJSONArray(VARIABLES_KEY));
	}


	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getVariablesSizes()
	 */
	public List<Integer> getVariablesSizes() {
		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		// just access the field directly
		return convertJSONArrayToList(jsonObject.getJSONArray(SIZES_KEY));
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getVariableSize(java.lang.String)
	 */
	public Integer getVariableSize(String variableName) {
		// find the index of variableName
		List<String> names = getVariableNames();
		int index = names.indexOf(variableName);
		
		// check if index is consistent
		List<Integer> sizes = getVariablesSizes();
		if (index < 0 || index >= sizes.size()) {
			return null;
		}
		
		// return the size at that index
		return sizes.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getCliqueNames()
	 */
	public List<String> getCliqueNames() {
		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		// obtain the array of cliques
		JSONArray cliques = jsonObject.getJSONArray(CLIQUES_KEY);
		if (cliques == null) {
			return Collections.EMPTY_LIST;
		}
		
		List<String> ret = new ArrayList<String>(cliques.length());	// the list to return
		
		// iterate on cliques and fill ret with names
		for (int i = 0; i < cliques.length(); i++) {
			JSONObject clique = cliques.getJSONObject(i);
			if (clique == null) {
				continue;
			}
			String name = clique.getString(CLIQUES_NAME_KEY);
			if (name != null) {
				ret.add(name);
			}
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getVariablesInClique(java.lang.String)
	 */
	public List<String> getVariablesInClique(String queriedName) {
		// TODO use a hash map instead of iterating on cliques
		
		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		
		// iterate on cliques
		
		JSONArray cliques = jsonObject.getJSONArray(CLIQUES_KEY);
		if (cliques == null) {
			return Collections.EMPTY_LIST;
		}
		
		// find clique with such name
		for (int i = 0; i < cliques.length(); i++) {
			JSONObject clique = cliques.getJSONObject(i);
			if (clique == null) {
				continue;
			}
			String currentCliqueName = clique.getString(CLIQUES_NAME_KEY);
			if (currentCliqueName == null) {
				continue;
			}
			if (queriedName.equals(currentCliqueName)) {
				// return the variables in the 1st clique we found
				return convertJSONArrayToList(clique.getJSONArray(CLIQUES_VARIABLES_KEY));
			}
		}
		
		// no clique with such name was found
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getSeparators()
	 */
	public List<Entry<String, String>> getSeparators() {
		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		// obtain the array of separators
		JSONArray seps = jsonObject.getJSONArray(SEPARATORS_KEY);
		if (seps == null) {
			return Collections.EMPTY_LIST;
		}
		
		List<Entry<String, String>> ret = new ArrayList<Entry<String, String>>(seps.length());	// the list to return
		
		// iterate on separators and fill ret with pairs of names
		for (int i = 0; i < seps.length(); i++) {
			try {
				JSONObject sep = seps.getJSONObject(i);
				ret.add(new AbstractMap.SimpleEntry(sep.getString(SEPARATORS_CLIQUE1_KEY), sep.getString(SEPARATORS_CLIQUE2_KEY)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getSeparatorContent(java.util.Map.Entry)
	 */
	
	public List<String> getVariablesInSeparator(Entry<String, String> query) {
		// TODO use a hash map for faster access
		if (query == null) {
			return Collections.EMPTY_LIST;
		}

		if (jsonObject == null) {
			return Collections.EMPTY_LIST;
		}
		// obtain the array of separators
		JSONArray seps = jsonObject.getJSONArray(SEPARATORS_KEY);
		if (seps == null) {
			return Collections.EMPTY_LIST;
		}
		
		// iterate on separators and fill ret with names of variables
		for (int i = 0; i < seps.length(); i++) {
			try {
				JSONObject sep = seps.getJSONObject(i);
				// if the names in the entry matches with the names in separator, this is the same separator
				if (query.getKey().equals(sep.getString(SEPARATORS_CLIQUE1_KEY))
						&& query.getValue().equals(sep.getString(SEPARATORS_CLIQUE2_KEY))) {
					return convertJSONArrayToList(sep.getJSONArray(SEPARATORS_VARIABLES_KEY));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return Collections.EMPTY_LIST;
	
	}


	/**
	 * @return the json object representing the content of clique structure.
	 */
	public JSONObject getJSONObject() {
		return jsonObject;
	}

	/**
	 * @param jsonObject : the json object representing the content of clique structure.
	 */
	public void setJSONObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	/**
	 * This is just an example of how to use {@link JSONCliqueStructureLoader}.
	 * It will print the contents that were loaded from a file
	 * @param args : must contain a name of JSON file.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		String fileName = "cliques.json";	// default place to look for json file
		if (args != null && args.length > 0) {
			fileName = args[0];	// extract name from argument
		}
		
		System.out.println("Loading JSON file: " + fileName);
		
		// instantiate the loader
		JSONCliqueStructureLoader loader = new JSONCliqueStructureLoader();
		
		// load (parse) the file
		loader.load(new FileInputStream(new File(fileName)));
		
		// start printing the content we just parsed
		
		// print names of random variables and number of possible states
		System.out.println("Variables and sizes: ");
		
		// extract the names and sizes
		List<String> varNames = loader.getVariableNames();
		List<Integer> varSizes = loader.getVariablesSizes();
		// basic assertions
		if (varNames.size() != varSizes.size()) {
			throw new IOException("Invalid file content: there are " + varNames.size() + " variables, but " 
					+ varSizes.size() + " had their sizes declared in the JSON file.");
		}
		// print the names and sizes
		for (int i = 0; i < varNames.size(); i++) {
			System.out.println("\tVariable = " + varNames.get(i) + ", size = " + varSizes.get(i));
		}

		
		
		System.out.println("\n\nCliques:");
		
		// iterate on cliques and print their content
		for (String name : loader.getCliqueNames()) {
			System.out.println("\tClique: " + name);
			System.out.print("\tVariables: \t");
			for (String varName : loader.getVariablesInClique(name)) {
				System.out.print(varName + "\t");
			}
			System.out.println("\n");
		}
		
		
		
		System.out.println("\n\nSeparators:");
		
		// iterate on separators and print their content.
		// Separators are useful if you'd like to exploit the tree structure of cliques
		for (Entry<String, String> cliquePair : loader.getSeparators()) {	
			// separators "separate" two cliques
			System.out.println("\tClique1 = " + cliquePair.getKey() + ", Clique2 = " + cliquePair.getValue());
			// print the set of variables in the separator (i.e. the variables in common between the cliques it separates)
			System.out.print("\tVariables: \t");
			for (String varName : loader.getVariablesInSeparator(cliquePair)) {
				System.out.print(varName + "\t");
			}
			System.out.println("\n");
		}
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see io.ICliqueStructureLoader#getJunctionTree()
	 */
	public IJunctionTree getJunctionTree() throws InstantiationException, IllegalAccessException {
		// create the variables
		Map<String, INode> nodeMap = new HashMap<String, INode>();
		List<String> varNames = getVariableNames();
		List<Integer> varSizes = getVariablesSizes();
		for (int varIndex = 0; varIndex < varNames.size(); varIndex++) {
			String name = getNameConverter().convertToName(varNames.get(varIndex));
			ProbabilisticNode node = new ProbabilisticNode();
			node.setName(name);
			for (int state = 0; state < varSizes.get(varIndex); state++) {
				node.appendState("" + state);
			}
			nodeMap.put(name, node);
		}
		
		// instantiate the junction tree
		IJunctionTree junctionTree = getJunctionTreeBuilder().buildJunctionTree(null);
		
		// create the cliques
		Map<String, Clique> cliqueMap = new HashMap<String, Clique>();
		for (String cliqueName : getCliqueNames()) {
			Clique clique = new Clique();
			int cliqueNum = getCliqueNumber(cliqueName);
			clique.setInternalIdentificator(cliqueNum);
			for (String varName : getVariablesInClique(cliqueName)) {
				INode node = nodeMap.get(varName);
				clique.getNodesList().add((Node) node);
			}
			// add variables to clique tables as well
			for (int varIndex = clique.getNodesList().size()-1; varIndex >= 0; varIndex--) {	// last variable is the one which iterates most, so needs to be added 1st
				clique.getProbabilityFunction().addVariable(clique.getNodesList().get(varIndex));
			}
			// also init clique tables (fill tables with variables)
			clique.getProbabilityFunction().fillTable(1f);
			junctionTree.getCliques().add(clique );
			cliqueMap.put(cliqueName, clique);
		}
		
		
		// create the separators
		List<Entry<String, String>> separators = getSeparators();
		for (int sepIndex = 0; sepIndex < separators.size(); sepIndex++) {
			Entry<String, String> entry = separators.get(sepIndex);
			
			Clique clique1 = cliqueMap.get(entry.getKey());
			Clique clique2 = cliqueMap.get(entry.getValue());
			
			Separator sep = new Separator(clique1, clique2);	// this should update clique1.getChildren() and clique2.getParent()
			sep.setInternalIdentificator(-(sepIndex+1));	// set internal ID unique and different from cliques
			
			sep.setNodes(new ArrayList<Node>(getVariablesInSeparator(entry).size()));	// make sure the list is initialized
			for (String varName : getVariablesInSeparator(entry)) {
				INode node = nodeMap.get(varName);
				sep.getNodesList().add((Node) node);
			}
			// add variables to separator tables as well
			for (int varIndex = sep.getNodesList().size()-1; varIndex >= 0; varIndex--) { // last variable is the one which iterates most, so needs to be added 1st
				sep.getProbabilityFunction().addVariable(sep.getNodesList().get(varIndex));
			}
			// also init clique tables (fill tables with variables)
			sep.getProbabilityFunction().fillTable(1f);
			junctionTree.addSeparator(sep);
		}
		
		// make sure internal identificators are up to date
//		updateCliqueAndSeparatorInternalIdentificators(junctionTree);
		
		return junctionTree;
	}
	

//	/**
//     * This method moves the root clique to 1st index, and then updates {@link IRandomVariable#getInternalIdentificator()}
//     * accordingly to its order of appearance in {@link IJunctionTree#getCliques()} and {@link IJunctionTree#getSeparators()}.
//     * This is necessary because some implementations assumes that {@link IRandomVariable#getInternalIdentificator()} is synchronized with indexes.
//	 * @param junctionTree 
//	 */
//	public void updateCliqueAndSeparatorInternalIdentificators(IJunctionTree junctionTree) {
//    	if (junctionTree.getCliques() != null && !junctionTree.getCliques().isEmpty()) {
//    		// move the new root clique to the 1st entry in the list of cliques in junction tree, because some algorithms assume the 1st element is the root;
//    		Clique root = junctionTree.getCliques().get(0);
//    		while (root.getParent() != null) { 
//    			root = root.getParent(); // go up in hierarchy until we find the root
//    		}
//    		int indexOfRoot = junctionTree.getCliques().indexOf(root);
//    		if (indexOfRoot > 0) {
//    			// move root to the beginning (index 0) of the list
//    			Collections.swap(junctionTree.getCliques(), 0, indexOfRoot);
//    		}
//    		
//    		// redistribute internal identifications accordingly to indexes
//    		for (int i = 0; i < junctionTree.getCliques().size(); i++) {
//    			junctionTree.getCliques().get(i).setIndex(i);
//    			junctionTree.getCliques().get(i).setInternalIdentificator(i);
//    		}
//    	}
//    	// do the same for separators
//    	int separatorIndex = -1;
//    	for (Separator sep : junctionTree.getSeparators()) {
//    		sep.setInternalIdentificator(separatorIndex--);
//    	}
//	}

	/**
	 * Extracts the clique number (id) from clique name
	 * @param cliqueName
	 * @return
	 */
	public int getCliqueNumber(String cliqueName) {
		cliqueName  = cliqueName.toUpperCase();
		cliqueName = cliqueName.replaceAll("[a-zA-Z-_\\.]", " ");
		for (String split : cliqueName.split("\\s")) {
			if (split != null && split.trim().length() > 0) {
				return Integer.parseInt(split);
			}
		}
		throw new IllegalArgumentException("Could not parse clique name " + cliqueName);
	}

	/**
	 * @return the junctionTreeBuilder
	 */
	public IJunctionTreeBuilder getJunctionTreeBuilder() {
		if (junctionTreeBuilder == null) {
			junctionTreeBuilder = new DefaultJunctionTreeBuilder();
		}
		return junctionTreeBuilder;
	}

	/**
	 * @param junctionTreeBuilder the junctionTreeBuilder to set
	 */
	public void setJunctionTreeBuilder(IJunctionTreeBuilder junctionTreeBuilder) {
		this.junctionTreeBuilder = junctionTreeBuilder;
	}

	/**
	 * @return the nameConverter
	 */
	public UniformNameConverter getNameConverter() {
		if (nameConverter == null) {
			nameConverter = new UniformNameConverter();
		}
		return nameConverter;
	}

	/**
	 * @param nameConverter the nameConverter to set
	 */
	public void setNameConverter(UniformNameConverter nameConverter) {
		this.nameConverter = nameConverter;
	}
	
}
