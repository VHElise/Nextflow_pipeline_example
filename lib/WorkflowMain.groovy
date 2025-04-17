//
// This file holds several functions specific to the main.nf workflow in the nf-core/rnaseq pipeline
//

import nextflow.Nextflow

class WorkflowMain {

    //
    // Generate help string
    //
    public static String help(workflow, params) {
        def command = "nextflow run ${workflow.manifest.name} -r ${workflow.manifest.version} --runName 'some_run_name' -profile aws,docker"
        def help_string = ''
        help_string += CellCartaTemplate.logo(workflow, params.monochromeLogs)
        help_string += CellCartaSchema.paramsHelp(workflow, params, command)
        help_string += CellCartaTemplate.dashedLine(params.monochromeLogs)
        help_string += '\n'
        return help_string
    }

    //
    // Generate parameter summary log string
    //
    public static String paramsSummaryLog(workflow, params) {
        def summary_log = ''
        summary_log += CellCartaTemplate.logo(workflow, params.monochromeLogs)
        summary_log += CellCartaSchema.paramsSummaryLog(workflow, params)
        summary_log += CellCartaTemplate.dashedLine(params.monochromeLogs)
        summary_log += '\n'
        return summary_log
    }

    //
    // Validate parameters and print summary to screen
    //
    public static void initialise(workflow, params, log) {
        // Print help to screen if required
        if (params.help) {
            log.info help(workflow, params)
            System.exit(0)
        }

        // Print parameter summary log to screen
        log.info paramsSummaryLog(workflow, params)

        // Warn about using custom configs to provide pipeline parameters
        CellCartaTemplate.warnParamsProvidedInConfig(workflow, log)

        // // Validate workflow parameters via the JSON schema
        // if (params.validate_params) {
        //     CellCartaSchema.validateParameters(workflow, params, log)
        // }

        // Check that a -profile or Nextflow config has been provided to run the pipeline
        CellCartaTemplate.checkConfigProvided(workflow, log)

        // Check that conda channels are set-up correctly
        if (workflow.profile.tokenize(',').intersect(['conda', 'mamba']).size() >= 1) {
            Utils.checkCondaChannels(log)
        }

        // Check AWS batch settings
        CellCartaTemplate.awsBatch(workflow, params)

    // // Check input has been provided
    // if (!params.input) {
    //     Nextflow.error("Please provide an input samplesheet to the pipeline e.g. '--input samplesheet.csv'")
    // }
    }

    //
    // Get attribute from genome config file e.g. fasta
    //
    public static Object getGenomeAttribute(params, attribute) {
        if (params.genomes && params.genome && params.genomes.containsKey(params.genome)) {
            if (params.genomes[ params.genome ].containsKey(attribute)) {
                return params.genomes[ params.genome ][ attribute ]
            }
        }
        return null
    }

    public static Object getGnomadAttribute(params, attribute) {
        if (params.gnomads && params.gnomad && params.gnomads.containsKey(params.gnomad)) {
            if (params.gnomads[ params.gnomad ].containsKey(attribute)) {
                return params.gnomads[ params.gnomad ][ attribute ]
            }
        }
        return null
    }

    public static Object getNestedGnomadAttribute(params, nest, attribute) {
        if (params.gnomads && params.gnomad && params.gnomads.containsKey(params.gnomad)) {
            if (params.gnomads[ params.gnomad ].containsKey(nest)) {
                if (params.gnomads[ params.gnomad ][ nest ].containsKey(attribute)) {
                    return params.gnomads[ params.gnomad ][ nest ][ attribute ]
                }
            }
        }
        return null
    }

}
