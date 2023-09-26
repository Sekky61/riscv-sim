package com.gradle.superscalarsim.blocks;

import com.gradle.superscalarsim.blocks.base.InstructionFetchBlock;
import com.gradle.superscalarsim.blocks.branch.BranchTargetBuffer;
import com.gradle.superscalarsim.blocks.branch.GShareUnit;
import com.gradle.superscalarsim.builders.InputCodeModelBuilder;
import com.gradle.superscalarsim.code.CodeParser;
import com.gradle.superscalarsim.code.SimCodeModelAllocator;
import com.gradle.superscalarsim.models.InputCodeModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstructionFetchBlockTest
{
  @Mock
  CodeParser codeParser;
  @Mock
  BranchTargetBuffer branchTargetBuffer;
  @Mock
  GShareUnit gShareUnit;

  private InstructionFetchBlock instructionFetchBlock;

  @Before
  public void setUp()
  {
    MockitoAnnotations.openMocks(this);
    this.instructionFetchBlock = new InstructionFetchBlock(new SimCodeModelAllocator(), codeParser,gShareUnit,branchTargetBuffer);
  }

  @Test
  public void instructionFetchSimulateThreeWay_emptyCode_returnsThreeNops()
  {
    Mockito.when(codeParser.getParsedCode()).thenReturn(new ArrayList<>());

    this.instructionFetchBlock.simulate();

    Assert.assertEquals(0, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }

  @Test
  public void instructionFetchSimulateThreeWay_threeInstructions_returnThreeInstructions()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);

    this.instructionFetchBlock.simulate();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }

  @Test
  public void instructionFetchSimulateThreeWay_twoInstructions_returnTwoInstructionsAndNop()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);

    this.instructionFetchBlock.simulate();

    Assert.assertEquals(2, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }

  @Test
  public void instructionFetchSimulateFiveWay_threeInstructions_returnThreeInstructionsAndTwoNops()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);

    this.instructionFetchBlock.setNumberOfWays(5);
    this.instructionFetchBlock.simulate();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(3).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(4).getInstructionName());
  }

  @Test
  public void instructionFetchSimulateThreeWay_threeInstructionsSimulateTwice_returnInstructionsFirstThenNops()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);

    this.instructionFetchBlock.simulate();
    this.instructionFetchBlock.simulate();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());
  }

  @Test
  public void instructionFetchSimulateBackwardsThreeWay_threeInstructionsSimulateThriceThenBack_returnCorrectValues()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);
    this.instructionFetchBlock.simulate();
    this.instructionFetchBlock.simulate();
    this.instructionFetchBlock.simulate();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());

    this.instructionFetchBlock.simulateBackwards();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("nop", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

    this.instructionFetchBlock.simulateBackwards();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

    this.instructionFetchBlock.simulateBackwards();

    Assert.assertEquals(0, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals(0, this.instructionFetchBlock.getFetchedCode().size());
  }

  @Test
  public void instructionFetchPropertyChange_threeInstructions_callsSimulateAndReturnsThreeInstructions()
  {
    InputCodeModel ins1 = new InputCodeModelBuilder().hasInstructionName("ins1").build();
    InputCodeModel ins2 = new InputCodeModelBuilder().hasInstructionName("ins2").build();
    InputCodeModel ins3 = new InputCodeModelBuilder().hasInstructionName("ins3").build();
    List<InputCodeModel> instructions = Arrays.asList(ins1, ins2, ins3);
    Mockito.when(codeParser.getParsedCode()).thenReturn(instructions);
    Assert.assertEquals(0, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals(0, this.instructionFetchBlock.getFetchedCode().size());

    this.instructionFetchBlock.simulate();

    Assert.assertEquals(3, this.instructionFetchBlock.getPcCounter());
    Assert.assertEquals("ins1", this.instructionFetchBlock.getFetchedCode().get(0).getInstructionName());
    Assert.assertEquals("ins2", this.instructionFetchBlock.getFetchedCode().get(1).getInstructionName());
    Assert.assertEquals("ins3", this.instructionFetchBlock.getFetchedCode().get(2).getInstructionName());

  }
}
