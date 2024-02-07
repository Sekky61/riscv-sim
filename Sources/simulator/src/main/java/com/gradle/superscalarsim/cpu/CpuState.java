/**
 * @file CpuState.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief The complete state of the CPU. Serializable for saving/loading.
 * @date 26 Sep      2023 10:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023 Michal Majer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gradle.superscalarsim.cpu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.*;
import com.gradle.superscalarsim.blocks.loadstore.*;
import com.gradle.superscalarsim.code.*;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.factories.InputCodeModelFactory;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.factories.SimCodeModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.managers.ManagerRegistry;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import com.gradle.superscalarsim.models.instruction.InstructionFunctionModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import com.gradle.superscalarsim.serialization.Serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @class CpuState
 * @brief The state of the CPU. Assumes the config is valid.
 * @details The order of the fields is important for serialization.
 * This class sets up the RISC-V execution environment interface (EEI), which is responsible for the ISA (supported instructions).
 * This is a user-level, bare-metal interface simulator.
 * The simulation will stop when the program returns from the entry function, or once the PC runs past the code and all instructions are retired.
 * There is also timeout for cycles.
 */
public class CpuState implements Serializable
{
  /**
   * The manager registry is used to keep track of all relevant models in the CPU.
   */
  public ManagerRegistry managerRegistry;
  
  public int tick;
  
  public InstructionMemoryBlock instructionMemoryBlock;
  
  // Housekeeping
  
  public SimulationStatistics statistics;
  
  // Branch prediction
  
  public BranchTargetBuffer branchTargetBuffer;
  public GlobalHistoryRegister globalHistoryRegister;
  public PatternHistoryTable patternHistoryTable;
  public GShareUnit gShareUnit;
  
  // Blocks
  
  public UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  
  public RenameMapTableBlock renameMapTableBlock;
  public InstructionFetchBlock instructionFetchBlock;
  public DecodeAndDispatchBlock decodeAndDispatchBlock;
  
  public Cache cache;
  public MemoryModel memoryModel;
  public CodeLoadStoreInterpreter loadStoreInterpreter;
  public StoreBufferBlock storeBufferBlock;
  public LoadBufferBlock loadBufferBlock;
  
  // ALU
  public CodeArithmeticInterpreter arithmeticInterpreter;
  public List<ArithmeticFunctionUnitBlock> arithmeticFunctionUnitBlocks;
  public List<ArithmeticFunctionUnitBlock> fpFunctionUnitBlocks;
  public IssueWindowBlock aluIssueWindowBlock;
  public IssueWindowBlock fpIssueWindowBlock;
  
  public CodeBranchInterpreter branchInterpreter;
  public List<BranchFunctionUnitBlock> branchFunctionUnitBlocks;
  public IssueWindowBlock branchIssueWindowBlock;
  
  // Load/Store
  public List<LoadStoreFunctionUnit> loadStoreFunctionUnits;
  public IssueWindowBlock loadStoreIssueWindowBlock;
  
  // Memory
  public List<MemoryAccessUnit> memoryAccessUnits;
  
  public SimulatedMemory simulatedMemory;
  
  public IssueWindowSuperBlock issueWindowSuperBlock;
  
  // ROB "without the state"
  public ReorderBufferBlock reorderBufferBlock;
  
  /**
   * @brief Debug log for debugging/presentation purposes
   */
  public DebugLog debugLog;
  
  public CpuState()
  {
    // Empty constructor for serialization
  }
  
  public CpuState(SimulationConfig config, InitLoader initLoader)
  {
    this.initState(config, initLoader);
  }
  
