package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
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
  private InstructionMemoryBlock instructionMemoryBlock;
  private InitLoader initLoader;
  
  private RenameMapTableBlock renameMapTableBlock;
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  private UnifiedRegisterFileBlock urf;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.initMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, RegisterTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, RegisterTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, RegisterTypeEnum.kInt, 11, RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, RegisterTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterModel integer6 = new RegisterModel("x6", false, RegisterTypeEnum.kInt, -20,
                                               RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(RegisterTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, RegisterTypeEnum.kFloat, 0f, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, RegisterTypeEnum.kFloat, 5.5f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, RegisterTypeEnum.kFloat, 3.125f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, RegisterTypeEnum.kFloat, 12.25f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(RegisterTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    loader = new InitLoader(Arrays.asList(integerFile, floatFile), null);
    
    loader              = new InitLoader(Arrays.asList(integerFile, floatFile), null);
    urf                 = new UnifiedRegisterFileBlock(loader, 320, new RegisterModelFactory());
    renameMapTableBlock = new RenameMapTableBlock(urf);
    int maximumInstructions = 20;
    decodeAndDispatchBlock = new DecodeAndDispatchBlock(instructionFetchBlock, renameMapTableBlock,
                                                        globalHistoryRegister, branchTargetBuffer,
                                                        instructionMemoryBlock, instructionFetchBlock.getNumberOfWays(),
                                                        new SimulationStatistics(maximumInstructions));
  }
  
  @Test
  public void decodeAndDispatchSimulate_wawDependency_renamesDestinationRegisters()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("add")
            .hasCodeLine("add x1,x2,x3").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("sub")
            .hasCodeLine("sub x1,x2,x3").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("mul")
            .hasCodeLine("mul x1,x2,x3").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0,x2,x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1,x2,x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2,x2,x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
  
  @Test
  public void decodeAndDispatchSimulate_warDependency_renamesDestinationRegisters()
  {
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x3").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x5").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("add")
            .hasCodeLine("add x1,x2,x3").hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("sub")
            .hasCodeLine("sub x2,x3,x4").hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("mul")
            .hasCodeLine("mul x3,x4,x5").hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0,x2,x3", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1,x3,x4", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2,x4,x5", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
  
  @Test
  public void decodeAndDispatchSimulate_rawDependency_renamesAllRegisters()
  {
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x3").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x5").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("add")
            .hasCodeLine("add x3,x4,x5").hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    SimCodeModel sim1 = new SimCodeModel(ins1, 0);
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("sub")
            .hasCodeLine("sub x2,x3,x4").hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    SimCodeModel sim2 = new SimCodeModel(ins2, 0);
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(loader).hasInstructionName("mul")
            .hasCodeLine("mul x1,x2,x3").hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    SimCodeModel       sim3         = new SimCodeModel(ins3, 0);
    List<SimCodeModel> instructions = Arrays.asList(sim1, sim2, sim3);
    Mockito.when(instructionFetchBlock.getFetchedCode()).thenReturn(instructions);
    
    decodeAndDispatchBlock.simulate();
    
    Assert.assertEquals(3, decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("add tg0,x4,x5", decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg1,tg0,x4", decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg2,tg1,tg0", decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
  }
}
