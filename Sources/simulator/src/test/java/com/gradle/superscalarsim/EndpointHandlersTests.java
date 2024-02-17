package com.gradle.superscalarsim;

import com.gradle.superscalarsim.cpu.SimulationConfig;
import com.gradle.superscalarsim.server.simulate.SimulateHandler;
import com.gradle.superscalarsim.server.simulate.SimulateRequest;
import com.gradle.superscalarsim.server.simulate.SimulateResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Test for the endpoint handlers
 */
public class EndpointHandlersTests
{
  /**
   * The /simulate endpoint generates a response
   */
  @Test
  public void testSimulateEndpoint()
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
}
