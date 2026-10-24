"""Builds a single-file HTML page to audition the music loops (with a button that jumps to the loop point).

Run from the repo root:  python3 tools/music/audition.py   -> ~/Downloads/CollectTheOrbs-music-options.html
"""
import base64, json, os, subprocess, sys, tempfile, wave
import numpy as np
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import compose

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
SR = compose.SR
NAMES = {'synthwave': ('Synthwave Run', 'Neon-city driving (116 BPM, A minor): punchy kick and claps, a bouncing sub bass, pumping saw-chord stabs, a gated arpeggio and a hook melody. Intro, groove, a breakdown with a riser, a second groove.'),
         'techno_pulse': ('Techno Pulse', 'Dark and hypnotic (128 BPM, D minor): a hard four-on-the-floor kick, a rolling acid bassline with a squelching filter, rolling hats, minimal chords and an eerie lead in the breakdown.'),
         'neon_house': ('Neon House', 'Bright and bouncy (124 BPM, C minor): offbeat bass, heavily sidechained chords that pump with the kick, a plucky running arpeggio and a catchy lead.')}


def wav_b64(x):
    """Lossless, so the page's loop point has no MP3 encoder gap."""
    wav = tempfile.mktemp(suffix='.wav')
    compose.write_wav(wav, x)
    d = open(wav, 'rb').read()
    os.remove(wav)
    return 'data:audio/wav;base64,' + base64.b64encode(d).decode(), len(d)


def mp3_b64(x):
    wav = tempfile.mktemp(suffix='.wav')
    out = tempfile.mktemp(suffix='.mp3')
    compose.write_wav(wav, x)
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', wav, '-b:a', '128k', out], check=True)
    d = open(out, 'rb').read()
    os.remove(wav), os.remove(out)
    return 'data:audio/mpeg;base64,' + base64.b64encode(d).decode(), len(d)


def current_music():
    tmp = tempfile.mktemp(suffix='.wav')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', os.path.join(ROOT, 'android', 'assets', 'audio', 'music.ogg'), '-ac', '2', '-ar', str(SR), tmp], check=True)
    with wave.open(tmp) as w:
        x = np.frombuffer(w.readframes(w.getnframes()), dtype='<i2').astype(float).reshape(-1, 2).T / 32768
    os.remove(tmp)
    return x


def main():
    cur = current_music()
    target = np.sqrt(np.mean(cur ** 2))
    rows = []
    c64, _ = mp3_b64(cur)
    rows.append({'id': 'current', 'name': 'Current music', 'desc': 'The music in the game now (84 s).', 'src': c64, 'dur': cur.shape[1] / SR})
    for key, spec in compose.TRACKS.items():
        x = compose.render_track(spec)
        x = x * (target / (np.sqrt(np.mean(x ** 2)) + 1e-9))
        x = x * min(1.0, 0.95 / np.max(np.abs(x)))
        src, size = wav_b64(x)
        rows.append({'id': key, 'name': NAMES[key][0], 'desc': NAMES[key][1], 'src': src, 'dur': x.shape[1] / SR})
        print(key, round(x.shape[1] / SR, 1), 's', round(size / 1e6, 2), 'MB wav')
    path = os.path.expanduser('~/Downloads/CollectTheOrbs-music-options.html')
    open(path, 'w').write(TEMPLATE.replace('__DATA__', json.dumps(rows)))
    print(path)


TEMPLATE = r'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Collect The Orbs - music options</title>
<style>
:root{--bg:#07070b;--card:#12121a;--line:#2a2a38;--text:#e8e8ef;--dim:#8d8da0;--acc:#4cc9ff}
@media (prefers-color-scheme: light){:root{--bg:#f4f4f8;--card:#fff;--line:#d6d6e0;--text:#16161d;--dim:#6a6a7c}}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:16px/1.5 system-ui,sans-serif;padding:20px 16px 60px;max-width:760px;margin:0 auto}
h1{font-size:1.5rem;margin:.2em 0}p.lead{color:var(--dim)}
.card{background:var(--card);border:1.5px solid var(--line);border-radius:14px;padding:14px;margin:14px 0}
.card.sel{border-color:var(--acc);box-shadow:0 0 0 2px color-mix(in srgb,var(--acc) 30%,transparent)}
.card h2{font-size:1.1rem;margin:0}.card small{color:var(--dim)}
.btns{display:flex;gap:8px;flex-wrap:wrap;margin-top:10px}
button{font:inherit;border:1px solid var(--line);background:transparent;color:var(--text);border-radius:8px;padding:6px 12px;cursor:pointer}
button:hover{border-color:var(--text)}button.on{border-color:var(--acc);color:var(--acc)}
.bar{height:6px;background:var(--line);border-radius:3px;margin-top:12px;overflow:hidden}.bar i{display:block;height:100%;width:0;background:var(--acc)}
</style></head><body>
<h1>Music options</h1>
<p class="lead">Bass-heavy, atmospheric electronic loops. Tap <b>Play</b> to start a loop (it repeats forever). Tap <b>Jump to the loop point</b> to hear the last 4 seconds and the restart: it should flow with no gap, click or sudden change. Volumes are matched to the current music. Tap a card's title to pick it.</p>
<div id="cards"></div>
<script>
const DATA=__DATA__;let cur=null,curId=null,pick=null;
function stop(){if(cur){cur.pause();cur=null;curId=null}}
function render(){const root=document.getElementById('cards');root.innerHTML='';
DATA.forEach(r=>{const c=document.createElement('div');c.className='card'+(pick===r.id?' sel':'');
c.innerHTML='<h2 style="cursor:pointer">'+r.name+(pick===r.id?' ✓':'')+'</h2><small>'+r.desc+' ('+Math.round(r.dur)+' s loop)</small><div class="btns"></div><div class="bar"><i></i></div>';
c.querySelector('h2').onclick=()=>{pick=r.id;try{localStorage.setItem('cto-music',pick)}catch(e){};render()};
const b=c.querySelector('.btns');
const play=document.createElement('button');play.textContent=curId===r.id?'❚❚ Stop':'▶ Play';
play.onclick=()=>{if(curId===r.id){stop();render();return}stop();const a=new Audio(r.src);a.loop=true;a.play();cur=a;curId=r.id;
a.ontimeupdate=()=>{const bar=document.querySelectorAll('.bar i')[DATA.indexOf(r)];if(bar)bar.style.width=(100*a.currentTime/a.duration)+'%'};render()};
const jump=document.createElement('button');jump.textContent='⏭ Jump to the loop point';
jump.onclick=()=>{if(curId!==r.id){stop();const a=new Audio(r.src);a.loop=true;cur=a;curId=r.id;a.ontimeupdate=()=>{const bar=document.querySelectorAll('.bar i')[DATA.indexOf(r)];if(bar)bar.style.width=(100*a.currentTime/a.duration)+'%'}}cur.currentTime=Math.max(0,r.dur-4);cur.play();render()};
b.appendChild(play);b.appendChild(jump);root.appendChild(c)})}
try{pick=localStorage.getItem('cto-music')}catch(e){}
render();
</script></body></html>'''

if __name__ == '__main__':
    main()
