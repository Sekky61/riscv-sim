/**
 * @file CodeParser.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains parser for user made code
 * @date 10 November  2020 18:00 (created) \n
 * 12 May       2020 11:00 (revised)
 * 26 Sep      2023 10:00 (revised)
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

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @class CodeParser
 * @brief Parses provided string of code
 * @detail Class which provides parsing logic for written code in assembly. Code shall contain instructions loaded
 * by InitLoader. Class also verifies parsed code and in case of failure, error message will be provided with
 * line number and cause.
 */
public class CodeParser
{
  /**
   * Pattern for matching hexadecimal values in argument
   */
  private final transient Pattern hexadecimalPattern;
  /**
   * Pattern for matching decimal values in argument
   */
  private final transient Pattern decimalPattern;
  /**
   * Pattern for matching register tags in instructionSyntax value of instruction
   */
  private final transient Pattern registerPattern;
  /**
   * Pattern for splitting instruction arguments
   */
  private final transient Pattern splitArgsPattern;
  /**
   * Pattern for matching immediate tags in instructionSyntax value of instruction
   */
  private final transient Pattern immediatePattern;
  /**
   * Pattern for matching labels in code
   */
  private final transient Pattern labelPattern;
  /**
   * Error messages from parsing ASM code.
   * TODO: Remove from the instance, return it from parse instead.
   *
   * @brief List of error messages
   */
  private final List<ParseError> errorMessages;
  /**
   * InitLoader object with loaded instructions and registers
   */
  private final InitLoader initLoader;
  /**
   * List of parsed instructions
   */
  private List<InputCodeModel> parsedCode;
  
  /**
   * The strings are without the colon at the end.
   * Label can point after the last instruction.
   *
   * @brief List of all labels
   */
  private Map<String, Integer> labels;
  
  /**
   * Nop instruction is instantiated once and reused, to have all SimCodeModel objects point to the same object.
   *
   * @brief Nop instruction
   */
  private final InputCodeModel nop;
  
  /**
   * @param [in] initLoader - InitLoader object with loaded instructions and registers
   *
   * @brief Constructor
   */
  public CodeParser(final InitLoader initLoader)
  {
    this.initLoader         = initLoader;
    this.errorMessages      = new ArrayList<>();
    this.parsedCode         = new ArrayList<>();
    this.labels             = new TreeMap<>();
    this.decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    this.registerPattern    = Pattern.compile("r[d,s]\\d*");
    // Any number of spaces, at most one comma between them
    this.splitArgsPattern = Pattern.compile("\\s*[,\\s]\\s*");
    this.immediatePattern = Pattern.compile("imm\\d*");
    // Anything with a colon at the end
    this.labelPattern = Pattern.compile("^[a-zA-Z0-9\\.]+:");
    this.nop          = new InputCodeModel(null, "nop", new ArrayList<>(), null, null, 0);
  }// end of Constructor
  
  /**
   * Does not check unused labels.
   *
   * @param codeString String holding unparsed code
   *
   * @return True, in case no errors arises, otherwise false
   * @brief Parse whole string with code
   */
  public boolean parse(final String codeString)
  {
    this.errorMessages.clear();
    this.parsedCode.clear();
    this.labels.clear();
    
    if (codeString == null)
    {
      this.errorMessages.add(new ParseError("error", "Code was not provided."));
      return false;
    }
    
    List<CodeToken> tokens = tokenize(codeString);
    
    // First pass - collect all labels
    collectLabels(tokens);
    // Second pass - parse instructions
    parseInstructions(tokens);
    boolean result = this.errorMessages.isEmpty();
    
    if (!result)
    {
      parsedCode.clear();
    }
    return result;
  }// end of parse
  
  /**
   * Code tokenizer - produces a list of words, commas, labels and newlines.
   *
   * @param code Code to tokenize
   *
   * @return List of tokens
   */
  public List<CodeToken> tokenize(final String code)
  {
    List<CodeToken> tokens = new ArrayList<>();
    // Split lines
    String[] lines = code.split("\n");
    // 1-based line index
    
    for (int i = 0; i < lines.length; i++)
    {
      int currentLineIndex = i + 1;
      
      // Remove comments
      String line = lines[i].split("#", 2)[0];
      
      // Split the code to words and commas
      Pattern pattern = Pattern.compile("[\\w\\.\\(\\)-]+:?|,");
      Matcher matcher = pattern.matcher(line);
      while (matcher.find())
      {
        String token = matcher.group();
        int    col   = matcher.start() + 1;
        if (token.equals(","))
        {
          tokens.add(new CodeToken(currentLineIndex, col, token, CodeToken.Type.COMMA));
        }
        else if (labelPattern.matcher(token).matches())
        {
          // Remove the colon
          String label = token.substring(0, token.length() - 1);
          tokens.add(new CodeToken(currentLineIndex, col, label, CodeToken.Type.LABEL));
        }
        else
        {
          tokens.add(new CodeToken(currentLineIndex, col, token, CodeToken.Type.WORD));
        }
      }
      tokens.add(new CodeToken(currentLineIndex, 1, "\n", CodeToken.Type.NEWLINE));
    }
    
    return tokens;
  }
  
