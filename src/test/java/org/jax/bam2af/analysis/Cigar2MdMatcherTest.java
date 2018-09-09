package org.jax.bam2af.analysis;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Cigar2MdMatcherTest {






    @Test
    public void test1() {
        BaseCall[] positions = new BaseCall[15];
        for (int i=0;i<15;i++){
            positions[i]=BaseCall.REFBASE;
        }
        CigarElement el = new CigarElement(15,CigarOperator.M);
        List<CigarElement> elemList = new ArrayList<>();
        elemList.add(el);
        Cigar cig = new Cigar(elemList);
        String MD="35";
        BaseCall[] pos= Cigar2MdMatcher.getPositions(cig,MD);
        assertEquals(positions,pos);
    }
}
