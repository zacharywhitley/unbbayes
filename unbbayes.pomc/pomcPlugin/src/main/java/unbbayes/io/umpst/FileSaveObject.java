package unbbayes.io.umpst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import unbbayes.model.umpst.project.UMPSTProject;

public class FileSaveObject {


		public static final String NULL = "null";

		private UMPSTProject umpstProject;

		public void saveUbf(File file,
				UMPSTProject umpstProject) throws IOException{


			this.umpstProject = umpstProject;
			//File file = new File("images/file.ump");

			/*-- Listing the overall data of the map --*/
			FileOutputStream fileOut = new FileOutputStream(new File(file.getPath()+".ump"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			
			out.writeObject(umpstProject); 
			
			out.close(); 
			fileOut.flush(); 
			fileOut.close(); 
			
			System.out.println("Serialization succesfull");
			
			
		}

}
