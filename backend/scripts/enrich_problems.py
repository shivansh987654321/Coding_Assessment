#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src' / 'main' / 'resources'
FILES = ['problems_all_easy.json', 'problems_all_medium.json', 'problems_all_hard.json']

TEMPLATE = "Problem: {title}\nSource: {url}\n\nDescription: Provide an implementation for '{title}'. Refer to the source URL for full details, constraints, and sample IO.\n"

def enrich_file(path: Path):
    data = json.loads(path.read_text())
    changed = False
    for entry in data:
        if 'statement' not in entry or entry.get('statement') is None or entry.get('statement') == "":
            title = entry.get('title','Untitled')
            url = entry.get('url','')
            entry['statement'] = TEMPLATE.format(title=title, url=url)
            if 'samples' not in entry:
                entry['samples'] = []
            changed = True
    if changed:
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False))
        print(f'Enriched {path.name} ({len(data)} entries)')
    else:
        print(f'No changes for {path.name}')

def main():
    for f in FILES:
        p = ROOT / f
        if p.exists():
            enrich_file(p)
        else:
            print('Missing', f)

if __name__ == '__main__':
    main()
