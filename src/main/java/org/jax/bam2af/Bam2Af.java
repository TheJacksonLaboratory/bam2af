package org.jax.bam2af;

import htsjdk.samtools.*;
import org.jax.bam2af.bam.SingleEndBamReader;
import org.jax.bam2af.bed.BedParser;
import org.jax.bam2af.bed.GenomicInterval;
import org.jax.bam2af.exception.Bam2AfException;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class Bam2Af {

    private final SingleEndBamReader seBamReader;

    private final String bedFilePath;

    private List<GenomicInterval> bedIntervalList;


    public static void main(String args[]) {
        String bamSample="scripts/sorted_toy_aln.bam";
        String bedSample="src/test/resources/bam/to_intervals.bed";
        // note--need to make index first: samtools index SRR6350627_sorted.bam
        Bam2Af b2f=new Bam2Af(bamSample,bedSample);
        b2f.parseBam();
    }



    public Bam2Af(String bamPath,String bedPath){
        this.bedFilePath=bedPath;
        initBedIntervals();
        this.seBamReader = new SingleEndBamReader(bamPath);
    }


    private void initBedIntervals() {
        BedParser parser = new BedParser(this.bedFilePath);
        try {
            this.bedIntervalList = parser.parse();
        } catch (Bam2AfException e){
            e.printStackTrace(); // TODO die gracefully
        }
    }



    public void parseBam() {
        for (GenomicInterval gi : this.bedIntervalList) {
            String chrom = gi.getChromosome();
            int from = gi.getFromPosition();
            int to = gi.getToPosition();
            SAMRecordIterator iter = seBamReader.getRecordsInInterval(chrom,from,to);
        }


        SAMRecordIterator iter = seBamReader.getGlobalIterator();    //query("my_chromosome", 0, 0, false);

        while(iter.hasNext()){
            if (iter==null) {
                System.err.println("item null");
                continue;
            }
            // Iterate thorough each record and extract fragment size
            final SAMRecord rec= iter.next();
            int tlen= rec.getInferredInsertSize();
            Cigar cigar = rec.getCigar();
            System.out.println(cigar.numCigarElements()+ " seq=" + rec.getReadString());

        }


    }



}
