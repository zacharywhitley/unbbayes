package unbbayes.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * This class is a {@link IPrintStreamBuilder} which
 * builds a {@link PrintStream} which simply
 * appends to a string.
 * For such purpose, it builds instances of {@link StringPrintStream}
 * @author Shou Matsumoto
 * @see StringReaderBuilder
 * @see unbbayes.io.NetIO
 */
public class StringPrintStreamBuilder implements IPrintStreamBuilder {

	private StringBuilder stringBuilder = null;
	
	/**
	 * Default constructor is kept protected in order to 
	 * allow easy inheritance.
	 */
	protected StringPrintStreamBuilder() { }
	
	/**
	 * @param stringBuilder: this will be passed to {@link StringPrintStream}
	 * which will be generated in {@link #getPrintStreamFromFile(File)}.
	 * By doing this, {@link StringPrintStream} will be able to append
	 * text to the string builder.
	 */
	public StringPrintStreamBuilder(StringBuilder stringBuilder) { 
		this.setStringBuilder(stringBuilder);
	}
	
	
	

	/**
	 * Returns an instance of {@link StringPrintStream}.
	 * This will return a {@link PrintStream} if {@link #getStringBuilder()} == null, though.
	 * @see unbbayes.io.IPrintStreamBuilder#getPrintStreamFromFile(java.io.File)
	 */
	public PrintStream getPrintStreamFromFile(File file) throws FileNotFoundException {
		// make sure string builder is not null
		if (this.getStringBuilder() == null) {
			return new PrintStream(file);
		}
		try {
			return new StringPrintStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This string builder is passed to {@link StringPrintStream#StringPrintStream(StringBuilder)}
	 * in {@link #getPrintStreamFromFile(File)}.
	 * @return the stringBuilder
	 */
	public StringBuilder getStringBuilder() {
		return stringBuilder;
	}

	/**
	 * This string builder is passed to {@link StringPrintStream#StringPrintStream(StringBuilder)}
	 * in {@link #getPrintStreamFromFile(File)}.
	 * @param stringBuilder the stringBuilder to set
	 */
	public void setStringBuilder(StringBuilder stringBuilder) {
		this.stringBuilder = stringBuilder;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (getStringBuilder()!=null)?getStringBuilder().toString():super.toString();
	}

	/**
	 * This is the class which actually represents a string, and
	 * all methods of {@link PrintStream} which appends text
	 * to a stream will actually append to the {@link StringPrintStreamBuilder#getStringBuilder()}
	 * @author Shou Matsumoto
	 *
	 */
	public class StringPrintStream extends PrintStream {

		/**
		 * Default constructor.
		 */
		public StringPrintStream() throws IOException {
			super(File.createTempFile("[IfYouSeeThisFile]","[ThereIsABugInUnBBayes]"));
		}


		/**
		 * nothing is needed
		 * @see java.io.PrintStream#flush()
		 */
		public void flush() { }

		/**
		 * nothing is needed
		 * @see java.io.PrintStream#close()
		 */
		public void close() {}

		/**
		 * Simply returns false
		 * @see java.io.PrintStream#checkError()
		 */
		public boolean checkError() { return false; }

		/**
		 * nothing is needed
		 * @see java.io.PrintStream#setError()
		 */
		protected void setError() { }

		/**
		 * nothing is needed
		 * @see java.io.PrintStream#clearError()
		 */
		protected void clearError() { }

		/* (non-Javadoc)
		 * @see java.io.PrintStream#write(int)
		 */
		public void write(int b) {
			getStringBuilder().append((char)b);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#write(byte[], int, int)
		 */
		public void write(byte[] buf, int off, int len) {
			for (int i = off; i < off+len; i++) {
				getStringBuilder().append((char)buf[i]);
			}
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(boolean)
		 */
		public void print(boolean b) {
			getStringBuilder().append(b);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(char)
		 */
		public void print(char c) {
			getStringBuilder().append(c);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(int)
		 */
		public void print(int i) {
			getStringBuilder().append(i);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(long)
		 */
		public void print(long l) {
			getStringBuilder().append(l);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(float)
		 */
		public void print(float f) {
			getStringBuilder().append(f);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(double)
		 */
		public void print(double d) {
			getStringBuilder().append(d);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(char[])
		 */
		public void print(char[] s) {
			for (char c : s) {
				getStringBuilder().append(c);
			}
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(java.lang.String)
		 */
		public void print(String s) {
			getStringBuilder().append(s);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#print(java.lang.Object)
		 */
		public void print(Object obj) {
			getStringBuilder().append(obj);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println()
		 */
		public void println() {
			getStringBuilder().append("\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(boolean)
		 */
		public void println(boolean x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(char)
		 */
		public void println(char x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(int)
		 */
		public void println(int x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(long)
		 */
		public void println(long x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(float)
		 */
		public void println(float x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(double)
		 */
		public void println(double x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(char[])
		 */
		public void println(char[] x) {
			for (char c : x) {
				getStringBuilder().append(c);
			}
			getStringBuilder().append("\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(java.lang.String)
		 */
		public void println(String x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(java.lang.Object)
		 */
		public void println(Object x) {
			getStringBuilder().append(x + "\n");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#printf(java.lang.String, java.lang.Object[])
		 */
		public PrintStream printf(String format, Object... args) {
			throw new UnsupportedOperationException("This PrintStream cannot handle formatted output.");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#printf(java.util.Locale, java.lang.String, java.lang.Object[])
		 */
		public PrintStream printf(Locale l, String format, Object... args) {
			throw new UnsupportedOperationException("This PrintStream cannot handle formatted output.");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#format(java.lang.String, java.lang.Object[])
		 */
		public PrintStream format(String format, Object... args) {
			throw new UnsupportedOperationException("This PrintStream cannot handle formatted output.");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#format(java.util.Locale, java.lang.String, java.lang.Object[])
		 */
		public PrintStream format(Locale l, String format, Object... args) {
			throw new UnsupportedOperationException("This PrintStream cannot handle formatted output.");
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#append(java.lang.CharSequence)
		 */
		public PrintStream append(CharSequence csq) {
			getStringBuilder().append(csq);
			return this;
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
		 */
		public PrintStream append(CharSequence csq, int start, int end) {
			getStringBuilder().append(csq.subSequence(start, end));
			return this;
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#append(char)
		 */
		public PrintStream append(char c) {
			getStringBuilder().append(c);
			return this;
		}

		/* (non-Javadoc)
		 * @see java.io.FilterOutputStream#write(byte[])
		 */
		public void write(byte[] b) throws IOException {
			for (byte c : b) {
				getStringBuilder().append((char)c);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return (getStringBuilder()!=null)?getStringBuilder().toString():super.toString();
		}
	}

}
