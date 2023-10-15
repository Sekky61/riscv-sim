/**
 * @file CodeLoadStoreInterpreter.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains load/store interpreter
 * @date 17 December  2020 10:00 (created) \n
 * 17 May       2021 11:00 (revised)
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
package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @class CodeLoadStoreInterpreter
 * @brief Interprets load/store instruction provided in InputCodeModel class
 */
public class CodeLoadStoreInterpreter
{
  /// Pattern for matching hexadecimal values in argument
  private transient final Pattern hexadecimalPattern;
  /// Pattern for matching decimal values in argument
  private transient final Pattern decimalPattern;
  /// Class that simulates memory
  private final MemoryModel memoryModel;
  /// InitLoader object with loaded instructions and registers
  private final InitLoader initLoader;
  private final UnifiedRegisterFileBlock registerFileBlock;
  
  /**
   * @param [in] initLoader - Initial loader of interpretable instructions and register files
   * @param [in] memoryModel - Class simulating memory
   *
   * @brief Constructor
   */
  public CodeLoadStoreInterpreter(final InitLoader initLoader,
                                  final MemoryModel memoryModel,
                                  final UnifiedRegisterFileBlock registerFileBlock)
  {
    this.decimalPattern     = Pattern.compile("-?\\d+(\\.\\d+)?");
    this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
    this.initLoader         = initLoader;
    this.memoryModel        = memoryModel;
    this.registerFileBlock  = registerFileBlock;
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  /**
   * @brief Resets the memory
   */
  public void resetMemory()
  {
    this.memoryModel.reset();
  }// end of Constructor
  //-------------------------------------------------------------------------------------------
  
  public long interpretAddress(final IInputCodeModel parsedCode)
  {
    final InstructionFunctionModel instruction = parsedCode.getInstructionFunctionModel();
    if (instruction == null)
    {
      return -1;
    }
    
    String[] interpretableAsParams = instruction.getInterpretableAs().replace(";", "").split(" ");
    
    InputCodeArgument addressRegister = parsedCode.getArgumentByName(interpretableAsParams[3]);
    InputCodeArgument offsetImmediate = parsedCode.getArgumentByName(interpretableAsParams[4]);
    
    long address = Double.valueOf(
                    getValueFromOperand(Objects.requireNonNull(addressRegister).getValue(), instruction.getInputDataType()))
            .longValue();
    long offset = Double.valueOf(
                    getValueFromOperand(Objects.requireNonNull(offsetImmediate).getValue(), instruction.getInputDataType()))
            .longValue();
    
    return address + offset;
  }// end of interpretAddress
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] parsedCode - code to be interpreted
   *
   * @return Returns double value, in case of store returns stored value, in case of load returns value from memory
   * @brief Interprets load/store instruction from parsedCode
   */
  public Pair<Integer, Double> interpretInstruction(final SimCodeModel parsedCode, int currentCycle)
  {
    final InstructionFunctionModel instruction = parsedCode.getInstructionFunctionModel();
    if (instruction == null)
    {
      return new Pair<>(0, Double.NaN);
    }
    
    String[] interpretableAsParams = instruction.getInterpretableAs().replace(";", "").split(" ");
    
    InputCodeArgument addressRegister   = parsedCode.getArgumentByName(interpretableAsParams[3]);
    InputCodeArgument offsetImmediate   = parsedCode.getArgumentByName(interpretableAsParams[4]);
    InputCodeArgument loadStoreRegister = parsedCode.getArgumentByName(interpretableAsParams[2]);
    
    long address = Double.valueOf(
                    getValueFromOperand(Objects.requireNonNull(addressRegister).getValue(), instruction.getInputDataType()))
            .longValue();
    long offset = Double.valueOf(
                    getValueFromOperand(Objects.requireNonNull(offsetImmediate).getValue(), instruction.getInputDataType()))
            .longValue();
    
    switch (interpretableAsParams[0])
    {
      case "load" ->
      {
        String   destination = Objects.requireNonNull(loadStoreRegister).getValue();
        String[] loadParams  = interpretableAsParams[1].split(":");
        return processLoadOperation(loadParams[0], loadParams[1].equals("unsigned"), destination, address, offset,
                                    parsedCode.getId(), currentCycle);
      }
      case "store" ->
      {
        double sourceValue = Double.valueOf(getValueFromOperand(Objects.requireNonNull(loadStoreRegister).getValue(),
                                                                instruction.getOutputDataType())).longValue();
        return processStoreOperation(interpretableAsParams[1], sourceValue, address, offset, parsedCode.getId(),
                                     currentCycle);
      }
      default -> throw new IllegalStateException("Unexpected value: " + interpretableAsParams[0]);
    }
  }// end of interpretInstruction
  //-------------------------------------------------------------------------------------------
  
  
  /**
   * @param [in] size        - Size of the loaded value
   * @param [in] isUnsigned  - Flag, if result should be sign extended or zero extended
   * @param [in] destination - Destination register
   * @param [in] address     - Address pointing to value in memory
   * @param [in] offset      - Address offset
   *
   * @return Double value from memory
   * @brief Processes load instruction
   */
  private Pair<Integer, Double> processLoadOperation(String size,
                                                     boolean isUnsigned,
                                                     String destination,
                                                     long address,
                                                     long offset,
                                                     int id,
                                                     int currentCycle)
  {
    long                effectiveAddress = address + offset;
    int                 numberOfBytes    = sizeToNumberOfBytes(size);
    Pair<Integer, Long> loadedData       = memoryModel.load(effectiveAddress, numberOfBytes, id, currentCycle);
    double              sign             = isUnsigned ? 1.0 : getSign(size, loadedData.getSecond());
    
    RegisterModel registerModel = null;
    // TODO: remove use of initLoader
    for (RegisterFileModel registerFileModel : initLoader.getRegisterFileModelList())
    {
      registerModel = registerFileModel.getRegister(destination);
      if (registerModel != null)
      {
        break;
      }
    }
    if (registerModel == null)
    {
      registerModel = this.registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative).stream()
              .filter(register -> register.getName().equals(destination)).findFirst().orElse(null);
    }
    
