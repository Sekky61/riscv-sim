package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.DecodeAndDispatchBlock;
import com.gradle.superscalarsim.blocks.base.InstructionFetchBlock;
import com.gradle.superscalarsim.blocks.base.RenameMapTableBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
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
import java.util.List;

public class DecodeAndDispatchBlockTest
{
  @Mock
  private InstructionFetchBlock instructionFetchBlock;
  @Mock
  private InitLoader loader;
  @Mock
  private BranchTargetBuffer branchTargetBuffer;
  @Mock
  private GlobalHistoryRegister globalHistoryRegister;
  @Mock
  private CodeParser codeParser;
  @Mock
  private InitLoader initLoader;
  
  private RenameMapTableBlock renameMapTableBlock;
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, 11, RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, DataTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterModel integer6 = new RegisterModel("x6", false, DataTypeEnum.kInt, -20, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, 5.5, RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, 3.125, RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, DataTypeEnum.kFloat, 12.25, RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(DataTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    List<RegisterFileModel> registerFileModels = Arrays.asList(integerFile, floatFile);
    Mockito.when(loader.getRegisterFileModelList()).thenReturn(registerFileModels);
    
    renameMapTableBlock = new RenameMapTableBlock(new UnifiedRegisterFileBlock(loader));
    SimCodeModelAllocator simCodeModelAllocator = new SimCodeModelAllocator();
    decodeAndDispatchBlock = new DecodeAndDispatchBlock(simCodeModelAllocator, instructionFetchBlock,
                                                        renameMapTableBlock, globalHistoryRegister, branchTargetBuffer,
                                                        codeParser);
  }
  
  @Test
  public void decodeAndDispatchSimulate_wawDependency_renamesDestinationRegisters()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("add").hasCodeLine("add x1 x2 x3")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("sub").hasCodeLine("sub x1 x2 x3")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("mul").hasCodeLine("mul x1 x2 x3")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0 x2 x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1 x2 x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2 x2 x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
  
  @Test
  public void decodeAndDispatchSimulate_warDependency_renamesDestinationRegisters()
  {
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("add").hasCodeLine("add x1 x2 x3")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("sub").hasCodeLine("sub x2 x3 x4")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("mul").hasCodeLine("mul x3 x4 x5")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0 x2 x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1 x3 x4", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2 x4 x5", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
  
  @Test
  public void decodeAndDispatchSimulate_rawDependency_renamesAllRegisters()
  {
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("add").hasCodeLine("add x3 x4 x5")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("sub").hasCodeLine("sub x2 x3 x4")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("mul").hasCodeLine("mul x1 x2 x3")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0 x4 x5", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1 tg0 x4", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2 tg1 tg0", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
}
