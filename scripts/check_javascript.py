from pathlib import Path
import re, subprocess
root=Path(__file__).resolve().parent.parent
out=root/'.runtime/jscheck';out.mkdir(parents=True,exist_ok=True)
failures=[]
for p in (root/'ht_vlxd/src/main/resources/templates').rglob('*.html'):
    for i,(attrs,code) in enumerate(re.findall(r'<script([^>]*)>([\s\S]*?)</script>',p.read_text(encoding='utf-8'))):
        if 'src=' in attrs or not code.strip() or 'application/' in attrs:continue
        code=re.sub(r'\[\[\$\{.*?\}\]\]', '0', code)
        target=out/f'{p.stem}-{i}.js';target.write_text(code,encoding='utf-8')
        result=subprocess.run(['node','--check',str(target)],capture_output=True,text=True,encoding='utf-8')
        if result.returncode:failures.append((str(p.relative_to(root)),result.stderr))
for p in (root/'ht_vlxd/src/main/resources/static/js').glob('*.js'):
    result=subprocess.run(['node','--check',str(p)],capture_output=True,text=True,encoding='utf-8')
    if result.returncode:failures.append((str(p.relative_to(root)),result.stderr))
for path,error in failures:print(path+'\n'+error)
print(f'JavaScript syntax failures: {len(failures)}')
raise SystemExit(bool(failures))
