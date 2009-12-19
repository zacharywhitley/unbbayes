/**
 * 
 */
package unbbayes.io.oobn.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.io.NetIO;
import unbbayes.io.builder.IProbabilisticNetworkBuilder;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.oobn.OOBNIOException;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.io.oobn.builder.DefaultOOBNClassBuilder;
import unbbayes.io.oobn.builder.DefaultPrivateOOBNNodeGraphicalWrapperBuilder;
import unbbayes.io.oobn.builder.IOOBNClassBuilder;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.exception.OOBNException;
import unbbayes.prs.oobn.impl.DefaultOOBNClass;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import unbbayes.util.Debug;

/**
 * 
 * I/O routines for OOBN
 * @author Shou Matsumoto
 *
 */
public class DefaultOOBNIO extends NetIO implements IObjectOrientedBayesianNetworkIO  {

	/**
	 * The file extension assumed by this class when recursivelly loading class dependency.
	 * This class will suppose the dependency to be loaded will have filename as
	 * [CLASSNAME]+ "." +[FILE_EXTENSION]
	 */
	public static final String FILE_EXTENSION = "oobn";
	
//	public static final String[] SUPPORTED_EXTENSIONS_LOAD = {FILE_EXTENSION , "net"};
	public static final String[] SUPPORTED_EXTENSIONS = {FILE_EXTENSION};
	
//	private NetIO netIO = null;
	private IObjectOrientedBayesianNetwork oobn = null;
	
	/**
	 * Stores the name and the class of dependencies already loaded
	 */
	private Map<String, IOOBNClass> classNameToClassMap = null;
	
	
	/** Load resource file from this package */
	private static ResourceBundle resources = ResourceBundle.getBundle(
			unbbayes.io.oobn.resources.OOBNIOResources.class.getName());
	
	/**
	 * Default constructor
	 */
	protected DefaultOOBNIO() {
//		this.netIO = new NetIO();
		this.oobn = ObjectOrientedBayesianNetwork.newInstance("");
		this.setClassNameToClassMap(new HashMap<String, IOOBNClass>());
	}
	
//	/**
//	 * Default constructor method
//	 * @return a new instance
//	 */
//	public static DefaultOOBNIO newInstance() {
//		return new DefaultOOBNIO();
//	}

	
//	/**
//	 * Constructor method indicating the netIO to use
//	 * @param netIO
//	 * @return a new instance
//	 */
//	public static DefaultOOBNIO newInstance(NetIO netIO) {
//		DefaultOOBNIO ret = new DefaultOOBNIO();
//		ret.setNetIO(netIO);
//		return ret;
//	}
	
