package hibernate;

import java.io.File;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private static SessionFactory sessionFactory;
	
	static{
		
				File file = new File("resources/hibernate.cfg.xml");
				
				Configuration config = new Configuration().configure(file);
				config.setProperty("hibernate.connection.url","jdbc:sqlite:"+ "data/data.db");
				config.addFile(new File("resources/Session.hbm.xml"));
				config.addFile(new File("resources/File.hbm.xml"));
				config.addFile(new File("resources/Attribute.hbm.xml"));
				config.addFile(new File("resources/Delta.hbm.xml"));
				config.addFile(new File("resources/DeltaContent.hbm.xml"));

				
				
				sessionFactory = config.buildSessionFactory();
				if(sessionFactory == null)
					throw new RuntimeException("Could not create Hibernate session");
	
			

	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown(){
		getSessionFactory().close();
	}
}
