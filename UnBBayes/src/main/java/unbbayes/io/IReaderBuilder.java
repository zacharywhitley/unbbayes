package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;

/**
 * This class is used in {@link NetIO#load(File, unbbayes.prs.builder.IProbabilisticNetworkBuilder)}
 * in order to convert a {@link File} to a {@link Reader}
 * @author Shou Matsumoto
 */
public interface IReaderBuilder {

	/**
	 * This is used in is used in {@link NetIO#load(File, unbbayes.prs.builder.IProbabilisticNetworkBuilder)}
	 * in order to convert a {@link File} to a {@link Reader}
	 * @param file
	 * @return a new instance of {@link Reader}
	 */
	public Reader getReaderFromFile(File file)  throws FileNotFoundException;
}
