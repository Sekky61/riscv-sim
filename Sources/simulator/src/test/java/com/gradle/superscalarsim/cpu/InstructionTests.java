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

import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of the whole CPU, executing a single instruction
 */
public class InstructionTests
{
  
  private CpuConfiguration cpuConfig;
  
  @Before
  public void setup()
  {
    cpuConfig = CpuConfiguration.getDefaultConfiguration();
  }
  
  @Test
  public void testADDI()
  {
    // Setup + exercise
    cpuConfig.code = "addi x1, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void testADDI_negative()
  {
    // Setup + exercise
    cpuConfig.code = "addi x1, x1, -5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-10);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(-15, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void testADDI_zeroReg()
  {
    // Setup + exercise
    cpuConfig.code = "addi x0, x0, 10";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x0").getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void testSUB()
  {
    // Setup + exercise
    cpuConfig.code = "sub x1, x2, x3";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * JAL saves the address of the next instruction in the register, and adds the immediate to the address
   * Used as a call instruction
   */
  @Test
  public void testJAL()
  {
    // Setup + exercise
    cpuConfig.code = "jal x1, 12";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(7, cpu.cpuState.tick);
    // PC+4=4 is saved in x1
    Assert.assertEquals(4, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * JALR jumps to the address in the register rs1, and saves the address of the next instruction in the register rd
   */
  @Test
  public void testJALR()
  {
    // Setup + exercise
    cpuConfig.code = "jalr x2, x8, 56";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x8").setValue(10);
    cpu.execute();
    
    // Assert
    // TODO: How to assert that it jumped to 10+56=66?
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getCommittedInstructions());
    Assert.assertEquals(4, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt),
                        0.5);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getAllBranches());
  }
  
  /**
   * XOR performs bitwise XOR on the two registers
   */
  @Test
  public void testXOR()
  {
    // Setup + exercise
    cpuConfig.code = "xor x3, x1, x2";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5);
    cpu.execute();
    
    // Assert
    // 1010 XOR 0101 = 1111
    Assert.assertEquals(15, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * XORI performs bitwise XOR on the register and the immediate
   */
  @Test
  public void testXORI()
  {
    // Setup + exercise
    cpuConfig.code = "xori x2, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(32);
    cpu.execute();
    
    // Assert
    // 100000 XOR 101 = 100101
    Assert.assertEquals(32 + 5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLL shifts the register left by the value of the second register
   */
  @Test
  public void testSLL()
  {
    // Setup + exercise
    cpuConfig.code = "sll x2, x1, x10";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2);
    cpu.execute();
    
    // Assert
    // 0001 << 2 = 0100
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLLI shifts the register left by the immediate
   */
  @Test
  public void testSLLI()
  {
    // Setup + exercise
    cpuConfig.code = "slli x2, x1, 2";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.execute();
    
    // Assert
    // 0001 << 2 = 0100
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SRL shifts the register right by the value of the second register
   */
  @Test
  public void testSRL()
  {
    // Setup + exercise
    cpuConfig.code = "srl x2, x1, x10\n" + "srl x3, x9, x10";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x9").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2);
    cpu.execute();
    
    // Assert
    // 1000 >> 2 = 0010
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    // 0010 >> 2 = 0000
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SRLI shifts the register right by the immediate.
   * Shift is logical - the sign bit is not preserved.
   */
  @Test
  public void testSRLI()
  {
    // Setup + exercise
    cpuConfig.code = "srli x2, x1, 2\nsrli x3, x5, 3";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9);
    int   x5                  = 0b11000000011000000000000000000000;
    float minusThreePointFive = Float.intBitsToFloat(x5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(minusThreePointFive);
    cpu.execute();
    
    // Assert
    // 1001 >> 2 = 0010
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0b00011000000011000000000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SRA shifts the register right by the value of the second register.
   * The sign bit is preserved.
   */
  @Test
  public void testSRA()
  {
    // Setup + exercise
    cpuConfig.code = "sra x2, x1, x10\n" + "sra x3, x9, x10";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x9").setValue(-1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x10").setValue(2);
    cpu.execute();
    
    // Assert
    // 1000 >> 2 = 1110
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    // 111...111 >> 2 = 111...111
    Assert.assertEquals(-1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SRAI shifts the register right by the immediate.
   * Shift is arithmetic - the sign bit is preserved.
   */
  @Test
  public void testSRAI()
  {
    // Setup + exercise
    cpuConfig.code = "srai x2, x1, 2\nsrai x3, x5, 3";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9);
    int x5 = 0b11111111111111111111111111000000;
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(x5);
    cpu.execute();
    
    // Assert
    // 1001 >> 2 = 1110
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0b11111111111111111111111111111000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLT sets the destination register to 1 if the first register is less than the second, 0 otherwise.
   * Signed operation.
   */
  @Test
  public void testSLT()
  {
    // Setup + exercise
    cpuConfig.code = "slt x4, x3, x2\n" + "slt x5, x2, x3";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(-3);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLTI sets the destination register to 1 if the first register is less than the immediate, 0 otherwise.
   * Signed operation.
   */
  @Test
  public void testSLTI()
  {
    // Setup + exercise
    cpuConfig.code = "slti x2, x1, 9\n" + "slti x3, x1, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLTU sets the destination register to 1 if the first register is less than the second, 0 otherwise.
   * Unsigned operation.
   */
  @Test
  public void testSLTU()
  {
    // Setup + exercise
    cpuConfig.code = "sltu x4, x3, x2\n" + "sltu x5, x2, x3";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(-3);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLTIU sets the destination register to 1 if the first register is less than the immediate, 0 otherwise.
   * Unsigned operation.
   */
  @Test
  public void testSLTIU()
  {
    // Setup + exercise
    cpuConfig.code = "sltiu x2, x1, 9\n" + "sltiu x3, x4, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(-1);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * ANDI performs bitwise AND on the register and the immediate
   */
  @Test
  public void testANDI()
  {
    // Setup + exercise
    cpuConfig.code = "andi x2, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.execute();
    
    // Assert
    // 10 AND 101 = 0
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * ORI performs bitwise OR on the register and the immediate
   */
  @Test
  public void testORI()
  {
    // Setup + exercise
    cpuConfig.code = "ori x2, x1, 5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(9);
    cpu.execute();
    
    // Assert
    // 1001 OR 101 = 1101
    Assert.assertEquals(13, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LUI loads the immediate into the upper 20 bits of the register. Fills the lower 12 bits with 0s.
   */
  @Test
  public void testLUI()
  {
    // Setup + exercise
    // 0b1100_0000_0000_0000_0001 == 0xC001
    cpuConfig.code = "lui x1, 0xC0010";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    // TODO: converting to doubles loses precision, so the value is not exactly 0xC0010000
    // Suggested fix: do not convert everything to doubles, but interpret byte arrays as ints or floats
    Assert.assertEquals(0b1100_0000_0000_0001_0000_000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LUI loads the immediate into the upper 20 bits of the register. Fills the lower 12 bits with 0s.
   */
  @Test
  public void testLUI_simple()
  {
    // Setup + exercise
    // 0b1100_0000_0000_0000_0001 == 0xC001
    cpuConfig.code = "lui x1, 1";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0b00000000000000000001_000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * AUIPC adds the shifted immediate to the PC and stores the result in the register.
   */
  @Test
  public void testAUIPC()
  {
    // Setup + exercise
    // PC is 0 here, 4 on the second instruction
    cpuConfig.code = "auipc x1, 0xaa\n" + "auipc x2, 0x1";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0xaa000, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0x1004, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LI loads the immediate into the register.
   * This is a pseudo-instruction.
   */
  @Test
  public void testLI()
  {
    // Setup + exercise
    cpuConfig.code = "li x1, 0xaa";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0xaa, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * MV moves the value of the second register into the first register.
   * This is a pseudo-instruction.
   */
  @Test
  public void testMV()
  {
    // Setup + exercise
    cpuConfig.code = "mv x1, x2";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(55);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(55, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * NEG negates the value (technically two's complement).
   * This is a pseudo-instruction.
   */
  @Test
  public void testNEG()
  {
    // Setup + exercise
    cpuConfig.code = "neg x1, x1";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(55);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-55, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SEQZ sets the register to 1 if the register is 0, 0 otherwise.
   * This is a pseudo-instruction.
   */
  @Test
  public void testSEQZ()
  {
    // Setup + exercise
    cpuConfig.code = "seqz x2, x1";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SNEZ sets the register to 1 if the register is not 0, 0 otherwise.
   * This is a pseudo-instruction.
   */
  @Test
  public void testSNEZ()
  {
    // Setup + exercise
    cpuConfig.code = "snez x2, x1\n" + "snez x3, x5";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SLTZ sets the register to 1 if the register is less than 0, 0 otherwise.
   * Signed operation, pseudo-instruction.
   */
  @Test
  public void testSLTZ()
  {
    // Setup + exercise
    cpuConfig.code = "sltz x2, x1\n" + "sltz x3, x5\n" + "sltz x4, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * SGTZ sets the register to 1 if the register is greater than 0, 0 otherwise.
   * Signed operation, pseudo-instruction.
   */
  @Test
  public void testSGTZ()
  {
    // Setup + exercise
    cpuConfig.code = "sgtz x2, x1\n" + "sgtz x3, x5\n" + "sgtz x4, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-6);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * BNE jumps if the two registers are not equal
   */
  @Test
  public void testBNE_notJump()
  {
    // Setup + exercise
    cpuConfig.code = "bne x1, x2, 12";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(16);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(16);
    cpu.execute();
    
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
    cpuConfig.code = "bne x1, x2, 20";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getAllBranches());
    // Prediction was not made
    Assert.assertEquals(0, cpu.cpuState.statisticsCounter.getCorrectlyPredictedBranches());
  }
  
  /**
   * BEQZ jumps if the register is 0
   */
  @Test
  public void testBEQZ()
  {
    // Setup + exercise
    cpuConfig.code = "beqz x1, 4 \n" + "beqz x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2);
    cpu.execute();
    
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
    cpuConfig.code = "bnez x1, 4 \n" + "bnez x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0);
    cpu.execute();
    
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
    cpuConfig.code = "blez x1, 4 \n" + "blez x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-1);
    cpu.execute();
    
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
    cpuConfig.code = "bgez x1, 4 \n" + "bgez x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1);
    cpu.execute();
    
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
    cpuConfig.code = "bltz x1, 4 \n" + "bltz x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-1);
    cpu.execute();
    
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
    cpuConfig.code = "bgtz x1, 4 \n" + "bgtz x2, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1);
    cpu.execute();
    
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
    cpuConfig.code = "bgt x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1);
    cpu.execute();
    
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
    cpuConfig.code = "ble x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2);
    cpu.execute();
    
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
    cpuConfig.code = "bgtu x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1);
    cpu.execute();
    
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
    cpuConfig.code = "bleu x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-2);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BNE jumps if the first register is not equal to the second
   */
  @Test
  public void testBNE()
  {
    // Setup + exercise
    cpuConfig.code = "bne x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BEQ jumps if the first register is equal to the second
   */
  @Test
  public void testBEQ()
  {
    // Setup + exercise
    cpuConfig.code = "beq x1, x2, 4\n" + "beq x1, x3, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(6);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGE jumps if the first register is greater than or equal to the second
   */
  @Test
  public void testBGE()
  {
    // Setup + exercise
    cpuConfig.code = "bge x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(1);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLT jumps if the first register is less than the second
   */
  @Test
  public void testBLT()
  {
    // Setup + exercise
    cpuConfig.code = "blt x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(-10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BLTU jumps if the first register is less than the second.
   */
  @Test
  public void testBLTU()
  {
    // Setup + exercise
    cpuConfig.code = "bltu x1, x2, 4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(10);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-2);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * BGEU jumps if the first register is greater than or equal to the second.
   */
  @Test
  public void testBGEU()
  {
    // Setup + exercise
    cpuConfig.code = "bgeu x1, x2, 4\n" + "bgeu x1, x3, 8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(-1);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * J jumps to the immediate
   */
  @Test
  public void testJ()
  {
    // Setup + exercise
    cpuConfig.code = "j 200";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertTrue(cpu.cpuState.instructionFetchBlock.getPc() > 200);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * JR jumps to the address in the register
   */
  @Test
  public void testJR()
  {
    // Setup + exercise
    cpuConfig.code = "jr x1";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(200);
    cpu.execute();
    
    // Assert
    Assert.assertTrue(cpu.cpuState.instructionFetchBlock.getPc() > 200);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * RET jumps to the address in the x1 register
   */
  @Test
  public void testRET()
  {
    // Setup + exercise
    cpuConfig.code = "ret";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(200);
    cpu.execute();
    
    // Assert
    Assert.assertTrue(cpu.cpuState.instructionFetchBlock.getPc() > 200);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * CALL jumps to the immediate and saves the return address in the x1 register
   */
  @Test
  public void testCALL()
  {
    // Setup + exercise
    cpuConfig.code = "call 200";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertTrue(cpu.cpuState.instructionFetchBlock.getPc() > 200);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
    Assert.assertEquals(4, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * TAIL jumps to the immediate. It changes  x6 to _part_of the address.
   * This is subtle behavior, that I would not expect to be used in practice, so it is not implemented.
   */
  @Test
  public void testTAIL()
  {
    // Setup + exercise
    cpuConfig.code = "tail 200";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.execute();
    
    // Assert
    Assert.assertTrue(cpu.cpuState.instructionFetchBlock.getPc() > 200);
    Assert.assertEquals(1, cpu.cpuState.statisticsCounter.getTakenBranches());
  }
  
  /**
   * MUL multiplies the two signed registers and stores the result (lower 32 bits) in the destination register
   */
  @Test
  public void testMUL()
  {
    // Setup + exercise
    cpuConfig.code = "mul x1, x2, x3\n" + "mul x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(4);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(-5);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(6, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt),
                        0.5);
    Assert.assertEquals(-20, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt),
                        0.5);
  }
  
  /**
   * MULH multiplies the two signed registers and stores the result (upper 32 bits) in the destination register
   */
  @Test
  public void testMULH()
  {
    // Setup + exercise
    cpuConfig.code = "mulh x1, x2, x3\n" + "mulh x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(Integer.MAX_VALUE);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(4);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    // 0111... << 2
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * MULHU multiplies the two unsigned registers and stores the result (upper 32 bits) in the destination register
   */
  @Test
  public void testMULHU()
  {
    // Setup + exercise
    cpuConfig.code = "mulhu x1, x2, x3\n" + "mulhu x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(-1);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(4);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(3, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * MULHSU multiplies the first signed and second unsigned registers and stores the result (upper 32 bits) in the
   * destination register
   */
  @Test
  public void testMULHSU()
  {
    // Setup + exercise
    cpuConfig.code = "mulhsu x1, x2, x3\n" + "mulhsu x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(-2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(2);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(-1);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    // Upper bits stayed zero
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * DIVU does unsigned division. Rounds towards zero.
   * TODO: Not verified with the spec. I suggest looking at GCC output which should be correct.
   */
  @Test
  public void testDIVU()
  {
    // Setup + exercise
    cpuConfig.code = "divu x1, x2, x3\n" + "divu x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(8);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(-1);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(2, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * REM does signed remainder.
   */
  @Test
  public void testREM()
  {
    // Setup + exercise
    cpuConfig.code = "rem x1, x2, x3\n" + "rem x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(9);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(-2);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * REMU does unsigned remainder.
   * TODO: Not verified with the spec. I suggest looking at GCC output which should be correct.
   */
  @Test
  public void testREMU()
  {
    // Setup + exercise
    cpuConfig.code = "remu x1, x2, x3\n" + "remu x4, x5, x6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(7);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").setValue(3);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x5").setValue(5);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x6").setValue(-2);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(5, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * FDIV.S does single precision floating point division
   */
  @Test
  public void testFDIV_S()
  {
    // Setup + exercise
    cpuConfig.code = "fdiv.s f1, f2, f3\n" + "fdiv.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f / 3.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f / -2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMUL.S does single precision floating point multiplication
   */
  @Test
  public void testFMUL_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmul.s f1, f2, f3\n" + "fmul.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f * 3.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f * -2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FSQRT.S does single precision floating point square root
   */
  @Test
  public void testFSQRT_S()
  {
    // Setup + exercise
    cpuConfig.code = "fsqrt.s f1, f2\n" + "fsqrt.s f3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(5.0f);
    cpu.execute();
    // TODO: test exception
    
    // Assert
    Assert.assertEquals((float) Math.sqrt(8.0f),
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals((float) Math.sqrt(5.0f),
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FSUB.S does single precision floating point subtraction
   */
  @Test
  public void testFSUB_S()
  {
    // Setup + exercise
    cpuConfig.code = "fsub.s f1, f2, f3\n" + "fsub.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f - 3.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f - -2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FADD.S does single precision floating point addition
   */
  @Test
  public void testFADD_S()
  {
    // Setup + exercise
    cpuConfig.code = "fadd.s f1, f2, f3\n" + "fadd.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f + 3.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f + -2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMIN.S does single precision floating point minimum
   */
  @Test
  public void testFMIN_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmin.s f1, f2, f3\n" + "fmin.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(3.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMAX.S does single precision floating point maximum
   */
  @Test
  public void testFMAX_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmax.s f1, f2, f3\n" + "fmax.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMADD.S performs a fused multiply addition
   */
  @Test
  public void testFMADD_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmadd.s f1, f2, f3, f4\n" + "fmadd.s f5, f6, f7, f8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f7").setValue(-2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f8").setValue(1.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f * 3.0f + 2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f * -2.0f + 1.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMSUB.S performs a fused multiply subtraction
   */
  @Test
  public void testFMSUB_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmsub.s f1, f2, f3, f4\n" + "fmsub.s f5, f6, f7, f8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f7").setValue(-2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f8").setValue(1.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f * 3.0f - 2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f * -2.0f - 1.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FNMADD.S performs a fused negative multiply addition
   */
  @Test
  public void testFNMADD_S()
  {
    // Setup + exercise
    cpuConfig.code = "fnmadd.s f1, f2, f3, f4\n" + "fnmadd.s f5, f6, f7, f8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f7").setValue(-2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f8").setValue(1.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-8.0f * 3.0f - 2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-(5.0f * -2.0f + 1.0f),
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FNMSUB.S performs a fused negative multiply subtraction
   */
  @Test
  public void testFNMSUB_S()
  {
    // Setup + exercise
    cpuConfig.code = "fnmsub.s f1, f2, f3, f4\n" + "fnmsub.s f5, f6, f7, f8";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f7").setValue(-2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f8").setValue(1.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-8.0f * 3.0f + 2.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-(5.0f * -2.0f - 1.0f),
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FSGNJ.S takes the sign from the second register and puts it on the first register
   */
  @Test
  public void testFSGNJ_S()
  {
    // Setup + exercise
    cpuConfig.code = "fsgnj.s f1, f2, f3\n" + "fsgnj.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(-3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FSGNJN.S takes the negated sign from the second register and puts it on the first register
   */
  @Test
  public void testFSGNJN_S()
  {
    // Setup + exercise
    cpuConfig.code = "fsgnjn.s f1, f2, f3\n" + "fsgnjn.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(-3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FSGNJX.S takes the xor of the signs and puts it on the first register
   */
  @Test
  public void testFSGNJX_S()
  {
    // Setup + exercise
    cpuConfig.code = "fsgnjx.s f1, f2, f3\n" + "fsgnjx.s f4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(-8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(-3.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(5.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(-2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMV.S moves the float
   */
  @Test
  public void testFMV_S()
  {
    // Setup + exercise
    cpuConfig.code = "fmv.s f1, f2\n" + "fmv.s f3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(-8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(5.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(-8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FABS.S takes the absolute value of the float
   */
  @Test
  public void testFABS_S()
  {
    // Setup + exercise
    cpuConfig.code = "fabs.s f1, f2\n" + "fabs.s f3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(-8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(5.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FNEG.S takes the negated value of the float
   */
  @Test
  public void testFNEG_S()
  {
    // Setup + exercise
    cpuConfig.code = "fneg.s f1, f2\n" + "fneg.s f3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(-8.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(5.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(8.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
    Assert.assertEquals(-5.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMV.W.X copies bits from the integer register to the float register
   */
  @Test
  public void testFMV_W_X()
  {
    // Setup + exercise
    cpuConfig.code = "fmv.w.x f1, x2\n" + "fmv.w.x f3, x4";
    Cpu cpu              = new Cpu(cpuConfig);
    int twentyEightFloat = 0b0_10000011_11000000000000000000000;
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(twentyEightFloat);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(28.0f,
                        (float) cpu.cpuState.unifiedRegisterFileBlock.getRegister("f1").getValue(DataTypeEnum.kFloat),
                        0.01);
  }
  
  /**
   * FMV.X.W copies bits from the float register to the integer register
   */
  @Test
  public void testFMV_X_W()
  {
    // Setup + exercise
    cpuConfig.code = "fmv.x.w x1, f2\n" + "fmv.x.w x3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(28.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0b0_10000011_11000000000000000000000,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * FLT.S sets the _integer_ register to 1 if the rs1 is less than the float register rs2
   */
  @Test
  public void testFLT_S()
  {
    // Setup + exercise
    cpuConfig.code = "flt.s x1, f2, f3\n" + "flt.s x4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(29.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(27.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * FLE.S sets the _integer_ register to 1 if the rs1 is less than or equal to the float register rs2
   */
  @Test
  public void testFLE_S()
  {
    // Setup + exercise
    cpuConfig.code = "fle.s x1, f2, f3\n" + "fle.s x4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(27.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * FEQ.S sets the _integer_ register to 1 if the rs1 is equal to the float register rs2
   */
  @Test
  public void testFEQ_S()
  {
    // Setup + exercise
    cpuConfig.code = "feq.s x1, f2, f3\n" + "feq.s x4, f5, f6";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f3").setValue(29.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f5").setValue(2.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f6").setValue(2.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(1, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * FCLASS.S classifies the float register (see {@link com.gradle.superscalarsim.code.Fclass}
   */
  @Test
  public void testFCLASS_S()
  {
    // Setup + exercise
    cpuConfig.code = "fclass.s x1, f2\n" + "fclass.s x3, f4";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f2").setValue(28.0f);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("f4").setValue(-0.0f);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0b1000000, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0b1010, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LB loads a byte from memory
   */
  @Test
  public void testLB()
  {
    // Setup + exercise
    cpuConfig.code = "lb x1, 0(x2)\n" + "lb x3, 2(x4)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(0xfe);
    cpu.cpuState.memoryModel.store(0x100, 0b11, 1, 0, 0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0b11, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0b11, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LH loads a half-word (2 bytes) from memory
   */
  @Test
  public void testLH()
  {
    // Setup + exercise
    // Load from address x2+0
    // Little endian so 0x10 is the first byte
    cpuConfig.code = "lh x1, 0(x2)\n" + "lh x3, 0(x4)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(0x101);
    cpu.cpuState.memoryModel.store(0x100, 0x10, 1, 0, 0);
    cpu.cpuState.memoryModel.store(0x102, 0xff, 1, 0, 0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x10, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0xffffff00,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LW loads a word (4 bytes) from memory
   */
  @Test
  public void testLW()
  {
    // Setup + exercise
    cpuConfig.code = "lw x1, 0(x2)\n" + "lw x3, 0(x4)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(0x103);
    cpu.cpuState.memoryModel.store(0x100, 0x12345678, 4, 0, 0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x12345678,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0x12, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LBU loads a byte from memory and zero extends it
   */
  @Test
  public void testLBU()
  {
    // Setup + exercise
    cpuConfig.code = "lbu x1, 0(x2)\n" + "lbu x3, 0(x4)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(0xff);
    cpu.cpuState.memoryModel.store(0x100, 255, 1, 0, 0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(255, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0, cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /**
   * LHU loads a half-word (2 bytes) from memory and zero extends it
   */
  @Test
  public void testLHU()
  {
    // Setup + exercise
    cpuConfig.code = "lhu x1, 0(x2)\n" + "lhu x3, 0(x4)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x4").setValue(0xff);
    cpu.cpuState.memoryModel.store(0x100, 0xffff, 2, 0, 0);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x0000ffff,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").getValue(DataTypeEnum.kInt));
    Assert.assertEquals(0x0000ff00,
                        cpu.cpuState.unifiedRegisterFileBlock.getRegister("x3").getValue(DataTypeEnum.kInt));
  }
  
  /*
   * LWU loads a word (4 bytes) from memory and zero extends it to *64 bits*
   * This is RV64I instruction
   * TODO: not implemented properly
   */
  
  /**
   * SB stores the low 8 bits of register to memory
   */
  @Test
  public void testSB()
  {
    // Setup + exercise
    cpuConfig.code = "sb x1, 0(x2)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0xff11);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x11, (long) cpu.cpuState.memoryModel.load(0x100, 1, 0, 0).getSecond());
    Assert.assertEquals(0x00, (long) cpu.cpuState.memoryModel.load(0x101, 1, 0, 0).getSecond());
  }
  
  /**
   * SH stores the low 16 bits of register to memory
   */
  @Test
  public void testSH()
  {
    // Setup + exercise
    cpuConfig.code = "sh x1, 0(x2)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0xff112233);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x33, (long) cpu.cpuState.memoryModel.load(0x100, 1, 0, 0).getSecond());
    Assert.assertEquals(0x22, (long) cpu.cpuState.memoryModel.load(0x101, 1, 0, 0).getSecond());
    Assert.assertEquals(0x00, (long) cpu.cpuState.memoryModel.load(0x102, 1, 0, 0).getSecond());
    Assert.assertEquals(0x00, (long) cpu.cpuState.memoryModel.load(0x103, 1, 0, 0).getSecond());
  }
  
  /**
   * SW stores the low 32 bits of register to memory
   */
  @Test
  public void testSW()
  {
    // Setup + exercise
    cpuConfig.code = "sw x1, 0(x2)";
    Cpu cpu = new Cpu(cpuConfig);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x1").setValue(0xff112233);
    cpu.cpuState.unifiedRegisterFileBlock.getRegister("x2").setValue(0x100);
    cpu.execute();
    
    // Assert
    Assert.assertEquals(0x33, (long) cpu.cpuState.memoryModel.load(0x100, 1, 0, 0).getSecond());
    Assert.assertEquals(0x22, (long) cpu.cpuState.memoryModel.load(0x101, 1, 0, 0).getSecond());
    Assert.assertEquals(0x11, (long) cpu.cpuState.memoryModel.load(0x102, 1, 0, 0).getSecond());
    Assert.assertEquals(0xff, (long) cpu.cpuState.memoryModel.load(0x103, 1, 0, 0).getSecond());
  }
}