  /**
   * @brief Initialize the CPU state - given the configuration.
   */
  public void initState(SimulationConfig config, InitLoader initLoader)
  {
    this.tick            = 0;
    this.managerRegistry = new ManagerRegistry();
    
    // Factories (for tracking instances of models)
    InputCodeModelFactory inputCodeModelFactory = new InputCodeModelFactory(managerRegistry.inputCodeManager);
    SimCodeModelFactory   simCodeModelFactory   = new SimCodeModelFactory(managerRegistry.simCodeManager);
    RegisterModelFactory  registerModelFactory  = new RegisterModelFactory(managerRegistry.registerModelManager);
    
    // Hack to load all function models and registers to manager
    initLoader.getInstructionFunctionModels()
            .forEach((name, model) -> managerRegistry.instructionFunctionManager.addInstance(model));
    initLoader.getRegisterFile().getRegisterMap(false)
            .forEach((name, model) -> managerRegistry.registerModelManager.addInstance(model));
    
    this.statistics      = new SimulationStatistics(-1, config.cpuConfig.coreClockFrequency, config.cpuConfig.fUnits);
    this.simulatedMemory = new SimulatedMemory(config.cpuConfig.storeLatency, config.cpuConfig.loadLatency, statistics);
    
    //
    // Parse code and allocate memory locations
    //
    
    CodeParser codeParser = new CodeParser(initLoader.getInstructionFunctionModels(), initLoader.getRegisterFile(),
                                           inputCodeModelFactory, config.memoryLocations);
    codeParser.parseCode(config.code, false); // false to avoid duplicate work
    if (!codeParser.success())
    {
      throw new IllegalStateException("Code parsing failed: " + codeParser.getErrorMessages());
    }
    // Parser now holds all memory locations, all labels, all errors
    
    // Initialize memory. This is linked to the values of labels in code, so relocating the labels changes the values in code
    MemoryInitializer memoryInitializer = new MemoryInitializer(128, config.cpuConfig.callStackSize);
    memoryInitializer.setLabels(codeParser.getLabels());
    memoryInitializer.addLocations(codeParser.getMemoryLocations());
    memoryInitializer.initializeMemory(simulatedMemory);
    
    codeParser.fillImmediateValues();
    if (!codeParser.success())
    {
      throw new IllegalStateException("Code parsing failed: " + codeParser.getErrorMessages());
    }
    
    // Count static instruction mix
    this.statistics.allocateInstructionStats(codeParser.getInstructions().size());
    codeParser.getInstructions()
            .forEach(ins -> statistics.staticInstructionMix.increment(ins.getInstructionTypeEnum()));
    
    InstructionFunctionModel nopFM = initLoader.getInstructionFunctionModel("nop");
    InputCodeModel nop = inputCodeModelFactory.createInstance(nopFM, new ArrayList<>(),
                                                              codeParser.getInstructions().size());
    this.instructionMemoryBlock = new InstructionMemoryBlock(codeParser.getInstructions(), codeParser.getLabels(), nop);
    
    // Create memory
    this.unifiedRegisterFileBlock = new UnifiedRegisterFileBlock(initLoader, config.cpuConfig.speculativeRegisters,
                                                                 registerModelFactory);
    this.debugLog                 = new DebugLog(unifiedRegisterFileBlock);
    // Set the sp to the end of the stack
    RegisterModel sp = this.unifiedRegisterFileBlock.getRegister("sp");
    if (sp != null)
    {
      sp.setValue(memoryInitializer.getStackPointer());
    }
    else
    {
      System.err.println("Warning: sp register not found. Not setting stack pointer.");
    }
    
    // Set the ra to the exit address
    RegisterModel ra = this.unifiedRegisterFileBlock.getRegister("ra");
    if (ra != null && (long) ra.getValue(DataTypeEnum.kLong) == 0)
    {
      ra.setValue(memoryInitializer.getExitPointer());
    }
    else
    {
      System.err.println("Warning: ra register not found or explicitly overwritten. Not setting exit address.");
    }
    
    this.renameMapTableBlock = new RenameMapTableBlock(unifiedRegisterFileBlock);
    
    this.globalHistoryRegister = new GlobalHistoryRegister(10);
    PatternHistoryTable.PredictorType predictorType = switch (config.cpuConfig.predictorType)
    {
      case "0bit" -> PatternHistoryTable.PredictorType.ZERO_BIT_PREDICTOR;
      case "1bit" -> PatternHistoryTable.PredictorType.ONE_BIT_PREDICTOR;
      case "2bit" -> PatternHistoryTable.PredictorType.TWO_BIT_PREDICTOR;
      default ->
              throw new IllegalStateException("Unexpected value for predictor type: " + config.cpuConfig.predictorType);
    };
    
    boolean[] defaultTaken = getDefaultTaken(config.cpuConfig);
    
    this.patternHistoryTable = new PatternHistoryTable(config.cpuConfig.phtSize, defaultTaken, predictorType);
    this.gShareUnit          = new GShareUnit(1024, this.globalHistoryRegister, this.patternHistoryTable);
    this.branchTargetBuffer  = new BranchTargetBuffer(config.cpuConfig.btbSize);
    
    ReplacementPoliciesEnum replacementPoliciesEnum = switch (config.cpuConfig.cacheReplacement)
    {
      case "LRU" -> ReplacementPoliciesEnum.LRU;
      case "FIFO" -> ReplacementPoliciesEnum.FIFO;
      case "Random" -> ReplacementPoliciesEnum.RANDOM;
      default -> throw new IllegalStateException(
              "Unexpected value for cache replacement: " + config.cpuConfig.cacheReplacement);
    };
    
    // Define memory
    boolean writeBack = true;
    if (!Objects.equals(config.cpuConfig.storeBehavior, "write-back"))
    {
      throw new IllegalStateException("Unexpected value for store behavior: " + config.cpuConfig.storeBehavior);
    }
    
    if (config.cpuConfig.useCache)
    {
      this.cache = new Cache(simulatedMemory, config.cpuConfig.cacheLines, config.cpuConfig.cacheAssoc,
                             config.cpuConfig.cacheLineSize, config.cpuConfig.cacheAccessDelay,
                             config.cpuConfig.cacheAccessDelay, replacementPoliciesEnum, writeBack, statistics);
    }
    else
    {
      this.cache = null;
    }
    
    this.memoryModel          = new MemoryModel(cache, simulatedMemory, statistics);
    this.loadStoreInterpreter = new CodeLoadStoreInterpreter();
    
    this.instructionFetchBlock = new InstructionFetchBlock(config.cpuConfig.fetchWidth,
                                                           config.cpuConfig.branchFollowLimit, simCodeModelFactory,
                                                           instructionMemoryBlock, gShareUnit, branchTargetBuffer);
    int entryPoint;
    if (config.entryPoint instanceof String)
    {
      Label label = instructionMemoryBlock.getLabels().get((String) config.entryPoint);
      if (label == null)
      {
        throw new IllegalArgumentException("Label " + config.entryPoint + " not found");
      }
      entryPoint = (int) label.getAddress();
    }
    else if (config.entryPoint instanceof Integer)
    {
      // should be validated by now
      entryPoint = (int) config.entryPoint;
    }
    else
    {
      throw new IllegalArgumentException("Unexpected value for entry point: " + config.entryPoint);
    }
    this.instructionFetchBlock.setPc(entryPoint);
    
    this.branchInterpreter      = new CodeBranchInterpreter();
    this.decodeAndDispatchBlock = new DecodeAndDispatchBlock(instructionFetchBlock, renameMapTableBlock,
                                                             globalHistoryRegister, branchTargetBuffer,
                                                             instructionMemoryBlock, config.cpuConfig.fetchWidth,
                                                             statistics, branchInterpreter);
    
    
    // Issue
    this.arithmeticInterpreter = new CodeArithmeticInterpreter();
    
    // Memory blocks
    this.storeBufferBlock = new StoreBufferBlock(config.cpuConfig.sbSize, unifiedRegisterFileBlock);
    this.loadBufferBlock  = new LoadBufferBlock(config.cpuConfig.lbSize, storeBufferBlock, unifiedRegisterFileBlock);
    
    // FUs
    this.aluIssueWindowBlock       = new IssueWindowBlock(InstructionTypeEnum.kIntArithmetic);
    this.branchIssueWindowBlock    = new IssueWindowBlock(InstructionTypeEnum.kJumpbranch);
    this.fpIssueWindowBlock        = new IssueWindowBlock(InstructionTypeEnum.kFloatArithmetic);
    this.loadStoreIssueWindowBlock = new IssueWindowBlock(InstructionTypeEnum.kLoadstore);
    
    this.issueWindowSuperBlock = new IssueWindowSuperBlock(decodeAndDispatchBlock,
                                                           List.of(aluIssueWindowBlock, fpIssueWindowBlock,
                                                                   branchIssueWindowBlock, loadStoreIssueWindowBlock));
    
    // ROB
    this.reorderBufferBlock = new ReorderBufferBlock(config.cpuConfig.robSize, config.cpuConfig.commitWidth,
                                                     renameMapTableBlock, decodeAndDispatchBlock, storeBufferBlock,
                                                     loadBufferBlock, issueWindowSuperBlock, gShareUnit,
                                                     branchTargetBuffer, instructionFetchBlock, statistics,
                                                     memoryInitializer.getExitPointer(), debugLog);
    
    this.arithmeticFunctionUnitBlocks = new ArrayList<>();
    this.fpFunctionUnitBlocks         = new ArrayList<>();
    this.loadStoreFunctionUnits       = new ArrayList<>();
    this.branchFunctionUnitBlocks     = new ArrayList<>();
    this.memoryAccessUnits            = new ArrayList<>();
    for (FunctionalUnitDescription fu : config.cpuConfig.fUnits)
    {
      switch (fu.fuType)
      {
        case FX ->
        {
          List<String> allowedOperators = fu.getAllowedOperations();
          ArithmeticFunctionUnitBlock functionBlock = new ArithmeticFunctionUnitBlock(fu, fpIssueWindowBlock,
                                                                                      allowedOperators, statistics);
          functionBlock.addArithmeticInterpreter(arithmeticInterpreter);
          this.aluIssueWindowBlock.addFunctionUnit(functionBlock);
          this.arithmeticFunctionUnitBlocks.add(functionBlock);
        }
        case FP ->
        {
          List<String> allowedOperators = fu.getAllowedOperations();
          ArithmeticFunctionUnitBlock functionBlock = new ArithmeticFunctionUnitBlock(fu, fpIssueWindowBlock,
                                                                                      allowedOperators, statistics);
          functionBlock.addArithmeticInterpreter(arithmeticInterpreter);
          this.fpIssueWindowBlock.addFunctionUnit(functionBlock);
          this.fpFunctionUnitBlocks.add(functionBlock);
        }
        case L_S ->
        {
          LoadStoreFunctionUnit loadStoreFunctionUnit = new LoadStoreFunctionUnit(fu, loadStoreIssueWindowBlock,
                                                                                  loadBufferBlock, storeBufferBlock,
                                                                                  loadStoreInterpreter, statistics);
          this.loadStoreIssueWindowBlock.addFunctionUnit(loadStoreFunctionUnit);
          this.loadStoreFunctionUnits.add(loadStoreFunctionUnit);
        }
        case Branch ->
        {
          BranchFunctionUnitBlock branchFunctionUnitBlock = new BranchFunctionUnitBlock(fu, branchIssueWindowBlock,
                                                                                        branchInterpreter, statistics);
          this.branchIssueWindowBlock.addFunctionUnit(branchFunctionUnitBlock);
          this.branchFunctionUnitBlocks.add(branchFunctionUnitBlock);
        }
        case Memory ->
        {
          MemoryAccessUnit memoryAccessUnit = new MemoryAccessUnit(fu, loadStoreIssueWindowBlock, loadBufferBlock,
                                                                   storeBufferBlock, memoryModel, loadStoreInterpreter,
                                                                   statistics);
          this.loadBufferBlock.addMemoryAccessUnit(memoryAccessUnit);
          this.storeBufferBlock.addMemoryAccessUnit(memoryAccessUnit);
          this.memoryAccessUnits.add(memoryAccessUnit);
        }
        default -> throw new IllegalStateException("Unexpected FU type: " + fu.fuType);
      }
    }
  }
  
