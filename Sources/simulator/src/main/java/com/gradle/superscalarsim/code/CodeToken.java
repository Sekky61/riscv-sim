package com.gradle.superscalarsim.code;

/**
 * @class CodeToken
 * @brief Represents a token from the code
 * Contains information for parsing the code and reporting errors
 */
public class CodeToken
{
  public int line;
  public int columnStart;
  public String text;
  public Type type;
  
  public CodeToken(int line, int columnStart, String text, Type type)
  {
    this.line        = line;
    this.columnStart = columnStart;
    this.text        = text;
    this.type        = type;
  }
  
  /**
   * @ String representation of the token for debugging
   */
  @Override
  public String toString()
  {
    return "[" + line + ":" + columnStart + ":" + (columnStart + text.length()) + "] " + text + " (" + type + ")";
  }
  
  public enum Type
  {
    WORD, LABEL, NEWLINE, COMMA
  }
}
