package unbbayes.util;

import java.awt.geom.Point2D;

public class SerializablePoint2D extends Point2D.Double implements
		java.io.Serializable {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private void writeObject(java.io.ObjectOutputStream out)
			throws java.io.IOException {
		out.writeDouble(x);
		out.writeDouble(y);
	}

	private void readObject(java.io.ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException {
		x = in.readDouble();
		y = in.readDouble();
	}
}