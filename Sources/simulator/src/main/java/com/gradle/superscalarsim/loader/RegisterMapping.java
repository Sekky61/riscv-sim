package com.gradle.superscalarsim.loader;

public class RegisterMapping
{
  public String register;
  public String alias;
  
  /**
   * @brief Default constructor for deserialization
   */
  RegisterMapping()
  {
  }
  
  public RegisterMapping(String register, String alias)
  {
    this.register = register;
    this.alias    = alias;
  }
}
