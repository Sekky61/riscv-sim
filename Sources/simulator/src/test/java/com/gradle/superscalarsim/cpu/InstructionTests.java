/**
 * @file    InstructionTests.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief   Tests of the whole CPU, executing a single instruction
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
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the whole CPU, executing a single instruction
 */
public class InstructionTests {

    private Cpu cpu;

    @Before
    public void setup() {
        cpu = new Cpu();
    }

    @Test
    public void testADDI() {
        // Setup + exercise
        Cpu cpu = ExecuteUtil.executeProgram("addi x1, x1, 5");

        // Assert
        Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
        Assert.assertEquals(5, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }

    @Test
    public void testADDI_negative() {
        // Setup + exercise
        Cpu cpu = ExecuteUtil.executeProgram("addi x1, x1, -14");

        // Assert
        Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
        Assert.assertEquals(-14, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }

    @Test
    public void testADDI_zeroReg() {
        // Setup + exercise
        Cpu cpu = ExecuteUtil.executeProgram("addi x0, x0, 10");

        // Assert
        Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x0"), 0.5);
    }

    @Test
    public void testSUB() {
        // Setup + exercise
        Cpu init = new Cpu();
        init.setDefaultState();
        init.cpuState.unifiedRegisterFileBlock.setRegisterValue("x1", 10.0);
        init.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 5.0);
        init.cpuState.unifiedRegisterFileBlock.setRegisterValue("x3", 3.0);
        Cpu cpu = ExecuteUtil.executeProgram("sub x1, x2, x3", init);

        // Assert
        Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }

    /**
     * JAL saves the address of the next instruction in the register, and adds the immediate to the address
     * Used as a call instruction
     */
    @Test
    public void testJAL() {
        // Setup + exercise
        Cpu cpu = ExecuteUtil.executeProgram("jal x1, 10");

        // Assert
        // TODO: actually, PC+4 should be saved, not PC+1
        Assert.assertEquals(10, cpu.cpuState.instructionFetchBlock.getPcCounter());
        Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }

    /**
     * JALR jumps to the address in the register rs1, and saves the address of the next instruction in the register rd
     */
    @Test
    public void testJALR() {
        // Setup + exercise
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x1", 10.0);
        cpu = ExecuteUtil.executeProgram("jalr x2, x1, 56", cpu);

        // Assert
        // Jumped to 10+56=66, and saved PC+4=4 in x2 (TODO: 1 for now)
        Assert.assertEquals(66, cpu.cpuState.instructionFetchBlock.getPcCounter());
        Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x2"), 0.5);
    }

    /**
     * XOR performs bitwise XOR on the two registers
     */
    @Test
    public void testXOR() {
        // Setup + exercise
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x1", 10.0);
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 5.0);
        cpu = ExecuteUtil.executeProgram("xor x3, x1, x2", cpu);

        // Assert
        // 1010 XOR 0101 = 1111
        Assert.assertEquals(15, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x3"), 0.5);
    }

    /**
     * BNE jumps if the two registers are not equal
     */
    @Test
    public void testBNE_notjump() {
        // Setup + exercise
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x1", 16.0);
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 16.0);
        cpu = ExecuteUtil.executeProgram("bne x1, x2, 10", cpu);

        // Assert
        Assert.assertEquals(1, cpu.cpuState.instructionFetchBlock.getPcCounter());
    }

    /**
     * BNE jumps if the two registers are not equal
     */
    @Test
    public void testBNE_jump() {
        // Setup + exercise
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x1", 1.0);
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 0.0);
        cpu = ExecuteUtil.executeProgram("bne x1, x2, 20", cpu);

        // Assert
        Assert.assertEquals(20, cpu.cpuState.instructionFetchBlock.getPcCounter());
    }

    /**
     * LH loads a half-word (2 bytes) from memory
     */
    @Test
    public void testLH() {
        // Setup + exercise
        cpu.cpuState.simulatedMemory.insertIntoMemory(0x5L, (byte) 0x10, 0);
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 0x5);
        // Load from address x2+0
        // Little endian so 0x10 is the first byte
        cpu = ExecuteUtil.executeProgram("lh x1, x2, 0", cpu);

        // Assert
        Assert.assertEquals(0x10, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }

    /**
     * LH loads a half-word (2 bytes) from memory
     */
    @Test
    public void testLH_little_endian() {
        // Setup + exercise
        cpu.cpuState.simulatedMemory.insertIntoMemory(0x5L, (byte) 0x01, 0);
        cpu.cpuState.unifiedRegisterFileBlock.setRegisterValue("x2", 0x4);
        // Load from address x2+0
        // Little endian so 0x10 is the high byte now
        cpu = ExecuteUtil.executeProgram("lh x1, x2, 0", cpu);

        // Assert
        Assert.assertEquals(0x0100, cpu.cpuState.unifiedRegisterFileBlock.getRegisterValue("x1"), 0.5);
    }
}
