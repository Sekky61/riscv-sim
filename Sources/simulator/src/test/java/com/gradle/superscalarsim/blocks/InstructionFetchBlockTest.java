package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.InstructionFetchBlock;
import com.gradle.superscalarsim.blocks.base.InstructionMemoryBlock;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.blocks.branch.GlobalHistoryRegister;
import com.gradle.superscalarsim.blocks.branch.PatternHistoryTable;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
import com.gradle.superscalarsim.models.InputCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class InstructionFetchBlockTest
{
  InstructionMemoryBlock instructionMemoryBlock;
  BranchTargetBuffer branchTargetBuffer;
  GShareUnit gShareUnit;
  
  private InstructionFetchBlock instructionFetchBlock;
  
  @Before
  public void setUp()
  {
    this.instructionMemoryBlock = new InstructionMemoryBlock(null, null);
    this.branchTargetBuffer     = new BranchTargetBuffer(1000);
    this.gShareUnit             = new GShareUnit(1, new GlobalHistoryRegister(1000),
                                                 new PatternHistoryTable(10, new boolean[]{true, false},
                                                                         PatternHistoryTable.PredictorType.TWO_BIT_PREDICTOR));
    SimCodeModelAllocator simCodeModelAllocator = new SimCodeModelAllocator();
    this.instructionFetchBlock = new InstructionFetchBlock(simCodeModelAllocator, this.instructionMemoryBlock,
                                                           this.gShareUnit, this.branchTargetBuffer);
  }
  
  @Test
  public void instructionFetchSimulateThreeWay_emptyCode_returnsThreeNops()
  {
    instructionMemoryBlock.setCode(Arrays.asList());
    
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(12, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void instructionFetchSimulateThreeWay_threeInstructions_returnThreeInstructions()
  {
    InputCodeModel       ins1         = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel       ins2         = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel       ins3         = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(12, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void instructionFetchSimulateThreeWay_twoInstructions_returnTwoInstructionsAndNop()
  {
    InputCodeModel       ins1         = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel       ins2         = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2);
    instructionMemoryBlock.setCode(instructions);
    
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(12, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void instructionFetchSimulateFiveWay_threeInstructions_returnThreeInstructionsAndTwoNops()
  {
    InputCodeModel       ins1         = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel       ins2         = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel       ins3         = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    this.instructionFetchBlock.setNumberOfWays(5);
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(5 * 4, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(3).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(4).getInstructionName());
  }
  
  @Test
  public void instructionFetchSimulateThreeWay_threeInstructionsSimulateTwice_returnInstructionsFirstThenNops()
  {
    InputCodeModel       ins1         = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel       ins2         = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel       ins3         = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    this.instructionFetchBlock.simulate();
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(24, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }
  
  @Test
  public void instructionFetchPropertyChange_threeInstructions_callsSimulateAndReturnsThreeInstructions()
  {
    InputCodeModel       ins1         = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel       ins2         = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel       ins3         = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    instructionMemoryBlock.setCode(instructions);
    
    Assert.assertEquals(0, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals(0, this.instructionFetchBlock.getFetchedCode().size());
    
    this.instructionFetchBlock.simulate();
    
    Assert.assertEquals(12, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    
  }
}
