/**
 * @file RegisterSubloader.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains loader for register file from .json file
 * @date 27 October  2020 15:00 (created) \n
 * 21 November 2020 19:00 (revised)
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
package com.gradle.superscalarsim.loader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.models.RegisterFileModel;
import com.gradle.superscalarsim.models.RegisterModel;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @class RegisterSubloader
 * @brief Loads register file from .json file
 * @details Class which loads register file into an InstructionFunctionModel object from .json file thanks to Gson class
 */
public class RegisterSubloader
{
  
  /**
   * @brief Constructor
   */
  public RegisterSubloader()
  {
  }// end of Constructor
  //------------------------------------------------------
  
  /**
   * @brief Reads .json file and creates RegisterFileModel object from contents of the file
   * @param [in] filePath - Path to file containing register file in json format
   * @return Register file in RegisterFileModel object
   */
  public final RegisterFileModel loadRegisterFile(final String filePath)
  {
    RegisterFileModel registerFileModel = null;
    try
    {
      Gson   gson   = new Gson();
      Reader reader = Files.newBufferedReader(Paths.get(filePath));
      registerFileModel = gson.fromJson(reader, RegisterFileModel.class);
      // Arch. registers are assigned by default
      for (RegisterModel register : registerFileModel.getRegisterList())
      {
        register.setReadiness(RegisterReadinessEnum.kAssigned);
      }
      registerFileModel = validateModel(registerFileModel) ? registerFileModel : null;
    }
    catch (IOException | JsonSyntaxException e)
    {
      e.printStackTrace();
    }
    return registerFileModel;
  }// end of loadRegisterFile
  //------------------------------------------------------
  
  /**
   * @brief Validates loaded register file model
   * @param [in] model - Validated model
   * @return True if model is correct, false otherwise
   */
  private boolean validateModel(final RegisterFileModel model)
  {
    return model.getName() != null && model.getDataType() != null && validateRegisters(model.getRegisterList());
  }// end of validateModel
  //------------------------------------------------------
  
  /**
   * @brief Validates raw data of loaded model
   * @param [in] registerList - List of instruction format items
   * @return True if list is valid, false otherwise
   */
  private boolean validateRegisters(final List<RegisterModel> registerList)
  {
    return registerList.stream().allMatch(register -> register.getName() != null);
  }// end of validateRegisters
  //------------------------------------------------------
}
