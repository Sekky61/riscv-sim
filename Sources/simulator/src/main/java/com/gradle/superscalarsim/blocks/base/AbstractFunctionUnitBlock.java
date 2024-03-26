/**
 * @file AbstractFunctionUnitBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains abstract class for all Function Units
 * @date 9  February   2021 16:00 (created) \n
 * 27 April      2021 20:00 (revised)
 * 26 Sep      2023 10:00 (revised)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2020  Jan Vavra
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
package com.gradle.superscalarsim.blocks.base;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.cpu.SimulationStatistics;
import com.gradle.superscalarsim.models.FunctionalUnitDescription;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;

/**
 * @class AbstractFunctionUnitBlock
 * @brief Abstract class containing interface and shared logic for all function units
 * @details The clock starts at 1, so after the first cycle in the GUI 1/X is visible.
 * The flow of execution is defined here, the specifics are implemented in the derived classes.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public abstract class AbstractFunctionUnitBlock implements AbstractBlock
{
  final static int counterStart = 1;
  
  /**
   * ID specifying when instruction passed specified FU.
   * Must be unique.
   */
  protected int functionUnitId;
  
  /**
   * Class containing logic of Instruction decode stage
   */
  @JsonIdentityReference(alwaysAsId = true)
  protected SimCodeModel simCodeModel;
  
  /**
   * Overall count of FUs in assigned issue window
   */
  protected int functionUnitCount;
  
  /**
   * Issue window block for comparing instruction and data types
   */
  @JsonIdentityReference(alwaysAsId = true)
  protected IssueWindowBlock issueWindowBlock;
  
  /**
   * Statistics for reporting FU usage
   */
  @JsonIdentityReference(alwaysAsId = true)
  protected SimulationStatistics statistics;
  /**
   * Delay for function unit. Can change based on the instruction.
   */
  protected int delay;
  /**
   * Counter variable. Used for counting delay of the function unit.
   */
  protected int counter;
  /**
   * Configuration of the function unit.
   * Latency, capabilities, etc.
   */
  private FunctionalUnitDescription description;
  
  public AbstractFunctionUnitBlock()
  {
  }
  
  /**
   * @param description      Delay for function unit
   * @param issueWindowBlock Issue window block for comparing instruction and data types
   * @param statistics       Statistics for reporting FU usage
   *
   * @brief Constructor
   */
  public AbstractFunctionUnitBlock(FunctionalUnitDescription description,
                                   IssueWindowBlock issueWindowBlock,
                                   SimulationStatistics statistics)
  {
    this.functionUnitId   = description.id;
    this.description      = description;
    this.statistics       = statistics;
    this.counter          = 0;
    this.simCodeModel     = null;
    this.issueWindowBlock = issueWindowBlock;
  }// end of Constructor
  //----------------------------------------------------------------------
  
  //----------------------------------------------------------------------
  
  /**
   * @return String representing function unit name
   * @brief Get function unit name
   */
  public String getName()
  {
    return description.name;
  }// end of getName
  //----------------------------------------------------------------------
  
  /**
   * @return Integer variable representing delay of the function unit
   * @brief Get currently set delay of the function unit
   */
  public int getDelay()
  {
    return description.latency;
  }// end of getDelay
  //----------------------------------------------------------------------
  
  /**
   * @param delay Integer, representing how many ticks does it take to generate result
   *
   * @brief Sets delay of the function unit
   */
  public void setDelay(int delay)
  {
    this.delay = delay;
  }// end of setDelay
  //----------------------------------------------------------------------
  
  /**
   * @return True if delay has passed, false otherwise
   * @brief Moves to counter up and checks if delay has passed
   */
  protected boolean hasDelayPassed()
  {
    return this.counter == (this.delay + counterStart);
  }// end of hasDelayPassed
  //----------------------------------------------------------------------
  
  /**
   * If the instruction is done executing, empty the function unit.
   * Ticks the counter.
   */
  public void emptyIfDone()
  {
    if (simCodeModel == null)
    {
      return;
    }
    if (this.simCodeModel.hasFailed())
    {
      handleFailedInstruction();
      return;
    }
    tickCounter();
    if (hasDelayPassed())
    {
      finishExecution();
    }
  }
  
  /**
   * @brief tick the counter one step
   */
  public void tickCounter()
  {
    this.counter = this.counter + 1;
  }// end of tickCounter
  
  /**
   * @return True if timer has started this cycle, false otherwise
   */
  public boolean hasTimerStartedThisTick()
  {
    return this.counter == counterStart;
  }
  
  /**
   * @return Current executed instruction
   * @brief Gets currently executed instruction
   */
  public SimCodeModel getSimCodeModel()
  {
    return simCodeModel;
  }// end of getDecodeCodeModel
  //----------------------------------------------------------------------
  
  /**
   * @return True if function unit is busy, false otherwise
   */
  public boolean isBusy()
  {
    if (counter == delay + counterStart)
    {
      return false;
    }
    return simCodeModel != null;// && counter < delay;
  }
  
  /**
   * @param decodeCodeModel Instruction to be executed
   *
   * @brief Sets instruction to be executed. If there is already instruction, it is finished and overwritten
   */
  public void startExecuting(SimCodeModel simCodeModel)
  {
    if (this.simCodeModel != null)
    {
      assert this.counter == this.delay;
      finishExecution();
    }
    this.counter      = counterStart;
    this.simCodeModel = simCodeModel;
    this.simCodeModel.setFunctionUnitId(this.functionUnitId);
  }// end of setDecodeCodeModel
  
  /**
   * The second part of FU cycle. Instruction can start work. Finishing is done in emptyIfDone.
   *
   * @param cycle Current cycle
   */
  protected void handleInstruction(int cycle)
  {
    if (this.simCodeModel.hasFailed())
    {
      handleFailedInstruction();
      return;
    }
    
    if (hasTimerStartedThisTick())
    {
      handleStartExecution(cycle);
    }
    
    incrementBusyCycles();
  }
  
  /**
   * @brief Finishes execution of the instruction
   */
  protected abstract void finishExecution();
  
  /**
   * @brief Action that should take place when an instruction failed.
   * Remove the instruction, reset counter, cancel memory transaction.
   */
  protected abstract void handleFailedInstruction();
  
  /**
   * @brief Action that should take place when an instruction starts executing.
   * Calculate the delay, start memory transaction.
   */
  protected abstract void handleStartExecution(int cycle);
  
  /**
   * @return True if function unit is idle, false if busy
   * @brief Checks if function unit has no instruction, that is being executed
   */
  public boolean isFunctionUnitEmpty()
  {
    return this.simCodeModel == null;
  }// end of isFunctionUnitEmpty
  //----------------------------------------------------------------------
  
  /**
   * @param functionUnitId ID for this function unit
   *
   * @brief Sets id for this function unit
   */
  public void setFunctionUnitId(int functionUnitId)
  {
    this.functionUnitId = functionUnitId;
  }// end of setFunctionUnitId
  //----------------------------------------------------------------------
  
  /**
   * @param functionUnitCount Number of function units in same issue window
   *
   * @brief Sets number of function units, which share same issue window
   */
  public void setFunctionUnitCount(int functionUnitCount)
  {
    this.functionUnitCount = functionUnitCount;
  }// end of setFunctionUnitCount
  //----------------------------------------------------------------------
  
  /**
   * @brief Set zero to counter and saves previous value of the counter to the stack
   */
  protected void zeroTheCounter()
  {
    this.counter = 0;
  }// end of setCounter
  //----------------------------------------------------------------------
  
  /**
   * Set the delay based on the instruction
   */
  public void setDelayBasedOnInstruction()
  {
    int delay = switch (this.simCodeModel.getInstructionFunctionModel().getInstructionType())
    {
      case kIntArithmetic, kFloatArithmetic -> getDelayBasedOnCapability();
      case kLoadstore, kJumpbranch -> this.description.latency;
    };
    this.setDelay(delay);
  }
  
  /**
   * Get the delay based on the instruction capability. This is only used for arithmetic instructions.
   */
  private int getDelayBasedOnCapability()
  {
    String                                   expr           = this.simCodeModel.getInstructionFunctionModel()
            .getInterpretableAs();
    FunctionalUnitDescription.CapabilityName capabilityName = FunctionalUnitDescription.classifyExpression(expr);
    for (FunctionalUnitDescription.Capability capability : this.description.operations)
    {
      if (capability.name == capabilityName)
      {
        return capability.latency;
      }
    }
    throw new RuntimeException("Unknown operation: " + expr);
  }
  
  protected FunctionalUnitDescription getDescription()
  {
    return this.description;
  }
  
  /**
   * @brief Increments number of busy cycles
   */
  public void incrementBusyCycles()
  {
    this.statistics.incrementBusyCycles(this.description.name);
  }
  
  /**
   * @param simCodeModel Instruction to be executed
   *
   * @return True if the function unit can execute the instruction, false otherwise.
   */
  public abstract boolean canExecuteInstruction(SimCodeModel simCodeModel);
}
