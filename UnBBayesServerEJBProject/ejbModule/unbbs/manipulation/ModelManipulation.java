package unbbs.manipulation;
/**
 * Remote interface for Enterprise Bean: ModelManipulation
 */
public interface ModelManipulation extends javax.ejb.EJBObject {
	public String compile(int modelId) throws java.rmi.RemoteException;
}
