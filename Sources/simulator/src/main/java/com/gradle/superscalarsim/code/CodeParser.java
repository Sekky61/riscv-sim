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
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.regex.Pattern;

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
   * Result of the parsing - list of labels (to code or data).
   * Some keys may point to the same label object.
   * Handles forward references. Is checked after parsing for errors.
   */
  Map<String, Symbol> symbolTable;
  /**
   * Error messages from parsing ASM code.
   *
   * @brief List of error messages. Contains warnings and errors. Warnings do not fail the compilation.
   */
  List<ParseError> errorMessages;
  
  /**
   * Last defined symbol. Used for assigning data to labels.
   */
  String lastSymbol;
  /**
   * True if the lastSymbol is bound to an object. Used for pointing several symbols to the same object.
   */
  boolean lastSymbolBound;
  
  /**
   * Alignment of the next object. Default is 0 (2^0 = 1 byte alignment).
   */
  int nextAlignment;
  
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
    
    this.symbolTable = new HashMap<>();
    if (memoryLocations != null)
    {
      for (MemoryLocation mem : memoryLocations)
      {
        Symbol symbol = new Symbol(mem.getName(), Symbol.SymbolType.DATA, new RegisterDataContainer(), mem);
        assert mem.names != null;
        for (String alias : mem.names)
        {
          this.symbolTable.put(alias, symbol);
        }
      }
    }
    
    this.lexer           = null;
    this.errorMessages   = new ArrayList<>();
    this.lastSymbol      = null;
    this.nextAlignment   = 0;
    this.lastSymbolBound = true; // Starts as true, because the first label is created to be bound to nothing
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
    // memoryLocations now has ALL memory locations, including those defined in the code and config
    
    this.lexer        = new Lexer(code);
    this.instructions = new ArrayList<>();
    
    parse();
    
    // Now that we know the addresses of labels, we can fill in the immediate values
    // It must be called !after! allocating memory, because expressions may contain labels
    if (fillImmediates)
    {
      fillImmediateValues();
    }
    
    // Delete code if errors
    if (containsErrors())
    {
      this.instructions = new ArrayList<>();
      this.symbolTable  = new HashMap<>();
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
      
      // Directive or instruction. Not both.
      if (lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
      {
        parseInstruction();
        
        // Maybe the instruction parsing failed. Skip until newline, comment, EOF
        while (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
                .equals(CodeToken.Type.EOF) && !lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
        {
          nextToken();
        }
      }
      else if (lexer.currentToken().type().equals(CodeToken.Type.DIRECTIVE))
      {
        parseDirective();
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
   * @brief Fills the missing constantValue field of immediate arguments based on the string representation of the value.
   * Missing should be only arithmetic expressions like "ptr+4".
   */
  public void fillImmediateValues()
  {
    for (InputCodeModel instruction : instructions)
    {
      argloop:
      for (InputCodeArgument argument : instruction.arguments())
      {
        InstructionArgument argModel = instruction.instructionFunctionModel().getArgumentByName(argument.getName());
        if (!argModel.isImmediate() || argument.getConstantValue() != null)
        {
          // Not constant or already filled
          continue;
        }
        CodeToken valueToken    = argument.getValueToken();
        String    argumentToken = valueToken.text();
        String[]  tokens        = ExpressionEvaluator.tokenize(argumentToken);
        // Replace labels with numbers
        for (int i = 0; i < tokens.length; i++)
        {
          String token = tokens[i];
          if (ExpressionEvaluator.isLiteral(token) || ExpressionEvaluator.isOperator(token))
          {
            continue;
          }
          Symbol symbol = symbolTable.get(token);
          if (symbol == null || symbol.getValue() == null)
          {
            addWarning(valueToken, "Symbol '" + argumentToken + "' is not defined");
            continue argloop;
          }
          // Replace label with its address
          assert symbol.getValue() != null; // code addresses must be known
          long address;
          if (argModel.isOffset())
          {
            // Offset must compute the difference between the label and the current instruction
            // and so cannot be linked to the label
            long offset = symbol.getAddress() - instruction.getPc();
            address = offset;
            argument.setConstantValue(RegisterDataContainer.fromValue(offset));
          }
          else
          {
            // If label is not defined, it will be caught later
            // This links the constant and the label through the shared reference
            argument.setConstantValue(symbol.getValue());
            address = symbol.getAddress();
          }
          tokens[i] = String.valueOf(address);
        }
        long evaluated = ExpressionEvaluator.evaluate(tokens);
        // May be a label or a constant
        RegisterDataContainer constantValue = RegisterDataContainer.parseAs(String.valueOf(evaluated), argModel.type());
        if (constantValue != null)
        {
          // Constant
          argument.setConstantValue(constantValue);
        }
        else
        {
          throw new RuntimeException("Unknown constant value");
        }
      }
      
    }
  }
  
  /**
   * @return True if there are any errors in the code. Warning messages are not considered errors.
   */
  public boolean containsErrors()
  {
    for (ParseError error : errorMessages)
    {
      if (error.kind.equals("error"))
      {
        return true;
      }
    }
    return false;
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
    
    if (symbolTable.containsKey(labelName))
    {
      if (symbolTable.get(labelName).getValue() == null)
      {
        // Label was used before definition
        // Assign the address to the label
        symbolTable.get(labelName).setValue(RegisterDataContainer.fromValue(instructions.size() * 4));
      }
      else
      {
        // second definition (wrong)
        addError(lexer.currentToken(), "Label '" + labelName + "' already defined");
      }
    }
    else if (lastSymbolBound)
    {
      symbolTable.put(labelName, new Symbol(labelName, Symbol.SymbolType.LABEL,
                                            RegisterDataContainer.fromValue(instructions.size() * 4), null));
      lastSymbol      = labelName;
      lastSymbolBound = false;
    }
    else
    {
      // Label points to the same symbol as the last one
      symbolTable.put(labelName, symbolTable.get(lastSymbol));
      lastSymbol = labelName;
    }
    
    // Consume label token
    nextToken();
  }

  /**
   * @brief Names of all double instructions
   * Used for detecting double instructions
   */
  private static final Set<String> DOUBLE_INSTRUCTION_NAMES = new HashSet<>(Arrays.asList("fmadd.d", "fmsub.d", "fnmsub.d", "fnmadd.d",
          "fadd.d", "fsub.d", "fmul.d", "fdiv.d", "fsqrt.d", "fmin.d", "fmax.d", "feq.d", "flt.d", "fle.d",
          "fclass.d", "fcvt.l.d", "fcvt.lu.d", "fcvt.d.l", "fcvt.d.lu", "fmv.x.d", "fmv.d.x"));
  
  /**
   * @brief Parse instruction. Current token is a symbol with the name of the instruction.
   * If there is a comment with debug info, it is attached to the instruction.
   * @details Collects arguments of an instruction from the token stream.
   * If the instruction is invalid, the token stream can be only partially consumed.
   */
  private void parseInstruction()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.SYMBOL);
    lastSymbolBound = true;
    
    CodeToken                instructionNameToken = lexer.currentToken();
    String                   instructionName      = instructionNameToken.text();
    InstructionFunctionModel instructionModel     = instructionModels.get(instructionName);
    
    // Check if instruction is valid
    if (instructionModel == null)
    {
      // Special error for double instructions
      if (DOUBLE_INSTRUCTION_NAMES.contains(instructionName))
      {
        addError(lexer.currentToken(), "Double instructions are not supported (caused by '" + instructionName + "')");
        return;
      }
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
      InputCodeArgument inputCodeArgument = new InputCodeArgument(argumentName, argumentToken);
      if (argument.isRegister())
      {
        // Try to find the register. Its existence ic checked in the next step
        RegisterModel register = registers.get(argumentToken.text());
        inputCodeArgument.setRegisterValue(register);
        isValid = checkRegisterArgument(inputCodeArgument, argument.type(), argumentToken);
      }
      else if (argument.isImmediate())
      {
        boolean isLValue = argumentName.equals("rd");
        checkImmediateArgument(inputCodeArgument, isLValue, argumentToken);
      }
      else
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
   * @brief Load next token
   */
  private void nextToken()
  {
    this.lexer.nextToken();
  }
  
  /**
   * @brief Parse directive. Current token is the directive name.
   * It may either be an align directive or a data directive.
   * Data directives bind to the last defined label.
   */
  private void parseDirective()
  {
    assert lexer.currentToken().type().equals(CodeToken.Type.DIRECTIVE);
    
    CodeToken directiveToken = lexer.currentToken();
    String    directive      = directiveToken.text();
    
    if (directive.equals(".align"))
    {
      // Align directive
      nextToken();
      if (!lexer.currentToken().type().equals(CodeToken.Type.SYMBOL))
      {
        addError(lexer.currentToken(), "Expected number after .align");
        return;
      }
      nextAlignment = Integer.parseInt(lexer.currentToken().text());
      nextToken();
      return;
    }
    
    // Data directive
    
    if (lastSymbol == null)
    {
      addError(lexer.currentToken(), "Data directive must be preceded by a label");
      return;
    }
    
    lastSymbolBound = true;
    
    Symbol symbol = symbolTable.get(lastSymbol);
    assert symbol != null;
    MemoryLocation mem = symbol.getMemoryLocation();
    if (mem == null)
    {
      // Create new memory location
      mem           = new MemoryLocation(List.of(symbol.name), nextAlignment, new ArrayList<>(), new ArrayList<>());
      nextAlignment = 0;
      symbol.setMemoryLocation(mem);
    }
    else
    {
      // Align it (ceil to the next multiple of alignment)
      mem.alignment = nextAlignment;
    }
    
    // Collect arguments (until newline, comment, EOF)
    List<CodeToken> args = new ArrayList<>();
    nextToken();
    
    // First argument
    if (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
            .equals(CodeToken.Type.EOF) && !lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
    {
      args.add(lexer.currentToken());
      nextToken();
    }
    
    // rest: [, arg]*
    while (!lexer.currentToken().type().equals(CodeToken.Type.NEWLINE) && !lexer.currentToken().type()
            .equals(CodeToken.Type.EOF) && !lexer.currentToken().type().equals(CodeToken.Type.COMMENT))
    {
      // assert a comma
      if (!lexer.currentToken().type().equals(CodeToken.Type.COMMA))
      {
        addError(lexer.currentToken(), "Expected comma, got " + lexer.currentToken().type());
        return;
      }
      nextToken();
      args.add(lexer.currentToken());
      nextToken();
    }
    int argCount = args.size();
    
    switch (directive)
    {
      case ".byte", ".hword", ".word" ->
      {
        if (argCount == 0)
        {
          addError(directiveToken, directive + " expected at least 1 argument, got " + argCount);
          break;
        }
        if (mem.getName() == null)
        {
          addError(directiveToken, directive + " expected label before it");
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
        for (int i = 0; i < args.size(); i++)
        {
          CodeToken arg = args.get(i);
          // It may be a label
          mem.addValue(arg.text());
        }
      }
      case ".ascii", ".asciiz", ".string" ->
      {
        // todo: escape sequences (https://ftp.gnu.org/old-gnu/Manuals/gas-2.9.1/html_chapter/as_3.html#SEC33)
        mem.addDataChunk(DataTypeEnum.kChar);
        if (argCount == 0)
        {
          addError(new CodeToken(0, 0, directive, CodeToken.Type.EOF),
                   directive + " expected at least 1 argument, got " + argCount);
          break;
        }
        
        for (int j = 0; j < args.size(); j++)
        {
          CodeToken str = args.get(j);
          if (!str.type().equals(CodeToken.Type.STRING))
          {
            addError(str, "Expected string literal, got " + str);
            break;
          }
          for (char c : str.text().toCharArray())
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
        // https://stackoverflow.com/questions/65641034/what-is-zero-in-gnu-gas
        mem.addDataChunk(DataTypeEnum.kByte);
        
        
        // first arg is the size
        if (argCount == 0)
        {
          addError(directiveToken, directive + " expected at least 1 argument, got " + argCount);
          break;
        }
        
        int size = Integer.parseInt(args.get(0).text());
        
        // second arg (if present, after comma) is the fill value
        String fill = "0";
        if (argCount == 2)
        {
          if (args.get(1).type() != CodeToken.Type.SYMBOL)
          {
            addError(args.get(1), "Expected fill value, got " + args.get(2).text());
            break;
          }
          fill = args.get(1).text();
        }
        
        for (int j = 0; j < size; j++)
        {
          mem.addValue(fill);
        }
      }
    }
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
   * @param token   Token where the error occurred
   * @param message Error message
   *
   * @brief Adds a single-line warning message to the list of errors. Warnings do not fail the compilation.
   */
  private void addWarning(CodeToken token, String message)
  {
    this.errorMessages.add(new ParseError("warning", message, token.line(), token.columnStart(), token.columnEnd()));
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
  
  /**
   * @return True if there are errors
   */
  public boolean hasErrors()
  {
    return !errorMessages.isEmpty();
  }
  
  public List<InputCodeModel> getInstructions()
  {
    return instructions;
  }
  
  public Map<String, Symbol> getSymbolTable()
  {
    return symbolTable;
  }
  
  public List<ParseError> getErrorMessages()
  {
    return errorMessages;
  }
}
