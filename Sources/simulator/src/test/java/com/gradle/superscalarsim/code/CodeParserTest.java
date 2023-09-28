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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

public class CodeParserTest {
    @Mock
    private InitLoader initLoader;

    private CodeParser codeParser;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        RegisterModel integer1 = new RegisterModel("x1", false, DataTypeEnum.kInt, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel integer2 = new RegisterModel("x2", false, DataTypeEnum.kInt, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel integer3 = new RegisterModel("x3", false, DataTypeEnum.kInt, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel integer4 = new RegisterModel("x4", false, DataTypeEnum.kInt, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterFileModel integerFile = new RegisterFileModelBuilder().hasName("integer")
                .hasDataType(DataTypeEnum.kInt)
                .hasRegisterList(Arrays.asList(integer1, integer2, integer3, integer4))
                .build();

        RegisterModel float1 = new RegisterModel("f1", false, DataTypeEnum.kFloat, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel float2 = new RegisterModel("f2", false, DataTypeEnum.kFloat, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel float3 = new RegisterModel("f3", false, DataTypeEnum.kFloat, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterModel float4 = new RegisterModel("f4", false, DataTypeEnum.kFloat, 0,
                RegisterReadinessEnum.kAssigned);
        RegisterFileModel floatFile = new RegisterFileModelBuilder().hasName("float")
                .hasDataType(DataTypeEnum.kFloat)
                .hasRegisterList(Arrays.asList(float1, float2, float3, float4))
                .build();

        Mockito.when(initLoader.getRegisterFileModelList()).thenReturn(
                Arrays.asList(integerFile, floatFile));

        InstructionFunctionModel instructionAdd = new InstructionFunctionModelBuilder().hasName(
                        "add")
                .hasType(InstructionTypeEnum.kArithmetic)
                .hasInputDataType(DataTypeEnum.kInt)
                .hasOutputDataType(DataTypeEnum.kInt)
                .isInterpretedAs("rd=rs1+rs2;")
                .hasSyntax("add rd rs1 rs2")
                .build();
        InstructionFunctionModel instrIntToFloat = new InstructionFunctionModelBuilder().hasName(
                        "fcvt.w.s")
                .hasType(InstructionTypeEnum.kArithmetic)
                .hasInputDataType(DataTypeEnum.kInt)
                .hasOutputDataType(DataTypeEnum.kFloat)
                .isInterpretedAs("rd=rs1;")
                .hasSyntax("fcvt.w.s rd rs1")
                .build();
        InstructionFunctionModel instructionFAdd = new InstructionFunctionModelBuilder().hasName(
                        "fadd")
                .hasType(InstructionTypeEnum.kArithmetic)
                .hasInputDataType(DataTypeEnum.kFloat)
                .hasOutputDataType(DataTypeEnum.kFloat)
                .isInterpretedAs("rd=rs1+rs2;")
                .hasSyntax("fadd rd rs1 rs2")
                .build();
        InstructionFunctionModel instructionAddi = new InstructionFunctionModelBuilder().hasName(
                        "addi")
                .hasType(InstructionTypeEnum.kArithmetic)
                .hasInputDataType(DataTypeEnum.kInt)
                .hasOutputDataType(DataTypeEnum.kInt)
                .isInterpretedAs("rd=rs1+imm;")
                .hasSyntax("addi rd rs1 imm")
                .build();
        InstructionFunctionModel instructionBranch = new InstructionFunctionModelBuilder().hasName(
                        "beq")
                .hasType(InstructionTypeEnum.kJumpbranch)
                .hasInputDataType(DataTypeEnum.kInt)
                .hasOutputDataType(DataTypeEnum.kInt)
                .isInterpretedAs("rs1 == rs2")
                .hasSyntax("beq rs1 rs2 imm")
                .build();
        Mockito.when(initLoader.getInstructionFunctionModelList())
                .thenReturn(
                        Arrays.asList(instructionAdd, instrIntToFloat, instructionFAdd,
                                instructionAddi, instructionBranch));

        ArrayList<InitLoader.RegisterMapping> registerAliases = new ArrayList<>();
        registerAliases.add(initLoader.new RegisterMapping("x0", "zero"));
        registerAliases.add(initLoader.new RegisterMapping("x2", "sp"));
        Mockito.when(initLoader.getRegisterAliases()).thenReturn(registerAliases);

        this.codeParser = new CodeParser(this.initLoader);
    }

    @Test
    public void parseCode_codeValid_returnTrueAndParsedCodeHasThreeInstr() {
        String code = """
                add x1 x2 x3
                fcvt.w.s f3 x1
                fadd f1 f2 f3
                """;
        Assert.assertTrue(codeParser.parse(code));
        Assert.assertEquals(3, codeParser.getParsedCode().size());
        Assert.assertTrue(codeParser.getErrorMessages().isEmpty());

        Assert.assertEquals("add",
                codeParser.getParsedCode().get(0).getInstructionName());
        Assert.assertEquals("fcvt.w.s",
                codeParser.getParsedCode().get(1).getInstructionName());
        Assert.assertEquals("fadd",
                codeParser.getParsedCode().get(2).getInstructionName());

        Assert.assertEquals("rd",
                codeParser.getParsedCode().get(0).getArguments().get(0).getName());
        Assert.assertEquals("rs1",
                codeParser.getParsedCode().get(0).getArguments().get(1).getName());
        Assert.assertEquals("rs2",
                codeParser.getParsedCode().get(0).getArguments().get(2).getName());

        Assert.assertEquals("x1",
                codeParser.getParsedCode().get(0).getArguments().get(0).getValue());
        Assert.assertEquals("x2",
                codeParser.getParsedCode().get(0).getArguments().get(1).getValue());
        Assert.assertEquals("x3",
                codeParser.getParsedCode().get(0).getArguments().get(2).getValue());
    }

    @Test
    public void parseCode_codeWithLabel_returnTrueAndParsedCodeHasSixInstr() {
        String code = """
                one:   add x1 x2 x3
                two:   fcvt.w.s f3 x1
                three: fadd f1 f2 f3
                """;
        Assert.assertTrue(codeParser.parse(code));
        Assert.assertEquals(6, codeParser.getParsedCode().size());
        Assert.assertTrue(codeParser.getErrorMessages().isEmpty());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(0).getInstructionName());
        Assert.assertEquals("one", codeParser.getParsedCode().get(0).getCodeLine());

        Assert.assertEquals("add",
                codeParser.getParsedCode().get(1).getInstructionName());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(2).getInstructionName());
        Assert.assertEquals("two", codeParser.getParsedCode().get(2).getCodeLine());

        Assert.assertEquals("fcvt.w.s",
                codeParser.getParsedCode().get(3).getInstructionName());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(4).getInstructionName());
        Assert.assertEquals("three", codeParser.getParsedCode().get(4).getCodeLine());

        Assert.assertEquals("fadd",
                codeParser.getParsedCode().get(5).getInstructionName());
    }

    @Test
    public void parseCode_codeWithLabelOnEmptyLine_returnTrueAndParsedCodeHasSixInstr() {
        String code = """
                one:
                add x1 x2 x3
                two:
                fcvt.w.s f3 x1
                three:
                fadd f1 f2 f3
                """;
        Assert.assertTrue(codeParser.parse(code));
        Assert.assertEquals(6, codeParser.getParsedCode().size());
        Assert.assertTrue(codeParser.getErrorMessages().isEmpty());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(0).getInstructionName());
        Assert.assertEquals("one", codeParser.getParsedCode().get(0).getCodeLine());

        Assert.assertEquals("add",
                codeParser.getParsedCode().get(1).getInstructionName());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(2).getInstructionName());
        Assert.assertEquals("two", codeParser.getParsedCode().get(2).getCodeLine());

        Assert.assertEquals("fcvt.w.s",
                codeParser.getParsedCode().get(3).getInstructionName());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(4).getInstructionName());
        Assert.assertEquals("three", codeParser.getParsedCode().get(4).getCodeLine());

        Assert.assertEquals("fadd",
                codeParser.getParsedCode().get(5).getInstructionName());
    }

