"""Treat the game's existing sounds (tools/sfx/originals) and build an audition page to compare the treatments.

Treatments (volume-matched to the original so A/B is fair):
  Polished : trim silence, fade the ends, remove sub-bass rumble; no tonal change
  Neon     : chorus, a shimmering reverb, a brighter top end and a wider stereo image
  Deeper   : pitched down ~2 semitones, more low end, a touch of warm saturation
  Crisp    : brighter and tighter, little room

Run from the repo root:  python3 tools/sfx/treat.py   -> ~/Downloads/CollectTheOrbs-sound-options.html
"""
import base64, json, os, subprocess, sys, tempfile, wave
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, fftconvolve, resample_poly

SR = 44100
HERE = os.path.dirname(os.path.abspath(__file__))
ORIG = os.path.join(HERE, 'originals')

SOUNDS = [  # name, file, description, file at the end (if any)
    ('menu_tap', 'buttonSmall', 'Small menu buttons', None), ('menu_big', 'buttonLarge', 'Big buttons: home, retry', None),
    ('cant', 'select', "Can't afford it", None), ('bought', 'bought', 'Buying something (also the game-over counter)', None),
    ('launch', 'launch', 'START: the game launches', None), ('pre', 'pre', 'Cue just before the run', None),
    ('absorb', 'absorb', 'Catching an orb with the right shield', None), ('wrong', 'loseLife', 'Hitting an orb with the wrong shield', None),
    ('shield_hit', 'shield', 'A hit absorbed by a shield item', None), ('life', 'life', 'Gold orb: energy restored', None),
    ('dead', 'dead', 'Game over', None), ('item_pickup', 'object', 'Picking up an item', None),
    ('item_tick', 'objectCount', 'Item about to run out', None), ('swap', 'swap', 'Color swap item (start / end)', 'swapReverse'),
    ('mult', 'mult', 'x2 / x3 item (start / end)', 'multReverse'), ('slow', 'slow', 'Slow item (start / end)', 'slowReverse'),
    ('shield_item', 'sword', 'Shield item (start / end)', 'swordReverse'), ('switch', 'change2', 'Switching shield color', None),
]


def load(name):
    tmp = tempfile.mktemp(suffix='.wav')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', os.path.join(ORIG, name + '.ogg'), '-ac', '2', '-ar', str(SR), tmp], check=True)
    with wave.open(tmp) as w:
        x = np.frombuffer(w.readframes(w.getnframes()), dtype='<i2').astype(float).reshape(-1, 2).T / 32768
    os.remove(tmp)
    return x


def biquad(x, kind, f0, gain_db=0.0, q=0.707):
    A = 10 ** (gain_db / 40)
    w = 2 * np.pi * f0 / SR
    c, s = np.cos(w), np.sin(w)
    al = s / (2 * q)
    if kind == 'lowshelf':
        b = [A * ((A + 1) - (A - 1) * c + 2 * np.sqrt(A) * al), 2 * A * ((A - 1) - (A + 1) * c), A * ((A + 1) - (A - 1) * c - 2 * np.sqrt(A) * al)]
        a = [(A + 1) + (A - 1) * c + 2 * np.sqrt(A) * al, -2 * ((A - 1) + (A + 1) * c), (A + 1) + (A - 1) * c - 2 * np.sqrt(A) * al]
    elif kind == 'highshelf':
        b = [A * ((A + 1) + (A - 1) * c + 2 * np.sqrt(A) * al), -2 * A * ((A - 1) + (A + 1) * c), A * ((A + 1) + (A - 1) * c - 2 * np.sqrt(A) * al)]
        a = [(A + 1) - (A - 1) * c + 2 * np.sqrt(A) * al, 2 * ((A - 1) - (A + 1) * c), (A + 1) - (A - 1) * c - 2 * np.sqrt(A) * al]
    else:  # highpass
        b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
        a = [1 + al, -2 * c, 1 - al]
    b, a = np.array(b) / a[0], np.array(a) / a[0]
    return np.stack([lfilter(b, a, ch) for ch in x])


