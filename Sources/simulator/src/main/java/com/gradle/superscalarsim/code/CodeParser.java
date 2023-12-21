/**
 * @file CodeParser.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains parser for parsing and validating ASM code
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

import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.IRegisterFile;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.gradle.superscalarsim.compiler.AsmParser.splitLines;

/**
 * @brief Transforms tokens from the lexer into instructions and labels
 * @details Class which provides parsing logic for code written in assembly. Code shall contain instructions loaded
 * by InitLoader. Class also verifies parsed code and in case of failure, error message will be provided with
 * line number and cause. Conforms to GCC RISC-V syntax.
 */
public class CodeParser
{
  /**
   * Descriptions of all instructions
   */
  Map<String, InstructionFunctionModel> instructionModels;
  
  /**
   * Descriptions of all memory locations defined in code
   */
  List<MemoryLocation> memoryLocations;
  
  /**
   * Descriptions of all register files
   */
  IRegisterFile registerFileModelList;
  
  /**
   * Factory for creating instances of InputCodeModel
   */
  InputCodeModelFactory inputCodeModelFactory;
  
  /**
   * Lexer for parsing the code
   */
  Lexer lexer;
  
  /**
   * Current token
   */
  CodeToken currentToken;
  
  /**
   * Peek token
   */
  CodeToken peekToken;
  
  /**
   * Result of the parsing - list of instructions.
   */
  List<InputCodeModel> instructions;
  
  /**
   * Result of the parsing - list of labels.
   * Some keys may point to the same label object.
   */
  Map<String, Label> labels;
  
  /**
   * Unconfirmed labels
   */
  List<CodeToken> unconfirmedLabels;
  
  /**
   * Error messages from parsing ASM code.
   *
   * @brief List of error messages
   */
  List<ParseError> errorMessages;
  
  Pattern decimalPattern;
  Pattern hexadecimalPattern;
  Pattern registerPattern;
  Pattern immediatePattern;
  
  /**
   * For cases when instance manager is not needed
   */
  public CodeParser(InitLoader initLoader)
  {
    this(initLoader.getInstructionFunctionModels(), initLoader.getRegisterFile(), null);
    // todo: maybe a dummyManager?
    this.inputCodeModelFactory = new InputCodeModelFactory();
  }
  
  /**
   * @brief Constructor
   */
  public CodeParser(Map<String, InstructionFunctionModel> instructionModels,
                    IRegisterFile registerFileModelList,
                    InputCodeModelFactory manager)
  {
    this.inputCodeModelFactory = manager;
    
    this.registerFileModelList = registerFileModelList;
    this.instructionModels     = instructionModels;
    this.lexer                 = null;
    this.currentToken          = null;
    this.peekToken             = null;
    this.errorMessages         = new ArrayList<>();
    this.unconfirmedLabels     = new ArrayList<>();
    
    this.memoryLocations = new ArrayList<>();
    
    this.decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    this.registerPattern    = Pattern.compile("r[d,s]\\d*");
    this.immediatePattern   = Pattern.compile("imm\\d*");
    
  }
  
  /**
   * More convenient constructor
   *
   * @brief Constructor when initloader is available
   */
  public CodeParser(InitLoader initLoader, InputCodeModelFactory manager)
  {
    this(initLoader.getInstructionFunctionModels(), initLoader.getRegisterFile(), manager);
  }
  
  /**
   * @return List of memory locations defined in the code
   */
  public List<MemoryLocation> getMemoryLocations()
  {
    return memoryLocations;
  }
  
  /**
   * After calling this method, the results can be collected from the instance.
   *
   * @param code ASM Code to parse
   */
  public void parseCode(String code)
  {
    parseCode(code, new HashMap<>());
  }
  
