package unbbayes;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;


/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 24/08/2006
 */
public class TestMain {
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		new Testset();
//		/* Save nearestneighbors */
//		File fileTest = new File("c:/nearest.txt");
//		RandomAccessFile file;
//		try {
//			file = new RandomAccessFile(fileTest, "rw");
//			file.seek(0);
//			float dist = 4;
//			int idx = 3;
//			file.writeFloat(dist);
//			file.writeInt(idx);
//			file.close();
//			
//			dist = 0;
//			idx = 0;
//			file = new RandomAccessFile(fileTest, "r");
//			file.seek(0);
//			System.out.print(file.readFloat());
//			System.out.print(file.readInt());
//			file.close();
//
//		} catch (Exception e){
//		}
	}
}