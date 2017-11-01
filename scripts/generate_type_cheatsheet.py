#!/usr/bin/python

"""
Quick script to generate a cheat sheet for all type systems currently installed with AMICUS
"""

import os
import sys
import re
from xml.etree import ElementTree

script_home = os.path.dirname(sys.argv[0])
type_systems_dir = os.path.join(script_home, '..', 'src', 'main', 'resources', 'typeSystems')
type_systems = os.listdir(type_systems_dir)
type_systems = [t for t in type_systems if t.lower().endswith('xml')]

output = open("typeSystemsCheatSheet.txt", "w")

all_descriptions = {}

def safewrite(maybetext):
    if maybetext is not None:
        output.write(maybetext)

def write_name(item):
    output.write("\n")
    output.write(item.text.strip())

def save_description(typename, item):
    if item is not None and item.text and len(item.text.strip()):
        all_descriptions[typename] = process(item.text)
#        output.write("  -  ")
#        output.write(process(item.text.strip()))

def process(text):
    return re.sub("\\s+", " ", text.strip())

def write_feature_desc(item):
    for maybename in item:
        if maybename.tag.endswith('name') and maybename.text is not None:
            output.write("\n\t")
            output.write(maybename.text)
            save_description(maybename.text, item)

def write_optional_isa(item):
    for maybesuper in item:
        if maybesuper.tag.endswith('supertypeName') and maybesuper.text:
            output.write("            (subtype of " + maybesuper.text + ")")
            
    

for type_system in type_systems:
    output.write(type_system.split('.xml')[0])
    output.write("\n")
    type_desc_elements = {}
    tree = ElementTree.parse(os.path.join(type_systems_dir, type_system))
    root = tree.getroot()
    for item in root.iter():
        if item.tag.endswith('typeDescription'):
            for maybename in item:
                if maybename.tag.endswith('name') and maybename.text:
                    type_desc_elements[maybename.text] = item

    for typename in sorted(type_desc_elements.keys()):
        item = type_desc_elements[typename]
        for maybename in item:
            if maybename.tag.endswith('name') and maybename.text:
                write_name(maybename)
                write_optional_isa(item)
                for maybedesc in item:
                    if maybedesc.tag.endswith('description'):
                        save_description(maybename.text, maybedesc)
                for maybefeatdesc in item.iter():
                    if maybefeatdesc.tag.endswith('featureDescription'):
                        write_feature_desc(maybefeatdesc)

    output.write("\n\n")


output.write("GLOSSARY\n\n")
for typename in sorted(all_descriptions.keys()):
    desc = all_descriptions[typename]
    output.write(typename)
    output.write("\n")
    output.write(desc)
    output.write("\n\n")

output.close()
