/**
 * @file RenameMapTableBlock.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for keeping mappings of registers
 * @date 3  February   2021 16:00 (created) \n
 * 28 April      2021 11:30 (revised)
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
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.RenameMapModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @class RenameMapTableBlock
 * @brief Class that keeps track of mappings between speculative and architectural registers
 * and free speculative registers
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public class RenameMapTableBlock
{
  /**
   * List of free speculative registers
   */
  private final List<String> freeList;
  /**
   * Map of speculative to architectural registers
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final Map<String, RenameMapModel> registerMap;
  /**
   * Map of references to certain speculative register
   */
  private final Map<String, Integer> referenceMap;
  /**
   * Class containing all registers, that simulator uses
   */
  @JsonIdentityReference(alwaysAsId = true)
  private final UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * @brief Constructor call initiateFreeList or clear before using
   */
  public RenameMapTableBlock()
  {
    this.freeList          = new ArrayList<>();
    this.registerMap       = new TreeMap<>();
    this.referenceMap      = new TreeMap<>();
    this.registerFileBlock = null;
  }
  
  /**
   * @param registerFileBlock Registers
   *
   * @brief Constructor
   */
  public RenameMapTableBlock(UnifiedRegisterFileBlock registerFileBlock)
  {
    this.freeList = new ArrayList<>();
    // TreeMaps to keep the order of keys - important for comparing serialized forms
    this.registerMap       = new TreeMap<>();
    this.referenceMap      = new TreeMap<>();
    this.registerFileBlock = registerFileBlock;
    
    initiateFreeList(registerFileBlock.getSpeculativeRegisterFile().getRegisterCount());
  }// end of Constructor
  //----------------------------------------------------------------------
  
  /**
   * @param specRegistersCount Number of speculative registers to create in free list
   *
   * @brief Creates speculative registers in free list, where for each one architectural one creates one speculative
   */
  private void initiateFreeList(int specRegistersCount)
  {
    for (int i = 0; i < specRegistersCount; i++)
    {
      this.freeList.add("tg" + i);
    }
  }// end of createSpeculativeRegisters
  //----------------------------------------------------------------------
  
  /**
   * @brief Clears all the active mappings
   */
  public void clear()
  {
    // Broken clear. TODO: remove all clearing and resetting. We have CpuConfiguration for that
    //    this.registerFileBlock.refreshRegisters();
    this.freeList.clear();
    this.registerMap.clear();
    this.referenceMap.clear();
    //    initiateFreeList(registerFileBlock.getSpeculativeRegisterFile().getRegisterList());
  }// end of clear
  //----------------------------------------------------------------------
  
  /**
   * @param registerName Name of the architectural register
   * @param order        Id specifying order between mappings to same register
   *
   * @return Reference to the speculative register
   * @brief Maps architectural register to free speculative one
   */
  public RegisterModel mapRegister(String registerName, int order)
  {
    // TODO: what if there is no free tag or free register in the field? Currently it throws exception
    String speculativeRegister = this.freeList.iterator().next();
    this.registerMap.put(speculativeRegister, new RenameMapModel(registerName, order));
    this.freeList.remove(speculativeRegister);
    this.referenceMap.put(speculativeRegister, 1);
    this.registerFileBlock.getRegister(speculativeRegister).setReadiness(RegisterReadinessEnum.kAllocated);
    return registerFileBlock.getRegister(speculativeRegister);
  }// end of mapRegister
  //----------------------------------------------------------------------
  
  /**
   * @param speculativeRegister Speculative register to be referenced
   *
   * @brief Increases number of references on certain speculative register
   */
  public void increaseReference(String speculativeRegister)
  {
    if (referenceMap.containsKey(speculativeRegister))
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
   * @param speculativeRegister Speculative speculativeRegister to be freed
   *
   * @return True if the reference count reached 0, false otherwise (not found or above 0)
   * @brief Lowers speculative register reference count and eventually frees the register from registerMap
   */
  public boolean reduceReference(String speculativeRegister)
  {
    if (!this.registerMap.containsKey(speculativeRegister) || !this.referenceMap.containsKey(speculativeRegister))
    {
      return false;
    }
    int currentRefCount = this.referenceMap.get(speculativeRegister) - 1;
    this.referenceMap.replace(speculativeRegister, currentRefCount);
    return currentRefCount == 0;
  }// end of reduceReference
  //----------------------------------------------------------------------
  
  /**
   * @param speculativeRegister Register to free
   *
   * @brief Only frees the specified register
   */
  public void freeMapping(String speculativeRegister)
  {
    if (!isSpeculativeRegister(speculativeRegister))
    {
      return;
    }
    
    RegisterModel specRegister = this.registerFileBlock.getRegister(speculativeRegister);
    if (specRegister == null)
    {
      throw new RuntimeException("Speculative register " + speculativeRegister + " not found");
    }
    
    this.registerFileBlock.getRegister(speculativeRegister).setReadiness(RegisterReadinessEnum.kFree);
    this.referenceMap.remove(specRegister.getName());
    this.registerMap.remove(specRegister.getName());
    this.freeList.add(specRegister.getName());
    
  }// end of freeMapping
  //----------------------------------------------------------------------
  
  /**
   * @param register Name of the register to be checked
   *
   * @return True if register is speculative, false otherwise
   * @brief Checks if the provided register if speculative or not
   */
  public boolean isSpeculativeRegister(String register)
  {
    return freeList.contains(register) || registerMap.containsKey(register);
  }// end of isSpeculativeRegister
  //----------------------------------------------------------------------
  
  /**
   * @param register architectural register
   *
   * @return Reference to a register that is currently mapped to `register`
   */
  public RegisterModel getMappingForRegister(String register)
  {
    if (this.registerFileBlock.getRegister(register).isConstant())
    {
      return registerFileBlock.getRegister(register);
    }
    // Iterate all renames, get the freshest rename
    String newestMapping = register;
    int    newestOrder   = -1;
    for (Map.Entry<String, RenameMapModel> entry : this.registerMap.entrySet())
    {
      if (entry.getValue().getArchitecturalRegister().equals(register) && entry.getValue().getOrder() > newestOrder)
      {
        newestMapping = entry.getKey();
        newestOrder   = entry.getValue().getOrder();
      }
    }
    return registerFileBlock.getRegister(newestMapping);
  }// end of getMappingForRegister
  //----------------------------------------------------------------------
  
  /**
   * @param speculativeRegister Name of the register to transfer value from
   *
   * @brief Directly copy the value from speculative to the mapped one
   */
  public void directCopyMapping(String speculativeRegister)
  {
    if (isSpeculativeRegister(speculativeRegister))
    {
      String        architecturalRegister = this.registerMap.get(speculativeRegister).getArchitecturalRegister();
      RegisterModel register              = this.registerFileBlock.getRegister(speculativeRegister);
      long          value                 = (long) register.getValue(DataTypeEnum.kLong);
      this.registerFileBlock.getRegister(architecturalRegister).setValue(value);
    }
  }// end of directCopyMapping
  //----------------------------------------------------------------------
  
  /**
   * @return Number of allocated speculative registers
   */
  public int getAllocatedSpeculativeRegistersCount()
  {
    return this.registerFileBlock.getSpeculativeRegisterFile().getRegisterCount() - this.freeList.size();
  }
}
