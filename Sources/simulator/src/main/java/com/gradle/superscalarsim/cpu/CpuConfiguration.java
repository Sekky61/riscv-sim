/**
 * @file CpuConfiguration.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief All the configuration for the CPU - can be used to create a CpuState
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

// The corresponding Typescript type is:
//
//type IsaConfig = {
//        robSize: number;
//        lbSize: number;
//        sbSize: number;
//        fetchWidth: number;
//        commitWidth: number;
//        btbSize: number;
//        phtSize: number;
//        predictorType: "1bit" | "2bit";
//        predictorDefault: "taken" | "not-taken";
//        fUnits: ({
//            id: number;
//            fuType: "L/S" | "Branch" | "Memory";
//            latency: number;
//            } | {
//            id: number;
//            fuType: "FX" | "FP";
//            latency: number;
//            operations: ("+" | "-" | "*" | "/" | "%" | "&" | "|" | ">>" | "<<" | ">>>" | "<" | ">" | "<=" | ">=" |
//            "==" | "!" | "++" | "--" | "#" | "<-" | "(" | ")")[];
//        })[];
//        cacheLines: number;
//        cacheLineSize: number;
//        cacheAssoc: number;
//        cacheReplacement: "LRU" | "FIFO" | "Random";
//        storeBehavior: "write-back";
//        storeLatency: number;
//        loadLatency: number;
//        laneReplacementDelay: number;
//        addRemainingDelay: boolean;
//    };

/**
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @class CpuConfiguration
 * @brief Class representing the CPU configuration
 * Can be used to create a CpuState
 */
public class CpuConfiguration implements Serializable
{
  /**
   * Code to run.
   * <p>
   * Must be part of the configuration because it is used to create the initial state
   */
  public String code;
  public int robSize;
  public int lbSize;
  public int sbSize;
  public int fetchWidth;
  /// Number of instructions that can be committed in one cycle - commitLimit on the ROB
  public int commitWidth;
  public int btbSize;
  public int phtSize;
  /**
   * One of 0bit, 1bit, 2bit
   */
  public String predictorType;
  /**
   * For zero bit one of "Taken", "Not Taken"
   * For one bit one of "Taken", "Not Taken"
   * For two bit one of "Strongly Not Taken", "Weakly Not Taken", "Weakly Taken", "Strongly Taken"
   */
  public String predictorDefault;
  public FUnit[] fUnits;
  public boolean useCache;
  public int cacheLines;
  public int cacheLineSize;
  public int cacheAssoc;
  public String cacheReplacement;
  public String storeBehavior;
  public int storeLatency;
  public int loadLatency;
  public int laneReplacementDelay;
  public boolean addRemainingDelay;
  
  public static CpuConfiguration getDefaultConfiguration()
  {
    CpuConfiguration config = new CpuConfiguration();
    config.code             = "";
    config.robSize          = 256;
    config.lbSize           = 64;
    config.sbSize           = 64;
    config.fetchWidth       = 3;
    config.commitWidth      = 4;
    config.btbSize          = 1024;
    config.phtSize          = 10;
    config.predictorType    = "2bit";
    config.predictorDefault = "Weakly Taken";
    config.fUnits           = new FUnit[5];
    // FX with all ops
    config.fUnits[0]            = new FUnit();
    config.fUnits[0].id         = 0;
    config.fUnits[0].fuType     = "FX";
    config.fUnits[0].latency    = 2;
    config.fUnits[0].operations = new String[]{"++", "--", "!", "#", "<-", "+", "-", "*", "/", "%", "&", "|", "^", ">>>", "<<", ">>", "<=", ">=", "==", "<", ">", "(", ")"};
    
    // FP with all ops
    config.fUnits[1]            = new FUnit();
    config.fUnits[1].id         = 1;
    config.fUnits[1].fuType     = "FP";
    config.fUnits[1].latency    = 2;
    config.fUnits[1].operations = new String[]{"++", "--", "!", "#", "<-", "+", "-", "*", "/", "%", "&", "|", "^", ">>>", "<<", ">>", "<=", ">=", "==", "<", ">", "(", ")"};
    
    // L/S
    config.fUnits[2]         = new FUnit();
    config.fUnits[2].id      = 2;
    config.fUnits[2].fuType  = "L/S";
    config.fUnits[2].latency = 1;
    
    // Branch
    config.fUnits[3]         = new FUnit();
    config.fUnits[3].id      = 3;
    config.fUnits[3].fuType  = "Branch";
    config.fUnits[3].latency = 2;
    
    // Memory
    config.fUnits[4]         = new FUnit();
    config.fUnits[4].id      = 4;
    config.fUnits[4].fuType  = "Memory";
    config.fUnits[4].latency = 1;
    
    config.useCache             = true;
    config.cacheLines           = 16;
    config.cacheLineSize        = 32;
    config.cacheAssoc           = 2;
    config.cacheReplacement     = "Random"; // TODO: Other policies have problem deserializing
    config.storeBehavior        = "write-back";
    config.storeLatency         = 0;
    config.loadLatency          = 1;
    config.laneReplacementDelay = 10;
    config.addRemainingDelay    = false;
    return config;
  }
  
