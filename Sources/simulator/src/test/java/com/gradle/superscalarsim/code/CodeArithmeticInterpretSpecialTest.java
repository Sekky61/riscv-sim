package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.*;
import com.gradle.superscalarsim.enums.DataTypeEnum;
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

public class CodeArithmeticInterpretSpecialTest
{
  @Mock
  private InitLoader initLoader;

  private CodeArithmeticInterpreter codeArithmeticInterpreter;

  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModelBuilder().hasName("x1").HasValue(0).IsConstant(false).build();
    RegisterModel integer2 = new RegisterModelBuilder().hasName("x2").HasValue(25).IsConstant(false).build();
    RegisterModel integer3 = new RegisterModelBuilder().hasName("x3").HasValue(6).IsConstant(false).build();
    RegisterModel integer4 = new RegisterModelBuilder().hasName("x4").HasValue(11).IsConstant(false).build();
    RegisterModel integer5 = new RegisterModelBuilder().hasName("x5").HasValue(-2).IsConstant(false).build();
    RegisterModel integer6 = new RegisterModelBuilder().hasName("x6").HasValue(-20).IsConstant(false).build();
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer")
        .hasDataType(DataTypeEnum.kInt)
        .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6))
        .build();

    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Collections.singletonList(integerFile));
    Mockito.when(initLoader.getInstructionFunctionModelList()).thenReturn(setUpInstructions());

    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(initLoader,new PrecedingTable(), new UnifiedRegisterFileBlock(initLoader));
  }

  @Test
  public void interpretInstruction_multipleLineInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeArgument argument4 = new InputCodeArgumentBuilder().hasName("rs3").hasValue("x4").build();
    InputCodeArgument argument5 = new InputCodeArgumentBuilder().hasName("rs4").hasValue("x5").build();
    InputCodeArgument argument6 = new InputCodeArgumentBuilder().hasName("rs5").hasValue("x6").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("multipleLines")
        .hasArguments(Arrays.asList(argument1, argument2, argument3, argument4, argument5, argument6))
        .build();
    Assert.assertEquals(2, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.01);
  }

  @Test
  public void interpretInstruction_multipleBracketsInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x4").build();
    InputCodeArgument argument4 = new InputCodeArgumentBuilder().hasName("rs3").hasValue("x5").build();
    InputCodeArgument argument5 = new InputCodeArgumentBuilder().hasName("rs4").hasValue("x2").build();
    InputCodeArgument argument6 = new InputCodeArgumentBuilder().hasName("rs5").hasValue("x6").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("multipleBrackets")
        .hasArguments(Arrays.asList(argument1, argument2, argument3, argument4, argument5, argument6))
        .build();
    Assert.assertEquals(15, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.01);
  }

  @Test
  public void interpretInstruction_multipleInstructionsInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("multipleInstructions")
        .hasArguments(Arrays.asList(argument1, argument2, argument3))
        .build();
    Assert.assertEquals(154, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.01);
  }

  @Test
  public void interpretInstruction_arrayOperationInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("bitArrayOperation")
        .hasArguments(Arrays.asList(argument1, argument2, argument3))
        .build();
    Assert.assertEquals(7, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel), 0.01);
  }

  @Test
  public void interpretInstruction_set31ThenSetZeroes_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeModel inputCodeModel1 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("set31")
        .hasArguments(Collections.singletonList(argument1))
        .build();

    Assert.assertEquals(31, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel1), 0.01);

    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeModel inputCodeModel2 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("setZeroes3to1")
        .hasArguments(Collections.singletonList(argument2))
        .build();

    Assert.assertEquals(17, this.codeArithmeticInterpreter.interpretInstruction(inputCodeModel2), 0.01);
  }

  private List<InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionMultipleLines = new InstructionFunctionModelBuilder().hasName("multipleLines")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=rs1+rs2;rd=rd-rs3;rd=rd*rs4;rd=rd/rs5;")
        .hasSyntax("multipleLines rd rs1 rs2 rs3 rs4 rs5")
        .build();

    InstructionFunctionModel instructionMultipleBrackets = new InstructionFunctionModelBuilder().hasName("multipleBrackets")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=rs1+(rs2-(rs3*(rs4/rs5)));")
        .hasSyntax("multipleBrackets rd rs1 rs2 rs3 rs4 rs5")
        .build();

    InstructionFunctionModel instructionMultipleInstructions = new InstructionFunctionModelBuilder().hasName("multipleInstructions")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=rs1*rs2+rs1/rs2;")
        .hasSyntax("multipleInstructions rd rs1 rs2")
        .build();

    InstructionFunctionModel instructionArrayOperations = new InstructionFunctionModelBuilder().hasName("bitArrayOperation")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd[2:0]=rs1[3:0]|rs2[3:0];")
        .hasSyntax("bitArrayOperation rd rs1 rs2")
        .build();

    InstructionFunctionModel instructionSet31 = new InstructionFunctionModelBuilder().hasName("set31")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd=31")
        .hasSyntax("set31 rd")
        .build();

    InstructionFunctionModel instructionSetZeroesOnIndexFrom3to1 = new InstructionFunctionModelBuilder().hasName("setZeroes3to1")
        .hasInputDataType(DataTypeEnum.kInt)
        .hasOutputDataType(DataTypeEnum.kInt)
        .isInterpretedAs("rd[3:1]=0")
        .hasSyntax("setZeroes3to1 rd")
        .build();


    return Arrays.asList(
        instructionMultipleLines,
        instructionMultipleBrackets,
        instructionMultipleInstructions,
        instructionArrayOperations,
        instructionSet31,
        instructionSetZeroesOnIndexFrom3to1);
  }
}
