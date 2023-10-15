package com.gradle.superscalarsim.builders;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;

import java.util.List;

public class RegisterFileModelBuilder
{
  private String name;
  private DataTypeEnum dataType;
  private List<RegisterModel> registerList;
  
  public RegisterFileModelBuilder()
  {
    this.name         = "";
    this.dataType     = null;
    this.registerList = null;
  }
  
  public RegisterFileModelBuilder hasName(String name)
  {
    this.name = name;
    return this;
  }
  
  public RegisterFileModelBuilder hasDataType(DataTypeEnum dataType)
  {
    this.dataType = dataType;
    return this;
  }
  
  public RegisterFileModelBuilder hasRegisterList(List<RegisterModel> registerList)
  {
    this.registerList = registerList;
    return this;
  }
  
  public RegisterFileModel build()
  {
    return new RegisterFileModel(this.name, this.dataType.toString(), this.registerList);
  }
}
