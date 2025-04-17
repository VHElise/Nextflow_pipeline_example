process PARAMETER_DUMP {
	
	publishDir "${params.tracedir}", mode: params.publishDirMode

	output:
	path("parameters.tsv")

	script:
	params_file = """${params.collect { k,v -> "${k}	${v}" }.join("\n")}""".stripIndent()
	"""
	echo "${params_file}" > parameters.tsv
	"""
}