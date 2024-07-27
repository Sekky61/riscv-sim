/**
 * @file Lexer.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains lexer for parsing ASM code
 * @date 10 October  2023 18:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2020  Jan Vavra
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gradle.superscalarsim.code;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspired by <a href="https://github.com/AZHenley/riscv-parser">riscv-parser</a>
 *
 * @brief Lexer for parsing ASM code
 */
public class Lexer
{
  /**
   * Static list of supported directives.
   */
  public static final List<String> DIRECTIVES = List.of(".byte", ".hword", ".word", ".align", ".ascii", ".asciiz",
                                                        ".string", ".skip", ".zero");
  
  /**
   * Parsed code
   */
  String code;
  
  /**
   * Current line in the code, (first line is 1)
   */
  int line;
  
  /**
   * Current index in the line, (first column is 1)
   */
  int column;
  
  /**
   * Current index in the code
   */
  int index;
  /**
   * Precomputed tokens
   */
  List<CodeToken> tokens;
  /**
   * Pointer to the next token
   */
  int tokenPtr;
  /**
   * Current character
   */
  private char ch;
  
  /**
   * @param code Code to parse
   *
   * @brief Constructor
   */
  public Lexer(String code)
  {
    if (code == null)
    {
      throw new IllegalArgumentException("Code cannot be empty");
    }
    if (code.isEmpty())
    {
      this.ch = '\0';
    }
    else
    {
      this.ch = code.charAt(0);
    }
    this.code   = code;
    this.line   = 1;
    this.column = 1;
    this.index  = 0;
    
    this.tokens   = new ArrayList<>();
    this.tokenPtr = 0;
    collectTokens();
    assert !tokens.isEmpty();
    assert tokens.get(tokens.size() - 1).type() == CodeToken.Type.EOF;
  }
  
  private void collectTokens()
  {
    while (true)
    {
      CodeToken token = parseToken();
      tokens.add(token);
      if (token.type() == CodeToken.Type.EOF)
      {
        break;
      }
    }
  }
  
  /**
   * @return New token form the code
   * @brief Produce next token
   */
  public CodeToken parseToken()
  {
    skipSpaces();
    
    int line        = this.line;
    int columnStart = this.column;
    
    switch (ch)
    {
      case '\0' ->
      {
        return new CodeToken(line, columnStart, "", CodeToken.Type.EOF);
      }
      case '\n' ->
      {
        nextChar();
        while (isSpace() || ch == '\n')
        {
          nextChar();
        }
        return new CodeToken(line, columnStart, "\n", CodeToken.Type.NEWLINE);
      }
      case '(' ->
      {
        nextChar();
        return new CodeToken(line, columnStart, "(", CodeToken.Type.L_PAREN);
      }
      case ')' ->
      {
        nextChar();
        return new CodeToken(line, columnStart, ")", CodeToken.Type.R_PAREN);
      }
      case ',' ->
      {
        nextChar();
        return new CodeToken(line, columnStart, ",", CodeToken.Type.COMMA);
      }
      case ':' ->
      {
        nextChar();
        return new CodeToken(line, columnStart, ":", CodeToken.Type.COLON);
      }
      case '#' ->
      {
        nextChar();
        StringBuilder comment = new StringBuilder();
        while (ch != '\n')
        {
          comment.append(ch);
          nextChar();
        }
        return new CodeToken(line, columnStart, comment.toString(), CodeToken.Type.COMMENT);
      }
      default ->
      {
        // Symbol
        // String case
        if (ch == '"')
        {
          nextChar();
          StringBuilder symbol = new StringBuilder();
          while (ch != '"')
          {
            symbol.append(ch);
            nextChar();
          }
          nextChar();
          return new CodeToken(line, columnStart, symbol.toString(), CodeToken.Type.STRING);
        }
        
        // General case
        StringBuilder symbol = new StringBuilder();
        while (!isSpace() && !isNewLine() && ch != '(' && ch != ')' && ch != ',' && ch != ':' && ch != '#' && ch != '\0')
        {
          symbol.append(ch);
          nextChar();
        }
        String string = symbol.toString();
        // label has precedence over directive (.LC0: is a label)
        if (ch == ':')
        {
          // Save token without colon
          nextChar();
          return new CodeToken(line, columnStart, string, CodeToken.Type.LABEL);
        }
        else if (DIRECTIVES.contains(string))
        {
          return new CodeToken(line, columnStart, string, CodeToken.Type.DIRECTIVE);
        }
        else
        {
          return new CodeToken(line, columnStart, string, CodeToken.Type.SYMBOL);
        }
      }
    }
  }
  
  /**
   * @brief Skip spaces
   */
  private void skipSpaces()
  {
    while (isSpace())
    {
      nextChar();
    }
  }
  
  /**
   * Mutates the state of the lexer to the next character in the code
   */
  private void nextChar()
  {
    if (ch == '\n')
    {
      line++;
      column = 0;
    }
    if (index < code.length() - 1)
    {
      index++;
      column++;
      ch = code.charAt(index);
    }
    else
    {
      ch = '\0';
    }
  }
  
  private boolean isSpace()
  {
    return ch == ' ' || ch == '\t';
  }
  
  private boolean isNewLine()
  {
    return ch == '\n';
  }
  
  public List<CodeToken> getTokens()
  {
    return tokens;
  }
  
  /**
   * @return Next token
   * @brief Produce next token
   */
  public CodeToken nextToken()
  {
    if (tokenPtr >= tokens.size())
    {
      return tokens.get(tokens.size() - 1);
    }
    return tokens.get(tokenPtr++);
  }
  
  public CodeToken currentToken()
  {
    if (tokenPtr >= tokens.size())
    {
      return tokens.get(tokens.size() - 1);
    }
    return tokens.get(tokenPtr);
  }
  
  public CodeToken peekToken()
  {
    if (tokenPtr + 1 >= tokens.size())
    {
      return tokens.get(tokens.size() - 1);
    }
    return tokens.get(tokenPtr + 1);
  }
  
  private boolean isHexDigit()
  {
    return isDecDigit() || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
  }
  
  private boolean isDecDigit()
  {
    return Character.isDigit(ch);
  }
  
  private boolean isPunct()
  {
    return ch == '$' || ch == '.' || ch == '_' || ch == '\'';
  }
  
  private boolean isAlpha()
  {
    return Character.isAlphabetic(ch);
  }
}
