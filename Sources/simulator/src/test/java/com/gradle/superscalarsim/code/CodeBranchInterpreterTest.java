package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CodeBranchInterpreterTest
{
  
  private InitLoader initLoader;
  
  private InstructionMemoryBlock instructionMemoryBlock;
  
  private CodeBranchInterpreter codeBranchInterpreter;
  
  @Before
  public void setUp()
  {
    this.initLoader = new InitLoader();
    List<InputCodeModel> inputCodeModels = setUpParsedCode();
    var                  labels          = setUpLabels();
    instructionMemoryBlock = new InstructionMemoryBlock(inputCodeModels, labels);
    
    UnifiedRegisterFileBlock unifiedRegisterFileBlock = new UnifiedRegisterFileBlock(initLoader);
    unifiedRegisterFileBlock.getRegister("x1").setValue(0);
    unifiedRegisterFileBlock.getRegister("x2").setValue(25);
    unifiedRegisterFileBlock.getRegister("x3").setValue(6);
    unifiedRegisterFileBlock.getRegister("x4").setValue(-2);
    
    this.codeBranchInterpreter = new CodeBranchInterpreter(instructionMemoryBlock, unifiedRegisterFileBlock,
                                                           new CodeArithmeticInterpreter(unifiedRegisterFileBlock));
  }
  
  private List<InputCodeModel> setUpParsedCode()
  {
    // one:
    // add x1 x3 x2
    // two:
    // sub x1 x3 x2
    // three:
    // mul x1 x3 x2
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelAdd = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("add")
            .hasArguments(Arrays.asList(argumentAdd1, argumentAdd2, argumentAdd3)).build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelSub = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("sub")
            .hasArguments(Arrays.asList(argumentSub1, argumentSub2, argumentSub3)).build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelMul = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("mul")
            .hasArguments(Arrays.asList(argumentMul1, argumentMul2, argumentMul3)).build();
    
    return Arrays.asList(inputCodeModelAdd, inputCodeModelSub, inputCodeModelMul);
  }
  
  private Map<String, Integer> setUpLabels()
  {
    return Map.of("one", 0, "two", 1, "three", 2);
  }
  
  @Test
  public void unconditionalJump_interpret_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("jal")
            .hasArguments(Arrays.asList(argument1, argument2)).build();
    
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("two").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("beq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "two" which is at index 1
    Assert.assertEquals(-2 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("two").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("beq")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("three").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bne")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "three" which is at index 2
    Assert.assertEquals(-1 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("three").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bne")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThan_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("blt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
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
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("blt")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bltu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bltu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bge")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should jump to the label "one" which is at index 0
    Assert.assertEquals(-3 * 4, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3 * 4).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bgeu")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    
    // Should not jump
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 3).isPresent());
  }
}
