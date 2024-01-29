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
import com.gradle.superscalarsim.models.instruction.DebugInfo;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
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
 * @brief Transforms tokens from the lexer into instructions and labels. Also collects defined data.
 * @details Class which provides parsing logic for code written in assembly. Code shall contain instructions loaded
 * by InitLoader. Class also verifies parsed code and in case of failure, error message will be provided with
 * line number and cause. Conforms to GCC RISC-V syntax.
 * <p>
 * If the code uses memory defined outside the code, it must be supplied in the constructor.
 * After parsing, the memoryLocations can be passed to {@link com.gradle.superscalarsim.cpu.MemoryInitializer} to initialize the main memory.
 */
public class CodeParser
{
  static Pattern decimalPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
  static Pattern hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
  static Pattern labelPattern = Pattern.compile("^[a-zA-Z0-9_\\.]+$");
  /**
   * Descriptions of all instructions
   */
  Map<String, InstructionFunctionModel> instructionModels;
  /**
   * Descriptions of all memory locations defined in code
   */
  List<MemoryLocation> memoryLocations;
  /**
   * Descriptions of all register files. Links arguments to registers.
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
   * Result of the parsing - list of instructions.
   */
  List<InputCodeModel> instructions;
  /**
   * Result of the parsing - list of labels.
   * Some keys may point to the same label object.
   */
  Map<String, Label> labels;
  /**
   * Error messages from parsing ASM code.
   *
   * @brief List of error messages
   */
  List<ParseError> errorMessages;
  
  /**
   * For cases when instance manager is not needed
   */
  public CodeParser(InitLoader initLoader)
  {
    this(initLoader.getInstructionFunctionModels(), initLoader.getRegisterFile(), new InputCodeModelFactory(),
         new ArrayList<>());
  }
  
  /**
   * @brief Constructor
   */
  public CodeParser(Map<String, InstructionFunctionModel> instructionModels,
                    IRegisterFile registerFileModelList,
                    InputCodeModelFactory manager,
                    List<MemoryLocation> memoryLocations)
  {
    this.inputCodeModelFactory = manager;
    
    this.registerFileModelList = registerFileModelList;
    this.instructionModels     = instructionModels;
    this.lexer                 = null;
    this.errorMessages         = new ArrayList<>();
    
    // Copy list, but not the objects
    this.labels          = new HashMap<>();
    this.memoryLocations = new ArrayList<>();
    this.memoryLocations.addAll(memoryLocations);
  }
  
  /**
   * For cases when instance manager is not needed, but memory locations are needed.
   */
  public CodeParser(InitLoader initLoader, List<MemoryLocation> memoryLocations)
  {
    this(initLoader.getInstructionFunctionModels(), initLoader.getRegisterFile(), new InputCodeModelFactory(),
         memoryLocations);
  }
  
  /**
   * @return List of memory locations defined in the code
   */
  public List<MemoryLocation> getMemoryLocations()
  {
    return memoryLocations;
  }
  