  /**
   * After calling this method, the results can be collected from the instance.
   *
   * @param code        ASM Code to parse
   * @param knownLabels List of labels that are known to exist in the code.
   *
   * @brief Parses the code
   */
  public void parseCode(String code, Map<String, Label> knownLabels)
  {
    // First, scan the code for directives like .asciiz, .word, etc. and note their locations and values
    // Then filter them out so lexer only sees instructions and labels
    this.errorMessages = new ArrayList<>();
    collectMemoryLocations(code);
    String filteredCode = filterDirectives(code);
    
    this.lexer        = new Lexer(filteredCode);
    this.currentToken = this.lexer.nextToken();
    this.peekToken    = this.lexer.nextToken();
    this.instructions = new ArrayList<>();
    this.labels       = new HashMap<>();
    // todo: Does not alias (two labels pointing to the same object)
    // Add known labels
    this.labels.putAll(knownLabels);
    this.unconfirmedLabels = new ArrayList<>();
    parse();
    
    // Delete code if errors
    if (!this.errorMessages.isEmpty())
    {
      this.instructions = new ArrayList<>();
      this.labels       = new HashMap<>();
    }
    
    // Add constantValues to labels in instructions
    // TODO: memory initializer would ideally have references to labels and change them directly
    for (InputCodeModel instruction : instructions)
    {
      for (InputCodeArgument argument : instruction.getArguments())
      {
        if (argument.getConstantValue() != null || !labels.containsKey(argument.getValue()))
        {
          continue;
        }
        RegisterDataContainer constantValue = new RegisterDataContainer();
        constantValue.setValue(labels.get(argument.getValue()).address);
        argument.setConstantValue(constantValue);
      }
    }
  }
  
