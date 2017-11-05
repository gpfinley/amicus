import sys
import subprocess
import json

"""
Search the local Elasticsearch index. Usage:
  python single_search.py <fieldname> <querystring>
"""

esindex = 'amicusdocs'
estype = 'amicusdoc'

if len(sys.argv) != 3:
    print "Usage:\n    python single_search.py <fieldname> <querystring>"
    sys.exit(1)

field, searchstring = sys.argv[1:3]

command = "curl http://localhost:9200/%s/%s/_search -d" % (esindex, estype)
query = ['{"query":{"match":{"%s":"%s"}}}' % (field, searchstring)]

outputraw = subprocess.check_output(command.split() + query)
output = json.loads(outputraw)

print len(output['hits']['hits']), 'hits:'
for hit in output['hits']['hits']:
    source = hit['_source']
    id = source['docId']
#    text = source['text'].strip()[:60].replace("\n", " ") + '...'
    print id #+ '    ' + text
