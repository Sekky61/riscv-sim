package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.arithmetic.AluIssueWindowBlock;
import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.arithmetic.FpIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.*;
import com.gradle.superscalarsim.blocks.loadstore.*;
import com.gradle.superscalarsim.builders.*;
import com.gradle.superscalarsim.code.*;
import com.gradle.superscalarsim.cpu.Cpu;
import com.gradle.superscalarsim.cpu.CpuConfiguration;
import com.gradle.superscalarsim.cpu.CpuState;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ForwardSimulationTest {
    // Use this to step the sim
    Cpu cpu;

    @Mock
    InitLoader initLoader;
    @Mock
    CodeParser codeParser;
    private SimCodeModelAllocator simCodeModelAllocator;
    private PrecedingTable precedingTable;
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
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        RegisterModel integer0 = new RegisterModelBuilder().hasName("x0").HasValue(
                0).IsConstant(true).build();
        RegisterModel integer1 = new RegisterModelBuilder().hasName("x1").HasValue(
                0).IsConstant(false).build();
        RegisterModel integer2 = new RegisterModelBuilder().hasName("x2").HasValue(
                25).IsConstant(false).build();
        RegisterModel integer3 = new RegisterModelBuilder().hasName("x3").HasValue(
                6).IsConstant(false).build();
        RegisterModel integer4 = new RegisterModelBuilder().hasName("x4").HasValue(
                -2).IsConstant(false).build();
        RegisterModel integer5 = new RegisterModelBuilder().hasName("x5").HasValue(
                0).IsConstant(false).build();
        RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName(
                "integer").hasDataType(DataTypeEnum.kInt).hasRegisterList(
                Arrays.asList(integer0, integer1, integer2, integer3, integer4,
                        integer5)).build();


        RegisterModel float1 = new RegisterModelBuilder().hasName("f1").HasValue(
                0).IsConstant(false).build();
        RegisterModel float2 = new RegisterModelBuilder().hasName("f2").HasValue(
                5.5).IsConstant(false).build();
        RegisterModel float3 = new RegisterModelBuilder().hasName("f3").HasValue(
                3.125).IsConstant(false).build();
        RegisterModel float4 = new RegisterModelBuilder().hasName("f4").HasValue(
                12.25).IsConstant(false).build();
        RegisterModel float5 = new RegisterModelBuilder().hasName("f5").HasValue(
                0.01).IsConstant(false).build();
        RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName(
                "float").hasDataType(DataTypeEnum.kFloat).hasRegisterList(
                Arrays.asList(float1, float2, float3, float4, float5)).build();

        CpuConfiguration cpuCfg = new CpuConfiguration();
        cpuCfg.robSize = 256;
        cpuCfg.lbSize = 64;
        cpuCfg.sbSize = 64;
        cpuCfg.fetchWidth = 3;
        cpuCfg.commitWidth = 4;
        cpuCfg.btbSize = 1024;
        cpuCfg.phtSize = 10;
        cpuCfg.predictorType = "2bit";
        cpuCfg.predictorDefault = "Weakly Taken";
        // cache
        cpuCfg.cacheLines = 16;
        cpuCfg.cacheAssoc = 2;
        cpuCfg.cacheLineSize = 16;
        cpuCfg.cacheReplacement = "Random";
        cpuCfg.storeBehavior = "write-back";
        cpuCfg.storeLatency = 0;
        cpuCfg.loadLatency = 0;
        cpuCfg.laneReplacementDelay = 0;
        cpuCfg.addRemainingDelay = false;
        // 3 FX: +, +, - (delay 2)
        // 3 FP: +, +, - (delay 2)
        // 1 L/S: (delay 1)
        // 2 branch: (delay 3)
        // 1 mem: (delay 1)
        cpuCfg.fUnits = new CpuConfiguration.FUnit[10];
        cpuCfg.fUnits[0] = new CpuConfiguration.FUnit(1, "FX", 2, new String[]{"+"});
        cpuCfg.fUnits[1] = new CpuConfiguration.FUnit(2, "FX", 2, new String[]{"+"});
        cpuCfg.fUnits[2] = new CpuConfiguration.FUnit(3, "FX", 2, new String[]{"-"});
        cpuCfg.fUnits[3] = new CpuConfiguration.FUnit(4, "FP", 2, new String[]{"+"});
        cpuCfg.fUnits[4] = new CpuConfiguration.FUnit(5, "FP", 2, new String[]{"+"});
        cpuCfg.fUnits[5] = new CpuConfiguration.FUnit(6, "FP", 2, new String[]{"-"});
        cpuCfg.fUnits[6] = new CpuConfiguration.FUnit(7, "L/S", 1, new String[]{});
        cpuCfg.fUnits[7] = new CpuConfiguration.FUnit(8, "Branch", 3, new String[]{});
        cpuCfg.fUnits[8] = new CpuConfiguration.FUnit(9, "Branch", 3, new String[]{});
        cpuCfg.fUnits[9] = new CpuConfiguration.FUnit(10, "Memory", 1, new String[]{});

        CpuState cpuState = new CpuState(cpuCfg);
        this.cpu = new Cpu(cpuState);

        this.codeParser = cpuState.codeParser;
        this.statisticsCounter = cpuState.statisticsCounter;
        this.precedingTable = cpuState.precedingTable;
        this.unifiedRegisterFileBlock = cpuState.unifiedRegisterFileBlock;
        this.renameMapTableBlock = cpuState.renameMapTableBlock;
        this.globalHistoryRegister = cpuState.globalHistoryRegister;
        this.gShareUnit = cpuState.gShareUnit;
        this.branchTargetBuffer = cpuState.branchTargetBuffer;
        this.memoryModel = cpuState.memoryModel;
        this.loadStoreInterpreter = cpuState.loadStoreInterpreter;
        this.simCodeModelAllocator = cpuState.simCodeModelAllocator;
        this.instructionFetchBlock = cpuState.instructionFetchBlock;
        this.decodeAndDispatchBlock = cpuState.decodeAndDispatchBlock;
        this.reorderBufferBlock = cpuState.reorderBufferBlock;
        this.issueWindowSuperBlock = cpuState.issueWindowSuperBlock;
        this.arithmeticInterpreter = cpuState.arithmeticInterpreter;
        this.branchInterpreter = cpuState.branchInterpreter;
        this.storeBufferBlock = cpuState.storeBufferBlock;
        this.loadBufferBlock = cpuState.loadBufferBlock;
        this.aluIssueWindowBlock = cpuState.aluIssueWindowBlock;
        this.fpIssueWindowBlock = cpuState.fpIssueWindowBlock;
        this.loadStoreIssueWindowBlock = cpuState.loadStoreIssueWindowBlock;
        this.branchIssueWindowBlock = cpuState.branchIssueWindowBlock;

        // FU
        this.addFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(0);
        this.addSecondFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(1);
        this.subFunctionBlock = cpuState.arithmeticFunctionUnitBlocks.get(2);
        this.faddFunctionBlock = cpuState.fpFunctionUnitBlocks.get(0);
        this.faddSecondFunctionBlock = cpuState.fpFunctionUnitBlocks.get(1);
        this.fsubFunctionBlock = cpuState.fpFunctionUnitBlocks.get(2);
        this.loadStoreFunctionUnit = cpuState.loadStoreFunctionUnits.get(0);
        this.memoryAccessUnit = cpuState.memoryAccessUnits.get(0);
        this.branchFunctionUnitBlock1 = cpuState.branchFunctionUnitBlocks.get(0);
        this.branchFunctionUnitBlock2 = cpuState.branchFunctionUnitBlocks.get(1);

        // Patch initloader, unifiedRegisterFileBlock, codeParser
        this.initLoader = cpuState.initLoader;
        this.initLoader.setRegisterFileModelList(Arrays.asList(integerFile, floatFile));
        this.initLoader.setInstructionFunctionModelList(setUpInstructions());

        // This adds the reg files, but also creates speculative registers!
        this.unifiedRegisterFileBlock.setRegisterList(new ArrayList<>());
        this.unifiedRegisterFileBlock.loadRegisters(
                this.initLoader.getRegisterFileModelList());
    }

    ///////////////////////////////////////////////////////////
    ///                 Arithmetic Tests                    ///
    ///////////////////////////////////////////////////////////

    @Test
    public void simulate_oneIntInstruction_finishesAfterSevenTicks() {
        InputCodeArgument argument1 = new InputCodeArgument("rd", "x1");
        InputCodeArgument argument2 = new InputCodeArgument("rs1", "x2");
        InputCodeArgument argument3 = new InputCodeArgument("rs2", "x3");

        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName("add")
                .hasCodeLine("add x1 x2 x3")
                .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
        List<InputCodeModel> instructions = Collections.singletonList(ins1);

        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals(1, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertTrue(
                this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("add",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getInstructionName());
        Assert.assertEquals("add t0 x2 x3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertEquals("add", this.aluIssueWindowBlock.getIssuedInstructions().get(
                0).getInstructionName());
        Assert.assertFalse(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals("add",
                this.reorderBufferBlock.getReorderQueue().peek().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isValid());
        Assert.assertFalse(this.reorderBufferBlock.getFlagsMap().get(0).isSpeculative());
        Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());

        this.cpu.step();
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().isEmpty());
        Assert.assertEquals(31, unifiedRegisterFileBlock.getRegisterValue("x1"), 0.01);
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
    }

    @Test
    public void simulate_threeIntRawInstructions_finishesAfterElevenTicks() {
        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x3").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x4").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x5").build();

        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x2").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x3").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x4").build();

        InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x2").build();
        InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x3").build();


        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName("add")
                .hasCodeLine("add x3 x4 x5")
                .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3))
                .build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName("add")
                .hasCodeLine("add x2 x3 x4")
                .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3))
                .build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName("add")
                .hasCodeLine("add x1 x2 x3")
                .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3))
                .build();
        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertTrue(
                this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("add",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getInstructionName());
        Assert.assertEquals("add",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getInstructionName());
        Assert.assertEquals("add",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getInstructionName());
        Assert.assertEquals("add t0 x4 x5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t1 t0 x4",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 t0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));

        this.cpu.step();
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertEquals(0,
                this.aluIssueWindowBlock.getIssuedInstructions().get(0).getId());
        Assert.assertEquals(1,
                this.aluIssueWindowBlock.getIssuedInstructions().get(1).getId());
        Assert.assertEquals(2,
                this.aluIssueWindowBlock.getIssuedInstructions().get(2).getId());
        Assert.assertEquals("add t0 x4 x5",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t1 t0 x4",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 t0",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        2).getRenamedCodeLine());
        Assert.assertFalse(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("add",
                this.reorderBufferBlock.getReorderQueue().peek().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());
        Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());

        this.cpu.step();
        Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("add t1 t0 x4",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 t0",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(2, this.aluIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("add t1 t0 x4",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 t0",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("add t2 t1 t0",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(1, this.aluIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("add t2 t1 t0",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("add",
                this.addFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.aluIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.addFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.addSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));

        this.cpu.step();
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(-2, this.unifiedRegisterFileBlock.getRegisterValue("x3"),
                0.01);
        Assert.assertEquals(-4, this.unifiedRegisterFileBlock.getRegisterValue("x2"),
                0.01);
        Assert.assertEquals(-6, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
    }

    @Test
    public void simulate_intOneRawConflict_usesFullPotentialOfTheProcessor() {
        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x5").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x4").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x5").build();

        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x2").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x3").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x4").build();

        InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x2").build();
        InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x3").build();

        InputCodeArgument argumentAdd21 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x4").build();
        InputCodeArgument argumentAdd22 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x4").build();
        InputCodeArgument argumentAdd23 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x3").build();


        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("sub"))
                .hasInstructionName(
                        "sub").hasCodeLine("sub x5 x4 x5").hasArguments(
                        Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName(
                        "add").hasCodeLine("add x2 x3 x4").hasArguments(
                        Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName(
                        "add").hasCodeLine("add x1 x2 x3").hasArguments(
                        Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
        InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("add"))
                .hasInstructionName(
                        "add").hasCodeLine("add x4 x4 x3").hasArguments(
                        Arrays.asList(argumentAdd21, argumentAdd22,
                                argumentAdd23)).build();
        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("sub",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("add",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("sub t0 x4 x5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t1 x3 x4",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 x3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("add t3 x4 x3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sub t0 x4 x5",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t1 x3 x4",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 x3",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        2).getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("add t2 t1 x3",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t3 x4 x3",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("sub t0 x4 x5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("add t1 x3 x4",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("add t2 t1 x3",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("add t3 x4 x3",
                this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("sub t0 x4 x5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("add t1 x3 x4",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals("add t3 x4 x3",
                this.addSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("add t2 t1 x3",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(4, this.unifiedRegisterFileBlock.getRegisterValue("t1"),
                0.01);
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertEquals("add t2 t1 x3",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(-2, this.unifiedRegisterFileBlock.getRegisterValue("x5"),
                0.01);
        Assert.assertEquals(4, this.unifiedRegisterFileBlock.getRegisterValue("x2"),
                0.01);
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(4, this.unifiedRegisterFileBlock.getRegisterValue("x2"),
                0.01);
        Assert.assertEquals(10, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
    }

    @Test
    public void simulate_oneFloatInstruction_finishesAfterSevenTicks() {
        InputCodeArgument argument1 = new InputCodeArgument("rd", "f1");
        InputCodeArgument argument2 = new InputCodeArgument("rs1", "f2");
        InputCodeArgument argument3 = new InputCodeArgument("rs2", "f3");

        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f1 f2 f3").hasArguments(
                        Arrays.asList(argument1, argument2, argument3)).build();
        List<InputCodeModel> instructions = Arrays.asList(ins1);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals(1, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertTrue(
                this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("fadd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getInstructionName());
        Assert.assertEquals("fadd t0 f2 f3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertEquals("fadd", this.fpIssueWindowBlock.getIssuedInstructions().get(
                0).getInstructionName());
        Assert.assertFalse(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals("fadd",
                this.reorderBufferBlock.getReorderQueue().peek().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isValid());
        Assert.assertFalse(this.reorderBufferBlock.getFlagsMap().get(0).isSpeculative());
        Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());

        this.cpu.step();
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().isEmpty());
        Assert.assertEquals(8.625, unifiedRegisterFileBlock.getRegisterValue("f1"),
                0.001);
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
    }

    @Test
    public void simulate_threeFloatRawInstructions_finishesAfterElevenTicks() {
        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f3").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f4").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f5").build();

        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f2").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f3").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f4").build();

        InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f1").build();
        InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f2").build();
        InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f3").build();


        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f3 f4 f5").hasArguments(
                        Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f2 f3 f4").hasArguments(
                        Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f1 f2 f3").hasArguments(
                        Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertTrue(
                this.decodeAndDispatchBlock.getBeforeRenameCodeList().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("fadd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getInstructionName());
        Assert.assertEquals("fadd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getInstructionName());
        Assert.assertEquals("fadd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getInstructionName());
        Assert.assertEquals("fadd t0 f4 f5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t1 t0 f4",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 t0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));

        this.cpu.step();
        Assert.assertTrue(this.decodeAndDispatchBlock.getAfterRenameCodeList().isEmpty());
        Assert.assertEquals(0,
                this.fpIssueWindowBlock.getIssuedInstructions().get(0).getId());
        Assert.assertEquals(1,
                this.fpIssueWindowBlock.getIssuedInstructions().get(1).getId());
        Assert.assertEquals(2,
                this.fpIssueWindowBlock.getIssuedInstructions().get(2).getId());
        Assert.assertEquals("fadd t0 f4 f5",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t1 t0 f4",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 t0",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        2).getRenamedCodeLine());
        Assert.assertFalse(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("fadd",
                this.reorderBufferBlock.getReorderQueue().peek().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());
        Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());

        this.cpu.step();
        Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("fadd t1 t0 f4",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 t0",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(2, this.fpIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("fadd t1 t0 f4",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 t0",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(0).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("fadd t2 t1 t0",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertEquals(1, this.fpIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("fadd t2 t1 t0",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(1).isBusy());
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertFalse(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("fadd",
                this.faddFunctionBlock.getSimCodeModel().getInstructionName());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertTrue(this.reorderBufferBlock.getFlagsMap().get(2).isBusy());

        this.cpu.step();
        Assert.assertTrue(this.fpIssueWindowBlock.getIssuedInstructions().isEmpty());
        Assert.assertTrue(this.faddFunctionBlock.isFunctionUnitEmpty());
        Assert.assertTrue(this.faddSecondFunctionBlock.isFunctionUnitEmpty());
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));

        this.cpu.step();
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(12.26, this.unifiedRegisterFileBlock.getRegisterValue("f3"),
                0.01);
        Assert.assertEquals(24.51, this.unifiedRegisterFileBlock.getRegisterValue("f2"),
                0.01);
        Assert.assertEquals(36.77, this.unifiedRegisterFileBlock.getRegisterValue("f1"),
                0.01);
    }

    @Test
    public void simulate_floatOneRawConflict_usesFullPotentialOfTheProcessor() {
        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f5").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f4").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f5").build();

        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f2").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f3").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f4").build();

        InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f1").build();
        InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f2").build();
        InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f3").build();

        InputCodeArgument argumentAdd21 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("f4").build();
        InputCodeArgument argumentAdd22 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("f4").build();
        InputCodeArgument argumentAdd23 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("f3").build();


        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fsub"))
                .hasInstructionName(
                        "fsub").hasCodeLine("fsub f5 f4 f5").hasArguments(
                        Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f2 f3 f4").hasArguments(
                        Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f1 f2 f3").hasArguments(
                        Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
        InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("fadd"))
                .hasInstructionName(
                        "fadd").hasCodeLine("fadd f4 f4 f3").hasArguments(
                        Arrays.asList(argumentAdd21, argumentAdd22,
                                argumentAdd23)).build();
        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, ins4);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("fsub",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("fadd",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("fsub t0 f4 f5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t1 f3 f4",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 f3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("fadd t3 f4 f3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fsub t0 f4 f5",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t1 f3 f4",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 f3",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        2).getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("fadd t2 t1 f3",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t3 f4 f3",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("fsub t0 f4 f5",
                this.fsubFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("fadd t1 f3 f4",
                this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals("fadd t2 t1 f3",
                this.fpIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("fadd t3 f4 f3",
                this.faddSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("fsub t0 f4 f5",
                this.fsubFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("fadd t1 f3 f4",
                this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals("fadd t3 f4 f3",
                this.faddSecondFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("fadd t2 t1 f3",
                this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(15.375, this.unifiedRegisterFileBlock.getRegisterValue("t1"),
                0.001);
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertEquals("fadd t2 t1 f3",
                this.faddFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(12.24, this.unifiedRegisterFileBlock.getRegisterValue("f5"),
                0.001);
        Assert.assertEquals(15.375, this.unifiedRegisterFileBlock.getRegisterValue("f2"),
                0.001);
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kAllocated,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t3"));
        Assert.assertEquals(RegisterReadinessEnum.kExecuted,
                this.unifiedRegisterFileBlock.getReadyMap().get("t2"));
        Assert.assertEquals(RegisterReadinessEnum.kAssigned,
                this.unifiedRegisterFileBlock.getReadyMap().get("t1"));
        Assert.assertEquals(RegisterReadinessEnum.kFree,
                this.unifiedRegisterFileBlock.getReadyMap().get("t0"));

        this.cpu.step();
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(15.375, this.unifiedRegisterFileBlock.getRegisterValue("f2"),
                0.001);
        Assert.assertEquals(18.5, this.unifiedRegisterFileBlock.getRegisterValue("f1"),
                0.01);
    }

    ///////////////////////////////////////////////////////////
    ///                 Branch Tests                        ///
    ///////////////////////////////////////////////////////////

    @Test
    public void simulate_jumpFromLabelToLabel_recordToBTB() {
        InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("lab3").build();

        InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("labFinal").build();

        InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp6 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("lab1").build();

        InputCodeArgument argumentJmp7 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp8 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("lab2").build();

        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 lab3").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp1, argumentJmp2)).build();
        InputCodeModel label1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("lab1").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).hasArguments(
                        Arrays.asList(argumentJmp3, argumentJmp4)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 labFinal").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp3, argumentJmp4)).build();
        InputCodeModel label2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("lab2").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).hasArguments(
                        Arrays.asList(argumentJmp3, argumentJmp4)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 lab1").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp5, argumentJmp6)).build();
        InputCodeModel label3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("lab3").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).hasArguments(
                        Arrays.asList(argumentJmp3, argumentJmp4)).build();
        InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 lab2").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp7, argumentJmp8)).build();
        InputCodeModel label4 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("labFinal").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).hasArguments(
                        Arrays.asList(argumentJmp3, argumentJmp4)).build();

        List<InputCodeModel> instructions = Arrays.asList(ins1, label1, ins2, label2,
                ins3, label3, ins4, label4);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t0 lab3",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(1, this.globalHistoryRegister.getRegisterValueAsInt());

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t1 lab2",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(1,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t0 lab3",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(3, this.globalHistoryRegister.getRegisterValueAsInt());

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t2 lab1",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(1,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t1 lab2",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t0 lab3",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(7, this.globalHistoryRegister.getRegisterValueAsInt());

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t3 labFinal",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(1,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t2 lab1",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t0 lab3",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("jal t1 lab2",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(15, this.globalHistoryRegister.getRegisterValueAsInt());

        this.cpu.step();
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals(2,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t2 lab1",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t3 labFinal",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("jal t0 lab3",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("jal t1 lab2",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(4, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(1,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t3 labFinal",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t2 lab1",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("jal t1 lab2",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(3, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals("jal t2 lab1",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("jal t3 labFinal",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertEquals(6, this.branchTargetBuffer.getEntryTarget(0));
        Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(0));
        Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(0));

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals("jal t2 lab1",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("jal t3 labFinal",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(4, this.branchTargetBuffer.getEntryTarget(6));
        Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(6));
        Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(3));

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertEquals("jal t3 labFinal",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(6).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(9).isReadyToBeCommitted());
        Assert.assertEquals(2, this.branchTargetBuffer.getEntryTarget(4));
        Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(4));
        Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(6));

        this.cpu.step();
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(8, this.branchTargetBuffer.getEntryTarget(2));
        Assert.assertTrue(this.branchTargetBuffer.isEntryUnconditional(2));
        Assert.assertEquals(-1, this.globalHistoryRegister.getHistoryValueAsInt(9));
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);
    }

    @Test
    public void simulate_wellDesignedLoop_oneMisfetch() {
        InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x3").build();
        InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x0").build();
        InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("loopEnd").build();


        InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x3").build();
        InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x3").build();
        InputCodeArgument argumentJmp6 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("1").build();

        InputCodeArgument argumentJmp7 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp8 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("loop").build();

        // Program:
        //
        // loop:
        // beq x3 x0 loopEnd  # starts with values 6, 0
        // subi x3 x3 1
        // jal x0 loop        # keeps saving 3 to x0
        // loopEnd:
        InputCodeModel label1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("loop").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).build();
        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("beq"))
                .hasInstructionName(
                        "beq").hasCodeLine("beq x3 x0 loopEnd").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("subi"))
                .hasInstructionName(
                        "subi").hasCodeLine("subi x3 x3 1").hasInstructionTypeEnum(
                        InstructionTypeEnum.kArithmetic).hasArguments(
                        Arrays.asList(argumentJmp4, argumentJmp5, argumentJmp6)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 loop").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp7, argumentJmp8)).build();
        InputCodeModel label2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("loopEnd").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).build();

        List<InputCodeModel> instructions = Arrays.asList(label1, ins1, ins2, ins3,
                label2);
        codeParser.setParsedCode(instructions);

        // First fetch (3)
        this.cpu.step();
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        // Prediction is not to take the branch (default value of predictor)
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);
        Assert.assertEquals(6, this.unifiedRegisterFileBlock.getRegisterValue("x3"),
                0.01);

        this.cpu.step();
        // Second fetch
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        // First decode
        Assert.assertEquals("beq x3 x0 loopEnd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 1",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        // jal is an unconditional jump, so fetch starts from `loop:` again
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        // Decode
        Assert.assertEquals("jal t1 loop",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 loopEnd",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        // subi got to the ALU issue window
        Assert.assertEquals("subi t0 x3 1",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t2 t0 1",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("jal t1 loop",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("jal t3 loop",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t2 t0 1",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t1 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("beq t2 x0 loopEnd",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t4 t2 1",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("jal t3 loop",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("jal t1 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        // There is a new instruction in the function block
        Assert.assertEquals("subi t2 t0 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("jal t1 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t2 t0 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        // beq (id 1), subi (id 2) and jal (id 3) are ready
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("jal t3 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t4 t2 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("jal t3 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t0 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t4 t2 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("jal t3 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t2 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t6 t4 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(6).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(7).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(9).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t5 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t2 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t6 t4 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(9).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t5 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t2 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t8 t6 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x0"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("jal t5 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t4 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t8 t6 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(12).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(13).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(15).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t7 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t4 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t10 t8 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(15).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t7 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t4 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t10 t8 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t7 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t6 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t12 t10 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(18).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(19).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(21).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t9 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t6 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t12 t10 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(21).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t9 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t6 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t14 t12 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t9 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t8 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t14 t12 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(24).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(25).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(27).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t11 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t8 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t16 t14 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(27).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t11 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t8 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t16 t14 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t11 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t10 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t18 t16 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(30).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(31).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(33).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t13 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t10 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t18 t16 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(33).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals("jal t13 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t10 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t20 t18 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t13 loop",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq t12 x0 loopEnd",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t20 t18 1",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(36).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(37).isReadyToBeCommitted());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(39).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.aluIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals(0,
                this.branchIssueWindowBlock.getIssuedInstructions().size());
        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x3"),
                0.01);
    }

    @Test
    public void simulate_ifElse_executeFirstFragment() {
        InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x5").build();
        InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x0").build();
        InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("labelIf").build();

        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x1").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("10").build();

        InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("labelFin").build();

        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x1").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("10").build();

        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("beq"))
                .hasInstructionName(
                        "beq").hasCodeLine("beq x5 x0 labelIf").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("subi"))
                .hasInstructionName(
                        "subi").hasCodeLine("subi x1 x1 10").hasInstructionTypeEnum(
                        InstructionTypeEnum.kArithmetic).hasArguments(
                        Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("jal"))
                .hasInstructionName(
                        "jal").hasCodeLine("jal x0 labelFin").hasInstructionTypeEnum(
                        InstructionTypeEnum.kJumpbranch).hasArguments(
                        Arrays.asList(argumentJmp4, argumentJmp5)).build();
        InputCodeModel label1 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("labelIf").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).build();
        InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("addi"))
                .hasInstructionName(
                        "addi").hasCodeLine("addi x1 x1 10").hasInstructionTypeEnum(
                        InstructionTypeEnum.kArithmetic).hasArguments(
                        Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
        InputCodeModel label2 = new InputCodeModelBuilder().hasLoader(initLoader)
                .hasInstructionFunctionModel(
                        this.initLoader.getInstructionFunctionModel("label"))
                .hasInstructionName(
                        "label").hasCodeLine("labelFin").hasInstructionTypeEnum(
                        InstructionTypeEnum.kLabel).build();

        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, label1, ins4,
                label2);
        codeParser.setParsedCode(instructions);

        // First fetch
        this.cpu.step();
        Assert.assertEquals(2, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        // Third instruction is not fetched - it is a second branch
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

        // Second fetch
        this.cpu.step();
        Assert.assertEquals(5, this.instructionFetchBlock.getPcCounter());
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("addi",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        // Decode has the first 2 fetched (nop is filtered)
        Assert.assertEquals("beq x5 x0 labelIf",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());

        this.cpu.step();
        // PC started as 5, but jal is unconditional jump, it gets evaluated in decode, sets PC to 6
        Assert.assertEquals(6, this.instructionFetchBlock.getPcCounter());
        // So nothing is fetched
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        // Decode has just the jal, the addi was discarded
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t1 labelFin",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        // beq and subi moved to their respective issue windows
        Assert.assertEquals("beq x5 x0 labelIf",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        // Last fetch was empty, so now decode is empty
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t1 labelFin",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq x5 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x5 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x5 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());

        this.cpu.step();
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
        // First instruction ready to be committed
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertNull(this.branchFunctionUnitBlock2.getSimCodeModel());
        Assert.assertTrue(this.reorderBufferBlock.getReorderQueue().isEmpty());
        Assert.assertEquals("addi",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

        this.cpu.step();
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("addi t2 x1 10",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("addi t2 x1 10",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("addi t2 x1 10",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("addi t2 x1 10",
                this.addFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(21).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(10, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
        Assert.assertEquals(0, this.globalHistoryRegister.getRegisterValueAsInt());
    }

    @Test
    public void simulate_ifElse_executeElseFragment() {
        InputCodeArgument argumentJmp1 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x3").build();
        InputCodeArgument argumentJmp2 = new InputCodeArgumentBuilder().hasName(
                "rs2").hasValue("x0").build();
        InputCodeArgument argumentJmp3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("labelIf").build();


        InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x1").build();
        InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("10").build();

        InputCodeArgument argumentJmp4 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x0").build();
        InputCodeArgument argumentJmp5 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("labelFin").build();

        InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName(
                "rd").hasValue("x1").build();
        InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName(
                "rs1").hasValue("x1").build();
        InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName(
                "imm").hasValue("10").build();

        InputCodeModel ins1 = new InputCodeModelBuilder().hasLoader(initLoader).hasLoader(initLoader).hasInstructionName(
                "beq").hasCodeLine("beq x3 x0 labelIf").hasInstructionTypeEnum(
                InstructionTypeEnum.kJumpbranch).hasArguments(
                Arrays.asList(argumentJmp1, argumentJmp2, argumentJmp3)).build();
        InputCodeModel ins2 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "subi").hasCodeLine("subi x1 x1 10").hasInstructionTypeEnum(
                InstructionTypeEnum.kArithmetic).hasArguments(
                Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
        InputCodeModel ins3 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "jal").hasCodeLine("jal x0 labelFin").hasInstructionTypeEnum(
                InstructionTypeEnum.kJumpbranch).hasArguments(
                Arrays.asList(argumentJmp4, argumentJmp5)).build();
        InputCodeModel label1 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "label").hasCodeLine("labelIf").hasInstructionTypeEnum(
                InstructionTypeEnum.kLabel).build();
        InputCodeModel ins4 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "addi").hasCodeLine("addi x1 x1 10").hasInstructionTypeEnum(
                InstructionTypeEnum.kArithmetic).hasArguments(
                Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
        InputCodeModel label2 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "label").hasCodeLine("labelFin").hasInstructionTypeEnum(
                InstructionTypeEnum.kLabel).build();

        List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3, label1, ins4,
                label2);
        codeParser.setParsedCode(instructions);
        // Code:
        //
        // beq x3 x0 labelIf
        // subi x1 x1 10
        // jal x0 labelFin
        // labelIf
        // addi x1 x1 10
        // labelFin

        this.cpu.step();
        // 3 fetches
        Assert.assertEquals("beq",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());


        this.cpu.step();
        // another 3 fetches, 2 from last one moved to decode
        Assert.assertEquals("jal",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("label",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("addi",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals("beq x3 x0 labelIf",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(1,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t1 labelFin",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 labelIf",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("jal t1 labelFin",
                this.branchIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x1 10",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("beq x3 x0 labelIf",
                this.branchFunctionUnitBlock1.getSimCodeModel().getRenamedCodeLine());
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());

        this.cpu.step();
        Assert.assertNull(this.subFunctionBlock.getSimCodeModel());
        Assert.assertNull(this.branchFunctionUnitBlock1.getSimCodeModel());
        Assert.assertEquals("jal t1 labelFin",
                this.branchFunctionUnitBlock2.getSimCodeModel().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(3).isReadyToBeCommitted());
        Assert.assertEquals(-10, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);

        this.cpu.step();
        Assert.assertEquals(1, this.globalHistoryRegister.getRegisterValueAsInt());
    }


    ///////////////////////////////////////////////////////////
    ///                 Load/Store Tests                    ///
    ///////////////////////////////////////////////////////////

    @Test
    public void simulate_oneStore_savesIntInMemory() {
        InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue(
                "x3").build();
        InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "sw").hasCodeLine("sw x3 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(store1, store2, store3)).build();

        // Just a testing instruction
        InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x1").build();
        InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "lw").hasCodeLine("lw x1 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(load1, load2, load3)).build();

        // Code:
        // sw x3 x2 0 (store 6 in memory)
        List<InputCodeModel> instructions = Collections.singletonList(storeCodeModel);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("sw",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals("sw x3 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("sw x3 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals(-1, this.storeBufferBlock.getStoreMap().get(0).getAddress());
        Assert.assertTrue(this.storeBufferBlock.getStoreMap().get(0).isSourceReady());
        Assert.assertEquals("sw x3 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("sw x3 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(-1, this.storeBufferBlock.getStoreMap().get(0).getAddress());
        Assert.assertTrue(this.storeBufferBlock.getStoreMap().get(0).isSourceReady());

        this.cpu.step();
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
        Assert.assertEquals(25, this.storeBufferBlock.getStoreMap().get(0).getAddress());
        Assert.assertTrue(this.storeBufferBlock.getStoreMap().get(0).isSourceReady());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(6, loadStoreInterpreter.interpretInstruction(
                new SimCodeModel(loadCodeModel, -1, -1), 0).getSecond(), 0.01);
    }


    @Test
    public void simulate_oneLoad_loadsIntInMemory() {
        InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x1").build();
        InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "lw").hasCodeLine("lw x1 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(load1, load2, load3)).build();

        List<InputCodeModel> instructions = Collections.singletonList(loadCodeModel);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals("lw t0 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t0 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals(-1, this.loadBufferBlock.getLoadMap().get(0).getAddress());
        Assert.assertFalse(this.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
        Assert.assertEquals("lw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t0 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(-1, this.loadBufferBlock.getLoadMap().get(0).getAddress());
        Assert.assertFalse(this.loadBufferBlock.getLoadMap().get(0).isDestinationReady());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
        Assert.assertEquals("lw t0 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(25, this.loadBufferBlock.getLoadMap().get(0).getAddress());
        Assert.assertFalse(this.loadBufferBlock.getLoadMap().get(0).isDestinationReady());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
        Assert.assertEquals(25, this.loadBufferBlock.getLoadMap().get(0).getAddress());
        Assert.assertTrue(this.loadBufferBlock.getLoadMap().get(0).isDestinationReady());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertTrue(this.unifiedRegisterFileBlock.getRegisterValue("x1") == 0);
    }

    @Test
    public void simulate_loadBypassing_successfullyLoadsFromStoreBuffer() {
        InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue(
                "x3").build();
        InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "sw").hasCodeLine("sw x3 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(store1, store2, store3)).build();

        InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x1").build();
        InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "lw").hasCodeLine("lw x1 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(load1, load2, load3)).build();

        InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x4").build();
        InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x4").build();
        InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "5").build();
        InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "subi").hasCodeLine("subi x4 x4 5").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kArithmetic).hasArguments(
                Arrays.asList(subi1, subi2, subi3)).build();

        List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel,
                loadCodeModel);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("sw",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(3,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("subi t0 x4 5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sw x3 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("sw x3 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("sw x3 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x4 5",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw x3 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sw x3 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x4 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("sw x3 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertFalse(this.loadBufferBlock.getLoadMap().get(2).isDestinationReady());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());
        Assert.assertTrue(this.storeBufferBlock.getStoreMap().get(1).isSourceReady());
        Assert.assertEquals(25, this.storeBufferBlock.getStoreMap().get(1).getAddress());
        Assert.assertFalse(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals("subi t0 x4 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertNull(this.loadStoreFunctionUnit.getSimCodeModel());
        Assert.assertEquals("sw x3 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals(25, this.loadBufferBlock.getLoadMap().get(2).getAddress());
        Assert.assertTrue(this.loadBufferBlock.getLoadMap().get(2).isDestinationReady());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(6, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
    }

    @Test
    public void simulate_loadForwarding_FailsAndRerunsFromLoad() {
        InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x3").build();
        InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x3").build();
        InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "5").build();
        InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "subi").hasCodeLine("subi x3 x3 5").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kArithmetic).hasArguments(
                Arrays.asList(subi1, subi2, subi3)).build();

        InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue(
                "x3").build();
        InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "sw").hasCodeLine("sw x3 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(store1, store2, store3)).build();

        InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x1").build();
        InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "lw").hasCodeLine("lw x1 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(load1, load2, load3)).build();

        // Code:
        // subi x3 x3 5
        // sw x3 x2 0 - store 6 to address x2 (25)
        // lw x1 x2 0 - load from address x2
        List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel,
                loadCodeModel);
        codeParser.setParsedCode(instructions);

        this.cpu.step();
        // Fetch all three instructions
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("sw",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(3,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("subi t0 x3 5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());

        this.cpu.step();
        // both load and store got issued and are in LB and SB
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        // Both instructions have ready operands, load is in the buffer first, so it will get to EX first
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        // Load got to EX
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        // Store stays in issue window
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());

        this.cpu.step();
        // Load got to mem access
        Assert.assertEquals("lw t1 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        // Mem load should be done, load should be ready to be committed
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(2).isReadyToBeCommitted());
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
        // Load is in load buffer
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());

        this.cpu.step();
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t2 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t2 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals("lw t2 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());

        this.cpu.step();
        Assert.assertEquals("lw t2 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(18).isReadyToBeCommitted());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());

        this.cpu.step();
        Assert.assertEquals(1, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
        Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());
    }

    @Test
    public void simulate_loadForwarding_FailsDuringMA() {
        InputCodeArgument subi1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x3").build();
        InputCodeArgument subi2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x3").build();
        InputCodeArgument subi3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "5").build();
        InputCodeModel subiCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "subi").hasCodeLine("subi x3 x3 5").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kArithmetic).hasArguments(
                Arrays.asList(subi1, subi2, subi3)).build();

        InputCodeArgument store1 = new InputCodeArgumentBuilder().hasName("rs2").hasValue(
                "x3").build();
        InputCodeArgument store2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument store3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel storeCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "sw").hasCodeLine("sw x3 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(store1, store2, store3)).build();

        InputCodeArgument load1 = new InputCodeArgumentBuilder().hasName("rd").hasValue(
                "x1").build();
        InputCodeArgument load2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue(
                "x2").build();
        InputCodeArgument load3 = new InputCodeArgumentBuilder().hasName("imm").hasValue(
                "0").build();
        InputCodeModel loadCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
                "lw").hasCodeLine("lw x1 x2 0").hasDataTypeEnum(
                DataTypeEnum.kInt).hasInstructionTypeEnum(
                InstructionTypeEnum.kLoadstore).hasArguments(
                Arrays.asList(load1, load2, load3)).build();

        // Program:
        //
        // subi x3 x3 5 # x3: 6 -> 1
        // sw x3 x2 0   # x3 (1) is stored to memory [25+0]
        // lw x1 x2 0   # x1: 0 -> 1
        List<InputCodeModel> instructions = Arrays.asList(subiCodeModel, storeCodeModel,
                loadCodeModel);
        codeParser.setParsedCode(instructions);
        this.memoryAccessUnit.setDelay(3);
        this.memoryAccessUnit.setBaseDelay(3);

        Assert.assertEquals(0, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
        Assert.assertEquals(25, this.unifiedRegisterFileBlock.getRegisterValue("x2"),
                0.01);
        Assert.assertEquals(6, this.unifiedRegisterFileBlock.getRegisterValue("x3"),
                0.01);

        this.cpu.step();
        Assert.assertEquals("subi",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals("sw",
                this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());

        this.cpu.step();
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(3,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("subi t0 x3 5",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        2).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("nop",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(0,
                this.decodeAndDispatchBlock.getAfterRenameCodeList().size());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        1).getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.aluIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("lw t1 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("subi t0 x3 5",
                this.subFunctionBlock.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t1 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(0).isReadyToBeCommitted());
        Assert.assertEquals("lw t1 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());


        this.cpu.step();
        // Conflict detected (bad load), throw it out, fetch again (fetch stops being stalled)
        Assert.assertEquals("lw",
                this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("sw t0 x2 0",
                this.storeBufferBlock.getStoreQueueFirst().getRenamedCodeLine());
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        //Assert.assertNull(this.memoryAccessUnit.getSimCodeModel());

        this.cpu.step();
        Assert.assertEquals(1, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t2 x2 0",
                this.decodeAndDispatchBlock.getAfterRenameCodeList().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t2 x2 0",
                this.loadStoreIssueWindowBlock.getIssuedInstructions().get(
                        0).getRenamedCodeLine());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals("lw t2 x2 0",
                this.loadStoreFunctionUnit.getSimCodeModel().getRenamedCodeLine());
        Assert.assertEquals("lw t2 x2 0",
                this.loadBufferBlock.getLoadQueueFirst().getRenamedCodeLine());
        Assert.assertEquals("sw t0 x2 0",
                this.memoryAccessUnit.getSimCodeModel().getRenamedCodeLine());

        this.cpu.step();
        Assert.assertEquals(2, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(1, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.storeBufferBlock.getQueueSize());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(1).isReadyToBeCommitted());
        Assert.assertTrue(
                this.reorderBufferBlock.getFlagsMap().get(18).isReadyToBeCommitted());

        this.cpu.step();
        Assert.assertEquals(0, this.reorderBufferBlock.getReorderQueue().size());
        Assert.assertEquals(0, this.loadBufferBlock.getQueueSize());
        Assert.assertEquals(0, this.storeBufferBlock.getQueueSize());
        Assert.assertEquals(1, this.unifiedRegisterFileBlock.getRegisterValue("x1"),
                0.01);
    }

    private List<InstructionFunctionModel> setUpInstructions() {
        InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName(
                "add").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1+rs2;").hasSyntax(
                "add rd rs1 rs2").build();

        InstructionFunctionModel instructionSub = new InstructionFunctionModelBuilder().hasName(
                "sub").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1-rs2;").hasSyntax(
                "sub rd rs1 rs2").build();

        InstructionFunctionModel instructionAddi = new InstructionFunctionModelBuilder().hasName(
                "addi").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1+imm;").hasSyntax(
                "addi rd rs1 imm").build();

        InstructionFunctionModel instructionSubi = new InstructionFunctionModelBuilder().hasName(
                "subi").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1-imm;").hasSyntax(
                "subi rd rs1 imm").build();

        InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName(
                "fadd").hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(
                DataTypeEnum.kFloat).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1+rs2;").hasSyntax(
                "add rd rs1 rs2").build();

        InstructionFunctionModel instructionFSub = new InstructionFunctionModelBuilder().hasName(
                "fsub").hasInputDataType(DataTypeEnum.kFloat).hasOutputDataType(
                DataTypeEnum.kFloat).hasType(
                InstructionTypeEnum.kArithmetic).isInterpretedAs("rd=rs1-rs2;").hasSyntax(
                "sub rd rs1 rs2").build();

        InstructionFunctionModel instructionJal = new InstructionFunctionModelBuilder().hasName(
                "jal").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kJumpbranch).isInterpretedAs("jump:imm").hasSyntax(
                "jal rd imm").build();

        InstructionFunctionModel instructionBeq = new InstructionFunctionModelBuilder().hasName(
                "beq").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kJumpbranch).isInterpretedAs(
                "signed:rs1 == rs2").hasSyntax("beq rs1 rs2 imm").build();

        InstructionFunctionModel instructionLoadWord = new InstructionFunctionModelBuilder().hasName(
                "lw").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kLoadstore).isInterpretedAs(
                "load word:signed rd rs1 imm").hasSyntax("lw rd rs1 imm").build();

        InstructionFunctionModel instructionStoreWord = new InstructionFunctionModelBuilder().hasName(
                "sw").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(
                DataTypeEnum.kInt).hasType(
                InstructionTypeEnum.kLoadstore).isInterpretedAs(
                "store word rs2 rs1 imm").hasSyntax("sw rs2 rs1 imm").build();

        return Arrays.asList(instructionAdd, instructionSub, instructionFAdd,
                instructionFSub, instructionJal, instructionBeq, instructionSubi,
                instructionAddi, instructionLoadWord, instructionStoreWord);
    }
}
