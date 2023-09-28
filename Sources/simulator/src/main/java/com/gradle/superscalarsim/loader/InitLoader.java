/**
 * @file InitLoader.java
 * @author Jan Vavra \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xvavra20@fit.vutbr.cz
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * @brief File contains initialization loader of registers and instructions used in simulation
 * @date 27 October  2020 15:00 (created) \n
 * 11 November 2020 11:30 (revised)
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
package com.gradle.superscalarsim.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.InstructionFunctionModel;
import com.gradle.superscalarsim.models.RegisterFileModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @class InitLoader
 * @brief Loads necessary objects for simulation
 * @details Class which loads register files and instruction set into a memory for use in simulation.\n
 *         Class shall be used as a first thing before any other work on simulation code
 */
public class InitLoader {
    /// Holds loaded register files
    private List<RegisterFileModel> registerFileModelList;
    /// Holds loaded ISA for interpreting values and action by simulation code
    private List<InstructionFunctionModel> instructionFunctionModelList;
    /// Name of directory with register files
    private String registerFileDir;

    /// Path to file with instructions definitions
    private String instructionsFilePath;
    /**
     * @brief File path with register aliases
     * File structure: array of objects with keys "register" and "alias"
     */
    private String registerAliasesFilePath;

    /**
     * The aliases between registers.
     * The key is the architecture name (x0), the value is the alias (zero).
     * Must be a list - register x8 has two aliases (s0 and fp).
     */
    private List<RegisterMapping> registerAliases;

    /// Holds error message, if any occurs, otherwise is empty
    private String errorMessage;

    /// size of cache in lines
    private int cacheSize;
    /// size of cache line
    private int cacheLineSize;
    /// cache associativity
    private int associativity;
    /// replacement policy used by cache
    private ReplacementPoliciesEnum replacementPolicy;
    /// Should the cache be used or directly memory?
    private boolean useCache;
    /// Is the cache set to writeback or writethrough
    private boolean cacheWriteback;
    private boolean cacheAddRemainingDelayToStore;
    private int cacheStoreDelay;
    private int cacheLoadDelay;
    private int cacheLineReplacementDelay;

    public class RegisterMapping {
        public String register;
        public String alias;
    }

    /**
     * @brief Constructor
     */
    public InitLoader() {
        this.registerFileModelList = new ArrayList<>();
        this.instructionFunctionModelList = new ArrayList<>();

        this.registerFileDir = "./registers/";
        this.instructionsFilePath = "./supportedInstructions.json";
        this.registerAliasesFilePath = "./registerAliases.json";

        this.errorMessage = "";

        this.cacheSize = 16;
        this.cacheLineSize = 32;
        this.associativity = 2;
        this.replacementPolicy = ReplacementPoliciesEnum.LRU;
        this.useCache = true;
        this.cacheWriteback = true;
        this.cacheAddRemainingDelayToStore = false;
        this.cacheStoreDelay = 0;
        this.cacheLoadDelay = 1;
        this.cacheLineReplacementDelay = 10;
    }// end of Constructor
    //------------------------------------------------------

    public String getInstructionsFilePath() {
        return instructionsFilePath;
    }

    public void setInstructionsFilePath(String instructionsFilePath) {
        this.instructionsFilePath = instructionsFilePath;
    }

    public String getRegisterFileDir() {
        return registerFileDir;
    }

    public void setRegisterFileDir(String registerFileDir) {
        this.registerFileDir = registerFileDir;
    }

    public void setAssociativity(int associativity) {
        this.associativity = associativity;
    }

