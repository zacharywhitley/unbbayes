/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.stanford.smi.protegex.owl.model.RDFProperty;

/**
 * This is a data transfer object for OWL/RDF properties
 * @author Shou Matsumoto
 *
 */
public class OWLPropertyDTO implements Transferable {

	private Collection<RDFProperty> rdfProperties;
	public static final DataFlavor[] DEFAULT_DATA_FLAVORS = {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "OWLPropertyDTO")};
	
	/**
	 * Default constructor is protected to allow inheritance
	 */
	protected OWLPropertyDTO() {
		this.rdfProperties = new HashSet<RDFProperty>();
	}
	
	/**
	 * Default constructor method initializing fields
	 * @param rdfProperties
	 * @return
	 */
	public static OWLPropertyDTO newInstance(Collection<RDFProperty> rdfProperties) {
		OWLPropertyDTO ret = new OWLPropertyDTO ();
		ret.setRdfProperties(rdfProperties);
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (this.isDataFlavorSupported(flavor)) {
			return this.getRdfProperties();
		}
		throw new UnsupportedFlavorException(flavor);
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return DEFAULT_DATA_FLAVORS;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (int i = 0; i < this.getTransferDataFlavors().length; i++) {
			if (this.getTransferDataFlavors()[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the rdfProperties
	 */
	public Collection<RDFProperty> getRdfProperties() {
		return rdfProperties;
	}

	/**
	 * @param rdfProperties the rdfProperties to set
	 */
	public void setRdfProperties(Collection<RDFProperty> rdfProperties) {
		this.rdfProperties = rdfProperties;
	}


}
