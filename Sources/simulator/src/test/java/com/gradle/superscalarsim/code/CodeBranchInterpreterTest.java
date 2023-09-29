package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

public class CodeBranchInterpreterTest
{
  
  @Mock
  private InitLoader initLoader;
  @Mock
  private CodeParser codeParser;
  
  private CodeBranchInterpreter codeBranchInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer")
                                                                  .hasDataType(DataTypeEnum.kInt)
                                                                  .hasRegisterList(
                                                                      Arrays.asList(integer1, integer2, integer3,
                                                                                    integer4))
                                                                  .build();
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Collections.singletonList(integerFile));
    Mockito.when(initLoader.getInstructionFunctionModelList()).thenReturn(setUpInstructions());
    
    List<InputCodeModel> inputCodeModels = setUpParsedCode();
    Mockito.when(codeParser.getParsedCode()).thenReturn(inputCodeModels);
    Mockito.when(codeParser.getLabelPosition(any())).thenCallRealMethod();
    
    this.codeBranchInterpreter = new CodeBranchInterpreter(codeParser, initLoader,
                                                           new UnifiedRegisterFileBlock(initLoader));
  }
  
  @Test
  public void unconditionalJump_interpret_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("jal")
                                                               .hasArguments(Arrays.asList(argument1, argument2))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("two").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("beq")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-3, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("two").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("beq")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("three").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bne")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-1, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpNotEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("three").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bne")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThan_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("blt")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
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
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("blt")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bltu")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpLessThanUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bltu")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bge")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bge")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqual_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bge")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionGreaterTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x4").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bgeu")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionEqualTrue_returnsJumpDifference()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x1").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bgeu")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertEquals(-5, this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).getAsInt());
  }
  
  @Test
  public void conditionalJumpGreaterOrEqualUnsigned_conditionFalse_returnsOne()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("imm").hasValue("one").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
                                                               .hasInstructionName("bgeu")
                                                               .hasArguments(
                                                                   Arrays.asList(argument1, argument2, argument3))
                                                               .build();
    
    Assert.assertFalse(this.codeBranchInterpreter.interpretInstruction(inputCodeModel, 6).isPresent());
  }
  
  
  private List<InputCodeModel> setUpParsedCode()
  {
    // one:
    // add x1 x3 x2
    // two:
    // sub x1 x3 x2
    // three:
    // mul x1 x3 x2
    InputCodeModel inputCodeModelLabelOne = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
        "label").hasCodeLine("one").build();
    
    InputCodeModel inputCodeModelLabelTwo = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
        "label").hasCodeLine("two").build();
    
    InputCodeModel inputCodeModelLabelThree = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName(
        "label").hasCodeLine("three").build();
    
    InputCodeArgument argumentAdd1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentAdd2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentAdd3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelAdd = new InputCodeModelBuilder().hasLoader(initLoader)
                                                                  .hasInstructionName("add")
                                                                  .hasArguments(
                                                                      Arrays.asList(argumentAdd1, argumentAdd2,
                                                                                    argumentAdd3))
                                                                  .build();
    
    InputCodeArgument argumentSub1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentSub2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentSub3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelSub = new InputCodeModelBuilder().hasLoader(initLoader)
                                                                  .hasInstructionName("sub")
                                                                  .hasArguments(
                                                                      Arrays.asList(argumentSub1, argumentSub2,
                                                                                    argumentSub3))
                                                                  .build();
    
    InputCodeArgument argumentMul1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argumentMul2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argumentMul3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x2").build();
    InputCodeModel inputCodeModelMul = new InputCodeModelBuilder().hasLoader(initLoader)
                                                                  .hasInstructionName("mul")
                                                                  .hasArguments(
                                                                      Arrays.asList(argumentMul1, argumentMul2,
                                                                                    argumentMul3))
                                                                  .build();
    
    return Arrays.asList(inputCodeModelLabelOne, inputCodeModelAdd, inputCodeModelLabelTwo, inputCodeModelSub,
                         inputCodeModelLabelThree, inputCodeModelMul);
  }
  
  private List<InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionJal = new InstructionFunctionModelBuilder().hasName("jal")
                                                                                   .hasInputDataType(DataTypeEnum.kInt)
                                                                                   .hasOutputDataType(DataTypeEnum.kInt)
                                                                                   .isInterpretedAs("jump:imm")
                                                                                   .hasSyntax("jal rd imm")
                                                                                   .build();
    
    InstructionFunctionModel instructionBeq = new InstructionFunctionModelBuilder().hasName("beq").hasInputDataType(
        DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("signed:rs1 == rs2").hasSyntax(
        "beq rs1 rs2 imm").build();
    
    InstructionFunctionModel instructionBne = new InstructionFunctionModelBuilder().hasName("bne").hasInputDataType(
        DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("signed:rs1 != rs2").hasSyntax(
        "bne rs1 rs2 imm").build();
    
    InstructionFunctionModel instructionBlt = new InstructionFunctionModelBuilder().hasName("blt").hasInputDataType(
        DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("signed:rs1 < rs2").hasSyntax(
        "blt rs1 rs2 imm").build();
    
    InstructionFunctionModel instructionBltu = new InstructionFunctionModelBuilder().hasName("bltu").hasInputDataType(
        DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("unsigned:rs1 < " + "rs2").hasSyntax(
        "bltu rs1 rs2 imm").build();
    
    InstructionFunctionModel instructionBge = new InstructionFunctionModelBuilder().hasName("bge").hasInputDataType(
        DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("signed:rs1 >= rs2").hasSyntax(
        "bge rs1 rs2 imm").build();
    
    InstructionFunctionModel instructionBgeu = new InstructionFunctionModelBuilder().hasName("bgeu")
                                                                                    .hasInputDataType(DataTypeEnum.kInt)
                                                                                    .hasOutputDataType(
                                                                                        DataTypeEnum.kInt)
                                                                                    .isInterpretedAs(
                                                                                        "unsigned:rs1 " + ">=" + " rs2")
                                                                                    .hasSyntax("bgeu rs1 rs2 imm")
                                                                                    .build();
    
    return Arrays.asList(instructionJal, instructionBeq, instructionBne, instructionBlt, instructionBltu,
                         instructionBge, instructionBgeu);
  }
}
