package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.arithmetic.AluIssueWindowBlock;
import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.arithmetic.FpIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.*;
import com.gradle.superscalarsim.blocks.loadstore.*;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.code.*;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.*;

public class ForwardSimulationTest
{
  // Use this to step the sim
  Cpu cpu;
  
  InitLoader initLoader;
  
  InstructionMemoryBlock instructionMemoryBlock;
  private SimCodeModelAllocator simCodeModelAllocator;
  private StatisticsCounter statisticsCounter;
  
  private InstructionFetchBlock instructionFetchBlock;
  private DecodeAndDispatchBlock decodeAndDispatchBlock;
  private IssueWindowSuperBlock issueWindowSuperBlock;
  
  private UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  private RenameMapTableBlock renameMapTableBlock;
  private ReorderBufferBlock reorderBufferBlock;
  
  private AluIssueWindowBlock aluIssueWindowBlock;
  private ArithmeticFunctionUnitBlock addFunctionBlock;
  private ArithmeticFunctionUnitBlock addSecondFunctionBlock;
  private ArithmeticFunctionUnitBlock subFunctionBlock;
  
  private FpIssueWindowBlock fpIssueWindowBlock;
  private ArithmeticFunctionUnitBlock faddFunctionBlock;
  private ArithmeticFunctionUnitBlock faddSecondFunctionBlock;
  private ArithmeticFunctionUnitBlock fsubFunctionBlock;
  
  private BranchIssueWindowBlock branchIssueWindowBlock;
  private BranchFunctionUnitBlock branchFunctionUnitBlock1;
  private BranchFunctionUnitBlock branchFunctionUnitBlock2;
  
  private LoadStoreIssueWindowBlock loadStoreIssueWindowBlock;
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
  
