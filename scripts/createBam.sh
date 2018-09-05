#!/bin/bash


BWA=./bwa

if [ ! -e toyidx.bwt ]; then
    ${BWA} index -p toyidx -a bwtsw genomeTest.fa
fi


if [ ! -e toy_aln.sai ]; then
    ${BWA} aln genomeTest.fa testreads.fastq -0 > toy_aln.sai
fi


if [ ! -e toy_aln.sam ]; then
    ${BWA} samse -f toy_aln.sam genomeTest.fa toy_aln.sai testreads.fastq
fi

if [ ! -e toy_aln.bam ]; then
    samtools view -Sb testreads.sam >toy_aln.bam
fi

if [ ! -e sorted_toy_aln.bam ]; then
   samtools sort toy_aln.bam > sorted_toy_aln.bam
fi

if [ ! -e sorted_toy_aln.bam.bai ]; then
    samtools index sorted_toy_aln.bam
fi
