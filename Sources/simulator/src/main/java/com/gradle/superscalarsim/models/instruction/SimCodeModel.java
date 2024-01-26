/**
 * @file SimCodeModel.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains extended container for processed line of code
 * @date 3  February   2021 16:00 (created) \n
 * 10 March      2021 18:00 (revised)
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
package com.gradle.superscalarsim.models.instruction;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gradle.superscalarsim.code.Expression;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.Identifiable;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @class SimCodeModel
 * @brief Instruction execution data such as renaming registers, exceptions, flags, timestamps.
 * Timestamps are zero if not valid.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SimCodeModel implements IInputCodeModel, Comparable<SimCodeModel>, Identifiable
{
  /**
   * Reference to original code model
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final InputCodeModel inputCodeModel;
  
  /**
   * ID of order of instructions processed by the fetch
   */
  private final int id;
  
  /**
   * A copy of arguments, which are used for renaming.
   * The order of arguments is the same as in the original code line and tests depend on this order.
   */
  private final List<InputCodeArgument> renamedArguments;
  /**
   * ID when the instruction was fetched
   */
  private int fetchId;
  /**
   * ID, when was instructions accepted by the issue window
   */
  private int issueWindowId;
  /**
   * ID of the function block, which processed this instruction
   */
  private int functionUnitId;
  /**
   * ID marking when was result ready
   */
  private int readyId;
  /**
   * ID marking when was instruction committed from ROB
   */
  private int commitId;
  /**
   * True if simcodemodel has left the system (committed, flushed).
   * A finished simcodemodel can be safely deleted.
   */
  private boolean isFinished;
  /**
   * A bit value marking failure due to wrong branch prediction
   */
  private boolean hasFailed;
  /**
   * Prediction made by branch predictor at the time of fetching.
   * Used for branch instructions.
   */
  private boolean branchPredicted;
  /**
   * Result of the branch computation.
   * Used to check for mispredictions.
   * True means branch was taken.
   */
  private boolean branchLogicResult;
  /**
   * Target of the branch instruction.
   * NOT an offset. If you need offset, describe it in the interpretableAs field (see JAL instruction).
   * Result of the branch actual computation, not the prediction.
   * Used to fix BTB and PC in misprediction.
   */
  private int branchTarget;
  /**
   * Invalid instructions are scheduled to be removed from the system.
   * Instruction starts as valid and becomes invalid when it is flushed.
   */
  private boolean isValid;
  
  /**
   * Instruction starts as busy and becomes not busy when the execution is finished.
   * Non-busy, non-speculative instructions are ready to be committed.
   */
  private boolean isBusy;
  
  /**
   * Is instruction speculative?
   */
  private boolean isSpeculative;
  /**
   * Exception caused by this instruction.
   * If committed, the exception takes effect and halts the simulation.
   */
  private InstructionException exception;
  
  /**
   * @param inputCodeModel Original code model
   * @param id             Number marking when was code accepted
   *
   * @brief Constructor which copies original InputCodeModel
   * This constructor can be used only through the SimCodeModelAllocator
   */
  public SimCodeModel(InputCodeModel inputCodeModel, int id, int fetchId)
  {
    this.inputCodeModel = inputCodeModel;
    this.id             = id;
    this.fetchId        = fetchId;
    this.isFinished     = false;
    this.hasFailed      = false;
    this.commitId       = -1;
    this.readyId        = -1;
    this.issueWindowId  = -1;
    this.functionUnitId = -1;
    
    isValid       = true;
    isBusy        = true;
    isSpeculative = false;
    
    exception = null;
    
    // Copy arguments
    this.renamedArguments = new ArrayList<>();
    for (InputCodeArgument argument : inputCodeModel.getArguments())
    {
      this.renamedArguments.add(new InputCodeArgument(argument));
    }
  }// end of Constructor
  
  public InstructionException getException()
  {
    return exception;
  }
  
  public void setException(InstructionException exception)
  {
    this.exception = exception;
  }
  
  /**
   * @return True if instruction is ready, false otherwise
   * @brief Checks if the instruction is ready for commit based on flags
   */
  public boolean isReadyToBeCommitted()
  {
    return !this.isBusy && !this.isSpeculative && this.isValid;
  }// end of isReadyToBeCommitted
  //------------------------------------------------------
  
  /**
   * @return True if instruction can be removed, false otherwise
   * @brief Checks if instruction has failed and can be removed.
   */
  public boolean shouldBeRemoved()
  {
    return !this.isSpeculative && !this.isValid;
  }// end of isReadyToBeRemoved
  //------------------------------------------------------
  
  /**
   * @return Boolean value of speculative bit
   * @brief Gets speculative bit
   */
  public boolean isSpeculative()
  {
    return this.isSpeculative;
  }// end of isSpeculative
  //------------------------------------------------------
  
  /**
   * @param speculative New value of the speculative bit
   *
   * @brief Sets speculative bit
   */
  public void setSpeculative(boolean speculative)
  {
    this.isSpeculative = speculative;
  }// end of setSpeculative
  //------------------------------------------------------
  
  /**
   * @return Boolean value of busy bit
   * @brief Gets busy bit
   */
  public boolean isBusy()
  {
    return this.isBusy;
  }// end of isBusy
  //------------------------------------------------------
  
  /**
   * @brief Sets busy bit
   */
  public void setBusy(boolean busy)
  {
    this.isBusy = busy;
  }// end of setBusy
  //------------------------------------------------------
  
  /**
   * @return True if instruction is valid, false otherwise
   */
  public boolean isValid()
  {
    return isValid;
  }
  
  /**
   * @brief Sets valid bit
   */
  public void setValid(boolean valid)
  {
    this.isValid = valid;
  }// end of setValid
  
  /**
   * @param windowId ID, when was instruction accepted to issue window
   *
   * @brief Sets accepted id to issue window
   */
  public void setIssueWindowId(int windowId)
  {
    this.issueWindowId = windowId;
  }// end of setIssueWindowId
  
  /**
   * @param functionUnitId ID of function block, which processed this instruction
   *
   * @brief Sets id of function block, which processed this instruction
   */
  public void setFunctionUnitId(int functionUnitId)
  {
    this.functionUnitId = functionUnitId;
  }// end of setFunctionUnitId
  //------------------------------------------------------
  
  public int getReadyId()
  {
    return readyId;
  }
  //------------------------------------------------------
  
  public int getCommitId()
  {
    return commitId;
  }
  //------------------------------------------------------
  
  /**
   * @param commitId ID of when was instruction committed
   *
   * @brief Sets id of when was instruction's result ready
   */
  public void setCommitId(int commitId)
  {
    this.commitId = commitId;
  }// end of setCommitId
  
  /**
   * @param codeModel Model to be compared to
   *
   * @return -1 if <, 0 if ==, > if 1
   * @brief Comparator function for assigning to priorityQueue
   */
  @Override
  public int compareTo(@NotNull SimCodeModel codeModel)
  {
    return -Integer.compare(codeModel.getIntegerId(), this.id);
  }// end of compareTo
  //------------------------------------------------------
  
  /**
   * @return ID of the model
   * @brief Gets ID of the model
   */
  public int getIntegerId()
  {
    return this.id;
  }// end of getId
  //------------------------------------------------------
  
  /**
   * @return Integer value of ID
   * @brief for priority queue
   */
  public String getId()
  {
    return Integer.toString(this.id);
  }
  //------------------------------------------------------
  
  /**
   * @return Boolean value marking failure to finish
   * @brief Get the bit value corresponding to failure due to wrong prediction
   */
  public boolean hasFailed()
  {
    return hasFailed;
  }// end of hasFailed
  //------------------------------------------------------
  
  /**
   * @param hasFailed - Has instruction failed to finish due to miss-prediction?
   *
   * @brief Set the bit marking to failure due to wrong prediction
   */
  public void setHasFailed(boolean hasFailed)
  {
    this.hasFailed = hasFailed;
  }// end of setHasFailed
  //------------------------------------------------------
  
  public boolean isBranchPredicted()
  {
    return branchPredicted;
  }
  
  public void setBranchPredicted(boolean branchPredicted)
  {
    this.branchPredicted = branchPredicted;
  }
  
  public boolean isBranchLogicResult()
  {
    return branchLogicResult;
  }
  
  public void setBranchLogicResult(boolean branchLogicResult)
  {
    this.branchLogicResult = branchLogicResult;
  }
  
  public int getBranchTarget()
  {
    return branchTarget;
  }
  
  public void setBranchTarget(int branchTarget)
  {
    this.branchTarget = branchTarget;
  }
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    return this.getRenamedCodeLine();
  }
  
  /**
   * Used for testing
   *
   * @return String with renamed code line
   * @brief Gets string representation of the instruction with renamed arguments
   */
  @JsonProperty
  public String getRenamedCodeLine()
  {
    StringBuilder genericLine = new StringBuilder(getInstructionName());
    genericLine.append(" ");
    List<InstructionFunctionModel.Argument> args = inputCodeModel.getInstructionFunctionModel().getAsmArguments();
    for (int i = 0; i < args.size(); i++)
    {
      boolean wrapInParens = inputCodeModel.getInstructionFunctionModel().getInstructionType()
              .equals(InstructionTypeEnum.kLoadstore) && i == args.size() - 1;
      if (i != 0)
      {
        if (wrapInParens)
        {
          genericLine.append("(");
        }
        else
        {
          genericLine.append(",");
        }
      }
      InstructionFunctionModel.Argument arg        = args.get(i);
      InputCodeArgument                 renamedArg = getArgumentByName(arg.name());
      if (renamedArg == null)
      {
        throw new RuntimeException("Argument " + arg.name() + " not found in " + this);
      }
      genericLine.append(renamedArg.getValue());
      if (wrapInParens)
      {
        genericLine.append(")");
      }
    }
    return genericLine.toString();
  }// end of getRenamedCodeLine
  
  @Override
  public String getInstructionName()
  {
    return inputCodeModel.getInstructionName();
  }
  
  /**
   * @return Renamed arguments of the instruction
   * @brief Gets arguments of the instruction, copied, so they are rewritable
   */
  @Override
  public List<InputCodeArgument> getArguments()
  {
    return renamedArguments;
  }
  
  /**
   * @param name Name of the argument
   *
   * @return An argument by its name
   */
  @Override
  public InputCodeArgument getArgumentByName(String name)
  {
    return renamedArguments.stream().filter(argument -> argument.getName().equals(name)).findFirst().orElse(null);
  }// end of getArgumentByName
  
  @Override
  public InstructionTypeEnum getInstructionTypeEnum()
  {
    return inputCodeModel.getInstructionTypeEnum();
  }
  
  /**
   * @return ID of the instruction (index in the code)
   */
  @Override
  public int getCodeId()
  {
    return inputCodeModel.getCodeId();
  }// end of getId
  
  @Override
  public InstructionFunctionModel getInstructionFunctionModel()
  {
    return inputCodeModel.getInstructionFunctionModel();
  }
  
  /**
   * @param finished True if simcodemodel has left the system (committed, flushed)
   */
  public void setFinished(boolean finished)
  {
    isFinished = finished;
  }
  
  /**
   * @return All arguments of the instruction as variables for the interpreter
   * @brief reads current register values (including speculative values), the PC, constants
   */
  public List<Expression.Variable> getVariables()
  {
    List<Expression.Variable> variables                = new ArrayList<>();
    InstructionFunctionModel  instructionFunctionModel = getInstructionFunctionModel();
    
    variables.add(
            new Expression.Variable("pc", DataTypeEnum.kInt, RegisterDataContainer.fromValue(getSavedPc()), true));
    
    for (InputCodeArgument var : getArguments())
    {
      InstructionFunctionModel.Argument argument   = instructionFunctionModel.getArgumentByName(var.getName());
      RegisterDataContainer             val        = var.getConstantValue();
      boolean                           isConstant = false;
      if (val == null)
      {
        // Try register
        RegisterModel reg = var.getRegisterValue();
        if (reg == null)
        {
          throw new IllegalStateException("Could not parse " + var.getValue() + " as constant or label");
        }
        val        = reg.getValueContainer();
        isConstant = reg.isConstant();
      }
      variables.add(new Expression.Variable(var.getName(), argument.type(), val, isConstant));
    }
    return variables;
  }
  
  /**
   * @return Saved value of the PC, when instruction was fetched
   */
  public int getSavedPc()
  {
    return inputCodeModel.getPc();
  }
  //-------------------------------------------------------------------------------------------
  
  /**
   * @return True if the model is load instruction, false otherwise
   * @brief Checks if specified code model is load instruction
   */
  public boolean isLoad()
  {
    if (getInstructionTypeEnum() != InstructionTypeEnum.kLoadstore)
    {
      return false;
    }
    InstructionFunctionModel instruction = getInstructionFunctionModel();
    return instruction != null && instruction.getInterpretableAs().startsWith("load");
  }// end of isInstructionLoad
  
  /**
   * @return True if the model is store instruction, false otherwise
   * @brief Checks if specified code model is store instruction
   */
  public boolean isStore()
  {
    if (getInstructionTypeEnum() != InstructionTypeEnum.kLoadstore)
    {
      return false;
    }
    InstructionFunctionModel instruction = getInstructionFunctionModel();
    return instruction != null && instruction.getInterpretableAs().startsWith("store");
  }// end of isInstructionStore
  
  /**
   * @return True if the instruction is ready to be executed, false otherwise.
   */
  public boolean isReadyToExecute()
  {
    for (InputCodeArgument argument : getArguments())
    {
      if (!argument.getName().startsWith("rs"))
      {
        continue;
      }
      RegisterModel         reg       = argument.getRegisterValue();
      RegisterReadinessEnum readiness = reg.getReadiness();
      boolean               validity  = readiness == RegisterReadinessEnum.kExecuted || readiness == RegisterReadinessEnum.kAssigned;
      if (!validity)
      {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @return True if the instruction is a conditional branch
   */
  public boolean isConditionalBranch()
  {
    return inputCodeModel.isConditionalBranch();
  }
}
