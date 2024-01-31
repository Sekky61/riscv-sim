/**
 * @file Triplet.java
 * @author Jakub Horky \n
 * Faculty of Information Technology \n
 * Brno University of Technology \n
 * xhorky28@stud.fit.vutbr.cz
 * @brief Container class of a triplet (tuple of size 3)
 * @date 09 February  2023 18:00 (created)
 * @section Licence
 * This file is part of the Superscalar simulator app
 * <p>
 * Copyright (C) 2023  Jakub Horky
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
package com.gradle.superscalarsim.models.util;

public class Triplet<T1, T2, T3>
{
  private T1 first;
  private T2 second;
  private T3 third;
  
  public Triplet(T1 first, T2 second, T3 third)
  {
    this.first  = first;
    this.second = second;
    this.third  = third;
  }
  
  public T1 getFirst()
  {
    return first;
  }
  
  public T2 getSecond()
  {
    return second;
  }
  
  public T3 getThird()
  {
    return third;
  }
  
  public void setFirst(T1 first)
  {
    this.first = first;
  }
  
  public void setSecond(T2 second)
  {
    this.second = second;
  }
  
  public void setThird(T3 third)
  {
    this.third = third;
  }
}