  /**
   * @param config The configuration
   *
   * @brief Get the default state for the predictor from the configuration
   */
  private static boolean[] getDefaultTaken(CpuConfig config)
  {
    boolean[] defaultTaken;
    if (config.predictorType.equals("0bit") || config.predictorType.equals("1bit"))
    {
      if (!Objects.equals(config.predictorDefault, "Taken") && !Objects.equals(config.predictorDefault, "Not Taken"))
      {
        throw new IllegalStateException("Unexpected value for 0bit/1bit predictor: " + config.predictorDefault);
      }
      boolean take = config.predictorDefault.equals("taken");
      defaultTaken = new boolean[]{take};
    }
    else
    {
      assert config.predictorType.equals("2bit");
      defaultTaken = switch (config.predictorDefault)
      {
        case "Strongly Not Taken" -> new boolean[]{false, false};
        case "Weakly Not Taken" -> new boolean[]{true, false};
        case "Weakly Taken" -> new boolean[]{false, true};
        case "Strongly Taken" -> new boolean[]{true, true};
        default -> throw new IllegalStateException("Unexpected value for 2bit predictor: " + config.predictorDefault);
      };
    }
    defaultTaken[0] = config.predictorDefault.equals("taken");
    return defaultTaken;
  }
  
  /**
   * Override equals to compare by value
   *
   * @param obj the other object
   *
   * @return true if the objects are equal by value
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null || getClass() != obj.getClass())
    {
      return false;
    }
    CpuState myObject = (CpuState) obj;
    
    // Compare:
    String meJson    = this.serialize();
    String otherJson = myObject.serialize();
    
    return meJson.equals(otherJson);
  }
  
  public String serialize()
  {
    ObjectMapper serializer = Serialization.getSerializer();
    try
    {
      return serializer.writeValueAsString(this);
    }
    catch (JsonProcessingException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @brief Calls all blocks and tell them to update their values (triggered by GlobalTimer)
   * Runs ROB at the end again
   * Mutates the object - if you want to keep the state, use `deepCopy()` first.
   */
  public void step()
  {
    // memory
    simulatedMemory.simulate(tick);
    if (cache != null)
    {
      cache.simulate(tick);
    }
    // rob
    reorderBufferBlock.simulate(tick);
    // Run all FUs
    arithmeticFunctionUnitBlocks.forEach(arithmeticFunctionUnitBlock -> arithmeticFunctionUnitBlock.simulate(tick));
    fpFunctionUnitBlocks.forEach(arithmeticFunctionUnitBlock -> arithmeticFunctionUnitBlock.simulate(tick));
    loadStoreFunctionUnits.forEach(loadStoreFunctionUnit -> loadStoreFunctionUnit.simulate(tick));
    memoryAccessUnits.forEach(memoryAccessUnit -> memoryAccessUnit.simulate(tick));
    branchFunctionUnitBlocks.forEach(branchFunctionUnitBlock -> branchFunctionUnitBlock.simulate(tick));
    // Check which buffer contains older instruction at the top
    // Null check first, if any is empty, the order does not matter
    if (loadBufferBlock.getQueueSize() == 0 || storeBufferBlock.getQueueSize() == 0 || loadBufferBlock.getLoadQueueFirst()
            .getIntegerId() < storeBufferBlock.getStoreQueueFirst().getIntegerId())
    {
      loadBufferBlock.simulate(tick);
      storeBufferBlock.simulate(tick);
    }
    else
    {
      storeBufferBlock.simulate(tick);
      loadBufferBlock.simulate(tick);
    }
    // run all AbstractIssueWindowBlock blocks
    aluIssueWindowBlock.simulate(tick);
    fpIssueWindowBlock.simulate(tick);
    branchIssueWindowBlock.simulate(tick);
    loadStoreIssueWindowBlock.simulate(tick);
    //    issueWindowSuperBlock.simulate(tick);
    reorderBufferBlock.simulate_issue(tick);
    decodeAndDispatchBlock.simulate(tick);
    instructionFetchBlock.simulate(tick);
    // Stats
    statistics.incrementClockCycles();
    
    this.tick++;
  }// end of run
  
