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

import com.gradle.superscalarsim.app.MyLogger;
import com.gradle.superscalarsim.compiler.GccCaller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @class ConfigLoader
 * @brief loading configuration from file. Based on Properties. Should run before any other class.
 */
public class ConfigLoader
{
  static Logger logger = MyLogger.initializeLogger("ConfigLoader", Level.INFO);
  public static String gccPath;
  public static String registerFileDirPath;
  public static Integer serverTimeoutMs;
  
  /*
    Load configuration from file depending on dev/prod profile.
    Runs before the CLI entry point, so it can rewrite these values.
    Currently, it is here for tests, other usecases should use CLI params and not config file.
   */
  static
  {
    String profile = System.getProperty("config.profile", "dev");
    logger.info("Loading configuration for profile: " + profile);
    logger.info("All properties: " + System.getProperties());
    String configFileName = "/config_" + profile + ".properties";
    // This is a path to a resource file, not an ordinary file
    try (InputStream input = ConfigLoader.class.getResourceAsStream(configFileName))
    {
      Properties prop = new Properties();
      
      // load a properties file
      prop.load(input);
      
      // Set properties as system properties
      prop.forEach((key, value) -> System.setProperty((String) key, (String) value));
      
      gccPath = prop.getProperty("gcc.path");
      logger.info("call to GccCaller.setCompilerPath(" + gccPath + ")");
      GccCaller.setCompilerPath(gccPath);
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }
}
