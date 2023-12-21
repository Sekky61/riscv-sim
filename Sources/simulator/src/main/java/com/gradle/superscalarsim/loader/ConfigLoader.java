/**
 * @file ConfigLoader.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains class for loading configuration from file
 * @date 30 Nov      2023 9:00 (created)
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


package com.gradle.superscalarsim.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @class ConfigLoader
 * @brief loading configuration from file. Based on Properties. Should run before any other class.
 */
public class ConfigLoader
{
  public static String gccPath;
  public static String registerFileDirPath;
  public static String instructionsFilePath;
  public static String registerAliasesFilePath;
  
  static
  {
    String profile = System.getProperty("config.profile", "dev");
    
    String configFileName = "config_" + profile + ".properties";
    System.err.println("ConfigLoader: loading " + configFileName);
    try (InputStream input = new FileInputStream(configFileName))
    {
      Properties prop = new Properties();
      
      // load a properties file
      prop.load(input);
      
      gccPath                 = prop.getProperty("gcc.path");
      registerFileDirPath     = prop.getProperty("registerFileDir.path");
      instructionsFilePath    = prop.getProperty("instructionsFile.path");
      registerAliasesFilePath = prop.getProperty("registerAliasesFile.path");
      System.err.println("ConfigLoader: gccPath = " + gccPath);
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }
}
