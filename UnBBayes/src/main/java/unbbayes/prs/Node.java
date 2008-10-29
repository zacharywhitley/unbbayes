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
package unbbayes.prs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unbbayes.draw.DrawElement;
import unbbayes.draw.DrawText;
import unbbayes.draw.IOnePositionDrawable;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.util.ArrayMap;
import unbbayes.util.SerializablePoint2D;

/**
 * A class representing a generic node.
 * @author Michael e Rommel
 */
public abstract class Node implements Serializable, IOnePositionDrawable {

	private String description;
	protected String name;
	protected String label;
	private boolean nameIsLabel = true;


	private static final Point DEFAULT_SIZE = new Point(35,35); 
	
	protected SerializablePoint2D position;
	protected static SerializablePoint2D size = new SerializablePoint2D(DEFAULT_SIZE.getX(), DEFAULT_SIZE.getY());

	protected SerializablePoint2D sizeVariable = new SerializablePoint2D();
	protected boolean sizeIsVariable = false;

	protected ArrayList<Node> parents;
	private ArrayList<Node> children;
	protected List<String> states;
	private ArrayList<Node> adjacents;
	private boolean bSelected;
	private String explanationDescription;
	private ArrayMap<String, ExplanationPhrase> phrasesMap;
	private int informationType;
	public int infoestados[];
	

	public static final int PROBABILISTIC_NODE_TYPE = 0;
	public static final int UTILITY_NODE_TYPE = 1;
	public static final int DECISION_NODE_TYPE = 2;

	public static final int DESCRIPTION_TYPE = 3;
	public static final int EXPLANATION_TYPE = 4;
	
	public static final int CONTINUOUS_NODE_TYPE = 5;

	protected DrawElement drawElement;
	
	/**
	 * Holds the mean of the values for each class if this is a numeric
	 * attribute node
	 */
	protected double[] mean;

	/**
	 * Holds the standard deviation of the values for each class if this is a
	 * numeric attribute node.
	 */
	protected double[] standardDeviation;

	/**
	 * Builds a new node and makes the expected
	 * initializations.
	 */
	public Node() {
		name = "";
		label = ""; // text inside node
		description = "";
		explanationDescription = "";
		adjacents = new ArrayList<Node>();
		parents = new ArrayList<Node>();
		children = new ArrayList<Node>();
		states = new ArrayList<String>();

		// width
		size.x = DEFAULT_SIZE.getX();
		// height
		size.y = DEFAULT_SIZE.getY();

		position = new SerializablePoint2D();
		bSelected = false;
		drawElement = new DrawText(name, position);
		drawElement.setFillColor(Color.black);
		phrasesMap = new ArrayMap<String, ExplanationPhrase>();
		informationType = DESCRIPTION_TYPE;
	}

	public static Point getDefaultSize(){
		return DEFAULT_SIZE;
	}
	
	public abstract int getType();

	/**
	 * Returns the type of information of this node.
	 * 
	 * @return Type of the information.
	 */
	public int getInformationType() {
		return informationType;
	}

