package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.models.InputCodeArgument;

public class InputCodeArgumentBuilder
{
  private String name;
  private String value;

  public InputCodeArgumentBuilder()
  {
    this.name  = "";
    this.value = "";
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

  public InputCodeArgument build()
  {
    return new InputCodeArgument(this.name, this.value);
  }
}
