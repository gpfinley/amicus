import os
import subprocess

DOCSPATH='/home/ubuntu/amicus/example_data/mtsamples_plaintext/'
INDEX = 'amicusdocs'
TYPE = 'amicusdoc'

def escape(orig):
    replacements = [("\n","\\n"), ("\t","\\t"), ("\r","\\r"), ('"','\\"')]
    for char, repl in replacements:
        orig = orig.replace(char, repl)
    return orig

for f in os.listdir(DOCSPATH):
    data = open(os.path.join(DOCSPATH, f)).read()
    data = escape(data)
    ID = f.split('.')[0]
    cmd = ['curl', '-X', 'POST', 'http://localhost:9200/' + INDEX + '/' + TYPE + '/' + ID, '-d', '{"docId":"' + ID + '", "text":"' + data + '"}']
    print 'COMMAND:'
    print ' '.join(cmd)
    print 'OUTPUT:'
    print subprocess.check_output(cmd)
