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

public class CodeLoadStoreInterpreterTest
{
  @Mock
  private InitLoader initLoader;
  
  private CodeLoadStoreInterpreter codeLoadStoreInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, -1000, RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, DataTypeEnum.kInt, -65535, RegisterReadinessEnum.kAssigned);
    RegisterModel integer6 = new RegisterModel("x6", false, DataTypeEnum.kInt, -4294967295L,
                                               RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kLong)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0.0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, 25.0, RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, 6.0, RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(DataTypeEnum.kDouble)
            .hasRegisterList(Arrays.asList(float1, float2, float3)).build();
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Arrays.asList(integerFile, floatFile));
    Mockito.when(initLoader.getInstructionFunctionModels()).thenReturn(setUpInstructions());
    Mockito.when(initLoader.getInstructionFunctionModel(any())).thenCallRealMethod();
    
    this.codeLoadStoreInterpreter = new CodeLoadStoreInterpreter(initLoader, new MemoryModel(new SimulatedMemory()),
                                                                 new UnifiedRegisterFileBlock(initLoader));
  }
  
  @Test
  public void storeByte_loadByte_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lbu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeShort_loadShort_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lhu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeInt_loadInt_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lwu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeLong_loadLong_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("ld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeFloat_loadFloat_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("flw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeDouble_loadDouble_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeDouble_loadFloat_returnsDifferentValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("flw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertNotEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeFloat_loadDouble_returnsDifferentValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertNotEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
  }
  
  @Test
  public void storeInt_loadSignedByte_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            -1000, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            24, 0.01);
  }
  
  @Test
  public void storeLong_loadSignedInt_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x6").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            -4294967295L, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            1, 0.01);
  }
  
  @Test
  public void storeInt_loadSignedHalf_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            -65535, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            1, 0.01);
  }
  
  @Test
  public void loadWord_dataUnknown_returns0()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    double result1 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    double result2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(0, result1, 0.0001);
    Assert.assertEquals(0, result2, 0.0001);
  }
  
  @Test
  public void loadWord_storeItBefore_returnsExactData()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            25, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertNotEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            25);
  }
  
  @Test
  public void storeTwoBytes_loadOneShort_getByteCombinedValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("1").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lhu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            1542, 0.01);
  }
  
  @Test
  public void storeTwoShort_loadOneInt_getShortCombinedValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("2").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lwu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            393222, 0.01);
  }
  
  @Test
  public void storeOneShort_loadOneInt_getRandomValues()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    Assert.assertEquals(
            this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0).getSecond(),
            6, 0.01);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lwu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    double result1 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    double result2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    
    Assert.assertEquals(6.0, result1, 0.0001);
    Assert.assertEquals(6.0, result2, 0.0001);
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionLoadByte = new InstructionFunctionModelBuilder().hasName("lb")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load byte:signed rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadByteUnsigned = new InstructionFunctionModelBuilder().hasName("lbu")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load byte:unsigned rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadHigh = new InstructionFunctionModelBuilder().hasName("lh")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load half:signed rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadHighUnsigned = new InstructionFunctionModelBuilder().hasName("lhu")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load half:unsigned rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadWord = new InstructionFunctionModelBuilder().hasName("lw")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load word:signed rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadWordUnsigned = new InstructionFunctionModelBuilder().hasName("lwu")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("load word:unsigned rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadDoubleWord = new InstructionFunctionModelBuilder().hasName("ld")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("load doubleword:signed rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadFloat = new InstructionFunctionModelBuilder().hasName("flw")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("load float:unsigned rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionLoadDouble = new InstructionFunctionModelBuilder().hasName("fld")
            .hasInputDataType(DataTypeEnum.kDouble).hasOutputDataType(DataTypeEnum.kDouble)
            .isInterpretedAs("load double:unsigned rd rs1 imm").hasArguments("rd,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreByte = new InstructionFunctionModelBuilder().hasName("sb")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store byte rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreHigh = new InstructionFunctionModelBuilder().hasName("sh")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store half rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreWord = new InstructionFunctionModelBuilder().hasName("sw")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store word rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreDoubleWord = new InstructionFunctionModelBuilder().hasName("sd")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("store doubleword rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreFloat = new InstructionFunctionModelBuilder().hasName("fsw")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("store float rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    InstructionFunctionModel instructionStoreDouble = new InstructionFunctionModelBuilder().hasName("fsd")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("store double rs2 rs1 imm").hasArguments("rs2,rs1,imm").build();
    
    return Map.ofEntries(Map.entry("lb", instructionLoadByte), Map.entry("lbu", instructionLoadByteUnsigned),
                         Map.entry("lh", instructionLoadHigh), Map.entry("lhu", instructionLoadHighUnsigned),
                         Map.entry("lw", instructionLoadWord), Map.entry("lwu", instructionLoadWordUnsigned),
                         Map.entry("ld", instructionLoadDoubleWord), Map.entry("flw", instructionLoadFloat),
                         Map.entry("fld", instructionLoadDouble), Map.entry("sb", instructionStoreByte),
                         Map.entry("sh", instructionStoreHigh), Map.entry("sw", instructionStoreWord),
                         Map.entry("sd", instructionStoreDoubleWord), Map.entry("fsw", instructionStoreFloat),
                         Map.entry("fsd", instructionStoreDouble));
  }
}
