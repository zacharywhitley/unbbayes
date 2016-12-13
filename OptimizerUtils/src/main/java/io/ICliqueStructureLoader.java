/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.JunctionTree;

/**
 * This is a common interface for classes that loads/saves streams which represent clique structures (e.g. a junction tree of cliques).
 * A clique is a set of random variables.
 * @author Shou Matsumoto
 */
public interface ICliqueStructureLoader {

	/**
	 * Loads a clique structure from an input stream (an input stream can be a file).
	 * This method must be called before accessing the getter methods.
	 * @param input : input stream be loaded/parsed.
	 */
	public void load(InputStream input) throws IOException;
	
	
	/**
	 * @return : a list of names of random variables loaded in {@link #load(InputStream)}.
	 * @see #getVariableSize(String)
	 * @see #getVariablesSizes()
	 */
	public List<String> getVariableNames();
	
	/**
	 * @return : list of sizes (i.e. number of possible states) of random variables in {@link #getVariableNames()}
	 * @see #getVariableSize(String)
	 */
	public List<Integer> getVariablesSizes();
	
	/**
	 * This facilitates {@link #getVariablesSize(String)} if a name is already known.
	 * @param variableName : name of random variable
	 * @return size (i.e. number of possible states) of a random variable
	 * @see #getVariableNames()
	 * @see #getVariablesSizes()
	 * @see #load(InputStream)
	 */
	public Integer getVariableSize(String variableName);
	
	/**
	 * @return a list of names of cliques.
	 * {@link #getVariablesInClique(String)} shall be called in order to extract its contents.
	 * @see #load(InputStream)
	 * @see #getVariablesInClique(String)
	 */
	public List<String> getCliqueNames();
	
	/**
	 * Obtains the set of variables that belongs to the clique.
	 * @param cliqueName : name of the clique
	 * @return : list of names of variables in clique.
	 * @see #getCliqueNames()
	 * @see #getVariableNames()
	 */
	public List<String> getVariablesInClique(String cliqueName);
	
	/**
	 * @return : a list of pair of names of cliques. A separator represents an intersection of two cliques.
	 * {@link #getVariablesInSeparator(Entry)} shall be called in order to extract its contents.
	 */
	public List<Entry<String, String>> getSeparators();
	
	/**
	 * @param separator : separator to be considered.
	 * @return list of names of variables that belongs to this separator.
	 * @see #getSeparators()
	 * @see #getVariableNames()
	 */
	public List<String> getVariablesInSeparator(Entry<String, String> separator);


	/**
	 * Converts the information read in {@link #load(InputStream)} to a junction tree
	 * @return instance of {@link JunctionTree}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public IJunctionTree getJunctionTree() throws InstantiationException, IllegalAccessException;
	
}
