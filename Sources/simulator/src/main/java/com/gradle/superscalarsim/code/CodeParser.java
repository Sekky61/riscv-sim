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
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.loader.IDataProvider;
import com.gradle.superscalarsim.models.instruction.*;
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
 * Furthermore, after the initial parse, memory must be allocated (memoryLocations must be passed to {@link com.gradle.superscalarsim.cpu.MemoryInitializer}) to initialize the main memory.
 * Only then can the immediate values be filled using {@link #fillImmediateValues()}.
 */
public class CodeParser
{
  /**
   * Label consists of alphanumeric characters, underscores and dots. The ':' (colon) is not part of the label.
   */
  static Pattern labelPattern = Pattern.compile("^[a-zA-Z0-9_.]+$");
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
  Map<String, RegisterModel> registers;
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
  public CodeParser(IDataProvider dataProvider)
  {
    this(dataProvider.getInstructionFunctionModels(), dataProvider.getRegisterFile().getRegisterMap(true),
         new InputCodeModelFactory(), new ArrayList<>());
  }
  
  /**
   * @brief Constructor
   */
  public CodeParser(Map<String, InstructionFunctionModel> instructionModels,
                    Map<String, RegisterModel> registers,
                    InputCodeModelFactory manager,
                    List<MemoryLocation> memoryLocations)
  {
    
    this.instructionModels     = instructionModels;
    this.registers             = registers;
    this.inputCodeModelFactory = manager;
    this.memoryLocations       = new ArrayList<>();
    this.memoryLocations.addAll(memoryLocations);
    
    this.lexer         = null;
    this.errorMessages = new ArrayList<>();
    this.labels        = new HashMap<>();
  }
  
  /**
   * @return List of memory locations defined in the code
   */
  public List<MemoryLocation> getMemoryLocations()
  {
    return memoryLocations;
  }
  
  /**
   * Convenient shortcut for code that does not use external memory locations
   *
   * @param code ASM code
   */
  public void parseCode(String code)
  {
    parseCode(code, true);
  }
  
  /**
   * After calling this method, the results (labels, code, data) can be collected from the instance.
   *
   * @param code           ASM Code to parse
   * @param fillImmediates If true, the immediate values will be filled. If false, they must be filled manually
   *
   * @brief Parses the code
   */
  public void parseCode(String code, boolean fillImmediates)
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
      Label label = new Label(mem.getName(), -1);
      if (mem.names != null)
      {
        for (String alias : mem.names)
        {
          this.labels.put(alias, label);
        }
      }
    }
    
    this.instructions = new ArrayList<>();
    
    parse();
    
    // Now that we know the addresses of labels, we can fill in the immediate values
    // It must be called !after! allocating memory, because expressions may contain labels
    if (fillImmediates)
    {
      fillImmediateValues();
    }
    
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
    MemoryLocation mem         = new MemoryLocation();
    boolean        isDataLabel = false;
    
    // Go through the program
    // Take note of .byte, .hword, .word, .align, .ascii, .asciiz, .string, .skip, .zero
    for (String line : lines)
    {
      if ((!isDirective(line) || line.startsWith(".align")) && (!isLabel(line) || isDataLabel) && mem.getName() != null)
      {
        // Labeled memory ends. Save memory location.
        if (isDataLabel)
        {
          memoryLocations.add(mem);
          // Start new memory location
        }
        mem         = new MemoryLocation();
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
        mem.addName(labelName);
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
          if (mem.getName() == null)
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
          if (mem.getName() != null)
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
          isDataLabel = true;
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
    if (mem.getName() != null && isDataLabel)
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
      
      // Skip comment. Comment is also parsed in instruction parsing, where it can be a debug info.
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
    // End of code
  }
  
  /**
   * @brief Fills the constantValue field of immediate arguments based on the string representation of the value.
   * It is an expression of labels and constants.
   * Examples: "6", "0x6", "label1", "label1+4".
   */
  public void fillImmediateValues()
  {
    for (InputCodeModel instruction : instructions)
    {
      argloop:
      for (InputCodeArgument argument : instruction.arguments())
      {
        InstructionArgument argModel      = instruction.instructionFunctionModel()
                .getArgumentByName(argument.getName());
        String              argumentToken = argument.getValue();
        if (argModel.isImmediate())
        {
          String[] tokens = ExpressionEvaluator.tokenize(argumentToken);
          // Replace labels with numbers
          for (int i = 0; i < tokens.length; i++)
          {
            String token = tokens[i];
            if (ExpressionEvaluator.isLiteral(token) || ExpressionEvaluator.isOperator(token))
            {
              continue;
            }
            Label label = labels.get(token);
            if (label == null)
            {
              addError(new CodeToken(0, 0, argumentToken, CodeToken.Type.EOF),
                       "Label '" + argumentToken + "' is not defined");
              continue argloop;
            }
            // Replace label with its address
            assert label.getAddress() != -1; // code addresses must be known
            long address;
            if (argModel.isOffset())
            {
              // Offset must compute the difference between the label and the current instruction
              // and so cannot be linked to the label
              long offset = label.getAddress() - instruction.getPc();
              address = offset;
              argument.setConstantValue(RegisterDataContainer.fromValue(offset));
            }
            else
            {
              // If label is not defined, it will be caught later
              // This links the constant and the label through the shared reference
              argument.setConstantValue(label.getAddressContainer());
              address = label.getAddress();
            }
            tokens[i] = String.valueOf(address);
          }
          long evaluated = ExpressionEvaluator.evaluate(tokens);
          // May be a label or a constant
          RegisterDataContainer constantValue = RegisterDataContainer.parseAs(String.valueOf(evaluated),
                                                                              argModel.type());
          if (constantValue != null)
          {
            // Constant
            argument.setConstantValue(constantValue);
          }
          else
          {
            throw new RuntimeException("Unknown constant value");
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
   * @brief Parse label. Current token is the label name, colon is removed by lexer.
   * The label must already be defined in the labels map, here it is only assigned an address.
   */
  private void parseLabel()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.LABEL);
    
    String labelName = lexer.currentToken().text();
    
    // Check label syntax
    if (!labelPattern.matcher(labelName).matches())
    {
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
      
      // Maybe the instruction parsing failed. Skip until newline, comment, EOF
      while (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
              .equals(CodeToken.Type.EOF) && !lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
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
    return true;
  }// end of checkImmediateArgument
  
  /**
   * @brief Parse instruction. Current token is a symbol with the name of the instruction.
   * If there is a comment with debug info, it is attached to the instruction.
   * @details Collects arguments of an instruction from the token stream.
   * If the instruction is invalid, the token stream can be only partially consumed.
   */
  private void parseInstruction()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.SYMBOL);
    
    CodeToken                instructionNameToken = lexer.currentToken();
    String                   instructionName      = instructionNameToken.text();
    InstructionFunctionModel instructionModel     = instructionModels.get(instructionName);
    
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
    
    // Validate register arguments
    List<InputCodeArgument> codeArguments = new ArrayList<>();
    
    // Collect args, fill with defaults if needed
    // todo: are two kinds of syntax allowed for l/s instructions?: l/s rd,imm(rs1) and l/s rd,rs1,imm ??
    int numArguments       = collectedArgs.size();
    int collectedArgsIndex = 0;
    boolean useDefaultArgs = numArguments < instructionModel.getAsmArguments()
            .size() && instructionModel.hasDefaultArguments();
    for (InstructionArgument argument : instructionModel.arguments())
    {
      boolean   hasDefault = argument.defaultValue() != null;
      CodeToken argumentToken;
      if ((argument.silent() || useDefaultArgs) && hasDefault)
      {
        // Add default value to the args
        argumentToken = new CodeToken(0, 0, argument.defaultValue(), CodeToken.Type.EOF);
      }
      else if (collectedArgsIndex < collectedArgs.size())
      {
        // Use collected argument
        argumentToken = collectedArgs.get(collectedArgsIndex);
        collectedArgsIndex++;
      }
      else
      {
        // Missing argument
        addError(instructionNameToken, "Missing argument " + argument.name());
        return;
      }
      
      // Now check the argument
      
      String            argumentName      = argument.name();
      boolean           isValid           = true;
      InputCodeArgument inputCodeArgument = new InputCodeArgument(argumentName, argumentToken.text());
      if (argument.isRegister())
      {
        // Try to find the register. Its existence ic checked in the next step
        RegisterModel register = registers.get(argumentToken.text());
        inputCodeArgument.setRegisterValue(register);
        isValid = checkRegisterArgument(inputCodeArgument, argument.type(), argumentToken);
      }
      else if (!argument.isImmediate())
      {
        // Immediate values are checked later
        throw new RuntimeException("Unknown argument type");
      }
      if (isValid)
      {
        codeArguments.add(inputCodeArgument);
      }
    }
    
    if (collectedArgsIndex < collectedArgs.size())
    {
      // Not all arguments were used
      addError(instructionNameToken, "Too many arguments");
      return;
    }
    
    // Optional debug info
    DebugInfo debugInfo = null;
    if (lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
    {
      boolean isDebugInfo = lexer.currentToken().text().startsWith("DEBUG\"") && lexer.currentToken().text()
              .endsWith("\"");
      if (isDebugInfo)
      {
        // Filter out the meat
        String debugInfoStr = lexer.currentToken().text().substring(6, lexer.currentToken().text().length() - 1);
        debugInfo = new DebugInfo(debugInfoStr);
      }
      nextToken();
    }
    InputCodeModel inputCodeModel = inputCodeModelFactory.createInstance(instructionModel, codeArguments,
                                                                         instructions.size(), debugInfo);
    
    instructions.add(inputCodeModel);
  }
  
  /**
   * @return List of tokens representing arguments, null in case of error.
   * @brief Collects arguments of an instruction from the token stream.
   * @details Instructions with parentheses can also be written with commas (example: flw rd,imm(rs1) and also flw rd,imm,rs1).
   */
  private List<CodeToken> collectInstructionArgs()
  {
    // Parse NOTHING | (SYMBOL (SEPARATOR SYMBOL)*)  -- (also case with zero arguments)
    // TODO: find out if to allow parens only on load/store instructions
    List<CodeToken> collectedArgs = new ArrayList<>();
    
    // No arguments case
    if (!lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
    {
      return collectedArgs;
    }
    
    // 1+ arguments case
    boolean openParen         = false;
    boolean expectingArgState = true;
    boolean afterClosedParen  = false;
    while (true)
    {
      if (expectingArgState)
      {
        if (!lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
        {
          if (afterClosedParen)
          {
            // Closed paren can appear as the last argument
            break;
          }
          addError(lexer.currentToken(), "Expected argument, got " + lexer.currentToken().type());
          return null;
        }
        collectedArgs.add(lexer.currentToken());
        nextToken();
        expectingArgState = false; // Next a comma or parenthesis
        continue;
      }
      
      // Not expecting argument
      
      boolean isComma  = lexer.currentToken().type().equals(CodeToken.Type.COMMA);
      boolean isLParen = lexer.currentToken().type().equals(CodeToken.Type.L_PAREN);
      boolean isRParen = lexer.currentToken().type().equals(CodeToken.Type.R_PAREN);
      boolean isEnd = lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) || lexer.currentToken().type()
              .equals(CodeToken.Type.EOF) || lexer.currentToken().type().equals(CodeToken.Type.COMMENT);
      
      if (isEnd)
      {
        break;
      }
      
      if (openParen)
      {
        if (isLParen)
        {
          addError(lexer.currentToken(), "Unexpected open parenthesis");
          return null;
        }
        else if (isRParen)
        {
          expectingArgState = true;
          openParen         = false;
          afterClosedParen  = true;
        }
        else
        {
          addError(lexer.currentToken(), "Expected close parenthesis, got " + lexer.currentToken().type());
          return null;
        }
      }
      else if (isComma || isLParen)
      {
        expectingArgState = true;
        if (isLParen)
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
    
    if (openParen)
    {
      // Left unclosed
      addError(lexer.currentToken(), "Expected close parenthesis, got " + lexer.currentToken().type());
      return null;
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
    if (!register.canHold(argumentDataType))
    {
      this.addError(token,
                    "Argument \"" + argumentValue + "\" is a register of wrong type (incompatible with " + argumentDataType + ").");
      return false;
    }
    return true;
  }// end of checkRegisterArgument
  
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
