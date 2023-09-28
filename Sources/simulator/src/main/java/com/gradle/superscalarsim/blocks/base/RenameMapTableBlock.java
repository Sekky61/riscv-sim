/**
 * @file    RenameMapTableBlock.java
 *
 * @author  Jan Vavra \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xvavra20@fit.vutbr.cz
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for keeping mappings of registers
 *
 * @date  3  February   2021 16:00 (created) \n
 *        28 April      2021 11:30 (revised)
 * 26 Sep      2023 10:00 (revised)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2020  Jan Vavra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.gradle.superscalarsim.blocks.base;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.RegisterModel;
import com.gradle.superscalarsim.models.RenameMapModel;

import java.util.*;

/**
 * @class RenameMapTableBlock
 * @brief Class that keeps track of mappings between speculative and architectural registers
 *        and free speculative registers
 */
public class RenameMapTableBlock
{
  /// List of free speculative registers
  private final List<String> freeList;
  /// Map of speculative to architectural registers
  private final Map<String, RenameMapModel> registerMap;
  /// Map of references to certain speculative register
  private final Map<String, Integer> referenceMap;
  /// Class containing all registers, that simulator uses
  private UnifiedRegisterFileBlock registerFileBlock;

  /**
   * @brief Constructor - call initiateFreeList or clear before using
   */
  public RenameMapTableBlock()
  {
    this.freeList          = new ArrayList<>();
    this.registerMap       = new HashMap<>();
    this.referenceMap      = new HashMap<>();
    this.registerFileBlock = null;
  }