	/**
	 * Constructor method indicating the OOBN to use
	 * @param oobn : OOBN to be managed by this IO. It may be an empty OOBN.
	 * @return a new instance
	 */
	public static DefaultOOBNIO newInstance(IObjectOrientedBayesianNetwork oobn) {
		DefaultOOBNIO ret = new DefaultOOBNIO();
		ret.setOobn(oobn);
		return ret;
	}
	
	
//	/**
//	 * Constructor method indicating the netIO and OOBN to use
//	 * @param netIO
//	 * @param oobn
//	 * @return a new instance
//	 */
//	public static DefaultOOBNIO newInstance(NetIO netIO, IObjectOrientedBayesianNetwork oobn) {
//		DefaultOOBNIO ret = new DefaultOOBNIO();
//		ret.setNetIO(netIO);
//		ret.setOobn(oobn);
//		return ret;
//	}
	

//	/* (non-Javadoc)
//	 * @see unbbayes.io.BaseIO#loadMSBN(java.io.File)
//	 */
//	public SingleAgentMSBN loadMSBN(File input) throws LoadException,
//			IOException {
//		// Why BaseIO should be aware of MSBN I/O implementation??? It should be done by another I/O class!!
//		Debug.println(this.getClass(), "An extremely horrible anti-pattern is forced by superclass or interface." 
//									  + this.getClass() + " refuses to realize such bizarre implementation.");
//		throw new IllegalArgumentException(
//			  new NoSuchMethodException(
//					  "No implementation of " + "SingleAgentMSBN loadMSBN(File input)" + " by " + this.getClass()));
//	}

	

//	/* (non-Javadoc)
//	 * @see unbbayes.io.BaseIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
//	 */
//	public void saveMSBN(File output, SingleAgentMSBN net)
//			throws FileNotFoundException {
//		// Why BaseIO should be aware of MSBN I/O implementation??? It should be done by another I/O class!!
//		Debug.println(this.getClass(), "An extremely horrible anti-pattern is forced by superclass or interface." 
//				  + this.getClass() + " refuses to realize such bizarre implementation.");
//		throw new IllegalArgumentException(
//			  new NoSuchMethodException(
//					  "No implementation of " + "saveMSBN(File output, SingleAgentMSBN net)" + " by " + this.getClass()));
//	}

	
	
	
	
	
//	/**
//	 * @param obj
//	 * @return
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	public boolean equals(Object obj) {
//		return netIO.equals(obj);
//	}
//
//	/**
//	 * @return
//	 * @see java.lang.Object#hashCode()
//	 */
//	public int hashCode() {
//		return netIO.hashCode();
//	}

	
	
	
	/**
	 * @see unbbayes.io.NetIO#load(java.io.File, unbbayes.io.builder.IProbabilisticNetworkBuilder)
	 * @param input
	 * @param networkBuilder
	 * @return
	 * @throws LoadException
	 * @throws IOException
	 * @see unbbayes.io.NetIO#load(java.io.File, unbbayes.io.builder.IProbabilisticNetworkBuilder)
	 */
	public DefaultOOBNClass load(File input,
			IProbabilisticNetworkBuilder networkBuilder) throws LoadException,
			IOException {
		
		DefaultOOBNClass ret = null;
		
		Debug.println(this.getClass(), "Loading multiple class is not implemented yet. Using default behavior...");
		
		// The name of class will be changed during net file loading, so the name can be initialized as anything,
		// but since this.load(input, ret, networkBuilder) can pass routine to superclass without setting network name,
		// let's initialize the name as the filename with no extension.
		int index = input.getName().lastIndexOf('.');
		String id = input.getName().substring(0, index);
		
		ret = (DefaultOOBNClass)networkBuilder.buildNetwork(id);
		
		this.load(input, ret, networkBuilder);
		
		return ret;
	}

	/**
	 * @param input
	 * @return 
	 * @throws LoadException
	 * @throws IOException
	 * @see unbbayes.io.NetIO#load(java.io.File)
	 */
	public Graph load(File input) throws LoadException,
			IOException {
		IProbabilisticNetworkBuilder builder = DefaultOOBNClassBuilder.newInstance();
		// set the class builder to build only private nodes
		builder.setProbabilisticNodeBuilder(DefaultPrivateOOBNNodeGraphicalWrapperBuilder.newInstance());
		this.load(input, builder);
		return (Graph)this.getOobn();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.NetIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws FileNotFoundException {
		this.save(output, net, true);
	}
	
	/**
	 * @param output
	 * @param net
	 * @param updateClassName : if set to true, this method will search an OOBN class inside {@link #getOobn()} 
	 * which {@link IOOBNClass#getNetwork()} matches "net"; if such class if found, it will attempt to change its
	 * OOBN class name to new file name, so that file name and class name remains identical.
	 * @throws FileNotFoundException
	 * @see unbbayes.io.NetIO#save(java.io.File, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	public void save(File output, Graph graph, boolean updateClassName)
			throws FileNotFoundException {
		
		SingleEntityNetwork net = (SingleEntityNetwork)graph;
		
		// updating class name
		if (updateClassName) {
			try {
				String newClassName = output.getName().substring(0, output.getName().lastIndexOf("."));
				for (IOOBNClass oobnClass : this.getOobn().getOOBNClassList()) {
					if (oobnClass.getNetwork().equals(net)) {
						oobnClass.setClassName(newClassName);
					}
				}
			} catch (Exception e) {
				Debug.println(this.getClass(), "Failed to update class name.", e);
			}
		}
		
		Debug.println(this.getClass(), "Saving multiple classes is not implemented yet. Using default behavior...");
		
		// list of ordinal nodes (nodes that may be stored as ordinal "node" declaration)
		List<OOBNNodeGraphicalWrapper> ordinalNodes = new ArrayList<OOBNNodeGraphicalWrapper>();
		
		// list of OOBN specific nodes (nodes that should be stored as "instance" declaration)
		List<OOBNNodeGraphicalWrapper> instanceNodes = new ArrayList<OOBNNodeGraphicalWrapper>();
		
		PrintStream stream = new PrintStream(new FileOutputStream(output));
		
//		IOOBNClass oobnClass = (IOOBNClass)net;
		
		// start class declaration
		
		stream.print("class ");
		stream.println(net.getName());
		stream.println("{");
		
		
		// fill header
		this.saveClassHeaderBody(stream, net);
		
		
		// extract ordinal (input/output/private) nodes and instance nodes
		// they are going to be used to fill node/instance declarations
		for (Node node : net.getNodes()) {
			if (node instanceof OOBNNodeGraphicalWrapper) {
				OOBNNodeGraphicalWrapper wrapper = (OOBNNodeGraphicalWrapper)node;
				if ( ( wrapper.getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE ) == 0 ) {
					// if node type is not instance-compatible, then add as ordinal node
					ordinalNodes.add(wrapper);
				} else if (wrapper.getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE) {
					// if node is exactly an instance type, add it as instance node
					instanceNodes.add(wrapper);
				} else {
					// the node is a instance input or instance output, which are inner nodes of instance nodes
					// we are not going to save them as ordinal declarations
				}
			} else {
				Debug.println(this.getClass(), node.getName() + " is not a OOBNNodeGraphicalWrapper");
			}
		}
		
		
		
		// start node declaration
		for (OOBNNodeGraphicalWrapper node : ordinalNodes) {
			this.saveNodeDeclaration(stream, node, net);
		}
		
		// start instance declaration
		for (OOBNNodeGraphicalWrapper instance : instanceNodes) {
			this.saveInstanceDeclaration(stream, instance, net);
		}
		
		
		// start potential declaration
		for (Node node : net.getNodes()) {
			if (node instanceof OOBNNodeGraphicalWrapper) {
				// if node type is not instance (nor derivate of instance node), then add potential
				if ( ( ((OOBNNodeGraphicalWrapper)node).getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE ) == 0 ) {
					this.savePotentialDeclaration(stream, node, net);
				}
			} else {
				Debug.println(this.getClass(), node.getName() + " is not a OOBNNodeGraphicalWrapper");
			}
		}
		
		
		// end class declaration
		stream.print("} ");
		stream.println("% class " + net.getName());
		
		
		stream.close();
	}

//	/**
//	 * @return
//	 * @see java.lang.Object#toString()
//	 */
//	public String toString() {
//		return netIO.toString();
//	}

//	/**
//	 * @return the netIO
//	 */
//	public NetIO getNetIO() {
//		return netIO;
//	}
//
//	/**
//	 * @param netIO the netIO to set
//	 */
//	public void setNetIO(NetIO netIO) {
//		this.netIO = netIO;
//	}

	/**
	 * @return the oobn
	 */
	public IObjectOrientedBayesianNetwork getOobn() {
		return oobn;
	}

	/**
	 * @param oobn the oobn to set
	 */
	public void setOobn(IObjectOrientedBayesianNetwork oobn) {
		this.oobn = oobn;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO#loadOOBN(java.io.File)
	 */
	public IObjectOrientedBayesianNetwork loadOOBN(File classFile) throws IOException {
		
		
		this.getOobn().setTitle(classFile.getName());
		try{
//			this.getOobn().getOOBNClassList().add((IOOBNClass)this.load(classFile));
			this.load(classFile);
		} catch (LoadException le) {
			throw new OOBNIOException(le);
		}
		return this.getOobn();
	}

	
	/**
	 * @return the classNameToClassMap
	 */
	public Map<String, IOOBNClass> getClassNameToClassMap() {
		return classNameToClassMap;
	}

	/**
	 * @param classNameToClassMap the classNameToClassMap to set
	 */
	public void setClassNameToClassMap(Map<String, IOOBNClass> classNameToClassMap) {
		this.classNameToClassMap = classNameToClassMap;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO#saveOOBNClass(java.io.File, unbbayes.prs.oobn.IOOBNClass)
	 */
	public void saveOOBNClass(File classFile, IOOBNClass oobnClass) throws IOException {
		
		String newClassName = classFile.getName().substring(0, classFile.getName().lastIndexOf("."));
		try{
			oobnClass.setClassName(newClassName);
		} catch (OOBNException oobne) {
			throw new OOBNIOException(oobne);
		}
		
		this.save(classFile, (SingleEntityNetwork)oobnClass.getNetwork(), false);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.io.NetIO#load(java.io.File, unbbayes.prs.bn.SingleEntityNetwork, unbbayes.io.builder.IProbabilisticNetworkBuilder)
	 */
	@Override
	protected void load(File input, SingleEntityNetwork net,
			IProbabilisticNetworkBuilder networkBuilder) throws IOException,
			LoadException {
		
		// list of names of all input nodes. We'll use them to set nodes as input after nodes are loaded
		List<String> inputNodesNames = new ArrayList<String>();
		
		// list of names of all output nodes. We'll use them to set nodes as output after nodes are loaded
		List<String> outputNodesNames = new ArrayList<String>();

		// map containing instance input nodes and names of their expected parents
		Map<OOBNNodeGraphicalWrapper, String> instanceInputParentMap = new HashMap<OOBNNodeGraphicalWrapper, String>();
		
		
		// setting up the stream tokenizer...
		BufferedReader r = new BufferedReader(new FileReader(input));
		StreamTokenizer st = new StreamTokenizer(r);
		this.setUpStreamTokenizer(st);

		// treat header
		
		getNext(st);
		if (st.sval.equals("class")) {
			// if header starts with "class", this is a class file. Extract class name
			getNext(st);
			if (st.ttype == st.TT_WORD) {
				// set the class name here
				net.setName(st.sval);
			} else {
				// the class came with no name specification... This is unexpected...
				throw new LoadException("NoClassNameDefinition");
			}
			// start analyzing header and fill list of input and ouput names
			this.loadClassHeader(st, net, inputNodesNames, outputNodesNames);
		} else {
			// this is not a class declaration. Maybe, a ordinal net declaration
			// let's stop treating the file with this class (which is not responsible for treating ordinal nets)
			// and pass it to superclass.
			// Unfortunatelly, if we use the superclass method using current network builder, it will work as every node are private,
			// so, let' call supermethod using DefaultOOBNClassBuilder (which generates output nodes as default)
			super.load(input, net, DefaultOOBNClassBuilder.newInstance());
			
			// add it to currently managed oobn.
			// since the currently managed oobn traces the already loaded classes, and is used by loadOOBN as a return value
			this.getOobn().getOOBNClassList().add((IOOBNClass)net);
			return;
		}

		// treat body
		
		while (getNext(st) != StreamTokenizer.TT_EOF) {
			
			// if declaration is "instance" type, treat it
			this.treatInstanceNodeDeclaration(input, st, net, (IOOBNClassBuilder)networkBuilder, networkBuilder, this.classNameToClassMap, instanceInputParentMap);
			
			// if declaration is "continuous node" type, treat it
			this.loadContinuousNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "node" type, treat it
			this.loadNodeDeclaration(st, net, networkBuilder);
			
			// if declaration is "potential" type, treat it
			this.loadPotentialDeclaration(st, net);
			
			// ignore other declarations
		}
		

		r.close();
		
		// since now all nodes are loaded, let's start changing node's type to input and/or output
		for (String outputName : outputNodesNames) {
			try{
				OOBNNodeGraphicalWrapper node = (OOBNNodeGraphicalWrapper)net.getNode(outputName);
				node.getWrappedNode().setType(IOOBNNode.TYPE_OUTPUT);
			} catch (ClassCastException cce) {
				// lets continue trying to change other nodes
				Debug.println(this.getClass(), "Could not change " + outputName + "'s oobn node type to output.", cce);
			} 
		}
		for (String inputName : inputNodesNames) {
			try{
				OOBNNodeGraphicalWrapper node = (OOBNNodeGraphicalWrapper)net.getNode(inputName);
				node.getWrappedNode().setType(IOOBNNode.TYPE_INPUT);
			} catch (ClassCastException cce) {
				// lets continue trying to change other nodes
				Debug.println(this.getClass(), "Could not change " + inputName + "'s oobn node type to input.", cce);
			}
		}
		
		// also, since all nodes are loaded, let's map the instance input's parents
		for (OOBNNodeGraphicalWrapper key : instanceInputParentMap.keySet()) {
			String parentName = instanceInputParentMap.get(key);
			try {
//				key.addParent(net.getNode(parentName));	
				net.addEdge(new Edge(net.getNode(parentName) , key));
			} catch (InvalidParentException ipe) {
				Debug.println(this.getClass(), "Error insertng instance input's parent: "
						+ key.getName() + " <- " + parentName , ipe);
			}
			
		}
		
		
		// I'm not sure if it is necessary to call the method below...
		this.setUpHierarchicTree(net);
		
		
		// add it to currently managed oobn.
		// since the currently managed oobn traces the already loaded classes
		this.getOobn().getOOBNClassList().add((IOOBNClass)net);
		
	}
	
	
	/**
	 * Treats new 3rd specification's header declarations
	 * @param st
	 * @param net
	 * @param inputNodesNames: ouptut parameter: list of names to be treated as input nodes
	 * @param outputNodesNames: ouptut parameter: list of names to be treated as output nodes
	 * @throws IOException
	 */
	protected void loadClassHeader( StreamTokenizer st, SingleEntityNetwork net
								  , List<String> inputNodesNames, List<String> outputNodesNames  )
								    throws IOException {
		getNext(st);
		if (st.sval.equals("{")) {
			getNext(st);
			while (!st.sval.equals("node") && !st.sval.equals("instance")) {
				if (st.sval.equals("inputs")) {
					// register inputs of this net
					// I cannot use getNext, because the specification does not use quoted text,
					// so, we have to trust that ';' stops statements
					while (getNextWithoutIgnoringChars(st, ';') == st.TT_WORD) {
						inputNodesNames.add(st.sval);
					}
				} else if (st.sval.equals("outputs")) {
					// register outputs of this net
					// I cannot use getNext, because the specification does not use quoted text,
					// so, we have to trust that ';' stops statements
					while (getNextWithoutIgnoringChars(st,';') == st.TT_WORD) {
						outputNodesNames.add(st.sval);
					}
				} else if (st.sval.equals("name")) {
					// avoid overwriting class name
				} else {
					// treat other declarations from net 2nd spec
					// I expect this method does not call getNext
					this.loadNetHeaderBody(st, net);
				}
				getNext(st);
			}		
			st.pushBack();
		}
	}

	/**
	 * Does the same as getNext, but doesn't ignore some chars
	 * @param st
	 * @param notToIgnore: chars not to be ignored
	 * @return
	 * @throws IOException
	 */
	protected int getNextWithoutIgnoringChars(StreamTokenizer st, char ... notToIgnore) throws IOException {
		do {
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_EOL) {
				super.lineno++;
			}
			for (char c : notToIgnore) {
				if (st.ttype == (int)c) {
					return st.ttype;
				}
			}
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}
	
	
	/**
	 * Treats new net 3rd specification for body (basically, "input" declaration)
	 * @param st
	 * @param net
	 * @param networkBuilder
	 * @param classNameToClassMap map containing already-inserted classes and its names
	 * @return map containing inner nodes and its parent's names
	 */
	protected void treatInstanceNodeDeclaration ( File originalClassFile, StreamTokenizer st, SingleEntityNetwork net
												, IOOBNClassBuilder classBuilder
												, IProbabilisticNetworkBuilder networkBuilder
												, Map<String, IOOBNClass> classNameToClassMap
												, Map<OOBNNodeGraphicalWrapper, String> instanceInputParentMap)
												  throws IOException, LoadException {
		
		
		
		if (st.sval.equals("instance")) {
			
			
			
			if (getNext(st) != st.TT_WORD) {
				throw new LoadException("-> ["+((super.lineno > st.lineno())?super.lineno:st.lineno()) + "] instance [???]");
			}
			String instanceName = st.sval; 	// extract instance node' name
			
			if (getNext(st) != st.TT_WORD) {
				throw new LoadException("-> ["+((super.lineno > st.lineno())?super.lineno:st.lineno()) + "] instance " + instanceName +" : [???] ");
			}
			String className = st.sval;	// extract class name
			
			Debug.println(this.getClass(), "Loading instance " + instanceName + " instanceof " + className );
			
			// extract class by classname
			IOOBNClass oobnClass = null;
			for (IOOBNClass currentClass : this.getOobn().getOOBNClassList()) {
				if (currentClass.getClassName().equals(className)) {
					oobnClass = currentClass;
					break;
				}
			}
			
			// if the class was not loaded before, load it recursively
			if (oobnClass == null) {
				// suppose file name is [CLASSNAME].oobn
				File input = new File(originalClassFile.getParent() , className + "." + FILE_EXTENSION);
				oobnClass = (IOOBNClass)classBuilder.buildNetwork(className);
				this.load(input, (SingleEntityNetwork)oobnClass.getNetwork(), networkBuilder);
				// load will also add the loaded new class to this.oobn
			}
			
			// create instance node using class we  just extracted
			OOBNNodeGraphicalWrapper instance = (OOBNNodeGraphicalWrapper)classBuilder.getInstanceNodeBuilder().buildInstanceNode(oobnClass);
			
			// rename instance name
			instance.setName(instanceName);
			
			while (getNextWithoutIgnoringChars(st, ';') != ';') {
				
				// start filling map of instance input node's parents
				
				String nodeName = st.sval;
				
				// find the node it refers (since the name it refers is the original one)
				OOBNNodeGraphicalWrapper instanceInputNode = null;
				for (OOBNNodeGraphicalWrapper inner : instance.getInnerNodes()) {
					if (inner.getWrappedNode().getOriginalClassNode().getName().equals(nodeName)) {
						instanceInputNode = inner;
						break;
					}
				}
				
				
				
				// extract the parent name
				if (this.getNext(st) != st.TT_WORD) {
					// there is no parent name declared within parent name mapping (this is an error)
					throw new LoadException(nodeName + " <- " + (char)st.ttype);
				}
				
				// assert instanceInputNode != null
				if (instanceInputNode != null) {
					instanceInputParentMap.put(instanceInputNode, st.sval);
				} else {
					Debug.println(this.getClass(), "An inconsistent instance input mapping was found: " + nodeName + " <- " + st.sval);
				}
				
				
			}
			
			// load instance output mapping
			for (getNext(st); !st.sval.equals("{") ; getNext(st)) {
				// ignore definition of original nodes, since they are automatic at UnBBayes OOBN implementation
				Debug.println(this.getClass(), "Ignoring instance output declaration: " + "[TTYPE = " + st.ttype + "] " + st.sval);
			}
			
			getNext(st);
			while (!st.sval.equals("}")) {
				this.loadNodeDeclarationBody(st, instance);
			}
			
			
			// add instance to network
			net.addNode(instance);
			
			
		}
		
		
	}

	
	/**
	 * Saves the "class [NAME] { [CONTENT_WRITTEN_BY_THIS_METHOD]" declaration until node/instance declaration is going to be saved
	 * Please, note that this method does not write "class" nor "[NAME]" nor "{"
	 * @param stream
	 * @param oobnClass
	 */
	protected void saveClassHeaderBody(PrintStream stream , SingleEntityNetwork net) {
		
		// lists to store input and output nodes
		List<OOBNNodeGraphicalWrapper> inputNodes = new ArrayList<OOBNNodeGraphicalWrapper>();
		List<OOBNNodeGraphicalWrapper> outputNodes = new ArrayList<OOBNNodeGraphicalWrapper>();
		
		// extract input and output nodes
		for (Node node : net.getNodes()) {
			if (node instanceof OOBNNodeGraphicalWrapper) {
				OOBNNodeGraphicalWrapper wrapper = (OOBNNodeGraphicalWrapper)node;
				if (wrapper.getWrappedNode().getType() == IOOBNNode.TYPE_INPUT) {
					inputNodes.add(wrapper);
				} else if (wrapper.getWrappedNode().getType() == IOOBNNode.TYPE_OUTPUT) {
					outputNodes.add(wrapper);
				}
			}
		}
		
		// save input declarations
		stream.print("\t inputs =  (");
		for (OOBNNodeGraphicalWrapper input : inputNodes) {
			stream.print(" ");
			stream.print(input.getName());
		}
		stream.println(" );");
		
		// save output declarations
		stream.print("\t outputs =  (");
		for (OOBNNodeGraphicalWrapper output : outputNodes) {
			stream.print(" ");
			stream.print(output.getName());
		}
		stream.println(" );");
		
		// save other declarations
		this.saveNetHeaderBody(stream, net);
		
		stream.println();
	}
	
	/**
	 * Stores the 
	 * "instance [NAME] : [CLASS] ( [LIST_OF_INPUT_INSTANTIATION] ; [LIST_OF_OUTPUT_INSTANTIATION] ) {[BODY]}" 
	 * declaration
	 * @param stream
	 * @param instance
	 * @param net
	 */
	protected void saveInstanceDeclaration (PrintStream stream , OOBNNodeGraphicalWrapper instance, SingleEntityNetwork net) {
		
		stream.print("\t instance ");
		stream.print(instance.getName());
		stream.print(" : ");
		stream.print(instance.getWrappedNode().getParentClass());
		
		stream.print(" (");
		
		// inner nodes' mapping
		this.saveInnerNodeMapping(stream, instance);
		
		stream.println(")");
		
		stream.println("\t {");
		
		// we only need to store position and label from instance nodes.
		this.saveNodeLabelAndPosition(stream, instance);
		
		stream.println("\t }");
		stream.println();
		
		
	}
	
	
	/**
	 * Stores the mappings of instances of input nodes and output nodes declared by instance nodes.
	 * 		
	 * 		[INPUT_NODE_NAME] = [PARENT_OF_INPUT] ... ; [OUTPUT_INSTANCE_NODE_NAME] = [ORIGINAL_OUTPUT_NODE_NAME_FROM_CLASS]...
	 * 
	 * 		Ex. ZoneMDPrev = C2; Zone_1_ZoneNature = ZoneNature, Zone_1_ZoneEShips = ZoneEShips, Zone_1_ZoneFShips = ZoneFShips, Zone_1_ZoneMD = ZoneMD
	 * 
	 * @param stream
	 * @param instance
	 */
	protected void saveInnerNodeMapping (PrintStream stream , OOBNNodeGraphicalWrapper instance) {

		// Lists of instance input and instance output
		List<OOBNNodeGraphicalWrapper> instanceInputs = new ArrayList<OOBNNodeGraphicalWrapper>();
		List<OOBNNodeGraphicalWrapper> instanceOutputs = new ArrayList<OOBNNodeGraphicalWrapper>();
		
		// extract instance input and output nodes
		for (OOBNNodeGraphicalWrapper inner : instance.getInnerNodes()) {
			if (inner.getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE_INPUT) {
				instanceInputs.add(inner);
			} else if (inner.getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE_OUTPUT) {
				instanceOutputs.add(inner);
			}
		}
		
		// start filling instance input's declarations
		
		for (Iterator<OOBNNodeGraphicalWrapper> iter = instanceInputs.iterator() ; iter.hasNext() ; ) {
			
			OOBNNodeGraphicalWrapper input = iter.next();
			
			if (input.getParents() == null || input.getParents().isEmpty()) {
				// ignore input parents with no parent (with no node to instantiate)
				continue;
			}
			stream.print(input.getWrappedNode().getOriginalClassNode().getName());
			stream.print(" = ");
			stream.print(input.getParents().get(0));	// we assume input instances have only 1 parent
			
			if (iter.hasNext()) {
				stream.print(", ");
			}
		}
		
		stream.print("; ");
		
		// start filling instance output's declarations
		
		for (Iterator<OOBNNodeGraphicalWrapper> iter = instanceOutputs.iterator() ; iter.hasNext() ; ) {
			
			OOBNNodeGraphicalWrapper output = iter.next();
			
			stream.print(output.getName());
			stream.print(" = ");
			stream.print(output.getWrappedNode().getOriginalClassNode().getName());
			
			if (iter.hasNext()) {
				stream.print(", ");
			}
		}

		// the last parenthesis is filled by upper caller method
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.NetIO#supportsExtension(java.lang.String)
	 */
	public boolean supports(String extension, boolean isLoadOnly) {
		for (String supported : this.getSupportedFileExtensions(isLoadOnly)) {
			if (supported.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.NetIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		return SUPPORTED_EXTENSIONS;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.NetIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return this.resources.getString("netFileFilterSaveOOBN");
	}
	
	
	
	
	
}
