//
// This file holds several functions used within the nf-core pipeline template.
//

import org.yaml.snakeyaml.Yaml

class CompanyTemplate {

    //
    // Check AWS Batch related parameters have been specified correctly
    //
    public static void awsBatch(workflow, params) {
        if (workflow.profile.contains('awsbatch')) {
            // Check params.awsqueue and params.awsregion have been set if running on AWSBatch
            assert (params.awsqueue && params.awsregion) : 'Specify correct --awsqueue and --awsregion parameters on AWSBatch!'
            // Check outdir paths to be S3 buckets if running on AWSBatch
            assert params.outdir.startsWith('s3:')       : 'Outdir not on S3 - specify S3 Bucket to run on AWSBatch!'
        }
    }

    //
    //  Warn if a -profile or Nextflow config has not been provided to run the pipeline
    //
    public static void checkConfigProvided(workflow, log) {
        if (workflow.profile == 'standard' && workflow.configFiles.size() <= 1) {
            log.warn "[$workflow.manifest.name] You are attempting to run the pipeline without any custom configuration!\n\n" +
                    'This will be dependent on your local compute environment but can be achieved via one or more of the following:\n' +
                    '   (1) Using an existing pipeline profile e.g. `-profile aws` or `-profile hgx_prd_docker`\n' +
                    '   (2) Using your own local custom config e.g. `-c /path/to/your/custom.config`'
        }
    }

    //
    //  Warn if using custom configs to provide pipeline parameters
    //
    public static void warnParamsProvidedInConfig(workflow, log) {
        if (workflow.configFiles.size() > 1) {
            log.warn '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n' +
                '  Multiple config files detected!\n' +
                "  Please provide pipeline parameters via the CLI or Nextflow '-params-file' option.\n" +
                "  Custom config files including those provided by the '-c' Nextflow option can be\n" +
                '  used to provide any configuration except for parameters.\n\n' +
                '  Docs: https://nf-co.re/usage/configuration#custom-configuration-files\n' +
                '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
        }
    }

    //
    // Generate version string
    //
    public static String version(workflow) {
        String version_string = ''

        if (workflow.manifest.version) {
            def prefix_v = workflow.manifest.version[0] != 'v' ? 'v' : ''
            version_string += "${prefix_v}${workflow.manifest.version}"
        }

        if (workflow.commitId) {
            def git_shortsha = workflow.commitId.substring(0, 7)
            version_string += "-g${git_shortsha}"
        }

        return version_string
    }

    //
    // Print pipeline summary on completion
    //
    public static void completed_summary(workflow, params, log) { //, pass_mapped_reads=[:], pass_trimmed_reads=[:], pass_strand_check=[:]) {
        Map colors = logColours(params.monochromeLogs)
        log.info ''
        if (workflow.success) {
            def color = colors.bigreen
            def status = []
            if (workflow.stats.ignoredCount != 0) {
                color = colors.biyellow
                status += ['with errored process(es)']
            }
            log.info "${colors.white}[$workflow.manifest.name]${color} Pipeline completed successfully ${status.join(', ')}${colors.reset}"
        } else {
            log.info "\n${colors.purple}[$workflow.manifest.name]${colors.red} Pipeline completed with errors:\n${colors.reset}"
            log.info "${workflow.errorMessage}"
        }
        log.info "${colors.white}---------------------------------------------------------------------------------------${colors.reset}"
        log.info "${colors.biblue}Command          ${colors.white}: ${workflow.commandLine}${colors.reset}"
        log.info ''
        log.info "${colors.biwhite}Pipeline Configuration:${colors.reset}"
        log.info "${colors.biblue}Script           ${colors.white}: ${workflow.scriptName}${colors.reset}"
        log.info "${colors.biblue}Profile(s)       ${colors.white}: ${workflow.profile}${colors.reset}"
        log.info "${colors.biblue}Resume           ${colors.white}: ${workflow.resume}${colors.reset}"
        log.info "${colors.biblue}Container engine ${colors.white}: ${workflow.containerEngine}${colors.reset}"

        log.info ''
        log.info "${colors.biwhite}Pipeline Information:${colors.reset}"
        log.info "${colors.biblue}Jobname          ${colors.white}: ${workflow.runName}${colors.reset}"
        log.info "${colors.biblue}Completed at     ${colors.white}: ${workflow.complete.format('dd MMM yyyy HH:mm:ss')}${colors.reset}"
        log.info "${colors.biblue}Duration         ${colors.white}: ${workflow.duration}${colors.reset}"
        log.info ''
        log.info "${colors.biwhite}Directory Settings:${colors.reset}"
        log.info "${colors.biblue}launchDir        ${colors.white}: ${workflow.launchDir}${colors.reset}"
        log.info "${colors.biblue}workDir          ${colors.white}: ${workflow.workDir}${colors.reset}"
        log.info "${colors.biblue}PublishDir       ${colors.white}: ${params.outdir}${colors.reset}"
        if (workflow.repository) {
            log.info ''
            log.info "${colors.biwhite}GitHub Settings:${colors.reset}"
            log.info "${colors.biblue}Repository       ${colors.white}: ${workflow.repository}${colors.reset}"
            log.info "${colors.biblue}Revision         ${colors.white}: ${workflow.revision}${colors.reset}"
            log.info "${colors.biblue}Commit ID        ${colors.white}: ${workflow.commitId}${colors.reset}"
        }
        log.info "${colors.white}---------------------------------------------------------------------------------------${colors.reset}"
    }

