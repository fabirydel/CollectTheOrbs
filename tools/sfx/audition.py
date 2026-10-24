"""Builds a single-file HTML page to audition the sound options (and compare with the current sounds).

Run from the repo root:  python3 tools/sfx/audition.py   -> ~/Downloads/CollectTheOrbs-sound-options.html
"""
import base64, json, os, subprocess, sys, tempfile
sys.path.insert(0, os.path.dirname(__file__))
import synth

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
AUDIO = os.path.join(ROOT, 'android', 'assets', 'audio')
CURRENT = {  # sound -> (file at the start, file at the end)
    'menu_tap': ('buttonSmall', None), 'menu_big': ('buttonLarge', None), 'cant': ('select', None), 'bought': ('bought', None),
    'launch': ('launch', None), 'pre': ('pre', None), 'absorb': ('absorb', None), 'wrong': ('loseLife', None), 'shield_hit': ('shield', None),
    'life': ('life', None), 'dead': ('dead', None), 'item_pickup': ('object', None), 'item_tick': ('objectCount', None),
    'swap': ('swap', 'swapReverse'), 'mult': ('mult', 'multReverse'), 'slow': ('slow', 'slowReverse'),
    'shield_item': ('sword', 'swordReverse'), 'switch': ('change2', None), 'count_tick': ('bought', None),
}
END = {'swap': 'swap_end', 'mult': 'mult_end', 'slow': 'slow_end', 'shield_item': 'shield_item_end'}


def mp3(src):
    out = tempfile.mktemp(suffix='.mp3')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', src, '-ac', '2', '-b:a', '96k', out], check=True)
    data = open(out, 'rb').read()
    os.remove(out)
    return 'data:audio/mpeg;base64,' + base64.b64encode(data).decode()


def main():
    wavdir = tempfile.mkdtemp()
    synth.render_all(wavdir)
    rows = []
    for name, desc, fn in synth.SOUNDS:
        cur = CURRENT[name]
        opts = {}
        cur_clip = {'start': mp3(os.path.join(AUDIO, cur[0] + '.ogg'))}
        if cur[1]:
            cur_clip['end'] = mp3(os.path.join(AUDIO, cur[1] + '.ogg'))
        opts['Current'] = cur_clip
        for kind, label in (('A', 'A Glass'), ('B', 'B Synth'), ('C', 'C Pulse')):
            clip = {'start': mp3(os.path.join(wavdir, '%s_%s.wav' % (name, kind)))}
            if name in END:
                clip['end'] = mp3(os.path.join(wavdir, '%s_%s.wav' % (END[name], kind)))
            opts[label] = clip
        rows.append({'name': name, 'desc': desc, 'opts': opts})
    html = TEMPLATE.replace('__DATA__', json.dumps(rows))
    path = os.path.expanduser('~/Downloads/CollectTheOrbs-sound-options.html')
    open(path, 'w').write(html)
    print(path, round(len(html) / 1e6, 1), 'MB')


TEMPLATE = r'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Collect The Orbs - sound options</title>
<style>
:root{--bg:#07070b;--card:#12121a;--line:#2a2a38;--text:#e8e8ef;--dim:#8d8da0;--a:#4cc9ff;--b:#b46cff;--c:#ffbe3c;--cur:#9a9aab}
@media (prefers-color-scheme: light){:root{--bg:#f4f4f8;--card:#fff;--line:#d6d6e0;--text:#16161d;--dim:#6a6a7c}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:16px/1.5 system-ui,sans-serif;padding:20px 16px 90px;max-width:920px;margin:0 auto}
h1{font-size:1.5rem;margin:.2em 0}p.lead{color:var(--dim);margin:.2em 0 1.2em}
.row{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:14px;margin:12px 0}
.row h2{font-size:1.05rem;margin:0}.row small{color:var(--dim)}
.opts{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:10px;margin-top:10px}
.opt{border:1.5px solid var(--line);border-radius:12px;padding:10px;cursor:pointer}
.opt.sel{border-color:var(--sel,#4cc9ff);box-shadow:0 0 0 2px color-mix(in srgb,var(--sel,#4cc9ff) 30%,transparent)}
.opt b{display:block;margin-bottom:6px}.btns{display:flex;gap:6px;flex-wrap:wrap}
button{font:inherit;border:1px solid var(--line);background:transparent;color:var(--text);border-radius:8px;padding:6px 12px;cursor:pointer}
button:hover{border-color:var(--text)}
#bar{position:fixed;left:0;right:0;bottom:0;background:var(--card);border-top:1px solid var(--line);padding:10px 16px;display:flex;gap:10px;align-items:center;justify-content:center;flex-wrap:wrap}
#bar span{color:var(--dim);font-size:.9rem}
</style></head><body>
<h1>Sound options</h1>
<p class="lead">Tap a button to hear it, then tap a card to pick it. <b>Current</b> is the sound in the game now. Three new families: <b style="color:var(--a)">A Glass</b> (soft, shimmering), <b style="color:var(--b)">B Synth</b> (synthwave pluck, echo), <b style="color:var(--c)">C Pulse</b> (tight, arcade-clean). Items with a start and an end sound (swap, x2/x3, slow, shield) have two buttons.</p>
<div id="rows"></div>
<div id="bar"><span id="count"></span><button id="copy">Copy my picks</button><span id="msg"></span></div>
<script>
const DATA=__DATA__;const COL={'Current':'var(--cur)','A Glass':'var(--a)','B Synth':'var(--b)','C Pulse':'var(--c)'};
let picks={};try{picks=JSON.parse(localStorage.getItem('cto-picks')||'{}')}catch(e){}
let audio=null;
function play(src){if(audio){audio.pause()}audio=new Audio(src);audio.play()}
function save(){try{localStorage.setItem('cto-picks',JSON.stringify(picks))}catch(e){}
document.getElementById('count').textContent=Object.keys(picks).length+' of '+DATA.length+' picked'}
function render(){const root=document.getElementById('rows');root.innerHTML='';
DATA.forEach(r=>{const d=document.createElement('div');d.className='row';
d.innerHTML='<h2>'+r.name.replace(/_/g,' ')+'</h2><small>'+r.desc+'</small>';
const o=document.createElement('div');o.className='opts';
Object.entries(r.opts).forEach(([k,clip])=>{const c=document.createElement('div');c.className='opt'+(picks[r.name]===k?' sel':'');c.style.setProperty('--sel',COL[k]);
c.innerHTML='<b style="color:'+COL[k]+'">'+k+'</b>';const b=document.createElement('div');b.className='btns';
[['start',clip.end?'Start':'Play'],['end','End']].forEach(([key,label])=>{if(clip[key]){const x=document.createElement('button');x.textContent='▶ '+label;x.onclick=e=>{e.stopPropagation();play(clip[key])};b.appendChild(x)}});
c.appendChild(b);c.onclick=()=>{picks[r.name]=k;save();render()};o.appendChild(c)});
d.appendChild(o);root.appendChild(d)});save()}
document.getElementById('copy').onclick=()=>{const t=DATA.map(r=>r.name+': '+(picks[r.name]||'(none)')).join('\n');navigator.clipboard.writeText(t).then(()=>{document.getElementById('msg').textContent='Copied'},()=>{prompt('Copy your picks',t)})};
render();
</script></body></html>'''

if __name__ == '__main__':
    main()
