package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.DynamicDataProvider;
import com.gradle.superscalarsim.loader.IDataProvider;
import com.gradle.superscalarsim.models.instruction.*;
import com.gradle.superscalarsim.models.register.RegisterFile;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.models.util.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CodeArithmeticInterpreterIntTest
{
  UnifiedRegisterFileBlock urf;
  private IDataProvider staticDataProvider;
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  @Before
  public void setUp()
  {
    RegisterModel integer1 = new RegisterModel("x1", false, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, RegisterTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, RegisterTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, RegisterTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(RegisterTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4)).build();
    
    
    RegisterFile registerFile = new RegisterFile(Collections.singletonList(integerFile), List.of());
    staticDataProvider = new DynamicDataProvider(registerFile, setUpInstructions());
    urf                = new UnifiedRegisterFileBlock(staticDataProvider.getRegisterFile().getRegisterMap(true), 320,
                                                      new RegisterModelFactory());
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter();
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName("add")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 + \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionSub = new InstructionFunctionModelBuilder().hasName("sub")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 - \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionMul = new InstructionFunctionModelBuilder().hasName("mul")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 * \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionIntDiv = new InstructionFunctionModelBuilder().hasName("div")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 / \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionShiftLeft = new InstructionFunctionModelBuilder().hasName("shiftLeft")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 << \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionShiftRight = new InstructionFunctionModelBuilder().hasName("shiftRight")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 >> \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLogShiftRight = new InstructionFunctionModelBuilder().hasName("shiftRightLog")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 >>> \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionMulAdd = new InstructionFunctionModelBuilder().hasName("mulAdd")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 + \\rs1 * \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionAddMul = new InstructionFunctionModelBuilder().hasName("addMul")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 * \\rs1 + \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionAnd = new InstructionFunctionModelBuilder().hasName("and")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 & \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionOr = new InstructionFunctionModelBuilder().hasName("or")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 | \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionCmpLt = new InstructionFunctionModelBuilder().hasName("cmpLt")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 < \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionCmpLe = new InstructionFunctionModelBuilder().hasName("cmpLe")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 <= \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionCmpEq = new InstructionFunctionModelBuilder().hasName("cmpEq")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 == \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionCmpGt = new InstructionFunctionModelBuilder().hasName("cmpGt")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 > \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionCmpGe = new InstructionFunctionModelBuilder().hasName("cmpGe")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("\\rs1 \\rs2 >= \\rd =").hasArguments(
                    List.of(new InstructionArgument("rd", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs1", DataTypeEnum.kInt, null),
                            new InstructionArgument("rs2", DataTypeEnum.kInt, null))).build();
    
    return Map.ofEntries(Map.entry("add", instructionAdd), Map.entry("sub", instructionSub),
                         Map.entry("mul", instructionMul), Map.entry("div", instructionIntDiv),
                         Map.entry("shiftLeft", instructionShiftLeft), Map.entry("shiftRight", instructionShiftRight),
                         Map.entry("shiftRightLog", instructionLogShiftRight), Map.entry("mulAdd", instructionMulAdd),
                         Map.entry("addMul", instructionAddMul), Map.entry("and", instructionAnd),
                         Map.entry("or", instructionOr), Map.entry("cmpLt", instructionCmpLt),
                         Map.entry("cmpLe", instructionCmpLe), Map.entry("cmpEq", instructionCmpEq),
                         Map.entry("cmpGt", instructionCmpGt), Map.entry("cmpGe", instructionCmpGe));
  }
  
  @Test
  public void interpretInstruction_intAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("add")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(31, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intSubInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("sub")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(-19, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("mul")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(150, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intDivInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("div")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(4, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intShiftLeftInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasConstant("2", DataTypeEnum.kInt)
            .build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("shiftLeft").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    // TODO: Fails because rs2 is set to immediate value
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(100, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intShiftRightInstructionWithPositiveNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasConstant("2", DataTypeEnum.kInt)
            .build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("shiftRight").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(6, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intShiftRightInstructionWithNegativeNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasConstant("1", DataTypeEnum.kInt)
            .build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("shiftRight").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(-1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intLogicalShiftRightInstructionWithNegativeNumber_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasConstant("1", DataTypeEnum.kInt)
            .build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("shiftRightLog").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(Integer.MAX_VALUE, (int) v.value.getValue(DataTypeEnum.kInt), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intMulAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("mulAdd").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(775, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intAddMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("addMul").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(175, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intAndInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("and")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(24, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intOrInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("or")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(31, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLtInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpLeInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpLe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpEq").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpEq").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpEqInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpEq").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGeInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGe").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(0, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_intCmpGtInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider)
            .hasInstructionName("cmpGt").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel).value();
    Assert.assertEquals(1, (int) v.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void interpretInstruction_divZeroRaisesException()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(staticDataProvider).hasInstructionName("div")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Result<Expression.Variable> v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertTrue(v.isException());
  }
}
