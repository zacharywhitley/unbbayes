package br.com.digitaxi.util;

import javax.transaction.Status;

import org.hibernate.Interceptor;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static class UnitHolder {
        public Session session;
        public Transaction transaction;
    }

    private int status;
    private static final ThreadLocal threadInterceptor = new ThreadLocal();
    private static final ThreadLocal threadUnit = new ThreadLocal();
    private static Configuration configuration;
	private static SessionFactory sessionFactory;

    public static int sessionOppened = 0;
    public static int sessionClosed = 0;
    public static int transactionOpend = 0;
    public static int transactionCommited = 0;

    /**
     * Returns the SessionFactory used for this static class.
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Returns the original Hibernate configuration.
     *
     * @return Configuration
     */
    public static Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Rebuild the SessionFactory with the static Configuration.
     *
     */
    public static void rebuildSessionFactory() throws Exception {
        synchronized(sessionFactory) {
            try {
                sessionFactory = getConfiguration().buildSessionFactory();
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     *
     * @param cfg
     */
    public static void rebuildSessionFactory(Configuration cfg) throws Exception {
        synchronized(sessionFactory) {
            try {
                sessionFactory = cfg.buildSessionFactory();
                configuration = cfg;
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    /**
     * Retrieves the current Session local to the thread.
     * <p/>
     * If no Session is open, opens a new Session for the running thread.
     *
     * @return Session
     */
    public static Session currentSession() throws HibernateException {
        //Session s = (Session) threadSession.get();
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Session s = unit.session;
        try {
            if (s == null) {
                //log.debug("Opening new Session for this thread.");
                if (getInterceptor() != null) {
                   // log.debug("Using interceptor: " + getInterceptor().getClass());
                    s = getSessionFactory().openSession(getInterceptor());
                } else {
                    s = getSessionFactory().openSession();
                }
                unit.session = s;
                //TODO: retirar
                sessionOppened++;
                //threadSession.set(s);
                threadUnit.set(unit);
            }
        } catch (HibernateException ex) {
            throw ex;
        }
        if (!s.isOpen()) {
            HibernateUtil.reconnect(s);
        }
        if (!s.isConnected()) {
            HibernateUtil.reconnect(s);
        }
        s.setFlushMode(FlushMode.COMMIT);
        return s;
    }
    
    /**
     * Closes the Session local to the thread.
     */
    public static void closeSession() throws HibernateException {
        try {
            UnitHolder unit = (UnitHolder)threadUnit.get();
            //log.trace("Unit: " + unit);
            if (unit==null) {
                unit = new UnitHolder();
                threadUnit.set(unit);
            }
            Session s = unit.session;
            if (s!=null) {
              //  log.trace("Closing Current session: " + s + ", open: " + s.isOpen());
            }
            if (!HibernateUtil.isSessionClosed()) {
                //log.debug("Closing Session of this thread.");
                s.close();
                sessionClosed++;
            }
            unit.session = null;
            Transaction tx = unit.transaction;
            if (tx!=null) {
                unit.transaction = null;
            }
        } catch (HibernateException ex) {
            //log.error("Exception: " + ex, ex);
            throw ex;
        }
    }
    
    public static boolean isSessionClosed() {
        try {
            UnitHolder unit = (UnitHolder)threadUnit.get();
            if (unit==null) {
                unit = new UnitHolder();
                threadUnit.set(unit);
            }
            Session s = unit.session;
            //log.debug("Session: " + s);
            if (s==null) {
                return true;
            }
            //log.debug("Opened: " + s.isOpen() + ", Connected: " + s.isConnected());
            if (!(s.isOpen() || s.isConnected())) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            //log.error(ex,ex);
            return false;
        }
    }
    
    /**
     * Start a new database transaction.
     */
    public static void beginTransaction() throws HibernateException {
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Transaction tx = unit.transaction;
        try {
            if (tx == null) {
              //  log.debug("Starting new database transaction in this thread.");
                tx = currentSession().beginTransaction();
                transactionOpend++;
                unit.transaction = tx;
                //Nao eh necessario!.
                threadUnit.set(unit);
            }
        } catch (HibernateException ex) {
            throw ex;
        }
    }
    
    /**
     * Commit the database transaction.
     */
    public static void commitTransaction() throws HibernateException {
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Transaction tx = unit.transaction;
        try {
            if ( tx != null && !tx.wasCommitted()
            && !tx.wasRolledBack() ) {
                //log.info("Committing database transaction of this thread.");
                tx.commit();
                transactionCommited++;
            }
            unit.transaction = null;
            
        } catch (HibernateException ex) {
            rollbackTransaction();
            throw ex;
        } finally {
            // No matter what happens, close the Session.
            try {
                HibernateUtil.closeSession();
            } catch (Exception ex) {
                //throw ex;
            }
        }
    }
    
    /**
     * Commit the database transaction.
     */
    public static void rollbackTransaction() throws HibernateException {
//		Transaction tx = (Transaction) threadTransaction.get();
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Transaction tx = unit.transaction;
        try {
            //threadTransaction.set(null);
            unit.transaction = null;
            if ( tx != null && !tx.wasCommitted() && !tx.wasRolledBack() ) {
                //log.debug("Tyring to rollback database transaction of this thread.");
                tx.rollback();
            }
            unit.transaction = null;
        } catch (HibernateException ex) {
            throw ex;
        } finally {
            // No matter what happens, close the Session.
            try {
                HibernateUtil.closeSession();
            } catch (Exception ex) {
                //throw ex;
            }
        }
    }
    
    /**
     * Reconnects a Hibernate Session to the current Thread.
     *
     * @param session The Hibernate Session to be reconnected.
     */
    public static void reconnect(Session session)
    throws HibernateException {
        /*try {
            session.reconnect();
        } catch (HibernateException e) {
            //log.debug(e.toString());
        }
        //threadSession.set(session);
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        unit.session = session;*/
    }
    
    /**
     * Disconnect and return Session from current Thread.
     *
     * @return Session the disconnected Session
     */
    public static Session disconnectSession()
    throws Exception {
        Session session = currentSession();
        try {
            //threadSession.set(null);
            UnitHolder unit = (UnitHolder)threadUnit.get();
            if (unit==null) {
                unit = new UnitHolder();
                threadUnit.set(unit);
            }
            unit.session =null;
            if (session.isConnected() && session.isOpen())
                session.disconnect();
        } catch (HibernateException ex) {
            throw ex;
        }
        return session;
    }
    
    /**
     * Register a Hibernate interceptor with the current thread.
     * <p>
     * Every Session opened is opened with this interceptor after
     * registration. Has no effect if the current Session of the
     * thread is already open, effective on next close()/getSession().
     */
    public static void registerInterceptor(Interceptor interceptor) {
        threadInterceptor.set(interceptor);
    }
    
    private static Interceptor getInterceptor() {
        Interceptor interceptor =
                (Interceptor) threadInterceptor.get();
        return interceptor;
    }
    
    /**
     *Metodos da Interface ITransaction...
     */
    
    public void init() throws Exception {
        //log.debug("Iniciando transacao do Hibernate...");
        try {
            HibernateUtil.beginTransaction();
            this.setStatus(Status.STATUS_ACTIVE);
           // log.debug("Transaction Status: " + this.getStatus());
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public void commit() throws Exception {
        try {
            HibernateUtil.commitTransaction();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public void rollback() throws Exception {
        try {
            HibernateUtil.rollbackTransaction();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public void setIsCommitAllowed(boolean commitAllowed) {
    }
    
    public boolean allowCommit() {
        return true;
    }
    
    public void finalize(String scope) throws Exception {
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Transaction tx = unit.transaction;
        if ( tx == null ) {
            //log.error("Hibernate Transaction null!");
            //throw new TransactionException("Hibernate Transaction is null!!!");
            this.setStatus(Status.STATUS_UNKNOWN);
            return;
            
        }
    }
    
    public boolean isInitialized() {
        UnitHolder unit = (UnitHolder)threadUnit.get();
        if (unit==null) {
            unit = new UnitHolder();
            threadUnit.set(unit);
        }
        Transaction tx = unit.transaction;
        try {
            if ( tx != null && !tx.wasCommitted()
            && !tx.wasRolledBack()) {
//                log.debug("A transacao existe e ainda nao foi emitido " +
//                        "um commit ou rollback! Retorando true...");
                return true;
            }
        } catch (HibernateException ex) {
//            log.error(ex,ex);
        }
        return false;
    }
    
    public int getStatus() {
        return status;
    }    
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void markRollbackOnly() {
        this.setStatus(Status.STATUS_MARKED_ROLLBACK);
    }
}
