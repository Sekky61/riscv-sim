package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
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

import java.util.*;

import static com.gradle.superscalarsim.models.register.RegisterDataContainer.interpretAs;

public class CodeLoadStoreInterpreterTest
{
  private InitLoader initLoader;
  
  private CodeLoadStoreInterpreter codeLoadStoreInterpreter;
  
  @Before
  public void setUp()
  {
    RegisterModel integer1 = new RegisterModel("x1", false, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, RegisterTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, RegisterTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, RegisterTypeEnum.kInt, -1000,
                                               RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, RegisterTypeEnum.kInt, -65535,
                                               RegisterReadinessEnum.kAssigned);
    RegisterModel integer6 = new RegisterModel("x6", false, RegisterTypeEnum.kInt, -4294967295L,
                                               RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(RegisterTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, RegisterTypeEnum.kFloat, 0.0f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, RegisterTypeEnum.kFloat, 25.0f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, RegisterTypeEnum.kFloat, 6.0f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, RegisterTypeEnum.kFloat, 6.0d,
                                             RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(RegisterTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    initLoader = new InitLoader(Arrays.asList(integerFile, floatFile), null);
    initLoader.setInstructionFunctionModels(setUpInstructions());
    
    InstructionMemoryBlock instructionMemoryBlock = new InstructionMemoryBlock(new ArrayList<>(), new HashMap<>(),
                                                                               null);
    this.codeLoadStoreInterpreter = new CodeLoadStoreInterpreter(new MemoryModel(new SimulatedMemory()),
                                                                 new UnifiedRegisterFileBlock(initLoader, 320,
                                                                                              new RegisterModelFactory()),
                                                                 instructionMemoryBlock);
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionLoadByte = new InstructionFunctionModelBuilder().hasName("lb")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load:8:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadByteUnsigned = new InstructionFunctionModelBuilder().hasName("lbu")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load:8:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kUInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadHigh = new InstructionFunctionModelBuilder().hasName("lh")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load:16:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadHighUnsigned = new InstructionFunctionModelBuilder().hasName("lhu")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load:16:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kUInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadWord = new InstructionFunctionModelBuilder().hasName("lw")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("load:32:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadWordUnsigned = new InstructionFunctionModelBuilder().hasName("lwu")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("load:32:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kUInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadDoubleWord = new InstructionFunctionModelBuilder().hasName("ld")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("load:64:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kLong, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadFloat = new InstructionFunctionModelBuilder().hasName("flw")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("load:32:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kFloat, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionLoadDouble = new InstructionFunctionModelBuilder().hasName("fld")
            .hasInputDataType(DataTypeEnum.kDouble).hasOutputDataType(DataTypeEnum.kDouble)
            .isInterpretedAs("load:64:\\rs1 \\imm +").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rd", DataTypeEnum.kDouble, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreByte = new InstructionFunctionModelBuilder().hasName("sb")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store:8:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreHigh = new InstructionFunctionModelBuilder().hasName("sh")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store:16:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreWord = new InstructionFunctionModelBuilder().hasName("sw")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("store:32:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreDoubleWord = new InstructionFunctionModelBuilder().hasName("sd")
            .hasInputDataType(DataTypeEnum.kLong).hasOutputDataType(DataTypeEnum.kLong)
            .isInterpretedAs("store:64:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreFloat = new InstructionFunctionModelBuilder().hasName("fsw")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("store:32:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kFloat, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    InstructionFunctionModel instructionStoreDouble = new InstructionFunctionModelBuilder().hasName("fsd")
            .hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(DataTypeEnum.kFloat)
            .isInterpretedAs("store:64:\\rs1 \\imm +:rs2").hasArguments(
                    List.of(new InstructionFunctionModel.Argument("rs2", DataTypeEnum.kDouble, null),
                            new InstructionFunctionModel.Argument("rs1", DataTypeEnum.kInt, null),
                            new InstructionFunctionModel.Argument("imm", DataTypeEnum.kInt, null))).build();
    
    return Map.ofEntries(Map.entry("lb", instructionLoadByte), Map.entry("lbu", instructionLoadByteUnsigned),
                         Map.entry("lh", instructionLoadHigh), Map.entry("lhu", instructionLoadHighUnsigned),
                         Map.entry("lw", instructionLoadWord), Map.entry("lwu", instructionLoadWordUnsigned),
                         Map.entry("ld", instructionLoadDoubleWord), Map.entry("flw", instructionLoadFloat),
                         Map.entry("fld", instructionLoadDouble), Map.entry("sb", instructionStoreByte),
                         Map.entry("sh", instructionStoreHigh), Map.entry("sw", instructionStoreWord),
                         Map.entry("sd", instructionStoreDoubleWord), Map.entry("fsw", instructionStoreFloat),
                         Map.entry("fsd", instructionStoreDouble));
  }
  
  @Test
  public void storeByte_loadByte_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lbu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
  }
  
  @Test
  public void storeShort_loadShort_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lhu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
  }
  
  @Test
  public void storeInt_loadInt_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lwu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
  }
  
  @Test
  public void storeLong_loadLong_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("ld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
  }
  
  @Test
  public void storeFloat_loadFloat_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    float r = (float) interpretAs(value, DataTypeEnum.kFloat);
    Assert.assertEquals(6.0f, r, 0.001);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("flw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    float r2 = (float) interpretAs(value, DataTypeEnum.kFloat);
    Assert.assertEquals(6, r2, 0.001);
  }
  
  @Test
  public void storeDouble_loadDouble_returnsExactValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    double r = (double) interpretAs(value, DataTypeEnum.kDouble);
    Assert.assertEquals(6.0d, r, 0.001);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    double r2 = (double) interpretAs(value2, DataTypeEnum.kDouble);
    Assert.assertEquals(6.0d, r2, 0.001);
  }
  
  @Test
  public void storeDouble_loadFloat_returnsDifferentValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    double r = (double) interpretAs(value, DataTypeEnum.kDouble);
    Assert.assertEquals(6.0d, r, 0.001);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("flw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertNotEquals(6, value2);
  }
  
  @Test
  public void storeFloat_loadDouble_returnsDifferentValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    // Cast to float
    float r = (float) interpretAs(value, DataTypeEnum.kFloat);
    Assert.assertEquals(6.0f, r, 0.001);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fld")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertNotEquals(6, value2);
  }
  
  @Test
  public void storeInt_loadSignedByte_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    // The bits have to be interpreted as int
    int signedValue = (int) interpretAs(value, DataTypeEnum.kInt);
    Assert.assertEquals(-1000, signedValue);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(24, value2);
  }
  
