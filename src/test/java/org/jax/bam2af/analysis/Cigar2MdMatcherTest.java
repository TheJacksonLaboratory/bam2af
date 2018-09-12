package org.jax.bam2af.analysis;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;


public class Cigar2MdMatcherTest {






    @Test
    public void test1() {
        BaseCall[] positions = new BaseCall[35];
        for (int i=0;i<35;i++){
            positions[i]=BaseCall.REFBASE;
        }
        CigarElement el = new CigarElement(35,CigarOperator.M);
        List<CigarElement> elemList = new ArrayList<>();
        elemList.add(el);
        Cigar cig = new Cigar(elemList);
        String MD="35";
        BaseCall[] pos= Cigar2MdMatcher.getPositions(cig,MD);
        assertArrayEquals(positions,pos);

    }

    /** 35 nt sequence, one mismatch at pos 10 with ALT=A */
    @Test
    public void testOneMismatch() {
        BaseCall[] positions = new BaseCall[35];
        for (int i=0;i<35;i++){
            positions[i]=BaseCall.REFBASE;
        }
        positions[9]=BaseCall.ALTBASE_A;
        CigarElement el = new CigarElement(35,CigarOperator.M);
        List<CigarElement> elemList = new ArrayList<>();
        elemList.add(el);
        Cigar cig = new Cigar(elemList);
        String MD="9A25";
        BaseCall[] pos= Cigar2MdMatcher.getPositions(cig,MD);
        assertArrayEquals(positions,pos);
//        for (int i=0;i<pos.length;i++) {
//            System.err.println(i +": pos="+pos[i] +", positions=" +positions[i]);
//        }
    }

}
