#!/usr/bin/env nextflow

// Check RunName
if ( !params.runName ) { exit 1, "The 'runName' parameter is undefined" }

// Include Merge module
include { MERGE_FASTQ } from '../modules/merge/fastq/main.nf'
include { MERGE_MD5SUM } from '../modules/md5sum/main.nf'


Channel
    .fromFilePairs( params.reads, size: params.singleEnd ? 1 : 2 ) // Set reads into channel
    .ifEmpty { exit 1, "Cannot find any reads matching: ${params.reads}\nNB: Path needs to be enclosed in quotes!\nNB: Path requires at least one * wildcard!\nIf this is single-end data, please specify --singleEnd on the command line." }
    .map { sample, reads -> tuple( sample.replaceAll(/_L\d+/, "").replaceAll(/_S\d+/, ""), reads ) } // Replace _L00* in the sampleID with nothing
    .groupTuple() // Group on sampleID and sort reads per forward or reverse
    .map { sample, reads -> reads[0].size() > 1 ? tuple( sample, params.singleEnd ?	reads*.getAt(0) : reads*.getAt(0), reads*.getAt(1) ) : tuple( sample, params.singleEnd ? reads*.getAt(0) : reads[0] ) }
    .branch { // Branch reads into fastqs to merge and not to merge based on channel size
        reads_to_merge: it[1].size() > 1
        reads_not_to_merge: it[1].size() <= 1
    }
    .set { ch_raw_reads }

workflow WF_MERGE_AND_MD5SUM {
    // Merge fastq files that have multiple lanes
    MERGE_FASTQ( ch_raw_reads.reads_to_merge )
    MERGE_MD5SUM( MERGE_FASTQ.out.md5sums.collect() )
}