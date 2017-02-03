#!/usr/bin/env bash

set -e

# Will run necessary Classes to go from system annotations of i2b2 2011 data to .con files compatible with that task.
# See also the config files: corefConceptMergerConfiguration.yml and corefConceptTextwriterConfiguration.yml.

i2b2datapath=/Users/gpfinley/i2b2_past/2011/Beth_Train/docs
i2b2goldpath=/Users/gpfinley/i2b2_past/2011/Beth_Train/concepts
#i2b2datapath=/Users/gpfinley/i2b2_past/2011/i2b2_Test/i2b2_Beth_Test/docs/
#i2b2goldpath=/Users/gpfinley/i2b2_past/2011/i2b2_Test/i2b2_Beth_Test/concepts/
extractedpath=data/plaintextAnnotations/corefConcepts
#extractedpath=data/plaintextAnnotations/corefConcepts_beth_test
hypothesispath=data/corefConceptsCon
#evalscriptpath=...
#goldstandardpath=...

#python scripts/rename_ctakes_files.py data/i2b2data/ctakes/

# -s for skip java
if [ "$1" != "-s" ]
  then
    java -Dconfig=corefConceptMergerConfiguration.yml -jar target/ensembles-1.0-SNAPSHOT-jar-with-dependencies.jar
    java -Dconfig=corefConceptTextwriterConfiguration.yml -jar target/ensembles-1.0-SNAPSHOT-jar-with-dependencies.jar extract
fi
java -cp target/ensembles-1.0-SNAPSHOT-jar-with-dependencies.jar edu.umn.ensembles.filetools.ExtractedToCorefConcepts $i2b2datapath $extractedpath $hypothesispath

python ~/ensembles/eval_scripts/con.py $i2b2goldpath $hypothesispath
