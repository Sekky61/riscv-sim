package com.gradle.superscalarsim;

import com.fasterxml.jackson.databind.JsonNode;
import com.gradle.superscalarsim.cpu.MemoryLocation;
import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.server.EndpointName;
import com.gradle.superscalarsim.server.ServerError;
import com.gradle.superscalarsim.server.ServerException;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigHandler;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigRequest;
import com.gradle.superscalarsim.server.checkConfig.CheckConfigResponse;
import com.gradle.superscalarsim.server.compile.CompileHandler;
import com.gradle.superscalarsim.server.compile.CompileRequest;
import com.gradle.superscalarsim.server.compile.CompileResponse;
import com.gradle.superscalarsim.server.instructionDescriptions.InstructionDescriptionHandler;
import com.gradle.superscalarsim.server.instructionDescriptions.InstructionDescriptionRequest;
import com.gradle.superscalarsim.server.instructionDescriptions.InstructionDescriptionResponse;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmHandler;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmRequest;
import com.gradle.superscalarsim.server.parseAsm.ParseAsmResponse;
import com.gradle.superscalarsim.server.schema.SchemaHandler;
import com.gradle.superscalarsim.server.schema.SchemaRequest;
import com.gradle.superscalarsim.server.simulate.SimulateHandler;
import com.gradle.superscalarsim.server.simulate.SimulateRequest;
import com.gradle.superscalarsim.server.simulate.SimulateResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gradle.superscalarsim.enums.DataTypeEnum.kInt;

/**
 * Test for the endpoint handlers
 */
public class EndpointHandlersTests
{
  /**
   * The /simulate endpoint generates a response
   */
  @Test
  public void testSimulateEndpoint() throws ServerException
  {
    SimulateHandler  handler = new SimulateHandler();
    SimulationConfig config  = new SimulationConfig();
    config.code = "nop";
    SimulateRequest request = new SimulateRequest(config, Optional.of(1));
    
    SimulateResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertNotNull(response.state);
    Assert.assertEquals(1, response.executedSteps);
  }
  
  /**
   * The /checkConfig endpoint generates a positive response
   */
  @Test
  public void testCheckConfigEndpointValid() throws ServerException
  {
    CheckConfigHandler handler = new CheckConfigHandler();
    SimulationConfig   config  = new SimulationConfig();
    CheckConfigRequest request = new CheckConfigRequest(config);
    
    CheckConfigResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertTrue(response.valid);
    Assert.assertNotNull(response.messages);
    Assert.assertTrue(response.messages.isEmpty());
  }
  
  /**
   * The /checkConfig endpoint generates a negative response
   */
  @Test
  public void testCheckConfigEndpointInvalid() throws ServerException
  {
    CheckConfigHandler handler = new CheckConfigHandler();
    SimulationConfig   config  = new SimulationConfig();
    config.code = "invalid";
    CheckConfigRequest request = new CheckConfigRequest(config);
    
    CheckConfigResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertFalse(response.valid);
    Assert.assertNotNull(response.messages);
    Assert.assertEquals(1, response.messages.size());
  }
  
  /**
   * The /checkConfig endpoint generates a negative response
   */
  @Test
  public void testCheckConfigEndpointCodeNull() throws ServerException
  {
    CheckConfigHandler handler = new CheckConfigHandler();
    SimulationConfig   config  = new SimulationConfig();
    config.code = null;
    CheckConfigRequest request = new CheckConfigRequest(config);
    
    CheckConfigResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertFalse(response.valid);
    Assert.assertNotNull(response.messages);
    Assert.assertEquals(1, response.messages.size());
  }
  
  /**
   * The /compile endpoint generates a response
   */
  @Test
  public void testCompileEndpoint() throws ServerException
  {
    CompileRequest request = new CompileRequest("", List.of(), List.of());
    CompileHandler handler = new CompileHandler();
    
    CompileResponse response = handler.resolve(request);
    
    // Assert - successful, so mapping must be there
    Assert.assertNotNull(response);
    Assert.assertTrue(response.success);
    Assert.assertNotNull(response.program);
    Assert.assertNotNull(response.asmToC);
    Assert.assertNull(response.compilerError);
    Assert.assertNull(response.error);
  }
  
