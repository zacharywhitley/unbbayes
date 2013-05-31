/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import unbbayes.prs.Graph;

/**
 * This class is used by {@link NetIO} in order
 * to get instances of {@link PrintStream} from a {@link File}.
 * This is useful in order to customize where to print 
 * the .net format (e.g. to a file, or to a string).
 * @author Shou Matsumoto
 *
 */
public interface IPrintStreamBuilder {
	
	/**
	 * This method is used in {@link NetIO#save(File, Graph)} in order to convert 
	 * {@link File} into {@link PrintStream}.
	 * Subclasses can overwrite this method in order to customize 
	 * the behavior of the {@link PrintStream} used by {@link NetIO}
	 * @param file :  the file to be used.
	 * @return an instance of {@link PrintStream}
	 * @throws FileNotFoundException
	 */
	public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException;

}
