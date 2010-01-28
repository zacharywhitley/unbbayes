/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DebuggingObjectOutputStream
    extends ObjectOutputStream {

  private static final Field DEPTH_FIELD;
  static {
    try {
      DEPTH_FIELD = ObjectOutputStream.class
          .getDeclaredField("depth");
      DEPTH_FIELD.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  final List<Object> stack
      = new ArrayList<Object>();

  /**
   * Indicates whether or not OOS has tried to
   * write an IOException (presumably as the
   * result of a serialization error) to the
   * stream.
   */
  boolean broken = false;

  public DebuggingObjectOutputStream(
      OutputStream out) throws IOException {
    super(out);
    enableReplaceObject(true);
  }

  /**
   * Abuse {@code replaceObject()} as a hook to
   * maintain our stack.
   */
  protected Object replaceObject(Object o) {
    // ObjectOutputStream writes serialization
    // exceptions to the stream. Ignore
    // everything after that so we don't lose
    // the path to a non-serializable object. So
    // long as the user doesn't write an
    // IOException as the root object, we're OK.
    int currentDepth = currentDepth();
    if (o instanceof IOException
        && currentDepth == 0) {
      broken = true;
    }
    if (!broken) {
      truncate(currentDepth);
      stack.add(o);
    }
    return o;
  }

  private void truncate(int depth) {
    while (stack.size() > depth) {
      pop();
    }
  }

  private Object pop() {
    return stack.remove(stack.size() - 1);
  }

  /**
   * Returns a 0-based depth within the object
   * graph of the current object being
   * serialized.
   */
  private int currentDepth() {
    try {
      Integer oneBased
          = ((Integer) DEPTH_FIELD.get(this));
      return oneBased - 1;
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns the path to the last object
   * serialized. If an exception occurred, this
   * should be the path to the non-serializable
   * object.
   */
  public List<Object> getStack() {
    return stack;
  }
}