  /**
   * The order of checks sets their priority.
   *
   * @return Reason for stopping the simulation, or kNotStopped if the simulation is still running.
   */
  public StopReason simStatus()
  {
    boolean halt = reorderBufferBlock.stopReason == StopReason.kCallStackHalt;
    if (halt)
    {
      return StopReason.kCallStackHalt;
    }
    if (tick > 1000000)
    {
      return StopReason.kMaxCycles;
    }
    boolean exceptionRaised = reorderBufferBlock.stopReason == StopReason.kException;
    if (exceptionRaised)
    {
      return StopReason.kException;
    }
    boolean robEmpty      = reorderBufferBlock.getReorderQueueSize() == 0;
    boolean pcEnd         = instructionFetchBlock.getPc() >= instructionMemoryBlock.getCode().size() * 4;
    boolean renameEmpty   = decodeAndDispatchBlock.getCodeBuffer().isEmpty();
    boolean fetchNotEmpty = !instructionFetchBlock.getFetchedCode().isEmpty();
    boolean nop           = fetchNotEmpty && instructionFetchBlock.getFetchedCode().get(0).getInstructionName()
            .equals("nop");
    if (robEmpty && pcEnd && renameEmpty && nop)
    {
      return StopReason.kEndOfCode;
    }
    return StopReason.kNotStopped;
  }
}
