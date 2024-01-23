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
  static String quickSortCode = """
          
          extern char *arr;
          
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
  
  /**
   * Setup cpu instance with the C code
   */
  private Cpu setupCpu(String cCode, List<MemoryLocation> memoryLocations)
  {
    GccCaller.CompileResult res = GccCaller.compile(cCode, List.of());
    Assert.assertTrue(res.success);
    CompiledProgram program             = AsmParser.parse(res.code, cCode.split("\n").length);
    String          concatenatedProgram = StringUtils.join(program.program, "\n");
    
    SimulationConfig cfg = SimulationConfig.getDefaultConfiguration();
    cfg.memoryLocations = memoryLocations;
    cfg.code            = concatenatedProgram;
    return new Cpu(cfg);
  }
  
  @Test
  public void test_validCProgram_passes()
  {
    // Alignment 2^4
    MemoryLocation arr = new MemoryLocation("arr", 4, DataTypeEnum.kByte,
                                            List.of("1", "2", "3", "5", "4", "6", "7", "8", "9", "10", "11", "12", "13",
                                                    "14", "15", "16"));
    Cpu cpu = setupCpu(quickSortCode, List.of(arr));
    
    
    cpu.execute();
    
    // Assert
    long arrPtr = cpu.cpuState.instructionMemoryBlock.getLabelPosition("arr");
    for (int i = 0; i < 16; i++)
    {
      Assert.assertEquals(i + 1, cpu.cpuState.simulatedMemory.getFromMemory(arrPtr + i));
    }
  }
}
