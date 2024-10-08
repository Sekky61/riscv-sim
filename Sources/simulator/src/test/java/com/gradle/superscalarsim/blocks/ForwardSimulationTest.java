package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.*;
import com.gradle.superscalarsim.blocks.loadstore.*;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.code.*;
import com.gradle.superscalarsim.cpu.*;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.loader.DynamicDataProvider;
import com.gradle.superscalarsim.loader.IDataProvider;
import com.gradle.superscalarsim.loader.StaticDataProvider;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.register.RegisterFile;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gradle.superscalarsim.blocks.branch.BitPredictor.WEAKLY_TAKEN;

public class ForwardSimulationTest
{
  // Use this to step the sim
  Cpu cpu;
  
  IDataProvider staticDataProvider;
  
  InstructionMemoryBlock instructionMemoryBlock;
  private SimulationStatistics simulationStatistics;
  
  private InstructionFetchBlock instructionFetchBlock;
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  private IssueWindowSuperBlock issueWindowSuperBlock;
  
  private UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  private RenameMapTableBlock renameMapTableBlock;
  private ReorderBufferBlock reorderBufferBlock;
  
  private IssueWindowBlock aluIssueWindowBlock;
  private ArithmeticFunctionUnitBlock addFunctionBlock;
  private ArithmeticFunctionUnitBlock addSecondFunctionBlock;
  private ArithmeticFunctionUnitBlock mulFunctionBlock;
  
  private IssueWindowBlock fpIssueWindowBlock;
  private ArithmeticFunctionUnitBlock faddFunctionBlock;
  private ArithmeticFunctionUnitBlock faddSecondFunctionBlock;
  private ArithmeticFunctionUnitBlock fmulFunctionBlock;
  
  private IssueWindowBlock branchIssueWindowBlock;
  private BranchFunctionUnitBlock branchFunctionUnitBlock1;
  private BranchFunctionUnitBlock branchFunctionUnitBlock2;
  
  private IssueWindowBlock loadStoreIssueWindowBlock;
  private LoadStoreFunctionUnit loadStoreFunctionUnit;
  private MemoryAccessUnit memoryAccessUnit;
  private StoreBufferBlock storeBufferBlock;
  private LoadBufferBlock loadBufferBlock;
  
  private GShareUnit gShareUnit;
  private BranchTargetBuffer branchTargetBuffer;
  private GlobalHistoryRegister globalHistoryRegister;
  
  private CodeArithmeticInterpreter arithmeticInterpreter;
  private CodeBranchInterpreter branchInterpreter;
  private CodeLoadStoreInterpreter loadStoreInterpreter;
  
  private MemoryModel memoryModel;
  
  private Cache cache;
  
