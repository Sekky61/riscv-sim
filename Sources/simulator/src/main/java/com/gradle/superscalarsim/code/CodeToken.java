package com.gradle.superscalarsim.code;

/**
 * @brief Represents a token from the code
 * Contains information for parsing the code and reporting errors
 */
public record CodeToken(int line, int columnStart, String text, Type type)
{
  public int columnEnd()
  {
    return columnStart + text.length() - 1;
  }
  
  /**
   * @ String representation of the token for debugging
   */
  @Override
  public String toString()
  {
    return "[" + line + ":" + columnStart + ":" + this.columnEnd() + "] " + text + " (" + type + ")";
  }
  
  public enum Type
  {
    WORD, LABEL, NEWLINE, COMMA
  }
}
