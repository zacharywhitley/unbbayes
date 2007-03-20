package br.com.digitaxi.util;

import java.util.Collection;
import java.util.List;
import org.hibernate.Session;

public class BaseDAO {

	public Collection list(Class clazz) throws Exception {
		List result = null;
		Session s = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		try {
			result = s.createQuery("from "+clazz.getName()+" ")
			.setMaxResults(100)
			.list();

			HibernateUtil.commitTransaction();
		} 
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
		}
		finally {
			s.close();
		}

		return result;
	}
}
