package org.plumelib.bcelutil;

import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.formatter.qual.FormatMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.determinism.qual.*;

/**
 * A logging class with the following features:
 *
 * <ul>
 *   <li>Can be enabled and disabled (when disabled, all operations are no-ops),
 *   <li>Can indent/exdent log output,
 *   <li>Writes to standard output, and
 *   <li>Can provide a stack trace.
 * </ul>
 */
public final class SimpleLog {

  /** If false, do no output. */
  public boolean enabled;

  /** The current indentation level. */
  private int indentLevel = 0;
  /** Indentation string for one level of indentation. */
  private final String INDENT_STR_ONE_LEVEL = "  ";
  /**
   * Cache for the current indentation string, or null if needs to be recomputed. Never access this
   * directly; always call {@link #getIndentString}.
   */
  private @Nullable String indentString = null;
  /** Cache of indentation strings that have been computed so far. */
  private List<String> indentStrings;

  /** Create a new SimpleLog object with logging enabled. */
  public SimpleLog() {
    this(true);
  }

  /**
   * Create a new SimpleLog object.
   *
   * @param enabled whether the logger starts out enabled
   */
  public SimpleLog(boolean enabled) {
    this.enabled = enabled;
    indentStrings = new ArrayList<String>();
    indentStrings.add("");
  }

  /**
   * Return whether logging is enabled.
   *
   * @return whether logging is enabled
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Log a message to System.out. The message is prepended with the current indentation string. The
   * indentation is only applied at the start of the message, not for every line break within the
   * message.
   *
   * @param format format string for message
   * @param args values to be substituted into format
   */
  @FormatMethod
  @SuppressWarnings("determinism:nondeterministic.tostring")  //  Determinism checker cannot track when Object.toString is deterministic (https://github.com/t-rasmud/checker-framework/issues/198)
  public void log(@Det SimpleLog this, @Det String format, @Nullable @Det Object @Det... args) {
    if (enabled) {
      System.out.print(getIndentString());
      System.out.printf(format, args);
    }
  }

  /** Print a stack trace to System.out. */
  public void logStackTrace(@Det SimpleLog this) {
    if (enabled) {
      @Det Throwable t = new @Det Throwable();
      t.fillInStackTrace();
      @Det StackTraceElement @Det[] ste_arr = t.getStackTrace();
      for (int ii = 2; ii < ste_arr.length; ii++) {
        StackTraceElement ste = ste_arr[ii];
        System.out.printf("%s  %s%n", getIndentString(), ste);
      }
    }
  }

  /**
   * Return the current indentation string.
   *
   * @return the current indentation string x
   */
  private String getIndentString() {
    assert enabled;
    if (indentString == null) {
      for (int i = indentStrings.size(); i <= indentLevel; i++) {
        indentStrings.add(indentStrings.get(i - 1) + INDENT_STR_ONE_LEVEL);
      }
      indentString = indentStrings.get(indentLevel);
    }
    return indentString;
  }

  /** Increases indentation by one level. */
  @SuppressWarnings("determinism:unary.increment.type.incompatible")  // Unsafe: Incrementing PolyDet int: {OrderNonDet Set<Det SimpleLog> st; @NonDet s = st.iterator().next(); st.indent()}
  public void indent() {
    if (enabled) {
      indentLevel++;
      indentString = null;
    }
  }

  /** Decreases indentation by one level. */
  public void exdent(@Det SimpleLog this) {
    if (enabled) {
      if (indentLevel == 0) {
        log("Called exdent when indentation level was 0.");
        logStackTrace();
      } else {
        indentLevel--;
        indentString = null;
      }
    }
  }

  /** Resets indentation to none. Has no effect if logging is disabled. */
  public void resetIndent() {
    if (enabled) {
      indentLevel = 0;
      indentString = "";
    }
  }
}
