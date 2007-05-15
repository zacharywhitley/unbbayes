/**
 * <p>Title: UnBBayes</p>
 * <p>Description: UBF file format manipulator</p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto (cardialfly@[yahoo|gmail].com)
 * @version 0.1
 * @since 01/05/2007
 */
package unbbayes.io.mebn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Ubf file format manipulator. </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto (cardialfly@[yahoo|gmail].com)
 * @version 0.1
 * @since 01/05/2007
 * @see unbbayes.io.mebn.resources.IoUbfResources
 */
public class UbfIO implements MebnIO {
	
	private static final String  prowlExtension = "owl";
		
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoUbfResources");	
	
	private PrOwlIO prowlIO = null;	// stores files w/ .owl extension
		
	
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
			{"NextArgumentDeclarator" , "NextArgument"},
			
			{"DomainResidentType" , "DomainResidentNode"},
			{"GenerativeInputType" , "GenerativeInputNode"},
			{"ContextType" , "ContextNode"},
			{"OrdinalVarType" , "OrdinalVar"},
	};
	
	
	public static final double ubfVersion = 0.02;
	
	public static final String fileExtension = "ubf";
	
	

	private UbfIO() {
		super();
		this.prowlIO = new PrOwlIO();
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
			if (name.compareTo(element.getName()) == 0) {
				return element;
			}
		}		
		return null;
	}
	
	
	private Node searchNode(String name , DomainMFrag mfrag) {
		for (Iterator iter = mfrag.getResidentNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.compareTo(element.getName()) == 0) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getInputNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.compareTo(element.getName()) == 0) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getContextNodeList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.compareTo(element.getName()) == 0) {
				return element;
			}
		}
		for (Iterator iter = mfrag.getOrdinaryVariableList().iterator(); iter.hasNext();) {
			Node element = (Node) iter.next();
			if (name.compareTo(element.getName()) == 0) {
				return element;
			}
		}
		return null;
	}
	
	
	private double readVersion (StreamTokenizer st) throws IOException {
		while (st.nextToken() != st.TT_EOF) {
			if ( ( st.ttype == st.TT_WORD ) 
					&& ( this.getToken("VersionDeclarator").compareTo(st.sval) == 0) ) {
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
					&& ( this.getToken("PrOwlFileDeclarator").compareTo(st.sval) == 0) ) {
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
			if (this.getToken("MFragDeclarator").compareTo(st.sval) == 0 ) {
				st.pushBack();
				break;
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			if ( this.getToken("MTheoryDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NextMFragDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NextResidentDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NextInputDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NextContextDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NextEntityDeclarator").compareTo(st.sval) == 0 )  {
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
		
		
		while (st.nextToken() != st.TT_EOF) {
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// determine considered mfrag
			if ( this.getToken("MFragDeclarator").compareTo(st.sval) == 0 )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						mfrag = this.searchMFrag(st.sval,mebn);
						//System.out.println("Updating mfrag " + mfrag.getName());
						break;
					}
				}
			}
			
			if (mfrag == null) {
				//System.out.println("MFrag still not found");
				continue;
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// Set NextOrdinalVarDeclarator
			if ( this.getToken("NextOrdinalVarDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("NodeDeclarator").compareTo(st.sval) == 0 )  {
				st.pushBack();
				//System.out.println("Node declaration found ");
				
				try  {
					this.updateNode(st,(DomainMFrag)mfrag);
				} catch (ClassCastException e) {
					throw new ClassCastException(resource.getString("MFragTypeException"));
				}
				
			}
			
			
		} // while not EOF
	}
	
	
	private void updateNode(StreamTokenizer st, DomainMFrag mfrag) throws IOException {
		
		Node node = null;
		//System.out.println("Updating Nodes");
		while (st.nextToken() != st.TT_EOF) {
			//System.out.println(">> Read (str)" + st.sval + " from UBF");
			//System.out.println(">> Read (number)" + st.nval + " from UBF");
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			//	finishing condition
			if (this.getToken("MFragDeclarator").compareTo(st.sval) == 0 ) {
				st.pushBack();
				//System.out.println("Next MFrag Found");
				
				break;
			}
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// determine considered node
			if ( this.getToken("NodeDeclarator").compareTo(st.sval) == 0 )  {
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_WORD) {
						node = this.searchNode(st.sval,mfrag);
						//System.out.println("Setting Node: " + node.getName());
						break;
					}
				}
			}
			
			if (node == null) {
				continue;
			}
			
			// TODO verify node type using TypeDeclarator
			
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			// Set node position
			if ( this.getToken("PositionDeclarator").compareTo(st.sval) == 0 )  {
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
			if ( this.getToken("SizeDeclarator").compareTo(st.sval) == 0 )  {
				double width = 100;
				while (st.nextToken() != st.TT_EOL) {
					if (st.ttype == st.TT_NUMBER) {
						width = st.nval;
						while (st.nextToken() != st.TT_EOL) {
							if (st.ttype == st.TT_NUMBER) {
								node.setSize(width,st.nval);
								//System.out.println("Setting node size:" + width + "," + st.nval);
								break;
							}
						}
						break;
					}
				}
			}
			
			
		} // while not EOF
	}
	

	
	
	
	/**
	 * Convert a token type to .ubf syntax. Search for UBF syntax.
	 * @param token identifier to be searched
	 * @return string representing that entity in .ubf syntax
	 * @see this.tokens
	 */
	protected String getToken(String s) throws IllegalArgumentException {
		
		for (int i = 0 ; i < tokens.length; i++  ) {
			if (tokens[i][0].compareTo(s) == 0) {
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
			if (tokens[i][0].compareTo(s) == 0) {
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
		
				
		MultiEntityBayesianNetwork mebn = null;	// target mebn
		
		// Inicially, deducing default owl file name in case we dont find it
		String owlFilePath = file.getPath().substring(0,file.getPath().lastIndexOf(this.fileExtension)) 
						+ prowlExtension;
		
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
		
		//System.out.println("Opening .owl file: " + owlFilePath);
		
		// Extracting owl file
		try {
			prowlFile = new File(owlFilePath);
			if (!prowlFile.exists()) {
				throw new IOException(this.resource.getString("NoProwlFound"));
			}
		
			mebn = this.prowlIO.loadMebn(prowlFile);
		} catch(Exception e) {
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
			System.err.println(e.getLocalizedMessage() + " : "
					+ resource.getString("MFragConfigError"));
		}
		
		
		
		
		return mebn;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn)
			throws IOException, IOMebnException {
		
		// Placeholder for Variable type names
		String varType = null;
		
		// Create .owl placeholder
		String noExtensionFileName = file.getPath().substring(0,file.getPath().lastIndexOf(this.fileExtension));
		File prowlFile = new File(noExtensionFileName + prowlExtension);
		
		// create output stream for .ubf files
		PrintStream out = new PrintStream(new FileOutputStream(file));
		
		// save .owl
		try {
			this.prowlIO.saveMebn(prowlFile,mebn);
		} catch(Exception e) {
			throw new IOException(e.getLocalizedMessage() + " : " + this.resource.getString("UnknownPrOWLError"));
		}
		
		// Extract relative prowlFile URI
		File root = new File("root");
		URI relativeURI = root.getCanonicalFile().getParentFile().toURI();
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
			DomainMFrag mfrag = (DomainMFrag) iter.next();
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
					for (Iterator iterator = mfrag.getResidentNodeList().iterator(); iterator.hasNext();) {
						ResidentNode node = (ResidentNode) iterator.next();
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
						//out.println(this.getToken("NextArgumentDeclarator") + node.getNumNextArgument());
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
					}
				}
			}
			
			
		} // for
		
		
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

}
