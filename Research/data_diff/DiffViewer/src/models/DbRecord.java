package models;

import hibernate.HibernateUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;


public abstract class DbRecord {
	static Runnable callback; // Ugly.  For JUnit's benefit.
	
	static Session hib_session;
	static Transaction tx;
	static int transaction_counter = 0;
	
	
	public synchronized void save()
	{
		safePerform(new Action<Object>(){
			public Object run(Session s)
			{
				s.save(DbRecord.this);
				return null;
			}
		});
		
		reload();
	}
	
	public synchronized void reload()
	{
		safePerform(new Action<Object>(){
			public Object run(Session s)
			{
				s.refresh(DbRecord.this);
				return null;
			}
		});
	}
	
	public synchronized void deleteAll()
	{
		safePerform(new Action<Object>(){
			public Object run(Session s)
			{
			    String query_string = "delete " + DbRecord.this.getClass().getName();
				Query query = s.createQuery(query_string);
				query.executeUpdate();
				
				return null;
			}
		});
	}
	
	public synchronized void runQuery(final String query_string) {
		safePerform(new Action<Object>(){
			public Object run(Session s)
			{
				Query query = s.createQuery(query_string);
				query.executeUpdate();
				
				return null;
			}
		});
	}
	

	public void vacuum() {

		safePerform(new Action<Object>(){
			public Object run(Session s)
			{
				try {
					Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ new File("data/data.db").getAbsolutePath());
					Statement statement = conn.createStatement();
					statement.executeUpdate("vacuum");
				
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				return null;
			}
		});
	}
	
	public synchronized Object first()
	{
		List all = all();
		if(all.size() < 1)
			return null;
		
		return all.get(0);
	}
	
	public synchronized List<DbRecord> all()
	{
		List ret = safePerform(new Action<List>(){
			public List run(Session s)
			{

			    String query_string = "select * from " + DbRecord.this.getTable();
				Query query = s.createSQLQuery(query_string).addEntity(DbRecord.this.getClass());
				
				List objects = query.list();
				
				return objects;
			}
		});

		return ret;
	}
	
	
	private synchronized <T> T safePerform(Action<T> action)
	{
		T thing = null;
		
		if(transaction_counter == 0)
		{
			beginTransaction();
			thing = action.run(hib_session);
			endTransaction();
		} else {
			thing = action.run(hib_session);
		}

		
		return thing;
	}
	
	private abstract class Action<T>
	{
		public abstract T run(Session s);
	}

	public static synchronized void beginTransaction() {
		if(transaction_counter > 0)
		{
			endTransaction(); // End the previous transaction
		}
		
		hib_session = HibernateUtil.getSessionFactory().openSession();
		tx = hib_session.beginTransaction();
		
	    transaction_counter++;
	}

	public static void endTransaction() {
		transaction_counter--;

		if(transaction_counter == 0)
		{
			tx.commit();
			
			hib_session.close();
	
	
			HibernateUtil.shutdown();
		}
	}

	public abstract String getTable();

}
