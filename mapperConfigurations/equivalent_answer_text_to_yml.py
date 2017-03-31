# Simple script to make it easier to manually create equivalent answer configurations.
#
# Read in a text file of groups of terms and convert into a representation
#   that can be read in as an EquivalentAnswerMapper config file.
# The text file should have each term on its own line, with an extra newline
#   separating each group of equivalent terms.

import os
import sys
import re


def wrap_if_necessary(term):
    if ',' in term or ':' in term:
        return term.join(("'", "'"))
    return term

infile = sys.argv[1]

alltext = open(infile).read()

groups = [g.strip().split('\n') for g in re.split('\n\\s*\n', alltext)]

w = open(os.path.splitext(infile)[0] + '.yml', 'w')
w.write("!!edu.umn.amicus.mappers.EquivalentAnswerMapper\nequivalentsList:\n")
for g in groups:
    w.write("- [")
    w.write(', '.join([wrap_if_necessary(t) for t in g]))
    w.write(']\n')
