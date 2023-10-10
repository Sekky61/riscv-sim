/**
 * @file InstructionTests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests of the whole CPU, executing a single instruction
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the whole CPU, executing a single instruction
 */
public class InstructionTests
{
  
  private Cpu cpu;
  
  @Before
  public void setup()
  {
    cpu = new Cpu();
  }
  
  @Test
  public void testADDI()
  {
    // Setup + exercise
    Cpu cpu = ExecuteUtil.executeProgram("addi x1, x1, 5");
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  @Test
  public void testADDI_negative()
  {
    // Setup + exercise
    Cpu cpu = ExecuteUtil.executeProgram("addi x1, x1, -14");
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(-14, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  @Test
  public void testADDI_zeroReg()
  {
    // Setup + exercise
    Cpu cpu = ExecuteUtil.executeProgram("addi x0, x0, 10");
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(), 0.5);
  }
  
  @Test
  public void testSUB()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3.0);
    Cpu cpuAfter = ExecuteUtil.executeProgram("sub x1, x2, x3", cpu);
    
    // Assert
    Assert.assertEquals(2, cpuAfter.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * JAL saves the address of the next instruction in the register, and adds the immediate to the address
   * Used as a call instruction
   */
  @Test
  public void testJAL()
  {
    // Setup + exercise
    Cpu cpu = ExecuteUtil.executeProgram("jal x1, 12");
    
    // Assert
    Assert.assertEquals(7, cpu.cpuState.tick);
    // PC+4=4 is saved in x1
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * JALR jumps to the address in the register rs1, and saves the address of the next instruction in the register rd
   */
  @Test
  public void testJALR()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10.0);
    cpu = ExecuteUtil.executeProgram("jalr x2, x1, 56", cpu);
    
    // Assert
    // TODO: How to assert that it jumped to 10+56=66?
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getAllBranches());
  }
  
