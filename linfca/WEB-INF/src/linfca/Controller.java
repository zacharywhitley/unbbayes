package linfca;
import org.jdom.*;
import java.sql.*;
 
public class Controller {
	private static Controller instance;
		
    private static final String DB_URL = "jdbc:mysql://localhost/linfca";
	private static final String USER_NAME = "";
	private static final String PASSWORD = "";
	
	public static Controller getInstance() throws Exception {
		if (instance == null) {
			instance = new Controller();			
		} 
		return instance;
	}
	
	public final Connection makeConnection() throws SQLException {
		return DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);				
	}
	
	private Controller() throws Exception {
		Class.forName("org.gjt.mm.mysql.Driver").newInstance();
	}
		
	/*	
	public Element execute(Element in) throws Exception {	
		String featureName = in.getName();	
		Feature feature = (Feature) Class.forName(featureName).newInstance();		
		return feature.process(in);
	}
	*/
}