  /**
   * @param tokens List of tokens
   *
   * @brief Collects all labels from the code, reports duplicate labels.
   */
  private void collectLabels(List<CodeToken> tokens)
  {
    labels = new TreeMap<>();
    for (CodeToken token : tokens)
    {
      if (token.type() != CodeToken.Type.LABEL)
      {
        continue;
      }
      String label = token.text();
      if (labels.containsKey(label))
      {
        this.addError(token, "Label \"" + label + "\" already exists in current scope.");
      }
      else
      {
        // We do not know the position of the label yet, so we put -1 there
        labels.put(label, -1);
      }
    }
  }
  
  /**
   * @param tokens List of tokens
   *
   * @brief Core of the parser
   * Mutates internal state - creates errorMessages, parsedCode
   */
  private void parseInstructions(List<CodeToken> tokens)
  {
    int currentTokenIndex = 0;
    outer:
    while (currentTokenIndex < tokens.size())
    {
      // A line-centric loop: first newlines and labels, then 1 instruction
      CodeToken currentToken = tokens.get(currentTokenIndex);
      switch (currentToken.type())
      {
        case LABEL ->
        {
          // Duplicate labels are already checked in collectLabels
          int    labelPosition = this.parsedCode.size();
          String labelName     = currentToken.text();
          labels.put(labelName, labelPosition);
          currentTokenIndex++;
          continue;
        }
        case NEWLINE ->
        {
          currentTokenIndex++;
          continue;
        }
        case COMMA ->
        {
          // Comma without an instruction - error
          addError(currentToken, "Unexpected comma.");
          currentTokenIndex++;
          continue;
        }
      }
      
      // One instruction - the switch above ensures that this token is a word
      String                   instructionName = currentToken.text();
      InstructionFunctionModel instruction     = initLoader.getInstructionFunctionModel(instructionName);
      
      if (instruction == null)
      {
        addError(currentToken, "Instruction \"" + instructionName + "\" does not exists.");
        // Recover - skip to the next newline
        currentTokenIndex = findNextNewline(tokens, currentTokenIndex);
        continue;
      }
      
      // Parse the instruction signature
      String[] splitSyntax = splitArgsPattern.split(instruction.getInstructionSyntax());
      int      numArgs     = splitSyntax.length - 1;
      
      // Parse arguments
      List<InputCodeArgument> parsedArgs = new ArrayList<>();
      for (int i = 0; i < numArgs; i++)
      {
        String paramType = splitSyntax[i + 1];
        
        // Load token if exists
        currentTokenIndex++;
        if (currentTokenIndex >= tokens.size())
        {
          addError(currentToken, "Expected argument, got end of file.");
          continue outer;
        }
        currentToken = tokens.get(currentTokenIndex);
        
        // first skip over comma, unless this is the first argument
        if (currentToken.type() == CodeToken.Type.COMMA)
        {
          if (i == 0)
          {
            addError(currentToken, "Comma not allowed between instruction name and first argument.");
            // Recover - do not do anything
          }
          currentTokenIndex++;
          if (currentTokenIndex >= tokens.size())
          {
            addError(currentToken, "Expected argument, got end of file.");
            continue outer;
          }
          currentToken = tokens.get(currentTokenIndex);
        }
        
        // Check if the token is a word
        if (currentToken.type() != CodeToken.Type.WORD)
        {
          addError(currentToken, "Expected argument, got " + currentToken.text() + ".");
          // Recover - skip to the next newline
          currentTokenIndex = findNextNewline(tokens, currentTokenIndex);
          continue outer;
        }
        
        InputCodeArgument arg                 = new InputCodeArgument(paramType, currentToken.text());
        boolean           isLValue            = paramType.equals("rd");
        DataTypeEnum      instructionDataType = isLValue ? instruction.getOutputDataType() : instruction.getInputDataType();
        boolean valid = validateArgument(arg, instruction.getInstructionType(), instructionDataType, isLValue,
                                         currentToken);
        if (valid)
        {
          parsedArgs.add(arg);
        }
      }
      
      // Create instruction model
      int codeIndex = this.parsedCode.size();
      InputCodeModel inputCodeModel = new InputCodeModel(instruction, instructionName, parsedArgs,
                                                         instruction.getInstructionType(),
                                                         instruction.getInputDataType(), codeIndex);
      this.parsedCode.add(inputCodeModel);
      
      // Peek at the next token. If it exists, it must be a newline
      currentTokenIndex++;
      if (currentTokenIndex < tokens.size())
      {
        currentToken = tokens.get(currentTokenIndex);
        if (currentToken.type() != CodeToken.Type.NEWLINE)
        {
          addError(currentToken, "Expected newline, got " + currentToken.text() + ".");
          // Recover - skip to the next newline
          currentTokenIndex = findNextNewline(tokens, currentTokenIndex);
          continue;
        }
      }
    }
  }
  
