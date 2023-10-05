package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;

import java.util.ArrayList;
import java.util.List;

public class InputCodeModelBuilder
{
  /// ID - the index of the instruction in the code
  private int id;
  private String instructionName;
  private String codeLine;
  private InstructionTypeEnum instructionTypeEnum;
  private List<InputCodeArgument> arguments;
  private DataTypeEnum dataTypeEnum;
  private InstructionFunctionModel instructionFunctionModel;
  private InitLoader loader;
  
  public InputCodeModelBuilder()
  {
    this.id                  = -1;
    this.instructionName     = "";
    this.codeLine            = "";
    this.arguments           = new ArrayList<>();
    this.instructionTypeEnum = null;
    this.dataTypeEnum        = null;
  }
  
  public InputCodeModelBuilder hasLoader(InitLoader loader)
  {
    this.loader = loader;
    return this;
  }
  
  public InputCodeModelBuilder hasInstructionFunctionModel(InstructionFunctionModel instructionFunctionModel)
  {
    this.instructionFunctionModel = instructionFunctionModel;
    return this;
  }
  
  public InputCodeModelBuilder hasInstructionName(String instructionName)
  {
    this.instructionName = instructionName;
    return this;
  }
  
  public InputCodeModelBuilder hasCodeLine(String codeLine)
  {
    this.codeLine = codeLine;
    return this;
  }
  
  public InputCodeModelBuilder hasArguments(List<InputCodeArgument> arguments)
  {
    this.arguments = arguments;
    return this;
  }
  
  public InputCodeModelBuilder hasInstructionTypeEnum(InstructionTypeEnum instructionTypeEnum)
  {
    this.instructionTypeEnum = instructionTypeEnum;
    return this;
  }
  
  public InputCodeModelBuilder hasDataTypeEnum(DataTypeEnum dataTypeEnum)
  {
    this.dataTypeEnum = dataTypeEnum;
    return this;
  }
  
  public InputCodeModelBuilder hasId(int id)
  {
    this.id = id;
    return this;
  }
  
  public InputCodeModel build()
  {
    InstructionFunctionModel model;
    if (this.loader != null)
    {
      model = this.loader.getInstructionFunctionModelList()
                         .stream()
                         .filter(instructionFunctionModel -> instructionFunctionModel.getName()
                                                                                     .equals(this.instructionName))
                         .findFirst()
                         .orElse(null);
    }
    else
    {
      model = this.instructionFunctionModel;
    }
    return new InputCodeModel(model, this.instructionName, this.codeLine, this.arguments, this.instructionTypeEnum,
                              this.dataTypeEnum, this.id);
  }
  
  
}
