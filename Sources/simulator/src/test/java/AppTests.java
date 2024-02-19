import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * TODO: move along with the Cli code
 */
public class AppTests
{
  CommandLine cmd;
  App app;
  
  StringWriter sw;
  
  @Before
  public void setUp()
  {
    app = new App();
    cmd = new CommandLine(app);
    
    sw = new StringWriter();
    cmd.setErr(new PrintWriter(sw));
    cmd.setOut(new PrintWriter(sw));
  }
  
  @Test
  public void testParseGoodServerArgs()
  {
    // Memory not required
    cmd.parseArgs("server");
  }
  
  @Test
  public void testParseGoodCliArgs()
  {
    // Memory not required
    cmd.parseArgs("cli", "--cpu", "cpuConfig.json", "--program", "program.json");
  }
  
  @Test
  public void testExecuteCliSimulation()
  {
    int exitCode = cmd.execute("cli", "--cpu", "examples/cpuConfigurations/default.json", "--program",
                               "examples/asmPrograms/basicFloatArithmetic.r5");
    Assert.assertEquals(0, exitCode);
    
    String output = sw.toString();
    
    Assert.assertTrue(output.contains("statistics"));
  }
  
  @Test
  public void testStartServer() throws InterruptedException
  {
    int MAX_RETRIES       = 20;
    int RETRY_INTERVAL_MS = 50;
    
    Thread serverThread = new Thread(() ->
                                     {
                                       int exitCode = cmd.execute("server");
                                       Assert.assertEquals(0, exitCode);
                                     });
    serverThread.start();
    
    // Wait for the server to be ready
    boolean serverReady = false;
    for (int i = 0; i < MAX_RETRIES; i++)
    {
      try
      {
        int responseCode = connectionResponse();
        if (responseCode == 200)
        {
          serverReady = true;
          break;
        }
      }
      catch (Exception ignored)
      {
      }
      Thread.sleep(RETRY_INTERVAL_MS);
    }
    
    Assert.assertTrue(serverReady);
    
    // Kill thread
    serverThread.interrupt();
  }
  
  @NotNull
  private static int connectionResponse() throws IOException
  {
    ServerApp serverApp = new ServerApp();
    String    path      = String.format("http://%s:%d/instructionDescription", serverApp.host, serverApp.port);
    
    // Make a request to the server
    URL               url = new URL(path); // Replace "yourPort" and "yourEndpoint" with appropriate values
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    // Send body
    con.setDoOutput(true);
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Accept", "application/json");
    con.getOutputStream().write("{}".getBytes());
    
    return con.getResponseCode();
  }
}