    byte[] bytes = new byte[numberOfBytes];
    for (int i = 0; i < numberOfBytes; i++)
    {
      bytes[i]   = (byte) (loadedData.getSecond() & ((1L << 8) - 1));
      loadedData = new Pair<>(loadedData.getFirst(), loadedData.getSecond() >> 8);
    }
    
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    double result = switch (numberOfBytes)
    {
      case 1 -> byteBuffer.get();
      case 2 -> byteBuffer.getShort();
      case 4 -> size.equals("float") ? byteBuffer.getFloat() : byteBuffer.getInt();
      case 8 -> size.equals("double") ? byteBuffer.getDouble() : byteBuffer.getLong();
      default -> Double.NaN;
    };
    
    Objects.requireNonNull(registerModel).setValue(result * sign);
    return new Pair<>(loadedData.getFirst(), result * sign);
  }// end of processLoadOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * Process store instruction
   *
   * @param [in] size        - Size of the stored value
   * @param [in] sourceValue - Value to be stored
   * @param [in] address     - Address pointing to value in memory
   * @param [in] offset      - Address offset
   * @param [in] id          - Id of a store instruction, that is being executed
   *
   * @return Stored value in Double
   */
  private Pair<Integer, Double> processStoreOperation(String size,
                                                      double sourceValue,
                                                      long address,
                                                      long offset,
                                                      int id,
                                                      int currentCycle)
  {
    long       effectiveAddress = address + offset;
    int        numberOfBytes    = sizeToNumberOfBytes(size);
    ByteBuffer byteBuffer       = ByteBuffer.allocate(numberOfBytes);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    switch (numberOfBytes)
    {
      case 1 -> byteBuffer.put((byte) sourceValue);
      case 2 -> byteBuffer.putShort((short) sourceValue);
      case 4 ->
      {
        if (size.equals("float"))
        {
          byteBuffer.putFloat((float) sourceValue);
        }
        else
        {
          byteBuffer.putInt((int) sourceValue);
        }
      }
      case 8 ->
      {
        if (size.equals("double"))
        {
          byteBuffer.putDouble(sourceValue);
        }
        else
        {
          byteBuffer.putLong((long) sourceValue);
        }
      }
    }
    byte[] bytes = byteBuffer.array();
    
    long storeValue = ((long) bytes[numberOfBytes - 1] & ((1 << 8) - 1));
    for (int i = numberOfBytes - 1; i > 0; i--)
    {
      storeValue = storeValue << 8;
      storeValue = storeValue | ((long) bytes[i - 1] & ((1 << 8) - 1));
    }
    
    return new Pair<>(memoryModel.store(effectiveAddress, storeValue, numberOfBytes, id, currentCycle), sourceValue);
  }// end of processStoreOperation
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] size - String value defining how many bytes to load/store
   *
   * @return Number of bytes to allocate
   * @brief Gets number of bytes according to size type
   */
  private int sizeToNumberOfBytes(String size)
  {
    return switch (size)
    {
      case "byte" -> 1;
      case "half" -> 2;
      case "word", "float" -> 4;
      case "doubleword", "double" -> 8;
      case "quadword" -> 16;
      default -> 0;
    };
  }// end of sizeToNumberOfBytes
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] size             - String value defining how many bytes to load
   * @param [in] effectiveAddress - Address pointing to specific place in memory
   *
   * @return Signum function value of loaded value in memory
   * @brief Get sign of loaded value
   */
  private double getSign(String size, long data)
  {
    return switch (size)
    {
      //TODO CONVERSION CHECK
      case "byte" -> Math.signum((byte) data);
      case "half" -> Math.signum((short) data);
      case "word" -> Math.signum((int) data);
      case "float" -> Math.signum((float) data);
      case "doubleword" -> Math.signum(data);
      case "double" -> Math.signum((double) data);
      default -> 1.0;
    };
  }// end of getSign
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] operand - Value to be parsed, either immediate (hex or decimal) or register
   * @param [in] dataType - Result data type
   *
   * @return Parsed double value from operand
   * @brief Gets value from string operand
   */
  private double getValueFromOperand(String operand, DataTypeEnum dataType)
  {
    // Checks if value is immediate
    if (hexadecimalPattern.matcher(operand).matches())
    {
      return Long.parseLong(operand.substring(2), 16);
    }
    else if (decimalPattern.matcher(operand).matches())
    {
      return Double.parseDouble(operand);
    }
    
    DataTypeEnum[] dataTypeEnums = getFitRegisterTypes(dataType);
    RegisterModel  registerModel = null;
    for (DataTypeEnum possibleDataType : dataTypeEnums)
    {
      registerModel = this.registerFileBlock.getRegisterList(possibleDataType).stream()
              .filter(register -> register.getName().equals(operand)).findFirst().orElse(null);
      if (registerModel != null)
      {
        break;
      }
    }
    
    if (registerModel == null)
    {
      registerModel = this.registerFileBlock.getRegisterList(DataTypeEnum.kSpeculative).stream()
              .filter(register -> register.getName().equals(operand)).findFirst().orElse(null);
    }
    
    return registerModel != null ? registerModel.getValue() : Double.NaN;
  }// end of getValueFromOperand
  //-------------------------------------------------------------------------------------------
  
  /**
   * @param [in] dataType - Data type of the register
   *
   * @return List of datatypes in which input datatype can fit
   * @brief Get list of data types in which specified data type can fit
   */
  private DataTypeEnum[] getFitRegisterTypes(DataTypeEnum dataType)
  {
    return switch (dataType)
    {
      case kInt -> new DataTypeEnum[]{DataTypeEnum.kInt, DataTypeEnum.kLong};
      case kLong -> new DataTypeEnum[]{DataTypeEnum.kLong};
      case kFloat -> new DataTypeEnum[]{DataTypeEnum.kFloat, DataTypeEnum.kDouble};
      case kDouble -> new DataTypeEnum[]{DataTypeEnum.kDouble};
      case kSpeculative -> new DataTypeEnum[]{};
    };
  }// end of getFitRegisterTypes
  //-------------------------------------------------------------------------------------------
}
