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
package com.gradle.superscalarsim.models;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @class SimCodeModel
 * @brief Instruction execution data (renaming)
 * Ids are zero if not yet processed
 */
public class SimCodeModel implements IInputCodeModel, Comparable<SimCodeModel>
{
  /**
   * Reference to original code model
   */
  private final InputCodeModel inputCodeModel;
  /**
   * Id of order of instructions processed by the fetch
   */
  private final int id;
  /**
   * Number marking bulk of instructions, which was fetched together
   */
  private int instructionBulkNumber;
  /**
   * Id, when was instructions accepted by the issue window
   */
  private int issueWindowId;
  /**
   * Id of the function block, which processed this instruction
   */
  private int functionUnitId;
  /**
   * Id marking when was result ready
   */
  private int readyId;
  /**
   * Id marking when was instruction committed from ROB
   */
  private int commitId;
  /**
   * Bit value marking failure due to wrong branch prediction
   */
  private boolean hasFailed;
  
  /**
   * Saved value of the PC, when instruction was fetched
   * Used for load/store and branch instructions
   * TODO: Optional
   */
  private int savedPc;
  /**
   * Prediction made by branch predictor at the time of fetching
   * Used for branch instructions
   */
  private boolean branchPredicted;
  /**
   * Result of the branch computation
   * Used to check for mispredictions
   * True means branch was taken
   */
  private boolean branchLogicResult;
  /**
   * Target of the branch instruction (offset from the savedPc)
   * Used to fix BTB and PC in misprediction
   */
  private int branchTargetOffset;
  /**
   * A copy of arguments, which are used for renaming
   */
  private final List<InputCodeArgument> renamedArguments;
  
