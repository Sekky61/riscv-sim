package com.gradle.superscalarsim.loader;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class RegisterSubloaderTest
{
  private RegisterSubloader registerSubloader;
  
  @Before
  public void setUp()
  {
    this.registerSubloader = new RegisterSubloader();
  }
  
  @Test
  public void creatingModelFromFile_fileExists_returnsModel()
  {
    RegisterFileModel model = this.registerSubloader.loadRegisterFile("./testFiles/testRegister.json");
    Assert.assertNotNull(model);
    
    Assert.assertEquals("Integer physical register", model.getName());
    Assert.assertEquals(RegisterTypeEnum.kInt, model.getDataType());
    
    Assert.assertEquals("x0", model.getRegisterList().get(0).getName());
    Assert.assertTrue(model.getRegisterList().get(0).isConstant());
    Assert.assertEquals(0, (int) model.getRegisterList().get(0).getValue(DataTypeEnum.kInt));
    
    Assert.assertEquals("x1", model.getRegisterList().get(1).getName());
    Assert.assertFalse(model.getRegisterList().get(1).isConstant());
    Assert.assertEquals(25, (int) model.getRegisterList().get(1).getValue(DataTypeEnum.kInt));
  }
  
  
  @Test
  public void creatingModelFromFile_pathIsEmpty_returnsNull()
  {
    RegisterFileModel model = this.registerSubloader.loadRegisterFile("/definitelyNotValidPath/notAFile.json");
    Assert.assertNull(model);
  }
  
  @Test
  public void creatingModelFromFile_fileIsCorrupted_returnsNull()
  {
    RegisterFileModel model = this.registerSubloader.loadRegisterFile("./testFiles/corrupted.json");
    Assert.assertNull(model);
  }
  
  @Test
  public void creatingModelFromFile_fileHasCorruptedRegister_returnsNull()
  {
    RegisterFileModel model = this.registerSubloader.loadRegisterFile("./testFiles/testRegisterCorruptedRegister.json");
    Assert.assertNull(model);
  }
  
  @Test
  public void testLoadingAliases()
  {
    // Setup
    InitLoader loader = new InitLoader();
    loader.setRegisterAliasesFilePath("testFiles/registerAliases.json");
    
    // Exercise
    try
    {
      loader.loadFromConfigFiles();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    
    //Assert
    Assert.assertEquals("x0", loader.getRegisterFile().getRegister("zero").getName());
    Assert.assertEquals("x1", loader.getRegisterFile().getRegister("ra").getName());
  }
}
