package com.gradle.superscalarsim.models;

/**
 * @param <T1> First object
 * @param <T2> Second object
 *
 * @class Pair
 * @brief Class representing pair of two objects
 */
public class Pair<T1, T2>
{
  private T1 first;
  private T2 second;
  
  public Pair(T1 first, T2 second)
  {
    this.first  = first;
    this.second = second;
  }
  
  public T1 getFirst()
  {
    return first;
  }
  
  public T2 getSecond()
  {
    return second;
  }
  
  
  public void setFirst(T1 first)
  {
    this.first = first;
  }
  
  public void setSecond(T2 second)
  {
    this.second = second;
  }
  
  /**
   * @brief Check if two pairs are equal
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (!(obj instanceof Pair))
    {
      return false;
    }
    Pair<T1, T2> other = (Pair<T1, T2>) obj;
    return this.first.equals(other.first) && this.second.equals(other.second);
  }
}
