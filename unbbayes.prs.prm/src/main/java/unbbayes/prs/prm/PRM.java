/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.prm.builders.IPRMClassBuilder;
import unbbayes.prs.prm.builders.PRMClassBuilder;

/**
 * Default implementation of IPRM
 * @author Shou Matsumoto
 *
 */
public class PRM implements IPRM {

	private String name = "PRM";
	private List<IPRMClass> prmClasses;
	

	private IPRMClassBuilder prmClassBuilder;
	
	private Map<String, Object> propertyMap = new HashMap<String, Object>();

	/**
	 *  At least one constructor is visible for subclasses
	 * to allow inheritance
	 */
	protected PRM() {
		// TODO Auto-generated constructor stub
		this.prmClasses = new ArrayList<IPRMClass>();
		this.prmClassBuilder = new PRMClassBuilder();
	}
	
	/**
	 * Default construction method using fields
	 * @param name
	 * @return
	 */
	public static PRM newInstance(String name) {
		PRM ret = new PRM();
		ret.name = name;
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRM#findPRMClassByName(java.lang.String)
	 */
	public IPRMClass findPRMClassByName(String name) {
		// just perform a linear search...
		
		for (IPRMClass prmClass : this.getIPRMClasses()) {
			if (prmClass.getName().equals(name)) {
				return prmClass;
			}
		}
		
		// nothing found
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRM#getIPRMClasses()
	 */
	public List<IPRMClass> getIPRMClasses() {
		return this.prmClasses;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRM#setIPRMClasses(java.util.List)
	 */
	public void setIPRMClasses(List<IPRMClass> prmClasses) {
		this.prmClasses = prmClasses;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#addEdge(unbbayes.prs.Edge)
	 */
	public void addEdge(Edge arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#addNode(unbbayes.prs.Node)
	 */
	public void addNode(Node arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#getEdges()
	 */
	public List<Edge> getEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#getNodeCount()
	 */
	public int getNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#getNodes()
	 */
	public ArrayList<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#hasEdge(unbbayes.prs.Node, unbbayes.prs.Node)
	 */
	public int hasEdge(Node arg0, Node arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#removeEdge(unbbayes.prs.Edge)
	 */
	public void removeEdge(Edge arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Graph#removeNode(unbbayes.prs.Node)
	 */
	public void removeNode(Node arg0) {
		// TODO Auto-generated method stub

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRM#addClass(unbbayes.prs.prm.IPRMClass)
	 */
	public void addPRMClass(IPRMClass prmClass) {
		this.getIPRMClasses().add(prmClass);
		if (!this.equals(prmClass.getPRM())) {
			prmClass.setPRM(this);
		}
		// TODO assure consistency
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRM#removeClass(unbbayes.prs.prm.IPRMClass)
	 */
	public void removePRMClass(IPRMClass prmClass) {
		this.getIPRMClasses().remove(prmClass);
		// TODO Assure consistency		
	}

	/**
	 * @return the prmClassBuilder
	 */
	public IPRMClassBuilder getPrmClassBuilder() {
		return prmClassBuilder;
	}

	/**
	 * @param prmClassBuilder the prmClassBuilder to set
	 */
	public void setPrmClassBuilder(IPRMClassBuilder prmClassBuilder) {
		this.prmClassBuilder = prmClassBuilder;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Graph#addProperty(java.lang.String, java.lang.Object)
	 */
	public void addProperty(String name, Object value) {
		getPropertyMap().put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Graph#removeProperty(java.lang.String)
	 */
	public void removeProperty(String name) {
		getPropertyMap().remove(name);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Graph#clearProperty()
	 */
	public void clearProperty() {
		getPropertyMap().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Graph#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return getPropertyMap().get(name);
	}

	/**
	 * @return the propertyMap
	 */
	protected Map<String, Object> getPropertyMap() {
		return this.propertyMap;
	}

	/**
	 * @param propertyMap the propertyMap to set
	 */
	protected void setPropertyMap(Map<String, Object> propertyMap) {
		this.propertyMap = propertyMap;
	}

	
	
}