  @Test
  public void storeLong_loadSignedInt_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x6").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sd")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(-4294967295L, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(1, value2);
  }
  
  @Test
  public void storeInt_loadSignedHalf_returnsRightSign()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    int signedValue = (int) interpretAs(value, DataTypeEnum.kInt);
    Assert.assertEquals(-65535, signedValue);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(1, value2);
  }
  
  @Test
  public void loadWord_dataUnknown_returns0()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    long result1 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    long result2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(0, result1);
    Assert.assertEquals(0, result2);
  }
  
  @Test
  public void loadWord_storeItBefore_returnsExactData()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(25, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    int valueInt = (int) interpretAs(value2, DataTypeEnum.kInt);
    Assert.assertEquals(25, valueInt);
  }
  
  @Test
  public void storeTwoBytes_loadOneShort_getByteCombinedValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("1").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sb")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lhu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value3 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(1542, value3);
  }
  
  @Test
  public void storeTwoShort_loadOneInt_getShortCombinedValue()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("2").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value2 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value2);
    
    argument1      = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    argument2      = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    argument3      = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lwu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value3 = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(393222, value3);
  }
  
  @Test
  public void storeOneShort_loadOneInt_getRandomValues()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sh")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    long value = this.codeLoadStoreInterpreter.interpretInstruction(new SimCodeModel(inputCodeModel, -1, -1), 0)
            .getSecond();
    Assert.assertEquals(6, value);
    
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
}
