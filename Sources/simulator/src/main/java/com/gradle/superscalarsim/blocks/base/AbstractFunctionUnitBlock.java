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

import com.gradle.superscalarsim.blocks.AbstractBlock;
import com.gradle.superscalarsim.models.SimCodeModel;

import java.util.Stack;

/**
 * @class AbstractFunctionUnitBlock
 * @brief Abstract class containing interface and shared logic for all function units
 */
public abstract class AbstractFunctionUnitBlock implements AbstractBlock
{
  /// Delay for function unit, representing how many ticks does it take to generate result
  private int delay;
  /// Counter variable
  private int counter;
  /// Name of the function unit
  private String name;
  /// Class containing simulated Reorder Buffer
  protected ReorderBufferBlock reorderBufferBlock;
  /// Class containing logic of Instruction decode stage
  protected SimCodeModel simCodeModel;
  
  /// Id specifying when instruction passed specified FU
  protected int functionUnitId;
  /// Overall count of FUs in assigned issue window
  protected int functionUnitCount;
  
  /// Stack holding values of counters when function block fails
  protected final Stack<Integer> failedCounters;
  /// Stack holding failed instructions
  protected final Stack<SimCodeModel> failedInstructions;
  /// Issue window block for comparing instruction and data types
  protected AbstractIssueWindowBlock issueWindowBlock;
  
  public AbstractFunctionUnitBlock()
  {
    this.failedCounters     = new Stack<>();
    this.failedInstructions = new Stack<>();
  }
  
  /**
   * @brief Constructor
   * @param [in] blockScheduleTask  - Task class, where blocks are periodically triggered by the GlobalTimer
   * @param [in] reorderBufferBlock - Class containing simulated Reorder Buffer
   * @param [in] delay              - Delay for function unit
   * @param [in] issueWindowBlock   - Issue window block for comparing instruction and data types
   */
  public AbstractFunctionUnitBlock(ReorderBufferBlock reorderBufferBlock,
                                   int delay,
                                   AbstractIssueWindowBlock issueWindowBlock)
  {
    this.reorderBufferBlock = reorderBufferBlock;
    this.delay              = delay;
    this.counter            = 0;
    this.simCodeModel       = null;
    this.failedCounters     = new Stack<>();
    this.failedInstructions = new Stack<>();
    this.issueWindowBlock   = issueWindowBlock;
    this.name               = "Function Unit";
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets the all the lists/stacks/variables in the function unit
   */
  @Override
  public void reset()
  {
    this.counter      = 0;
    this.simCodeModel = null;
    this.failedCounters.clear();
    this.failedInstructions.clear();
  }// end of reset
  //----------------------------------------------------------------------
  
  /**
   * @brief Get function unit name
   * @return String representing function unit name
   */
  public String getName()
  {
    return name;
  }// end of getName
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets the name of the function unit
   * @param [in] name - New name for the function unit
   */
  public void setName(String name)
  {
    this.name = name;
  }// end of setName
  //----------------------------------------------------------------------
  
  /**
   * @brief Get currently set delay of the function unit
   * @return Integer variable representing delay of the function unit
   */
  public int getDelay()
  {
    return delay;
  }// end of getDelay
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets the counter to zero (needs to be used before setting the new instruction)
   */
  public void resetCounter()
  {
    this.counter = 0;
  }// end of resetCounter
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets the counter to the delay value decremented by one
   */
  public void resetReverseCounter()
  {
    this.counter = this.delay - 1;
  }// end of resetReverseCounter
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets delay of an function unit
   * @param [in] delay - Integer, representing how many ticks does it take to generate result
   */
  public void setDelay(int delay)
  {
    this.delay = delay;
  }// end of setDelay
  //----------------------------------------------------------------------
  
  /**
   * @brief Moves to counter up and checks if delay has passed
   * @return True if delay has passed, false otherwise
   */
  protected boolean hasDelayPassed()
  {
    this.counter = Math.min(this.counter + 1, this.delay);
    return this.counter == this.delay;
  }// end of hasDelayPassed
  //----------------------------------------------------------------------
  
  /**
   * @brief Moves the counter down and checks if counter reached 0
   * @return True if counter is at 0, false otherwise
   */
  public boolean hasReversedDelayPassed()
  {
    this.counter = Math.max(this.counter - 1, 0);
    return this.counter == 0;
  }// end of hasReversedDelayPassed
  //----------------------------------------------------------------------
  
  /**
   * @brief Moves the counter down, stops at zero
   */
  protected void reduceCounter()
  {
    this.counter = Math.max(this.counter - 1, 0);
  }// end of hasReversedDelayPassed
  
  public boolean hasTimerStarted()
  {
    return this.counter == 0;
  }
  
  /**
   * @brief Gets currently executed instruction
   * @return Current executed instruction
   */
  public SimCodeModel getSimCodeModel()
  {
    return simCodeModel;
  }// end of getDecodeCodeModel
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets instruction to be executed
   * @param [in] decodeCodeModel - Instruction to be executed
   */
  public void setSimCodeModel(SimCodeModel simCodeModel)
  {
    this.simCodeModel = simCodeModel;
  }// end of setDecodeCodeModel
  //----------------------------------------------------------------------
  
  /**
   * @brief Checks if function unit has no instruction, that is being executed
   * @return True if function unit is idle, false if busy
   */
  public boolean isFunctionUnitEmpty()
  {
    return this.simCodeModel == null;
  }// end of isFunctionUnitEmpty
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets id for this function unit
   * @param [in] functionUnitId - Id for this function unit
   */
  public void setFunctionUnitId(int functionUnitId)
  {
    this.functionUnitId = functionUnitId;
  }// end of setFunctionUnitId
  //----------------------------------------------------------------------
  
  /**
   * @brief Sets number of function units, which share same issue window
   * @param [in] functionUnitCount - Number of function units in same issue window
   */
  public void setFunctionUnitCount(int functionUnitCount)
  {
    this.functionUnitCount = functionUnitCount;
  }// end of setFunctionUnitCount
  //----------------------------------------------------------------------
  
  /**
   * @brief Resets counter from stack of failed instruction counters
   */
  public void popHistoryCounter()
  {
    this.counter = this.failedCounters.pop() - 1;
  }// end of resetCounter
  //----------------------------------------------------------------------
  
  /**
   * @brief Set zero to counter and saves previous value of the counter to the stack
   */
  protected void zeroTheCounter()
  {
    this.failedCounters.push(this.counter);
    this.counter = 0;
  }// end of setCounter
  //----------------------------------------------------------------------
  
  /**
   * @brief Returns value of the counter
   */
  protected int getCounter()
  {
    return this.counter;
  }// end of setCounter
  //----------------------------------------------------------------------
  
}
