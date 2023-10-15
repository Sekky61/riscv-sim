package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

public class CodeArithmeticInterpreterFloatTest
{
  
  @Mock
  private InitLoader initLoader;
  
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, 5.5f, RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, 3.125f, RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, DataTypeEnum.kFloat, 12.25f, RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(DataTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Collections.singletonList(floatFile));
    Mockito.when(initLoader.getInstructionFunctionModels()).thenReturn(setUpInstructions());
    Mockito.when(initLoader.getInstructionFunctionModel(any())).thenCallRealMethod();
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(new UnifiedRegisterFileBlock(initLoader));
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName("fadd")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("\\rs1 \\rs2 + \\rd =").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFSub = new InstructionFunctionModelBuilder().hasName("fsub")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("\\rs1 \\rs2 - \\rd =").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFMul = new InstructionFunctionModelBuilder().hasName("fmul")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("\\rs1 \\rs2 * \\rd =").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFDiv = new InstructionFunctionModelBuilder().hasName("fdiv")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("\\rs1 \\rs2 / \\rd =").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFCmpLt = new InstructionFunctionModelBuilder().hasName("fcmplt")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("\\rs1 \\rs2 < \\rd =").hasArguments("rd,rs1,rs2").build();
    
    
    return Map.ofEntries(Map.entry("fadd", instructionFAdd), Map.entry("fsub", instructionFSub),
                         Map.entry("fmul", instructionFMul), Map.entry("fdiv", instructionFDiv),
                         Map.entry("fcmplt", instructionFCmpLt));
  }
  
  @Test
  public void interpretInstruction_floatAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fadd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(8.625, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatSubInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsub")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(-2.375, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fmul")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(17.1875, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatDivInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fdiv")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(1.76, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatCmpLtInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmplt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(1.0, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatCmpLtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmplt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(0.0, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatCmpLtInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmplt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(0.0, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
}
