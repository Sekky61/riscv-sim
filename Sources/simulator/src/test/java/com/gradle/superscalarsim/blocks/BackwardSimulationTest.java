package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.branch.BranchFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.loadstore.LoadStoreFunctionUnit;
import com.gradle.superscalarsim.blocks.loadstore.MemoryAccessUnit;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.cpu.CpuConfiguration.FUnit.Type;
import com.gradle.superscalarsim.cpu.CpuState;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
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
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

public class BackwardSimulationTest
{
  Cpu cpu;
  
  @Test
  public void simulateBackwards_oneIntInstruction_simulateToEndAndBack()
  {
    setUp("add x1, x2, x3");
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(31, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertTrue(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals("add",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.cpu.cpuState.reorderBufferBlock.getReorderQueue().findFirst()
            .get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isValid());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isSpeculative());
    Assert.assertTrue(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("add tg0,x2,x3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(12, this.cpu.cpuState.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  public void setUp(String code)
  {
    MockitoAnnotations.openMocks(this);
    
    RegisterModel integer0 = new RegisterModel("x0", true, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer1 = new RegisterModel("x1", false, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, RegisterTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, RegisterTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, RegisterTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, RegisterTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(RegisterTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer0, integer1, integer2, integer3, integer4, integer5)).build();
    
    
    RegisterModel float1 = new RegisterModel("f1", false, RegisterTypeEnum.kFloat, 0f, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, RegisterTypeEnum.kFloat, 5.5f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, RegisterTypeEnum.kFloat, 3.125f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, RegisterTypeEnum.kFloat, 12.25f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float5 = new RegisterModel("f5", false, RegisterTypeEnum.kFloat, 0.01f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(RegisterTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4, float5)).build();
    
    CpuConfiguration cpuCfg = new CpuConfiguration();
    cpuCfg.code             = code;
    cpuCfg.robSize          = 256;
    cpuCfg.lbSize           = 64;
    cpuCfg.sbSize           = 64;
    cpuCfg.fetchWidth       = 3;
    cpuCfg.commitWidth      = 4;
    cpuCfg.btbSize          = 1024;
    cpuCfg.phtSize          = 10;
    cpuCfg.predictorType    = "2bit";
    cpuCfg.predictorDefault = "Weakly Taken";
    // cache
    cpuCfg.cacheLines           = 16;
    cpuCfg.cacheAssoc           = 2;
    cpuCfg.cacheLineSize        = 16;
    cpuCfg.cacheReplacement     = "Random";
    cpuCfg.storeBehavior        = "write-back";
    cpuCfg.storeLatency         = 0;
    cpuCfg.loadLatency          = 0;
    cpuCfg.laneReplacementDelay = 0;
    cpuCfg.addRemainingDelay    = false;
    // 3 FX: +, +, - (delay 2)
    // 3 FP: +, +, - (delay 2)
    // 1 L/S: (delay 1)
    // 2 branch: (delay 3)
    // 1 mem: (delay 1)
    cpuCfg.fUnits    = new CpuConfiguration.FUnit[10];
    cpuCfg.fUnits[0] = new CpuConfiguration.FUnit(1, Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[1] = new CpuConfiguration.FUnit(2, Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[2] = new CpuConfiguration.FUnit(3, Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[3] = new CpuConfiguration.FUnit(4, Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[4] = new CpuConfiguration.FUnit(5, Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[5] = new CpuConfiguration.FUnit(6, Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[6] = new CpuConfiguration.FUnit(7, Type.L_S, 1, new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[7] = new CpuConfiguration.FUnit(8, Type.Branch, 3, new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[8] = new CpuConfiguration.FUnit(9, Type.Branch, 3, new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[9] = new CpuConfiguration.FUnit(10, Type.Memory, 1, new CpuConfiguration.FUnit.Capability[]{});
    
    this.cpu = new Cpu(cpuCfg);
    CpuState cpuState = this.cpu.cpuState;
    
    InitLoader initLoader = new InitLoader();
    
    // Load predefined register files
    initLoader.setRegisterFileModelList(Arrays.asList(integerFile, floatFile));
    
    // This adds the reg files, but also creates speculative registers!
    this.cpu.cpuState.unifiedRegisterFileBlock.setRegistersWithList(new ArrayList<>());
    this.cpu.cpuState.unifiedRegisterFileBlock.loadRegisters(initLoader.getRegisterFileModelList(),
                                                             new RegisterModelFactory());
    
    // Remove sub to get control over FU allocation
    cpu.cpuState.arithmeticFunctionUnitBlocks.get(0).getAllowedOperators().remove("-");
    cpu.cpuState.arithmeticFunctionUnitBlocks.get(1).getAllowedOperators().remove("-");
    cpu.cpuState.fpFunctionUnitBlocks.get(0).getAllowedOperators().remove("-");
    cpu.cpuState.fpFunctionUnitBlocks.get(1).getAllowedOperators().remove("-");
  }
  
  private ArithmeticFunctionUnitBlock getAddFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.arithmeticFunctionUnitBlocks.get(0);
  }
  
  @Test
  public void simulateBackwards_intRawInstructions_simulateToEndAndBack()
  {
    setUp("""
                  add x3, x4, x5
                  add x2, x3, x4
                  add x1, x2, x3""");
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(-2, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(-4, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(-6, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertTrue(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg1,tg0,x4",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg1,tg0,x4",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("add", getAddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getIntegerId());
    Assert.assertEquals(1, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getIntegerId());
    Assert.assertEquals(2, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(2).getIntegerId());
    Assert.assertEquals("add tg0,x4,x5",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertNotEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(3, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.cpu.cpuState.reorderBufferBlock.getReorderQueue().findFirst()
            .get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    Assert.assertTrue(getAddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getAddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add tg0,x4,x5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertEquals("add",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("add",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getInstructionName());
    Assert.assertEquals("add",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(12, this.cpu.cpuState.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  private ArithmeticFunctionUnitBlock getAddSecondFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.arithmeticFunctionUnitBlocks.get(1);
  }
  
  @Test
  public void simulateBackwards_intOneRawConflict_simulateToEndAndBack()
  {
    setUp("""
                  sub x5, x4, x5
                  add x2, x3, x4
                  add x1, x2, x3
                  add x4, x4, x3
                  """);
    
    // x1 = 0
    // x2 = 25
    // x3 = 6
    // x4 = -2
    // x5 = 0
    //
    // Calculation:
    // x5 = -2
    // x2 = 4
    // x1 = 4 + 6 = 10
    // x4 = -2 + 6 = 4
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(4, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(10, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("add tg2,tg1,x3", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-2, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(4, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(4, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("add tg3,x4,x3", getAddSecondFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(4, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getValue(), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("add tg2,tg1,x3",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3", getAddSecondFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(4, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg2,tg1,x3",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(3, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg3,x4,x3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sub tg0,x4,x5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("sub", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    Assert.assertEquals(25, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.01);
  }
  
  private ArithmeticFunctionUnitBlock getSubFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.arithmeticFunctionUnitBlocks.get(2);
  }
  
  @Test
  public void simulateBackwards_oneFloatInstruction_simulateToEndAndBack()
  {
    setUp("""
                  fadd.s f1, f2, f3
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(8.625f, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1")
            .getValue(DataTypeEnum.kFloat), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertTrue(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals("fadd.s",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.reorderBufferBlock.getReorderQueue().findFirst()
            .get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isValid());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isSpeculative());
    Assert.assertTrue(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f2,f3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(12, this.cpu.cpuState.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  private ArithmeticFunctionUnitBlock getFaddFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.fpFunctionUnitBlocks.get(0);
  }
  
  @Test
  public void simulateBackwards_threeFloatRawInstructions_simulateToEndAndBack()
  {
    setUp("""
                  fadd.s f3, f4, f5
                  fadd.s f2, f3, f4
                  fadd.s f1, f2, f3
                  """);
    
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(12.26, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3")
            .getValue(DataTypeEnum.kFloat), 0.001);
    Assert.assertEquals(24.51, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2")
            .getValue(DataTypeEnum.kFloat), 0.001);
    Assert.assertEquals(36.77, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1")
            .getValue(DataTypeEnum.kFloat), 0.001);
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertTrue(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", getFaddFunctionBlock(cpu).getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getIntegerId());
    Assert.assertEquals(1, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getIntegerId());
    Assert.assertEquals(2, this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(2).getIntegerId());
    Assert.assertEquals("fadd.s tg0,f4,f5",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertNotEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(3, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.reorderBufferBlock.getReorderQueue().findFirst()
            .get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    Assert.assertTrue(getFaddFunctionBlock(cpu).isFunctionUnitEmpty());
    Assert.assertTrue(getFaddSecondFunctionBlock(cpu).isFunctionUnitEmpty());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("fadd.s",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getInstructionName());
    Assert.assertEquals("fadd.s",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f4,f5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(12, this.cpu.cpuState.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.cpu.cpuState.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  private ArithmeticFunctionUnitBlock getFaddSecondFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.fpFunctionUnitBlocks.get(1);
  }
  
  @Test
  public void simulate_floatOneRawConflict_usesFullPotentialOfTheProcessor()
  {
    setUp("""
                  fsub.s f5, f4, f5
                  fadd.s f2, f3, f4
                  fadd.s f1, f2, f3
                  fadd.s f4, f4, f3
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(15.375f, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2")
            .getValue(DataTypeEnum.kFloat), 0.001);
    Assert.assertEquals(18.5f, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1")
            .getValue(DataTypeEnum.kFloat), 0.01);
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("fadd.s tg2,tg1,f3", getFaddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(12.24, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5")
            .getValue(DataTypeEnum.kFloat), 0.001);
    Assert.assertEquals(15.375, (float) this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2")
            .getValue(DataTypeEnum.kFloat), 0.001);
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(4, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("fadd.s tg3,f4,f3", getFaddSecondFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3", getFaddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(15.375, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getValue(), 0.001);
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3", getFaddSecondFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5", getFsubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4", getFaddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(4, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5", getFsubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4", getFaddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals(3, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg3,f4,f3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.cpu.cpuState.fpIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fsub.s tg0,f4,f5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.stepBack();
    Assert.assertEquals("fsub.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree,
                        this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    Assert.assertEquals(5.5, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").getValue(), 0.001);
  }
  
  private ArithmeticFunctionUnitBlock getFsubFunctionBlock(Cpu cpu)
  {
    return cpu.cpuState.fpFunctionUnitBlocks.get(2);
  }
  
  @Test
  public void simulateBackwards_jumpFromLabelToLabel_recordToBTB()
  {
    setUp("""
                  jal x0, lab3
                  lab1:
                  jal x0, labFinal
                  lab2:
                  jal x0, lab1
                  lab3:
                  jal x0, lab2
                  labFinal:
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(16, this.cpu.cpuState.branchTargetBuffer.getEntryTarget(4));
    Assert.assertTrue(this.cpu.cpuState.branchTargetBuffer.isEntryUnconditional(8));
    Assert.assertEquals(-1, this.cpu.cpuState.globalHistoryRegister.getHistoryValueAsInt(9));
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock2(cpu).getSimCodeModel());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(-1, this.cpu.cpuState.globalHistoryRegister.getHistoryValueAsInt(6));
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertEquals("jal tg3,labFinal", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(2, getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getArguments().size());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(6).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals(2, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("jal tg2,lab1", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-1, this.cpu.cpuState.globalHistoryRegister.getHistoryValueAsInt(3));
    
    this.cpu.stepBack();
    Assert.assertEquals(3, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(-1, this.cpu.cpuState.globalHistoryRegister.getHistoryValueAsInt(0));
    
    this.cpu.stepBack();
    Assert.assertEquals(4, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg3,labFinal",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg2,lab1", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals(2, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg3,labFinal",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(15, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg1,lab2",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(7, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,lab2",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg0,lab3",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(3, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg0,lab3",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  private BranchFunctionUnitBlock getBranchFunctionUnitBlock1(Cpu cpu)
  {
    return cpu.cpuState.branchFunctionUnitBlocks.get(0);
  }
  
  private BranchFunctionUnitBlock getBranchFunctionUnitBlock2(Cpu cpu)
  {
    return cpu.cpuState.branchFunctionUnitBlocks.get(1);
  }
  
  @Test
  public void simulateBackwards_wellDesignedLoop_oneMisfetch()
  {
    setUp("""
                  loop:
                  beq x3, x0, loopEnd
                  subi x3, x3, 1
                  jal x0, loop
                  loopEnd:
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertNull(getBranchFunctionUnitBlock2(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.01);
    Assert.assertEquals(0, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg13,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg12,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg20,tg18,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(36).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(37).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(39).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(8, this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(1, this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().size());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg13,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg20,tg18,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg13,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg18,tg16,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(33).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg11,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg18,tg16,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(30).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(31).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(33).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg11,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg16,tg14,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg11,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg16,tg14,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(27).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg9,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg14,tg12,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(24).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(25).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(27).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg9,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg14,tg12,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg9,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg12,tg10,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg7,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg12,tg10,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(18).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(19).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg7,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg10,tg8,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg7,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg10,tg8,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(15).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg5,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg8,tg6,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(12).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(13).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(15).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg5,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg8,tg6,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg5,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg3,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(6).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(7).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg1,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg2,x0,loopEnd",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg3,loop",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg1,loop",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq x3,x0,loopEnd",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.01);
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
  }
  
  @Test
  public void simulateBackwards_ifElse_executeFirstFragment()
  {
    
    setUp("""
                  beq x5, x0, labelIf
                  subi x1, x1, 10
                  jal x0, labelFin
                  labelIf:
                  addi x1, x1, 10
                  labelFin:
                  """);
    // x5=0, x1=0
    // x1 = 10
    
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(10, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("addi tg2,x1,10", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("addi tg2,x1,10", getAddFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("addi tg2,x1,10",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("addi tg2,x1,10",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock2(cpu).getSimCodeModel());
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("addi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    this.cpu.stepBack();
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("addi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq x5,x0,labelIf",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void simulateBackwards_ifElse_executeElseFragment()
  {
    setUp("""
                  beq x5, x0, labelIf
                  subi x1, x1, 10
                  jal x0, labelFin
                  labelIf:
                  addi x1, x1, 10
                  labelFin:
                  """);
    // x5 is 0 so we should jump
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(1, this.cpu.cpuState.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(-10, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    Assert.assertNull(getBranchFunctionUnitBlock1(cpu).getSimCodeModel());
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(getSubFunctionBlock(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal tg1,labelFin", getBranchFunctionUnitBlock2(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", getBranchFunctionUnitBlock1(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf",
                        this.cpu.cpuState.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("jal", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("label", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("addi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq x3,x0,labelIf",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("beq", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void simulateBackwards_oneStore_savesIntInMemory()
  {
    setUp("""
                  sw x3, 0(x2)
                  """);
    
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(new InitLoader()).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(6, this.cpu.cpuState.loadStoreInterpreter.interpretInstruction(
            new SimCodeModel(loadCodeModel, -1, -1), 0).getSecond(), 0.01);
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertNull(getLoadStoreFunctionUnit(cpu).getSimCodeModel());
    Assert.assertEquals(25, this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).getAddress());
    Assert.assertTrue(this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).isSourceReady());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertNull(getLoadStoreFunctionUnit(cpu).getSimCodeModel());
    Assert.assertEquals(25, this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).getAddress());
    Assert.assertTrue(this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).isSourceReady());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertNotNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-1, this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).getAddress());
    Assert.assertTrue(this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).isSourceReady());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(-1, this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).getAddress());
    Assert.assertTrue(this.cpu.cpuState.storeBufferBlock.getStoreMap().get(0).isSourceReady());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("sw x3,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    
    this.cpu.stepBack();
    Assert.assertEquals("sw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
  }
  
  private LoadStoreFunctionUnit getLoadStoreFunctionUnit(Cpu cpu)
  {
    return cpu.cpuState.loadStoreFunctionUnits.get(0);
  }
  
  private MemoryAccessUnit getMemoryAccessUnit(Cpu cpu)
  {
    return cpu.cpuState.memoryAccessUnits.get(0);
  }
  
  @Test
  public void simulateBackwards_oneLoad_loadsIntInMemory()
  {
    setUp("""
                  lw x1, 0(x2)
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.0);
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertNull(getLoadStoreFunctionUnit(cpu).getSimCodeModel());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    Assert.assertEquals(25, this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).getAddress());
    Assert.assertTrue(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertNull(getLoadStoreFunctionUnit(cpu).getSimCodeModel());
    Assert.assertEquals("lw tg0,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(25, this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).getAddress());
    Assert.assertFalse(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg0,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-1, this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).getAddress());
    Assert.assertFalse(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg0,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(-1, this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).getAddress());
    Assert.assertFalse(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
    Assert.assertEquals("lw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw tg0,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
  }
  
  @Test
  public void simulateBackwards_loadBypassing_successfullyLoadsFromStoreBuffer()
  {
    setUp("""
                  subi x4, x4, 5
                  sw x3, 0(x2)
                  lw x1, 0(x2)
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(0, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(6, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertNull(getLoadStoreFunctionUnit(cpu).getSimCodeModel());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(25, this.cpu.cpuState.loadBufferBlock.getLoadMap().get(2).getAddress());
    Assert.assertTrue(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(2).isDestinationReady());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.cpu.cpuState.loadBufferBlock.getLoadMap().get(2).isDestinationReady());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.storeBufferBlock.getStoreMap().get(1).isSourceReady());
    Assert.assertEquals(25, this.cpu.cpuState.storeBufferBlock.getStoreMap().get(1).getAddress());
    Assert.assertFalse(this.cpu.cpuState.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("subi tg0,x4,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x4,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(1)
            .getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x4,5",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x4,5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
  }
  
  @Test
  public void simulateBackwards_loadForwarding_FailsDuringMA()
  {
    setUp("""
                  subi x3, x3, 5
                  sw x3, 0(x2)
                  lw x1, 0(x2)
                  """);
    
    getMemoryAccessUnit(cpu).setDelay(3);
    getMemoryAccessUnit(cpu).setBaseDelay(3);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(1, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    
    this.cpu.stepBack();
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw tg2,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("lw tg1,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(1)
            .getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x3,5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
  }
  
  @Test
  public void simulateBackwards_loadForwarding_FailsAndRerunsFromLoad()
  {
    setUp("""
                  subi x3, x3, 5
                  sw x3, 0(x2)
                  lw x1, 0(x2)
                  """);
    
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    this.cpu.step();
    Assert.assertEquals(1, this.cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(18).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw tg2,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw tg2,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    
    this.cpu.stepBack();
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.cpu.cpuState.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("sw tg0,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", getMemoryAccessUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", getLoadStoreFunctionUnit(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", getSubFunctionBlock(cpu).getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(getMemoryAccessUnit(cpu).getSimCodeModel());
    
    this.cpu.stepBack();
    Assert.assertEquals(1, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.cpu.cpuState.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(0)
            .getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadStoreIssueWindowBlock.getIssuedInstructions().get(1)
            .getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.cpu.cpuState.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.cpu.cpuState.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5",
                        this.cpu.cpuState.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("nop", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x3,5",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.cpu.cpuState.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.stepBack();
    Assert.assertEquals("subi", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.cpu.cpuState.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.cpu.cpuState.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.cpu.cpuState.storeBufferBlock.getQueueSize());
  }
}
