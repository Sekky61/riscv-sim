package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;

public class CodeParserTest
{
  @Mock
  private InitLoader initLoader;
  
  private InstructionMemoryBlock instructionMemoryBlock;
  
  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
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
    
    Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(Arrays.asList(integerFile, floatFile));
    
    InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName("add")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1+rs2;").hasSyntax("add rd rs1 rs2").build();
    InstructionFunctionModel instrIntToFloat = new InstructionFunctionModelBuilder().hasName("fcvt.w.s")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1;").hasSyntax("fcvt.w.s rd rs1").build();
    InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName("fadd")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kFloat)
            .hasOutputDataType(DataTypeEnum.kFloat).isInterpretedAs("rd=rs1+rs2;").hasSyntax("fadd rd rs1 rs2").build();
    InstructionFunctionModel instructionAddi = new InstructionFunctionModelBuilder().hasName("addi")
            .hasType(InstructionTypeEnum.kArithmetic).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rd=rs1+imm;").hasSyntax("addi rd rs1 imm").build();
    InstructionFunctionModel instructionBranch = new InstructionFunctionModelBuilder().hasName("beq")
            .hasType(InstructionTypeEnum.kJumpbranch).hasInputDataType(DataTypeEnum.kInt)
            .hasOutputDataType(DataTypeEnum.kInt).isInterpretedAs("rs1 == rs2").hasSyntax("beq rs1 rs2 imm").build();
    Mockito.when(initLoader.getInstructionFunctionModelList()).thenReturn(
            Arrays.asList(instructionAdd, instrIntToFloat, instructionFAdd, instructionAddi, instructionBranch));
    Mockito.when(initLoader.getInstructionFunctionModel(any())).thenCallRealMethod();
    
    ArrayList<InitLoader.RegisterMapping> registerAliases = new ArrayList<>();
    registerAliases.add(initLoader.new RegisterMapping("x0", "zero"));
    registerAliases.add(initLoader.new RegisterMapping("x2", "sp"));
    Mockito.when(initLoader.getRegisterAliases()).thenReturn(registerAliases);
    
    this.instructionMemoryBlock = new InstructionMemoryBlock(this.initLoader);
  }
  
  @Test
  public void parseCode_codeValid_returnTrueAndParsedCodeHasThreeInstr()
  {
    String code = """
            add x1 x2 x3
            fcvt.w.s f3 x1
            fadd f1 f2 f3
            """;
    Assert.assertTrue(instructionMemoryBlock.parse(code));
    Assert.assertEquals(3, instructionMemoryBlock.getParsedCode().size());
    Assert.assertTrue(instructionMemoryBlock.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", instructionMemoryBlock.getParsedCode().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", instructionMemoryBlock.getParsedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd", instructionMemoryBlock.getParsedCode().get(2).getInstructionName());
    
    Assert.assertEquals("rd", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(0).getName());
    Assert.assertEquals("rs1", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(1).getName());
    Assert.assertEquals("rs2", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(2).getName());
    
    Assert.assertEquals("x1", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(0).getValue());
    Assert.assertEquals("x2", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(1).getValue());
    Assert.assertEquals("x3", instructionMemoryBlock.getParsedCode().get(0).getArguments().get(2).getValue());
  }
  
  @Test
  public void parseCode_codeWithLabel_returnTrueAndParsedCodeHasSixInstr()
  {
    String code = """
            one:   add x1 x2 x3
            two:   fcvt.w.s f3 x1
            three: fadd f1 f2 f3
            """;
    Assert.assertTrue(instructionMemoryBlock.parse(code));
    Assert.assertEquals(3, instructionMemoryBlock.getParsedCode().size());
    Assert.assertTrue(instructionMemoryBlock.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", instructionMemoryBlock.getParsedCode().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", instructionMemoryBlock.getParsedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd", instructionMemoryBlock.getParsedCode().get(2).getInstructionName());
    
    Assert.assertEquals(0, instructionMemoryBlock.getLabelPosition("one"));
    Assert.assertEquals(4, instructionMemoryBlock.getLabelPosition("two"));
    Assert.assertEquals(8, instructionMemoryBlock.getLabelPosition("three"));
  }
  
  @Test
  public void parseCode_codeWithLabelOnEmptyLine_returnTrueAndParsedCodeHasSixInstr()
  {
    String code = """
            one:
            add x1 x2 x3
            two:
            fcvt.w.s f3 x1
            three:
            fadd f1 f2 f3
            """;
    Assert.assertTrue(instructionMemoryBlock.parse(code));
    Assert.assertEquals(3, instructionMemoryBlock.getParsedCode().size());
    Assert.assertTrue(instructionMemoryBlock.getErrorMessages().isEmpty());
    
    Assert.assertEquals("add", instructionMemoryBlock.getParsedCode().get(0).getInstructionName());
    Assert.assertEquals("fcvt.w.s", instructionMemoryBlock.getParsedCode().get(1).getInstructionName());
    Assert.assertEquals("fadd", instructionMemoryBlock.getParsedCode().get(2).getInstructionName());
    
    Assert.assertEquals(0, instructionMemoryBlock.getLabelPosition("one"));
    Assert.assertEquals(4, instructionMemoryBlock.getLabelPosition("two"));
    Assert.assertEquals(8, instructionMemoryBlock.getLabelPosition("three"));
  }
  
  @Test
  public void parseCode_codeWithRepeatingLabels_returnFalse()
  {
    String code = """
            one:
            add x1 x2 x3
            one:
            fcvt.w.s f3 x1
            one:
            fadd f1 f2 f3
            """;
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    
    Assert.assertEquals(2, this.instructionMemoryBlock.getErrorMessages().size());
    Assert.assertEquals(3, this.instructionMemoryBlock.getErrorMessages().get(0).line);
    Assert.assertEquals(5, this.instructionMemoryBlock.getErrorMessages().get(1).line);
  }
  
  @Test
  public void parseCode_codeWithMissingLabel_returnFalseAndErrorMessageIsSet()
  {
    String code = """
            one:
            add x1 x2 x3
            fcvt.w.s f3 x1
            beq x1 x2 two
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
    ParseError firstError = instructionMemoryBlock.getErrorMessages().get(0);
    Assert.assertEquals(4, firstError.line);
    Assert.assertEquals(11, firstError.columnStart);
    Assert.assertEquals(13, firstError.columnEnd);
  }
  
  @Test
  public void parseCode_multipleLabels_perInstruction()
  {
    String code = """
            one:
            two: three: add x1 x2 x3
            """;
    
    Assert.assertTrue(instructionMemoryBlock.parse(code));
    Assert.assertEquals(1, instructionMemoryBlock.getParsedCode().size());
    Assert.assertEquals(0, instructionMemoryBlock.getLabelPosition("one"));
    Assert.assertEquals(0, instructionMemoryBlock.getLabelPosition("two"));
    Assert.assertEquals(0, instructionMemoryBlock.getLabelPosition("three"));
  }
  
  @Test
  public void parseCode_lessArgumentsThatExpected_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 x2
            fcvt.w.s f3 x1
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_moreArgumentsThatExpected_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 x2 x3
            fcvt.w.s f3 x1 x1
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
    Assert.assertEquals(2, instructionMemoryBlock.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_invalidInstruction_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 x2 x3
            someRandomInstruction f3 x1 # a comment here
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
    Assert.assertEquals(2, instructionMemoryBlock.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_lValueIsDecimal_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1, x2, x3 # commas allowed
            fcvt.w.s f3 x1
            fadd 20 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
    Assert.assertEquals(3, instructionMemoryBlock.getErrorMessages().get(0).line);
  }
  
  @Test
  public void parseCode_lValueIsHexadecimal_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 x2 x3
            fcvt.w.s f3 x1
            fadd 0x20 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_invalidValue_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 value x2
            fcvt.w.s f3 x1
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_immediateInsteadOfRegister_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            add x1 x2 0x01
            fcvt.w.s f3 x1
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_registerInsteadOfImmediate_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            addi x1 x2 x3
            fcvt.w.s f3 x1
            fadd f1 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_multipleErrors_returnsFalseAndErrorMessageIsSet()
  {
    String code = """
            addi x1 x2 x3
            fcvt.w.s f3 x1 x2
            fadd 0x01 f2 f3
            """;
    
    Assert.assertFalse(instructionMemoryBlock.parse(code));
    Assert.assertEquals(0, instructionMemoryBlock.getParsedCode().size());
    
    Assert.assertEquals(3, instructionMemoryBlock.getErrorMessages().size());
  }
  
  @Test
  public void parseCode_codeValid_parsesAliasedRegisters()
  {
    String code = """
            addi sp x3 5
            beq x3 zero 0
            """;
    
    boolean success = instructionMemoryBlock.parse(code);
    
    Assert.assertTrue(success);
    Assert.assertEquals(2, instructionMemoryBlock.getParsedCode().size());
    Assert.assertTrue(instructionMemoryBlock.getErrorMessages().isEmpty());
  }
  
  @Test
  public void parseCode_colonInTheMiddleOfAWord_correctlySplits()
  {
    String code = """
            addi sp x3 5
            anyt:hing  # Takes this as a label and a word 'hing'
            beq x3 zero 0
            """;
    
    boolean success = instructionMemoryBlock.parse(code);
    
    Assert.assertFalse(success);
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
    Assert.assertEquals(2, instructionMemoryBlock.getErrorMessages().get(0).line);
    Assert.assertEquals(6, instructionMemoryBlock.getErrorMessages().get(0).columnStart);
    Assert.assertEquals(9, instructionMemoryBlock.getErrorMessages().get(0).columnEnd);
  }
  
  @Test
  public void parseCode_labelAsArgument_returnsFalse()
  {
    String code = """
            addi sp L: addi sp sp 1
            """;
    
    boolean success = instructionMemoryBlock.parse(code);
    
    // The parser may not recover well, so number of errors is not checked
    Assert.assertFalse(success);
  }
  
  @Test
  public void parseCode_wrongTypeAlias_returnsFalse()
  {
    // sp is an int register alias, using it in a float instruction should fail
    String code = """
            fadd f1 sp f2
            """;
    
    boolean success = instructionMemoryBlock.parse(code);
    
    Assert.assertFalse(success);
    Assert.assertEquals(1, instructionMemoryBlock.getErrorMessages().size());
  }
}
