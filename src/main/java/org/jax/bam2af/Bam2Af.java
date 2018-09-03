package org.jax.bam2af;

import htsjdk.samtools.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class Bam2Af {


    public static void main(String args[]) {
        String bamSample="/home/peter/data/chipseq/SRR6350627_sorted.bam";
        Bam2Af b2f=new Bam2Af(bamSample);
        b2f.parseBam(bamSample);
    }



    public Bam2Af(String bamPath){


    }


    public void parseBam(String bamPath) {
        System.err.println("Reading BAM file "+bamPath);
        File bamFile=new File(bamPath);
        if (!bamFile.exists()) {
            System.err.println(bamPath + " was not found. Please check path");
            System.exit(1);
        }
        SamReader reader=SamReaderFactory.makeDefault().open(new File(bamPath));
        List<SAMSequenceRecord> refSeqs= reader.getFileHeader().getSequenceDictionary().getSequences();
        for(SAMSequenceRecord ref : refSeqs){
            System.out.println("I got the following: "+ref.getSequenceName());
        }
        // Start iterating from start to end of chr7.
        SAMRecordIterator iter = reader.query("chr7", 0, 0, false);

        while(iter.hasNext()){
            // Iterate thorough each record and extract fragment size
            SAMRecord rec= iter.next();
            int tlen= rec.getInferredInsertSize();
            System.out.println(tlen);
        }


    }



}