  /**
   * @brief Constructor - not used anymore
   * @param [in] instructionFunctionModel - Instruction function model
   * @param [in] instructionName       - Name of the parsed instruction
   * @param [in] codeLine              - Unparsed line of code
   * @param [in] arguments             - Arguments of the instruction
   * @param [in] instructionBulkNumber - Id marking when was code accepted
   */
  public SimCodeModel(InstructionFunctionModel instructionFunctionModel,
                      String instructionName,
                      String codeLine,
                      InstructionTypeEnum instructionTypeEnum,
                      DataTypeEnum dataTypeEnum,
                      List<InputCodeArgument> arguments,
                      int id,
                      int instructionBulkNumber)
  {
    this.inputCodeModel        = new InputCodeModel(instructionFunctionModel, instructionName, codeLine, arguments,
                                                    instructionTypeEnum, dataTypeEnum, 0);
    this.id                    = id;
    this.instructionBulkNumber = instructionBulkNumber;
    this.hasFailed             = false;
    // Copy arguments
    this.renamedArguments = new ArrayList<>();
    for (InputCodeArgument argument : arguments)
    {
      this.renamedArguments.add(new InputCodeArgument(argument));
    }
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @param [in] inputCodeModel - Original code model
   * @param [in] id             - Number marking when was code accepted
   * @brief Constructor which copies original InputCodeModel
   * This constructor can be used only through the SimCodeModelAllocator
   */
  public SimCodeModel(InputCodeModel inputCodeModel, int id, int instructionBulkNumber)
  {
    this.inputCodeModel        = inputCodeModel;
    this.id                    = id;
    this.instructionBulkNumber = instructionBulkNumber;
    this.hasFailed             = false;
    // Copy arguments
    this.renamedArguments = new ArrayList<>();
    for (InputCodeArgument argument : inputCodeModel.getArguments())
    {
      this.renamedArguments.add(new InputCodeArgument(argument));
    }
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @brief Comparator function for assigning to priorityQueue
   * @param [in] codeModel - Model to be compared to
   * @return -1 if <, 0 if ==, > if 1
   */
  @Override
  public int compareTo(@NotNull SimCodeModel codeModel)
  {
    return -Integer.compare(codeModel.getId(), this.id);
  }// end of compareTo
  //------------------------------------------------------
  
  /**
   * @brief Gets number marking when was code processed
   * @return Integer value of bulk number
   */
  public int getInstructionBulkNumber()
  {
    return instructionBulkNumber;
  }// end of getId
  //------------------------------------------------------
  
  /**
   * @brief Gets renamed code line
   * @return String with renamed code line
   */
  public String getRenamedCodeLine()
  {
    String[]      argsArray   = getCodeLine().split(" ");
    StringBuilder genericLine = new StringBuilder(argsArray[0] + " ");
    for (int i = 0; i < argsArray.length - 1; i++)
    {
      InputCodeArgument argument = getArguments().get(i);
      genericLine.append(argument.getValue()).append(" ");
    }
    return genericLine.toString().trim();
  }// end of getRenamedCodeLine
  //------------------------------------------------------
  
  /**
   * @brief Gets Id of the model
   * @return Id of the model
   */
  public int getId()
  {
    return id;
  }// end of getId
  //------------------------------------------------------
  
  /**
   * @brief Gets accepted id to issue window
   * @return Id, when was instruction accepted to issue window, 0 if not yet processed
   */
  public int getIssueWindowId()
  {
    return issueWindowId;
  }// end of getIssueWindowId
  //------------------------------------------------------
  
  /**
   * @brief Gets id of function block, which processed this instruction
   * @return Id of function block, which processed this instruction, 0 if not yet processed
   */
  public int getFunctionUnitId()
  {
    return functionUnitId;
  }// end of getFunctionUnitId
  //------------------------------------------------------
  
  /**
   * @brief Gets id of when was instruction's result ready
   * @return Id of when was instruction's result ready, 0 if not yet processed
   */
  public int getReadyId()
  {
    return readyId;
  }// end of getReadyId
  //------------------------------------------------------
  
  /**
   * @brief Gets id of when was instruction committed
   * @return Id of when was instruction committed, 0 if not yet processed
   */
  public int getCommitId()
  {
    return commitId;
  }// end of getCommitId
  //------------------------------------------------------
  
  
  public void setInstructionBulkNumber(int instructionBulkNumber)
  {
    this.instructionBulkNumber = instructionBulkNumber;
  }
  
  /**
   * @brief Sets accepted id to issue window
   * @param [in] windowId - Id, when was instruction accepted to issue window
   */
  public void setIssueWindowId(int windowId)
  {
    this.issueWindowId = windowId;
  }// end of setIssueWindowId
  //------------------------------------------------------
  
  /**
   * @brief Sets id of function block, which processed this instruction
   * @param [in] functionUnitId - Id of function block, which processed this instruction
   */
  public void setFunctionUnitId(int functionUnitId)
  {
    this.functionUnitId = functionUnitId;
  }// end of setFunctionUnitId
  //------------------------------------------------------
  
  /**
   * @brief Sets id of when was instruction's result ready
   * @param [in] readyId - Id of when was instruction's result ready
   */
  public void setReadyId(int readyId)
  {
    this.readyId = readyId;
  }// end of setReadyId
  //------------------------------------------------------
  
  /**
   * @brief Sets id of when was instruction's result ready
   * @param [in] commitId - Id of when was instruction committed
   */
  public void setCommitId(int commitId)
  {
    this.commitId = commitId;
  }// end of setCommitId
  //------------------------------------------------------
  
  /**
   * @brief Get bit value corresponding to failure due to wrong prediction
   * @return Boolean value marking failure to finish
   */
  public boolean hasFailed()
  {
    return hasFailed;
  }// end of hasFailed
  //------------------------------------------------------
  
  /**
   * @brief Set bit marking to failure due to wrong prediction
   * @param hasFailed - Has instruction failed to finish due to missprediction?
   */
  public void setHasFailed(boolean hasFailed)
  {
    this.hasFailed = hasFailed;
  }// end of setHasFailed
  
  @Override
  public String getInstructionName()
  {
    return inputCodeModel.getInstructionName();
  }
  
  @Override
  public String getCodeLine()
  {
    return inputCodeModel.getCodeLine();
  }
  
  /**
   * @brief Gets arguments of the instruction, copied so they are rewritable
   * @return Renamed arguments of the instruction
   */
  @Override
  public List<InputCodeArgument> getArguments()
  {
    return renamedArguments;
  }
  
  /**
   * @param name Name of the argument
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
  
  @Override
  public DataTypeEnum getResultDataType()
  {
    return inputCodeModel.getResultDataType();
  }
  
  @Override
  public int getCodeId()
  {
    return inputCodeModel.getCodeId();
  }// end of getId
  
  /**
   * @return Saved value of the PC, when instruction was fetched
   */
  public int getSavedPc()
  {
    return savedPc;
  }
  
  public void setSavedPc(int savedPc)
  {
    this.savedPc = savedPc;
  }
  
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
  
  public int getBranchTargetOffset()
  {
    return branchTargetOffset;
  }
  
  public void setBranchTargetOffset(int branchTargetOffset)
  {
    this.branchTargetOffset = branchTargetOffset;
  }
  
  @Override
  public InstructionFunctionModel getInstructionFunctionModel()
  {
    return inputCodeModel.getInstructionFunctionModel();
  }
  
  /**
   * String representation of the object
   */
  @Override
  public String toString()
  {
    return this.getRenamedCodeLine();
  }
}
