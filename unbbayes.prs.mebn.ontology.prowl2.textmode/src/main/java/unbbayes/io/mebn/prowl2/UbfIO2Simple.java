package unbbayes.io.mebn.prowl2;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;

import unbbayes.io.exception.LoadException;
import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.prowl2.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.ObjectEntityInstanceOrdereable;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.prowl2.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.util.Debug;


/**
 * @author Shou Matsumoto
 */
public class UbfIO2Simple extends UbfIO {

	private double ubfVersion = 2.0d;
	
	private MebnIO prowlIO = OWLAPICompatiblePROWL2IO.newInstance();
	
	private String prowlFileExtension = "owl";
	
	private String ubfFileExtension = FILE_EXTENSION;
	
	private boolean isToUpdateMEBNName = false;
	
	private Logger log = Logger.getLogger(getClass());
	

	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.io.mebn.resources.IoUbfResources.class.getName(),
				Locale.getDefault(), 
				getClass().getClassLoader()
			);
	
	/**
	 * The default constructor is only made public because of plug-in's requirements.
	 * It won't initialize complex fields (e.g. resource classes, wrapped classes, etc).
	 * @deprecated use {@link #getInstance()} instead.
	 */
	public UbfIO2Simple() {
		super();
		try {
			// make sure name is different from superclass
			this.setName(getClass().getSimpleName());
		} catch (Throwable e) {
			getLog().warn("Failed to set IO component's name to " + getClass().getSimpleName() 
					+ ". Current name is " + getName(), e);
		}
	}
	
	/**
	 * Construction method for {@link UbfIO2Simple}
	 * @return {@link UbfIO2Simple} instance
	 */
	public static UbfIO getInstance() {
		UbfIO2Simple ret = new UbfIO2Simple();
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException, IOException {
		if (input.getName().toLowerCase().endsWith("."+this.getProwlFileExtension().toLowerCase())) {
			return this.getProwlIO().loadMebn(input);
		} else {
			return this.loadMebn(input);
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.UbfIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		// we must adapt the supported file extensions to include the one getProwlFileExtension() returns
		try {
			if (isLoadOnly) {	// PR-OWL is not supported in "save", so the adaptation is only necessary in "load" time
				// obtain extensions supported by superclass
				String[] extensionsFromSuper =  super.getSupportedFileExtensions(isLoadOnly);	// isLoadOnly == true
				
				// prepare to fill extensions supported by this class (we must also use the getProwlFileExtension())
				List<String> myExtensions = new ArrayList<String>();
				boolean hasFoundPROWL = false;	// verify if the superclass already supports PR-OWL file extension
				for (String extension : extensionsFromSuper) {
					myExtensions.add(extension);
					if (extension.equalsIgnoreCase(this.getProwlFileExtension())) {
						hasFoundPROWL = true;
					}
				}
				if (!hasFoundPROWL) {
					// we must add PR-OWL compatibility if it is not added yet
					myExtensions.add(this.getProwlFileExtension()); 
				}
				return myExtensions.toArray(new String[myExtensions.size()]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// default behavior: use the one from superclass
		return super.getSupportedFileExtensions(isLoadOnly);
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.UbfIO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException {
		
		MultiEntityBayesianNetwork mebn = null;	// target mebn
		
		// Initially, let's deduce the default owl file name, just in in case we couldn't find it out
		String owlFilePath = file.getPath().substring(0,file.getPath().lastIndexOf(this.getUBFFileExtension())) 
						+ this.getProwlFileExtension();
        
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
		if (this.ubfVersion < version) {
			throw new IOMebnException("Incompatible UBF version found: " + version + ". Expected " + this.ubfVersion);
		}
		if (this.ubfVersion > version) {
			// use old loader
			return super.loadMebn(file);
		}
		
		// Read correspondent owl file name
		owlFilePath = this.readOwlFile(st);
		
		// Make owl file path relative to ubf file path
		owlFilePath = file.getParentFile().getCanonicalPath().concat("/" + owlFilePath);
		
		
		getLog().debug("Opening .owl file: " + owlFilePath);
		
		// Extracting owl file
		try {
			prowlFile = new File(owlFilePath);
			if (!prowlFile.exists()) {
				throw new IOException("No associated PR-OWL file found. UBF must point to some PR-OWL (.owl) file");
			}
		
			mebn = this.getProwlIO().loadMebn(prowlFile);
		} catch(Exception e) {
			throw new IOException(e.getLocalizedMessage() + ". This can be because of invalid PR-OWL scheme.", e);
		}
		
		if (mebn == null) {
			throw new IOException(this.getResource().getString("MTheoryConfigError"));
		}
		
		//System.out.println("\n\nLoading UBF config");
		
		// trating global data (MTheory)		
		try {
			this.updateMTheory(st,mebn, this.isToUpdateMEBNName());
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage() + " : "
					+ this.getResource().getString("MTheoryConfigError"));
		}
		
		// treating local data (MFrag)
		try {
			this.updateMFrag(st,mebn);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage() + " : "
					+ this.getResource().getString("MFragConfigError"));
		}

	    // treating object entity (instances) information (mainly, ordenable entities)
		this.updateObjectEntities(st, mebn);
		
		return mebn;
	}
	
	/**
	 * This method reads the version number from a UBF file
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the version number's line.
	 * @return the version number read
	 * @throws IOException
	 */
	protected double readVersion (StreamTokenizer st) throws IOException {
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			if ( ( st.ttype == StreamTokenizer.TT_WORD ) 
					&& ( this.getToken("VersionDeclarator").equals(st.sval)) ) {
				if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
					return st.nval;
				}  else {
					break;
				}
			}
		}
		throw new IOException("Invalid UBF syntax found at line " + st.lineno() 
				+  ". Expected version number.");
	}
	
	/**
	 * Reads the line specifying the name of the associated OWL file.
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the PR-OWL file declaration.
	 * @return the name of the OWL (PR-OWL) file
	 * @throws IOException
	 */
	protected String readOwlFile (StreamTokenizer st) throws IOException {
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			if ( ( st.ttype == StreamTokenizer.TT_WORD ) 
					&& ( this.getToken("PrOwlFileDeclarator").equals(st.sval)) ) {
				if (st.nextToken() != StreamTokenizer.TT_EOL) {
					return st.sval;
				}  else {
					break;
				}
			}
		}

		throw new IOException("Invalid UBF syntax found at line " + st.lineno() 
				+  ". Expected path to associated PR-OWL (.owl) file.");
	}
	

	/**
	 * Reads the MTheory block of the UBF. It stops if it finds a MFrag declaration or
	 * ObjectEntity declaration.
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the MTheory declaration.
	 * @param mebn : the MEBN currently being read
	 * @param isToUpdateMEBNName : if true, it updates the name of "mebn" using the value read from
	 * the UBF file.
	 * @throws IOException
	 */
	protected void updateMTheory(StreamTokenizer st, MultiEntityBayesianNetwork mebn, boolean isToUpdateMEBNName) 
						throws IOException {
		
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
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
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("MTheoryDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					// go to the end of line.
					if (isToUpdateMEBNName && st.ttype == StreamTokenizer.TT_WORD) {
						mebn.setName(st.sval);
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextMFragDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mebn.setDomainMFragNum((int)st.nval);
						//System.out.println("Setting mfrag number to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextResidentDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mebn.setDomainResidentNodeNum((int)st.nval);
						//System.out.println("Setting NextResidentDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextInputDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mebn.setGenerativeInputNodeNum((int)st.nval);
						//System.out.println("Setting NextInputDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextContextDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mebn.setContextNodeNum((int)st.nval);
						//System.out.println("Setting NextContextDeclarator to " + st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			if ( this.getToken("NextEntityDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mebn.setEntityNum((int)st.nval);
						break;
					}
				}
			} 
			
		} // while not EOF
	} // end of method
	

	/**
	 * Reads the MFrag block of an UBF file. It will finish if an ObjectEntity declaration
	 * is found.
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the MFrag declaration.
	 * @param mebn : the MEBN currently being loaded
	 * @throws IOException
	 * @throws ClassCastException
	 */
	protected void updateMFrag(StreamTokenizer st, MultiEntityBayesianNetwork mebn ) 
				throws IOException, ClassCastException {

		MFrag mfrag = null;
		
		// this orderingIndex will be used to reorder the mebn elements according to the appearance order in UBF file
		int mfragOrderingIndex = 0; 
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			
			// finishing condition
			if (this.getToken("ObjectEntityDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			// determine considered mfrag
			if ( this.getToken("MFragDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_WORD) {
						mfrag = mebn.getMFragByName(st.sval);
						if (mfrag == null) {
							getLog().warn("MFrag " + st.sval + " not found...");
						}
						break;
					}
				}
				if (mfrag == null) {
					// UBF contains more MFrags than expected. Let's just ignore such abundant MFrags...
					continue;	//
				} else {
					if (this.isToUseOrderOfUBFFile()) {
						// put mfrag to the expected position (orderingIndex)
						// Note: this code expects that changes on mebn.getMFragList() will influence the order of MFrags in mebn (i.e. getMFragList does not return a copy of the actual list)
						try {
							Collections.swap(mebn.getMFragList(), mfragOrderingIndex, mebn.getMFragList().indexOf(mfrag));
							try {
								getLog().debug(
										"Swapped MFrag \"" + mfrag.getName() 
										+ "\" to the position " + mfragOrderingIndex);
							} catch (Throwable t) {
								// ignore error in debug class
							}
							mfragOrderingIndex++;
						} catch (Exception e) {
							try {
								getLog().debug(
										"Could not place MFrag \"" + mfrag.getName() 
										+ "\" in the position " + mfragOrderingIndex + ".", e);
							} catch (Throwable t) {
								// ignore error in debug class
							}
						}
					}
				}
			}
			
			// by now, mfrag != null
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// Set NextOrdinalVarDeclarator
			if ( this.getToken("NextOrdinalVarDeclarator").equals(st.sval)  )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						mfrag.setOrdinaryVariableNum((int)st.nval);
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// treats nodes
			if ( this.getToken("NodeDeclarator").equals(st.sval)  )  {
				st.pushBack();
				this.updateNode(st,(MFrag)mfrag);
			}
			
		} // while not EOF
	}
	

	/**
	 * Reads the ObjectEntity block of an UBF file. It will finish if an End of File
	 * is found.
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the ObjectEntity declaration.
	 * @param mebn : the MEBN currently being loaded
	 * @throws IOException
	 * @throws ClassCastException
	 */
	@SuppressWarnings("deprecation")
	protected void updateObjectEntities(StreamTokenizer st, MultiEntityBayesianNetwork mebn ) 
									throws IOException, ClassCastException {
		
		ObjectEntity objectEntity = null;
		
		/*
		 * Since before setting an objectEntity w/ order we must remove all instances of it,
		 * but we need to keep track of what instances were declared within OWL file, then we
		 * just save their names and clear them when order shall be set 
		 */
		Map<String, IRI> owlDeclaredInstanceNames = null;
		while (st.nextToken() != StreamTokenizer.TT_EOF) {
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// determine considered mfrag
			if ( this.getToken("ObjectEntityDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_WORD) {
						objectEntity = mebn.getObjectEntityContainer().getObjectEntityByName(st.sval);
						// initiate tracking of entity instances
						owlDeclaredInstanceNames = new HashMap<String, IRI>();
						break;
					}
				}
			}
			
			if (objectEntity == null) {
				continue;
			}else{
				// store already declared instances
				for (ObjectEntityInstance entityInstance : objectEntity.getInstanceList()) {
					owlDeclaredInstanceNames.put(entityInstance.getName(), IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, entityInstance));
				}
				// clear instances in order to let it have order
				mebn.getObjectEntityContainer().clearAllInstances(objectEntity);
				try {
					objectEntity.setOrdereable(true);
				} catch (Exception e) {
					// UBF errors are not fatal, so, lets go on
					e.printStackTrace();
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// Set NextOrdinalVarDeclarator
			if ( this.getToken("EntityInstancesDeclarator").equals(st.sval) )  {
				ObjectEntityInstanceOrdereable prev = null; 
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_WORD) {
						String name = st.sval;
						if (owlDeclaredInstanceNames.get(name) == null) {
							// we should only add the instances also declared previously in OWL file
							continue;
						}
						try {
							ObjectEntityInstanceOrdereable oe = (ObjectEntityInstanceOrdereable)objectEntity.addInstance(name); 
							IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, oe, owlDeclaredInstanceNames.get(name));
							mebn.getObjectEntityContainer().addEntityInstance(oe);
							oe.setPrev(prev);
							if(prev!=null) prev.setProc(oe);
							prev = oe; 
						} catch (Exception e) {
							// UBF errors are not fatal, so, lets go on
							e.printStackTrace();
						} 
					}
				}
			}
			
		} // while not EOF
	}
	

	/**
	 * Reads the Node block of an UBF file. It will finish if another MFrag declaration or
	 * an ObjectEntity declaration is found.
	 * @param st : stream tokenizer of UBF file. The cursor must be immediately
	 * before the Node declaration.
	 * @param mfrag : the MFrag currently being loaded
	 * @param st
	 * @throws IOException
	 */
	protected void updateNode(StreamTokenizer st, MFrag mfrag) throws IOException {
		
		Node node = null;
		
		// the following indexes are used to reorder the nodes within the mfrag
		int residentNodeIndex = 0;
		int inputNodeIndex = 0;
		int contextNodeIndex = 0;
		int ordinaryVariableIndex = 0;
		while (st.nextToken() != StreamTokenizer.TT_EOF ) {
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			//	finishing condition
			if (this.getToken("MFragDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			// finishing condition
			if (this.getToken("ObjectEntityDeclarator").equals(st.sval) ) {
				st.pushBack();
				break;
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// determine considered node
			if ( this.getToken("NodeDeclarator").equals(st.sval) )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_WORD) {
						node = this.searchNode(st.sval,mfrag);
						break;
					}
				}
				if (node == null) {
					continue;
				} else {
					// reorder node by its type
					if (this.isToUseOrderOfUBFFile()) {
						if (node instanceof ResidentNode) {
							try {
								Collections.swap(mfrag.getResidentNodeList(), residentNodeIndex, mfrag.getResidentNodeList().indexOf(node));
								residentNodeIndex++; // we should update index only on success.
								getLog().debug("Swapping resident node " + node + " to index " + residentNodeIndex);
								getLog().debug("Swapping resident node " + node + " to index " + residentNodeIndex);
							} catch (Exception e) {
								getLog().debug("Could not reorder resident node " + node + " to index " + residentNodeIndex, e);
							}
						} else if (node instanceof InputNode) {
							try {
								Collections.swap(mfrag.getInputNodeList(), inputNodeIndex, mfrag.getInputNodeList().indexOf(node));
								inputNodeIndex++; // we should update index only on success.
								getLog().debug("Swapping input node " + node + " to index " + inputNodeIndex);
							} catch (Exception e) {
								getLog().debug("Could not reorder input node " + node + " to index " + inputNodeIndex, e);
							}
						} else if (node instanceof ContextNode) {
							try {
								Collections.swap(mfrag.getContextNodeList(), contextNodeIndex, mfrag.getContextNodeList().indexOf(node));
								contextNodeIndex++; // we should update index only on success.
								getLog().debug("Swapping context node " + node + " to index " + contextNodeIndex);
							} catch (Exception e) {
								getLog().warn("Could not reorder context node " + node + " to index " + contextNodeIndex, e);
							}
						} else if (node instanceof OrdinaryVariable){
							try {
								Collections.swap(mfrag.getOrdinaryVariableList(), ordinaryVariableIndex, mfrag.getOrdinaryVariableList().indexOf(node));
								ordinaryVariableIndex++; // we should update index only on success.
								getLog().debug("Swapping ordinary variable " + node + " to index " + ordinaryVariableIndex);
							} catch (Exception e) {
								getLog().warn("Could not reorder ordinary variable " + node + " to index " + ordinaryVariableIndex, e);
							}
						} else {
							getLog().warn("The type of node is unknown: " + node);
						}
					}
				}
			}
			
			// by now, node != null
			
			// TODO verify node type using TypeDeclarator instead
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				continue;
			}
			// Set node position
			if ( this.getToken("PositionDeclarator").equals(st.sval)  )  {
				int posx = 15;
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						posx = (int)st.nval;
						while (st.nextToken() != StreamTokenizer.TT_EOL) {
							if (st.ttype == StreamTokenizer.TT_NUMBER) {
								node.setPosition(posx,(int)st.nval);
								//System.out.println("Setting node pos:" + posx + "," + st.nval);
								break;
							}
						}
						break;
					}
				}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				// there is a format error, but UBF error should not be fatal. Let's just keep going on
				continue;
			}
			
			//	Set node size (height,width)
			if ( this.getToken("SizeDeclarator").equals(st.sval)  )  {
				double width = 100;
				double height = 100;
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						//by young						
						width = Math.max(node.getWidth(), st.nval);
												
						while (st.nextToken() != StreamTokenizer.TT_EOL) {
							if (st.ttype == StreamTokenizer.TT_NUMBER) {
								
								//by young
								height = Math.max(node.getHeight(), st.nval);
								Debug.println("Setting node size:" + width + "," + height);
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
				
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_NUMBER) {
						Color c = new Color((int)st.nval);
						node.setColor(c);
						}
						break;
					}
			}
			
			if (st.ttype != StreamTokenizer.TT_WORD) {
				// there is a format error, but UBF error should not be fatal. Let's just keep going on
				continue;
			}
			
			// determine the order of possible values
			if ( this.getToken("PossibleValuesOrder").equals(st.sval)  )  {
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (st.ttype == StreamTokenizer.TT_WORD || st.ttype == StreamTokenizer.TT_NUMBER) { // accept numbers as possible values (for future releases)
						String name = st.sval;
						// only update resident nodes
						if (node instanceof ResidentNode) {
							getLog().debug("Solving the order of possible values of " + node);
							
							ResidentNode resident = (ResidentNode)node;
							if (!resident.hasPossibleValue(name)) {
								// we should only add the instances also declared previously in OWL file
								continue;
							}
							
							try {
								// solve order of possible values
								int possibleValueIndex = 0; // this index will be used to order the possible values
								do { // because the cursor is already at the 1st possible value, we use do-while instead of just while
									if (st.ttype == StreamTokenizer.TT_EOL) {
										getLog().debug("End of line found.");
										break;
									} else if (st.ttype == StreamTokenizer.TT_WORD) {
										if (this.isToUseOrderOfUBFFile()) {
											// find the name of the state
											getLog().debug("Reordering the possible value: " + st.sval);
											int oldIndex = resident.getPossibleValueIndex(st.sval);
											if (oldIndex >= 0 ) {
												getLog().debug("Swapping indexes: " + possibleValueIndex + " - " + oldIndex);
												try {
													Collections.swap(resident.getPossibleValueLinkList(), possibleValueIndex, oldIndex);
													possibleValueIndex++; // only update indexes if swap was successful
												} catch (Exception e) {
													getLog().debug("Could not reorder state indexes: " 
															+ possibleValueIndex
															+ ", " + oldIndex, e);
												}
												try {
													getLog().debug("Altered values: " 
															+ resident.getPossibleValueLinkList().get(possibleValueIndex).getState().getName()
															+ ", " 
															+ resident.getPossibleValueLinkList().get(oldIndex).getState().getName());
												} catch (Throwable t) {
													// ignore error in debug class
												}
											} else {
												getLog().warn("State was not found: " + st.sval);
											}
										}
									}
								} while (st.nextToken() != StreamTokenizer.TT_EOF );
							} catch(Exception ex){
								ex.printStackTrace(); 
							}
						} else {
							getLog().warn(node + " is not a resident node, thus no order is appliable.");
						}
					}
				}
			}
		} // while not EOF
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
//		String varType = null;
		
		// Create .owl placeholder
		String noExtensionFileName = file.getPath().substring(0,file.getPath().lastIndexOf(this.getUBFFileExtension()));
		File prowlFile = new File(noExtensionFileName + this.getProwlFileExtension());
		
		// create output stream for .ubf files
		PrintStream out = new PrintStream(new FileOutputStream(file));
		
		// save .owl
		try {
			this.getProwlIO().saveMebn(prowlFile,mebn);
		} catch(Exception e) {
			out.close();
			throw new IOException("Failed  to write associated PR-OWL (.owl) file: " + e.getLocalizedMessage(), e);
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
		for (Iterator<MFrag> iter = mebn.getDomainMFragList().iterator(); iter.hasNext();) {
			MFrag mfrag = iter.next();
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
					for (Iterator<InputNode> iterator = mfrag.getInputNodeList().iterator(); iterator.hasNext();) {
						InputNode node = iterator.next();
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
					for (Iterator<ContextNode> iterator = mfrag.getContextNodeList().iterator(); iterator.hasNext();) {
						ContextNode node = iterator.next();
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
					for (Iterator<OrdinaryVariable> iterator = mfrag.getOrdinaryVariableList().iterator(); iterator.hasNext();) {
						OrdinaryVariable node =  iterator.next();
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
		
		out.close();
		
	}

	/**
	 * This method searches for a {@link ResidentNode}, {@link InputNode}, {@link ContextNode} or {@link OrdinaryVariable}
	 * from a name. This method was created because {@link MFrag} does not provide a general search method including
	 * all types of nodes. The order of the search is: 1 - resident nodes; 2 - input nodes; 3 - context nodes; 4 - ordinary variables
	 * @param name	: the name of the node to look for.
	 * @param mfrag	: the mfrag containing (or not) the node
	 * @return : the node or null if it is not found
	 */
	protected Node searchNode(String name , MFrag mfrag) {
		Node node = null;	// auxiliary variable
		
		// resident node
		node = mfrag.getDomainResidentNodeByName(name);
		if (node != null) {
			return node;
		}

		// input node (MFrag seems not to provide a method to find an input node by name)
		for (Iterator<InputNode> iter = mfrag.getInputNodeList().iterator(); iter.hasNext();) {
			node = iter.next();
			if (name.equals(node.getName())) {
				return node;
			}
		}		
		
		// context node
		node = mfrag.getContextNodeByName(name);
		if (node != null) {
			return node;
		}

		// ordinary variable
		node = mfrag.getOrdinaryVariableByName(name);
		if (node != null) {
			return node;
		}
		
		// nothing found
		return null;
	}

	/**
	 * @return the ubfVersion
	 */
	public double getUbfVersion() {
		return ubfVersion;
	}

	/**
	 * @param ubfVersion the ubfVersion to set
	 */
	public void setUbfVersion(double ubfVersion) {
		this.ubfVersion = ubfVersion;
	}

	/**
	 * @return the prowlIO . A new instance will be created if none was specified
	 */
	public MebnIO getProwlIO() {
		if (prowlIO == null) {
			prowlIO = OWLAPICompatiblePROWL2IO.newInstance();
		}
		return prowlIO;
	}

	/**
	 * @param prowlIO the prowlIO to set
	 */
	public void setProwlIO(MebnIO prowlIO) {
		this.prowlIO = prowlIO;
	}
	
	

	/**
	 * This attribute stores the file extension of a normal PR-OWL file.
	 * If a file using this extension is passed to {@link #load(File)},
	 * then the file will be delegated to {@link #getProwlIO()}.
	 * @return the prowlFileExtension
	 */
	public String getProwlFileExtension() {
		return prowlFileExtension;
	}

	/**
	 * This attribute stores the file extension of a normal PR-OWL file.
	 * If a file using this extension is passed to {@link #load(File)},
	 * then the file will be delegated to {@link #getProwlIO()}.
	 * @param prowlFileExtension the prowlFileExtension to set
	 */
	public void setProwlFileExtension(String prowlFileExtension) {
		this.prowlFileExtension = prowlFileExtension;
	}

	/**
	 * If set to true, {@link #loadMebn(File)} will call {@link #updateMTheory(StreamTokenizer, MultiEntityBayesianNetwork, boolean)}
	 * setting the last boolean parameter to true.
	 * @return the isToUpdateMEBNName
	 * @see #updateMTheory(StreamTokenizer, MultiEntityBayesianNetwork, boolean)
	 */
	public boolean isToUpdateMEBNName() {
		return isToUpdateMEBNName;
	}

	/**
	 * If set to true, {@link #loadMebn(File)} will call {@link #updateMTheory(StreamTokenizer, MultiEntityBayesianNetwork, boolean)}
	 * setting the last boolean parameter to true.
	 * @param isToUpdateMEBNName the isToUpdateMEBNName to set
	 * @see #updateMTheory(StreamTokenizer, MultiEntityBayesianNetwork, boolean)
	 */
	public void setToUpdateMEBNName(boolean isToUpdateMEBNName) {
		this.isToUpdateMEBNName = isToUpdateMEBNName;
	}

	/**
	 * This is the .ubf file extension. Change this if you want this class to start handling other file extensions.
	 * @return the ubfFileExtension
	 */
	public String getUBFFileExtension() {
		return ubfFileExtension;
	}

	/**
	 * This is the .ubf file extension. Change this if you want this class to start handling other file extensions.
	 * @param ubfFileExtension the ubfFileExtension to set
	 */
	public void setUBFFileExtension(String ubfFileExtension) {
		this.ubfFileExtension = ubfFileExtension;
	}

	/**
	 * @return the log
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(Logger log) {
		this.log = log;
	}

}
