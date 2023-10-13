package com.gradle.superscalarsim.loader;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.RegisterFileModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    Assert.assertEquals(DataTypeEnum.kInt, model.getDataType());
    
    Assert.assertEquals("x0", model.getRegisterList().get(0).getName());
    Assert.assertTrue(model.getRegisterList().get(0).isConstant());
    Assert.assertEquals(0, model.getRegisterList().get(0).getValue(), 0.01);
    
    Assert.assertEquals("x1", model.getRegisterList().get(1).getName());
    Assert.assertFalse(model.getRegisterList().get(1).isConstant());
    Assert.assertEquals(25, model.getRegisterList().get(1).getValue(), 0.01);
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
    loader.loadFromConfigFiles();
    
    //Assert
    Assert.assertEquals("x0", loader.getRegisterAliases().get(0).register);
    Assert.assertEquals("zero", loader.getRegisterAliases().get(0).alias);
    
    Assert.assertEquals("x1", loader.getRegisterAliases().get(1).register);
    Assert.assertEquals("ra", loader.getRegisterAliases().get(1).alias);
  }
}