  /**
   * @brief Constructor
   * @param [in] loader - InitLoader class holding information about instruction and registers
   */
  public RenameMapTableBlock(UnifiedRegisterFileBlock registerFileBlock)
  {
    this.freeList          = new ArrayList<>();
    this.registerMap       = new HashMap<>();
    this.referenceMap      = new HashMap<>();
    this.registerFileBlock = registerFileBlock;

    initiateFreeList(registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative));
  }// end of Constructor
  //----------------------------------------------------------------------

  /**
   * @brief Clears all the active mappings
   */
  public void clear()
  {
    this.registerFileBlock.refreshRegisters();
    this.freeList.clear();
    this.registerMap.clear();
    this.referenceMap.clear();
    initiateFreeList(registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative));
  }// end of clear
  //----------------------------------------------------------------------

  /**
   * @brief Maps architectural register to free speculative one
   * @param [in] registerName - Name of the architectural register
   * @param [in] order        - Id specifying order between mappings to same register
   * @return Name of the speculative register, which is mapped to architectural
   */
  public String mapRegister(String registerName, int order)
  {
    String speculativeRegister = this.freeList.iterator().next();
    this.registerMap.put(speculativeRegister,new RenameMapModel(registerName, order));
    this.freeList.remove(speculativeRegister);
    this.referenceMap.put(speculativeRegister, 1);
    this.registerFileBlock.getRegister(speculativeRegister).setReadiness(RegisterReadinessEnum.kAllocated);
    return speculativeRegister;
  }// end of mapRegister
  //----------------------------------------------------------------------

  /**
   * @brief Argument overriden architectural register to direct speculative one
   * @param [in] registerName        - Name of the architectural register
   * @param [in] speculativeRegister - Name of the speculative register
   * @param [in] order               - Id specifying order between mappings to same register
   * @return Name of the speculative register, which is mapped to architectural
   */
  public String mapRegister(String registerName, String speculativeRegister, int order)
  {
    // TODO: merge with previous method
    this.registerMap.put(speculativeRegister,new RenameMapModel(registerName, order));
    this.freeList.remove(speculativeRegister);
    increaseReference(speculativeRegister);
    this.registerFileBlock.getRegister(speculativeRegister).setReadiness(RegisterReadinessEnum.kAllocated);
    return speculativeRegister;
  }// end of mapRegister
  //----------------------------------------------------------------------

  /**
   * @brief Increases number of references on certain speculative register
   * @param [in] speculativeRegister - Speculative register to be referenced
   */
  public void increaseReference(String speculativeRegister)
  {
    if(referenceMap.containsKey(speculativeRegister))
    {
      int currentRefCount = this.referenceMap.get(speculativeRegister) + 1;
      this.referenceMap.replace(speculativeRegister, currentRefCount);
    }
    else
    {
      this.referenceMap.put(speculativeRegister, 1);
    }
  }// end of increaseReference
  //----------------------------------------------------------------------

  /**
   * @brief Lowers speculative register reference count and eventually frees the register from registerMap
   * @param [in] speculativeRegister - Speculative speculativeRegister to be freed
   * @return True if the reference count reached 0, false otherwise (not found or above 0)
   */
  public boolean reduceReference(String speculativeRegister)
  {
    if(!this.registerMap.containsKey(speculativeRegister) || !this.referenceMap.containsKey(speculativeRegister))
    {
      return false;
    }
    int currentRefCount = this.referenceMap.get(speculativeRegister) - 1;
    this.referenceMap.replace(speculativeRegister, currentRefCount);
    return currentRefCount == 0;
  }// end of reduceReference
  //----------------------------------------------------------------------

  /**
   * @brief Copies value from speculative to architectural register and frees the mapping
   * @param [in] speculativeRegister - Register to copy from and free
   */
  public void copyAndFreeMapping(String speculativeRegister)
  {
    if(isSpeculativeRegister(speculativeRegister))
    {
      RenameMapModel architecturalRegister = this.registerMap.get(speculativeRegister);
      this.registerFileBlock.copyAndFree(speculativeRegister, architecturalRegister.getArchitecturalRegister());

      this.referenceMap.remove(speculativeRegister);
      this.registerMap.remove(speculativeRegister);
      this.freeList.add(speculativeRegister);
    }
  }// end of copyAndFreeMapping
  //----------------------------------------------------------------------

  /**
   * @brief Only frees the specified register
   * @param [in] speculativeRegister - Register to free
   */
  public void freeMapping(String speculativeRegister)
  {
    if(isSpeculativeRegister(speculativeRegister))
    {
      this.registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative).stream()
        .filter(reg -> reg.getName().equals(speculativeRegister))
        .findFirst().ifPresent(register ->
      {
        this.registerFileBlock.getRegister(speculativeRegister).setReadiness(RegisterReadinessEnum.kFree);
        this.referenceMap.remove(register.getName());
        this.registerMap.remove(register.getName());
        this.freeList.add(register.getName());
      });
    }
  }// end of freeMapping
  //----------------------------------------------------------------------

  /**
   * @brief Gives speculative register for mapped architectural one
   * @param [in] register - Speculative register
   * @return Architectural register
   */
  public String getMappingForRegister(String register)
  {
    if(this.registerFileBlock.isRegisterConstant(register))
    {
      return register;
    }
    return this.registerMap.entrySet().stream()
      .filter(entry -> register.equals(entry.getValue().getArchitecturalRegister())).sorted(Map.Entry.comparingByValue())
      .reduce((first, second) -> second).map(Map.Entry::getKey).orElse(register);
  }// end of getMappingForRegister
  //----------------------------------------------------------------------

  /**
   * @brief Get map containing current mappings
   * @return Map of all register mappings
   */
  public Map<String, RenameMapModel> getRegisterMap()
  {
    return registerMap;
  }// end of getMappings
  //----------------------------------------------------------------------

  /**
   * @brief Creates speculative registers in free list, where for each one architectural one creates one speculative
   * @param [in] registerMap - Set of all registers in unified register file
   */
  private void initiateFreeList(List<RegisterModel> registerModelList)
  {
    List<String> registerModelSubList = new ArrayList<>();
    for (RegisterModel register:registerModelList)
    {
      registerModelSubList.add(register.getName());
    }

    freeList.addAll(registerModelSubList);
  }// end of createSpeculativeRegisters
  //----------------------------------------------------------------------

  /**
   * @brief Checks if the provided register if speculative or not
   * @param [in] register - Name of the register to be checked
   * @return True if register is speculative, false otherwise
   */
  public boolean isSpeculativeRegister(String register)
  {
    return freeList.contains(register) || registerMap.containsKey(register);
  }// end of isSpeculativeRegister
  //----------------------------------------------------------------------

  /**
   * @brief Directly copy the value from speculative to the mapped one
   * @param [in] speculativeRegister - Name of the register to transfer value from
   */
  public void directCopyMapping(String speculativeRegister)
  {
    if(isSpeculativeRegister(speculativeRegister))
    {
      RenameMapModel architecturalRegister = this.registerMap.get(speculativeRegister);
      double value = this.registerFileBlock.getRegisterValue(speculativeRegister);
      this.registerFileBlock.setRegisterValue(architecturalRegister.getArchitecturalRegister(), value);
    }
  }// end of directCopyMapping
  //----------------------------------------------------------------------

  /**
   * @brief Get mapped architectural register of input speculative one
   * @param [in] speculativeRegister - Name of the speculative register
   */
  public String getMapping(String speculativeRegister)
  {
    if(isSpeculativeRegister(speculativeRegister))
    {
      RenameMapModel architecturalRegister = this.registerMap.get(speculativeRegister);
      return architecturalRegister.getArchitecturalRegister();
    }
    return null;
  }// end of getMapping
  //----------------------------------------------------------------------
}