  /**
   * @param lineNumber Line number of the error
   * @param spanStart  Start of the error span (column)
   * @param spanEnd    End of the error span (column)
   * @param message    Error message
   *
   * @brief Adds a single-line error message to the list of errors
   */
  private void addError(CodeToken token, String message)
  {
    this.errorMessages.add(new ParseError("error", message, token.line(), token.columnStart(), token.columnEnd()));
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Finds the next newline token in the list of tokens, starting from the given index
   */
  int findNextNewline(List<CodeToken> tokens, int currentTokenIndex)
  {
    while (currentTokenIndex < tokens.size())
    {
      CodeToken currentToken = tokens.get(currentTokenIndex);
      if (currentToken.type() == CodeToken.Type.NEWLINE)
      {
        return currentTokenIndex;
      }
      currentTokenIndex++;
    }
    return tokens.size();
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in]  inputCodeArgument   - Argument to be verified
   * @param [in]  instructionType     - Type of the instruction
   * @param [in]  instructionDataType - Data type of an argument according to instruction template
   * @param [in]  isLValue            - True, if the currently verified argument lvalue, false otherwise
   * @param token - Token of the argument, for error reporting
   *
   * @return True in case of valid argument, false otherwise
   * @brief Validates argument's value and data type
   */
  private boolean validateArgument(final InputCodeArgument inputCodeArgument,
                                   final InstructionTypeEnum instructionType,
                                   final DataTypeEnum instructionDataType,
                                   final boolean isLValue,
                                   CodeToken token)
  {
    String  argumentName  = inputCodeArgument.getName();
    String  argumentValue = inputCodeArgument.getValue();
    boolean isDirectValue = isNumeralLiteral(argumentValue);
    
    if (this.immediatePattern.matcher(argumentName).matches())
    {
      //if instruction is jump/branch, expected imm value can be a label or a direct value in the +-1MiB range
      // numeral values are already checked in the previous step
      boolean isBranch = instructionType == InstructionTypeEnum.kJumpbranch;
      return checkImmediateArgument(argumentValue, isLValue, isDirectValue, isBranch, token);
    }
    else if (this.registerPattern.matcher(argumentName).matches())
    {
      // A register (rd, rs1, ...)
      return checkRegisterArgument(argumentValue, instructionDataType, isLValue, isDirectValue, token);
    }
    return false;
  }// end of validateArgument
  
  private boolean isNumeralLiteral(String argValue)
  {
    return this.hexadecimalPattern.matcher(argValue).matches() || this.decimalPattern.matcher(argValue).matches();
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in]     argumentValue - Value of an argument in string format
   * @param [in]     isLValue      - True, if the currently verified argument lvalue, false otherwise
   * @param [in]     isDirectValue - True, if argument is decimal or hexadecimal , false otherwise
   * @param isBranch
   * @param token
   *
   * @return True if argument is valid immediated value, otherwise false
   * @brief Verifies if argument is immediate value
   */
  private boolean checkImmediateArgument(final String argumentValue,
                                         final boolean isLValue,
                                         final boolean isDirectValue,
                                         boolean isBranch,
                                         CodeToken token)
  {
    if (isLValue)
    {
      this.addError(token, "LValue cannot be immediate value.");
      return false;
    }
    
    if (!isDirectValue && !isBranch)
    {
      this.addError(token, "Expecting immediate value, got : \"" + argumentValue + "\".");
      return false;
    }
    
    if (!isDirectValue && !this.labels.containsKey(argumentValue))
    {
      this.addError(token, "Label \"" + argumentValue + "\" does not exist.");
      return false;
    }
    
    return true;
  }// end of checkImmediateArgument
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in]  argumentValue    - Value of an argument in string format
   * @param [in]  argumentDataType - Expected data type of an argument
   * @param [in]  isLValue         - True, if the currently verified argument lvalue, false otherwise
   * @param [in]  isDirectValue    - True, if argument is decimal or hexadecimal , false otherwise
   * @param token
   *
   * @return True if argument is valid register, otherwise false
   * @brief Verifies if argument is register
   */
  private boolean checkRegisterArgument(final String argumentValue,
                                        final DataTypeEnum argumentDataType,
                                        final boolean isLValue,
                                        final boolean isDirectValue,
                                        CodeToken token)
  {
    if (isDirectValue && isLValue)
    {
      this.addError(token, "LValue cannot be immediate value.");
      return false;
    }
    if (isDirectValue)
    {
      this.addError(token, "Expected register, got : \"" + argumentValue + "\".");
      return false;
    }
    
    // Lookup all register files and aliases, check if the register exists
    // Assumes that the register names and aliases are unique
    // First try to find alias
    String registerToLookFor = argumentValue;
    for (InitLoader.RegisterMapping alias : initLoader.getRegisterAliases())
    {
      if (alias.alias.equals(argumentValue))
      {
        // Look for this register instead of the alias
        registerToLookFor = alias.register;
        break;
      }
    }
    for (RegisterFileModel registerFileModel : initLoader.getRegisterFileModelList())
    {
      if (!checkDatatype(argumentDataType, registerFileModel.getDataType()))
      {
        // Incorrect data type in this register file, skip
        continue;
      }
      RegisterModel reg = registerFileModel.getRegister(registerToLookFor);
      if (reg != null)
      {
        return true;
      }
    }
    this.addError(token, "Argument \"" + argumentValue + "\" is not a register nor a value.");
    return false;
  }// end of checkRegisterArgument
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] argumentDataType - The datatype of an argument
   * @param [in] registerDataType - The datatype of an register file
   *
   * @return True if argument can fit inside the register, false otherwise
   * @brief Check argument and register data types if they fit within each other
   */
  private boolean checkDatatype(final DataTypeEnum argumentDataType, final DataTypeEnum registerDataType)
  {
    return switch (argumentDataType)
    {
      case kInt -> registerDataType == DataTypeEnum.kInt || registerDataType == DataTypeEnum.kLong;
      case kLong -> registerDataType == DataTypeEnum.kLong;
      case kFloat -> registerDataType == DataTypeEnum.kFloat || registerDataType == DataTypeEnum.kDouble;
      case kDouble -> registerDataType == DataTypeEnum.kDouble;
      case kSpeculative -> false;
    };
  }// end of checkDatatype
  //-------------------------------------------------------------------------------------------
  
  /**
   * Get the position of the label in the memory. (Assumes the label exists and instructions are 4 bytes long)
   *
   * @param label Label name to search for. (example: "loop")
   *
   * @return Position of the label in the code, or -1 if the label does not exist.
   */
  public int getLabelPosition(String label)
  {
    Integer index = labels.get(label);
    if (index == null)
    {
      return -1;
    }
    return index * 4;
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return List of parsed instructions
   * @brief Get list of parsed instructions
   */
  public List<InputCodeModel> getParsedCode()
  {
    return parsedCode;
  }// end of getParsedCode
  //-------------------------------------------------------------------------------------------
  
  public void setParsedCode(List<InputCodeModel> parsedCode)
  {
    this.parsedCode = parsedCode;
  }
  
  /**
   * Get instruction at the given PC. Assumes an instruction is 4 bytes long.
   *
   * @param pc Program counter.
   *
   * @return Instruction at the given PC, or a nop if the PC is out of range.
   */
  public InputCodeModel getInstructionAt(int pc)
  {
    assert pc % 4 == 0;
    int index = pc / 4;
    // getParsedCode so it is mockable
    if (index < 0 || index >= getParsedCode().size())
    {
      return this.nop;
    }
    return getParsedCode().get(index);
  }
  
  /**
   * @return Error messages
   * @brief Get list of error messages in case of load failure
   */
  public List<ParseError> getErrorMessages()
  {
    return errorMessages;
  }// end of getErrorMessage
  
  public void setLabels(Map<String, Integer> labels)
  {
    this.labels = labels;
  }
  
  public InputCodeModel getNop()
  {
    return nop;
  }
}
