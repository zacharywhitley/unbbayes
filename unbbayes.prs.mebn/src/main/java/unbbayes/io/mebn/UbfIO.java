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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.io.exception.LoadException;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.exception.ObjectEntityHasInstancesException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.util.Debug;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Ubf file format manipulator. </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto (cardialfly@[yahoo|gmail].com)
 * @version 0.2
 * @since 01/05/2007 (version 0.1)
 * @since 12/08/2010 (version 0.2) - reordering some in-memory MEBN elements (e.g. node's possible values, MFrags, nodes) 
 * respecting the order declared within the original UBF file.
 * @see unbbayes.io.mebn.IoUbfResources
 */
public class UbfIO implements MebnIO {
	
	private static final String  PROWL_EXTENSION = "owl";
		
	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.mebn.resources.IoUbfResources.class.getName());	
	
	private MebnIO prowlIO = null;	// stores files w/ .owl extension
		
	
	private String[][] tokens = { // tokens used for .ubf file construction
			{"CommentInitiator","%"},
			{"ArgumentSeparator",","},
			{"AttributionSeparator","="},
			{"Quote","\""},
			
			{"VersionDeclarator" , "Version"},
			{"PrOwlFileDeclarator" , "PrOwl"},
			
			{"MTheoryDeclarator" , "MTheory"},
			{"NextMFragDeclarator" , "NextMFrag"},
			{"NextResidentDeclarator" , "NextResident"},
			{"NextInputDeclarator" , "NextInput"},
			{"NextContextDeclarator" , "NextContext"},
			{"NextEntityDeclarator" , "NextEntity"},	
			
			{"MFragDeclarator" , "MFrag"},	
			
			{"NextOrdinalVarDeclarator" , "NextOrdinaryVar"},	
			
			{"NodeDeclarator" , "Node"},	
			{"TypeDeclarator" , "Type"},
			{"PositionDeclarator" , "Position"},	
			{"SizeDeclarator" , "Size"},	
			{"ColorDeclarator" , "Color"},
			
			{"NextArgumentDeclarator" , "NextArgument"},
			
			{"DomainResidentType" , "DomainResidentNode"},
			{"GenerativeInputType" , "GenerativeInputNode"},
			{"ContextType" , "ContextNode"},
			{"OrdinalVarType" , "OrdinalVar"},
			
			{"ObjectEntityDeclarator" , "ObjEntity"},
			{"EntityInstancesDeclarator" , "Instances"},
			{"PossibleValuesOrder" , "PossibleValuesOrder"},
	};
	
	
	public static final double ubfVersion = 0.04;
	
	public static final String FILE_EXTENSION = "ubf";
	
	/** Supported file extensions (with no dot) at loading time */
	public static final String[] SUPPORTED_EXTENSIONS_LOAD = {FILE_EXTENSION, PROWL_EXTENSION};
	
	/** Supported file extensions for loading and savin time (no dots) */
	public static final String[] SUPPORTED_EXTENSIONS = {FILE_EXTENSION};

	private String name = "UnBBayes Format for MEBN";
	
	private boolean toUseOrderOfUBFFile = true;
	
	/**
	 * This default constructor is made public for plugin support.
	 * @deprecated use {@link #getInstance()} instead.
	 */
	public UbfIO() {
		super();
		this.prowlIO = new PrOwlIO();
		//Debug.setDebug(true);
	}		
	/**
	 * Construction method for UbfIO
	 * @return UbfIO instance
	 */
	public static UbfIO getInstance() {
		return new UbfIO();
	}
	
	
	
	private MFrag searchMFrag(String name , MultiEntityBayesianNetwork mebn) {
		//System.out.println("- searching for MFrag ");
		for (Iterator iter = mebn.getDomainMFragList().iterator(); iter.hasNext();) {
			MFrag element = (MFrag) iter.next();
			//System.out.println("- searching MFrag " + name + " on " + element.getName());
			if (name.equals(element.getName())) {
				return element;
			}
		}		
		return null;
	}
	
	
	private Node searchNode(String name , MFrag mfrag) {
		for (Iterator iter = mfrag.getResidentNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.equals(element.getName())) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getInputNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.equals(element.getName())) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getContextNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.equals(element.getName())) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getOrdinaryVariableList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.equals(element.getName()) ) {
				return element;
			}
		}
		return null;
	}
	
	
	private double readVersion (StreamTokenizer st) throws IOException {
		while (st.nextToken() != st.TT_EOF) {
			if ( ( st.ttype == st.TT_WORD ) 
					&& ( this.getToken("VersionDeclarator").equals(st.sval)) ) {
				if (st.nextToken() == st.TT_NUMBER) {
					return st.nval;
				}  else {
					break;
				}
			}
		}
		throw new IOException(resource.getString("InvalidSyntax"));
	}
	
	private String readOwlFile (StreamTokenizer st) throws IOException {
		while (st.nextToken() != st.TT_EOF) {
			if ( ( st.ttype == st.TT_WORD ) 
					&& ( this.getToken("PrOwlFileDeclarator").equals(st.sval)) ) {
				if (st.nextToken() != st.TT_EOL) {
					return st.sval;
				}  else {
					break;
				}
			}
		}
		throw new IOException(resource.getString("InvalidSyntax"));
	}
	
	
	private void updateMTheory(StreamTokenizer st, MultiEntityBayesianNetwork mebn ) 
						throws IOException {
		
		//System.out.println("Updating MTheory");
		
		while (st.nextToken() != st.TT_EOF) {
			
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			
			// finishing condition
			if (this.getToken("MFragDeclarator").equals(st.sval)  ) {
				st.pushBack();
				break;
			}
			// finishing condition
			if (this.getToken("ObjectEntityDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("MTheoryDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					/*if (st.ttype == st.TT_WORD) {
						mebn.setName(st.sval);
						//System.out.println("Setting mtheory name to " + st.sval);
						break;
					}*/
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextMFragDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mebn.setDomainMFragNum((int)st.nval);
						//System.out.println("Setting mfrag number to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextResidentDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mebn.setDomainResidentNodeNum((int)st.nval);
						//System.out.println("Setting NextResidentDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextInputDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mebn.setGenerativeInputNodeNum((int)st.nval);
						//System.out.println("Setting NextInputDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextContextDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mebn.setContextNodeNum((int)st.nval);
						//System.out.println("Setting NextContextDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextEntityDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mebn.setEntityNum((int)st.nval);
						//System.out.println("Setting NextEntityDeclarator to " + st.nval);
						break;
					}
				}
			} 
			
		} // while not EOF
		
			
	} // end of method
	
	
	private void updateMFrag(StreamTokenizer st, MultiEntityBayesianNetwork mebn ) 
				throws IOException, ClassCastException {

		MFrag mfrag = null;
		
		//System.out.println("Updating MFrag");
		
		
		// this orderingIndex will be used to reorder the mebn elements according to the appearance order in UBF file
		int mfragOrderingIndex = 0; 
		while (st.nextToken() != st.TT_EOF) {
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			
			// finishing condition
			if (this.getToken("ObjectEntityDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			// determine considered mfrag
			if ( this.getToken("MFragDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						mfrag = this.searchMFrag(st.sval,mebn);
						//System.out.println("Updating mfrag " + mfrag.getName());
						break;
					}
				}
				if (mfrag == null) {
					//System.out.println("MFrag still not found");
					continue;
				} else {
					if (this.isToUseOrderOfUBFFile()) {
						// put mfrag to the expected position (orderingIndex)
						// Note: this code expects that changes on mebn.getMFragList() will influence the order of MFrags in mebn (i.e. getMFragList does not return a copy of the actual list)
						try {
							Collections.swap(mebn.getMFragList(), mfragOrderingIndex, mebn.getMFragList().indexOf(mfrag));
							Debug.println(this.getClass(), 
									"Swapped MFrag \"" + mfrag.getName() 
									+ "\" to the position " + mfragOrderingIndex);
							mfragOrderingIndex++;
						} catch (Exception e) {
							Debug.println(this.getClass(), 
									"Could not place MFrag \"" + mfrag.getName() 
									+ "\" in the position " + mfragOrderingIndex + ".", e);
						}
					}
				}
			}
			
			// by now, mfrag != null
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// Set NextOrdinalVarDeclarator
			if ( this.getToken("NextOrdinalVarDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						mfrag.setOrdinaryVariableNum((int)st.nval);
						//System.out.println("Setting OV to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// treats nodes
			if ( this.getToken("NodeDeclarator").equals(st.sval)  )  {
				st.pushBack();
				//System.out.println("Node declaration found ");
				
				try  {
					this.updateNode(st,(MFrag)mfrag);
				} catch (ClassCastException e) {
					throw new ClassCastException(resource.getString("MFragTypeException"));
				}
				
			}
			
			
		} // while not EOF
	}
	
	private void updateObjectEntities(StreamTokenizer st, MultiEntityBayesianNetwork mebn ) 
	throws IOException, ClassCastException {
		
		ObjectEntity objectEntity = null;
		
		/*
		 * Since before setting an objectEntity w/ order we must remove all instances of it,
		 * but we need to keep track of what instances were declared within OWL file, then we
		 * just save their names and clear them when order shall be set 
		 */
		List<String> owlDeclaredInstanceNames = null;
		while (st.nextToken() != st.TT_EOF) {
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// determine considered mfrag
			if ( this.getToken("ObjectEntityDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						objectEntity = mebn.getObjectEntityContainer().getObjectEntityByName(st.sval);
						// initiate tracking of entity instances
						owlDeclaredInstanceNames = new ArrayList<String>();
						//System.out.println("Updating mfrag " + mfrag.getName());
						break;
					}
				}
			}
			
			
			if (objectEntity == null) {
				//System.out.println("ObjectEntity still not found");
				continue;
			}else{
				// store already declared instances
				for (ObjectEntityInstance entityInstance : objectEntity.getInstanceList()) {
					owlDeclaredInstanceNames.add(entityInstance.getName());
				}
				// clear instances in order to let it have order
				mebn.getObjectEntityContainer().clearAllInstances(objectEntity);
				try {
					objectEntity.setOrdereable(true);
				} catch (ObjectEntityHasInstancesException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// Set NextOrdinalVarDeclarator
			if ( this.getToken("EntityInstancesDeclarator").equals(st.sval) )  {
				ObjectEntityInstanceOrdereable prev = null; 
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						String name = st.sval;
						if (!owlDeclaredInstanceNames.contains(name)) {
							// we should only add the instances also declared previously in OWL file
							continue;
						}
						try {
							ObjectEntityInstanceOrdereable oe = (ObjectEntityInstanceOrdereable)objectEntity.addInstance(name); 
							mebn.getObjectEntityContainer().addEntityInstance(oe);
							oe.setPrev(prev);
							if(prev!=null) prev.setProc(oe);
							prev = oe; 
						} catch (TypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch(Exception ex){
							ex.printStackTrace(); 
						}
					}
				}
			}
			
		} // while not EOF
	}
	
	private void updateNode(StreamTokenizer st, MFrag mfrag) throws IOException {
		
		Node node = null;
		//System.out.println("Updating Nodes");
		
		// the following indexes are used to reorder the nodes within the mfrag
		int residentNodeIndex = 0;
		int inputNodeIndex = 0;
		int contextNodeIndex = 0;
		int ordinaryVariableIndex = 0;
		while (st.nextToken() != st.TT_EOF ) {
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			//	finishing condition
			if (this.getToken("MFragDeclarator").equals(st.sval) ) {
				st.pushBack();
				//System.out.println("Next MFrag Found");
				
				break;
			}
			
			// finishing condition
			if (this.getToken("ObjectEntityDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// determine considered node
			if ( this.getToken("NodeDeclarator").equals(st.sval) )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						node = this.searchNode(st.sval,mfrag);
						//System.out.println("Setting Node: " + node.getName());
						break;
					}
				}
				if (node == null) {
					continue;
				} else {
					// reorder node by its type
					if (this.isToUseOrderOfUBFFile()) {
						if (node instanceof IResidentNode) {
							try {
								Collections.swap(mfrag.getResidentNodeList(), residentNodeIndex, mfrag.getResidentNodeList().indexOf(node));
								Debug.println(this.getClass(), "Swapping resident node " + node.getName() + " to index " + residentNodeIndex);
								residentNodeIndex++; // we should update index only on success.
							} catch (Exception e) {
								Debug.println(this.getClass(), "Could not reorder resident node " + node.getName() + " to index " + residentNodeIndex, e);
							}
						} else if (node instanceof InputNode) {
							try {
								Collections.swap(mfrag.getInputNodeList(), inputNodeIndex, mfrag.getInputNodeList().indexOf(node));
								Debug.println(this.getClass(), "Swapping input node " + node.getName() + " to index " + inputNodeIndex);
								inputNodeIndex++; // we should update index only on success.
							} catch (Exception e) {
								Debug.println(this.getClass(), "Could not reorder input node " + node.getName() + " to index " + inputNodeIndex, e);
							}
						} else if (node instanceof ContextNode) {
							try {
								Collections.swap(mfrag.getContextNodeList(), contextNodeIndex, mfrag.getContextNodeList().indexOf(node));
								Debug.println(this.getClass(), "Swapping context node " + node.getName() + " to index " + contextNodeIndex);
								contextNodeIndex++; // we should update index only on success.
							} catch (Exception e) {
								Debug.println(this.getClass(), "Could not reorder context node " + node.getName() + " to index " + contextNodeIndex, e);
							}
						} else if (node instanceof OrdinaryVariable){
							try {
								Collections.swap(mfrag.getOrdinaryVariableList(), ordinaryVariableIndex, mfrag.getOrdinaryVariableList().indexOf(node));
								Debug.println(this.getClass(), "Swapping ordinary variable " + node.getName() + " to index " + ordinaryVariableIndex);
								ordinaryVariableIndex++; // we should update index only on success.
							} catch (Exception e) {
								Debug.println(this.getClass(), "Could not reorder ordinary variable " + node.getName() + " to index " + ordinaryVariableIndex, e);
							}
						} else {
							Debug.println(this.getClass(), "The type of node is unknown: " + node.getClass().getName());
						}
					}
				}
			}
			
			
			// by now, node != null
			
			
			
			// TODO verify node type using TypeDeclarator
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// Set node position
			if ( this.getToken("PositionDeclarator").equals(st.sval)  )  {
				int posx = 15;
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						posx = (int)st.nval;
						while (st.nextToken() != st.TT_EOL) {
							if (st.ttype == st.TT_NUMBER) {
								node.setPosition(posx,(int)st.nval);
								//System.out.println("Setting node pos:" + posx + "," + st.nval);
								break;
							}
						}
						break;
					}
				}
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
//			 Set node size (height,width)
			if ( this.getToken("SizeDeclarator").equals(st.sval)  )  {
				double width = 100;
				double height = 100;
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						//by young						
						width = Math.max(node.getWidth(), st.nval);
												
						while (st.nextToken() != st.TT_EOL) {
							if (st.ttype == st.TT_NUMBER) {
								
								//by young
								height = Math.max(node.getHeight(), st.nval);
//								System.out.println("Setting node size:" + width + "," + height);
								node.setSize(width,height);
								
								break;
							}
						}
						break;
					}
				}
			}
			
			//by young
			// determine node color 
			if ( this.getToken("ColorDeclarator").equals(st.sval)  )  {
				
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						Color c = new Color((int)st.nval);
						node.setColor(c);
						}
						break;
					}
			}
			
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// determine the order of possible values
			if ( this.getToken("PossibleValuesOrder").equals(st.sval)  )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD || st.ttype == st.TT_NUMBER) { // accept numbers as possible values (for future releases)
						String name = st.sval;
						// only update resident nodes
						if (node instanceof IResidentNode) {
							Debug.println(this.getClass() , "Solving the order of possible values of " + node.getName());
							
							IResidentNode resident = (IResidentNode)node;
							if (!resident.hasPossibleValue(name)) {
								// we should only add the instances also declared previously in OWL file
								continue;
							}
							
							try {
								// solve order of possible values
								int possibleValueIndex = 0; // this index will be used to order the possible values
								do { // because the cursor is already at the 1st possible value, we use do-while instead of just while
									if (st.ttype == st.TT_EOL) {
										Debug.println(this.getClass() , "End of line found.");
										break;
									} else if (st.ttype == st.TT_WORD) {
										if (this.isToUseOrderOfUBFFile()) {
											// find the name of the state
											Debug.println(this.getClass() , "Reordering the possible value: " + st.sval);
											int oldIndex = resident.getPossibleValueIndex(st.sval);
											if (oldIndex >= 0 ) {
												Debug.println(this.getClass() , "Swapping indexes: " + possibleValueIndex + " - " + oldIndex);
												try {
													Collections.swap(resident.getPossibleValueLinkList(), possibleValueIndex, oldIndex);
													possibleValueIndex++; // only update indexes if swap was successful
												} catch (Exception e) {
													Debug.println(this.getClass(), "Could not reorder state indexes: " 
															+ possibleValueIndex
															+ ", " + oldIndex, e);
												}
												try {
													Debug.println(this.getClass() , "Altered values: " 
															+ resident.getPossibleValueLinkList().get(possibleValueIndex - 1).getState().getName()
															+ ", " 
															+ resident.getPossibleValueLinkList().get(oldIndex).getState().getName());
												} catch (Throwable t) {
													t.printStackTrace();
												}
											} else {
												Debug.println(this.getClass() , "State was not found: " + st.sval);
											}
										}
									}
								} while (st.nextToken() != st.TT_EOF );
							} catch(Exception ex){
								ex.printStackTrace(); 
							}
						} else {
							Debug.println(this.getClass(), node.getName() + " is not a resident node, thus no order is appliable.");
						}
					}
				}
			}
			
			
				
		} // while not EOF
	}
	

	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		if (input.getName().toLowerCase().endsWith("."+this.PROWL_EXTENSION.toLowerCase())) {
			return this.prowlIO.loadMebn(input);
		} else {
			return this.loadMebn(input);
		}
	}
	
	/**
	 * Delegates to load {@link #load(File)} after setting
	 * {@link #setToUseOrderOfUBFFile(isToUseOrderOfUBFFile)}
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 * @param input the input file in .ubf (or .owl) format
	 * @param isToUseOrderOfUBFFile if true, the elements in the loaded {@link Graph} will 
	 * be ordered respecting the appearance order within the UBF file.
	 */
	public Graph load(File input, boolean isToUseOrderOfUBFFile) throws LoadException, IOException {
		this.setToUseOrderOfUBFFile(isToUseOrderOfUBFFile);
		return this.load(input);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws IOException {
		this.saveMebn(output, (MultiEntityBayesianNetwork)net);
	}
	
	/**
	 * Convert a token type to .ubf syntax. Search for UBF syntax.
	 * @param token identifier to be searched
	 * @return string representing that entity in .ubf syntax
	 * @see this.tokens
	 */
	protected String getToken(String s) throws IllegalArgumentException {
		
		for (int i = 0 ; i < tokens.length; i++  ) {
			if (tokens[i][0].equals(s) ) {
				return tokens[i][1];
			}
		}
		throw new IllegalArgumentException(this.resource.getString("InvalidSyntax"));
	}
	
	/**
	 * Sets a token value. Use it only if you want to change UBF synax in runtime.
	 * @param s: token identifier to be searched
	 * @param newValue: new value for s
	 * @return string representing that entity in .ubf syntax
	 * @see this.tokens
	 */
	protected void setToken(String s, String newValue) throws IllegalArgumentException {
		
		for (int i = 0 ; i < tokens.length; i++  ) {
			if (tokens[i][0].equals(s) ) {
				tokens[i][1] = newValue;
			}
		}
		throw new IllegalArgumentException(this.resource.getString("InvalidSyntax"));
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {

//		GUICommand cancelCommand = new GUICommand(){
//
//			public void execute() {
//				UbfIO.this.prowlIO.getLoader().cancel(); 
//			}
//			
//		}; 
		
//		ProgressBarPanel progressBar = new ProgressBarPanel(); 
//        createAndShowProgressBar(progressBar);
//        this.prowlIO.getLoader().attach(progressBar); 
//        progressBar.update(); 
        
		MultiEntityBayesianNetwork mebn = null;	// target mebn
		
		// Inicially, deducing default owl file name in case we dont find it
		String owlFilePath = file.getPath().substring(0,file.getPath().lastIndexOf(this.FILE_EXTENSION)) 
						+ PROWL_EXTENSION;
        
		File prowlFile = null;	// correspondent owl file
		
		// set up UBF file tokenizer
		StreamTokenizer st = new StreamTokenizer(new BufferedReader(new FileReader(file)));
		st.wordChars('A', 'z');
		st.wordChars('.', '.');
		//st.wordChars('=','=');		
		st.whitespaceChars(this.getToken("ArgumentSeparator").charAt(0),
						   this.getToken("ArgumentSeparator").charAt(0));	// jumps separators
		st.whitespaceChars(this.getToken("AttributionSeparator").charAt(0),
				   		   this.getToken("AttributionSeparator").charAt(0));	// jumps separators
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.quoteChar('"');
		st.quoteChar('\'');		
		st.eolIsSignificant(true);	// an attributeXvalue pair must reside in a same line
		st.commentChar(this.getToken("CommentInitiator").charAt(0));
		
		
		// Read UBF version
		double version = this.readVersion(st);
		//System.out.println("UBF version is " + version);
		if (this.ubfVersion < version) {
			throw new IOMebnException(resource.getString("IncompatibleVersion"));
		}
		
		// Read correspondent owl file name
		try {
			owlFilePath = this.readOwlFile(st);
		} catch (IOException e) {
			throw e;
		}
		
		// Make owl file path relative to ubf file path
		owlFilePath = file.getParentFile().getCanonicalPath().concat("/" + owlFilePath);
		
		
		Debug.println("Opening .owl file: " + owlFilePath);
		
		// Extracting owl file
		try {
			prowlFile = new File(owlFilePath);
			if (!prowlFile.exists()) {
				throw new IOException(this.resource.getString("NoProwlFound"));
			}
		
			mebn = this.prowlIO.loadMebn(prowlFile);
		} catch(Exception e) {
			e.printStackTrace();
			throw new IOException(e.getLocalizedMessage() + " : " + this.resource.getString("InvalidProwlScheme"));
		}
		
		//System.out.println("\n\nLoading UBF config");
		
		// trating global data (MTheory)		
		try {
			this.updateMTheory(st,mebn);
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage() + " : "
					+ resource.getString("MTheoryConfigError"));
		}
		
		// treating local data (MFrag)
		try {
			this.updateMFrag(st,mebn);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage() + " : "
					+ resource.getString("MFragConfigError"));
		}

	    // treating object entity (instances) information (mainly, ordenable entities)
		updateObjectEntities(st, mebn);
		
		return mebn;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn)
			throws IOException, IOMebnException {
		if (file.getName().lastIndexOf(".") < 0) {
			String name = file.getPath() + "."+SUPPORTED_EXTENSIONS[0];
			file = new File(name);
		}
		
		// Placeholder for Variable type names
		String varType = null;
		
		// Create .owl placeholder
		String noExtensionFileName = file.getPath().substring(0,file.getPath().lastIndexOf(this.FILE_EXTENSION));
		File prowlFile = new File(noExtensionFileName + PROWL_EXTENSION);
		
		// create output stream for .ubf files
		PrintStream out = new PrintStream(new FileOutputStream(file));
		
		// save .owl
		try {
			this.prowlIO.saveMebn(prowlFile,mebn);
		} catch(Exception e) {
			e.printStackTrace();
			throw new IOException(e.getLocalizedMessage() + " : " + this.resource.getString("UnknownPrOWLError"));
		}
		
		
		// Extract relative prowlFile URI
		//File root = new File("root");
		//URI relativeURI = root.getCanonicalFile().getParentFile().toURI();
		URI relativeURI = file.getCanonicalFile().getParentFile().toURI();
		relativeURI = relativeURI.relativize(prowlFile.toURI());
		
		
		// Save .ubf header
		out.println(this.getToken("CommentInitiator") + resource.getString("UBFFileHeader"));
		out.println(this.getToken("VersionDeclarator") 
				  + this.getToken("AttributionSeparator") + this.ubfVersion);
		out.println(this.getToken("PrOwlFileDeclarator")				  
				  + this.getToken("AttributionSeparator") 
				  + this.getToken("Quote")
				  + relativeURI.getPath()
				  + this.getToken("Quote") );
		
		// Save Mtheory declarations
		out.println();
		out.println(this.getToken("CommentInitiator") + resource.getString("UBFMTheory"));
		out.println(this.getToken("MTheoryDeclarator")  
				+ this.getToken("AttributionSeparator") +  mebn.getName() );
		out.println(this.getToken("NextMFragDeclarator")  
				+ this.getToken("AttributionSeparator") + mebn.getDomainMFragNum() );
		out.println(this.getToken("NextResidentDeclarator")  
				+ this.getToken("AttributionSeparator") + mebn.getDomainResidentNodeNum());
		out.println(this.getToken("NextInputDeclarator")  
				+ this.getToken("AttributionSeparator") + mebn.getGenerativeInputNodeNum());
		out.println(this.getToken("NextContextDeclarator")  
				+ this.getToken("AttributionSeparator") + mebn.getContextNodeNum());
		out.println(this.getToken("NextEntityDeclarator")  
				+ this.getToken("AttributionSeparator") + mebn.getEntityNum());
		
		
		
		//	Save MFrags and Nodes declarations
		out.println();
		out.println(this.getToken("CommentInitiator") + resource.getString("UBFMFragsNodes"));
		for (Iterator iter = mebn.getDomainMFragList().iterator(); iter.hasNext();) {
			MFrag mfrag = (MFrag) iter.next();
			out.println();
			out.println(this.getToken("CommentInitiator") + resource.getString("UBFMFrags"));
			out.println(this.getToken("MFragDeclarator")  
					+ this.getToken("AttributionSeparator") + mfrag.getName());
			out.println(this.getToken("NextOrdinalVarDeclarator")  
					+ this.getToken("AttributionSeparator") + mfrag.getOrdinaryVariableNum());
			
			// Listing Resident nodes
			if (mfrag.getResidentNodeList() != null) {
				if (!mfrag.getResidentNodeList().isEmpty()) {
					out.println();
					out.println(this.getToken("CommentInitiator") + resource.getString("UBFResidentNodes"));
					out.println();
					for (Iterator<ResidentNode> iterator = mfrag.getResidentNodeList().iterator(); iterator.hasNext();) {
						ResidentNode node = iterator.next();
						out.println();				
						out.println(this.getToken("NodeDeclarator")  
								+ this.getToken("AttributionSeparator") + node.getName());
						out.println(this.getToken("TypeDeclarator")  
								+ this.getToken("AttributionSeparator") + this.getToken("DomainResidentType"));
						out.println( this.getToken("PositionDeclarator")  
								+ this.getToken("AttributionSeparator") + node.getPosition().getX() 
								   + this.getToken("ArgumentSeparator")  + node.getPosition().getY());
						out.println( this.getToken("SizeDeclarator")     
								+ this.getToken("AttributionSeparator") + node.getWidth()
								   + this.getToken("ArgumentSeparator") + node.getHeight());
						
						//by young
						out.println( this.getToken("ColorDeclarator")     
								+ this.getToken("AttributionSeparator") + node.getColor().getRGB());
						
						//out.println(this.getToken("NextArgumentDeclarator") + node.getNumNextArgument());
						
						// listing the order of the resident node's possible values
						out.print( this.getToken("PossibleValuesOrder") + this.getToken("AttributionSeparator"));
						for (Iterator<StateLink> stateLinkIterator =  node.getPossibleValueLinkList().iterator(); stateLinkIterator.hasNext();) {
							try {
								StateLink possibleValue = stateLinkIterator.next();
								out.print(possibleValue.getState().getName());
								if (stateLinkIterator.hasNext()) {
									out.print(this.getToken("ArgumentSeparator"));
								}
							} catch (Throwable e) {
								// the order of the states is not so important, thus we may just continue in case of any error.
								e.printStackTrace();
							}
						}
						out.println(); // finish listing the order of the possible values with a line feed
					}
				}	
			}
			
			
			// Listing Input nodes
			if (mfrag.getInputNodeList() != null) {
				if (!mfrag.getInputNodeList().isEmpty()) {
					out.println();
					out.println(this.getToken("CommentInitiator") + resource.getString("UBFInputNodes"));
					out.println();
					for (Iterator iterator = mfrag.getInputNodeList().iterator(); iterator.hasNext();) {
						InputNode node = (InputNode) iterator.next();
						out.println();				
						out.println(this.getToken("NodeDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getName());
						out.println(this.getToken("TypeDeclarator")  
								+ this.getToken("AttributionSeparator") +  this.getToken("GenerativeInputType"));
						out.println( this.getToken("PositionDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getPosition().getX() 
								   + this.getToken("ArgumentSeparator")  + node.getPosition().getY());
						out.println( this.getToken("SizeDeclarator")     
								+ this.getToken("AttributionSeparator") +  node.getWidth()
								   + this.getToken("ArgumentSeparator") + node.getHeight());
						 
						//by young
						out.println( this.getToken("ColorDeclarator")     
								+ this.getToken("AttributionSeparator") + node.getColor().getRGB());
					}
				}
			}
			
			
			//	 Listing Context nodes
			if (mfrag.getContextNodeList() != null) {
				if (!mfrag.getContextNodeList().isEmpty()) {
					out.println();
					out.println(this.getToken("CommentInitiator") + resource.getString("UBFContextNodes"));
					out.println();
					for (Iterator iterator = mfrag.getContextNodeList().iterator(); iterator.hasNext();) {
						ContextNode node = (ContextNode) iterator.next();
						out.println();				
						out.println(this.getToken("NodeDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getName());
						out.println(this.getToken("TypeDeclarator")  
								+ this.getToken("AttributionSeparator") +  this.getToken("ContextType"));
						out.println( this.getToken("PositionDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getPosition().getX() 
								   + this.getToken("ArgumentSeparator")  + node.getPosition().getY());
						out.println( this.getToken("SizeDeclarator")     
								+ this.getToken("AttributionSeparator") +  node.getWidth()
								   + this.getToken("ArgumentSeparator") + node.getHeight());
						
						 
						//by young
						out.println( this.getToken("ColorDeclarator")     
								+ this.getToken("AttributionSeparator") + node.getColor().getRGB());
					}
				}
			}
			
			
			//	 Listing Ordinary variables
			if ( mfrag.getOrdinaryVariableList() != null) {
				if (!mfrag.getOrdinaryVariableList().isEmpty()) {
					out.println();
					out.println(this.getToken("CommentInitiator") + resource.getString("UBFOrdinalVars"));
					out.println();
					for (Iterator iterator = mfrag.getOrdinaryVariableList().iterator(); iterator.hasNext();) {
						OrdinaryVariable node = (OrdinaryVariable) iterator.next();
						out.println();				
						out.println(this.getToken("NodeDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getName());
						out.println(this.getToken("TypeDeclarator")  
								+ this.getToken("AttributionSeparator") +  this.getToken("OrdinalVarType"));
						out.println( this.getToken("PositionDeclarator")  
								+ this.getToken("AttributionSeparator") +  node.getPosition().getX() 
								   + this.getToken("ArgumentSeparator")  + node.getPosition().getY());
						out.println( this.getToken("SizeDeclarator")     
								+ this.getToken("AttributionSeparator") +  node.getWidth()
								   + this.getToken("ArgumentSeparator") + node.getHeight());
						
						 
						//by young
						out.println( this.getToken("ColorDeclarator")     
								+ this.getToken("AttributionSeparator") + node.getColor().getRGB());
					}
				}
			}
			
			
		} // for
		
		//	Save Object Entity Instance Ordereables declarations
		out.println();
		out.println(this.getToken("CommentInitiator") + resource.getString("UBFObjectEntityInstances"));
		for(ObjectEntity objectEntity: mebn.getObjectEntityContainer().getListEntity()){
			if(objectEntity.isOrdereable()){ //For this version, only the ordereables instances will be saved. 
				out.println();
				out.println(this.getToken("ObjectEntityDeclarator")
						+ this.getToken("AttributionSeparator") +  objectEntity.getName());
				out.print(this.getToken("EntityInstancesDeclarator") + this.getToken("AttributionSeparator"));
				
				List<ObjectEntityInstanceOrdereable> list = new ArrayList<ObjectEntityInstanceOrdereable>();
				for(ObjectEntityInstance instance: objectEntity.getInstanceList()){
					list.add((ObjectEntityInstanceOrdereable)instance);
				}
				
				String instances = ""; 
				for(ObjectEntityInstance instance: ObjectEntityInstanceOrdereable.ordererList(list)){
					instances+= instance.getName() + this.getToken("ArgumentSeparator");
				}
				//delete argumetn separator. 
				if(list.size() > 0 ){
					instances = instances.substring(0, instances.length() - this.getToken("ArgumentSeparator").length());
				}
				out.println(instances);
			}
		}
		
	}



	/**
	 * @return Returns the resource.
	 */
	public ResourceBundle getResource() {
		return resource;
	}



	/**
	 * @param resource The resource to set.
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}
	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#getFileExtension()
	 */
	public String getFileExtension() {
		return this.FILE_EXTENSION;
	}
	
	/**
	 * Checks if file extension is compatible to what this i/o expects.
	 * @see #supports(File, boolean)
	 * @param extension
	 * @param isLoadOnly
	 * @return
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		for (String ext : this.getSupportedFileExtensions(isLoadOnly)) {
			if (ext.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return (isLoadOnly?SUPPORTED_EXTENSIONS_LOAD:SUPPORTED_EXTENSIONS);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		if (isLoadOnly) {
			return "UnBBayes File (.ubf), PR-OWL (.owl)";
		} else {
			return "UnBBayes File (.ubf)";
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		String fileExtension = null;
		try {
			if (file.isDirectory()) {
				// do not support directory
				return false;
			}
			int index = file.getName().lastIndexOf(".");
			if (!isLoadOnly && index < 0) {
				// force this to support saving files with no extension
				return true;
			}
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return this.supports(fileExtension, isLoadOnly);
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * This boolean attribute indicates that this {@link UbfIO} must
	 * reorder the loaded MEBN elements respecting its appearance order
	 * in the original UBF file.
	 * @return the toUseOrderOfUBFFile
	 */
	public boolean isToUseOrderOfUBFFile() {
		return toUseOrderOfUBFFile;
	}
	
	/**
	 * This boolean attribute indicates that this {@link UbfIO} must
	 * reorder the loaded MEBN elements respecting its appearance order
	 * in the original UBF file.
	 * @param toUseOrderOfUBFFile the toUseOrderOfUBFFile to set
	 */
	public void setToUseOrderOfUBFFile(boolean toUseOrderOfUBFFile) {
		this.toUseOrderOfUBFFile = toUseOrderOfUBFFile;
	}
	/**
	 * @return the prowlIO
	 */
	public MebnIO getProwlIO() {
		return prowlIO;
	}
	/**
	 * @param prowlIO the prowlIO to set
	 */
	public void setProwlIO(MebnIO prowlIO) {
		this.prowlIO = prowlIO;
	}
	/**
	 * @return the tokens
	 */
	public String[][] getTokens() {
		return tokens;
	}
	/**
	 * @param tokens the tokens to set
	 */
	public void setTokens(String[][] tokens) {
		this.tokens = tokens;
	}
	

}