  @Before
  public void setUp()
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
    cpuCfg.fUnits[0] = new CpuConfiguration.FUnit(1, CpuConfiguration.FUnit.Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[1] = new CpuConfiguration.FUnit(2, CpuConfiguration.FUnit.Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[2] = new CpuConfiguration.FUnit(3, CpuConfiguration.FUnit.Type.FX, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[3] = new CpuConfiguration.FUnit(4, CpuConfiguration.FUnit.Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[4] = new CpuConfiguration.FUnit(5, CpuConfiguration.FUnit.Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[5] = new CpuConfiguration.FUnit(6, CpuConfiguration.FUnit.Type.FP, 2,
                                                  new CpuConfiguration.FUnit.Capability[]{CpuConfiguration.FUnit.Capability.addition});
    cpuCfg.fUnits[6] = new CpuConfiguration.FUnit(7, CpuConfiguration.FUnit.Type.L_S, 1,
                                                  new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[7] = new CpuConfiguration.FUnit(8, CpuConfiguration.FUnit.Type.Branch, 3,
                                                  new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[8] = new CpuConfiguration.FUnit(9, CpuConfiguration.FUnit.Type.Branch, 3,
                                                  new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.fUnits[9] = new CpuConfiguration.FUnit(10, CpuConfiguration.FUnit.Type.Memory, 1,
                                                  new CpuConfiguration.FUnit.Capability[]{});
    cpuCfg.code      = "";
    
    this.cpu = new Cpu(cpuCfg);
    CpuState cpuState = this.cpu.cpuState;
    
    this.instructionMemoryBlock    = cpuState.instructionMemoryBlock;
    this.statisticsCounter         = cpuState.statisticsCounter;
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
    
    // FU
    this.addFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(0);
    // Remove subtract
    addFunctionBlock.getAllowedOperators().remove("-");
    this.addSecondFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(1);
    // Remove subtract
    addSecondFunctionBlock.getAllowedOperators().remove("-");
    this.subFunctionBlock  = cpuState.arithmeticFunctionUnitBlocks.get(2);
    this.faddFunctionBlock = cpuState.fpFunctionUnitBlocks.get(0);
    // Remove subtract
    faddFunctionBlock.getAllowedOperators().remove("-");
    this.faddSecondFunctionBlock = cpuState.fpFunctionUnitBlocks.get(1);
    // Remove subtract
    faddSecondFunctionBlock.getAllowedOperators().remove("-");
    this.fsubFunctionBlock        = cpuState.fpFunctionUnitBlocks.get(2);
    this.loadStoreFunctionUnit    = cpuState.loadStoreFunctionUnits.get(0);
    this.memoryAccessUnit         = cpuState.memoryAccessUnits.get(0);
    this.branchFunctionUnitBlock1 = cpuState.branchFunctionUnitBlocks.get(0);
    this.branchFunctionUnitBlock2 = cpuState.branchFunctionUnitBlocks.get(1);
    
    this.initLoader = new InitLoader();
    // Load predefined register files
    this.initLoader.setRegisterFileModelList(Arrays.asList(integerFile, floatFile));
    
    // This adds the reg files, but also creates speculative registers!
    this.unifiedRegisterFileBlock.setRegistersWithList(new ArrayList<>());
    this.unifiedRegisterFileBlock.loadRegisters(this.initLoader.getRegisterFileModelList(), new RegisterModelFactory());
  }
  
  ///////////////////////////////////////////////////////////
  ///                 Arithmetic Tests                    ///
  ///////////////////////////////////////////////////////////
  
  @Test
  public void simulate_oneIntInstruction_finishesAfterSevenTicks()
  {
    InputCodeArgument argument1 = new InputCodeArgument("rd", "x1");
    InputCodeArgument argument2 = new InputCodeArgument("rs1", "x2");
    InputCodeArgument argument3 = new InputCodeArgument("rs2", "x3");
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x1,x2,x3").hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    List<InputCodeModel> instructions = Collections.singletonList(ins1);
    
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("add tg0,x2,x3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals("add", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add",
                        this.reorderBufferBlock.getReorderQueue().findFirst().get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isValid());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).reorderFlags.isSpeculative());
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
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
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x3,x4,x5").hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x2,x3,x4").hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x1,x2,x3").hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getInstructionName());
    Assert.assertEquals("add", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getInstructionName());
    Assert.assertEquals("add tg0,x4,x5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().get(0).getIntegerId());
    Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().get(1).getIntegerId());
    Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().get(2).getIntegerId());
    Assert.assertEquals("add tg0,x4,x5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,tg0,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add",
                        this.reorderBufferBlock.getReorderQueue().findFirst().get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg1,tg0,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("add tg2,tg1,tg0",
                        this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add", this.addFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
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
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x5").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x5").build();
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x2").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    InputCodeArgument argumentAdd21 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x4").build();
    InputCodeArgument argumentAdd22 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argumentAdd23 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("sub")).hasInstructionName("sub")
            .hasCodeLine("sub x5,x4,x5").hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x2,x3,x4").hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x1,x2,x3").hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("add")).hasInstructionName("add")
            .hasCodeLine("add x4,x4,x3").hasArguments(Arrays.asList(argumentAdd21, argumentAdd22, argumentAdd23))
            .build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals("sub", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("add", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sub tg0,x4,x5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg3,x4,x3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("add tg2,tg1,x3", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("add tg3,x4,x3", this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sub tg0,x4,x5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg1,x3,x4", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("add tg3,x4,x3", this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("add tg2,tg1,x3", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
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
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("add tg2,tg1,x3", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(-2, (int) this.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(4, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(4, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(10, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_oneFloatInstruction_finishesAfterSevenTicks()
  {
    InputCodeArgument argument1 = new InputCodeArgument("rd", "f1");
    InputCodeArgument argument2 = new InputCodeArgument("rs1", "f2");
    InputCodeArgument argument3 = new InputCodeArgument("rs2", "f3");
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f1,f2,f3")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    List<InputCodeModel> instructions = Collections.singletonList(ins1);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f2,f3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    // Instruction is in the issue window
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertEquals("fadd.s", this.fpIssueWindowBlock.getIssuedInstructions().get(0).getInstructionName());
    Assert.assertNotEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s",
                        this.reorderBufferBlock.getReorderQueue().findFirst().get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isValid());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).reorderFlags.isSpeculative());
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    
    this.cpu.step();
    // Instruction moves from issue window to function block
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
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
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f3").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f4").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f5").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f3,f4,f5")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f2,f3,f4")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f1,f2,f3")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals(12, this.instructionFetchBlock.getPc());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
    Assert.assertTrue(this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getInstructionName());
    Assert.assertEquals("fadd.s tg0,f4,f5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    
    this.cpu.step();
    Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
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
    Assert.assertEquals("fadd.s",
                        this.reorderBufferBlock.getReorderQueue().findFirst().get().simCodeModel.getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg1,tg0,f4",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("fadd.s tg2,tg1,tg0",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isBusy());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
    this.cpu.step();
    Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
    Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
    Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s", this.faddFunctionBlock.getSimCodeModel().getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isBusy());
    
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
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f5").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f4").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f5").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f2").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f4").build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f2").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    
    InputCodeArgument argumentAdd21 = new InputCodeArgumentBuilder().hasName("rd").hasValue("f4").build();
    InputCodeArgument argumentAdd22 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("f4").build();
    InputCodeArgument argumentAdd23 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("f3").build();
    
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fsub.s"))
            .hasInstructionName("fsub.s").hasCodeLine("fsub.s f5,f4,f5")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f2,f3,f4")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f1,f2,f3")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("fadd.s"))
            .hasInstructionName("fadd.s").hasCodeLine("fadd.s f4,f4,f3")
            .hasArguments(Arrays.asList(argumentAdd21, argumentAdd22, argumentAdd23)).build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals("fsub.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals("fadd.s", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("fsub.s tg0,f4,f5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg1,f3,f4",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    Assert.assertEquals(RegisterReadinessEnum.kFree, this.unifiedRegisterFileBlock.getRegister("tg3").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg2").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg1").getReadiness());
    Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                        this.unifiedRegisterFileBlock.getRegister("tg0").getReadiness());
    
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg3,f4,f3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5",
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
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5", this.fsubFunctionBlock.getSimCodeModel().getRenamedCodeLine());
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
    Assert.assertEquals("fadd.s tg2,tg1,f3",
                        this.fpIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("fadd.s tg3,f4,f3", this.faddSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("fsub.s tg0,f4,f5", this.fsubFunctionBlock.getSimCodeModel().getRenamedCodeLine());
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
    Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("fadd.s tg2,tg1,f3", this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(12.24, (float) this.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
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
    InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName("imm").hasValue("lab3").build();
    
    InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName("imm").hasValue("labFinal").build();
    
    InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp6 = new InputCodeArgumentBuilder().hasName("imm").hasValue("lab1").build();
    
    InputCodeArgument argumentJmp7 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp8 = new InputCodeArgumentBuilder().hasName("imm").hasValue("lab2").build();
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,lab3").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp1, argumentJmp2)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,labFinal").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp3, argumentJmp4)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,lab1").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp5, argumentJmp6)).build();
    InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,lab2").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp7, argumentJmp8)).build();
    
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
    instructionMemoryBlock.setCode(instructions);
    instructionMemoryBlock.setLabels(Map.of("lab1", 1, "lab2", 2, "lab3", 3, "labFinal", 4));
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg0,lab3",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,lab2",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg0,lab3",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(3, this.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg1,lab2",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(7, this.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg3,labFinal",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals(1, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg0,lab3", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg1,lab2", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(15, this.globalHistoryRegister.getRegisterValueAsInt());
    
    this.cpu.step();
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
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
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals("jal tg2,lab1", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(12, this.branchTargetBuffer.getEntryTarget(0));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(0));
    Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(0));
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("jal tg2,lab1", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(8, this.branchTargetBuffer.getEntryTarget(12));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(12));
    Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(3));
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertEquals("jal tg3,labFinal", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(6).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(4, this.branchTargetBuffer.getEntryTarget(8));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(8));
    Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(6));
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(16, this.branchTargetBuffer.getEntryTarget(4));
    Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(8));
    Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(9));
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_wellDesignedLoop_oneMisfetch()
  {
    InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x0").build();
    InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("loopEnd").build();
    
    
    InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentJmp6 = new InputCodeArgumentBuilder().hasName("imm").hasValue("1").build();
    
    InputCodeArgument argumentJmp7 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp8 = new InputCodeArgumentBuilder().hasName("imm").hasValue("loop").build();
    
    // InputCodeModel[=\s\w\.\(\"\)]+\.hasInstructionName\(\"label\"\)[\s\w\.\(\"\)\,]+;
    
    // Program:
    //
    // loop:
    // beq x3 x0 loopEnd  # starts with values 6, 0
    // subi x3 x3 1
    // jal x0 loop        # keeps saving 3 to x0
    // loopEnd:
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("beq")).hasInstructionName("beq")
            .hasCodeLine("beq x3,x0,loopEnd").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("subi")).hasInstructionName("subi")
            .hasCodeLine("subi x3,x3,1").hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic)
            .hasArguments(Arrays.asList(argumentJmp4, argumentJmp5, argumentJmp6)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,loop").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp7, argumentJmp8)).build();
    
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    instructionMemoryBlock.setLabels(Map.of("loop", 0, "loopEnd", 3));
    
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
    Assert.assertEquals("beq x3,x0,loopEnd",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    // jal is an unconditional jump, so fetch starts from `loop:` again
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    // Decode
    Assert.assertEquals("jal tg1,loop",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    // subi got to the ALU issue window
    Assert.assertEquals("subi tg0,x3,1", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    // ROB - first two instructions are in the ROB, not ready
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq tg2,x0,loopEnd",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("jal tg3,loop",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    // There is a new instruction in the function block
    Assert.assertEquals("subi tg2,tg0,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg1,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg2,tg0,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    // beq (id 1), subi (id 2) and jal (id 3) are ready
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted()); // jal
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    // beq is ID 6
    Assert.assertEquals("beq tg0,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg4,tg2,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal tg3,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(6).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(7).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg6,tg4,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(9).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg2,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg8,tg6,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("jal tg5,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg8,tg6,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(12).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(13).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(15).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg7,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg10,tg8,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(15).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg7,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg4,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg10,tg8,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg7,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg12,tg10,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(18).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(19).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg9,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg12,tg10,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg9,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg6,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg14,tg12,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg9,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg14,tg12,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(24).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(25).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(27).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg11,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg16,tg14,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(27).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg11,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg8,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg16,tg14,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg11,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg18,tg16,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(30).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(31).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(33).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg13,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg18,tg16,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(33).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals("jal tg13,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg10,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg20,tg18,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg13,loop", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq tg12,x0,loopEnd", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg20,tg18,1", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(36).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(37).reorderFlags.isReadyToBeCommitted());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(39).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, this.branchIssueWindowBlock.getIssuedInstructions().size());
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_ifElse_executeFirstFragment()
  {
    InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x5").build();
    InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x0").build();
    InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("labelIf").build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("10").build();
    
    InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName("imm").hasValue("labelFin").build();
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("10").build();
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("beq")).hasInstructionName("beq")
            .hasCodeLine("beq x5,x0,labelIf").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("subi")).hasInstructionName("subi")
            .hasCodeLine("subi x1,x1,10").hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic)
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("jal")).hasInstructionName("jal")
            .hasCodeLine("jal x0,labelFin").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp4, argumentJmp5)).build();
    InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionFunctionModel(this.initLoader.getInstructionFunctionModel("addi")).hasInstructionName("addi")
            .hasCodeLine("addi x1,x1,10").hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic)
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
    instructionMemoryBlock.setCode(instructions);
    instructionMemoryBlock.setLabels(Map.of("labelIf", 3, "labelFin", 4));
    
    // First fetch
    this.cpu.step();
    Assert.assertEquals(8, this.instructionFetchBlock.getPc());
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    // Third instruction is not fetched - it is a second branch
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    // Second fetch
    this.cpu.step();
    Assert.assertEquals(20, this.instructionFetchBlock.getPc());
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    // Decode has the first 2 fetched (nop is filtered)
    Assert.assertEquals("beq x5,x0,labelIf",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    
    this.cpu.step();
    // So nothing is fetched
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    // Decode has just the jal, the addi was discarded
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    // beq and subi moved to their respective issue windows
    Assert.assertEquals("beq x5,x0,labelIf",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    // Last fetch was empty, so now decode is empty
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x5,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    // First instruction ready to be committed
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("addi tg2,x1,10",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg2,x1,10", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg2,x1,10", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("addi tg2,x1,10", this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(21).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(10, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValueAsInt());
  }
  
  
  ///////////////////////////////////////////////////////////
  ///                 Load/Store Tests                    ///
  ///////////////////////////////////////////////////////////
  
  @Test
  public void simulate_ifElse_executeElseFragment()
  {
    InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x0").build();
    InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("labelIf").build();
    
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("10").build();
    
    InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x0").build();
    InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName("imm").hasValue("labelFin").build();
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("10").build();
    
    InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader).hasLoader(initLoader)
            .hasInstructionName("beq").hasCodeLine("beq x3,x0,labelIf")
            .hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("subi")
            .hasCodeLine("subi x1,x1,10").hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic)
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("jal")
            .hasCodeLine("jal x0,labelFin").hasInstructionTypeEnum(InstructionTypeEnum.kJumpbranch)
            .hasArguments(Arrays.asList(argumentJmp4, argumentJmp5)).build();
    InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("addi")
            .hasCodeLine("addi x1,x1,10").hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic)
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
    instructionMemoryBlock.setCode(instructions);
    instructionMemoryBlock.setLabels(Map.of("labelIf", 3, "labelFin", 4));
    // Code:
    //
    // beq x3 x0 labelIf
    // subi x1 x1 10
    // jal x0 labelFin
    // labelIf:
    // addi x1 x1 10
    // labelFin:
    
    this.cpu.step();
    // 3 fetches
    Assert.assertEquals("beq", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
    
    this.cpu.step();
    // another 3 fetches, 2 from last one moved to decode
    Assert.assertEquals("jal", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("addi", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("beq x3,x0,labelIf",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("jal tg1,labelFin",
                        this.branchIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x1,10", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("beq x3,x0,labelIf", this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
    Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
    Assert.assertEquals("jal tg1,labelFin", this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(3).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(-10, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals(1, this.globalHistoryRegister.getRegisterValueAsInt());
  }
  
  @Test
  public void simulate_oneStore_savesIntInMemory()
  {
    InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasCodeLine("sw x3,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(store1, store2, store3))
            .build();
    
    // Just a testing instruction
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    // Code:
    // sw x3 x2 0 (store 6 in memory)
    List<InputCodeModel> instructions = Collections.singletonList(storeCodeModel);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("sw x3,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
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
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals(25, this.storeBufferBlock.getStoreBufferItem(0).getAddress());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(0).isSourceReady());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(6, loadStoreInterpreter.interpretInstruction(new SimCodeModel(loadCodeModel, -1, -1), 0)
            .getSecond(), 0.01);
  }
  
  @Test
  public void simulate_oneLoad_loadsIntInMemory()
  {
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    List<InputCodeModel> instructions = Collections.singletonList(loadCodeModel);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("lw tg0,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
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
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals("lw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(0).getAddress());
    Assert.assertTrue(this.loadBufferBlock.getLoadBufferItem(0).isDestinationReady());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.0);
  }
  
  @Test
  public void simulate_loadBypassing_successfullyLoadsFromStoreBuffer()
  {
    InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasCodeLine("sw x3,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(store1, store2, store3))
            .build();
    
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x4").build();
    InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("5").build();
    InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("subi")
            .hasCodeLine("subi x4,x4,5").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic).hasArguments(Arrays.asList(subi1, subi2, subi3))
            .build();
    
    List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel, loadCodeModel);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x4,5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("sw x3,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x4,5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw x3,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x4,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertFalse(this.loadBufferBlock.getLoadBufferItem(2).isDestinationReady());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.storeBufferBlock.getStoreBufferItem(1).isSourceReady());
    Assert.assertEquals(25, this.storeBufferBlock.getStoreBufferItem(1).getAddress());
    Assert.assertFalse(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("subi tg0,x4,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
    Assert.assertEquals("sw x3,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(25, this.loadBufferBlock.getLoadBufferItem(2).getAddress());
    Assert.assertTrue(this.loadBufferBlock.getLoadBufferItem(2).isDestinationReady());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(6, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void simulate_loadForwarding_FailsAndRerunsFromLoad()
  {
    InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("5").build();
    InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("subi")
            .hasCodeLine("subi x3,x3,5").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic).hasArguments(Arrays.asList(subi1, subi2, subi3))
            .build();
    
    InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasCodeLine("sw x3,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(store1, store2, store3))
            .build();
    
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    // Code:
    // subi x3 x3 5
    // sw x3 x2 0 - store 6 to address x2 (25)
    // lw x1 x2 0 - load from address x2
    List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel, loadCodeModel);
    instructionMemoryBlock.setCode(instructions);
    
    this.cpu.step();
    // Fetch all three instructions
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x3,5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.step();
    // both load and store got issued and are in LB and SB
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    // Both instructions have ready operands, load is in the buffer first, so it will get to EX first
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    // Load got to EX
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    // Store stays in issue window
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    
    this.cpu.step();
    // Load got to mem access
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    // Mem load should be done, load should be ready to be committed
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(2).reorderFlags.isReadyToBeCommitted());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    // Load is in load buffer
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals("lw tg2,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals("lw tg2,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(18).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals(1, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
  }
  
  @Test
  public void simulate_loadForwarding_FailsDuringMA()
  {
    InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x3").build();
    InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("5").build();
    InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("subi")
            .hasCodeLine("subi x3,x3,5").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kArithmetic).hasArguments(Arrays.asList(subi1, subi2, subi3))
            .build();
    
    InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sw")
            .hasCodeLine("sw x3,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(store1, store2, store3))
            .build();
    
    InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("0").build();
    InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("lw")
            .hasCodeLine("lw x1,0(x2)").hasDataTypeEnum(DataTypeEnum.kInt)
            .hasInstructionTypeEnum(InstructionTypeEnum.kLoadstore).hasArguments(Arrays.asList(load1, load2, load3))
            .build();
    
    // Program:
    //
    // subi x3 x3 5 # x3: 6 -> 1
    // sw x3 x2 0   # x3 (1) is stored to memory [25+0]
    // lw x1 x2 0   # x1: 0 -> 1
    List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel, loadCodeModel);
    instructionMemoryBlock.setCode(instructions);
    this.memoryAccessUnit.setDelay(3);
    this.memoryAccessUnit.setBaseDelay(3);
    
    Assert.assertEquals(0, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(25, (int) this.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt), 0.01);
    Assert.assertEquals(6, (int) this.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt), 0.01);
    
    this.cpu.step();
    Assert.assertEquals("subi", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("sw", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    
    this.cpu.step();
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(3, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("subi tg0,x3,5",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(1).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(2).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(0, this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(1).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.aluIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("subi tg0,x3,5", this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg1,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(0).reorderFlags.isReadyToBeCommitted());
    Assert.assertEquals("lw tg1,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    
    
    this.cpu.step();
    // Conflict detected (bad load), throw it out, fetch again (fetch stops being stalled)
    Assert.assertEquals("lw", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("sw tg0,0(x2)", this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    //Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    
    this.cpu.step();
    Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.decodeAndDispatchBlock.getAfterRenameCodeList().get(0).getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)",
                        this.loadStoreIssueWindowBlock.getIssuedInstructions().get(0).getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals("lw tg2,0(x2)", this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
    Assert.assertEquals("lw tg2,0(x2)", this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
    Assert.assertEquals("sw tg0,0(x2)", this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
    
    this.cpu.step();
    Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(1).reorderFlags.isReadyToBeCommitted());
    Assert.assertTrue(this.reorderBufferBlock.getRobItem(18).reorderFlags.isReadyToBeCommitted());
    
    this.cpu.step();
    Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueueSize());
    Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
    Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
    Assert.assertEquals(1, (int) this.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt), 0.01);
  }
}
