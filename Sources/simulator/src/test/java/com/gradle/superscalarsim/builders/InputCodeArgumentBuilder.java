package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.register.RegisterModel;

public class InputCodeArgumentBuilder
{
  private String name;
  private String value;
  private RegisterModel registerModel;
  
  UnifiedRegisterFileBlock unifiedRegisterFileBlock;
  
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
  
  public InputCodeArgumentBuilder hasValue(String value)
  {
    this.value = value;
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
    return new InputCodeArgument(this.name, this.value, registerModel);
  }
}
