#!/usr/bin/env python3
import argparse, json, shutil, sys
from pathlib import Path
from docx import Document

def extract(path):
    doc=Document(path); blocks=[]
    blocks.extend(p.text.strip() for p in doc.paragraphs if p.text.strip())
    for table in doc.tables:
        for row in table.rows:
            blocks.extend(cell.text.strip() for cell in row.cells if cell.text.strip())
    print(json.dumps({'text':'\n'.join(blocks),'bullets':[p.text.strip() for p in doc.paragraphs if p.text.strip() and ('list' in p.style.name.lower() or p.text.lstrip().startswith(('•','-')))]}))

def generate(source,target,bullets):
    shutil.copy2(source,target); doc=Document(target); candidates=[p for p in doc.paragraphs if p.text.strip() and ('list' in p.style.name.lower() or p.text.lstrip().startswith(('•','-')))]
    if not candidates: raise RuntimeError('No bullet paragraphs found in master resume')
    for paragraph,value in zip(candidates,bullets):
        if paragraph.runs:
            paragraph.runs[0].text=value
            for run in paragraph.runs[1:]: run.text=''
        else: paragraph.text=value
    doc.save(target); print(json.dumps({'path':str(Path(target).resolve()),'replaced':min(len(candidates),len(bullets))}))

p=argparse.ArgumentParser(); p.add_argument('command',choices=['extract','generate']); p.add_argument('--source',required=True); p.add_argument('--target'); args=p.parse_args()
try:
    if args.command=='extract': extract(args.source)
    else: generate(args.source,args.target,json.load(sys.stdin)['bullets'])
except Exception as e:
    print(str(e),file=sys.stderr); sys.exit(2)