    public void setCacheLineSize(int cacheLineSize) {
        this.cacheLineSize = cacheLineSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public void setCacheWriteback(boolean cacheWriteback) {
        this.cacheWriteback = cacheWriteback;
    }

    public void setReplacementPolicy(ReplacementPoliciesEnum replacementPolicy) {
        this.replacementPolicy = replacementPolicy;
    }

    public void setCacheAddRemainingDelayToStore(boolean remainingDelay) {
        cacheAddRemainingDelayToStore = remainingDelay;
    }

    public void setStoreDelay(int storeDelay) {
        this.cacheStoreDelay = storeDelay;
    }

    public void setLoadDelay(int loadDelay) {
        this.cacheLoadDelay = loadDelay;
    }

    public void setLineReplacementDelay(int lineReplacementDelay) {
        this.cacheLineReplacementDelay = lineReplacementDelay;
    }

    public int getAssociativity() {
        return associativity;
    }

    public int getCacheLineSize() {
        return cacheLineSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public boolean getUseCache() {
        return useCache;
    }

    public boolean getCacheWriteback() {
        return cacheWriteback;
    }

    public ReplacementPoliciesEnum getReplacementPolicy() {
        return replacementPolicy;
    }

    public boolean getCacheAddRemainingDelayToStore() {
        return cacheAddRemainingDelayToStore;
    }

    public int getCacheStoreDelay() {
        return cacheStoreDelay;
    }

    public int getCacheLoadDelay() {
        return cacheLoadDelay;
    }

    public int getCacheLineReplacementDelay() {
        return cacheLineReplacementDelay;
    }

    /**
     * @brief Calls appropriate subloaders and loads lists
     */
    public void load() {
        try {
            loadRegisters();
            loadInstructions();
            loadAliases();
        } catch (NullPointerException | IOException e) {
            handleNullPointerException();
        }
    }// end of load

    private void loadAliases() {
        Gson gson = new Gson();
        Reader reader = null;
        try {
            reader = Files.newBufferedReader(Paths.get(registerAliasesFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // read
        registerAliases = gson.fromJson(reader, new TypeToken<List<RegisterMapping>>() {
        }.getType());
    }
    //------------------------------------------------------

    /**
     * @brief Get loaded register files
     * @return List of register files
     */
    public List<RegisterFileModel> getRegisterFileModelList() {
        if (registerFileModelList.isEmpty()) {
            load();
        }
        return registerFileModelList;
    }// end of getRegisterFileModelList
    //------------------------------------------------------

    /**
     * @brief Get loaded instruction set
     * @return Set of instructions in list
     */
    public List<InstructionFunctionModel> getInstructionFunctionModelList() {
        if (instructionFunctionModelList.isEmpty()) {
            load();
        }
        return instructionFunctionModelList;
    }// end of getInstructionFunctionModelList
    //------------------------------------------------------

    public InstructionFunctionModel getInstructionFunctionModel(String instructionName) {
        if (instructionFunctionModelList.isEmpty()) {
            load();
        }
        for (InstructionFunctionModel instruction : instructionFunctionModelList) {
            if (instruction.getName().equals(instructionName)) {
                return instruction;
            }
        }
        return null;
    }

    /**
     * @brief Get error message in case of load failure
     * @return Error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }// end of getErrorMessage
    //------------------------------------------------------

    /**
     * @brief Calls subloader for register files and saves them into list
     * @throws NullPointerException - Thrown in case of empty directory
     */
    private void loadRegisters() throws NullPointerException {
        this.registerFileModelList.clear();
        final File registerFolder = new File(this.registerFileDir);
        final RegisterSubloader subloader = new RegisterSubloader();

        for (final File file : Objects.requireNonNull(registerFolder.listFiles())) {
            this.registerFileModelList.add(subloader.loadRegisterFile(file.getAbsolutePath()));
        }
    }// end of loadRegisters
    //------------------------------------------------------

    /**
     * @brief Calls subloader for instruction set and saves it into list
     * @throws NullPointerException - Thrown in case of empty directory
     */
    private void loadInstructions() throws NullPointerException, IOException {
        // All instructions are in a single .json file.
        // The structure is a single object with keys being the instruction names and
        // values being the InstructionFunctionModel objects.

        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get("./supportedInstructions.json"));
        // read to a map
        Map<String, InstructionFunctionModel> instructions = gson.fromJson(reader, new TypeToken<Map<String, InstructionFunctionModel>>() {
        }.getType());
        // add to list
        this.instructionFunctionModelList.addAll(instructions.values());
    }// end of loadInstructions
    //------------------------------------------------------

    /**
     * @brief Sets error message in case of NullPointerException and prints it into stderr
     */
    private void handleNullPointerException() {
        if (registerFileModelList.isEmpty()) {
            this.errorMessage = "Directory with register files is empty. Aborting...";
        } else {
            this.errorMessage = "Directory with instructions is empty. Aborting...";
        }
        System.err.println(this.errorMessage);
    }// end of handleNullPointerException
    //------------------------------------------------------

    public void setRegisterFileModelList(List<RegisterFileModel> registerFileModelList) {
        this.registerFileModelList = registerFileModelList;
    }

    public void setInstructionFunctionModelList(List<InstructionFunctionModel> instructionFunctionModelList) {
        this.instructionFunctionModelList = instructionFunctionModelList;
    }

    /**
     * @brief Get register aliases
     */
    public List<RegisterMapping> getRegisterAliases() {
        return registerAliases;
    }
}
