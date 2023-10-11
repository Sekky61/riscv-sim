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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gradle.superscalarsim.blocks.CacheStatisticsCounter;
import com.gradle.superscalarsim.blocks.StatisticsCounter;
import com.gradle.superscalarsim.blocks.arithmetic.AluIssueWindowBlock;
import com.gradle.superscalarsim.blocks.arithmetic.ArithmeticFunctionUnitBlock;
import com.gradle.superscalarsim.blocks.arithmetic.FpIssueWindowBlock;
import com.gradle.superscalarsim.blocks.base.*;
import com.gradle.superscalarsim.blocks.branch.*;
import com.gradle.superscalarsim.blocks.loadstore.*;
import com.gradle.superscalarsim.code.*;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.serialization.GsonConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @class CpuState
 * @brief The state of the CPU.
 * @details The order of the fields is important for serialization.
 */
public class CpuState implements Serializable
{
  public int tick;
  
  /**
   * Warning: this must be a reference to the same loader as in Cpu
   */
  public InitLoader initLoader;
  public InstructionMemoryBlock instructionMemoryBlock;
  public SimCodeModelAllocator simCodeModelAllocator;
  
  public ReorderBufferState reorderBufferState;
  
  // Housekeeping
  
  public StatisticsCounter statisticsCounter;
  public CacheStatisticsCounter cacheStatisticsCounter;
  
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
  // TODO this is serialized as ref
  public LoadBufferBlock loadBufferBlock;
  
  // ALU
  public CodeArithmeticInterpreter arithmeticInterpreter;
  public List<ArithmeticFunctionUnitBlock> arithmeticFunctionUnitBlocks;
  public List<ArithmeticFunctionUnitBlock> fpFunctionUnitBlocks;
  public AluIssueWindowBlock aluIssueWindowBlock;
  public FpIssueWindowBlock fpIssueWindowBlock;
  
  public CodeBranchInterpreter branchInterpreter;
  public List<BranchFunctionUnitBlock> branchFunctionUnitBlocks;
  public BranchIssueWindowBlock branchIssueWindowBlock;
  
  // Load/Store
  public List<LoadStoreFunctionUnit> loadStoreFunctionUnits;
  public LoadStoreIssueWindowBlock loadStoreIssueWindowBlock;
  
  // Memory
  public List<MemoryAccessUnit> memoryAccessUnits;
  
  public SimulatedMemory simulatedMemory;
  
  public IssueWindowSuperBlock issueWindowSuperBlock;
  
  // ROB "without the state"
  public ReorderBufferBlock reorderBufferBlock;
  
  public CpuState()
  {
    // Empty constructor for serialization
    
  }
  
  public CpuState(CpuConfiguration config, InitLoader initLoader)
  {
    this.initLoader = initLoader;
    this.initState(config);
  }
  
