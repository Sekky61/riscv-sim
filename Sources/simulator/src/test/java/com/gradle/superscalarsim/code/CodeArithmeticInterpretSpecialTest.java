package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

public class CodeArithmeticInterpretSpecialTest
{
  @Mock
  private InitLoader initLoader;
  
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 25, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 6, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, 11, RegisterReadinessEnum.kAssigned);
    RegisterModel integer5 = new RegisterModel("x5", false, DataTypeEnum.kInt, -2, RegisterReadinessEnum.kAssigned);
    RegisterModel integer6 = new RegisterModel("x6", false, DataTypeEnum.kInt, -20, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4, integer5, integer6)).build();
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Collections.singletonList(integerFile));
    Mockito.when(initLoader.getInstructionFunctionModels()).thenReturn(setUpInstructions());
    Mockito.when(initLoader.getInstructionFunctionModel(any())).thenCallRealMethod();
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter(new UnifiedRegisterFileBlock(initLoader));
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
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("multipleLines")
            .hasArguments(Arrays.asList(argument1, argument2, argument3, argument4, argument5, argument6)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(2, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
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
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("multipleBrackets")
            .hasArguments(Arrays.asList(argument1, argument2, argument3, argument4, argument5, argument6)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(15, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_multipleInstructionsInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("multipleInstructions").hasArguments(Arrays.asList(argument1, argument2, argument3))
            .build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(154, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_arrayOperationInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rs1").hasValue("x2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder().hasName("rs2").hasValue("x3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("bitArrayOperation").hasArguments(Arrays.asList(argument1, argument2, argument3))
            .build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(7, (int) v.value.getValue(DataTypeEnum.kInt), 0.01);
  }
  
  @Test
  public void interpretInstruction_set31ThenSetZeroes_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeModel inputCodeModel1 = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("set31")
            .hasArguments(Collections.singletonList(argument1)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel1, 0, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(31, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
    
    InputCodeArgument argument2 = new InputCodeArgumentBuilder().hasName("rd").hasValue("x1").build();
    InputCodeModel inputCodeModel2 = new InputCodeModelBuilder().hasLoader(initLoader)
            .hasInstructionName("setZeroes3to1").hasArguments(Collections.singletonList(argument2)).build();
    SimCodeModel codeModel2 = new SimCodeModel(inputCodeModel2, 0, 0);
    
    Expression.Variable v2 = this.codeArithmeticInterpreter.interpretInstruction(codeModel2);
    Assert.assertEquals(17, (int) v2.value.getValue(DataTypeEnum.kInt), 0.01);
  }
  
  private Map<String, InstructionFunctionModel> setUpInstructions()
  {
    InstructionFunctionModel instructionMultipleLines = new InstructionFunctionModelBuilder().hasName("multipleLines")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1+rs2;rd=rd-rs3;rd=rd*rs4;rd=rd/rs5;").hasArguments("rd,rs1,rs2,rs3,rs4,rs5")
            .build();
    
    InstructionFunctionModel instructionMultipleBrackets = new InstructionFunctionModelBuilder().hasName(
                    "multipleBrackets").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1+(rs2-(rs3*(rs4/rs5)));").hasArguments("rd,rs1,rs2,rs3,rs4,rs5").build();
    
    InstructionFunctionModel instructionMultipleInstructions = new InstructionFunctionModelBuilder().hasName(
                    "multipleInstructions").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd=rs1*rs2+rs1/rs2;").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionArrayOperations = new InstructionFunctionModelBuilder().hasName(
                    "bitArrayOperation").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd[2:0]=rs1[3:0]|rs2[3:0];").hasArguments("rd,rs1,rs2").build();
    
    InstructionFunctionModel instructionSet31 = new InstructionFunctionModelBuilder().hasName("set31")
            .hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=31")
            .hasArguments("set31 rd").build();
    
    InstructionFunctionModel instructionSetZeroesOnIndexFrom3to1 = new InstructionFunctionModelBuilder().hasName(
                    "setZeroes3to1").hasInputDataType(DataTypeEnum.kInt).hasOutputDataType(DataTypeEnum.kInt)
            .isInterpretedAs("rd[3:1]=0").hasArguments("rd").build();
    
    
    return Map.ofEntries(Map.entry("multipleLines", instructionMultipleLines),
                         Map.entry("multipleBrackets", instructionMultipleBrackets),
                         Map.entry("multipleInstructions", instructionMultipleInstructions),
                         Map.entry("bitArrayOperation", instructionArrayOperations),
                         Map.entry("set31", instructionSet31),
                         Map.entry("setZeroes3to1", instructionSetZeroesOnIndexFrom3to1));
  }
}
