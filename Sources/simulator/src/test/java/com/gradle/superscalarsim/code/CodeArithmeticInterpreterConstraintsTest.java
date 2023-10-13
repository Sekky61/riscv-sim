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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

public class CodeArithmeticInterpreterConstraintsTest
{
  @Mock
  private InitLoader initLoader;
  
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, Integer.MAX_VALUE,
                                               RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, Integer.MIN_VALUE,
                                               RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, 4, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4)).build();
    
    RegisterModel long1 = new RegisterModel("l1", false, DataTypeEnum.kLong, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel long2 = new RegisterModel("l2", false, DataTypeEnum.kLong, Long.MAX_VALUE,
                                            RegisterReadinessEnum.kAssigned);
    RegisterModel long3 = new RegisterModel("l3", false, DataTypeEnum.kLong, Long.MIN_VALUE,
                                            RegisterReadinessEnum.kAssigned);
    RegisterFileModel longFile = new RegisterFileModelBuilder().hasName("long").hasDataType(DataTypeEnum.kLong)
            .hasRegisterList(Arrays.asList(long1, long2, long3)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, Float.MAX_VALUE,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, Float.MIN_VALUE,
                                             RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(DataTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3)).build();
    
    RegisterModel double1 = new RegisterModel("d1", false, DataTypeEnum.kDouble, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel double2 = new RegisterModel("d2", false, DataTypeEnum.kDouble, Double.MAX_VALUE,
                                              RegisterReadinessEnum.kAssigned);
    RegisterModel double3 = new RegisterModel("d3", false, DataTypeEnum.kDouble, Double.MIN_VALUE,
                                              RegisterReadinessEnum.kAssigned);
    RegisterFileModel doubleFile = new RegisterFileModelBuilder().hasName("double").hasDataType(DataTypeEnum.kDouble)
            .hasRegisterList(Arrays.asList(double1, double2, double3)).build();
    
    Mockito.when(initLoader.getRegisterFileModelList())
            .thenReturn(Arrays.asList(integerFile, longFile, floatFile, doubleFile));
    Mockito.when(initLoader.getInstructionFunctionModels()).thenReturn(setUpInstructions());
    Mockito.when(initLoader.getInstructionFunctionModel(any())).thenCallRealMethod();
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(new UnifiedRegisterFileBlock(initLoader));
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionIntInc = new InstructionFunctionModelBuilder().hasName("intInc")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=++rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionIntDec = new InstructionFunctionModelBuilder().hasName("intDec")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=--rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionLongInc = new InstructionFunctionModelBuilder().hasName("longInc")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong).isInterpretedAs("rd=++rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionLongDec = new InstructionFunctionModelBuilder().hasName("longDec")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong).isInterpretedAs("rd=--rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionFloatInc = new InstructionFunctionModelBuilder().hasName("floatInc")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=++rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionFloatDec = new InstructionFunctionModelBuilder().hasName("floatDec")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=--rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionDoubleInc = new InstructionFunctionModelBuilder().hasName("doubleInc")
            .hasInputDataType(DataTypeEnum.kDouble).hasOutputDataType(DataTypeEnum.kDouble).isInterpretedAs("rd=++rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionDoubleDec = new InstructionFunctionModelBuilder().hasName("doubleDec")
            .hasInputDataType(DataTypeEnum.kDouble).hasOutputDataType(DataTypeEnum.kDouble).isInterpretedAs("rd=--rs1")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionIntDiv = new InstructionFunctionModelBuilder().hasName("div")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1/rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    return Map.ofEntries(Map.entry("intInc", instructionIntInc), Map.entry("intDec", instructionIntDec),
                         Map.entry("longInc", instructionLongInc), Map.entry("longDec", instructionLongDec),
                         Map.entry("floatInc", instructionFloatInc), Map.entry("floatDec", instructionFloatDec),
                         Map.entry("doubleInc", instructionDoubleInc), Map.entry("doubleDec", instructionDoubleDec),
                         Map.entry("div", instructionIntDiv));
  }
  
  @Test
  public void interpretInstruction_intIncrementAtIntMax_overflowsToIntMin()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("intInc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Integer.MIN_VALUE, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_intDecrementAtIntMin_overflowsToIntMax()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("intDec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Integer.MAX_VALUE, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_longIncrementAtLongMax_overflowsToLongMin()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("l1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("l2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("longInc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Long.MIN_VALUE, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_longDecrementAtLongMin_overflowsToLongMax()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("l1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("l3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("longDec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Long.MAX_VALUE, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatIncrementAtFloatMax_overflowsToNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatInc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Float.NaN, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatDecrementAtFloatMin_getsMinusOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatDec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(-1, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_doubleIncrementAtDoubleMax_overflowsToNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatInc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Double.NaN, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_doubleDecrementAtDoubleMin_getsMinusOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("floatDec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(-1, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_divideByZero_returnsNan()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("div")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(Double.NaN, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
}
