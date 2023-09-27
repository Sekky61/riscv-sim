/**
 * @file    ConfigurationTests.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Tests for the CpuConfiguration class
 *
 * @date  26 Sep      2023 10:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023 Michal Majer
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
 
package com.gradle.superscalarsim.cpu;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTests {

    @Test
    public void testDefaultConfiguration_Passes() {
        CpuConfiguration config = CpuConfiguration.getDefaultConfiguration();
        Assert.assertTrue(config.validate().valid);
    }

    @Test
    public void testNoFus_Fails() {
        CpuConfiguration config = CpuConfiguration.getDefaultConfiguration();
        config.fUnits = new CpuConfiguration.FUnit[0];
        Assert.assertFalse(config.validate().valid);
    }

    @Test
    public void testPredictors() {
        CpuConfiguration config = CpuConfiguration.getDefaultConfiguration();
        // A bad combination of predictors
        config.predictorType = "2bit";
        config.predictorDefault = "Taken";

        Assert.assertFalse(config.validate().valid);
    }

    @Test
    public void testDefaultConfig_BuildsState() {
        CpuConfiguration config = CpuConfiguration.getDefaultConfiguration();
        Cpu cpu = new Cpu(config);
        Assert.assertNotNull(cpu.cpuState);
    }
}
