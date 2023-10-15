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
  
  @Test
  public void interpretInstruction_floatAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fadd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(8.625, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(-2.375, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(17.1875, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(1.76, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatIncInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("finc")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(4.125, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatDecInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fdec")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(2.125, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
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
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpLeInstructionWithLessThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmple")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpLeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmple")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpLeInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmple")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpEqInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpeq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpEqInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpeq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpEqInstructionWithGreaterThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpeq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGeInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGeInstructionWithEqualArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmple")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGeInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGtInstructionWithLessThanArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpgt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGtInstructionWithEqualArguments_returnZero()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpgt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(0.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  @Test
  public void interpretInstruction_floatCmpGtInstructionWithGreaterThanArguments_returnOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fcmpgt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Assert.assertEquals(1.0, this.codeArithmeticInterpreter.interpretInstruction(codeModel), 0.0001);
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName("fadd")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1+rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFSub = new InstructionFunctionModelBuilder().hasName("fsub")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1-rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFMul = new InstructionFunctionModelBuilder().hasName("fmul")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1*rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFDiv = new InstructionFunctionModelBuilder().hasName("fdiv")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1/rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFInc = new InstructionFunctionModelBuilder().hasName("finc")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=++rs1;")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionFDec = new InstructionFunctionModelBuilder().hasName("fdec")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=--rs1;")
            .hasArguments("rd,rs1").build();
    
    InstructionFunctionModel instructionFCmpLt = new InstructionFunctionModelBuilder().hasName("fcmplt")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1<rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFCmpLe = new InstructionFunctionModelBuilder().hasName("fcmple")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("rd=rs1<=rs2;").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFCmpEq = new InstructionFunctionModelBuilder().hasName("fcmpeq")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("rd=rs1==rs2;").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFCmpGe = new InstructionFunctionModelBuilder().hasName("fcmpge")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("rd=rs1>=rs2;").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionFCmpGt = new InstructionFunctionModelBuilder().hasName("fcmpgt")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1>rs2;")
            .hasArguments("rd,rs1,rs2").build();
    
    return Map.ofEntries(Map.entry("fadd", instructionFAdd), Map.entry("fsub", instructionFSub),
                         Map.entry("fmul", instructionFMul), Map.entry("fdiv", instructionFDiv),
                         Map.entry("finc", instructionFInc), Map.entry("fdec", instructionFDec),
                         Map.entry("fcmplt", instructionFCmpLt), Map.entry("fcmple", instructionFCmpLe),
                         Map.entry("fcmpeq", instructionFCmpEq), Map.entry("fcmpge", instructionFCmpGe),
                         Map.entry("fcmpgt", instructionFCmpGt));
  }
}
