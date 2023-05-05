# Automated Mock Generation of External Web Service Interactions for White-Box Fuzzing

In this package, we provide necessary information for replicating the experiment in the paper that includes 


- [EvoMaster](EvoMaster): tools described in the paper with automate mock web service generation.
- [jar](jar): runnable jar for EvoMaster.
- [EMB](EMB): three JVM case studies employed for the experiment.
- [build.py](build.py): a python script to build tools and all three case studies from.
- [exp.py](exp.py): a python script to generate scripts to conduct experiments.
- [R-Scripts](R-Scripts): an R script to analyse results and generate tables and figures reported in the paper.
- [example](example): folder contains an example generated tests from a case study


## Building everything

In this repo, we provide a python script to build tools and all three case studies.

Go to the root, run 
> `python3 build.py`

After the build is finished, for each of the case studies, JARs will be available under the `dist` folder.

For EvoMaster, its `jar` file would be found under `EvoMaster/core/target`.

> Note: Docker is required to run `genome-nexus` case study.

## Run Experiments

`exp.py <cluster> <baseSeed> <dir> <minSeed> <maxSeed> <maxActions> <minutesPerRun> <nJobs>`

The above command will produce scripts to execute the experiments. Additional information about replicating studies can be found on the below provided link.

## Analyse Results

The below archives contains results from the experiments we conducted.

- internet_fcompressedData.zip
- internet_fsnapshotCompressedData.zip
- internet_tcompressedData.zip
- internet_tsnapshotCompressedData.zip

You can analyse these using the provided R scripts.


## Links

We ran experiments on EMB based on the provided guidelines from the authors. You can find more information from these links, if needed.

- [EMB](https://github.com/EMResearch/EMB/blob/master/README.md): you can find more information about building case studies
- [EvoMaster](https://github.com/EMResearch/EvoMaster/blob/master/docs/build.md): you can find more information about building EvoMaster
- [Replicating Studies](https://github.com/EMResearch/EvoMaster/blob/master/docs/replicating_studies.md): you can find more information about how to replicate studies