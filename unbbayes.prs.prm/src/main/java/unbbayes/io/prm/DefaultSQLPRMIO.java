/**
 * 
 */
package unbbayes.io.prm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.prm.AttributeDescriptor;
import unbbayes.prs.prm.AttributeValue;
import unbbayes.prs.prm.ForeignKey;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IForeignKey;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.PRM;
import unbbayes.prs.prm.PRMClass;
import unbbayes.prs.prm.PRMObject;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultSQLPRMIO implements IPRMIO {

	private String name = "SQL2PRM";
	
	/** Supported file extension */
	public static final String[] SUPPORTED_EXTENSIONS = {"sql"};

	/** Textual description of supported file extension */
	public static final String SUPPORTED_FILE_DESCRIPTION = "Non-standard SQL script for UnBBayes' PRM (.sql)";
	
	/**
	 * This default constructor is made public for plugin support.
	 * @deprecated use {@link #getInstance()} instead.
	 */
	public DefaultSQLPRMIO() {
		super();
	}		
	/**
	 * Construction method for DefaultSQLPRMIO
	 * @return UbfIO instance
	 */
	public static DefaultSQLPRMIO getInstance() {
		return new DefaultSQLPRMIO();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean arg0) {
		return SUPPORTED_EXTENSIONS;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean arg0) {
		return SUPPORTED_FILE_DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#load(java.io.File)
	 */
	public Graph load(File file) throws LoadException, IOException {
		// set up PRM
		IPRM prm = PRM.newInstance(file.getName());
		
		// set up SQL file tokenizer
		StreamTokenizer st = this.setUpStreamTokenizer(file);
		
		// start reading file. we only handle CREATE, ALTER or INSERT
		while (st.nextToken() != st.TT_EOF) {
			// handle CREATE TABLE
			if ( ( st.ttype == st.TT_WORD ) && "CREATE".equalsIgnoreCase(st.sval) ) {
				this.handleCreateTable(st, prm);
			}
			
			// handle ALTER TABLE
			if ( ( st.ttype == st.TT_WORD ) && "ALTER".equalsIgnoreCase(st.sval) ) {
				this.handleAlterTable(st, prm);
			}
			
			// handle INSERT INTO
			if ( ( st.ttype == st.TT_WORD ) && "INSERT".equalsIgnoreCase(st.sval) ) {
				this.handleInsertInto(st, prm);
			}
		}
		
		return prm;
	}
	
	/**
	 * Handles the CREATE TABLE statement. It assumes st is pointing
	 * to CREATE token. At the end, st is going to be pointing at ';'.
	 * @param st
	 * @param prm
	 * @throws IOException 
	 */
	protected void handleCreateTable(StreamTokenizer st, IPRM prm) throws IOException {

		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == ';') {
				break;
			}
			if (st.ttype == st.TT_WORD && "TABLE".equalsIgnoreCase(st.sval)) {
				if (st.nextToken() != st.TT_EOF) {
					if (st.ttype == st.TT_WORD || st.ttype == '"') {
						IPRMClass prmClass = PRMClass.newInstance(prm, st.sval);
						prm.addPRMClass(prmClass);
						if (st.nextToken() != st.TT_EOF) {
							if (st.ttype == '(') {
								this.handleColumns(st, prmClass);
							}
						}
					}
				}
			}
		}

		
	}
	
	/**
	 * Handles the column at CREATE TABLE statement.
	 * It assumes the st is pointing to a "(" (open parenthesis)
	 * and st is going to point to a ")" (closing parenthesis)
	 * after execution.
	 * @param st
	 * @param prmClass
	 * @return the generated column
	 * @throws IOException 
	 */
	protected IAttributeDescriptor handleColumns(StreamTokenizer st, IPRMClass prmClass) throws IOException {
		
		IAttributeDescriptor ret = null;
		
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == st.TT_WORD || st.ttype == '"') {
				// extract attribute
				ret = AttributeDescriptor.newInstance(prmClass, st.sval);
				// TODO solve types (currently, only strings are available)
				if (st.nextToken() == st.TT_WORD) {
					// solve type
					if (st.nextToken() == '(') {
						if (st.nextToken() == st.TT_NUMBER) {
							// solve size
							if (st.nextToken() != ')') {
								// invalid close parentheses
								System.err.println("Invalid column size format (parentheses not closed). Type = " + st.ttype 
										+ ", number = " + st.nval 
										+ ", string = " + st.sval 
										+ ", char = " + (char)st.ttype
										+ ", line = " + st.lineno());
								st.pushBack();
							}
						} else {
							// invalid size
							System.err.println("Invalid column size format. Type = " + st.ttype 
									+ ", number = " + st.nval 
									+ ", string = " + st.sval 
									+ ", char = " + (char)st.ttype
									+ ", line = " + st.lineno());
							st.pushBack();
						}
					} else { // this is not a open-parenthesis
						// no size
					}
					// solve mandatory
					if (st.nextToken() == st.TT_WORD && "not".equalsIgnoreCase(st.sval)) {
						// this is a mandatory field
						if (st.nextToken() == st.TT_WORD && "null".equalsIgnoreCase(st.sval)) {
							ret.setMandatory(true);
						} else {
							// invalid mandatory statement
							System.err.println("Invalid mandatory statement. Type = " + st.ttype 
									+ ", number = " + st.nval 
									+ ", string = " + st.sval 
									+ ", char = " + (char)st.ttype
									+ ", line = " + st.lineno());
							st.pushBack();
						}
					} else {
						// this is not a mandatory field
						st.pushBack();
					}
				}
			} else if (st.ttype == ',') {
				continue;
			} else if (st.ttype == ')') {
				break;
			}
		}
		return ret;
	}
	/**
	 * Handles the ALTER TABLE statement. It assumes st is pointing
	 * to ALTER token. At the end, st is going to be pointing at ';'.
	 * @param st
	 * @param prm
	 * @throws IOException 
	 */
	protected void handleAlterTable(StreamTokenizer st, IPRM prm) throws IOException {
		
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == ';') {
				break;
			} else if (st.ttype == st.TT_WORD && "TABLE".equalsIgnoreCase(st.sval)) {
				// this is alter table command
				st.nextToken();
				if (st.ttype == '"' || st.ttype == st.TT_WORD) {
					// extract table name
					IPRMClass prmClass = prm.findPRMClassByName(st.sval);
					if (prmClass != null) {
						if (st.nextToken() == st.TT_WORD && "ADD".equalsIgnoreCase(st.sval)
								&& st.nextToken() == st.TT_WORD && "CONSTRAINT".equalsIgnoreCase(st.sval)) {
							// this is a ADD CONSTRAINT statement
							// extract constraint name
							if (st.nextToken() == st.TT_WORD) {
								String constraintName = st.sval;
								if (st.nextToken() == st.TT_WORD) {
									if ("PRIMARY".equalsIgnoreCase(st.sval)) {
										// handle primary key
										this.handlePrimaryKey(st, prmClass, constraintName);
									} else if ("FOREIGN".equalsIgnoreCase(st.sval)) {
										// handle foreign key
										this.handleForeignKey(st, prmClass, constraintName);
									} else if ("CHECK".equalsIgnoreCase(st.sval)) {
										// handle attribute's possible values
										this.handleCheck(st, prmClass, constraintName);
									}
								}
							}
						} else {
							// invalid add constraint statement
							System.err.println("Invalid ADD CONSTRAINT command. Type = " + st.ttype 
									+ ", number = " + st.nval 
									+ ", string = " + st.sval 
									+ ", char = " + (char)st.ttype
									+ ", line = " + st.lineno());
						}
					} else {
						// this is an invalid table name
						System.err.println("Invalid table name at ALTER TABLE command. Type = " + st.ttype 
								+ ", number = " + st.nval 
								+ ", string = " + st.sval 
								+ ", char = " + (char)st.ttype
								+ ", line = " + st.lineno());
						st.pushBack();
					}
				} else {
					st.pushBack();
				}
			}
		}
	}
	
	/**
	 * Handles the check statement on "alter table add constraint" statement.
	 * st will point to "check" command and it will be pointing to the token before the ';' after
	 * execution.
	 * @param st
	 * @param prmClass
	 * @param constraintName
	 * @throws IOException 
	 */
	protected void handleCheck(StreamTokenizer st, IPRMClass prmClass,
			String constraintName) throws IOException {

		if (st.nextToken() == '(') {
			// extract attribute name
			st.nextToken();
			if (st.ttype == '"' || st.ttype == st.TT_WORD) {
				IAttributeDescriptor attribute = prmClass.findAttributeDescriptorByName(st.sval);
				if (attribute == null) {
					// this is an invalid attribute
					System.err.println("Invalid check statement for attribute. Type = " + st.ttype 
							+ ", number = " + st.nval 
							+ ", string = " + st.sval 
							+ ", char = " + (char)st.ttype
							+ ", line = " + st.lineno());
					// go to next statement
					while (st.nextToken() != st.TT_EOF) {
						if (st.ttype == ';') {
							st.pushBack();
							return;
						}
					}
				}
				// extract IN statement
				st.nextToken();
				if (st.ttype != st.TT_WORD || !"IN".equalsIgnoreCase(st.sval)) {
					// This is not a "IN" statement. ignore it and go to next statement
					while (st.nextToken() != st.TT_EOF) {
						if (st.ttype == ';') {
							st.pushBack();
							return;
						}
					}
				}
				if (st.nextToken() == '(') {
					while(st.nextToken() != st.TT_EOF) {
						if (st.ttype == '\'' || st.ttype == st.TT_WORD) {
							// add possible value
							attribute.appendState(st.sval);
							if (st.nextToken() != ',') {
								st.pushBack();	// if the next token is ')', the nest loop will break the loop
							} else {
								// there is more PK - the next loop will handle it
							}
						} else if (st.ttype == ')') {
							// end of statement
							if (st.nextToken() != ')') {
								// there must be 2 closing parenthesis
								System.err.println("There must be 2 closing parenthesis. Type = " + st.ttype 
										+ ", number = " + st.nval 
										+ ", string = " + st.sval 
										+ ", char = " + (char)st.ttype
										+ ", line = " + st.lineno());
							}
							return;
						} else if (st.ttype == ';') {
							// invalid - no close parenthesis
							System.err.println("No closing parenthesis found. Type = " + st.ttype 
									+ ", number = " + st.nval 
									+ ", string = " + st.sval 
									+ ", char = " + (char)st.ttype
									+ ", line = " + st.lineno());
							st.pushBack();
							break;
						}
						
					}
					return;	// OK
				}
				// If the execution reaches this code, this is an invalid "IN" statement. ignore it and go to next statement
				while (st.nextToken() != st.TT_EOF) {
					if (st.ttype == ';') {
						st.pushBack();
						return;
					}
				}
				
			}
		}
		// this is an invalid check statement
		System.err.println("Invalid primary key statement. Type = " + st.ttype 
				+ ", number = " + st.nval 
				+ ", string = " + st.sval 
				+ ", char = " + (char)st.ttype
				+ ", line = " + st.lineno());
		st.pushBack();
	}
	
	/**
	 * Handles the foreign key statement on "alter table add constraint" statement.
	 * st will point to "foreign" command and it will be pointing to the token before the ';' after
	 * execution.
	 * @param st
	 * @param prmClass
	 * @param constraintName
	 * @throws IOException 
	 */
	protected void handleForeignKey(StreamTokenizer st, IPRMClass prmClass,
			String constraintName) throws IOException {
		// create foreign key
		IForeignKey fk = ForeignKey.newInstance();
		fk.setName(constraintName);
		fk.setClassFrom(prmClass);
		if (!prmClass.getForeignKeys().contains(fk)) {
			prmClass.getForeignKeys().add(fk);
		}

		if (st.nextToken() == st.TT_WORD && "KEY".equalsIgnoreCase(st.sval)) {
			if (st.nextToken() == '(') {
				while(st.nextToken() != st.TT_EOF) {
					if (st.ttype == '"' || st.ttype == st.TT_WORD) {
						// extract the attribute which is a FK
						IAttributeDescriptor fkAttribute = prmClass.findAttributeDescriptorByName(st.sval);
						if (fkAttribute != null) {
							fk.getKeyAttributesFrom().add(fkAttribute);
							if (st.nextToken() != ',') {
								st.pushBack();	// if the next token is ')', the nest loop will break the loop
							} else {
								// there is more PK - the next loop will handle it
							}
						}
					} else if (st.ttype == ')') {
						// references statement
						if (st.nextToken() == st.TT_WORD && "REFERENCES".equalsIgnoreCase(st.sval)) {
							st.nextToken();
							if (st.ttype == '"' || st.ttype == st.TT_WORD) {
								// extract referenced class
								IPRMClass prmClassTo = prmClass.getPRM().findPRMClassByName(st.sval);
								if (prmClassTo != null) {
									fk.setClassTo(prmClassTo);
									// extract referenced primary keys
									while(st.nextToken() != st.TT_EOF) {
										if (st.ttype == '"' || st.ttype == st.TT_WORD) {
											// extract the attribute which is a PK
											IAttributeDescriptor pkAttribute = prmClassTo.findAttributeDescriptorByName(st.sval);
											if (pkAttribute != null) {
												fk.getKeyAttributesTo().add(pkAttribute);
												if (st.nextToken() != ',') {
													st.pushBack();	// if the next token is ')', the nest loop will break the loop
												} else {
													// there is more PK - the next loop will handle it
												}
											}
										} else if (st.ttype == ')') {
											// end of statement
											return;
										} else if (st.ttype == ';') {
											// invalid - no close parenthesis
											// by breaking, it will print an error message and push back st
											break;
										}
										
									}
								}
							}
						}
					} else if (st.ttype == ';') {
						// invalid - no close parenthesis
						// by breaking, it will print an error message and push back st
						break;
					}
					
				}
				return;	// OK
			}
		}
		// this is an invalid foreign key statement
		System.err.println("Invalid foreign key statement. Type = " + st.ttype 
				+ ", number = " + st.nval 
				+ ", string = " + st.sval 
				+ ", char = " + (char)st.ttype
				+ ", line = " + st.lineno());
		st.pushBack();
		prmClass.getForeignKeys().remove(fk);
	}
	
	/**
	 * Handles the primary key statement on "alter table add constraint" statement.
	 * st will point to "primary" command and it will be pointing to the token before the ';' after
	 * execution.
	 * @param st
	 * @param prmClass
	 * @param constraintName
	 * @throws IOException 
	 */
	protected void handlePrimaryKey(StreamTokenizer st, IPRMClass prmClass,
			String constraintName) throws IOException {
		if (st.nextToken() == st.TT_WORD && "KEY".equalsIgnoreCase(st.sval)) {
			if (st.nextToken() == '(') {
				while(st.nextToken() != st.TT_EOF) {
					if (st.ttype == '"' || st.ttype == st.TT_WORD) {
						// set attribute as PK
						IAttributeDescriptor pk = prmClass.findAttributeDescriptorByName(st.sval);
						if (pk != null) {
							// set as PK
							pk.setPrimaryKey(true);
							pk.setMandatory(true);
							prmClass.setPrimaryKeyName(constraintName);
							if (st.nextToken() != ',') {
								st.pushBack();	// if the next token is ')', the nest loop will break the loop
							} else {
								// there is more PK - the next loop will handle it
							}
						} else {
							// invalid attribute name
							System.err.println("Invalid PK name found. Type = " + st.ttype 
									+ ", number = " + st.nval 
									+ ", string = " + st.sval 
									+ ", char = " + (char)st.ttype
									+ ", line = " + st.lineno());
							return;
						}
					} else if (st.ttype == ')') {
						// end of statement
						return;
					} else if (st.ttype == ';') {
						// invalid - no close parenthesis
						// by breaking, it will print an error message and push back st
						break;
					}
					
				}
				return;	// OK
			}
		}
		// this is an invalid primary key statement
		System.err.println("Invalid primary key statement. Type = " + st.ttype 
				+ ", number = " + st.nval 
				+ ", string = " + st.sval 
				+ ", char = " + (char)st.ttype
				+ ", line = " + st.lineno());
		st.pushBack();
	}
	/**
	 * Handles the INSERT INTO statement. It assumes st is pointing
	 * to INSERT token. At the end, st is going to be pointing at ';'.
	 * @param st
	 * @param prm
	 * @throws IOException 
	 */
	protected void handleInsertInto(StreamTokenizer st, IPRM prm) throws IOException {
		
		if (st.nextToken() == st.TT_WORD && "INTO".equalsIgnoreCase(st.sval)) {
			if (st.nextToken() == '"' || st.ttype == st.TT_WORD) {
				// extract class name
				IPRMClass prmClass = prm.findPRMClassByName(st.sval);
				if (prmClass != null) {
					// prepare PRM Object
					IPRMObject prmObject = PRMObject.newInstance(prmClass);
					// store the attribute names in order
					List<IAttributeDescriptor> attributes = new ArrayList<IAttributeDescriptor>();
					// read attributes
					if (st.nextToken() == '(') {
						while (st.nextToken() != st.TT_EOF) {
							if (st.ttype == '"' || st.ttype == st.TT_WORD) {
								// obtain attribute
								IAttributeDescriptor attribute = prmClass.findAttributeDescriptorByName(st.sval);
								if (attribute !=  null) {
									attributes.add(attribute);
									if (st.nextToken() != ',') {
										// there is no more attributes
										st.pushBack();
									} else {
										// the next loop will handle the other attribute
									}
								} else {
									System.err.println("Invalid attribute found. Type = " + st.ttype 
											+ ", number = " + st.nval 
											+ ", string = " + st.sval 
											+ ", char = " + (char)st.ttype
											+ ", line = " + st.lineno());
								}
							} else if (st.ttype == ')') {
								// no more attributes
								break;
							} else if (st.ttype == ';') {
								System.err.println("Unnexpected end of INSERT INTO reached. Type = " + st.ttype 
										+ ", number = " + st.nval 
										+ ", string = " + st.sval 
										+ ", char = " + (char)st.ttype
										+ ", line = " + st.lineno());
								return;
							}
						}
						if (st.nextToken() == st.TT_WORD && "VALUES".equalsIgnoreCase(st.sval)) {
							if (st.nextToken() == '(') {
								// extract values, by attributes
								for (IAttributeDescriptor attribute : attributes) {
									if (st.nextToken() == '\'' || st.ttype ==st.TT_WORD || st.ttype == st.TT_NUMBER) {
										// add value (it may be a number or a string)
										IAttributeValue value = AttributeValue.newInstance(prmObject, attribute);
										value.setValue((st.ttype == st.TT_NUMBER)?(Double.toString(st.nval)):st.sval);
										st.nextToken();
										if (st.ttype == ',') {
											continue;
										} else if (st.ttype == ')') {
											// end of statement. Move cursor until we find a ';' and return
											while (st.nextToken() != st.TT_EOF) {
												if (st.ttype == ';') {
													return;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// if the execution reaches this code, there is a problem
		System.err.println("Invalid INSERT INTO found. Type = " + st.ttype 
				+ ", number = " + st.nval 
				+ ", string = " + st.sval 
				+ ", char = " + (char)st.ttype
				+ ", line = " + st.lineno());
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == ';') {
				break;
			}
		}
	}
	
	/**
	 * Sets up the stream tokenizer used by {@link #load(File)}
	 * @param file
	 * @return
	 */
	protected StreamTokenizer setUpStreamTokenizer(File file) {
		StreamTokenizer st = null;
		try {
			st = new StreamTokenizer(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		st.wordChars('A', 'z');
		st.wordChars('.', '.');
		st.wordChars('_', '_');
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.quoteChar('"');
		st.quoteChar('\'');		
		st.eolIsSignificant(false);
		st.slashStarComments(true);
//		st.ordinaryChar(';');
		return st;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File arg0, Graph arg1) throws IOException {
		
		// initial assertives
		if (arg0 == null) {
			throw new NullPointerException("File == null");
		}
		if (arg1 == null) {
			throw new NullPointerException("PRM == null");
		}
		if (!(arg1 instanceof IPRM)) {
			throw new IllegalArgumentException("!(PRM instanceof unbbayes.prs.prm.IPRM)");
		}
		
		// extract PRM
		IPRM prm = (IPRM)arg1;
		
		// create output stream for .ubf files
		PrintStream out = new PrintStream(new FileOutputStream(arg0));
		
		// initial comment
		out.println("/* VERSION = 0.0.1 ORACLE-LIKE */");
		out.println("/* Non standard UnBBayes-PRM SQL script file. */");
		out.println("/* This file was generated by UnBBayes-PRM plugin on " + new Date().toString() + " */");
		out.println("/* You may change the order of the statements, but please, do not change the statements themselves. */");
		out.println();
		
		for (IPRMClass prmClass : prm.getIPRMClasses()) {
			// print table
			/*
			 * CREATE TABLE "table" (
			 * 		"attribute1" VARCHAR2(300)
			 * 		"attribute1" VARCHAR2(300) not null
			 */
			
			out.println();
			out.println("CREATE TABLE \"" + prmClass.getName() + "\" (");
			
			// store PK
			List<String> pkNames = new ArrayList<String>();
			
			// print columns
			for (Iterator<IAttributeDescriptor> it = prmClass.getAttributeDescriptors().iterator(); it.hasNext(); ) {
				IAttributeDescriptor attribute = it.next();
				
				// columns. The types are string (varchar2)
				out.println("\t \"" + attribute.getName() + "\" \t VARCHAR2(300) \t " 
						+ (attribute.isMandatory()?"not null":"") + (it.hasNext()?",":""));
				
				// store name if this is a PK
				if (attribute.isPrimaryKey()) {
					pkNames.add(attribute.getName());
				}
				
			}
			out.println(");");
			
			// add constraint for PK
			/*
			 * ALTER TABLE "table" ADD CONSTRAINT primaryKeyName PRIMARY KEY ("attribute");
			 */
			out.print("ALTER TABLE \"" + prmClass.getName() + "\" ADD CONSTRAINT " + prmClass.getPrimaryKeyName() + " PRIMARY KEY (");
			for (Iterator<String> it = pkNames.iterator() ; it.hasNext() ; ) {
				String pkName = it.next();
				out.print("\"" + pkName + "\"");
				if (it.hasNext()) {
					out.print(", ");
				}
			}
			out.println(");");

			// attribute's possible values:
			/*
			 * ALTER TABLE "x" ADD CONSTRAINT constraint_name { CHECK ("column_name" IN ('NEW YORK', 'BOSTON', 'CHICAGO'))};
			 */
			for (IAttributeDescriptor attribute : prmClass.getAttributeDescriptors()) {
				// only add possible values for potential random variables (i.e. ignore mandatory fields)
				if (!attribute.isMandatory()) {
					out.print("ALTER TABLE \"" + prmClass.getName() + "\" ADD CONSTRAINT CK_" + attribute.getName() 
							+ " CHECK ( \"" + attribute.getName() + "\" IN (");
					for (int i = 0; i < attribute.getStatesSize(); i++) {
						out.print("'" + attribute.getStateAt(i) + "'");
						if (i + 1 < attribute.getStatesSize()) {
							out.print(", ");
						}
					}
					out.println("));");
				}
			}
			
			
			// store FK
			/*
			 * ALTER TABLE "table" ADD CONSTRAINT fkname FOREIGN KEY ("foreignKeyAttribute") REFERENCES "table2" ("primaryKeyAttribute");
			 */
//			out.println();
//			out.println("/* Storing foreign keys */");
//			out.println();
			for (IForeignKey fk : prmClass.getForeignKeys()) {
				out.println();
				out.print("ALTER TABLE \"" 
						+ fk.getClassFrom().getName() 
						+ "\" ADD CONSTRAINT " 
						+ fk.getName()
						+ " FOREIGN KEY (");
				for (Iterator<IAttributeDescriptor> it = fk.getKeyAttributesFrom().iterator(); it.hasNext(); ) {
					out.print("\""+ it.next().getName() + "\"");
					if (it.hasNext()) {
						out.print(", ");
					}
				}
				out.print(")  REFERENCES \""
						+ fk.getClassTo().getName()
						+ "\" (");
				for (Iterator<IAttributeDescriptor> it = fk.getKeyAttributesTo().iterator(); it.hasNext(); ) {
					out.print("\""+ it.next().getName() + "\"");
					if (it.hasNext()) {
						out.print(", ");
					}
				}
				out.println(");");
			}
		}
		
		
		// TODO store dependency as table (2 tables: dependency chain and probability value?)

		/* TODO
		 * chain:
		 * 		ID (number PK), chainID (number), columnFrom (string), columnTo (string), isInverse (bool), fkName (string), aggregate(string) 
		 * probability
		 * 		ID (number PK), chainID (number FK to chain), value (number 10,4 = float)
		 * 
		 * Insert data too
		 */
		
//		out.println();
//		out.println("/* Storing data entries */");
//		out.println();
		// store data
		for (IPRMClass prmClass : prm.getIPRMClasses()) {
			for (IPRMObject prmObject : prmClass.getPRMObjects()) {
				// convert a key set to a key list, in order to preserve the order
				List<IAttributeDescriptor> keyList = new ArrayList<IAttributeDescriptor>(prmObject.getAttributeValueMap().keySet());

				/*
				 * INSERT INTO "CONFIDENCIALIDADE_INFORMACAO" (COD_CONFIDENCIALIDADE_INFO, TXT_DESCRICAO) VALUES (1 , 'Confidencial');	
				 */
				out.print("INSERT INTO \"" + prmClass.getName() + "\" (");
				for (Iterator<IAttributeDescriptor> it = keyList.iterator(); it.hasNext(); ) {
					out.print(it.next().getName() + (it.hasNext()?", ":""));
				}
				out.print(") VALUES (");
				for (Iterator<IAttributeDescriptor> it = keyList.iterator(); it.hasNext(); ) {
					out.print("'" + prmObject.getAttributeValueMap().get(it.next()).getValue()+ "'" + (it.hasNext()?", ":""));
				}
				out.println(");");
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#setName(java.lang.String)
	 */
	public void setName(String arg0) {
		this.name = arg0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		//extract file extension
		String fileExtension = null;
		try {
			int index = file.getName().lastIndexOf(".");
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		// compare file extension
		for (String ext : this.getSupportedFileExtensions(isLoadOnly)) {
			if (ext.equalsIgnoreCase(fileExtension)) {
				return true;
			}
		}
		return false;
	}

}
