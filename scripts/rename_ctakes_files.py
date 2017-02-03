"""

Renames files in a directory (command line argument) based on their documentID.
Works for cTAKES xmi or xml files if they have been renamed 'doc0.xml', etc. by cTAKES.
Makes cTAKES filenames compatible with BioMedICUS or CLAMP, which keep the original filename.

"""

import re
import subprocess
import sys
import os

try:
    d = sys.argv[1]
except:
    print('usage:\npython ' + sys.argv[0] + ' <directory_containing_ctakes_outputs>')
    sys.exit(1)

files = subprocess.check_output(["ls", d]).split("\n")
files = [f for f in files if len(f) and 'TypeSystem' not in f]

for f in files:
    text = open(os.path.join(d, f)).read()
    doc_id = re.search('documentID="([^"]+)"', text).group(1)
    doc_id = os.path.splitext(doc_id)[0]
    ext = os.path.splitext(f)[1]
    if ext == "":
        ext = ".xml"
    subprocess.call(['mv', os.path.join(d, f), os.path.join(d, doc_id + ext)])
