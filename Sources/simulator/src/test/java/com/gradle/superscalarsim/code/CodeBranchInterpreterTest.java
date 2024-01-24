package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.instruction.InputCodeArgument;
import com.gradle.superscalarsim.models.instruction.InputCodeModel;
import com.gradle.superscalarsim.models.instruction.SimCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class CodeBranchInterpreterTest
{
  
  private InitLoader initLoader;
  
  private InstructionMemoryBlock instructionMemoryBlock;
  
  private CodeBranchInterpreter codeBranchInterpreter;
  private UnifiedRegisterFileBlock urf;
  
  @Before
  public void setUp()
  {
    this.initLoader = new InitLoader();
    urf             = new UnifiedRegisterFileBlock(initLoader, 320, new RegisterModelFactory());
    List<InputCodeModel> inputCodeModels = setUpParsedCode();
    var                  labels          = setUpLabels();
    var                  nopFM           = initLoader.getInstructionFunctionModel("nop");
    InputCodeModel       nop             = new InputCodeModel(nopFM, new ArrayList<>(), 0);
    instructionMemoryBlock = new InstructionMemoryBlock(inputCodeModels, labels, nop);
    
    urf.getRegister("x1").setValue(0);
    urf.getRegister("x2").setValue(25);
    urf.getRegister("x3").setValue(6);
    urf.getRegister("x4").setValue(-2);
    
    this.codeBranchInterpreter = new CodeBranchInterpreter();
  }
  
  private List<InputCodeModel> setUpParsedCode()
  {
    // one:
    // add x1 x3 x2
    // two:
    // sub x1 x3 x2
    // three:
    // mul x1 x3 x2
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModelAdd = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("add")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModelSub = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sub")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x3").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeModel inputCodeModelMul = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("mul")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    
    return Arrays.asList(inputCodeModelAdd, inputCodeModelSub, inputCodeModelMul);
  }
  
  private Map<String, Label> setUpLabels()
  {
    Map<String, Label> labels = new HashMap<>();
    labels.put("one", new Label("one", 0));
    labels.put("two", new Label("two", 4));
    labels.put("three", new Label("three", 8));
    return labels;
  }
  
  @Test
  public void unconditionalJump_interpret_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("jal")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("two", -8).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("beq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "two" which is at index 1
    Assert.assertEquals(4, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("two", 4).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("beq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3);
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("three", -4).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bne")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "three" which is at index 2
    Assert.assertEquals(8, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("three", 8).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bne")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3);
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThan_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("blt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpLessThan_conditionFalse_returnsOne()
  {
    // (The code)
    // one:
    // add x1 x3 x2
    // two:
    // sub x1 x3 x2
    // three:
    // mul x1 x3 x2
    //
    // blt x1 x4 one
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("blt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bltu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bltu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(0, this.codeBranchInterpreter.interpretInstruction(simCodeModel).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("imm").hasLabel("one", -12).build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel simCodeModel = new SimCodeModel(inputCodeModel, 0);
    simCodeModel.setSavedPc(3 * 4);
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(simCodeModel).isPresent());
  }
}