def trim(x, thresh=0.004, fade=0.004):
    amp = np.max(np.abs(x), axis=0)
    idx = np.where(amp > thresh * amp.max())[0]
    x = x[:, idx[0]: idx[-1] + 1].copy()
    f = min(int(fade * SR), x.shape[1] // 2)
    x[:, :f] *= np.linspace(0, 1, f)
    x[:, -f:] *= np.linspace(1, 0, f)
    return x


def reverb(x, tau, wet, seed=2, tail=True):
    r = np.random.RandomState(seed)
    n = int(tau * 3 * SR) + 64
    t = np.arange(n) / SR
    out = []
    for ch in range(2):
        ir = sosfilt(butter(2, 6000, 'low', fs=SR, output='sos'), r.randn(n)) * np.exp(-t / tau)
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        out.append(fftconvolve(x[ch], ir)[: x.shape[1] + (n // 2 if tail else 0)])
    L = x.shape[1] + (n // 2 if tail else 0)
    dry = np.pad(x, ((0, 0), (0, L - x.shape[1])))
    return dry * (1 - wet) + np.stack(out) * wet * 2.0


def chorus(x, depth=0.0025, rate=0.8, mix=0.4):
    n = x.shape[1]
    t = np.arange(n) / SR
    out = np.zeros_like(x)
    for ch in range(2):
        d = (0.014 + depth * np.sin(2 * np.pi * rate * t + ch * 1.7)) * SR
        pos = np.arange(n) - d
        i = np.clip(np.floor(pos).astype(int), 0, n - 1)
        fr = pos - np.floor(pos)
        out[ch] = x[ch][i] * (1 - fr) + x[ch][np.clip(i + 1, 0, n - 1)] * fr
    return x * (1 - mix * 0.5) + out * mix


def widen(x, ms=0.011):
    m = (x[0] + x[1]) / 2
    s = (x[0] - x[1]) / 2
    d = int(ms * SR)
    s2 = np.pad(s, (d, 0))[: len(s)] * 1.6
    return np.stack([m + s2, m - s2])


def shimmer(x, level=0.14):
    """An octave-up copy, delayed a little, fed into the reverb: a glassy halo."""
    up = resample_poly(x, 1, 2, axis=1)
    up = np.pad(up, ((0, 0), (int(0.03 * SR), x.shape[1] - up.shape[1] - int(0.03 * SR) if x.shape[1] > up.shape[1] + int(0.03 * SR) else 0)))[:, : x.shape[1]]
    return x + level * up


def match_level(y, ref):
    rms = lambda a: np.sqrt(np.mean(a ** 2)) + 1e-9
    y = y * (rms(ref) / rms(y))
    peak = np.max(np.abs(y))
    if peak > 0.95:   # soft-limit rather than clip
        y = np.tanh(y * 1.0 / 0.95) * 0.95 if peak < 1.6 else y * (0.95 / peak)
    return y


def polished(x):
    return trim(biquad(x, 'highpass', 35))


def neon(x):
    y = chorus(x)
    y = widen(y)
    y = biquad(y, 'highshelf', 5000, 3.5)
    y = reverb(shimmer(y), 0.32, 0.26)
    return trim(y, 0.006)


def deeper(x):
    y = resample_poly(x, 9, 8, axis=1)   # ~ -2 semitones (and a little longer)
    y = biquad(y, 'lowshelf', 160, 4.5)
    y = np.tanh(y * 1.5) / 1.5 * 1.1
    y = reverb(y, 0.12, 0.1)
    return trim(y, 0.006)


def crisp(x):
    y = biquad(x, 'highpass', 140)
    y = biquad(y, 'highshelf', 4000, 4.5)
    y = reverb(y, 0.05, 0.08)
    return trim(y, 0.006)


TREATMENTS = [('Polished', polished), ('Neon', neon), ('Deeper', deeper), ('Crisp', crisp)]


def mp3(x):
    wav = tempfile.mktemp(suffix='.wav')
    out = tempfile.mktemp(suffix='.mp3')
    with wave.open(wav, 'wb') as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((np.clip(x.T, -1, 1) * 32767).astype('<i2').tobytes())
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', wav, '-b:a', '112k', out], check=True)
    data = open(out, 'rb').read()
    os.remove(wav)
    os.remove(out)
    return 'data:audio/mpeg;base64,' + base64.b64encode(data).decode()


def clips(file):
    x = load(file)
    ref = trim(x)
    out = {'Current': mp3(x)}
    for name, fn in TREATMENTS:
        out[name] = mp3(match_level(fn(x), ref))
    return out


def main():
    rows = []
    for name, file, desc, end in SOUNDS:
        start = clips(file)
        endc = clips(end) if end else {}
        opts = {}
        for label in ['Current'] + [t[0] for t in TREATMENTS]:
            clip = {'start': start[label]}
            if end:
                clip['end'] = endc[label]
            opts[label] = clip
        rows.append({'name': name, 'desc': desc, 'opts': opts})
    path = os.path.expanduser('~/Downloads/CollectTheOrbs-sound-options.html')
    html = TEMPLATE.replace('__DATA__', json.dumps(rows))
    open(path, 'w').write(html)
    print(path, round(len(html) / 1e6, 1), 'MB')


TEMPLATE = r'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Collect The Orbs - sound options</title>
<style>
:root{--bg:#07070b;--card:#12121a;--line:#2a2a38;--text:#e8e8ef;--dim:#8d8da0;--a:#4cc9ff;--b:#b46cff;--c:#ffbe3c;--cur:#9a9aab;--d:#5be3a6}
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
<p class="lead">These are the <b>current game sounds</b>, treated. Tap ▶ to hear one, then tap a card to pick it. Volumes are matched to the current sound so the comparison is fair. <b style="color:var(--a)">Polished</b> only cleans up (trims silence, fades the ends, no tonal change). <b style="color:var(--b)">Neon</b> adds a shimmering reverb, a little chorus, a brighter top and a wider stereo image. <b style="color:var(--c)">Deeper</b> is pitched down a little with more low end and a touch of warmth. <b style="color:var(--d)">Crisp</b> is brighter and tighter with almost no room. Items with a start and an end sound have two buttons.</p>
<div id="rows"></div>
<div id="bar"><span id="count"></span><button id="copy">Copy my picks</button><span id="msg"></span></div>
<script>
const DATA=__DATA__;const COL={'Current':'var(--cur)','Polished':'var(--a)','Neon':'var(--b)','Deeper':'var(--c)','Crisp':'var(--d)'};
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
