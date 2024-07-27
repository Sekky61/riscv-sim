import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.superscalarsim.cpu.DebugLog;
import com.gradle.superscalarsim.serialization.Serialization;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TODO: move along with the Cli code
 */
public class CliTests
{
  CommandLine cmd;
  CliApp cliApp;
  
  StringWriter sw;
  
  @Before
  public void setUp()
  {
    cliApp = new CliApp();
    cmd    = new CommandLine(cliApp);
    
    sw = new StringWriter();
    cmd.setErr(new PrintWriter(sw));
    cmd.setOut(new PrintWriter(sw));
  }
  
  @Test
  public void testParseGoodArgs()
  {
    // Memory not required
    cmd.parseArgs("--cpu", "cpu.json", "--program", "program.json");
  }
  
  @Test
  public void testCpuRequired()
  {
    int exitCode = cmd.execute("--program", "program.json");
    Assert.assertEquals(2, exitCode);
    Assert.assertTrue(sw.toString().startsWith("Missing required option: '--cpu=FILE'"));
  }
  
  @Test
  public void testProgramRequired()
  {
    int exitCode = cmd.execute("--cpu", "cpu.json");
    Assert.assertEquals(2, exitCode);
    Assert.assertTrue(sw.toString().startsWith("Missing required option: '--program=FILE'"));
  }
  
  @Test
  public void testExecuteFilesDoNotExist()
  {
    int exitCode = cmd.execute("--cpu", "cpu.json", "--program", "program.json");
    Assert.assertEquals(2, exitCode);
    Assert.assertTrue(sw.toString().startsWith("File does not exist: cpu.json"));
  }
  
  /**
   * The test is designed to work with CWD set to the root of the simulator
   */
  @Test
  public void testExecuteGoodArgs()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/basicFloatArithmetic.r5");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    Assert.assertTrue(output.contains("statistics"));
  }
  
  /**
   * The test is designed to work with CWD set to the root of the simulator
   */
  @Test
  public void testExecuteWithMemory()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/externMemory.r5", "--memory", "examples/memory/constant.json");
    String output = sw.toString();
    Assert.assertEquals(0, exitCode);
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    Assert.assertTrue(output.contains("Loaded constant 42"));
  }
  
  /**
   * The test is designed to work with CWD set to the root of the simulator
   */
  @Test
  public void testPrettyPrint()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/basicFloatArithmetic.r5", "--pretty");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    // Is pretty printed
    Assert.assertTrue(output.contains("\n"));
    Assert.assertTrue(output.contains("statistics"));
  }
  
  @Test
  public void testBasicLoop()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/basicLoop.r5");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    Assert.assertTrue(output.contains("\n"));
    Assert.assertTrue(output.contains("statistics"));
    Assert.assertTrue(cliApp.response.state.tick < 100);
  }
  
  @Test
  public void testCallStack()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/callStack.r5", "--entry", "main");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    Assert.assertTrue(output.contains("\n"));
    Assert.assertTrue(output.contains("statistics"));
    
    // There are two prints. One when calling the function and one when entering the function
    Assert.assertEquals(2, cliApp.response.state.debugLog.getEntries().size());
    DebugLog.Entry entry  = cliApp.response.state.debugLog.getEntries().get(0);
    DebugLog.Entry entry2 = cliApp.response.state.debugLog.getEntries().get(1);
    Assert.assertEquals("Call f", entry.getMessage());
    Assert.assertEquals("Enter f", entry2.getMessage());
  }
  
  /**
   * The test is designed to work with CWD set to the root of the simulator
   */
  @Test
  public void testConfigWithName()
  {
    int exitCode = cmd.execute("--cpu", "examples/cpuConfigurations/defaultWithName.json", "--program",
                               "examples/asmPrograms/basicFloatArithmetic.r5");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    // Is a valid JSON
    ObjectMapper deserializer = Serialization.getDeserializer();
    try
    {
      deserializer.readTree(output);
    }
    catch (Exception e)
    {
      Assert.fail("Output is not a valid JSON");
    }
    
    Assert.assertTrue(output.contains("\n"));
    Assert.assertTrue(output.contains("statistics"));
  }
}
