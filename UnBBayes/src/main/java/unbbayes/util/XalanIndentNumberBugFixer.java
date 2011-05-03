
package unbbayes.util;

import java.util.Collection;
import java.util.HashSet;

import org.apache.xalan.processor.TransformerFactoryImpl;

/**
 * This class extends the apache Xalan in order to stop
 * failing if it finds a indent-number argument
 * @author Shou Matsumoto
 */
public class XalanIndentNumberBugFixer extends TransformerFactoryImpl {
	private Collection<String> ignoredArguments = new HashSet<String>();

	/**
	 * The default constructor must be public, so that Xalan can correctly instantiate it
	 * using reflection.
	 * It initializes {@link #getIgnoredArguments()} as "indent-number"
	 */
	public XalanIndentNumberBugFixer() {
		super();
		this.ignoredArguments = new HashSet<String>();
		this.ignoredArguments.add("indent-number");
	}

	/* (non-Javadoc)
	 * @see org.apache.xalan.processor.TransformerFactoryImpl#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value)
			throws IllegalArgumentException {
		// verify if name is in the list of ignored argument names
		if (this.getIgnoredArguments() != null) {
			for (String ignoredName : this.getIgnoredArguments()) {
				if (ignoredName == null) {
					if (name == null) {
						return;	// both name and ignore name was null
					}
				} else if (ignoredName.equals(name)) {
					return;	// ignoredName was not null and it was equal to name.
				}
			}
		}
		// delegate to super if name was not supposed to be ignored
		super.setAttribute(name, value);
	}

	/**
	 * Arguments with these names will be ignored in {@link #setAttribute(String, Object)}
	 * @return the ignoredArguments
	 */
	public Collection<String> getIgnoredArguments() {
		return ignoredArguments;
	}

	/**
	 * Arguments with these names will be ignored in {@link #setAttribute(String, Object)}
	 * @param ignoredArguments the ignoredArguments to set
	 */
	public void setIgnoredArguments(Collection<String> ignoredArguments) {
		this.ignoredArguments = ignoredArguments;
	}

	
}