    //
    // ANSII Colours used for terminal logging
    //
    public static Map logColours(Boolean monochromeLogs) {
        Map colorcodes = [:]

        // Reset / Meta
        colorcodes['reset']      = monochromeLogs ? '' : "\033[0m"
        colorcodes['bold']       = monochromeLogs ? '' : "\033[1m"
        colorcodes['dim']        = monochromeLogs ? '' : "\033[2m"
        colorcodes['underlined'] = monochromeLogs ? '' : "\033[4m"
        colorcodes['blink']      = monochromeLogs ? '' : "\033[5m"
        colorcodes['reverse']    = monochromeLogs ? '' : "\033[7m"
        colorcodes['hidden']     = monochromeLogs ? '' : "\033[8m"

        // Regular Colors
        colorcodes['black']      = monochromeLogs ? '' : "\033[0;30m"
        colorcodes['red']        = monochromeLogs ? '' : "\033[0;31m"
        colorcodes['green']      = monochromeLogs ? '' : "\033[0;32m"
        colorcodes['yellow']     = monochromeLogs ? '' : "\033[0;33m"
        colorcodes['blue']       = monochromeLogs ? '' : "\033[0;34m"
        colorcodes['purple']     = monochromeLogs ? '' : "\033[0;35m"
        colorcodes['cyan']       = monochromeLogs ? '' : "\033[0;36m"
        colorcodes['white']      = monochromeLogs ? '' : "\033[0;37m"

        // Bold
        colorcodes['bblack']     = monochromeLogs ? '' : "\033[1;30m"
        colorcodes['bred']       = monochromeLogs ? '' : "\033[1;31m"
        colorcodes['bgreen']     = monochromeLogs ? '' : "\033[1;32m"
        colorcodes['byellow']    = monochromeLogs ? '' : "\033[1;33m"
        colorcodes['bblue']      = monochromeLogs ? '' : "\033[1;34m"
        colorcodes['bpurple']    = monochromeLogs ? '' : "\033[1;35m"
        colorcodes['bcyan']      = monochromeLogs ? '' : "\033[1;36m"
        colorcodes['bwhite']     = monochromeLogs ? '' : "\033[1;37m"

        // Underline
        colorcodes['ublack']     = monochromeLogs ? '' : "\033[4;30m"
        colorcodes['ured']       = monochromeLogs ? '' : "\033[4;31m"
        colorcodes['ugreen']     = monochromeLogs ? '' : "\033[4;32m"
        colorcodes['uyellow']    = monochromeLogs ? '' : "\033[4;33m"
        colorcodes['ublue']      = monochromeLogs ? '' : "\033[4;34m"
        colorcodes['upurple']    = monochromeLogs ? '' : "\033[4;35m"
        colorcodes['ucyan']      = monochromeLogs ? '' : "\033[4;36m"
        colorcodes['uwhite']     = monochromeLogs ? '' : "\033[4;37m"

        // High Intensity
        colorcodes['iblack']     = monochromeLogs ? '' : "\033[0;90m"
        colorcodes['ired']       = monochromeLogs ? '' : "\033[0;91m"
        colorcodes['igreen']     = monochromeLogs ? '' : "\033[0;92m"
        colorcodes['iyellow']    = monochromeLogs ? '' : "\033[0;93m"
        colorcodes['iblue']      = monochromeLogs ? '' : "\033[0;94m"
        colorcodes['ipurple']    = monochromeLogs ? '' : "\033[0;95m"
        colorcodes['icyan']      = monochromeLogs ? '' : "\033[0;96m"
        colorcodes['iwhite']     = monochromeLogs ? '' : "\033[0;97m"

        // Bold High Intensity
        colorcodes['biblack']    = monochromeLogs ? '' : "\033[1;90m"
        colorcodes['bired']      = monochromeLogs ? '' : "\033[1;91m"
        colorcodes['bigreen']    = monochromeLogs ? '' : "\033[1;92m"
        colorcodes['biyellow']   = monochromeLogs ? '' : "\033[1;93m"
        colorcodes['biblue']     = monochromeLogs ? '' : "\033[1;94m"
        colorcodes['bipurple']   = monochromeLogs ? '' : "\033[1;95m"
        colorcodes['bicyan']     = monochromeLogs ? '' : "\033[1;96m"
        colorcodes['biwhite']    = monochromeLogs ? '' : "\033[1;97m"

        return colorcodes
    }

    public static String dashedLine(monochromeLogs) {
        Map colors = logColours(monochromeLogs)
        return "${colors.white}=======  =======  =======  =======  =======  =======  =======  =======  ======= =======${colors.reset}"
    }

    //
    // nf-core logo
    //
    public static String logo(workflow, monochromeLogs) {
        Map colors = logColours(monochromeLogs)
        String workflow_version = CompanyTemplate.version(workflow)
        String.format(
            """\n
${dashedLine(monochromeLogs)}\n
${colors.biwhite}Nicely Formatted ${colors.biblue}Company${colors.biwhite} Name${colors.reset}
\n${dashedLine(monochromeLogs)}
""".stripIndent()
        )
    }

}
