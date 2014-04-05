/**
 * 
 */
package edu.gmu.ace.scicast.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;

/**
 * Converts a graph to a space-separated matrix of 0 and 1.
 * Each row/column represent a node in BN.
 * If the cell in row r and column c is 1, then there is an arc from node r to node c.
 * For instance, the network 0<-1->2 is represented as:
 * <br/>
 * <br/>
 * 0 0 0 <br/>
 * 1 0 1 <br/>
 * 0 0 0 <br/>
 * <br/>
 * This matrix is used by the matlab implementation of markov engine.
 * @author Shou Matsumoto
 *
 */
public class BNToChildrenMatrixConverter implements BaseIO {

	/** Contains only "txt" */
	public static final String[] EXTENSIONS = {"txt"};
	
	private String name = "Matrix of children (for Matlab)";

	/**
	 * Converts a graph to a space-separated matrix of 0 and 1.
	 * Each row/column represent a node in BN.
	 * If the cell in row r and column c is 1, then there is an arc from node r to node c.
	 * For instance, the network 0<-1->2 is represented as:
	 * <br/>
	 * <br/>
	 * 0 0 0 <br/>
	 * 1 0 1 <br/>
	 * 0 0 0 <br/>
	 * <br/>
	 * This matrix is used by the matlab implementation of markov engine.
	 */
	public BNToChildrenMatrixConverter() {
		// TODO Auto-generated constructor stub
	}
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		throw new UnsupportedOperationException("Loading is not supported yet");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File file, Graph net) throws IOException {
		
		// open stream in order to write file
		PrintStream printStream = new PrintStream(new FileOutputStream(file));
		
		// simply fill with 1 or 0, separated by space
		for (Node parent : net.getNodes()) {
			for (Node child : net.getNodes()) {
				printStream.print((isAdjacent(parent, child)?"1":"0") + " ");
			}
			printStream.println();
		}
	
		// flush stream
		printStream.close();
	}
	
	/**
	 * @param parent
	 * @param child
	 * @return true if the node "parent" is the parent of the node "child". False otherwise.
	 */
	public boolean isAdjacent(INode parent, INode child) {
		return child.getParentNodes().contains(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		return !isLoadOnly && (!file.isDirectory() && file.getName().lastIndexOf("."+getSupportedFileExtensions(false)[0]) > 0);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		if (isLoadOnly) {
			return new String[0];
		}
		return EXTENSIONS;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return ".txt (BN matrix)";
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getName()
	 */
	public String getName() {
		return name ;
	}

	

}
