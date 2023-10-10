package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodeArithmeticInterpreterIntTest
{
  @Mock
  private InitLoader initLoader;
  
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4)).build();
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Collections.singletonList(integerFile));
    Mockito.when(initLoader.getInstructionFunctionModelList()).thenReturn(setUpInstructions());
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(new UnifiedRegisterFileBlock(initLoader));
  }
  
  @Test
  public void interpretInstruction_intAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("add")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(31, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.01);
  }
  
  @Test
  public void interpretInstruction_intSubInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sub")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(-19, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("mul")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(150, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intDivInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("div")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(4, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intShiftLeftInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("shiftLeft")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(100, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intShiftRightInstructionWithPositiveNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("shiftRight")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(6, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intShiftRightInstructionWithNegativeNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("1").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("shiftRight")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(-1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intLogicalShiftRightInstructionWithNegativeNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("1").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("shiftRightLog").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(Integer.MAX_VALUE, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intMulAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("mulAdd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(775, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intAddMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("addMul")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(175, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intAndInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("and")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(24, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intOrInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("or")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(31, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intNotInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("not")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intIncInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("inc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    Assert.assertEquals(26, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intDecInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("dec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    Assert.assertEquals(24, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpLe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpEq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpEq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpEq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGe")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(0, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("cmpGt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }
  
  private List<InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName("add")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1+rs2;")
            .hasSyntax("add rd rs1 rs2").build();
    
    InstructionFunctionModel instructionSub = new InstructionFunctionModelBuilder().hasName("sub")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1-rs2;")
            .hasSyntax("sub rd rs1 rs2").build();
    
    InstructionFunctionModel instructionMul = new InstructionFunctionModelBuilder().hasName("mul")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1*rs2;")
            .hasSyntax("mul rd rs1 rs2").build();
    
    InstructionFunctionModel instructionIntDiv = new InstructionFunctionModelBuilder().hasName("div")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1/rs2;")
            .hasSyntax("div rd rs1 rs2").build();
    
    InstructionFunctionModel instructionShiftLeft = new InstructionFunctionModelBuilder().hasName("shiftLeft")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1" + "<<rs2;").hasSyntax("shiftLeft rd " + "rs1" + " rs2").build();
    
    InstructionFunctionModel instructionShiftRight = new InstructionFunctionModelBuilder().hasName("shiftRight")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1" + ">>rs2;").hasSyntax("shiftRight rd " + "rs1" + " rs2").build();
    
    InstructionFunctionModel instructionLogShiftRight = new InstructionFunctionModelBuilder().hasName("shiftRightLog")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1" + ">>>rs2;").hasSyntax("shiftRight " + "rd " + "rs1 rs2").build();
    
    InstructionFunctionModel instructionMulAdd = new InstructionFunctionModelBuilder().hasName("mulAdd")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1*" + "(rs1+rs2)").hasSyntax("mulAdd rd rs1 rs2").build();
    
    InstructionFunctionModel instructionAddMul = new InstructionFunctionModelBuilder().hasName("addMul")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1+" + "(rs1*rs2)").hasSyntax("addMul rd rs1 rs2").build();
    
    InstructionFunctionModel instructionAnd = new InstructionFunctionModelBuilder().hasName("and")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1&rs2")
            .hasSyntax("and rd rs1 rs2").build();
    
    InstructionFunctionModel instructionOr = new InstructionFunctionModelBuilder().hasName("or")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1|rs2")
            .hasSyntax("and rd rs1 rs2").build();
    
    InstructionFunctionModel instructionNot = new InstructionFunctionModelBuilder().hasName("not")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=!rs1")
            .hasSyntax("not rd rs1").build();
    
    InstructionFunctionModel instructionInc = new InstructionFunctionModelBuilder().hasName("inc")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=++rs1")
            .hasSyntax("inc rd rs1").build();
    
    InstructionFunctionModel instructionDec = new InstructionFunctionModelBuilder().hasName("dec")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=--rs1")
            .hasSyntax("dec rd rs1").build();
    
    InstructionFunctionModel instructionCmpLt = new InstructionFunctionModelBuilder().hasName("cmpLt")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1<rs2")
            .hasSyntax("cmpLt rd rs1 rs2").build();
    
    InstructionFunctionModel instructionCmpLe = new InstructionFunctionModelBuilder().hasName("cmpLe")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1<=rs2")
            .hasSyntax("cmpLe rd rs1 rs2").build();
    
    InstructionFunctionModel instructionCmpEq = new InstructionFunctionModelBuilder().hasName("cmpEq")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1==rs2")
            .hasSyntax("cmpEq rd rs1 rs2").build();
    
    InstructionFunctionModel instructionCmpGt = new InstructionFunctionModelBuilder().hasName("cmpGt")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1>rs2")
            .hasSyntax("cmpGt rd rs1 rs2").build();
    
    InstructionFunctionModel instructionCmpGe = new InstructionFunctionModelBuilder().hasName("cmpGe")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1>=rs2")
            .hasSyntax("cmpGe rd rs1 rs2").build();
    
    return Arrays.asList(instructionAdd, instructionSub, instructionMul, instructionIntDiv, instructionShiftLeft,
                         instructionShiftRight, instructionLogShiftRight, instructionMulAdd, instructionAddMul,
                         instructionAnd, instructionOr, instructionNot, instructionInc, instructionDec,
                         instructionCmpLt, instructionCmpLe, instructionCmpEq, instructionCmpGe, instructionCmpGt);
  }
}