  @Test
  public void testCompileMissingCode()
  {
    CompileRequest request = new CompileRequest(null, List.of(), List.of());
    CompileHandler handler = new CompileHandler();
    
    try
    {
      handler.resolve(request);
    }
    catch (ServerException e)
    {
      ServerError error = e.getError();
      Assert.assertEquals("Missing code", error.message());
      Assert.assertEquals("code", error.field());
    }
  }
  
  @Test
  public void testCompileEndpointMemory() throws ServerException
  {
    MemoryLocation arr = new MemoryLocation("arr", 4, kInt, List.of("0", "1", "2", "3"));
    CompileRequest request = new CompileRequest("""
                                                        extern int arr[];
                                                        int main() {
                                                          return arr[1];
                                                        }""", List.of(), List.of(arr));
    CompileHandler handler = new CompileHandler();
    
    CompileResponse response = handler.resolve(request);
    
    // Assert - successful, so mapping must be there
    Assert.assertNotNull(response);
    Assert.assertTrue(response.success);
    Assert.assertNotNull(response.program);
    Assert.assertNotNull(response.asmToC);
    Assert.assertNull(response.compilerError);
    Assert.assertNull(response.error);
    Assert.assertNull(response.asmErrors);
  }
  
  @Test
  public void testCompileEndpointMemoryError() throws ServerException
  {
    CompileRequest request = new CompileRequest("""
                                                        extern int arr[];
                                                        int main() {
                                                          return arr[1];
                                                        }""", List.of(), List.of());
    CompileHandler handler = new CompileHandler();
    
    CompileResponse response = handler.resolve(request);
    
    // Assert - there is an error
    Assert.assertNotNull(response);
    Assert.assertFalse(response.success);
    Assert.assertNull(response.program);
    Assert.assertNull(response.asmToC);
    
    Assert.assertNull(response.compilerError);
    Assert.assertNotNull(response.error);
    Assert.assertNotNull(response.asmErrors);
  }
  
  @Test
  public void testInstructionDescriptions()
  {
    InstructionDescriptionRequest request = new InstructionDescriptionRequest();
    InstructionDescriptionHandler handler = new InstructionDescriptionHandler();
    
    InstructionDescriptionResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertNotNull(response.models);
    Assert.assertFalse(response.models.isEmpty());
    Assert.assertTrue(response.models.get("addi").name().contains("addi"));
  }
  
  @Test
  public void testParseAsm() throws ServerException
  {
    ParseAsmRequest request = new ParseAsmRequest("addi x1, x2, 3", new ArrayList<>());
    ParseAsmHandler handler = new ParseAsmHandler();
    
    ParseAsmResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertTrue(response.success);
    Assert.assertNotNull(response.errors);
  }
  
  @Test
  public void testParseAsmMemory() throws ServerException
  {
    MemoryLocation  ptr     = new MemoryLocation("ptr", 4, kInt, List.of("7"));
    ParseAsmRequest request = new ParseAsmRequest("lla x7, ptr", List.of(ptr));
    ParseAsmHandler handler = new ParseAsmHandler();
    
    ParseAsmResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertTrue(response.success);
    Assert.assertNotNull(response.errors);
  }
  
  @Test
  public void testParseAsmInvalid() throws ServerException
  {
    ParseAsmRequest request = new ParseAsmRequest("invalid", new ArrayList<>());
    ParseAsmHandler handler = new ParseAsmHandler();
    
    ParseAsmResponse response = handler.resolve(request);
    
    Assert.assertNotNull(response);
    Assert.assertFalse(response.success);
    Assert.assertNotNull(response.errors);
    Assert.assertFalse(response.errors.isEmpty());
  }
  
  /**
   * Not a good test, but to show it doesn't crash
   */
  @Test
  public void testSchema() throws ServerException
  {
    SchemaRequest request = new SchemaRequest(EndpointName.simulate, SchemaRequest.RequestResponse.request);
    SchemaHandler handler = new SchemaHandler();
    
    JsonNode response = handler.resolve(request);
    
    Assert.assertNotNull(response);
  }
}
