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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmTests
{
  public static String quicksortAssembly = """
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
  
  String writeMem = """
          writeMem:
              addi sp,sp,-32
              sw s0,28(sp)
              addi s0,sp,32
              sw zero,-20(s0)
              j .L2
          .L3:
              lla a4,ptr
              lw a5,-20(s0)
              slli a5,a5,2
              add a5,a4,a5
              lw a4,-20(s0)
              sw a4,0(a5)
              lw a5,-20(s0)
              addi a5,a5,1
              sw a5,-20(s0)
          .L2:
              lw a4,-20(s0)
              li a5,31
              ble a4,a5,.L3
              nop
              mv a0,a5
              lw s0,28(sp)
              addi sp,sp,32
              jr ra
              .align 2
          ptr:
              .zero 128""";
  
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
    
    cpu.execute(true);
    
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
    Cpu cpu = setupCCode("/c/recursiveFactorial.c", "main", List.of(), true);
    cpu.execute(false);
    
    // Assert
    Assert.assertSame(StopReason.kCallStackHalt, cpu.stopReason);
  }
  
  /**
   * Setup cpu instance with the C code
   */
  private Cpu setupCCode(String cCodeResourcePath,
                         String entryPoint,
                         List<MemoryLocation> memoryLocations,
                         boolean optimize)
  {
    String cCode = null;
    try
    {
      cCode = new String(AlgorithmTests.class.getResourceAsStream(cCodeResourcePath).readAllBytes());
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    List<String>            optimizationFlags = optimize ? List.of("O2") : List.of();
    GccCaller.CompileResult res               = GccCaller.compile(cCode, optimizationFlags);
    Assert.assertTrue(res.success);
    CompiledProgram program             = AsmParser.parse(res.code);
    String          concatenatedProgram = StringUtils.join(program.program, "\n");
    
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.memoryLocations = memoryLocations;
    cfg.code            = concatenatedProgram;
    cfg.entryPoint      = entryPoint;
    return new Cpu(cfg);
  }
  
  /**
   * TODO: miscompiles for arrayLen=1
   * Used to fail at arrayLen>50 due to losing instructions between decode and rob
   */
  @Test
  public void test_simpleArrayProgram()
  {
    // Setup
    int          arrayLen     = 55;
    List<String> hundredOnes  = new ArrayList<>();
    List<String> hundredNines = new ArrayList<>();
    for (int i = 0; i < arrayLen; i++)
    {
      hundredOnes.add("1");
      hundredNines.add("9");
    }
    
    List<MemoryLocation> memoryLocations = List.of(new MemoryLocation("a", 4, DataTypeEnum.kFloat, hundredOnes),
                                                   new MemoryLocation("b", 4, DataTypeEnum.kFloat, hundredNines));
    
    Cpu cpu = setupCCode("/c/axpy.c", "main", memoryLocations, true);
    cpu.execute(true);
    
    // Verify
    // Check memory at a
    long aPtr    = cpu.cpuState.instructionMemoryBlock.getLabelPosition("a");
    int  correct = 0;
    for (int i = 0; i < arrayLen; i++)
    {
      byte[]  bytes     = cpu.cpuState.simulatedMemory.getFromMemory(aPtr + i * 4, 4);
      float   converted = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
      boolean isCorrect = Math.abs(converted - 91.0f) < 0.01;
      if (isCorrect)
      {
        correct++;
      }
      //      Assert.assertEquals(91.0f, converted, 0.01);
    }
    
    Assert.assertEquals(arrayLen, correct);
  }
  
  @Test
  public void test_simpleMatrixMul()
  {
    // Setup
    Cpu cpu = setupCCode("/c/simpleMatrixMul.c", "main", List.of(), true);
    cpu.execute(true);
    
    int[] result = new int[]{4, 8, 12, 16, 8, 16, 24, 32, 12, 24, 36, 48, 16, 32, 48, 64};
    
    // Verify
    // Check array resultMatrix
    long resultMatrixPtr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("resultMatrix");
    
    for (int i = 0; i < 16; i++)
    {
      Assert.assertEquals(result[i], cpu.cpuState.simulatedMemory.getFromMemory(resultMatrixPtr + i * 4));
    }
  }
  
  @Test
  public void test_LinkedList() throws IOException
  {
    // Setup
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    String code = new String(AlgorithmTests.class.getResourceAsStream("/assembler/linkedList.r5").readAllBytes());
    cfg.code       = code;
    cfg.entryPoint = "main";
    Cpu cpu = new Cpu(cfg);
    cpu.execute(true);
    
    // Assert
    // There should be prints
    List<DebugLog.Entry> logEntries = cpu.cpuState.debugLog.getEntries();
    Assert.assertFalse(logEntries.isEmpty());
    Assert.assertTrue(logEntries.get(0).getMessage().startsWith("Insert 1"));
    Assert.assertTrue(logEntries.get(1).getMessage().startsWith("Insert 2"));
    Assert.assertTrue(logEntries.get(2).getMessage().startsWith("Insert 3"));
    Assert.assertTrue(logEntries.get(3).getMessage().startsWith("Insert 4"));
    Assert.assertTrue(logEntries.get(4).getMessage().startsWith("Insert 5"));
    // Print backwards
    Assert.assertTrue(logEntries.get(5).getMessage().startsWith("Node 5"));
    Assert.assertTrue(logEntries.get(6).getMessage().startsWith("Node 4"));
    Assert.assertTrue(logEntries.get(7).getMessage().startsWith("Node 3"));
    Assert.assertTrue(logEntries.get(8).getMessage().startsWith("Node 2"));
    Assert.assertTrue(logEntries.get(9).getMessage().startsWith("Node 1"));
  }
  
  @Test
  public void test_DynamicDispatch() throws IOException
  {
    // Setup
    SimulationConfig cfg = new SimulationConfig();
    String code = new String(AlgorithmTests.class.getResourceAsStream("/assembler/functionPointers.r5").readAllBytes());
    cfg.code                                              = code;
    cfg.entryPoint                                        = "main";
    cfg.cpuConfig.fUnits.get(0).operations.get(0).latency = 5;
    Cpu cpu = new Cpu(cfg);
    cpu.execute(true);
    
    // Assert
    // There should be prints, first a drawCircle, then a drawRectangle
    List<DebugLog.Entry> logEntries = cpu.cpuState.debugLog.getEntries();
    Assert.assertEquals(4, logEntries.size());
    Assert.assertTrue(logEntries.get(0).getMessage().startsWith("drawCircle"));
    Assert.assertTrue(logEntries.get(1).getMessage().startsWith("drawRectangle"));
    Assert.assertTrue(logEntries.get(2).getMessage().startsWith("drawCircle"));
    Assert.assertTrue(logEntries.get(3).getMessage().startsWith("drawRectangle"));
  }
  
  @Test
  public void test_writeMem()
  {
    // Setup
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.code       = writeMem;
    cfg.entryPoint = "writeMem";
    Cpu cpu = new Cpu(cfg);
    cpu.execute(true);
    
    // Assert
    // TODO, for now just happy it doesn't crash
    
    // get ptr, it should be an array: [0, 1, 2, 3, ..., 31]
    long ptr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("ptr");
    for (int i = 0; i < 32; i++)
    {
      byte[] data = cpu.cpuState.memoryModel.getData(ptr + 4 * i, 4);
      Assert.assertEquals(i, data[0]);
    }
  }
}
