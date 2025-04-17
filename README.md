# merge-fastq
Nextflow pipeline to merge fastq files

# 1. Running the pipeline
To merge the different lanes of a Fastq file into one on AWS, use the following command and replace `<run-name>` with your run name.

## On AWS
```
nextflow run -profile aws nf-merge-fastq -r v1.0.0 --runName '<run-name>'
```
The pipeline will attempt to find Fastq files with the following pattern: `S3://<input-bucket>/<run-name>/Fastq/*_R{1,2}_001.fastq.gz`.
If such files do not exist, please use the parameter `--reads` to define the correct read location. The merged fastq files will be placed in a `Merged` directory in the input directory. If you want it to be placed elsewhere than the input directory, please specify the output path using `--outName`