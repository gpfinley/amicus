# ensembles

Combine outputs of UIMA-based annotators (like BioMedICUS, cTAKES, CLAMP) into a single output.

## How it works

Runs a single UIMA CPE for merging. The Collection Reader reads in names of XMI files from individual systems directories in 'systemsData'.

Runs a separate CPE for evaluating a merged system on a specific task.

## Current status

Works. Lots of documentation needed (both usage and API). Will add more tasks (and hopefully interesting ones).