  @Before
  public void setUp()
  {
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
    
    CpuConfig cpuCfg = new CpuConfig();
    cpuCfg.robSize               = 256;
    cpuCfg.lbSize                = 64;
    cpuCfg.sbSize                = 64;
    cpuCfg.fetchWidth            = 3;
    cpuCfg.branchFollowLimit     = 1;
    cpuCfg.commitWidth           = 4;
    cpuCfg.btbSize               = 1024;
    cpuCfg.phtSize               = 10;
    cpuCfg.predictorType         = BitPredictor.PredictorType.TWO_BIT_PREDICTOR;
    cpuCfg.predictorDefaultState = WEAKLY_TAKEN;// "Weakly Taken";
    // cache
    cpuCfg.useCache             = true;
    cpuCfg.cacheLines           = 16;
    cpuCfg.cacheAssoc           = 2;
    cpuCfg.cacheLineSize        = 16;
    cpuCfg.cacheReplacement     = "Random";
    cpuCfg.storeBehavior        = "write-back";
    cpuCfg.cacheAccessDelay     = 1;
    cpuCfg.storeLatency         = 1;
    cpuCfg.loadLatency          = 1;
    cpuCfg.laneReplacementDelay = 0;
    cpuCfg.speculativeRegisters = 320;
    cpuCfg.callStackSize        = 512;
    // 3 FX: +, +, - (delay 2)
    // 3 FP: +, +, * (delay 2)
    // 1 L/S: (delay 1)
    // 2 branch: (delay 3)
    // 1 mem: (delay 1)
    cpuCfg.fUnits = Arrays.asList(new FunctionalUnitDescription(0, FunctionalUnitDescription.Type.FX, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(FunctionalUnitDescription.CapabilityName.addition, 2)), "FX"),
                                  new FunctionalUnitDescription(1, FunctionalUnitDescription.Type.FX, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.addition, 2)), "FX"),
                                  new FunctionalUnitDescription(2, FunctionalUnitDescription.Type.FX, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.multiplication, 2)), "FX"),
                                  new FunctionalUnitDescription(3, FunctionalUnitDescription.Type.FP, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.addition, 2)), "FP"),
                                  new FunctionalUnitDescription(4, FunctionalUnitDescription.Type.FP, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.addition, 2)), "FP"),
                                  new FunctionalUnitDescription(5, FunctionalUnitDescription.Type.FP, Arrays.asList(
                                          new FunctionalUnitDescription.Capability(
                                                  FunctionalUnitDescription.CapabilityName.multiplication, 2)), "FP"),
                                  new FunctionalUnitDescription(6, FunctionalUnitDescription.Type.L_S, 1, "L/S"),
                                  new FunctionalUnitDescription(7, FunctionalUnitDescription.Type.Branch, 3, "Branch"),
                                  new FunctionalUnitDescription(8, FunctionalUnitDescription.Type.Branch, 3, "Branch"),
                                  new FunctionalUnitDescription(9, FunctionalUnitDescription.Type.Memory, 1, "Mem"));
    
    
    SimulationConfig cfg = new SimulationConfig("", new ArrayList<>(), cpuCfg, 0);
    
    RegisterFile registerFile = new RegisterFile(Arrays.asList(integerFile, floatFile), List.of());
    this.staticDataProvider = new DynamicDataProvider(registerFile,
                                                      new StaticDataProvider().getInstructionFunctionModels());
    this.cpu                = new Cpu(cfg, null, staticDataProvider);
    CpuState cpuState = this.cpu.cpuState;
    // Fix the statistics - nothing gets allocated
    cpuState.statistics.allocateInstructionStats(50);
    
    this.instructionMemoryBlock    = cpuState.instructionMemoryBlock;
    this.simulationStatistics      = cpuState.statistics;
    this.unifiedRegisterFileBlock  = cpuState.unifiedRegisterFileBlock;
    this.renameMapTableBlock       = cpuState.renameMapTableBlock;
    this.globalHistoryRegister     = cpuState.globalHistoryRegister;
    this.gShareUnit                = cpuState.gShareUnit;
    this.branchTargetBuffer        = cpuState.branchTargetBuffer;
    this.memoryModel               = cpuState.memoryModel;
    this.loadStoreInterpreter      = cpuState.loadStoreInterpreter;
    this.instructionFetchBlock     = cpuState.instructionFetchBlock;
    this.decodeAndDispatchBlock    = cpuState.decodeAndDispatchBlock;
    this.reorderBufferBlock        = cpuState.reorderBufferBlock;
    this.issueWindowSuperBlock     = cpuState.issueWindowSuperBlock;
    this.arithmeticInterpreter     = cpuState.arithmeticInterpreter;
    this.branchInterpreter         = cpuState.branchInterpreter;
    this.storeBufferBlock          = cpuState.storeBufferBlock;
    this.loadBufferBlock           = cpuState.loadBufferBlock;
    this.aluIssueWindowBlock       = cpuState.aluIssueWindowBlock;
    this.fpIssueWindowBlock        = cpuState.fpIssueWindowBlock;
    this.loadStoreIssueWindowBlock = cpuState.loadStoreIssueWindowBlock;
    this.branchIssueWindowBlock    = cpuState.branchIssueWindowBlock;
    this.cache                     = cpuState.cache;
    
    // FU
    this.addFunctionBlock       = cpuState.arithmeticFunctionUnitBlocks.get(0);
    this.addSecondFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(1);
    this.mulFunctionBlock       = cpuState.arithmeticFunctionUnitBlocks.get(2);
    
    this.faddFunctionBlock       = cpuState.fpFunctionUnitBlocks.get(0);
    this.faddSecondFunctionBlock = cpuState.fpFunctionUnitBlocks.get(1);
    this.fmulFunctionBlock       = cpuState.fpFunctionUnitBlocks.get(2);
    
    this.loadStoreFunctionUnit    = cpuState.loadStoreFunctionUnits.get(0);
    this.memoryAccessUnit         = cpuState.memoryAccessUnits.get(0);
    this.branchFunctionUnitBlock1 = cpuState.branchFunctionUnitBlocks.get(0);
    this.branchFunctionUnitBlock2 = cpuState.branchFunctionUnitBlocks.get(1);
    
    // This adds the reg files, but also creates speculative registers!
    this.unifiedRegisterFileBlock.setRegistersWithList(Arrays.asList(integerFile, floatFile));
  }
  
  ///////////////////////////////////////////////////////////
  ///                 Arithmetic Tests                    ///
  ///////////////////////////////////////////////////////////
  
  @Test
  public void simulate_oneIntInstruction_finishesAfterSevenTicks()
  {
    
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 add x1,x2,x3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getInstructionName());
    Assert.assertEquals("add tg0,x2,x3", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals("add", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.reorderBufferBlock.getReorderQueue().findFirst().get().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isValid());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).isSpeculative());
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(31, (int) unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  @Test
  public void simulate_threeIntRawInstructions_finishesAfterElevenTicks()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 add x3,x4,x5
                                 add x2,x3,x4
                                 add x1,x2,x3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getInstructionName());
    Assert.assertEquals("add tg0,x4,x5", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getRenamedCodeLine());
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().get(0).getIntegerId());
    Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().get(1).getIntegerId());
    Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().get(2).getIntegerId());
    Assert.assertEquals("add tg0,x4,x5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.reorderBufferBlock.getReorderQueue().findFirst().get().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg1,tg0,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg1,tg0,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(-2, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(-4, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(-6, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_intOneRawConflict_usesFullPotentialOfTheProcessor()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 mul x5,x4,x5
                                 add x2,x3,x4
                                 add x1,x2,x3
                                 add x4,x4,x3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    // Fetch 3
    this.cpu.step();
    Assert.assertEquals("mul", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    // Fetch last, rename 3
    this.cpu.step();
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("mul tg0,x4,x5", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    // Issue the three, decode the last
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg3,x4,x3", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x4,x5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    
    // The [0] or [1] can be executed, [0] is chosen (no particular reason), removed from the issue window
    // Also the last instruction made it to issue window
    this.cpu.step();
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    // 2 FUs busy, last adder did not have anything to work on
    Assert.assertEquals("mul tg0,x4,x5", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    // The last instruction is executed by addSecondFunctionBlock
    // One instruction waits for the tg1 to finish
    this.cpu.step();
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    // 3 FUs busy
    Assert.assertEquals("add tg3,x4,x3", this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x4,x5", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    // First two computed
    this.cpu.step();
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    // addFunctionBlock starts work on the last instruction immediately
    Assert.assertEquals("add tg2,tg1,x3", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3", this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    // tg0 done, tg1 done
    Assert.assertEquals(4, (int) this.unifiedRegisterFileBlock.getRegister("tg1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // The first two instructions are committed
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    // Last one is done, but the #3 (id 2) is still computing in addFunctionBlock
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertEquals("add tg2,tg1,x3", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    // Registers
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(4, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    // Head of ROB is committable
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    // And committed
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(4, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(10, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_oneFloatInstruction_finishesAfterSevenTicks()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 fadd.s f1,f2,f3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f2,f3", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // Instruction is in the issue window
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals("fadd.s", this.fpIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.reorderBufferBlock.getReorderQueue().findFirst().get().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isValid());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).isSpeculative());
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    // Instruction moves from issue window to function block
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(8.625f, (float) unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.001f);
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
  }
  
  @Test
  public void simulate_threeFloatRawInstructions_finishesAfterElevenTicks()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 fadd.s f3,f4,f5
                                 fadd.s f2,f3,f4
                                 fadd.s f1,f2,f3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f4,f5", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,tg0,f4", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getRenamedCodeLine());
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getCodeBuffer().isEmpty());
    Assert.assertEquals(0, this.fpIssueWindowBlock.getIssuedInstructions().get(0).getIntegerId());
    Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().get(1).getIntegerId());
    Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().get(2).getIntegerId());
    Assert.assertEquals("fadd.s tg0,f4,f5",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.reorderBufferBlock.getReorderQueue().findFirst().get().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(12.26, (float) this.unifiedRegisterFileBlock.getRegister("f3").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(24.51, (float) this.unifiedRegisterFileBlock.getRegister("f2").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(36.77, (float) this.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  ///////////////////////////////////////////////////////////
  ///                 Branch Tests                        ///
  ///////////////////////////////////////////////////////////
  
  @Test
  public void simulate_floatOneRawConflict_usesFullPotentialOfTheProcessor()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 fmul.s f5,f4,f5
                                 fadd.s f2,f3,f4
                                 fadd.s f1,f2,f3
                                 fadd.s f4,f4,f3
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    
    this.cpu.step();
    Assert.assertEquals("fmul.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fmul.s tg0,f4,f5", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg3,f4,f3", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("fmul.s tg0,f4,f5",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // 2 left in issue, 2 executing
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fmul.s tg0,f4,f5", this.fmulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4", this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // second clock of execution. Last started also, only tg2 is waiting
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3", this.faddSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fmul.s tg0,f4,f5", this.fmulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4", this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // 0, 1 ready, 2 can be executed, 3 executing
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertEquals("fadd.s tg3,f4,f3", this.faddSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3", this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(15.375f, (float) this.unifiedRegisterFileBlock.getRegister("tg1").getValue(DataTypeEnum.kFloat),
                        0.001f);
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertEquals("fadd.s tg2,tg1,f3", this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0.1224, (float) this.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
                        0.001);
    Assert.assertEquals(15.375, (float) this.unifiedRegisterFileBlock.getRegister("f2").getValue(DataTypeEnum.kFloat),
                        0.001);
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(15.375, (float) this.unifiedRegisterFileBlock.getRegister("f2").getValue(DataTypeEnum.kFloat),
                        0.001);
    Assert.assertEquals(18.5, (float) this.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  @Test
  public void simulate_jumpFromLabelToLabel_recordToBTB()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  jal x0,lab3
                                 lab1:
                                  jal x0,labFinal
                                 lab2:
                                  jal x0,lab1
                                 lab3:
                                  jal x0,lab2
                                 labFinal:
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg0,lab3", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    // No conditionals, so no history
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg1,lab2", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg0,lab3",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg2,lab1", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg1,lab2",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg3,labFinal", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
    
    this.cpu.step();
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals(2, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg3,labFinal",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg2,lab1", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertEquals(12, this.branchTargetBuffer.getEntryTarget(0));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(0));
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("jal tg2,lab1", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(8, this.branchTargetBuffer.getEntryTarget(12));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(12));
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(6).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(9).isReadyToBeCommitted());
    Assert.assertEquals(4, this.branchTargetBuffer.getEntryTarget(8));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(8));
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(16, this.branchTargetBuffer.getEntryTarget(4));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(8));
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void simulate_wellDesignedLoop_oneMisfetch()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  loop:
                                    beq x3,x0,loopEnd
                                    subi x3,x3,1
                                    jal x0,loop
                                  loopEnd:
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    // What should happen:
    // The first iteration beq cannot be taken (no target)
    // The predictor is weakly taken, so the second iteration it will be weakly not taken
    // x3 is 6 so 6 iterations will be executed
    
    // First fetch (3)
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    // Prediction is not to take the branch (default value of predictor)
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(6, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    // Second fetch
    // jal has ID 3 - ids are not continuous, but reflect the order of fetch, counting nops
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    // First decode
    Assert.assertEquals("beq x3,x0,loopEnd", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    // jal is an unconditional jump, so fetch starts from `loop:` again
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    // Decode
    Assert.assertEquals("jal tg1,loop", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    // subi got to the ALU issue window
    Assert.assertEquals("subi tg0,x3,1", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    // ROB - first two instructions are in the ROB, not ready
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg0,x0,loopEnd", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg3,loop",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    // There is a new instruction in the function block
    Assert.assertEquals("subi tg2,tg0,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    // beq (id 1), subi (id 2) and jal (id 3) are ready
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted()); // jal
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    // beq is ID 6
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(6).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(7).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(9).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(9).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg7,tg6,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg7,tg6,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(12).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(13).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(15).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg3,tg7,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(15).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg3,tg7,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg3,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(18).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(19).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(21).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg0,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg3,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(21).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg0,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg9,tg2,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg0,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg7,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg9,tg2,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(24).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(25).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(27).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg8,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg7,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg1,tg9,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(27).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg8,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg7,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg1,tg9,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg8,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg1,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(30).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(31).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(33).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg1,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(33).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg11,tg6,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg11,tg6,1", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(36).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(37).isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(39).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.addFunctionBlock.getSimCodeModel());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_ifElse_executeFirstFragment()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  beq x5,x0,labelIf
                                  mul x1,x1,x2
                                  jal x0,labelFin
                                 labelIf:
                                  addi x1,x1,10
                                 labelFin:
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    
    // x5 id zero in the beginning, so the first conditional is taken
    
    // First fetch
    this.cpu.step();
    Assert.assertEquals(8, this.instructionFetchBlock.getPc());
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("mul", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    // Third instruction is not fetched - it is a second branch
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
    
    // Second fetch
    this.cpu.step();
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue()); // Not taken, destination unknown
    Assert.assertEquals(20, this.instructionFetchBlock.getPc());
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    // Decode has the first 2 fetched (nop is filtered)
    Assert.assertEquals("beq x5,x0,labelIf", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x1,x2", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    
    this.cpu.step();
    // So nothing is fetched
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    // Decode has just the jal, the addi was discarded
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg1,labelFin", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    // beq and mul moved to their respective issue windows
    Assert.assertEquals("beq x5,x0,labelIf",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x1,x2", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    // Last fetch was empty, so now decode is empty
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x1,x2", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x1,x2", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    // First instruction ready to be committed
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("addi tg0,x1,10", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg0,x1,10", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg0,x1,10", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg0,x1,10", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(21).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(10, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(1, this.globalHistoryRegister.getRegisterValue());
  }
  
  
  ///////////////////////////////////////////////////////////
  ///                 Load/Store Tests                    ///
  ///////////////////////////////////////////////////////////
  
  @Test
  public void simulate_ifElse_executeElseFragment()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  beq x3, x0, labelIf
                                  mul x1, x3, x3
                                  jal x0, labelFin
                                 labelIf:
                                  addi x1, x1, 10
                                 labelFin:
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    
    this.cpu.step();
    // 3 fetches, but it stops before second branch (fetch limitation)
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("mul", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    
    this.cpu.step();
    // another 3 fetches, 2 from last one moved to decode
    // The jal jump cannot be followed, it is seen for the first time
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq x3,x0,labelIf", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg1,labelFin", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    // The addi does not get out of the decode buffer, because the jal is computed and followed
    Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).isReadyToBeCommitted());
    Assert.assertEquals(36, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    
    this.cpu.step();
    // No conditional has been predicted
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValue());
  }
  
  @Test
  public void simulate_oneStore_savesIntInMemory()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  sw x3,0(x2)
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    
    this.cpu.step();
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("sw x3,0(x2)", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(-1, this.storeBufferBlock.getStoreBufferItem(0).getAddress());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(0).isSourceReady());
    Assert.assertEquals("sw x3,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-1, this.storeBufferBlock.getStoreBufferItem(0).getAddress());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(0).isSourceReady());
    
    this.cpu.step();
    // Out of L/S. Address computed. Already in MAU.
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals(25, this.storeBufferBlock.getStoreBufferItem(0).getAddress());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(0).isSourceReady());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    // The store delay is 1, MAU delay is 1, so after two clocks...
    this.cpu.step();
    this.cpu.step();
    // ... it should be executed and ready to be committed
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    
    Assert.assertEquals(6, this.cache.getData(25, 4));
  }
  
  @Test
  public void simulate_oneLoad_loadsIntInMemory()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 lw x1,0(x2)
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    
    this.cpu.step();
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("lw tg0,0(x2)", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg0,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(-1, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    Assert.assertEquals("lw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg0,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-1, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    
    this.cpu.step();
    // load arrived to MMU. The cache delay is 1, main memory delay is 1. So after two clocks, it should be executed
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals("lw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    
    this.cpu.step();
    this.cpu.step();
    
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertTrue(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.0);
  }
  
  @Test
  public void simulate_loadBypassing_successfullyLoadsFromStoreBuffer()
  {
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                 mul x4,x4,x4
                                 sw x3,0(x2)
                                 lw x1,0(x2)
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    // The store will execute first, load will take the value from the store buffer
    // Load will not touch memory.
    
    this.cpu.step();
    Assert.assertEquals("mul", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("mul tg0,x4,x4", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.decodeAndDispatchBlock.getCodeBuffer().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.decodeAndDispatchBlock.getCodeBuffer().get(2).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals("sw x3,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x4,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x4,x4", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(2).isDestinationReady());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(2).isReadyToBeCommitted());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(1).isSourceReady());
    Assert.assertEquals(25, this.storeBufferBlock.getStoreBufferItem(1).getAddress());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertEquals("mul tg0,x4,x4", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    // Store moved to MAU (as it is about to commit)
    Assert.assertEquals("sw x3,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("sw x3,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(2).getAddress());
    Assert.assertTrue(this.loadBufferBlock.getLoadBufferItem(2).isDestinationReady());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isReadyToBeCommitted());
    
    Assert.assertEquals("sw x3,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    this.cpu.step();
    // store and load in rob
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).isReadyToBeCommitted());
    Assert.assertTrue(this.loadBufferBlock.getLoadBufferItem(2).hasBypassed());
    this.cpu.step();
    // Nothing in ROB
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    
    
    Assert.assertEquals(6, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void simulate_loadForwarding_FailsDuringMA()
  {
    // Program:
    //
    // mul x3 x3 x3 # x3: 6 -> 36
    // sw x3 x2 0   # x3 (1) is stored to memory [25+0]
    // lw x1 x2 0   # x1: 0 -> 36
    CodeParser codeParser = new CodeParser(staticDataProvider);
    codeParser.parseCode("""
                                  mul x3, x3, x3
                                  sw x3, 0(x2)
                                  lw x1, 0(x2)
                                 """);
    instructionMemoryBlock.setCode(codeParser.getInstructions());
    instructionMemoryBlock.setLabels(codeParser.getSymbolTable());
    // Load will be ready to execute first. It will go to MAU, because the store address will not be computed yet.
    // Store will finish later. The load will be flushed and re-executed.
    
    // Watch out! MAU setBaseDelay no longer does anything
    this.memoryAccessUnit.setDelay(3);
    this.memoryAccessUnit.setBaseDelay(3);
    
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(25, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(6, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("mul", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals(3, this.decodeAndDispatchBlock.getCodeBuffer().size());
    
    this.cpu.step();
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getCodeBuffer().size());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    // compute load address
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    // store waits for free FU
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    // load has address, goes to MAU (no forwarding, because store is not ready)
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    // store address is computed
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("mul tg0,x3,x3", this.mulFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    // mul is ready
    Assert.assertNull(this.mulFunctionBlock.getSimCodeModel());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).isReadyToBeCommitted());
    // clock 2 (final) of MA
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    // Store can start computing address
    Assert.assertEquals("sw tg0,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    
    
    this.cpu.step();
    // Load is finished computing, sits in ROB
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).isReadyToBeCommitted());
    // MA free, so sw starts. It should be in cache so 1 clock
    Assert.assertEquals("sw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    // Conflict detected (bad load), throw it out, fetch again (fetch stops being stalled)
    
    this.cpu.step();
    // Load memory access done, now for the store. It should be in cache so 1 clock
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    
    this.cpu.step();
    // Store is committed
    Assert.assertEquals(2, this.simulationStatistics.committedInstructions);
    // but while commiting, it becomes apparent that the load was bad speculation
    Assert.assertTrue(this.reorderBufferBlock.reorderQueue.isEmpty());
    // immediately fetch again
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals("lw tg1,0(x2)", this.decodeAndDispatchBlock.getCodeBuffer().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    // Now to mem access. should be in cache.
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    // in cache
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.reorderQueue.getFirst().isReadyToBeCommitted());
    
    this.cpu.step();
    
    
    Assert.assertEquals(36, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
}
