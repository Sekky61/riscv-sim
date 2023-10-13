package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.builders.InstructionFunctionModelBuilder;
import com.gradle.superscalarsim.builders.RegisterFileModelBuilder;
import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.enums.RegisterReadinessEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.RegisterFileModel;
import com.gradle.superscalarsim.models.RegisterModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class CodeParserTest
{
  private CodeParser codeParser;
  
  @Before
  public void setUp()
  {
    RegisterModel integer0 = new RegisterModel("x0", true, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, 0, RegisterReadinessEnum.kAssigned);
    RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer").hasDataType(DataTypeEnum.kInt)
            .hasRegisterList(Arrays.asList(integer0, integer1, integer2, integer3, integer4)).build();
    
    RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterModel float4 = new RegisterModel("f4", false, DataTypeEnum.kFloat, 0, RegisterReadinessEnum.kAssigned);
    RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float").hasDataType(DataTypeEnum.kFloat)
            .hasRegisterList(Arrays.asList(float1, float2, float3, float4)).build();
    
    List<RegisterFileModel> registerFileModelList = new ArrayList<>();
    registerFileModelList.add(integerFile);
    registerFileModelList.add(floatFile);
    
    InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName("add")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1+rs2;").hasArguments("rd,rs1,rs2").build();
    InstructionFunctionModel instrIntToFloat = new InstructionFunctionModelBuilder().hasName("fcvt.w.s")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1;").hasArguments("rd,rs1").build();
    InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName("fadd")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kFloat)
            .hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1+rs2;").hasArguments("rd,rs1,rs2").build();
    InstructionFunctionModel instructionAddi = new InstructionFunctionModelBuilder().hasName("addi")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1+imm;").hasArguments("rd,rs1,imm").build();
    InstructionFunctionModel instructionBranch = new InstructionFunctionModelBuilder().hasName("beq")
            .hasType(InstructionTypeEnum.kJumpbranch).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rs1 == rs2").hasArguments("rs1,rs2,imm").build();
    InstructionFunctionModel instructionStore = new InstructionFunctionModelBuilder().hasName("sw")
            .hasType(InstructionTypeEnum.kLoadstore).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("store word rs2 rs1 imm").hasArguments("rs2,imm(rs1)")
            .build();
    InstructionFunctionModel instructionJarl = new InstructionFunctionModelBuilder().hasName("jalr")
            .hasType(InstructionTypeEnum.kJumpbranch).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("signed:rs1+imm:true")
            .hasArguments("rd,rs1,imm:x1..0").build();
    
    Map<String, InstructionFunctionModel> insModels = new TreeMap<>();
    insModels.put("add", instructionAdd);
    insModels.put("fcvt.w.s", instrIntToFloat);
    insModels.put("fadd", instructionFAdd);
    insModels.put("addi", instructionAddi);
    insModels.put("beq", instructionBranch);
    insModels.put("sw", instructionStore);
    insModels.put("jalr", instructionJarl);
    
    ArrayList<InitLoader.RegisterMapping> registerAliases = new ArrayList<>();
    registerAliases.add(new InitLoader.RegisterMapping("x0", "zero"));
    registerAliases.add(new InitLoader.RegisterMapping("x2", "sp"));
    
    this.codeParser = new CodeParser(insModels, registerFileModelList, registerAliases);
  }
  
  @Test
  public void parseCode_storeSyntax()
  {
    String code = "sw x3, 0(x2)";
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(1, codeParser.getInstructions().size());
    Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
    
    Assert.assertEquals("sw", codeParser.getInstructions().get(0).getInstructionName());
    Assert.assertEquals("x3", codeParser.getInstructions().get(0).getArgumentByName("rs2").getValue());
    Assert.assertEquals("0", codeParser.getInstructions().get(0).getArgumentByName("imm").getValue());
    Assert.assertEquals("x2", codeParser.getInstructions().get(0).getArgumentByName("rs1").getValue());
  }
  
  @Test
  public void parseCode_codeValid_returnTrueAndParsedCodeHasThreeInstr()
  {
    String code = """
            add x1, x2, x3
            fcvt.w.s f3, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(3, codeParser.getInstructions().size());
    Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", codeParser.getInstructions().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", codeParser.getInstructions().get(1).getInstructionName());
    Assert.assertEquals("fadd", codeParser.getInstructions().get(2).getInstructionName());
    
    Assert.assertEquals("x1", codeParser.getInstructions().get(0).getArgumentByName("rd").getValue());
    Assert.assertEquals("x2", codeParser.getInstructions().get(0).getArgumentByName("rs1").getValue());
    Assert.assertEquals("x3", codeParser.getInstructions().get(0).getArgumentByName("rs2").getValue());
  }
  
  @Test
  public void parseCode_codeWithLabel_returnTrueAndParsedCodeHasSixInstr()
  {
    String code = """
            one:   add x1, x2, x3
            two:   fcvt.w.s f3, x1
            three: fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(3, codeParser.getInstructions().size());
    Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", codeParser.getInstructions().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", codeParser.getInstructions().get(1).getInstructionName());
    Assert.assertEquals("fadd", codeParser.getInstructions().get(2).getInstructionName());
    
    // Parser does not deal in bytes, but index offsets
    Assert.assertEquals(0, (int) codeParser.getLabels().get("one"));
    Assert.assertEquals(1, (int) codeParser.getLabels().get("two"));
    Assert.assertEquals(2, (int) codeParser.getLabels().get("three"));
  }
  
  @Test
  public void parseCode_codeWithLabelOnEmptyLine_returnTrueAndParsedCodeHasSixInstr()
  {
    String code = """
            one:
            add x1, x2, x3
            two:
            fcvt.w.s f3, x1
            three:
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(3, codeParser.getInstructions().size());
    Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", codeParser.getInstructions().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", codeParser.getInstructions().get(1).getInstructionName());
    Assert.assertEquals("fadd", codeParser.getInstructions().get(2).getInstructionName());
    
    Assert.assertEquals(0, (int) codeParser.getLabels().get("one"));
    Assert.assertEquals(1, (int) codeParser.getLabels().get("two"));
    Assert.assertEquals(2, (int) codeParser.getLabels().get("three"));
  }
  
  @Test
  public void parseCode_codeWithRepeatingLabels_returnFalse()
  {
    String code = """
            one:
            add x1, x2, x3
            one:
            fcvt.w.s f3, x1
            one:
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    
    Assert.assertEquals(2, this.codeParser.getErrorMessages().size());
    Assert.assertEquals(3, this.codeParser.getErrorMessages().get(0).line);
    Assert.assertEquals(5, this.codeParser.getErrorMessages().get(1).line);
  }
  
  @Test
  public void parseCode_codeWithMissingLabel_returnFalseAndErrorMessageIsSet()
  {
    String code = """
            one:
            add x1, x2, x3
            fcvt.w.s f3, x1
            beq x1, x2, two
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
    ParseError firstError = codeParser.getErrorMessages().get(0);
    Assert.assertEquals(4, firstError.line);
    Assert.assertEquals(13, firstError.columnStart);
    Assert.assertEquals(15, firstError.columnEnd);
  }
  
  @Test
  public void parseCode_multipleLabels_perInstruction()
  {
    String code = """
            one:
            two: three: add x1, x2, x3
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(1, codeParser.getInstructions().size());
    Assert.assertEquals(0, (int) codeParser.getLabels().get("one"));
    Assert.assertEquals(0, (int) codeParser.getLabels().get("two"));
    Assert.assertEquals(0, (int) codeParser.getLabels().get("three"));
  }
  
  @Test
  public void parseCode_lessArgumentsThatExpected_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2
            fcvt.w.s f3, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(2, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_moreArgumentsThatExpected_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, x3
            fcvt.w.s f3, x1, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
    Assert.assertEquals(2, codeParser.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_invalidInstruction_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, x3
            someRandomInstruction f3, x1 # a comment here
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
    Assert.assertEquals(2, codeParser.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_lValueIsDecimal_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, x3 # commas required
            fcvt.w.s f3, x1
            fadd 20, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
    Assert.assertEquals(3, codeParser.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_lValueIsHexadecimal_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, x3
            fcvt.w.s f3, x1
            fadd 0x20, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_invalidValue_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, value, x2
            fcvt.w.s f3, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_immediateInsteadOfRegister_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, 0x01
            fcvt.w.s f3, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_registerInsteadOfImmediate_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            addi x1, x2, x3
            fcvt.w.s f3, x1
            fadd f1, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_multipleErrors_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            addi x1, x2, x3
            fcvt.w.s f3, x1, x2
            fadd 0x01, f2, f3
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(0, codeParser.getInstructions().size());
    
    Assert.assertEquals(3, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_codeValid_parsesAliasedRegisters()
  {
    String code = """
            addi sp, x3, 5
            beq x3, zero, 0
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
    Assert.assertEquals(2, codeParser.getInstructions().size());
    Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
  }
  
  @Test
  public void parseCode_colonInTheMiddleOfAWord_correctlySplits()
  {
    String code = """
            addi sp, x3, 5
            anyt:hing  # Takes this as a label and a word 'hing'
            beq x3, zero, 0
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
    Assert.assertEquals(2, codeParser.getErrorMessages().get(0).line);
    Assert.assertEquals(6, codeParser.getErrorMessages().get(0).columnStart);
    Assert.assertEquals(9, codeParser.getErrorMessages().get(0).columnEnd);
  }
  
  @Test
  public void parseCode_labelAsArgument_returnsFalse()
  {
    String code = """
            addi sp L: addi sp, sp, 1
            """;
    codeParser.parseCode(code);
    
    // The parser may not recover well, so number of errors is not checked
    Assert.assertFalse(codeParser.success());
  }
  
  @Test
  public void parseCode_wrongTypeAlias_returnsFalse()
  {
    // sp is an int register alias, using it in a float instruction should fail
    String code = """
            fadd f1, sp ,f2
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
    Assert.assertEquals(1, codeParser.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_jarl_onlyOneOptionalArgument()
  {
    String code = """
            jalr x1, x2
            """;
    codeParser.parseCode(code);
    
    Assert.assertFalse(codeParser.success());
  }
  
  @Test
  public void parseCode_jarl_correctFullForm()
  {
    String code = """
            jalr x2, x3, 5
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
  }
  
  @Test
  public void parseCode_jarl_correctShortForm()
  {
    String code = """
            jalr x4
            """;
    codeParser.parseCode(code);
    
    Assert.assertTrue(codeParser.success());
  }
}
