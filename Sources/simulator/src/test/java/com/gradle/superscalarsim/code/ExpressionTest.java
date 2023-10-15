package com.gradle.superscalarsim.code;

import com.gradle.superscalarsim.enums.DataTypeEnum;
import com.gradle.superscalarsim.models.register.RegisterDataContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ExpressionTest
{
  @Test
  public void testBinaryPlus()
  {
    Expression.interpret("1 2 +", null);
  }
  
  @Test
  public void testWriteVariable()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kInt, RegisterDataContainer.fromValue(0)));
    Expression.interpret("1 \\x =", vars);
    
    // Assert
    Expression.Variable x = vars.get(0);
    Assert.assertEquals(1, (int) x.value.getValue(DataTypeEnum.kInt));
    Assert.assertEquals("x", x.tag);
  }
  
  @Test
  public void testReadVariable()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kInt, RegisterDataContainer.fromValue(1)),
            new Expression.Variable("y", DataTypeEnum.kInt, RegisterDataContainer.fromValue(0)));
    Expression.interpret("\\x 5 + \\y =", vars);
    
    // Assert
    Expression.Variable x = vars.get(0);
    Assert.assertEquals(1, (int) x.value.getValue(DataTypeEnum.kInt));
    Assert.assertEquals("x", x.tag);
    
    Expression.Variable y = vars.get(1);
    Assert.assertEquals(6, (int) y.value.getValue(DataTypeEnum.kInt));
    Assert.assertEquals("y", y.tag);
  }
  
  @Test
  public void testSignedCompare()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kInt, RegisterDataContainer.fromValue(1)),
            new Expression.Variable("y", DataTypeEnum.kInt, RegisterDataContainer.fromValue(-1)),
            new Expression.Variable("b", DataTypeEnum.kBool, RegisterDataContainer.fromValue(false)));
    Expression.interpret("\\x \\y > \\b =", vars);
    
    // Assert
    Expression.Variable b = vars.get(2);
    Assert.assertEquals(true, b.value.getValue(DataTypeEnum.kBool));
  }
  
  @Test
  public void testUnsignedCompare()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kUInt, RegisterDataContainer.fromValue(1)),
            new Expression.Variable("y", DataTypeEnum.kUInt, RegisterDataContainer.fromValue(-1)),
            new Expression.Variable("b", DataTypeEnum.kBool, RegisterDataContainer.fromValue(true)));
    Expression.interpret("\\x \\y > \\b =", vars);
    
    // Assert
    Expression.Variable b = vars.get(2);
    Assert.assertEquals(false, b.value.getValue(DataTypeEnum.kBool));
  }
  
  @Test
  public void testAssignSignedToUnsigned()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kUInt, RegisterDataContainer.fromValue(0)),
            new Expression.Variable("y", DataTypeEnum.kInt, RegisterDataContainer.fromValue(-1)));
    Expression.interpret("\\y \\x =", vars);
    
    // Assert
    Expression.Variable x             = vars.get(0);
    int                 unsignedValue = (int) x.value.getValue(DataTypeEnum.kUInt);
    Assert.assertEquals(0xFFFFFFFFL, Integer.toUnsignedLong(unsignedValue));
  }
  
  @Test
  public void testAssignConstant()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kInt, RegisterDataContainer.fromValue(0)));
    Expression.interpret("5 0x3 + \\x =", vars);
    
    // Assert
    Expression.Variable x = vars.get(0);
    Assert.assertEquals(8, (int) x.value.getValue(DataTypeEnum.kInt));
  }
  
  @Test
  public void testIncompatibleTypesOperation()
  {
    List<Expression.Variable> vars = List.of(
            new Expression.Variable("x", DataTypeEnum.kFloat, RegisterDataContainer.fromValue(0.0f)));
    
    Assert.assertThrows(IllegalArgumentException.class, () -> Expression.interpret("5 \\x +", vars));
  }
}