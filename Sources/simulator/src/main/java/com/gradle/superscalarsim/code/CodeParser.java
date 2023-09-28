/**
 * @file CodeParser.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author Michal Majer
 * Faculty of Information Technology
 * Brno University of Technology
 * xmajer21@stud.fit.vutbr.cz
 * @brief File contains parser for user made code
 * @date 10 November  2020 18:00 (created) \n
 * 12 May       2020 11:00 (revised)
 * 26 Sep      2023 10:00 (revised)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2020  Jan Vavra
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.enums.InstructionTypeEnum;
import com.gradle.superscalarsim.loader.InitLoader;
import com.gradle.superscalarsim.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @class CodeParser
 * @brief Parses provided string of code
 * @detail Class which provides parsing logic for written code in assembly. Code shall contain instructions loaded
 * by InitLoader. Class also verifies parsed code and in case of failure, error message will be provided with
 * line number and cause.
 */
public class CodeParser {
    /// Pattern for matching hexadecimal values in argument
    private final transient Pattern hexadecimalPattern;
    /// Pattern for matching decimal values in argument
    private final transient Pattern decimalPattern;
    /// Pattern for matching register tags in instructionSyntax value of instruction
    private final transient Pattern registerPattern;
    /// Pattern for splitting instruction arguments
    private final transient Pattern splitArgsPattern;
    /// Pattern for matching immediate tags in instructionSyntax value of instruction
    private final transient Pattern immediatePattern;
    /// Pattern for matching labels in code
    private final transient Pattern labelPattern;
    /// Pattern for matching labels in code
    private final transient Pattern parsedLabelPattern;
    /// Pattern for matching comments
    private final transient Pattern commentPattern;
    /// Holds error messages, if any occurs, otherwise is empty
    private final List<String> errorMessages;
    /// InitLoader object with loaded instructions and registers
    private InitLoader initLoader;
    /// List of parsed instructions
    private List<InputCodeModel> parsedCode;
    /// Counter for number of lines processed
    private int codeLineNumber;

