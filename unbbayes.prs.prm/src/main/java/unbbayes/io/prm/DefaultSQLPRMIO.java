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
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IForeignKey;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMClass;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.PRM;
import unbbayes.prs.prm.PRMClass;
import unbbayes.util.Debug;

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

		Debug.println(this.getClass(), "CREATE TABLE, in");
		
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
		// TODO Auto-generated method stub

		Debug.println(this.getClass(), "CREATE TABLE, out");
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Handles the column at CREATE TABLE statement
	 * @param st
	 * @param prmClass
	 * @return the generated column
	 * @throws IOException 
	 */
	protected IAttributeDescriptor handleColumns(StreamTokenizer st, IPRMClass prmClass) throws IOException {
		Debug.println(this.getClass(), "Handle columns in");
		while (st.nextToken() != st.TT_EOF) {
			// TODO Auto-generated method stub
			if (st.ttype == ',') {
				Debug.println(this.getClass(), "Handling next column");
				continue;
			} else if (st.ttype == ')') {
				Debug.println(this.getClass(), "Column over");
				break;
			}
		}
		Debug.println(this.getClass(), "Handle columns out");
		return null;
	}
	/**
	 * Handles the ALTER TABLE statement. It assumes st is pointing
	 * to ALTER token. At the end, st is going to be pointing at ';'.
	 * @param st
	 * @param prm
	 * @throws IOException 
	 */
	protected void handleAlterTable(StreamTokenizer st, IPRM prm) throws IOException {
		
		Debug.println(this.getClass(), "ALTER TABLE, in");
		
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == ';') {
				break;
			}
		}
		// TODO Auto-generated method stub

		Debug.println(this.getClass(), "ALTER TABLE, out");
	}
	
	/**
	 * Handles the INSERT INTO statement. It assumes st is pointing
	 * to INSERT token. At the end, st is going to be pointing at ';'.
	 * @param st
	 * @param prm
	 * @throws IOException 
	 */
	protected void handleInsertInto(StreamTokenizer st, IPRM prm) throws IOException {

		Debug.println(this.getClass(), "INSERT INTO, in");
		
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype == ';') {
				break;
			}
		}
		// TODO Auto-generated method stub

		Debug.println(this.getClass(), "INSERT INTO, out");
		// TODO Auto-generated method stub
		
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
			 * ALTER TABLE "table" ADD CONSTRAINT PK_table PRIMARY KEY ("attribute");
			 */
			out.print("ALTER TABLE \"" + prmClass.getName() + "\" ADD CONSTRAINT PK_" + prmClass.getName() + " PRIMARY KEY (");
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
		
		out.println();
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