  /**
   * After calling this method, the results (labels, code, data) can be collected from the instance.
   *
   * @param code ASM Code to parse
   *
   * @brief Parses the code
   */
  public void parseCode(String code)
  {
    // First, scan the code for directives like .asciiz, .word, etc. and note their locations and values
    // Then filter them out so lexer only sees instructions and labels
    this.errorMessages = new ArrayList<>();
    collectMemoryLocations(code);
    String filteredCode = filterDirectives(code);
    // memoryLocations now has ALL memory locations, including those defined in the code and config
    
    this.lexer = new Lexer(filteredCode);
    // Collect labels before parsing instructions
    collectCodeLabels();
    // Memory location names must also be known as labels
    for (MemoryLocation mem : memoryLocations)
    {
      // Address not yet known
      Label label = new Label(mem.name, -1);
      this.labels.put(mem.name, label);
      for (String alias : mem.aliases)
      {
        this.labels.put(alias, label);
      }
    }
    
    this.instructions = new ArrayList<>();
    
    parse();
    
    // Now that we know the addresses of labels, we can fill in the immediate values
    fillImmediateValues();
    
    // Delete code if errors
    if (!this.errorMessages.isEmpty())
    {
      this.instructions = new ArrayList<>();
      this.labels       = new HashMap<>();
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
    List<String>   lines       = splitLines(code);
    MemoryLocation mem         = new MemoryLocation(null, 1);
    boolean        isDataLabel = false;
    
    // Go through the program
    // Take note of .byte, .hword, .word, .align, .ascii, .asciiz, .string, .skip, .zero
    for (String line : lines)
    {
      if ((!isDirective(line) || line.startsWith(".align")) && (!isLabel(line) || isDataLabel) && mem.name != null)
      {
        // Labeled memory ends. Save memory location.
        if (isDataLabel)
        {
          memoryLocations.add(mem);
          // Start new memory location
        }
        mem         = new MemoryLocation(null, 1);
        isDataLabel = false;
        // We still want to parse .align and labels
        if (!line.startsWith(".align") && !isLabel(line))
        {
          continue;
        }
      }
      
      if (isLabel(line))
      {
        // todo multiple labels on one line
        String labelName = line.substring(0, line.length() - 1);
        if (mem.name == null)
        {
          mem.name = labelName;
        }
        else
        {
          mem.addAlias(labelName);
        }
        continue;
      }
      
      // Split, but do not split strings such as "Hello, world!"
      List<String> tokens = tokenizeLine(line); //line.split("[\\s,]+");
      
      String directive = tokens.get(0);
      int    argCount  = tokens.size() - 1;
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
          isDataLabel = true;
          for (int j = 1; j < tokens.size(); j++)
          {
            // It may be a label
            mem.addValue(tokens.get(j));
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
          mem.alignment = Integer.parseInt(tokens.get(1));
        }
        case ".ascii", ".asciiz", ".string" ->
        {
          // todo: escape sequences (https://ftp.gnu.org/old-gnu/Manuals/gas-2.9.1/html_chapter/as_3.html#SEC33)
          mem.addDataChunk(DataTypeEnum.kChar);
          isDataLabel = true;
          if (argCount == 0)
          {
            addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF),
                     directive + " expected at least 1 argument, got " + argCount);
            break;
          }
          
          for (int j = 1; j < tokens.size(); j++)
          {
            String token = tokens.get(j);
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
            if (directive.equals(".asciiz") || directive.equals(".string"))
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
          int    size = Integer.parseInt(tokens.get(1));
          String fill = "0";
          if (argCount == 2)
          {
            fill = tokens.get(2);
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
  
  /**
   * @return The code, without directives (data) and without labels pointing to data
   */
  private String filterDirectives(String code)
  {
    String[]      lines   = code.split("\n");
    StringBuilder builder = new StringBuilder();
    for (String line : lines)
    {
      String trimmedLine = line.trim();
      // do not filter out labels
      if (trimmedLine.startsWith(".") && !trimmedLine.endsWith(":"))
      {
        // Directive
        continue;
      }
      builder.append(line).append("\n");
    }
    return builder.toString();
  }
  
  private void collectCodeLabels()
  {
    List<CodeToken> tokens = lexer.getTokens();
    for (CodeToken token : tokens)
    {
      if (token.type().equals(CodeToken.Type.LABEL))
      {
        String labelName = token.text();
        if (labels.containsKey(labelName))
        {
          addError(token, "Label '" + labelName + "' already defined");
          continue;
        }
        // todo multiple labels pointing to the same address/instruction - alias in the map
        // Add label, this is in "bytes"
        Label lab = new Label(labelName, -1);
        labels.put(labelName, lab);
      }
    }
  }
  
  /**
   * Creates list of instructions, labels, errors (mutates the state of the parser).
   *
   * @brief Parses the code
   */
  private void parse()
  {
    assert lexer != null;
    
    while (!lexer.currentToken().type().equals(CodeToken.Type.EOF))
    {
      // Label
      if (lexer.currentToken().type().equals(CodeToken.Type.LABEL))
      {
        parseLabel();
        // Allow multiple labels on a line. Start over
        continue;
      }
      
      // Directive or instruction
      if (lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
      {
        parseDirectiveOrInstruction();
      }
      
      // Skip comment
      if (lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
      {
        nextToken();
      }
      
      // If instruction parsing ended early, skip to the next line
      if (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
              .equals(CodeToken.Type.EOF))
      {
        addError(lexer.currentToken(), "Expected newline, got " + lexer.currentToken().type());
      }
      
      while (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
              .equals(CodeToken.Type.EOF))
      {
        nextToken();
      }
      nextToken();
    }
    
    // Check if all labels are defined
    for (Label unconfirmedLabel : labels.values())
    {
      if (unconfirmedLabel.getAddress() == -1)
      {
        addError(new CodeToken(0, 0, unconfirmedLabel.name, CodeToken.Type.EOF),
                 "Label '" + unconfirmedLabel.name + "' is not defined");
      }
    }
  }
  
  private void fillImmediateValues()
  {
    for (InputCodeModel instruction : instructions)
    {
      for (InputCodeArgument argument : instruction.getArguments())
      {
        InstructionFunctionModel.Argument argModel = instruction.getInstructionFunctionModel()
                .getArgumentByName(argument.getName());
        String argumentToken = argument.getValue();
        if (argModel.isImmediate())
        {
          // May be a label or a constant
          RegisterDataContainer constantValue = RegisterDataContainer.parseAs(argumentToken, argModel.type());
          if (constantValue != null)
          {
            // Constant
            argument.setConstantValue(constantValue);
          }
          else
          {
            // Label, save both string and constant value
            Label label = labels.get(argumentToken);
            if (label == null)
            {
              addError(new CodeToken(0, 0, argumentToken, CodeToken.Type.EOF),
                       "Label '" + argumentToken + "' is not defined");
              continue;
            }
            assert label.getAddress() != -1; // code addresses must be known
            if (argModel.isOffset())
            {
              // Offset must compute the difference between the label and the current instruction
              // and so cannot be linked to the label
              long offset = label.getAddress() - instruction.getPc();
              argument.setConstantValue(RegisterDataContainer.fromValue(offset));
            }
            else
            {
              // If label is not defined, it will be caught later
              // This links the constant and the label through the shared reference
              argument.setConstantValue(label.getAddressContainer());
            }
            
          }
          boolean isLValue = argument.getName().equals("rd");
          checkImmediateArgument(argument, isLValue, new CodeToken(0, 0, argumentToken, CodeToken.Type.EOF));
        }
      }
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
   * @param line Line to tokenize
   *
   * @return Tokens of the assembly line, split by spaces and commas, but not inside strings
   */
  private List<String> tokenizeLine(String line)
  {
    List<String>  tokens       = new ArrayList<>();
    StringBuilder currentToken = new StringBuilder();
    boolean       inString     = false;
    for (char c : line.toCharArray())
    {
      if (c == '"')
      {
        inString = !inString;
      }
      if (c == ',' && !inString)
      {
        tokens.add(currentToken.toString());
        currentToken = new StringBuilder();
      }
      else if (c == ' ' && !inString)
      {
        if (!currentToken.isEmpty())
        {
          tokens.add(currentToken.toString());
          currentToken = new StringBuilder();
        }
      }
      else
      {
        currentToken.append(c);
      }
    }
    if (!currentToken.isEmpty())
    {
      tokens.add(currentToken.toString());
    }
    return tokens;
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
    assert lexer.currentToken().type().equals(CodeToken.Type.LABEL);
    
    String labelName = lexer.currentToken().text();
    
    // Check label syntax
    if (!labelPattern.matcher(labelName).matches())
    {
      // todo test
      addError(lexer.currentToken(), "Invalid label name '" + labelName + "'");
    }
    
    assert labels.containsKey(labelName);
    
    labels.get(labelName).getAddressContainer().setValue(instructions.size() * 4);
    
    // Consume label token
    nextToken();
  }
  
  /**
   * @brief Parse directive or instruction. Current token is a symbol.
   */
  private void parseDirectiveOrInstruction()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.SYMBOL);
    
    String symbolName = lexer.currentToken().text();
    
    // Check if symbol is a directive
    if (!symbolName.startsWith("."))
    {
      parseInstruction();
      
      // Skip to the next line
      while (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
              .equals(CodeToken.Type.EOF))
      {
        nextToken();
      }
    }
    // todo maybe filtering would not be necessary if directives were parsed here
  }
  
  /**
   * @brief Load next token
   */
  private void nextToken()
  {
    this.lexer.nextToken();
  }
  
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
    if (isLValue)
    {
      this.addError(token, "LValue cannot be immediate value.");
      return false;
    }
    
    boolean isDirectValue = isNumeralLiteral(token.text());
    if (!isDirectValue && !this.labels.containsKey(argument.getValue()))
    {
      addError(token, "Argument \"" + argument.getValue() + "\" is not a label or a constant.");
    }
    
    return true;
  }// end of checkImmediateArgument
  
  /**
   * @brief Parse instruction. Current token is a symbol with the name of the instruction.
   * If there is a comment with debug info, it is attached to the instruction.
   */
  private void parseInstruction()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.SYMBOL);
    
    CodeToken                instructionNameToken = lexer.currentToken();
    String                   instructionName      = instructionNameToken.text();
    InstructionFunctionModel instructionModel     = getInstructionFunctionModel(instructionName);
    
    // Check if instruction is valid
    if (instructionModel == null)
    {
      addError(lexer.currentToken(), "Unknown instruction '" + instructionName + "'");
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
        isValid           = checkRegisterArgument(inputCodeArgument, instructionDataType, argumentToken);
      }
      else if (argument.isImmediate())
      {
        // check later
        inputCodeArgument = new InputCodeArgument(argumentName, argumentToken.text());
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
    
    // Optional debug info
    if (lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
    {
      boolean isDebugInfo = lexer.currentToken().text().startsWith("DEBUG\"") && lexer.currentToken().text()
              .endsWith("\"");
      if (isDebugInfo)
      {
        // Filter out the meat
        String debugInfo = lexer.currentToken().text().substring(6, lexer.currentToken().text().length() - 1);
        inputCodeModel.setDebugInfo(new DebugInfo(debugInfo));
      }
      nextToken();
    }
    
    instructions.add(inputCodeModel);
  }
  
  private boolean isNumeralLiteral(String argValue)
  {
    return hexadecimalPattern.matcher(argValue).matches() || decimalPattern.matcher(argValue).matches();
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
        if (!lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
        {
          if (followsSeparator)
          {
            addError(lexer.currentToken(), "Expected argument, got " + lexer.currentToken().type());
            return null;
          }
          else
          {
            // Not an error (zero arguments?)
            return collectedArgs;
          }
        }
        collectedArgs.add(lexer.currentToken());
        nextToken();
        expectingArgState = false; // Now a comma
        followsSeparator  = false;
        continue;
      }
      
      // Not expecting argument
      
      boolean isComma      = lexer.currentToken().type().equals(CodeToken.Type.COMMA);
      boolean isParen      = lexer.currentToken().type().equals(CodeToken.Type.L_PAREN);
      boolean isCloseParen = lexer.currentToken().type().equals(CodeToken.Type.R_PAREN);
      boolean isEnd = lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) || lexer.currentToken().type()
              .equals(CodeToken.Type.EOF) || lexer.currentToken().type().equals(CodeToken.Type.COMMENT);
      
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
        addError(lexer.currentToken(), "Expected comma or parenthesis, got " + lexer.currentToken().type());
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
      this.addError(token, "Argument \"" + argumentValue + "\" is not a register.");
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
