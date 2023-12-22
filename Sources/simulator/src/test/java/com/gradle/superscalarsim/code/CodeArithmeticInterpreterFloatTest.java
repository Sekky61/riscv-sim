package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.UnifiedRegisterFileBlock;
import com.gradle.superscalarsim.builders.InputCodeArgumentBuilder;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.enums.RegisterTypeEnum;
import com.gradle.superscalarsim.factories.RegisterModelFactory;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InputCodeArgument;
import com.gradle.superscalarsim.models.InputCodeModel;
import com.gradle.superscalarsim.models.SimCodeModel;
import com.gradle.superscalarsim.models.register.RegisterFileModel;
import com.gradle.superscalarsim.models.register.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeArithmeticInterpreterFloatTest
{
  
  private InitLoader initLoader;
  
  private CodeArithmeticInterpreter codeArithmeticInterpreter;
  
  UnifiedRegisterFileBlock urf;
  
  @Before
  public void setUp()
  {
    RegisterModel float1 = new RegisterModel("f1", false, RegisterTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, RegisterTypeEnum.kFloat, 5.5f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, RegisterTypeEnum.kFloat, 3.125f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, RegisterTypeEnum.kFloat, 12.25f,
                                             RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(RegisterTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    this.initLoader = new InitLoader();
    urf             = new UnifiedRegisterFileBlock(initLoader, 320, new RegisterModelFactory());
    // This adds the reg files, but also creates speculative registers!
    urf.setRegistersWithList(new ArrayList<>());
    urf.loadRegisters(List.of(floatFile), new RegisterModelFactory());
    
    this.codeArithmeticInterpreter = new CodeArithmeticInterpreter();
  }
  
  @Test
  public void interpretInstruction_floatAddInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fadd.s")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(8.625, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatSubInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("f3").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("f2").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fsub.s")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(-2.375, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatMulInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fmul.s")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(17.1875, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
  
  @Test
  public void interpretInstruction_floatDivInstruction_returnValid()
  {
    InputCodeArgument argument1 = new InputCodeArgumentBuilder(urf).hasName("rd").hasRegister("f1").build();
    InputCodeArgument argument2 = new InputCodeArgumentBuilder(urf).hasName("rs1").hasRegister("f2").build();
    InputCodeArgument argument3 = new InputCodeArgumentBuilder(urf).hasName("rs2").hasRegister("f3").build();
    InputCodeModel inputCodeModel = new InputCodeModelBuilder().hasLoader(initLoader).hasInstructionName("fdiv.s")
            .hasArguments(Arrays.asList(argument1, argument2, argument3)).build();
    SimCodeModel codeModel = new SimCodeModel(inputCodeModel, 0);
    
    Expression.Variable v = this.codeArithmeticInterpreter.interpretInstruction(codeModel);
    Assert.assertEquals(1.76, (float) v.value.getValue(DataTypeEnum.kFloat), 0.01);
  }
}