  /**
   * @return ValidationResult
   * @brief Validates configuration
   */
  public ValidationResult validate()
  {
    // List of error messages
    ArrayList<String> errorMessages = new ArrayList<>();
    
    if (code == null)
    {
      errorMessages.add("Code must not be null");
    }
    // Null checks
    if (fUnits == null)
    {
      errorMessages.add("FUnits must not be null");
    }
    if (predictorType == null)
    {
      errorMessages.add("Predictor type must not be null");
    }
    if (predictorDefault == null)
    {
      errorMessages.add("Predictor default must not be null");
    }
    if (cacheReplacement == null)
    {
      errorMessages.add("Cache replacement must not be null");
    }
    if (storeBehavior == null)
    {
      errorMessages.add("Store behavior must not be null");
    }
    
    // ROB
    if (robSize < 1)
    {
      errorMessages.add("ROB size must be greater than 0");
    }
    
    // LB
    if (lbSize < 1)
    {
      errorMessages.add("LB size must be greater than 0");
    }
    
    // SB
    if (sbSize < 1)
    {
      errorMessages.add("SB size must be greater than 0");
    }
    
    // Fetch width
    if (fetchWidth < 1)
    {
      errorMessages.add("Fetch width must be greater than 0");
    }
    
    // Commit width
    if (commitWidth < 1)
    {
      errorMessages.add("Commit width must be greater than 0");
    }
    
    // BTB
    if (btbSize < 1)
    {
      errorMessages.add("BTB size must be greater than 0");
    }
    
    // PHT
    if (phtSize < 1)
    {
      errorMessages.add("PHT size must be greater than 0");
    }
    
    // FUnits
    if (fUnits.length < 1)
    {
      errorMessages.add("FUnits size must be greater than 0");
    }
    
    // Cache
    if (useCache)
    {
      
      // Cache line size
      if (cacheLineSize < 1)
      {
        errorMessages.add("Cache line size must be greater than 0");
      }
      
      // Cache lines
      if (cacheLines < 1)
      {
        errorMessages.add("Cache lines must be greater than 0");
      }
      
      // Cache assoc
      if (cacheAssoc < 1)
      {
        errorMessages.add("Cache assoc must be greater than 0");
      }
      
      // Lane replacement delay
      if (laneReplacementDelay < 0)
      {
        errorMessages.add("Lane replacement delay must be greater than 0");
      }
    }
    
    // Store latency
    if (storeLatency < 0)
    {
      errorMessages.add("Store latency must be greater than 0");
    }
    
    // Load latency
    if (loadLatency < 0)
    {
      errorMessages.add("Load latency must be greater than 0");
    }
    
    // TODO: move thorough validation
    
    // Allowed predictor types: 0bit, 1bit, 2bit
    if (!Objects.equals(predictorType, "0bit") && !Objects.equals(predictorType, "1bit") && !Objects.equals(
            predictorType, "2bit"))
    {
      errorMessages.add("Predictor type must be one of 0bit, 1bit, 2bit");
    }
    
    if (predictorType.equals("0bit"))
    {
      // Allowed predictor defaults: Taken, Not Taken
      if (!Objects.equals(predictorDefault, "Taken") && !Objects.equals(predictorDefault, "Not Taken"))
      {
        errorMessages.add("Predictor default form 0bit predictor must be one of Taken, Not Taken");
      }
    }
    
    if (predictorType.equals("1bit"))
    {
      // Allowed predictor defaults: Taken, Not Taken
      if (!Objects.equals(predictorDefault, "Taken") && !Objects.equals(predictorDefault, "Not Taken"))
      {
        errorMessages.add("Predictor default form 1bit predictor must be one of Taken, Not Taken");
      }
    }
    
    if (predictorType.equals("2bit"))
    {
      // Allowed predictor defaults: Strongly Not Taken, Weakly Not Taken, Weakly Taken, Strongly Taken
      if (!Objects.equals(predictorDefault, "Strongly Not Taken") && !Objects.equals(predictorDefault,
                                                                                     "Weakly Not " + "Taken") && !Objects.equals(
              predictorDefault, "Weakly Taken") && !Objects.equals(predictorDefault, "Strongly Taken"))
      {
        errorMessages.add(
                "Predictor default form 2bit predictor must be one of Strongly Not Taken, Weakly Not Taken," + " Weakly " + "Taken, Strongly Taken");
      }
    }
    
    // FU
    for (int i = 0; i < fUnits.length; i++)
    {
      FUnit fu = fUnits[i];
      if (fu.latency < 0)
      {
        errorMessages.add(String.format("FU %d: latency must be greater than 0", i));
      }
      
      switch (fu.fuType)
      {
        case "L/S", "Branch", "Memory" ->
        {
          if (fu.operations != null)
          {
            errorMessages.add(String.format("FU %d: %s FU must not have operations", i, fu.fuType));
          }
        }
        case "FX", "FP" ->
        {
          if (fu.operations == null)
          {
            errorMessages.add(String.format("FU %d: %s FU must have operations", i, fu.fuType));
          }
        }
        default -> errorMessages.add("Unknown FU type: " + fu.fuType);
      }
    }
    
    if (errorMessages.isEmpty())
    {
      return new ValidationResult(true, null);
    }
    else
    {
      return new ValidationResult(false, errorMessages);
    }
  }
  
  public static class FUnit
  {
    public int id;
    public String fuType;
    public int latency;
    public String[] operations;
    
    public FUnit()
    {
    
    }
    
    public FUnit(int id, String fuType, int latency, String[] operations)
    {
      this.id         = id;
      this.fuType     = fuType;
      this.latency    = latency;
      this.operations = operations;
    }
  }
  
  public static class ValidationResult
  {
    public boolean valid;
    public ArrayList<String> messages;
    
    public ValidationResult(boolean valid, ArrayList<String> messages)
    {
      this.valid    = valid;
      this.messages = messages;
    }
  }
}