    @Test
    public void parseCode_codeWithRepeatingLabels_returnTrueAndParsedCodeHasFourInstr() {
        String code = """
                one:
                add x1 x2 x3
                one:
                fcvt.w.s f3 x1
                one:
                fadd f1 f2 f3
                """;
        Assert.assertTrue(codeParser.parse(code));
        Assert.assertEquals(4, codeParser.getParsedCode().size());

        Assert.assertEquals("label",
                codeParser.getParsedCode().get(0).getInstructionName());
        Assert.assertEquals("one", codeParser.getParsedCode().get(0).getCodeLine());

        Assert.assertEquals("add",
                codeParser.getParsedCode().get(1).getInstructionName());
        Assert.assertEquals("fcvt.w.s",
                codeParser.getParsedCode().get(2).getInstructionName());
        Assert.assertEquals("fadd",
                codeParser.getParsedCode().get(3).getInstructionName());

        String errorMessage = "Line 3: Warning - Label \"one\" already exists in current scope, using the first instance.\n";
        Assert.assertEquals(this.codeParser.getErrorMessages().size(), 2);
        Assert.assertEquals(this.codeParser.getErrorMessages().get(0), errorMessage);
    }

    @Test
    public void parseCode_codeWithMissingLabel_returnFalseAndErrorMessageIsSet() {
        String code = """
                one:
                add x1 x2 x3
                fcvt.w.s f3 x1
                beq x1 x2 two
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());
        String errorMessage = "Line 4: Label \"two\" does not exists in current scope.\n";
        Assert.assertEquals(1, codeParser.getErrorMessages().size());
        Assert.assertEquals(errorMessage, codeParser.getErrorMessages().get(0));
    }

    @Test
    public void parseCode_lessArgumentsThatExpected_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 
                fcvt.w.s f3 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_moreArgumentsThatExpected_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 x3 
                fcvt.w.s f3 x1 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_invalidInstruction_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 x3
                someRandomInstruction f3 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_lValueIsDecimal_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 x3
                fcvt.w.s f3 x1
                fadd 20 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_lValueIsHexadecimal_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 x3
                fcvt.w.s f3 x1
                fadd 0x20 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_invalidValue_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 value x2 
                fcvt.w.s f3 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_immediateInsteadOfRegister_returnsFalseAndErrorMessageIsSet() {
        String code = """
                add x1 x2 0x01 
                fcvt.w.s f3 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_registerInsteadOfImmediate_returnsFalseAndErrorMessageIsSet() {
        String code = """
                addi x1 x2 x3 
                fcvt.w.s f3 x1
                fadd f1 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(1, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_multipleErrors_returnsFalseAndErrorMessageIsSet() {
        String code = """
                addi x1 x2 x3 
                fcvt.w.s f3 x1 x2
                fadd 0x01 f2 f3
                """;

        Assert.assertFalse(codeParser.parse(code));
        Assert.assertEquals(0, codeParser.getParsedCode().size());

        Assert.assertEquals(3, codeParser.getErrorMessages().size());
    }

    @Test
    public void parseCode_codeValid_parsesAliasedRegisters() {
        String code = """
                addi sp x3 5
                beq x3 zero 0
                """;

        boolean success = codeParser.parse(code);

        Assert.assertTrue(success);
        Assert.assertEquals(2, codeParser.getParsedCode().size());
        Assert.assertTrue(codeParser.getErrorMessages().isEmpty());
    }
}
