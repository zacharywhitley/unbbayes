package unbbs.controller.model;

import java.rmi.RemoteException;

import unbbs.persistence.model.ModelValue;

import unbbs.persistence.model.DomainValue;
/**
 * Remote interface for Enterprise Bean: ModelSession
 */
public interface ModelSession extends javax.ejb.EJBObject {

	public void saveModel(
		int id,
		String name,
		String description,
		String model,
		int domainId)
		throws RemoteException;
		
	public ModelValue detailModel(int id) throws RemoteException;
	public ModelValue[] searchModel(String search)
		throws java.rmi.RemoteException;
	public void editModel(
		int id,
		String name,
		String description,
		String model,
		int domainId)
		throws java.rmi.RemoteException;
	public DomainValue[] searchAllDomain() throws java.rmi.RemoteException;
}
