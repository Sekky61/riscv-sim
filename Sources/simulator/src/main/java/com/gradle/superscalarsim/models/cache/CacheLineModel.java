/**
 * @file    CacheLineModel.java
 *
 * @author  Jakub Horky \n
 *          Faculty of Information Technology \n
 *          Brno University of Technology \n
 *          xhorky28@fit.vutbr.cz
 *
 * @brief File contains container class for cache line
 *
 * @date  04 April 2023 14:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023 Jakub Horky
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
package com.gradle.superscalarsim.models.cache;

import java.util.Stack;

/**
 * @class CacheLineModel
 * @brief Container class for cache line
 */
public class CacheLineModel
{
    /// If this line contains valid data
    boolean valid;
    ///If this line holds modified data
    boolean dirty;
    ///Top of the address to correctly and uniquely identify line
    long tag;
    ///Held data
    int[] line;
    ///Size of the line in bytes
    int lineSize;
    ///Index of the index line inside cache (multiple lines can have same index)
    int index;
    /// baseAddress of this line (Address of first byte)
    long baseAddress;

    Stack<CacheLineModel> lineHistory;
    Stack<Integer> lineIdHistory;

    /**
     * @brief Constructor
     * @param lineSize - size of the line in bytes - must be multiple of 4
     */
    public CacheLineModel(int lineSize, int index)
    {
        this.valid = false;
        this.dirty = false;
        this.tag   = 0;
        this.line  = new int[lineSize/4];
        this.lineSize = lineSize;
        this.lineIdHistory = new Stack<>();
        this.lineHistory = new Stack<>();
        this.index = index;
        this.baseAddress = 0;
    }

    /**
     * @brief get if the line is dirty
     * @return boolean - dirty
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * @brief get if the line is valid
     * @return boolean - valid
     */
    public boolean isValid()
    {
        return valid;
    }

    /**
     * @brief get the tag of the line
     * @return long - tag
     */
    public long getTag()
    {
        return tag;
    }

    /**
     * @brief Get data on the index
     * @param index Index inside the line
     * @param size Size of requested data - 1,2,4
     * @return int - data
     */
    public int getData(int index, int size)
    {
        int targetIndex = index/4;
        int indexOffset = index%4;
        int mask;
        if (size == 4)
        {
            mask = -1;
        }
        else if (size == 2)
        {
            mask = (1 << 16)-1;
        }
        else if (size == 1)
        {
            mask = (1 << 8)-1;
        }
        else
            throw new RuntimeException("Misaligned load in cache line is not supported");
        if (
            (size == 4 && indexOffset != 0) ||
            (size == 2 && indexOffset%2 != 0)
        )
            throw new RuntimeException("Misaligned load in cache line is not supported");

        return (line[targetIndex]>>>(indexOffset*8)) & mask;
    }

    /**
     * @brief Sets data on the index
     * @param index Index inside the line
     * @param size Size of requested data - 1,2,4
     * @param data Data to be stored
     */
    public void setData(int index, int size, int data)
    {
        int targetIndex = index/4;
        int indexOffset = index%4;
        int mask;
        //The value is aligned to the start of the block
        if (size == 4)
        {
            mask = ~(-1);
        }
        else if (size == 2)
        {
            mask = ~((-(1 << 16))>>>((4-size-indexOffset)*8));
        }
        else if (size == 1)
        {
            mask = ~((-(1 << 24))>>>((4-size-indexOffset)*8));
        }
        else
            throw new RuntimeException("Misaligned store in cache line is not supported");
        if (
            (size == 4 && indexOffset != 0) ||
            (size == 2 && indexOffset%2 != 0)
        )
            throw new RuntimeException("Misaligned store in cache line is not supported");

        //OriginalData with place for new data zeroed out
        int originalData = line[targetIndex] & mask;
        //New data in place of zeroed out original ORED with the original ones
        line[targetIndex] = originalData | ((data<<indexOffset*8) & ~mask);
    }


    /**
     * @brief Sets if this line contains valid data
     * @param valid - if line is valid
     */
    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    /**
     * @brief Sets if this line contains dirty data
     * @param dirty - if line is dirty
     */
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    /**
     * @brief Sets Tag of the data stored in this line
     * @param tag - Tag of the line
     */
    public void setTag(long tag)
    {
        this.tag = tag;
    }

    /**
     * @brief Saves current line to history
     * @param id - Current clock cycle
     */
    public void saveToHistory(int id)
    {
        CacheLineModel backupLine = new CacheLineModel(lineSize, index);
        for (int i = 0; i<lineSize; i+=4)
            backupLine.setData(i, 4, getData(i, 4));
        backupLine.setValid(isValid());
        backupLine.setDirty(isDirty());
        backupLine.setTag(getTag());

        lineHistory.add(backupLine);
        lineIdHistory.add(id);
    }

    /**
     * @brief Restores current line from history
     * @param id - Current clock cycle
     */
    public void revertHistory(int id)
    {
        while(!lineIdHistory.isEmpty() && (lineIdHistory.peek() == id))
        {
            CacheLineModel backupLine = lineHistory.peek();

            setValid(backupLine.isValid());
            setDirty(backupLine.isDirty());
            setTag(backupLine.getTag());
            for (int i = 0; i<lineSize; i+=4)
                setData(i, 4, backupLine.getData(i, 4));
            lineIdHistory.pop();
            lineHistory.pop();
        }
    }

    /**
     * @brief Gets the whole line with stored data
     * @return int[] - array of words in cache line
     */
    public int[] getLine()
    {
        return line;
    }

    public int getIndex(){ return index; }

    public void setBaseAddress(long baseAddress){ this.baseAddress = baseAddress ; }
    public long getBaseAddress(){ return baseAddress; }
}