  /**
   * @brief Initialize the CPU state - given the configuration.
   */
  public void initState(CpuConfiguration config)
  {
    this.tick = 0;
    
    this.instructionMemoryBlock = new InstructionMemoryBlock(initLoader);
    this.instructionMemoryBlock.parse(config.code);
    
    simCodeModelAllocator = new SimCodeModelAllocator();
    
    this.reorderBufferState        = new ReorderBufferState();
    reorderBufferState.bufferSize  = config.robSize;
    reorderBufferState.commitLimit = config.commitWidth;
    
    this.statisticsCounter      = new StatisticsCounter();
    this.cacheStatisticsCounter = new CacheStatisticsCounter();
    
    this.unifiedRegisterFileBlock = new UnifiedRegisterFileBlock(initLoader);
    this.renameMapTableBlock      = new RenameMapTableBlock(unifiedRegisterFileBlock);
    
    this.globalHistoryRegister = new GlobalHistoryRegister(10);
    PatternHistoryTable.PredictorType predictorType = switch (config.predictorType)
    {
      case "0bit" -> PatternHistoryTable.PredictorType.ZERO_BIT_PREDICTOR;
      case "1bit" -> PatternHistoryTable.PredictorType.ONE_BIT_PREDICTOR;
      case "2bit" -> PatternHistoryTable.PredictorType.TWO_BIT_PREDICTOR;
      default -> throw new IllegalStateException("Unexpected value for predictor type: " + config.predictorType);
    };
    
    boolean[] defaultTaken = getDefaultTaken(config);
    
    this.patternHistoryTable = new PatternHistoryTable(config.phtSize, defaultTaken, predictorType);
    this.gShareUnit          = new GShareUnit(1024, this.globalHistoryRegister, this.patternHistoryTable);
    this.branchTargetBuffer  = new BranchTargetBuffer(config.btbSize);
    this.simulatedMemory     = new SimulatedMemory();
    
    ReplacementPoliciesEnum replacementPoliciesEnum = switch (config.cacheReplacement)
    {
      case "LRU" -> ReplacementPoliciesEnum.LRU;
      case "FIFO" -> ReplacementPoliciesEnum.FIFO;
      case "Random" -> ReplacementPoliciesEnum.RANDOM;
      default -> throw new IllegalStateException("Unexpected value for cache replacement: " + config.cacheReplacement);
    };
    
    boolean writeBack = true;
    if (!Objects.equals(config.storeBehavior, "write-back"))
    {
      throw new IllegalStateException("Unexpected value for store behavior: " + config.storeBehavior);
    }
    
    this.cache = new Cache(simulatedMemory, config.cacheLines, config.cacheAssoc, config.cacheLineSize,
                           replacementPoliciesEnum, writeBack, config.addRemainingDelay, config.storeLatency,
                           config.loadLatency, config.laneReplacementDelay, this.cacheStatisticsCounter);
    
    this.memoryModel          = new MemoryModel(cache, cacheStatisticsCounter);
    this.loadStoreInterpreter = new CodeLoadStoreInterpreter(initLoader, memoryModel, unifiedRegisterFileBlock);
    
    this.instructionFetchBlock = new InstructionFetchBlock(simCodeModelAllocator, instructionMemoryBlock, gShareUnit,
                                                           branchTargetBuffer);
    instructionFetchBlock.setNumberOfWays(config.fetchWidth);
    
    this.decodeAndDispatchBlock = new DecodeAndDispatchBlock(simCodeModelAllocator, instructionFetchBlock,
                                                             renameMapTableBlock, globalHistoryRegister,
                                                             branchTargetBuffer, instructionMemoryBlock);
    this.reorderBufferBlock     = new ReorderBufferBlock(unifiedRegisterFileBlock, renameMapTableBlock,
                                                         decodeAndDispatchBlock, gShareUnit, branchTargetBuffer,
                                                         instructionFetchBlock, statisticsCounter, reorderBufferState);
    this.issueWindowSuperBlock  = new IssueWindowSuperBlock(decodeAndDispatchBlock);
    this.arithmeticInterpreter  = new CodeArithmeticInterpreter(unifiedRegisterFileBlock);
    this.branchInterpreter      = new CodeBranchInterpreter(instructionMemoryBlock, unifiedRegisterFileBlock,
                                                            arithmeticInterpreter);
    
    this.storeBufferBlock = new StoreBufferBlock(loadStoreInterpreter, decodeAndDispatchBlock, unifiedRegisterFileBlock,
                                                 reorderBufferBlock);
    storeBufferBlock.setBufferSize(config.sbSize);
    
    this.loadBufferBlock = new LoadBufferBlock(storeBufferBlock, decodeAndDispatchBlock, unifiedRegisterFileBlock,
                                               reorderBufferBlock, instructionFetchBlock);
    loadBufferBlock.setBufferSize(config.lbSize);
    
    // FUs
    
    this.aluIssueWindowBlock       = new AluIssueWindowBlock(unifiedRegisterFileBlock);
    this.branchIssueWindowBlock    = new BranchIssueWindowBlock(unifiedRegisterFileBlock);
    this.fpIssueWindowBlock        = new FpIssueWindowBlock(unifiedRegisterFileBlock);
    this.loadStoreIssueWindowBlock = new LoadStoreIssueWindowBlock(unifiedRegisterFileBlock);
    
    this.issueWindowSuperBlock.addAluIssueWindow(aluIssueWindowBlock);
    this.issueWindowSuperBlock.addFpIssueWindow(fpIssueWindowBlock);
    this.issueWindowSuperBlock.addLoadStoreIssueWindow(loadStoreIssueWindowBlock);
    this.issueWindowSuperBlock.addBranchIssueWindow(branchIssueWindowBlock);
    
    this.arithmeticFunctionUnitBlocks = new ArrayList<>();
    this.fpFunctionUnitBlocks         = new ArrayList<>();
    this.loadStoreFunctionUnits       = new ArrayList<>();
    this.branchFunctionUnitBlocks     = new ArrayList<>();
    this.memoryAccessUnits            = new ArrayList<>();
    for (CpuConfiguration.FUnit fu : config.fUnits)
    {
      switch (fu.fuType)
      {
        case "FX" ->
        {
          ArithmeticFunctionUnitBlock functionBlock = new ArithmeticFunctionUnitBlock(reorderBufferBlock, fu.latency,
                                                                                      fpIssueWindowBlock,
                                                                                      fu.operations);
          functionBlock.addArithmeticInterpreter(arithmeticInterpreter);
          functionBlock.addRegisterFileBlock(unifiedRegisterFileBlock);
          this.aluIssueWindowBlock.setFunctionUnitBlock(functionBlock);
          this.arithmeticFunctionUnitBlocks.add(functionBlock);
        }
        case "FP" ->
        {
          ArithmeticFunctionUnitBlock functionBlock = new ArithmeticFunctionUnitBlock(reorderBufferBlock, fu.latency,
                                                                                      fpIssueWindowBlock,
                                                                                      fu.operations);
          functionBlock.addArithmeticInterpreter(arithmeticInterpreter);
          functionBlock.addRegisterFileBlock(unifiedRegisterFileBlock);
          this.fpIssueWindowBlock.setFunctionUnitBlock(functionBlock);
          this.fpFunctionUnitBlocks.add(functionBlock);
        }
        case "L/S" ->
        {
          LoadStoreFunctionUnit loadStoreFunctionUnit = new LoadStoreFunctionUnit(reorderBufferBlock, fu.latency,
                                                                                  loadStoreIssueWindowBlock,
                                                                                  loadBufferBlock, storeBufferBlock,
                                                                                  loadStoreInterpreter);
          this.loadStoreIssueWindowBlock.setFunctionUnitBlock(loadStoreFunctionUnit);
          this.loadStoreFunctionUnits.add(loadStoreFunctionUnit);
        }
        case "Branch" ->
        {
          BranchFunctionUnitBlock branchFunctionUnitBlock = new BranchFunctionUnitBlock(reorderBufferBlock,
                                                                                        branchIssueWindowBlock,
                                                                                        fu.latency);
          branchFunctionUnitBlock.addBranchInterpreter(branchInterpreter);
          branchFunctionUnitBlock.addRegisterFileBlock(unifiedRegisterFileBlock);
          this.branchIssueWindowBlock.setFunctionUnitBlock(branchFunctionUnitBlock);
          this.branchFunctionUnitBlocks.add(branchFunctionUnitBlock);
        }
        case "Memory" ->
        {
          MemoryAccessUnit memoryAccessUnit = new MemoryAccessUnit(reorderBufferBlock, fu.latency,
                                                                   loadStoreIssueWindowBlock, loadBufferBlock,
                                                                   storeBufferBlock, loadStoreInterpreter,
                                                                   unifiedRegisterFileBlock);
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
  private static boolean[] getDefaultTaken(CpuConfiguration config)
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
   * @brief Create a new CPU state - a deep copy of the current state.
   */
  public CpuState deepCopy()
  {
    // Serialize and deserialize
    String serialized = this.serialize();
    return CpuState.deserialize(serialized);
  }
  
  public String serialize()
  {
    return JsonWriter.objectToJson(this, GsonConfiguration.getJsonWriterOptions());
  }
  
  public static CpuState deserialize(String json)
  {
    CpuState state = (CpuState) JsonReader.jsonToJava(json, GsonConfiguration.getJsonWriterOptions());
    return state;
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
    String meJson    = JsonWriter.objectToJson(this, GsonConfiguration.getJsonWriterOptions());
    String otherJson = JsonWriter.objectToJson(myObject, GsonConfiguration.getJsonWriterOptions());
    
    return meJson.equals(otherJson);
  }
  
  /**
   * @brief Calls all blocks and tell them to update their values (triggered by GlobalTimer)
   * Runs ROB at the end again
   * Mutates the object - if you want to keep the state, use `deepCopy()` first.
   */
  public void step()
  {
    reorderBufferBlock.simulate();
    // Run all FUs
    arithmeticFunctionUnitBlocks.forEach(ArithmeticFunctionUnitBlock::simulate);
    fpFunctionUnitBlocks.forEach(ArithmeticFunctionUnitBlock::simulate);
    loadStoreFunctionUnits.forEach(LoadStoreFunctionUnit::simulate);
    memoryAccessUnits.forEach(MemoryAccessUnit::simulate);
    branchFunctionUnitBlocks.forEach(BranchFunctionUnitBlock::simulate);
    // Check which buffer contains older instruction at the top
    // Null check first, if any is empty, the order does not matter
    if (loadBufferBlock.getLoadQueueFirst() == null || storeBufferBlock.getStoreQueueFirst() == null || loadBufferBlock.getLoadQueueFirst()
            .getId() < storeBufferBlock.getStoreQueueFirst().getId())
    {
      loadBufferBlock.simulate();
      storeBufferBlock.simulate();
    }
    else
    {
      storeBufferBlock.simulate();
      loadBufferBlock.simulate();
    }
    // run all AbstractIssueWindowBlock blocks
    aluIssueWindowBlock.simulate();
    fpIssueWindowBlock.simulate();
    branchIssueWindowBlock.simulate();
    loadStoreIssueWindowBlock.simulate();
    issueWindowSuperBlock.simulate();
    decodeAndDispatchBlock.simulate();
    instructionFetchBlock.simulate();
    // bump commit id of ROB
    reorderBufferBlock.bumpCommitID();
    // Stats
    statisticsCounter.incrementClockCycles();
    
    this.tick++;
    
    // Clean up
    simCodeModelAllocator.deleteOldReferences();
  }// end of run
}
