/**
 * @file AbstractIssueWindowBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains abstract class for all Issue Windows
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
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;

import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * @class AbstractIssueWindowBlock
 * @brief Abstract class, containing interface and shared logic for all Issuing windows
 */
public abstract class AbstractIssueWindowBlock implements AbstractBlock {
    /// List of all instructions dispatched to this window
    private final List<SimCodeModel> issuedInstructions;

    /// Map of readiness of each instruction in window
    protected final Map<Integer, List<IssueItemModel>> argumentValidityMap;
    /// Initial loader of interpretable instructions and register files
    private InitLoader loader;
    /// Class containing all registers, that simulator uses
    private UnifiedRegisterFileBlock registerFileBlock;

    /// Stack for all instructions which failed in the instruction window
    protected final Stack<SimCodeModel> failedInstructions;
    /// Stack for the validity maps of the failed instructions
    protected final Stack<List<IssueItemModel>> failedValidityMaps;

    /// Id counter specifying which issue window did the instruction took
    protected int windowId;

    public AbstractIssueWindowBlock() {
        this.issuedInstructions = new ArrayList<>();
        this.argumentValidityMap = new HashMap<>();

        this.failedValidityMaps = new Stack<>();
        this.failedInstructions = new Stack<>();
    }

    /**
     * @param [in] blockScheduleTask - Task class, where blocks are periodically triggered by the GlobalTimer
     * @param [in] loader            - Initial loader of interpretable instructions and register files
     * @param [in] registerFileBlock - Class containing all registers, that simulator uses
     * @brief Constructor
     */
    public AbstractIssueWindowBlock(
                                    InitLoader loader,
                                    UnifiedRegisterFileBlock registerFileBlock) {
        this.issuedInstructions = new ArrayList<>();
        this.argumentValidityMap = new HashMap<>();

        this.loader = loader;
        this.registerFileBlock = registerFileBlock;

        this.failedValidityMaps = new Stack<>();
        this.failedInstructions = new Stack<>();
    }// end of Constructor
    //----------------------------------------------------------------------

    /**
     * @param [in] instruction - instruction to be issued
     * @return Suitable function unit
     * @brief Selects suitable function unit for certain instruction
     */
    public abstract AbstractFunctionUnitBlock selectSufficientFunctionUnit(InstructionFunctionModel instruction);

    /**
     * @param [in] instructionType - Type of the instruction (branch, arithmetic, eg.)
     * @return True if compatible, false otherwise
     * @brief Checks if provided instruction type is compatible with this window and its FUs
     */
    public abstract boolean isCorrectInstructionType(InstructionTypeEnum instructionType);

    /**
     * @param [in] dataType - Data type (int, float, eg.)
     * @return True if compatible, false otherwise
     * @brief Checks if provided data type is compatible with this window and its FUs
     */
    public abstract boolean isCorrectDataType(DataTypeEnum dataType);

    /**
     * @brief Set number of function units to function unit for correct id creation
     */
    public abstract void setFunctionUnitCountInUnits();

    /**
     * @return Issue Instruction list
     * @brief Gets Issued Instruction list
     */
    public List<SimCodeModel> getIssuedInstructions() {
        return issuedInstructions;
    }// end of getIssuedInstructions
    //----------------------------------------------------------------------

    /**
     * @param [in] renamedCodeLine - String of the renamed code line (id)
     * @return Validity map entry
     * @brief Get validity map entry for specified instruction
     */
    public List<IssueItemModel> getIssueItem(int codeModelId) {
        return this.argumentValidityMap.get(codeModelId);
    }// end of getIssueItem
    //----------------------------------------------------------------------

    /**
     * @brief Resets the all the lists/stacks/variables in the issue window
     */
    @Override
    public void reset() {
        this.issuedInstructions.clear();
        this.argumentValidityMap.clear();
        this.failedValidityMaps.clear();
        this.failedInstructions.clear();
    }// end of reset
    //----------------------------------------------------------------------

    /**
     * @brief Simulates issuing instructions to FUs
     * Shared behavior for all issue windows
     */
    @Override
    public void simulate() {
        checkForFailedInstructions();
        updateValidityItems();
        List<SimCodeModel> removedCodeList = new ArrayList<>();
        for (SimCodeModel currentModel : this.issuedInstructions) {
            // Can instruction be issued?
            if (!instructionReadyForIssue(currentModel)) {
                continue;
            }

            // Is any FU free?
            InstructionFunctionModel instruction = currentModel.getInstructionFunctionModel();
            AbstractFunctionUnitBlock functionUnitBlock = selectSufficientFunctionUnit(instruction);

            if (functionUnitBlock == null || functionUnitBlock.getSimCodeModel() != null) {
                continue;
            }

            // Instruction is ready for execution and there is free FU -> issue instruction
            currentModel.setIssueWindowId(this.windowId);
            functionUnitBlock.resetCounter();
            functionUnitBlock.setSimCodeModel(currentModel);
            removedCodeList.add(currentModel);
            this.argumentValidityMap.remove(currentModel.getId());
        }

        // Remove issued instructions from the window
        this.issuedInstructions.removeAll(removedCodeList);
        this.windowId = this.windowId + 1;
    }// end of simulate
    //----------------------------------------------------------------------

