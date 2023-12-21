package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import com.gradle.superscalarsim.models.register.RegisterModel;

public class InputCodeArgumentBuilder
{
  UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  private String name;
  private String value;
  private RegisterModel registerModel;
  private RegisterDataContainer constantValue;
  
  public InputCodeArgumentBuilder(UnifiedRegisterFileBlock unifiedRegisterFileBlock)
  {
    this.name                     = "";
    this.value                    = "";
    this.unifiedRegisterFileBlock = unifiedRegisterFileBlock;
  }
  
  public InputCodeArgumentBuilder hasName(String name)
  {
    this.name = name;
    return this;
  }
  
  public InputCodeArgumentBuilder hasLabel(String value)
  {
    this.value = value;
    return this;
  }
  
  public InputCodeArgumentBuilder hasConstant(String value, DataTypeEnum type)
  {
    RegisterDataContainer constantValue = RegisterDataContainer.parseAs(value, type);
    if (constantValue == null)
    {
      throw new IllegalArgumentException("Could not parse constant value: " + value);
    }
    this.value         = value;
    this.constantValue = constantValue;
    return this;
  }
  
  public InputCodeArgumentBuilder hasRegister(String regName)
  {
    this.value         = regName;
    this.registerModel = unifiedRegisterFileBlock.getRegister(regName);
    return this;
  }
  
  public InputCodeArgument build()
  {
    if (registerModel != null)
    {
      return new InputCodeArgument(this.name, this.value, registerModel);
    }
    else if (constantValue != null)
    {
      return new InputCodeArgument(this.name, constantValue);
    }
    else
    {
      return new InputCodeArgument(this.name, this.value);
    }
  }
}
