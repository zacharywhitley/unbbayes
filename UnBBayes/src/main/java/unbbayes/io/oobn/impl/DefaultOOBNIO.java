/**
 * 
 */
package unbbayes.io.oobn.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import unbbayes.io.NetIO;
import unbbayes.io.builder.IProbabilisticNetworkBuilder;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.SaveException;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.io.oobn.builder.DefaultOOBNClassBuilder;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
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

	private NetIO netIO = null;
	private IObjectOrientedBayesianNetwork oobn = null;
	
	/**
	 * Default constructor
	 */
	protected DefaultOOBNIO() {
		this.netIO = new NetIO();
		this.oobn = ObjectOrientedBayesianNetwork.newInstance("");
	}
	
	/**
	 * Default constructor method
	 * @return a new instance
	 */
	public static DefaultOOBNIO newInstance() {
		return new DefaultOOBNIO();
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#loadMSBN(java.io.File)
	 */
	public SingleAgentMSBN loadMSBN(File input) throws LoadException,
			IOException {
		// Why BaseIO should be aware of MSBN I/O implementation??? It should be done by another I/O class!!
		Debug.println(this.getClass(), "An extremely horrible anti-pattern is forced by superclass or interface." 
									  + this.getClass() + " refuses to realize such bizarre implementation.");
		throw new IllegalArgumentException(
			  new NoSuchMethodException(
					  "No implementation of " + "SingleAgentMSBN loadMSBN(File input)" + " by " + this.getClass()));
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#saveMSBN(java.io.File, unbbayes.prs.msbn.SingleAgentMSBN)
	 */
	public void saveMSBN(File output, SingleAgentMSBN net)
			throws FileNotFoundException {
		// Why BaseIO should be aware of MSBN I/O implementation??? It should be done by another I/O class!!
		Debug.println(this.getClass(), "An extremely horrible anti-pattern is forced by superclass or interface." 
				  + this.getClass() + " refuses to realize such bizarre implementation.");
		throw new IllegalArgumentException(
			  new NoSuchMethodException(
					  "No implementation of " + "saveMSBN(File output, SingleAgentMSBN net)" + " by " + this.getClass()));
	}

	
	
	
	
	
	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return netIO.equals(obj);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return netIO.hashCode();
	}

	/**
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
		
		ProbabilisticNetwork net = netIO.load(input, networkBuilder);
		
		ret = (DefaultOOBNClass)net;
		
		return ret;
	}

	/**
	 * @param input
	 * @return
	 * @throws LoadException
	 * @throws IOException
	 * @see unbbayes.io.NetIO#load(java.io.File)
	 */
	public ProbabilisticNetwork load(File input) throws LoadException,
			IOException {
		return this.load(input, DefaultOOBNClassBuilder.newInstance());
	}

	/**
	 * @param output
	 * @param net
	 * @throws FileNotFoundException
	 * @see unbbayes.io.NetIO#save(java.io.File, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	public void save(File output, SingleEntityNetwork net)
			throws FileNotFoundException {
		Debug.println(this.getClass(), "Saving single class is not implemented yet. Using default behavior...");
		netIO.save(output, net);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return netIO.toString();
	}

	/**
	 * @return the netIO
	 */
	public NetIO getNetIO() {
		return netIO;
	}

	/**
	 * @param netIO the netIO to set
	 */
	public void setNetIO(NetIO netIO) {
		this.netIO = netIO;
	}

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
			this.getOobn().getOOBNClassList().add((IOOBNClass)this.load(classFile));
		} catch (LoadException le) {
			throw new IOException(le);
		}
		return this.getOobn();
	}
	
	
	
	

}