    /**
     * @return Window id
     * @brief Gets assigned id for this window
     */
    public int getWindowId() {
        return windowId;
    }// end of getWindowId
    //----------------------------------------------------------------------

    /**
     * @param [in] windowId - Id for this window
     * @brief Sets id for this window
     */
    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }// end of setWindowId
    //----------------------------------------------------------------------

    /**
     * @param [in] codeModel - Instruction to be added
     * @brief Adds new instruction to window list
     */
    public void dispatchInstruction(SimCodeModel codeModel) {
        this.issuedInstructions.add(codeModel);
    }// end of dispatchInstruction
    //----------------------------------------------------------------------

    /**
     * @param [in] codeModel - Instruction to be added to map
     * @brief Creates entry in argumentValidityMap
     */
    public void createArgumentValidityEntry(SimCodeModel codeModel) {
        List<IssueItemModel> itemModelList = new ArrayList<>();
        for (InputCodeArgument argument : codeModel.getArguments()) {
            if (argument.getName().startsWith("rs")) {
                String registerName = argument.getValue();
                RegisterModel reg = this.registerFileBlock.getRegister(registerName);
                RegisterReadinessEnum readiness = reg.getReadiness();
                boolean validity = readiness == RegisterReadinessEnum.kExecuted || readiness == RegisterReadinessEnum.kAssigned;
                itemModelList.add(new IssueItemModel(registerName, reg.getValue(), validity));
            } else if (argument.getName().startsWith("imm")) {
                try {
                    itemModelList.add(new IssueItemModel(argument.getName(), Double.parseDouble(argument.getValue()), true));
                } catch (NumberFormatException e) {
                }
            }
        }
        this.argumentValidityMap.put(codeModel.getId(), itemModelList);
    }// end of createArgumentValidityEntry
    //----------------------------------------------------------------------

    /**
     * @param [in] currentModel - Instruction to be checked
     * @return Trrue if instruction can be issued, false otherwise
     * @brief Checks if instruction is ready to be issued to FU
     */
    private boolean instructionReadyForIssue(SimCodeModel currentModel) {
        List<IssueItemModel> items = this.argumentValidityMap.get(currentModel.getId());
        boolean isReady = true;
        for (IssueItemModel item : items) {
            isReady &= item.isValidityBit();
        }
        return isReady;
    }// end of instructionReadyForIssue
    //----------------------------------------------------------------------

    /**
     * @brief Updates validity bits for every instruction in this window
     */
    protected void updateValidityItems() {
        for (List<IssueItemModel> itemList : argumentValidityMap.values()) {
            itemList.forEach(this::updateValidityItem);
        }
    }// end of updateValidityItems
    //----------------------------------------------------------------------

    /**
     * @brief Updates validity bits for certain instruction
     */
    private void updateValidityItem(IssueItemModel item) {
        if (item.getTag().equals("imm")) {
            return;
        }
        // TODO: maybe refactor this pattern into a function
        RegisterModel reg = this.registerFileBlock.getRegister(item.getTag());
        RegisterReadinessEnum readinessEnum = reg.getReadiness();
        if (readinessEnum == RegisterReadinessEnum.kExecuted || readinessEnum == RegisterReadinessEnum.kAssigned) {
            item.setValue(reg.getValue());
            item.setValidityBit(true);
        } else {
            item.setValue(0);
            item.setValidityBit(false);
        }
    }// end of updateValidityItem
    //----------------------------------------------------------------------

    /**
     * @brief Checks for instructions that were removed because of bad prediction
     */
    private void checkForFailedInstructions() {
        List<SimCodeModel> removedInstructions = new ArrayList<>();
        for (SimCodeModel codeModel : this.issuedInstructions) {
            if (codeModel.hasFailed()) {
                codeModel.setIssueWindowId(this.windowId);
                this.failedValidityMaps.push(this.argumentValidityMap.get(codeModel.getId()));
                this.failedInstructions.push(codeModel);
                this.argumentValidityMap.remove(codeModel.getId());
                removedInstructions.add(codeModel);
            }
        }
        this.issuedInstructions.removeAll(removedInstructions);
    }// end of checkForFailedInstructions
    //----------------------------------------------------------------------
}
