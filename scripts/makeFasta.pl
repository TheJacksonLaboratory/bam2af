#!/usr/bin/perl -w
use strict;
use IO::File;

#Script for making 'fake' reads to help test the Java code
#1. run the script. It will produce two files
# -- genomeTest.fa (the genome)
# -- testreads.fastaq (the fake NGS data)
# index the genome

#2. align the reads
#./bwa mem -R '@RG\tID:rg1\tSM:NA12878\tPL:illumina\tLB:lib1\tPU:H7AP8ADXX:1:TAAGGCGA' genomeTest.fa testreads.fastq > testreads.sam 
#3. convert to BAM file
# samtools view -Sb testreads.sam >testreads.bam
#4. sort the alignmnets
# samtools sort testreads.bam >testsorted.bam


my $READLEN=35;

my $seq= "CCCAATGTCTGTGGATCACGTTATAATGCTTACTGTTGCCCTGGATGGAAAACCTTACCTGGCGGAAATC";
my $qual="dhhhgchhhghhhfhhhhhdhhhhehhghfhhhchfddffcffafhfghehggfcffdhfehhhhcehdc";


#output a genome file 
my $fh = new IO::File(">genomeTest.fa") or die "$!";
print $fh ">my_chromosome\n$seq";
$fh->close();

# Output FASTQ reads from this genome with two variant positions.
# One has an allelic frequency of 50% and the other of 20%.

$fh=new IO::File(">testreads.fastq") or die "$!";

my %mut =(
    'A'=>'G',
    'C'=>'A',
    'G'=>'T',
    'T'=>'C');


my $nreads_per_position=5;


for (my $i=0;$i<length($seq)-$READLEN;$i++) {
    for (my $j=0;$j<$nreads_per_position;$j++) {
	my $subseq = make_read($seq,$i);
	my $subqual = substr($qual,$i,$READLEN);
	my $fakename=sprintf("read_%d_%d",$i,$j);
	print $fh ">$fakename\n$subseq\n+\n$subqual\n";
    }
}
$fh->close();



sub make_read {
    my ($seq,$startpos)=@_;
    my $mutpos1=12;
    my $mutpos2=27;
    my $af1=0.5;
    my $af2=0.2;
    my $N=length $seq;
    my $ran = rand();

    for (my $i=$startpos;$i<$N&&$i<$startpos+$READLEN;$i++) {
	if ($i==$mutpos1 && $ran < $af1) {
	   # print "making MUIT\n";
	    # print substr($seq,$startpos,$READLEN),"\n";
	    my $c=substr($seq,$i,1);
	    my $m=$mut{$c};
	    substr($seq,$i,1) = $m;
	}
	if ($i==$mutpos2 && $ran < $af2) {
	    my $c=substr($seq,$i,1);
	    my $m=$mut{$c};
	    substr($seq,$i,1) = $m;
	}
    }
    return substr($seq,$startpos,$READLEN);
}

