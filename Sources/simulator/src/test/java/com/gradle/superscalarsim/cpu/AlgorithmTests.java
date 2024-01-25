/**
 * @file Algorithmtests.java
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief Tests of some C algorithms
 * @date 23 Jan      2024 10:00 (created)
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

import com.gradle.superscalarsim.compiler.AsmParser;
import com.gradle.superscalarsim.compiler.CompiledProgram;
import com.gradle.superscalarsim.compiler.GccCaller;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class AlgorithmTests
{
  static String recursiveFactorialCode = """
          int main() {
              int num = 2;
              int result = factorial(num);
              return result;
          }
          
          int factorial(int n) {
              // Base case: factorial of 0 is 1
              if (n == 0 || n == 1) {
                  return 1;
              } else {
                  // Recursive case: n! = n * (n-1)!
                  return n * factorial(n - 1);
              }
          }
          """;
  
  static String quickSortCode = """
          
          // NOT the same as `extern char *arr`;
          extern char arr[];
          
          // Starts here
          int main() {
              int size = 16;
              quicksort(arr, 0, size - 1);
              return 0;
          }
          
          void swap(char *a, char *b) {
              char temp = *a;
              *a = *b;
              *b = temp;
          }
          
          int partition(char arr[], int low, int high) {
              char pivot = arr[high];
              int i = low - 1;
              
              for (int j = low; j < high; j++) {
                  if (arr[j] <= pivot) {
                      i++;
                      swap(&arr[i], &arr[j]);
                  }
              }
              
              swap(&arr[i + 1], &arr[high]);
              return i + 1;
          }
          
          void quicksort(char arr[], int low, int high) {
              if (low < high) {
                  int pivotIndex = partition(arr, low, high);
                  
                  quicksort(arr, low, pivotIndex - 1);
                  quicksort(arr, pivotIndex + 1, high);
              }
          }
          """;
  
  static String quicksortAssembly = """
          main:
              addi sp,sp,-16
              li a2,15
              li a1,0
              la a0,arr
              sw ra,12(sp)
              call quicksort
              lw ra,12(sp)
              li a0,0
              addi sp,sp,16
              jr ra
          swap:
              lbu a4,0(a1)
              lbu a5,0(a0)
              sb a4,0(a0)
              sb a5,0(a1)
              ret
          partition:
              add a6,a0,a2
              lbu a7,0(a6)
              mv a3,a0
              addi a4,a1,-1
              ble a2,a1,.L7
              add a1,a0,a1
          .L6:
              lbu a5,0(a1)
              bgtu a5,a7,.L5
              addi a4,a4,1
              add a2,a3,a4
              lbu a0,0(a2)
              sb a5,0(a2)
              sb a0,0(a1)
          .L5:
              addi a1,a1,1
              bne a6,a1,.L6
              addi a4,a4,1
              lbu a7,0(a6)
              add a3,a3,a4
              lbu a5,0(a3)
              sb a7,0(a3)
              mv a0,a4
              sb a5,0(a6)
              ret
          .L7:
              mv a4,a1
              add a3,a3,a4
              lbu a5,0(a3)
              sb a7,0(a3)
              mv a0,a1
              sb a5,0(a6)
              ret
          quicksort:
              ble a2,a1,.L38
              addi sp,sp,-96
              sw s0,88(sp)
              sw s6,64(sp)
              sw s8,56(sp)
              sw ra,92(sp)
              sw s1,84(sp)
              sw s2,80(sp)
              sw s3,76(sp)
              sw s4,72(sp)
              sw s5,68(sp)
              sw s7,60(sp)
              sw s9,52(sp)
              sw s10,48(sp)
              sw s11,44(sp)
              mv s8,a1
              mv s6,a2
              mv s0,a0
          .L27:
              mv a2,s6
              mv a1,s8
              mv a0,s0
              call partition
              sw a0,4(sp)
              addi s9,a0,-1
              ble s9,s8,.L11
          .L26:
              mv a2,s9
              mv a1,s8
              mv a0,s0
              call partition
              sw a0,8(sp)
              addi s10,a0,-1
              ble s10,s8,.L12
          .L25:
              mv a2,s10
              mv a1,s8
              mv a0,s0
              call partition
              sw a0,12(sp)
              addi s11,a0,-1
              ble s11,s8,.L13
          .L24:
              mv a2,s11
              mv a1,s8
              mv a0,s0
              call partition
              sw a0,16(sp)
              addi s3,a0,-1
              ble s3,s8,.L14
          .L23:
              mv a2,s3
              mv a1,s8
              mv a0,s0
              call partition
              addi s2,a0,-1
              mv s1,a0
              ble s2,s8,.L15
          .L22:
              mv a2,s2
              mv a1,s8
              mv a0,s0
              call partition
              addi s7,a0,-1
              mv s4,a0
              ble s7,s8,.L16
          .L21:
              mv a2,s7
              mv a1,s8
              mv a0,s0
              call partition
              sw a0,20(sp)
              addi s5,a0,-1
              ble s5,s8,.L17
          .L20:
              mv a2,s5
              mv a1,s8
              mv a0,s0
              call partition
              addi a3,a0,-1
              mv a4,a0
              ble a3,s8,.L18
          .L19:
              mv a2,a3
              mv a1,s8
              mv a0,s0
              sw a4,28(sp)
              sw a3,24(sp)
              call partition
              mv a6,a0
              mv a1,s8
              addi a2,a0,-1
              mv a0,s0
              addi s8,a6,1
              call quicksort
              lw a3,24(sp)
              lw a4,28(sp)
              bgt a3,s8,.L19
          .L18:
              addi s8,a4,1
              bgt s5,s8,.L20
          .L17:
              lw a5,20(sp)
              addi s8,a5,1
              bgt s7,s8,.L21
          .L16:
              addi s8,s4,1
              bgt s2,s8,.L22
          .L15:
              addi s8,s1,1
              bgt s3,s8,.L23
          .L14:
              lw a5,16(sp)
              addi s8,a5,1
              bgt s11,s8,.L24
          .L13:
              lw a5,12(sp)
              addi s8,a5,1
              bgt s10,s8,.L25
          .L12:
              lw a5,8(sp)
              addi s8,a5,1
              bgt s9,s8,.L26
          .L11:
              lw a5,4(sp)
              addi s8,a5,1
              blt s8,s6,.L27
              lw ra,92(sp)
              lw s0,88(sp)
              lw s1,84(sp)
              lw s2,80(sp)
              lw s3,76(sp)
              lw s4,72(sp)
              lw s5,68(sp)
              lw s6,64(sp)
              lw s7,60(sp)
              lw s8,56(sp)
              lw s9,52(sp)
              lw s10,48(sp)
              lw s11,44(sp)
              addi sp,sp,96
              jr ra
          .L38:
              ret
              """;
  
  static String writeLoopCode = """
          int ptr[32];
          
          int writeMem() {
            for(int i = 0; i < 32; i++) {
              ptr[i] = i;
            }
          }
          """;
  
  @Test
  public void test_quicksort()
  {
    // Alignment 2^4
    MemoryLocation arr = new MemoryLocation("arr", 4, DataTypeEnum.kByte,
                                            List.of("1", "16", "3", "10", "4", "6", "7", "8", "9", "5", "11", "12",
                                                    "13", "14", "15", "2"));
    //    Cpu cpu = setupCpu(quickSortCode, List.of(arr));
    // TODO unoptimized version of the program does not work
    
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.memoryLocations = List.of(arr);
    cfg.code            = quicksortAssembly;
    Cpu cpu = new Cpu(cfg);
    
    cpu.execute();
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.stopReason);
    
    long arrPtr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    for (int i = 0; i < 16; i++)
    {
      Assert.assertEquals(i + 1, cpu.cpuState.simulatedMemory.getFromMemory(arrPtr + i));
    }
  }
  
  @Test
  public void test_recursiveFactorial()
  {
    // Alignment 2^4
    Cpu cpu = setupCpu(recursiveFactorialCode, "main", List.of());
    cpu.execute();
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.stopReason);
  }
  
  /**
   * Setup cpu instance with the C code
   */
  private Cpu setupCpu(String cCode, String entryPoint, List<MemoryLocation> memoryLocations)
  {
    GccCaller.CompileResult res = GccCaller.compile(cCode, List.of("O2"));
    Assert.assertTrue(res.success);
    CompiledProgram program             = AsmParser.parse(res.code, cCode.split("\n").length);
    String          concatenatedProgram = StringUtils.join(program.program, "\n");
    
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.memoryLocations = memoryLocations;
    cfg.code            = concatenatedProgram;
    cfg.entryPoint      = entryPoint;
    return new Cpu(cfg);
  }
  
  @Test
  public void simulate_badSpeculativeLoad()
  {
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code = """
             addi x12, x12, -5
             subi x13, x0, 5
             beq x12, x13, label
             lw x12, 0(x12)
            label:
            """;
    // The load will be to a negative address, but it will be purely speculative.
    // It should not crash the simulation
    
    Cpu cpu = new Cpu(cfg);
    cpu.execute();
    
    Assert.assertEquals(-5, (int) cpu.cpuState.unifiedRegisterFileBlock.getRegister("x12").getValue(DataTypeEnum.kInt));
  }
}