  /**
   * Fills the memoryLocations list with memory locations defined in the code.
   * Does not understand expressions and many obscure directives.
   *
   * @param code ASM code
   */
  private void collectMemoryLocations(String code)
  {
    List<String>   lines = splitLines(code);
    MemoryLocation mem   = new MemoryLocation(null, 1);
    
    // Go through the program
    // Take note of .byte, .hword, .word, .align, .ascii, .asciiz, .skip, .zero
    for (String line : lines)
    {
      if (!isDirective(line))
      {
        // Save memory location when label ends
        if (mem.name != null)
        {
          // Is it a data label?
          if (!mem.getBytes().isEmpty())
          {
            memoryLocations.add(mem);
          }
          // Start new memory location
          mem = new MemoryLocation(null, 1);
        }
        if (isLabel(line))
        {
          // todo multiple labels on one line, multiple labels on multiple lines
          mem.name = line.substring(0, line.length() - 1);
        }
        continue;
      }
      String[] tokens = line.split("[\\s,]+");
      
      String directive = tokens[0];
      int    argCount  = tokens.length - 1;
      switch (directive)
      {
        case ".byte", ".hword", ".word" ->
        {
          if (argCount == 0)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF),
                     directive + " expected at least 1 argument, got " + argCount);
            break;
          }
          if (mem.name == null)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF), directive + " expected label before it");
            break;
          }
          DataTypeEnum t = switch (directive)
          {
            case ".byte" -> DataTypeEnum.kByte;
            case ".hword" -> DataTypeEnum.kShort;
            case ".word" -> DataTypeEnum.kInt;
            default -> null;
          };
          mem.addDataChunk(t);
          for (int j = 1; j < tokens.length; j++)
          {
            mem.addValue(tokens[j]);
          }
        }
        case ".align" ->
        {
          if (argCount != 1)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF), ".align expected 1 argument, got " + argCount);
            break;
          }
          if (mem.name != null)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF), ".align expected before a label");
          }
          mem.alignment = Integer.parseInt(tokens[1]);
        }
        case ".ascii", ".asciiz" ->
        {
          mem.addDataChunk(DataTypeEnum.kChar);
          if (argCount == 0)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF),
                     ".ascii expected at least 1 argument, got " + argCount);
            break;
          }
          
          for (int j = 1; j < tokens.length; j++)
          {
            String token = tokens[j];
            if (!token.startsWith("\"") || !token.endsWith("\""))
            {
              addError(new CodeToken(0, 0, token, CodeToken.Type.EOF), "Expected string literal, got " + token);
              break;
            }
            token = token.substring(1, token.length() - 1);
            for (char c : token.toCharArray())
            {
              mem.addValue(c + "");
            }
            if (directive.equals(".asciiz"))
            {
              mem.addValue("\0");
            }
          }
        }
        case ".skip", ".zero" ->
        {
          // .zero is an alias for .skip
          mem.addDataChunk(DataTypeEnum.kByte);
          if (argCount != 1 && argCount != 2)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF), ".skip expected 1 argument, got " + argCount);
            break;
          }
          int    size = Integer.parseInt(tokens[1]);
          String fill = "0";
          if (argCount == 2)
          {
            fill = tokens[2];
          }
          for (int j = 0; j < size; j++)
          {
            mem.addValue(fill);
          }
        }
      }
    }
    // Save last memory location
    if (mem.name != null)
    {
      memoryLocations.add(mem);
    }
  }
  
  private String filterDirectives(String code)
  {
    List<String>  lines   = splitLines(code);
    StringBuilder builder = new StringBuilder();
    for (String line : lines)
    {
      // do not filter out labels
      if (line.startsWith(".") && !line.endsWith(":"))
      {
        // Directive
        continue;
      }
      builder.append(line).append("\n");
    }
    return builder.toString();
  }
  
  /**
   * Creates list of instructions, labels, errors (mutates the state of the parser).
   *
   * @brief Parses the code
   */
  private void parse()
  {
    assert lexer != null;
    assert currentToken != null;
    
    while (!currentToken.type().equals(CodeToken.Type.EOF))
    {
      // Label
      if (peekToken.type().equals(CodeToken.Type.COLON))
      {
        parseLabel();
        // Allow multiple labels on a line. Start over
        continue;
      }
      
      // Directive or instruction
      if (currentToken.type().equals(CodeToken.Type.SYMBOL))
      {
        parseDirectiveOrInstruction();
      }
      
      // Skip comment
      if (currentToken.type().equals(CodeToken.Type.COMMENT))
      {
        nextToken();
      }
      
      // If instruction parsing ended early, skip to the next line
      if (!currentToken.type().equals(CodeToken.Type.NEWLINE) && !currentToken.type().equals(CodeToken.Type.EOF))
      {
        addError(currentToken, "Expected newline, got " + currentToken.type());
      }
      
      while (!currentToken.type().equals(CodeToken.Type.NEWLINE) && !currentToken.type().equals(CodeToken.Type.EOF))
      {
        nextToken();
      }
      nextToken();
    }
    
    // Check if all labels are defined
    for (CodeToken unconfirmedLabel : unconfirmedLabels)
    {
      addError(unconfirmedLabel, "Label '" + unconfirmedLabel.text() + "' is not defined");
    }
  }
  
  /**
   * @return True if the line is a directive, false otherwise
   */
  private boolean isDirective(String line)
  {
    return line.startsWith(".") && !line.endsWith(":");
  }
  
  private boolean isLabel(String line)
  {
    return line.endsWith(":");
  }
  
  /**
   * @param token   Token where the error occurred
   * @param message Error message
   *
   * @brief Adds a single-line error message to the list of errors
   */
  private void addError(CodeToken token, String message)
  {
    this.errorMessages.add(new ParseError("error", message, token.line(), token.columnStart(), token.columnEnd()));
  }
  
  /**
   * @brief Parse label. Current token is the label name, peek token is colon.
   */
  private void parseLabel()
  {
    assert currentToken.type().equals(CodeToken.Type.SYMBOL);
    assert peekToken.type().equals(CodeToken.Type.COLON);
    
    String labelName = currentToken.text();
    
    // Check label syntax
    if (!labelName.matches("^[a-zA-Z0-9_\\.]+$"))
    {
      addError(currentToken, "Invalid label name '" + labelName + "'");
    }
    
    // Check if label is already defined
    if (labels.containsKey(labelName))
    {
      addError(currentToken, "Label '" + labelName + "' already defined");
    }
    else
    {
      // Add label, this is in "bytes"
      // If there already is a label with this name, throw
      if (labels.containsKey(labelName))
      {
        addError(currentToken, "Label '" + labelName + "' already defined");
      }
      // If there already is a label with this address, point to it
      Label lab = null;
      for (Label label : labels.values())
      {
        if (label.address == instructions.size() * 4)
        {
          lab = label;
          break;
        }
      }
      if (lab == null)
      {
        lab = new Label(labelName, instructions.size() * 4);
      }
      labels.put(labelName, lab);
      // Clear unconfirmed labels
      unconfirmedLabels.removeIf(token -> token.text().equals(labelName));
    }
    
    // Consume label name and colon
    nextToken();
    nextToken();
  }
  
  /**
   * @brief Parse directive or instruction. Current token is a symbol.
   */
  private void parseDirectiveOrInstruction()
  {
    assert currentToken.type().equals(CodeToken.Type.SYMBOL);
    
    String symbolName = currentToken.text();
    
    // Check if symbol is a directive
    if (symbolName.startsWith("."))
    {
      parseDirective();
    }
    else
    {
      parseInstruction();
      
      // Skip to the next line
      while (!currentToken.type().equals(CodeToken.Type.NEWLINE) && !currentToken.type().equals(CodeToken.Type.EOF))
      {
        nextToken();
      }
    }
  }
  
  /**
   * @brief Load next token
   */
  private void nextToken()
  {
    this.currentToken = this.peekToken;
    this.peekToken    = this.lexer.nextToken();
  }
  
  private void parseDirective()
  {
    // TODO: Implement
  }
  
  /**
   * @brief Parse instruction. Current token is a symbol with the name of the instruction.
   */
  private void parseInstruction()
  {
    assert currentToken.type().equals(CodeToken.Type.SYMBOL);
    
    CodeToken                instructionNameToken = currentToken;
    String                   instructionName      = instructionNameToken.text();
    InstructionFunctionModel instructionModel     = getInstructionFunctionModel(instructionName);
    
    // Check if instruction is valid
    if (instructionModel == null)
    {
      addError(currentToken, "Unknown instruction '" + instructionName + "'");
      return;
    }
    
    // Consume instruction name
    nextToken();
    
    // Collect arguments
    List<CodeToken> collectedArgs = collectInstructionArgs();
    if (collectedArgs == null)
    {
      return;
    }
    
    // Collect args, fill with defaults if needed
    Map<String, CodeToken> args               = new HashMap<>();
    int                    numArguments       = collectedArgs.size();
    int                    collectedArgsIndex = 0;
    boolean useDefaultArgs = numArguments < instructionModel.getAsmArguments()
            .size() && instructionModel.hasDefaultArguments();
    for (InstructionFunctionModel.Argument argument : instructionModel.getArguments())
    {
      String  key        = argument.name();
      boolean hasDefault = argument.defaultValue() != null;
      if (argument.silent())
      {
        // Add to the args, assumes all silent arguments have default values
        args.put(key, new CodeToken(0, 0, argument.defaultValue(), CodeToken.Type.EOF));
      }
      else if (useDefaultArgs && hasDefault)
      {
        // Use default argument
        args.put(key, new CodeToken(0, 0, argument.defaultValue(), CodeToken.Type.EOF));
      }
      else if (collectedArgsIndex < collectedArgs.size())
      {
        // Use collected argument
        args.put(key, collectedArgs.get(collectedArgsIndex));
        collectedArgsIndex++;
      }
      else
      {
        // Missing argument
        addError(instructionNameToken, "Missing argument " + argument.name());
        return;
      }
    }
    
    if (collectedArgsIndex < collectedArgs.size())
    {
      // Not all arguments were used
      addError(instructionNameToken, "Too many arguments");
      return;
    }
    
    // Validate arguments
    List<InputCodeArgument> codeArguments = new ArrayList<>();
    for (InstructionFunctionModel.Argument argument : instructionModel.getArguments())
    {
      CodeToken argumentToken = args.get(argument.name());
      String    argumentName  = argument.name();
      // Try to find the register
      boolean           isValid             = true;
      boolean           isLValue            = argumentName.equals("rd");
      InputCodeArgument inputCodeArgument   = null;
      DataTypeEnum      instructionDataType = instructionModel.getArgumentByName(argumentName).type();
      if (argument.isRegister())
      {
        RegisterModel register = registerFileModelList.getRegister(argumentToken.text());
        inputCodeArgument = new InputCodeArgument(argumentName, argumentToken.text(), register);
        isValid &= checkRegisterArgument(inputCodeArgument, instructionDataType, argumentToken);
      }
      else if (argument.isImmediate())
      {
        // May be a label or a constant
        RegisterDataContainer constantValue = RegisterDataContainer.parseAs(argumentToken.text(), argument.type());
        if (constantValue != null)
        {
          // Constant
          inputCodeArgument = new InputCodeArgument(argumentName, constantValue);
        }
        else
        {
          // Label
          inputCodeArgument = new InputCodeArgument(argumentName, argumentToken.text());
        }
        isValid &= checkImmediateArgument(inputCodeArgument, isLValue, argumentToken);
      }
      else
      {
        throw new RuntimeException("Unknown argument type");
      }
      if (isValid)
      {
        codeArguments.add(inputCodeArgument);
      }
    }
    
    InputCodeModel inputCodeModel = inputCodeModelFactory.createInstance(instructionModel, codeArguments,
                                                                         instructions.size());
    instructions.add(inputCodeModel);
  }
  
  private InstructionFunctionModel getInstructionFunctionModel(String instructionName)
  {
    return instructionModels.get(instructionName);
  }
  
  /**
   * @return List of tokens representing arguments, null in case of error
   * @brief Collects arguments of an instruction from the token stream
   */
  private List<CodeToken> collectInstructionArgs()
  {
    // TODO: bugs like: addi x1(x2(x3
    boolean         openParen         = false;
    boolean         expectingArgState = true;
    boolean         followsSeparator  = false;
    List<CodeToken> collectedArgs     = new ArrayList<>();
    while (true)
    {
      // instructions with parentheses can also be written with commas
      // example: flw rd,imm(rs1) also flw rd,imm,rs1
      
      if (expectingArgState)
      {
        if (!currentToken.type().equals(CodeToken.Type.SYMBOL))
        {
          if (followsSeparator)
          {
            addError(currentToken, "Expected argument, got " + currentToken.type());
            return null;
          }
          else
          {
            // Not an error (zero arguments?)
            return collectedArgs;
          }
        }
        collectedArgs.add(currentToken);
        nextToken();
        expectingArgState = false; // Now a comma
        followsSeparator  = false;
        continue;
      }
      
      // Not expecting argument
      
      boolean isComma      = currentToken.type().equals(CodeToken.Type.COMMA);
      boolean isParen      = currentToken.type().equals(CodeToken.Type.L_PAREN);
      boolean isCloseParen = currentToken.type().equals(CodeToken.Type.R_PAREN);
      boolean isEnd = currentToken.type().equals(CodeToken.Type.NEWLINE) || currentToken.type()
              .equals(CodeToken.Type.EOF) || currentToken.type().equals(CodeToken.Type.COMMENT);
      
      if (isEnd)
      {
        break;
      }
      
      if (openParen && isCloseParen)
      {
        openParen = false;
      }
      else if (isComma || isParen)
      {
        expectingArgState = true;
        followsSeparator  = true;
        if (isParen)
        {
          openParen = true;
        }
      }
      else
      {
        // Give up, next line
        addError(currentToken, "Expected comma or parenthesis, got " + currentToken.type());
        return null;
      }
      nextToken();
    }
    return collectedArgs;
  }
  
  /**
   * @param argument         Argument to be verified. Must be a register, but register can be null
   * @param argumentDataType Data type of argument according to instruction template
   * @param token            Token of the argument, for error reporting
   *
   * @return True if register argument is valid, false otherwise
   * @brief Verifies if argument that is supposed to be a register is valid
   */
  private boolean checkRegisterArgument(final InputCodeArgument argument,
                                        final DataTypeEnum argumentDataType,
                                        CodeToken token)
  {
    String        argumentValue = token.text();
    RegisterModel register      = argument.getRegisterValue();
    if (register == null)
    {
      this.addError(token, "Argument \"" + argumentValue + "\" is not a register nor a value.");
      return false;
    }
    if (!checkDatatype(argumentDataType, register.getType()))
    {
      this.addError(token, "Argument \"" + argumentValue + "\" is a register of wrong type.");
      return false;
    }
    return true;
  }// end of checkRegisterArgument
  
  /**
   * @param argument Argument to be verified. Must be an immediate value, either a label or a constant
   * @param isLValue True if the argument is a lvalue, false otherwise
   * @param token    Token of the argument, for error reporting
   *
   * @return True if argument is valid immediate value, otherwise false
   * @brief Verifies if argument is immediate value
   */
  private boolean checkImmediateArgument(final InputCodeArgument argument, final boolean isLValue, CodeToken token)
  {
    boolean isDirectValue = isNumeralLiteral(token.text());
    if (isLValue)
    {
      this.addError(token, "LValue cannot be immediate value.");
      return false;
    }
    
    if (!isDirectValue && !this.labels.containsKey(argument.getValue()))
    {
      this.unconfirmedLabels.add(token);
    }
    
    return true;
  }// end of checkImmediateArgument
  
  /**
   * @param argumentDataType The datatype of an argument
   * @param registerDataType The datatype of a register file
   *
   * @return True if argument is compatible with register, otherwise false
   * @brief Check argument and register data types if they fit within each other
   */
  private boolean checkDatatype(final DataTypeEnum argumentDataType, final RegisterTypeEnum registerDataType)
  {
    // TODO: move this
    return switch (argumentDataType)
    {
      case kInt, kUInt, kLong, kULong, kBool, kByte, kShort, kChar -> registerDataType == RegisterTypeEnum.kInt;
      case kFloat, kDouble -> registerDataType == RegisterTypeEnum.kFloat;
    };
  }// end of checkDatatype
  
  private boolean isNumeralLiteral(String argValue)
  {
    return this.hexadecimalPattern.matcher(argValue).matches() || this.decimalPattern.matcher(argValue).matches();
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return True if the parsing was successful, false otherwise
   */
  public boolean success()
  {
    return this.errorMessages.isEmpty();
  }
  
  public List<InputCodeModel> getInstructions()
  {
    return instructions;
  }
  
  public Map<String, Label> getLabels()
  {
    return labels;
  }
  
  public List<ParseError> getErrorMessages()
  {
    return errorMessages;
  }
}
