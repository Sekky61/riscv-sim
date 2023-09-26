/**
 * @file    CacheBuilder.java
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 * 
 * @brief Builder pattern for Cache
 *
 * @date  26 Sep      2023 10:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023 Michal Majer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
package com.gradle.superscalarsim.blocks.loadstore;

import com.gradle.superscalarsim.blocks.CacheStatisticsCounter;
import com.gradle.superscalarsim.code.SimulatedMemory;
import com.gradle.superscalarsim.enums.cache.ReplacementPoliciesEnum;
import com.gradle.superscalarsim.models.cache.CacheAccess;
import com.gradle.superscalarsim.models.cache.CacheLineModel;
import com.gradle.superscalarsim.models.cache.ReplacementPolicyModel;

import java.util.Stack;

public class CacheBuilder {
    private CacheLineModel[][] cache;
    private SimulatedMemory memory;
    private int numberOfLines;
    private int associativity;
    private int lineSize;
    private ReplacementPolicyModel replacementPolicy;

    private ReplacementPoliciesEnum replacementPolicyType;
    private boolean writeBack;
    private boolean addRemainingDelayToStore;
    private int storeDelay;
    private int loadDelay;
    private int lineReplacementDelay;
    private CacheStatisticsCounter cacheStatisticsCounter;

    private int cycleEndOfReplacement;

    private Stack<CacheLineModel> cacheLineHistory;
    private Stack<Integer> cacheIdHistory;
    private Stack<CacheAccess> lastAccess;
    public void setCacheLineHistory(Stack<CacheLineModel> cacheLineHistory) {
        this.cacheLineHistory = cacheLineHistory;
    }

    public void setCacheIdHistory(Stack<Integer> cacheIdHistory) {
        this.cacheIdHistory = cacheIdHistory;
    }

    public void setLastAccess(Stack<CacheAccess> lastAccess) {
        this.lastAccess = lastAccess;
    }

    public CacheBuilder setMemory(SimulatedMemory memory) {
        this.memory = memory;
        return this;
    }

    public CacheBuilder setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
        return this;
    }

    public CacheBuilder setAssociativity(int associativity) {
        this.associativity = associativity;
        return this;
    }

    public CacheBuilder setLineSize(int lineSize) {
        this.lineSize = lineSize;
        return this;
    }

    public CacheBuilder setReplacementPolicy(ReplacementPolicyModel replacementPolicy) {
        this.replacementPolicy = replacementPolicy;
        return this;
    }

    public CacheBuilder setWriteBack(boolean writeBack) {
        this.writeBack = writeBack;
        return this;
    }

    public CacheBuilder setAddRemainingDelayToStore(boolean addRemainingDelayToStore) {
        this.addRemainingDelayToStore = addRemainingDelayToStore;
        return this;
    }

    public CacheBuilder setStoreDelay(int storeDelay) {
        this.storeDelay = storeDelay;
        return this;
    }

    public CacheBuilder setLoadDelay(int loadDelay) {
        this.loadDelay = loadDelay;
        return this;
    }

    public CacheBuilder setLineReplacementDelay(int lineReplacementDelay) {
        this.lineReplacementDelay = lineReplacementDelay;
        return this;
    }

    public CacheBuilder setCacheStatisticsCounter(CacheStatisticsCounter cacheStatisticsCounter) {
        this.cacheStatisticsCounter = cacheStatisticsCounter;
        return this;
    }

    public CacheBuilder setCache(CacheLineModel[][] cache) {
        this.cache = cache;
        return this;
    }

    public void setReplacementPolicyType(ReplacementPoliciesEnum replacementPolicyType) {
        this.replacementPolicyType = replacementPolicyType;
    }

    public void setCycleEndOfReplacement(int cycleEndOfReplacement) {
        this.cycleEndOfReplacement = cycleEndOfReplacement;
    }

    public Cache createCache() {
        return new Cache(cacheStatisticsCounter, numberOfLines, associativity, lineSize, cache, replacementPolicy, memory, writeBack, cacheLineHistory, cacheIdHistory, lastAccess, storeDelay, loadDelay, lineReplacementDelay, addRemainingDelayToStore, replacementPolicyType, cycleEndOfReplacement);
    }
}