package unbbayes.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonDataUtil {

	private static CommonDataUtil  singleton;


	private String authorName = null; 
	private String actualDate = null; 
	
	private CommonDataUtil(){
		
	}
	
	
	public static CommonDataUtil getInstance(){
		
		if (singleton!=null){
			return singleton; 
		}else{
			return new CommonDataUtil(); 
		}
		
	}
	
	public String getAuthorName() {
		return authorName;
	}


	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}


	public String getActualDate() {
		Date date = new Date(System.currentTimeMillis()); 
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
		return formatador.format(date);
	}


//	public void setActualDate(String actualDate) {
//		this.actualDate = actualDate;
//	}
	
	
}
