/*
MergeFastq
	- Purpose:
		Merge multiple lanes of forward or reverse reads
	- Input:
		Tuple containing the sample name (val), the forward reads (path) and the reverse reads (path)
	- output:
		merged_fastq: tuple containing the sampleID (val) and merged fastq files (path)
*/
process MERGE_FASTQ {
	tag {sampleID}

	publishDir "${params.outdir}/", mode: params.publishDirMode,
		saveAs: {filename ->
			if (filename.indexOf(".gz") > 0) "Merged/${filename}"
			else null
		}

	input:
	tuple val(sampleID), path(forward), path(reverse)

	output:
	path("*.tsv"), emit: md5sums
	path("*.fastq.gz"), emit: merged_fastq

	script:
	fw_cmd = "cat ${forward[0]} ${forward[1]} > ${sampleID}_R1_001.fastq.gz"
	rv_cmd = params.singleEnd ? "" : "cat ${reverse[0]} ${reverse[1]} > ${sampleID}_R2_001.fastq.gz"
	"""
	${fw_cmd}
	${rv_cmd}
	md5sum ${sampleID}_R1_001.fastq.gz > ${sampleID}_R1_001.md5.tsv
	md5sum ${sampleID}_R2_001.fastq.gz > ${sampleID}_R2_001.md5.tsv
	"""
}
