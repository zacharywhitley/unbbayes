package unbbs.controller.model;
/**
 * Home interface for Enterprise Bean: ModelSession
 */
public interface ModelSessionHome extends javax.ejb.EJBHome {
	/**
	 * Creates a default instance of Session Bean: ModelSession
	 */
	public unbbs.controller.model.ModelSession create()
		throws javax.ejb.CreateException, java.rmi.RemoteException;
}
