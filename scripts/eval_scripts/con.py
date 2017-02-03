from __future__ import division
import sys
import os
import re

gold_path, hyp_path = sys.argv[1:3]

truepos = 0
falsepos = 0
falseneg = 0

labelcorrect = 0

def get_spans(line):
    return tuple(line.split('|')[0].split()[-2:])

for fname in os.listdir(hyp_path):
    if fname.startswith('.'): continue
    hyplines = [line.lower().strip() for line in open(os.path.join(hyp_path, fname))]
    goldlines = [line.lower().strip() for line in open(os.path.join(gold_path, fname))]

    hyps_list = [get_spans(l) for l in hyplines]
    hyps = set(hyps_list)
    if (len(hyps_list) != len(hyps)):
        print 'WARNING: multiple markers detected on same span in file', fname
        for x in hyps:
            count = sum([y==x for y in hyps_list])
            if count > 1:
                print fname, x, count
    #hyps = {get_spans(l) for l in hyplines}
    hyp_labels = [l.split('|')[-1] for l in hyplines]
    golds = {get_spans(l) for l in goldlines}
    
    labelcorrect += len(set(hyplines).intersection(set(goldlines)))

    fpset = hyps.difference(golds)
    fnset = golds.difference(hyps)
    tpset = golds.intersection(hyps)

    falseneg += len(fnset)
    falsepos += len(fpset)
    truepos += len(tpset)

    print fname
    #print fpset
    for hs, hl in zip(hyps_list, hyp_labels):
        if hs not in golds and 'person' in hl:
            print hs, hl

#    if len(set(hyplines).intersection(set(goldlines))) > len(golds.intersection(hyps)):
#        print hyplines
#        print goldlines
#        print fname

print 'true positives', truepos
print 'false positives', falsepos
print 'false negatives', falseneg

prec = truepos / (truepos + falsepos)
rec = truepos / (truepos + falseneg)

print 'precision', prec
print 'recall', rec
F = 2 * prec * rec / (prec + rec)
print 'F', F

print 'label correct % (among true positives)', labelcorrect / truepos

print 'scrict F (labels correct)', labelcorrect / truepos * F
