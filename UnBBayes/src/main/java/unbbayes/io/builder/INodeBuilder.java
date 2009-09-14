/**
 * 
 */
package unbbayes.io.builder;

/**
 * 
 * A builder for Nodes, used by the I/O classes and/or Network builders in order to generate
 * a new Node.
 * This is useful in order to re-use I/O classes to load specific subclasses of Nodes,
 * instead of hard-coding Node types.
 * @author Shou Matsumoto
 *
 */
public interface INodeBuilder {
	
	/**
	 * Builds a new instance of Node
	 * @return instance of Node
	 */
	public unbbayes.prs.Node buildNode();
}