    /**
     * @param [in] initLoader - InitLoader object with loaded instructions and registers
     * @brief Constructor
     */
    public CodeParser(final InitLoader initLoader) {
        this.initLoader = initLoader;
        this.errorMessages = new ArrayList<>();
        this.codeLineNumber = 1;
        this.parsedCode = new ArrayList<>();
        this.decimalPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        this.hexadecimalPattern = Pattern.compile("0x\\p{XDigit}+");
        this.registerPattern = Pattern.compile("r[d,s]\\d*");
        // Any number of spaces, at most one comma between them
        this.splitArgsPattern = Pattern.compile("\\s*[,\\s]\\s*");
        this.immediatePattern = Pattern.compile("imm\\d*");
        // Anything with a colon at the end
        this.labelPattern = Pattern.compile("^[a-zA-Z0-9\\.]+:");
        this.parsedLabelPattern = Pattern.compile("^[a-zA-Z0-9]+$");
        // Hashtag and everything after it until end (must be applied to a line)
        this.commentPattern = Pattern.compile("#.*$");
    }// end of Constructor
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] codeString - String holding unparsed code
     * @return True, in case no errors arises, otherwise false
     * @brief Parse whole string with code
     */
    public boolean parse(final String codeString) {
        this.errorMessages.clear();
        this.parsedCode.clear();
        this.codeLineNumber = 1;
        List<String> codeLines = Arrays.asList(codeString.split("\\r?\\n"));
        boolean result = true;
        codeLines.replaceAll(String::trim);

        for (String codeLine : codeLines) {
            result = parseLine(codeLine) && result;
            this.codeLineNumber++;
        }
        result = areLabelsMissing() && result;
        if (!result) {
            parsedCode.clear();
        }
        return result;
    }// end of parse
    //-------------------------------------------------------------------------------------------

    /**
     * @return List of parsed instructions
     * @brief Get list of parsed instructions
     */
    public List<InputCodeModel> getParsedCode() {
        return parsedCode;
    }// end of getParsedCode
    //-------------------------------------------------------------------------------------------

    public void setParsedCode(List<InputCodeModel> parsedCode) {
        this.parsedCode = parsedCode;
    }

    /**
     * Get the position of the label in the code. (Assumes the label exists)
     * @param label Label name to search for. (example: "loop")
     * @return Position of the label in the code, or -1 if the label does not exist.
     */
    public int getLabelPosition(String label) {
        InputCodeModel labelCode = getParsedCode().stream().filter(
                inputCodeModel -> inputCodeModel.getInstructionName().equals(
                        "label") && inputCodeModel.getCodeLine().equals(
                        label)).findFirst().orElse(null);
        return getParsedCode().indexOf(labelCode);
    }
    //-------------------------------------------------------------------------------------------

    /**
     * @return Error messages
     * @brief Get list of error messages in case of load failure
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }// end of getErrorMessage
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] codeLine - Line with code
     * @return Success - True, in case no errors arises, otherwise false
     * @brief Parse exactly one line of code.
     * A line of code can include optional label, optional instruction and optional comment
     */
    private boolean parseLine(final String codeLine) {
        // Copy the line - string is immutable, so copy is performed
        String line = codeLine;

        if (codeLine.isEmpty() || commentPattern.matcher(codeLine).matches()) {
            return true;
        }

        // Remove comment and trim
        Matcher commentMatcher = this.commentPattern.matcher(line);
        line = commentMatcher.replaceAll("");
        String cleanedCodeLine = line.trim();

        // First try to parse label and add it
        Matcher labelMatcher = this.labelPattern.matcher(cleanedCodeLine);
        boolean hasLabel = labelMatcher.find();
        if (hasLabel) {
            String label = labelMatcher.group(0);
            insertLabel(label);
            // Remove label from code line
            cleanedCodeLine = labelMatcher.replaceFirst("");
        }

        if (cleanedCodeLine.isEmpty()) {
            return true;
        }

        // Line is not empty, so it _must_ contain instruction
        int insertionIndex = this.parsedCode.size();
        InputCodeModel codeModel = parseInstruction(cleanedCodeLine, insertionIndex);
        if (codeModel == null) {
            return false;
        }

        parsedCode.add(codeModel);
        return true;
    }// end of parseLine
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in,out] codeModel - Parsed code model
     * @return True, in case of valid instruction and its arguments, otherwise false
     * @brief Validate parsed instruction
     */
    private boolean validateCodeModel(InputCodeModel codeModel) {
        // Instruction validation -> instruction exists
        InstructionFunctionModel instruction = codeModel.getInstructionFunctionModel();
        if (instruction == null) {
            this.errorMessages.add(
                    "Line " + this.codeLineNumber + ": Instruction does not exists.\n");
            return false;
        }

        // Number of arguments validation
        List<String> splitSyntax = new LinkedList<>(
                Arrays.asList(instruction.getInstructionSyntax().split(" ")));
        splitSyntax.remove(0);
        int syntaxArgumentSize = splitSyntax.size();
        int codeModelArgumentSize = codeModel.getArguments().size();

        if (syntaxArgumentSize != codeModelArgumentSize) {
            this.errorMessages.add(
                    "Line " + this.codeLineNumber + ": Invalid number of arguments. Expected: " +
                            syntaxArgumentSize + ", got: " + codeModelArgumentSize + ".\n");
            return false;
        }

        // Validation of separate arguments
        for (int i = 0; i < codeModel.getArguments().size(); i++) {
            DataTypeEnum instructionDataType =
                    isLValue(splitSyntax.get(i), instruction.getInterpretableAs(),
                            instruction.getInstructionType()) ?
                            instruction.getOutputDataType() :
                            instruction.getInputDataType();
            // "isLValue" is true only for the first argument of the instruction
            boolean isArgumentValid = validateArgument(codeModel.getArguments().get(i),
                    instruction.getInstructionType(),
                    instructionDataType,
                    i == 0);
            if (!isArgumentValid) {
                return false;
            }
        }
        return true;
    }// end of validateCodeModel
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] tag        - Associated argument tag from instruction syntax, have the same position as input argument
     * @param [in] interpretableAs - String of commands stored in instruction, used for interpretation
     * @return True if argument tag is lValue, otherwise false
     * @brief Checks if current argument is lValue
     */
    private boolean isLValue(final String tag, final String interpretableAs, final InstructionTypeEnum instructionType) {
        if (instructionType == InstructionTypeEnum.kLoadstore) {
            int wherePosition = 2;
            String[] splitInterpretable = interpretableAs.split(" ");
            return tag.equals(splitInterpretable[wherePosition]);
        } else {
            String[] splitInterpretable = interpretableAs.split(";");
            for (String command : splitInterpretable) {
                String commandLValue = command.split("=")[0].trim();
                if (commandLValue.equals(tag)) {
                    return true;
                }
            }
        }
        return false;
    }// end of isLValue
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] inputCodeArgument   - Argument to be verified
     * @param [in] instructionType     - Type of the instruction
     * @param [in] instructionDataType - Data type of an argument according to instruction template
     * @param [in] isLValue            - True, if the currently verified argument lvalue, false otherwise
     * @return True in case of valid argument, false otherwise
     * @brief Validates argument's value and data type
     */
    private boolean validateArgument(final InputCodeArgument inputCodeArgument,
                                     final InstructionTypeEnum instructionType,
                                     final DataTypeEnum instructionDataType,
                                     final boolean isLValue) {
        String argumentName = inputCodeArgument.getName();
        String argumentValue = inputCodeArgument.getValue();
        boolean isDirectValue = isNumeralLiteral(argumentValue);

        if (this.immediatePattern.matcher(argumentName).matches()) {
            //if instruction is jump/branch, expected imm value can be a label or a direct value in the +-1MiB range
            // numeral values are already checked in the previous step
            // let's check if the value is a label
            if (!isDirectValue && instructionType == InstructionTypeEnum.kJumpbranch) {
                isDirectValue = this.parsedLabelPattern.matcher(argumentValue).matches();
            }
            return checkImmediateArgument(argumentValue, isLValue, isDirectValue);
        } else if (this.registerPattern.matcher(argumentName).matches()) {
            // A register (rd, rs1, ...)
            return checkRegisterArgument(argumentValue, instructionDataType, isLValue,
                    isDirectValue);
        }
        return false;
    }// end of validateArgument
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] argumentValue - Value of an argument in string format
     * @param [in] isLValue      - True, if the currently verified argument lvalue, false otherwise
     * @param [in] isDirectValue - True, if argument is decimal or hexadecimal , false otherwise
     * @return True if argument is valid immediated value, otherwise false
     * @brief Verifies if argument is immediate value
     */
    private boolean checkImmediateArgument(final String argumentValue,
                                           final boolean isLValue,
                                           final boolean isDirectValue) {
        if (isLValue) {
            this.errorMessages.add(
                    "Line " + this.codeLineNumber + ": LValue cannot be immediate value.\n");
            return false;
        } else if (!isDirectValue) {
            this.errorMessages.add("Line " + this.codeLineNumber +
                    ": Expecting immediate value, got :" + argumentValue + ".\n");
            return false;
        }
        return true;
    }// end of checkImmediateArgument
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] argumentValue    - Value of an argument in string format
     * @param [in] argumentDataType - Expected data type of an argument
     * @param [in] isLValue         - True, if the currently verified argument lvalue, false otherwise
     * @param [in] isDirectValue    - True, if argument is decimal or hexadecimal , false otherwise
     * @return True if argument is valid register, otherwise false
     * @brief Verifies if argument is register
     */
    private boolean checkRegisterArgument(final String argumentValue,
                                          final DataTypeEnum argumentDataType,
                                          final boolean isLValue,
                                          final boolean isDirectValue) {
        if (isDirectValue && isLValue) {
            this.errorMessages.add(
                    "Line " + this.codeLineNumber + ": LValue cannot be immediate value.\n");
            return false;
        }
        if (isDirectValue) {
            this.errorMessages.add(
                    "Line " + this.codeLineNumber + ": Expecting register, got :" + argumentValue + ".\n");
            return false;
        }
        for (RegisterFileModel registerFileModel : initLoader.getRegisterFileModelList()) {
            if (!checkDatatype(argumentDataType,
                    registerFileModel.getDataType())) {
                // Incorrect data type in this register file, skip
                continue;
            }
            RegisterModel regModel = registerFileModel.getRegister(argumentValue);
            if (regModel != null) {
                return true;
            }
        }
        this.errorMessages.add(
                "Line " + this.codeLineNumber + ": Argument \"" + argumentValue + "\" is not a register nor value.\n");
        return false;
    }// end of checkRegisterArgument
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] argumentDataType - The datatype of an argument
     * @param [in] registerDataType - The datatype of an register file
     * @return True if argument can fit inside the register, false otherwise
     * @brief Check argument and register data types if they fit within each other
     */
    private boolean checkDatatype(final DataTypeEnum argumentDataType, final DataTypeEnum registerDataType) {
        return switch (argumentDataType) {
            case kInt ->
                    registerDataType == DataTypeEnum.kInt || registerDataType == DataTypeEnum.kLong;
            case kLong -> registerDataType == DataTypeEnum.kLong;
            case kFloat ->
                    registerDataType == DataTypeEnum.kFloat || registerDataType == DataTypeEnum.kDouble;
            case kDouble -> registerDataType == DataTypeEnum.kDouble;
            case kSpeculative -> false;
        };
    }// end of checkDatatype
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] codeLine - Line of code to be parsed
     * @param [in] insertionIndex - Index of the instruction in the code
     * @return InputCodeModel object containing parsed code of a valid instruction, or null in case of error
     * @brief Parses instruction from code line. Must not contain label. Must not contain comment.
     */
    private InputCodeModel parseInstruction(final String codeLine, final int insertionIndex) {
        String cleanedCodeLine = codeLine.trim();
        if (cleanedCodeLine.isEmpty()) {
            return null;
        }

        // Split the line to instruction name and array of arguments
        List<String> splitCodeLine = new LinkedList<>(
                Arrays.asList(cleanedCodeLine.split("\\s+", 2)));
        if (splitCodeLine.size() > 2) {
            return null;
        }
        String instructionName = splitCodeLine.get(0);
        String argumentsString = splitCodeLine.get(1);

        // Split arguments
        String[] arguments = new String[0];
        if (argumentsString != null) {
            arguments = this.splitArgsPattern.split(argumentsString);
        }

        // Instruction validation -> instruction exists
        InstructionFunctionModel instDescription = initLoader.getInstructionFunctionModelList().stream()
                .filter(instr -> instructionName.equals(
                        instr.getName())).findFirst().orElse(null);
        if (instDescription == null) {
            // Add error
            this.errorMessages.add("Line " + this.codeLineNumber +
                    ": Error - unknown instruction '" + instructionName + "'\n"
            );
            return null;
        }

        // Check the arguments based on the description
        List<String> splitSyntax = new LinkedList<>(
                Arrays.asList(instDescription.getInstructionSyntax().split(" ")));
        splitSyntax.remove(0);
        int expectedNumOfArgs = splitSyntax.size();

        if (arguments.length != expectedNumOfArgs) {
            this.errorMessages.add("Line " + this.codeLineNumber +
                    ": Error - instruction '" + instructionName + "' expected "
                    + expectedNumOfArgs + " arguments, got " + arguments.length + "\n"
            );
            return null;
        }

        List<InputCodeArgument> parsedArgs = new ArrayList<>();
        // Add key:value pairs (for example rd: x0)
        for (int i = 0; i < arguments.length; i++) {
            parsedArgs.add(new InputCodeArgument(splitSyntax.get(i), arguments[i]));
        }

        InputCodeModel instruction = new InputCodeModel(
                instDescription,
                instructionName,
                cleanedCodeLine,
                parsedArgs,
                instDescription.getInstructionType(),
                instDescription.getOutputDataType(),
                insertionIndex
        );

        // Now validate (semantics)
        if (!validateCodeModel(instruction)) {
            return null;
        }

        return instruction;
    }// end of processCodeLine
    //-------------------------------------------------------------------------------------------

    /**
     * @param [in] label - Name of the branch label, including the colon at the end
     * @brief Inserts label inside a code as InputCodeModel
     */
    private void insertLabel(final String label) {
        String labelWithoutColon = label.substring(0, label.length() - 1);
        if (this.parsedCode.stream().anyMatch(
                code -> code.getCodeLine().equals(labelWithoutColon))) {
            this.errorMessages.add("Line " + this.codeLineNumber +
                    ": Warning - Label \"" + labelWithoutColon + "\" already exists in current scope, using the first instance.\n"
            );
            return;
        }
        int insertionIndex = this.parsedCode.size();
        InputCodeModel inputCodeModel =
                new InputCodeModel(null, "label", labelWithoutColon, null,
                        InstructionTypeEnum.kLabel, null, insertionIndex);
        this.parsedCode.add(inputCodeModel);
    }// end of insertLabel
    //-------------------------------------------------------------------------------------------

    /**
     * @return True if all labels are defined, false otherwise
     * @brief Checks if jump instructions have defined labels in parsed code
     */
    private boolean areLabelsMissing() {
        this.codeLineNumber = 1;
        for (InputCodeModel codeModel : this.parsedCode) {
            InstructionFunctionModel instruction = initLoader.getInstructionFunctionModelList().stream()
                    .filter(instr -> codeModel.getInstructionName().equals(
                            instr.getName()))
                    .findFirst()
                    .orElse(null);
            if (instruction != null && instruction.getInstructionType() == InstructionTypeEnum.kJumpbranch) {
                InputCodeArgument jumpBranchArgument = codeModel.getArguments().stream()
                        .filter(arg -> arg.getName().equals("imm"))
                        .findFirst().orElse(null);
                if (jumpBranchArgument != null) {
                    // Checking if jump target exists: two cases - label or literal value
                    boolean isLabelText = isLabel(jumpBranchArgument.getValue());
                    if (!isLabelText) {
                        // It must be literal value
                        continue;
                    }
                    boolean labelExists = this.parsedCode.stream()
                            .anyMatch(code -> code.getCodeLine().equals(
                                    jumpBranchArgument.getValue()));
                    if (!labelExists) {
                        this.errorMessages.add("Line " + this.codeLineNumber +
                                ": Label \"" + jumpBranchArgument.getValue() +
                                "\" does not exists in current scope.\n"
                        );
                        return false;
                    }
                } else {
                    InputCodeArgument jumpBranchRegisterDestination = codeModel.getArguments().stream()
                            .filter(arg -> arg.getName().equals("rs1"))
                            .findFirst().orElse(null);
                    if (jumpBranchRegisterDestination == null) {
                        this.errorMessages.add("Line " + this.codeLineNumber +
                                ": There was something wrong with the label\n"
                        );
                        return false;
                    }
                }
            }
            this.codeLineNumber++;
        }
        return true;
    }// end of areLabelsMissing

    private boolean isNumeralLiteral(String argValue) {
        return this.hexadecimalPattern.matcher(argValue).matches() ||
                this.decimalPattern.matcher(argValue).matches();
    }

    /**
     * @param argValue - The argument value to be checked (e.g. "loop", "26")
     * @return true if the argument is a label, false otherwise
     * @brief Checks if the argument is a label
     */
    private boolean isLabel(String argValue) {
        if (isNumeralLiteral(argValue)) {
            return false;
        }
        return this.parsedLabelPattern.matcher(argValue).matches();
    }

}
