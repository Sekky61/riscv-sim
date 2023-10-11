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
    if (code == null || code.isEmpty())
    {
      throw new IllegalArgumentException("Code cannot be empty");
    }
    this.code   = code;
    this.line   = 1;
    this.column = 1;
    this.index  = 0;
    this.ch     = code.charAt(0);
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
  
  /**
   * @return List of tokens
   * @brief Tokenize the whole code
   */
  public List<CodeToken> tokenize()
  {
    List<CodeToken> tokens = new ArrayList<>();
    
    CodeToken token = nextToken();
    while (token.type() != CodeToken.Type.EOF)
    {
      tokens.add(token);
      token = nextToken();
    }
    
    return tokens;
  }
  
  /**
   * @return Next token
   * @brief Produce next token
   */
  public CodeToken nextToken()
  {
    skipSpaces();
    
    switch (ch)
    {
      case '\0' ->
      {
        return new CodeToken(line, column, "", CodeToken.Type.EOF);
      }
      case '\n' ->
      {
        nextChar();
        while (isSpace() || ch == '\n')
        {
          nextChar();
        }
        return new CodeToken(line, column, "\n", CodeToken.Type.NEWLINE);
      }
      case '+' ->
      {
        nextChar();
        return new CodeToken(line, column, "+", CodeToken.Type.PLUS);
      }
      case '-' ->
      {
        nextChar();
        return new CodeToken(line, column, "-", CodeToken.Type.MINUS);
      }
      case '(' ->
      {
        nextChar();
        return new CodeToken(line, column, "(", CodeToken.Type.L_PAREN);
      }
      case ')' ->
      {
        nextChar();
        return new CodeToken(line, column, ")", CodeToken.Type.R_PAREN);
      }
      case '.' ->
      {
        nextChar();
        return new CodeToken(line, column, ".", CodeToken.Type.DOT);
      }
      case ',' ->
      {
        nextChar();
        return new CodeToken(line, column, ",", CodeToken.Type.COMMA);
      }
      case ':' ->
      {
        nextChar();
        return new CodeToken(line, column, ":", CodeToken.Type.COLON);
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
        return new CodeToken(line, column, comment.toString(), CodeToken.Type.COMMENT);
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
          return new CodeToken(line, column, symbol.toString(), CodeToken.Type.STRING);
        }
        
        // Number case
        // Hex number case
        if (ch == '0')
        {
          nextChar();
          if (ch == 'x' || ch == 'X')
          {
            nextChar();
            StringBuilder number = new StringBuilder("0x");
            while (isHexDigit())
            {
              number.append(ch);
              nextChar();
            }
            return new CodeToken(line, column, number.toString(), CodeToken.Type.NUMBER);
          }
        }
        // Dec number case
        if (isDecDigit())
        {
          StringBuilder number = new StringBuilder();
          while (isDecDigit())
          {
            number.append(ch);
            nextChar();
          }
          return new CodeToken(line, column, number.toString(), CodeToken.Type.NUMBER);
        }
        
        // General case
        StringBuilder symbol = new StringBuilder();
        while (isAlpha() || isDecDigit() || isPunct())
        {
          symbol.append(ch);
          nextChar();
        }
        return new CodeToken(line, column, symbol.toString(), CodeToken.Type.SYMBOL);
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
  
  private boolean isSpace()
  {
    return ch == ' ' || ch == '\t';
  }
  
  private boolean isDecDigit()
  {
    return Character.isDigit(ch);
  }
  
  private boolean isHexDigit()
  {
    return isDecDigit() || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
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
