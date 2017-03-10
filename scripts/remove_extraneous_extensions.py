"""

Renames files in a directory (command line argument) to not have intermediate extensions.
Works for cTAKES xmi or xml files if the original file extension has been retained.

"""

import re
import subprocess
import sys
import os

try:
    d = sys.argv[1]
except:
    print('usage:\npython ' + sys.argv[0] + ' <path-to-directory>')
    sys.exit(1)

files = subprocess.check_output(["ls", d]).split("\n")
files = [f for f in files if len(f) and 'TypeSystem' not in f]

for f in files:
    oldname = f
    newname = re.sub('(\....)+(\.xm[il])', '\\2', oldname, flags=re.IGNORECASE)
    subprocess.call(['mv', os.path.join(d, oldname), os.path.join(d, newname)])
