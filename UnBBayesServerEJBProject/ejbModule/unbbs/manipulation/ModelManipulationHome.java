package unbbs.manipulation;
/**
 * Home interface for Enterprise Bean: ModelManipulation
 */
public interface ModelManipulationHome extends javax.ejb.EJBHome {
	/**
	 * Creates a default instance of Session Bean: ModelManipulation
	 */
	public unbbs.manipulation.ModelManipulation create()
		throws javax.ejb.CreateException, java.rmi.RemoteException;
}