  /**
   * XOR performs bitwise XOR on the two registers
   */
  @Test
  public void testXOR()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5.0);
    cpu = ExecuteUtil.executeProgram("xor x3, x1, x2", cpu);
    
    // Assert
    // 1010 XOR 0101 = 1111
    Assert.assertEquals(15, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * XORI performs bitwise XOR on the register and the immediate
   */
  @Test
  public void testXORI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(32.0);
    cpu = ExecuteUtil.executeProgram("xori x2, x1, 5", cpu);
    
    // Assert
    // 100000 XOR 101 = 100101
    Assert.assertEquals(32 + 5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * SLL shifts the register left by the value of the second register
   */
  @Test
  public void testSLL()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("sll x2, x1, x10", cpu);
    
    // Assert
    // 0001 << 2 = 0100
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * SLLI shifts the register left by the immediate
   */
  @Test
  public void testSLLI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu = ExecuteUtil.executeProgram("slli x2, x1, 2", cpu);
    
    // Assert
    // 0001 << 2 = 0100
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * SRL shifts the register right by the value of the second register
   */
  @Test
  public void testSRL()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x9").setValue(2.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("srl x2, x1, x10\n" + "srl x3, x9, x10", cpu);
    
    // Assert
    // 1000 >> 2 = 0010
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    // 0010 >> 2 = 0000
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SRLI shifts the register right by the immediate.
   * Shift is logical - the sign bit is not preserved.
   */
  @Test
  public void testSRLI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9.0);
    int x5 = 0b11111111111111111111111111000000;
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue((double) x5);
    cpu = ExecuteUtil.executeProgram("srli x2, x1, 2\nsrli x3, x5, 3", cpu);
    
    // Assert
    // 1001 >> 2 = 0010
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(0b00011111111111111111111111111000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SRA shifts the register right by the value of the second register.
   * The sign bit is preserved.
   */
  @Test
  public void testSRA()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x9").setValue(-1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("sra x2, x1, x10\n" + "sra x3, x9, x10", cpu);
    
    // Assert
    // 1000 >> 2 = 1110
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    // 111...111 >> 2 = 111...111
    Assert.assertEquals(-1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SRAI shifts the register right by the immediate.
   * Shift is arithmetic - the sign bit is preserved.
   */
  @Test
  public void testSRAI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9.0);
    int x5 = 0b11111111111111111111111111000000;
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue((double) x5);
    cpu = ExecuteUtil.executeProgram("srai x2, x1, 2\nsrai x3, x5, 3", cpu);
    
    // Assert
    // 1001 >> 2 = 1110
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(0b11111111111111111111111111111000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SLT sets the destination register to 1 if the first register is less than the second, 0 otherwise.
   * Signed operation.
   */
  @Test
  public void testSLT()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(-3.0);
    cpu = ExecuteUtil.executeProgram("slt x4, x3, x2\n" + "slt x5, x2, x3", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(), 0.5);
  }
  
  /**
   * SLTI sets the destination register to 1 if the first register is less than the immediate, 0 otherwise.
   * Signed operation.
   */
  @Test
  public void testSLTI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu = ExecuteUtil.executeProgram("slti x2, x1, 9\n" + "slti x3, x1, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SLTU sets the destination register to 1 if the first register is less than the second, 0 otherwise.
   * Unsigned operation.
   */
  @Test
  public void testSLTU()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(-3.0);
    cpu = ExecuteUtil.executeProgram("sltu x4, x3, x2\n" + "sltu x5, x2, x3", cpu);
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(), 0.5);
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(), 0.5);
  }
  
  /**
   * SLTIU sets the destination register to 1 if the first register is less than the immediate, 0 otherwise.
   * Unsigned operation.
   */
  @Test
  public void testSLTIU()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(-1.0);
    cpu = ExecuteUtil.executeProgram("sltiu x2, x1, 9\n" + "sltiu x3, x4, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * ANDI performs bitwise AND on the register and the immediate
   */
  @Test
  public void testANDI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("andi x2, x1, 5", cpu);
    
    // Assert
    // 10 AND 101 = 0
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * ORI performs bitwise OR on the register and the immediate
   */
  @Test
  public void testORI()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9.0);
    cpu = ExecuteUtil.executeProgram("ori x2, x1, 5", cpu);
    
    // Assert
    // 1001 OR 101 = 1101
    Assert.assertEquals(13, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * LUI loads the immediate into the upper 20 bits of the register. Fills the lower 12 bits with 0s.
   */
  @Test
  public void testLUI()
  {
    // Setup + exercise
    // 0b1100_0000_0000_0000_0001 == 0xC001
    cpu = ExecuteUtil.executeProgram("lui x1, 0xC001", cpu);
    
    // Assert
    // TODO: converting to doubles loses precision, so the value is not exactly 0xC0010000
    // Suggested fix: do not convert everything to doubles, but interpret byte arrays as ints or floats
    Assert.assertEquals(0b11000000000000000001_000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * LUI loads the immediate into the upper 20 bits of the register. Fills the lower 12 bits with 0s.
   */
  @Test
  public void testLUI_simple()
  {
    // Setup + exercise
    // 0b1100_0000_0000_0000_0001 == 0xC001
    cpu = ExecuteUtil.executeProgram("lui x1, 1", cpu);
    
    // Assert
    Assert.assertEquals(0b00000000000000000001_000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * AUIPC adds the shifted immediate to the PC and stores the result in the register.
   */
  @Test
  public void testAUIPC()
  {
    // Setup + exercise
    // PC is 0 here, 4 on the second instruction
    cpu = ExecuteUtil.executeProgram("auipc x1, 0xaa\n" + "auipc x2, 0x1", cpu);
    
    // Assert
    Assert.assertEquals(0xaa000, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
    Assert.assertEquals(0x1004, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * LI loads the immediate into the register.
   * This is a pseudo-instruction.
   */
  @Test
  public void testLI()
  {
    // Setup + exercise
    cpu = ExecuteUtil.executeProgram("li x1, 0xaa", cpu);
    
    // Assert
    Assert.assertEquals(0xaa, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * MV moves the value of the second register into the first register.
   * This is a pseudo-instruction.
   */
  @Test
  public void testMV()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(55);
    cpu = ExecuteUtil.executeProgram("mv x1, x2", cpu);
    
    // Assert
    Assert.assertEquals(55, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * NEG negates the value (technically two's complement).
   * This is a pseudo-instruction.
   */
  @Test
  public void testNEG()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(55);
    cpu = ExecuteUtil.executeProgram("neg x1, x1", cpu);
    
    // Assert
    Assert.assertEquals(-55, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * SEQZ sets the register to 1 if the register is 0, 0 otherwise.
   * This is a pseudo-instruction.
   */
  @Test
  public void testSEQZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu = ExecuteUtil.executeProgram("seqz x2, x1", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
  }
  
  /**
   * SNEZ sets the register to 1 if the register is not 0, 0 otherwise.
   * This is a pseudo-instruction.
   */
  @Test
  public void testSNEZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu = ExecuteUtil.executeProgram("snez x2, x1\n" + "snez x3, x5", cpu);
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
  }
  
  /**
   * SLTZ sets the register to 1 if the register is less than 0, 0 otherwise.
   * Signed operation, pseudo-instruction.
   */
  @Test
  public void testSLTZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(0);
    cpu = ExecuteUtil.executeProgram("sltz x2, x1\n" + "sltz x3, x5\n" + "sltz x4, x6", cpu);
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(), 0.5);
  }
  
  /**
   * SGTZ sets the register to 1 if the register is greater than 0, 0 otherwise.
   * Signed operation, pseudo-instruction.
   */
  @Test
  public void testSGTZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(0);
    cpu = ExecuteUtil.executeProgram("sgtz x2, x1\n" + "sgtz x3, x5\n" + "sgtz x4, x6", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(), 0.5);
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(), 0.5);
  }
  
  /**
   * BNE jumps if the two registers are not equal
   */
  @Test
  public void testBNE_notJump()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(16.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(16.0);
    cpu = ExecuteUtil.executeProgram("bne x1, x2, 12", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getAllBranches());
    // Default prediction is to jump, but there is not a BTB entry for this branch, so we couldn't predict
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCorrectlyPredictedBranches());
  }
  
  /**
   * BNE jumps if the two registers are not equal
   */
  @Test
  public void testBNE_jump()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0.0);
    cpu = ExecuteUtil.executeProgram("bne x1, x2, 20", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getAllBranches());
    // Prediction was not made
    Assert.assertEquals(0, cpu.cpuState.statisticsCounter.getCorrectlyPredictedBranches());
  }
  
  /**
   * LH loads a half-word (2 bytes) from memory
   */
  @Test
  public void testLH()
  {
    // Setup + exercise
    cpu.cpuState.simulatedMemory.insertIntoMemory(0x5L, (byte) 0x10, 0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x5);
    // Load from address x2+0
    // Little endian so 0x10 is the first byte
    cpu = ExecuteUtil.executeProgram("lh x1, x2, 0", cpu);
    
    // Assert
    Assert.assertEquals(0x10, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * LH loads a half-word (2 bytes) from memory
   */
  @Test
  public void testLH_little_endian()
  {
    // Setup + exercise
    cpu.cpuState.simulatedMemory.insertIntoMemory(0x5L, (byte) 0x01, 0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x4);
    // Load from address x2+0
    // Little endian so 0x10 is the high byte now
    cpu = ExecuteUtil.executeProgram("lh x1, x2, 0", cpu);
    
    // Assert
    Assert.assertEquals(0x0100, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(), 0.5);
  }
  
  /**
   * BEQZ jumps if the register is 0
   */
  @Test
  public void testBEQZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("beqz x1, 4 \n" + "beqz x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BNEZ jumps if the register is not 0
   */
  @Test
  public void testBNEZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0.0);
    cpu = ExecuteUtil.executeProgram("bnez x1, 4 \n" + "bnez x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLEZ jumps if the register is less than or equal to 0
   */
  @Test
  public void testBLEZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-1.0);
    cpu = ExecuteUtil.executeProgram("blez x1, 4 \n" + "blez x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGEZ jumps if the register is greater than or equal to 0
   */
  @Test
  public void testBGEZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1.0);
    cpu = ExecuteUtil.executeProgram("bgez x1, 4 \n" + "bgez x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLTZ jumps if the register is less than 0
   */
  @Test
  public void testBLTZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-1.0);
    cpu = ExecuteUtil.executeProgram("bltz x1, 4 \n" + "bltz x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGTZ jumps if the register is greater than 0
   */
  @Test
  public void testBGTZ()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-1.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1.0);
    cpu = ExecuteUtil.executeProgram("bgtz x1, 4 \n" + "bgtz x2, 8", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGT jumps if the first register is greater than the second
   */
  @Test
  public void testBGT()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1.0);
    cpu = ExecuteUtil.executeProgram("bgt x1, x2, 4", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLE jumps if the first register is less than or equal to the second
   */
  @Test
  public void testBLE()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-10.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2.0);
    cpu = ExecuteUtil.executeProgram("ble x1, x2, 4", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGTU jumps if the first register is greater than the second
   */
  @Test
  public void testBGTU()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1.0);
    cpu = ExecuteUtil.executeProgram("bgtu x1, x2, 4", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLEU jumps if the first register is less than or equal to the second.
   * Unsigned operation.
   */
  @Test
  public void testBLEU()
  {
    // Setup + exercise
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10.0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-2.0);
    cpu = ExecuteUtil.executeProgram("bleu x1, x2, 4", cpu);
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
}
