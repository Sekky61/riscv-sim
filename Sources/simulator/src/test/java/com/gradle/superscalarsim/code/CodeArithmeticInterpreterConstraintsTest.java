package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.builders.*;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class CodeArithmeticInterpreterConstraintsTest {
  @Mock
  private InitLoader initLoader;

  private CodeArithmeticInterpreter codeArithmeticInterpreter;

  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModelBuilder().hasName("x1").HasValue(0).IsConstant(false).build();
    RegisterModel integer2 = new RegisterModelBuilder().hasName("x2").HasValue(Integer.MAX_VALUE).IsConstant(false).build();
    RegisterModel integer3 = new RegisterModelBuilder().hasName("x3").HasValue(Integer.MIN_VALUE).IsConstant(false).build();
    RegisterModel integer4 = new RegisterModelBuilder().hasName("x4").HasValue(4).IsConstant(false).build();
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer")
        .hasDataType(DataTypeEnum.kInt)
        .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4))
        .build();

    RegisterModel long1 = new RegisterModelBuilder().hasName("l1").HasValue(0).IsConstant(false).build();
    RegisterModel long2 = new RegisterModelBuilder().hasName("l2").HasValue(Long.MAX_VALUE).IsConstant(false).build();
    RegisterModel long3 = new RegisterModelBuilder().hasName("l3").HasValue(Long.MIN_VALUE).IsConstant(false).build();
    RegisterFileModel longFile = new RegisterFileModelBuilder().hasName("long")
        .hasDataType(DataTypeEnum.kLong)
        .hasRegisterList(Arrays.asList(long1, long2, long3))
        .build();

    RegisterModel float1 = new RegisterModelBuilder().hasName("f1").HasValue(0).IsConstant(false).build();
    RegisterModel float2 = new RegisterModelBuilder().hasName("f2").HasValue(Float.MAX_VALUE).IsConstant(false).build();
    RegisterModel float3 = new RegisterModelBuilder().hasName("f3").HasValue(Float.MIN_VALUE).IsConstant(false).build();
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float")
        .hasDataType(DataTypeEnum.kFloat)
        .hasRegisterList(Arrays.asList(float1, float2, float3))
        .build();

    RegisterModel double1 = new RegisterModelBuilder().hasName("d1").HasValue(0).IsConstant(false).build();
    RegisterModel double2 = new RegisterModelBuilder().hasName("d2").HasValue(Double.MAX_VALUE).IsConstant(false).build();
    RegisterModel double3 = new RegisterModelBuilder().hasName("d3").HasValue(Double.MIN_VALUE).IsConstant(false).build();
    RegisterFileModel doubleFile = new RegisterFileModelBuilder().hasName("double")
        .hasDataType(DataTypeEnum.kDouble)
        .hasRegisterList(Arrays.asList(double1, double2, double3))
        .build();

    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Arrays.asList(integerFile, longFile, floatFile, doubleFile));
    Mockito.when(initLoader.getInstructionFunctionModelList()).thenReturn(setUpInstructions());


    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(initLoader,new PrecedingTable(), new UnifiedRegisterFileBlock(initLoader));
  }

  @Test
  public void interpretInstruction_intIncrementAtIntMax_overflowsToIntMin()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("intInc")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();

    Assert.assertEquals(Integer.MIN_VALUE, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_intDecrementAtIntMin_overflowsToIntMax()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("intDec")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();

    Assert.assertEquals(Integer.MAX_VALUE, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_longIncrementAtLongMax_overflowsToLongMin()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("l1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("l2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("longInc")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();
    Assert.assertEquals(Long.MIN_VALUE, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_longDecrementAtLongMin_overflowsToLongMax()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("l1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("l3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("longDec")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();

    Assert.assertEquals(Long.MAX_VALUE, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_floatIncrementAtFloatMax_overflowsToNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatInc")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();
    Assert.assertEquals(Float.NaN, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_floatDecrementAtFloatMin_getsMinusOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatDec")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();

    Assert.assertEquals(-1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_doubleIncrementAtDoubleMax_overflowsToNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatInc")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();
    Assert.assertEquals(Double.NaN, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_doubleDecrementAtDoubleMin_getsMinusOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatDec")
        .hasArguments(Arrays.asList(argument1, argument2))
        .build();

    Assert.assertEquals(-1, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  @Test
  public void interpretInstruction_divideByZero_returnsNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("div")
        .hasArguments(Arrays.asList(argument1, argument2, argument3))
        .build();

    Assert.assertEquals(Double.NaN, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.0001);
  }

  private List<InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionIntInc = new InstructionFunctionModelBuilder().hasName("intInc")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=++rs1")
        .hasSyntax("intInc rd rs1")
        .build();

    InstructionFunctionModel instructionIntDec = new InstructionFunctionModelBuilder().hasName("intDec")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=--rs1")
        .hasSyntax("intDec rd rs1")
        .build();

    InstructionFunctionModel instructionLongInc = new InstructionFunctionModelBuilder().hasName("longInc")
        .hasInputDataType(DataTypeEnum.kLong)
        .hasOutputDataType(DataTypeEnum.kLong)
        .isInterpretedAs("rd=++rs1")
        .hasSyntax("longInc rd rs1")
        .build();

    InstructionFunctionModel instructionLongDec = new InstructionFunctionModelBuilder().hasName("longDec")
        .hasInputDataType(DataTypeEnum.kLong)
        .hasOutputDataType(DataTypeEnum.kLong)
        .isInterpretedAs("rd=--rs1")
        .hasSyntax("longDec rd rs1")
        .build();

    InstructionFunctionModel instructionFloatInc = new InstructionFunctionModelBuilder().hasName("floatInc")
        .hasInputDataType(DataTypeEnum.kFloat)
        .hasOutputDataType(DataTypeEnum.kFloat)
        .isInterpretedAs("rd=++rs1")
        .hasSyntax("floatInc rd rs1")
        .build();

    InstructionFunctionModel instructionFloatDec = new InstructionFunctionModelBuilder().hasName("floatDec")
        .hasInputDataType(DataTypeEnum.kFloat)
        .hasOutputDataType(DataTypeEnum.kFloat)
        .isInterpretedAs("rd=--rs1")
        .hasSyntax("floatDec rd rs1")
        .build();

    InstructionFunctionModel instructionDoubleInc = new InstructionFunctionModelBuilder().hasName("doubleInc")
        .hasInputDataType(DataTypeEnum.kDouble)
        .hasOutputDataType(DataTypeEnum.kDouble)
        .isInterpretedAs("rd=++rs1")
        .hasSyntax("doubleInc rd rs1")
        .build();

    InstructionFunctionModel instructionDoubleDec = new InstructionFunctionModelBuilder().hasName("doubleDec")
        .hasInputDataType(DataTypeEnum.kDouble)
        .hasOutputDataType(DataTypeEnum.kDouble)
        .isInterpretedAs("rd=--rs1")
        .hasSyntax("doubleDec rd rs1")
        .build();

    InstructionFunctionModel instructionIntDiv = new InstructionFunctionModelBuilder().hasName("div")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=rs1/rs2;")
        .hasSyntax("div rd rs1 rs2")
        .build();

    return Arrays.asList(
        instructionIntInc,
        instructionIntDec,
        instructionLongInc,
        instructionLongDec,
        instructionFloatInc,
        instructionFloatDec,
        instructionDoubleInc,
        instructionDoubleDec,
        instructionIntDiv);
  }
}
