process MERGE_MD5SUM {

	publishDir "${params.outdir}", mode: params.publishDirMode,
		saveAs: {filename -> "Merged/${filename}"}

	input:
	path(md5_files)

	output:
	path("md5sum.tsv")

	script:
	"""
	cat ${md5_files} > md5sum.tsv
	"""
}
