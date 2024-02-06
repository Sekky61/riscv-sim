package com.gradle.superscalarsim.loader;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterFile;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class RegisterSubloaderTest
{
  @Test
  public void creatingModelFromFile_fileExists_returnsModel()
  {
    InitLoader initLoader = new InitLoader();
    initLoader.registerFileResourceDirPath = "/testRegister.json";
    
    // Execute
    try
    {
      initLoader.loadFromConfigFiles();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    
    RegisterFile model = initLoader.getRegisterFile();
    
    // TODO: file data type is no longer a thing
    // Assert.assertEquals(RegisterTypeEnum.kInt, model.getDataType());
    
    RegisterModel x0 = model.getRegister("x0");
    Assert.assertNotNull(x0);
    Assert.assertTrue(x0.isConstant());
    Assert.assertEquals(0, (int) x0.getValue(DataTypeEnum.kInt));
    
    RegisterModel x1 = model.getRegister("x1");
    Assert.assertNotNull(x1);
    Assert.assertFalse(x1.isConstant());
    Assert.assertEquals(25, (int) x1.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void creatingModelFromFile_fileHasCorruptedRegister_returnsNull()
  {
    InitLoader initLoader = new InitLoader();
    initLoader.registerFileResourceDirPath = "/testRegisterCorruptedRegister.json";
    
    Assert.assertThrows(Exception.class, initLoader::loadFromConfigFiles);
  }
}