	/**
	 * Modify the node's type of information.
	 * The types can be:
	 * 		DESCRIPTION_TYPE: for description nodes.
	 * 		EXPLANATION_TYPE: for explanation nodes.
	 * @param informationType
	 *            type of information
	 * @throws Exception
	 *            if the type of information is invalid
	 */
	public void setInformationType(int informationType) /* throws Exception */{
		if ((informationType > 2) && (informationType < 5))
			this.informationType = informationType;
		/*
		 * else { throw new Exception("Valor de infroma��o inv�lido"); }
		 */
	}

	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		phrasesMap.put(explanationPhrase.getNode(), explanationPhrase);
	}

	public ExplanationPhrase getExplanationPhrase(String node) throws Exception {
		ExplanationPhrase ep = phrasesMap.get(node);
		if (ep == null) {
			throw new Exception("N� n�o encontrado.");
		} else {
			return (ExplanationPhrase) ep;
		}
	}

	/**
	 * Changes node's description.
	 * 
	 * @param texto
	 *            node's description.
	 */
	public void setDescription(String texto) {
		this.description = texto;
	}

	/**
	 * Set the node's name.
	 * 
	 * @param name
	 *            Node's name.
	 */
	public void setName(String name) {
		this.name = name;
		if (nameIsLabel == true) {
			((DrawText) drawElement).setText(name);
		}
	}

	/**
	 * Set the node's label (text of the node).
	 * 
	 * @param label
	 *            Node's label.
	 */
	public void setLabel(String label) {
		this.label = label;
		nameIsLabel = false;
		((DrawText) drawElement).setText(label);
	}

	/**
	 * Return the node's label (text of the node).
	 * 
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets a new list of children.
	 * @param children
	 * 			List of nodes representing the children.
	 */
	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}

	/**
	 * Sets a new list of parents.
	 * @param parents
	 * 			List of nodes representing the parents.
	 */
	public void setParents(ArrayList<Node> parents) {
		this.parents = parents;
	}
	
	public void addChild(Node filho){
		this.children.add(filho);
	}
	
	public void addParent(Node parent){
		this.parents.add(parent);
	}

	public boolean isParentOf(Node child) {
		// boolean result=children.contains(child);
		boolean result = false;
		int j = children.size();
		try {
			for (int i = 0; i < j; i++) {
				result = ((result) || ((child.getName()) == (children.get(i)
						.getName())));
			}
		} catch (Exception ee) {
			int debug = 0;
			int debug2 = debug;
		}
		return result;
	}

	public boolean isChildOf(Node parent) {
		// boolean result=parents.contains(parent);
		boolean result = false;
		int j = parents.size();
		for (int i = 0; i < j; i++) {
			result = ((result) || ((parent.getName()) == (parents.get(i)
					.getName())));
		}
		return result;
	}

	/**
	 * Modifica a descri��o da explana��o do n�.
	 * Modifies the description of the explanation of the node.
	 * @param texto
	 * 			A text representing the node's explanation's description.
	 *            descri��o da explana��o do n�.
	 */
	public void setExplanationDescription(String text) {
		this.explanationDescription = text;
	}

	/**
	 * Modifies the ArrayMap with the phrases
	 * @param phrasesMap
	 *            a new ArrayMap to be set
	 * @return the old phrasesMap.
	 */
	public ArrayMap<String, ExplanationPhrase> setPhrasesMap(
			ArrayMap<String, ExplanationPhrase> phrasesMap) {
		ArrayMap<String, ExplanationPhrase> old = this.phrasesMap;
		this.phrasesMap = phrasesMap;
		return old;
	}

	/**
	 * Obtains the name of this node.
	 * @return node's description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Obtains a list of adjacents.
	 * @return reference for this node's adjacents.
	 */
	public ArrayList<Node> getAdjacents() {
		return adjacents;
	}

	/**
	 * Returns the node's literal symbol.
	 * 
	 * @return node's literal abbreviation.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Obtains a list of children.
	 * @return list of children.
	 */
	public final ArrayList<Node> getChildren() {
		return children;
	}

	/**
	 * Obtains a list of parents.
	 * @return list of parents.
	 */
	public final ArrayList<Node> getParents() {
		return parents;
	}

	/**
	 * Obtains the description of the explanation of the node.
	 * @return description of the explanation of the node
	 */
	public String getExplanationDescription() {
		return explanationDescription;
	}

	/**
	 * Obtains the ArrayMap with the phrases.
	 * @return ArrayMap with the phrases.
	 */
	public ArrayMap<String, ExplanationPhrase> getPhrasesMap() {
		return this.phrasesMap;
	}

	/**
	 * Used within dalgo2
	 */
	public void atualizatamanhoinfoestados() {
		int i = states.size();
		infoestados = new int[i];

		/*
		 * nao precisa, pois o array eh criado sempre com o valor 0 for (int j =
		 * 0; j < i; j++) infoestados[j] = 0;
		 */
	}

	/**
	 * Inserts a state with the specified name at the end of the list.
	 * @param state
	 *            Name of the state to be added.
	 */
	public void appendState(String state) {
		updateTables();
		states.add(state);
	}

	/**
	 * Deletes the node's last inserted state (i.e the last element inside the list of states).
	 */
	public void removeLastState() {
		if (states.size() > 1) {
			updateTables();
			states.remove(states.size() - 1);
		}
	}

	/**
	 * Used within dalgo2. It should not be used with nodes having potential table's informations.
	 */
	public void removeStateAt(int index) {
		states.remove(index);
		this.atualizatamanhoinfoestados();
	}

	/**
	 * Replaces a state at given position to the specified position.
	 * @param state
	 * 				Name of the new state.
	 * @param index
	 * 				Position of the state being substituted. Starts with 0.
	 */
	public void setStateAt(String state, int index) {
		states.set(index, state);
	}

	/*
	 * public boolean existState(String state) { int size = states.size(); for
	 * (int i=0; i<size; i++) { if (states.get(i).equals(state)) return true; }
	 * return false; }
	 */

	/**
	 * It returns the node's quantity of states.
	 * @return How many states the node has.
	 */
	public final int getStatesSize() {
		return states.size();
	}

	/**
	 * Returns the state of the position given by <code>index</code>
	 * @param index
	 *            position of the state to be read.
	 * @return Name of the state at <code>index</code>
	 */
	public final String getStateAt(int index) {
		return (String) (states.get(index));
	}

	/**
	 * Builds the list of adjacent nodes.
	 * (the parents and children of this node)
	 */
	public void makeAdjacents() {
		adjacents.addAll(parents);
		adjacents.addAll(children);
	}

	/**
	 * Clears the list of adjacent nodes.
	 */
	public void clearAdjacents() {
		adjacents.clear();
	}

	/**
	 * This should be used to notify the tables which this variable is part of that
	 * there were some modification at this variable's internal structure.
	 */
	private void updateTables() {
		ITabledVariable aux;
		if (this instanceof ITabledVariable) {
			aux = (ITabledVariable) this;
			aux.getPotentialTable().variableModified();
		}

		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i) instanceof ITabledVariable) {
				aux = (ITabledVariable) children.get(i);
				aux.getPotentialTable().variableModified();
			}
		}
	}

	/**
	 * Sets the adjacents.
	 * 
	 * @param adjacents
	 *            The adjacents to set
	 */
	public void setAdjacents(ArrayList<Node> adjacents) {
		this.adjacents = adjacents;
	}

	/**
	 * Sets the states.
	 * 
	 * @param states
	 *            The states to set
	 */
	public void setStates(List<String> states) {
		this.states = states;
	}

	/**
	 * Prints a description of the node using "description (name)" format (without
	 * the quotes). It is used by the Interface's JTree when net is compiled.
	 * 
	 * @return formatted node description.
	 */
	public String toString() {
		return description + " (" + name + ")";
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		
		if((obj != null)&&(obj instanceof Node)){
		   Node node = (Node) obj;
		   return (node.name.equals(this.name));
		}
		
		return false; //obj == null && this != null 
		
	}

	public void paint(Graphics2D graphics) {
		drawElement.paint(graphics);
	}

	public boolean isPointInDrawableArea(int x, int y) {
		double x1 = position.x;
		double y1 = position.y;
		double width = size.x / 2;
		double height = size.y / 2;

		if ((x >= x1 - width) && (x <= x1 + width) && (y >= y1 - height)
				&& (y <= y1 + height)) {
			return true;
		}

		return false;
	}

	public boolean isSelected() {
		return bSelected;
	}

	public void setSelected(boolean b) {
		bSelected = b;
	}

	public Point2D.Double getPosition() {
		return position;
	}

	public void setPosition(double x, double y) {
		position.setLocation(x, y);
	}

	/**
	 * Get the node's width.
	 * 
	 * @return Node's width.
	 */
	public static int getWidth() {
		return (int) size.x;
	}

	/**
	 * Get the node's height.
	 * 
	 * @return The node's height.
	 */
	public static int getHeight() {
		return (int) size.y;
	}

	/**
	 * Returns the node's size (x,y) where x = width and y = height.
	 * 
	 * @return The node's size.
	 */
	public static Point2D.Double getSize() {

		return size;

	}

	/**
	 * Set the node's size.
	 * 
	 * @param width
	 *            The node's width.
	 * @param height
	 *            The node's height.
	 */
	public static void setSize(double width, double height) {
		size.setLocation(width, height);
	}

	public void setSizeVariable(double width, double height) {
		sizeVariable.setLocation(width, height);
	}

	public void setSizeIsVariable(boolean is) {
		sizeIsVariable = is;
	}

	/**
	 * Set the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public void setMean(double[] mean) {
		this.mean = mean;
	}

	/**
	 * Set the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public void setStandardDeviation(double[] standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	/**
	 * Get the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public double[] getMean() {
		return mean;
	}

	/**
	 * Get the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public double[] getStandardDeviation() {
		return standardDeviation;
	}

}
