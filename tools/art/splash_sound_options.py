"""Alternative neon sound sets for the splash. Each set: hum (loops), zap, strike, dip as wav, plus demo_<set>.wav that plays them in the order the splash does."""
import numpy as np, os, wave
SR = 44100
OUT = os.path.join(os.path.dirname(__file__), 'splash_sound_options')
r = np.random.default_rng(9)

def lp(x, k): return np.convolve(x, np.ones(k) / k, mode='same')
def hp(x, k): return x - lp(x, k)
def t_(n): return np.arange(n) / SR
def env(n, a, d): t = t_(n); return np.minimum(t / a, 1) * np.exp(-t / d)
def norm(x, p=0.8): return x / (np.abs(x).max() + 1e-9) * p
def loopfade(x, ms=10):
    f = int(ms / 1000 * SR); x = x.copy(); x[:f] *= np.linspace(0, 1, f); x[-f:] *= np.linspace(1, 0, f); return x
def noise(n): return r.normal(0, 1, n)
def clicks(n, rate, tail, dur):
    z = np.zeros(n)
    for i in np.nonzero(r.random(n) < rate / SR)[0]:
        m = min(n - i, int(dur * SR)); z[i:i + m] += r.uniform(.3, 1) * r.choice([-1, 1]) * noise(m) * np.exp(-np.arange(m) / (tail * SR))
    return z

def set_A():  # transformer: deep, warm, mechanical
    n = 2 * SR; t = t_(n)
    hum = sum(a * np.sin(2 * np.pi * 50 * k * t + k * .7) for k, a in ((1, 1), (2, .7), (3, .35), (4, .3), (6, .12)))
    hum = norm(hum) + .12 * norm(lp(noise(n), 40)) * (.6 + .4 * np.sin(2 * np.pi * 100 * t))
    n2 = int(.25 * SR)
    zap = norm(lp(noise(n2), 6) * env(n2, .001, .02) + .6 * np.sin(2 * np.pi * 70 * t_(n2)) * env(n2, .002, .06))
    n3 = int(.9 * SR); t3 = t_(n3)
    clack = lp(noise(n3), 3) * env(n3, .0005, .012) + .7 * lp(noise(n3), 18) * env(n3, .0005, .035)
    swell = np.sin(2 * np.pi * 50 * t3 + 2 * np.pi * 25 * t3 ** 2) * (1 - np.exp(-t3 * 6)) * np.exp(-t3 / .6)
    strike = norm(clack * 1.2 + .9 * swell)
    n4 = int(.6 * SR); t4 = t_(n4)
    dip = norm(np.sin(2 * np.pi * np.cumsum(90 * np.exp(-t4 * 4) + 28) / SR) * env(n4, .004, .25) + .5 * lp(noise(n4), 25) * env(n4, .002, .12))
    return hum, zap, strike, dip

def set_B():  # ballast sizzle: bright, frying, electric
    n = 2 * SR; t = t_(n)
    whine = np.sin(2 * np.pi * 8000 * t + 3 * np.sin(2 * np.pi * 3 * t)) * (.5 + .5 * np.sin(2 * np.pi * 2 * t))
    fry = clicks(n, 900, .0007, .004) ; fry = fry / np.abs(fry).max()
    hiss = hp(noise(n), 8); hiss /= np.abs(hiss).max()
    hum = .18 * whine + .55 * fry + .25 * hiss * (.6 + .4 * np.sin(2 * np.pi * 1 * t)) + .25 * np.sin(2 * np.pi * 120 * t)
    n2 = int(.2 * SR); t2 = t_(n2)
    zap = norm(hp(noise(n2), 5) * env(n2, .0005, .03) + .5 * np.sin(2 * np.pi * 2400 * t2 * (1 - 2 * t2)) * env(n2, .001, .04) + clicks(n2, 60, .002, .01))
    n3 = int(.8 * SR); t3 = t_(n3)
    sizzle = hp(noise(n3), 6) * (1 - np.exp(-t3 * 7)) * np.exp(-t3 / .45)
    strike = norm(clicks(n3, 80, .0015, .008) * env(n3, .0001, .35) + .8 * sizzle + .5 * np.sin(2 * np.pi * (3000 + 4000 * t3) * t3) * env(n3, .01, .12))
    n4 = int(.55 * SR); t4 = t_(n4)
    dip = norm(np.sin(2 * np.pi * np.cumsum(6000 * np.exp(-t4 * 7) + 200) / SR) * env(n4, .003, .2) + .4 * hp(noise(n4), 6) * env(n4, .002, .08))
    return hum, zap, strike, dip

def set_C():  # soft synth: musical, glassy, retro
    n = 2 * SR; t = t_(n)
    hum = np.sin(2 * np.pi * 55 * t) + .5 * np.sin(2 * np.pi * 82.5 * t) + .25 * np.sin(2 * np.pi * 110 * t)
    hum = norm(hum) * (.8 + .2 * np.sin(2 * np.pi * 1 * t)) + .06 * norm(hp(lp(noise(n), 4), 40))
    n2 = int(.18 * SR); t2 = t_(n2); f = r.choice([1320, 1760, 1568])
    zap = norm(np.sin(2 * np.pi * f * t2 * (1 + .5 * np.exp(-t2 * 40))) * env(n2, .001, .03) + .4 * np.sin(2 * np.pi * f * 2 * t2) * env(n2, .001, .015))
    n3 = int(1.0 * SR); t3 = t_(n3); strike = np.zeros(n3)
    for k, fr in enumerate((220, 330, 440, 660, 880)):
        s = int(k * .07 * SR); tt = t_(n3 - s)
        strike[s:] += (np.sin(2 * np.pi * fr * tt) + .3 * np.sin(2 * np.pi * fr * 2 * tt)) * env(n3 - s, .002, .22) * (.5 + .1 * k)
    strike = norm(strike + .15 * hp(noise(n3), 8) * env(n3, .0005, .02))
    n4 = int(.6 * SR); t4 = t_(n4)
    dip = norm((np.sin(2 * np.pi * np.cumsum(660 * np.exp(-t4 * 5) + 80) / SR) + .3 * np.sin(2 * np.pi * np.cumsum(990 * np.exp(-t4 * 5) + 120) / SR)) * env(n4, .003, .3))
    return hum, zap, strike, dip

def save(path, x):
    with wave.open(path, 'wb') as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes((np.clip(x, -1, 1) * 32767).astype(np.int16).tobytes())

def demo(hum, zap, strike, dip):
    n = 12 * SR; m = np.zeros(n)
    def put(x, at, v):
        i = int(at * SR); k = min(len(x), n - i); m[i:i + k] += x[:k] * v
    for i in range(6): put(strike if i == 0 else zap, .3 + .25 * i + (0 if i else 0), 1 if i == 0 else .35)
    rep = np.tile(hum, 6)[:n]
    ramp = np.clip((t_(n) - .3) / 1.5, 0, 1) * 0.3
    m += rep * ramp * (1 - .7 * ((t_(n) > 8) & (t_(n) < 8.35)))
    for at in (3.2, 3.32, 3.43, 3.6, 3.7): put(zap, at, .5)
    for at in (6.0, 6.1, 6.2, 6.3): put(zap, at, .5)
    put(dip, 8, .8)
    for at in (10.0, 10.1, 10.2, 10.35): put(zap, at, .5)
    return m

if __name__ == '__main__':
    for name, fn in (('A_transformer', set_A), ('B_sizzle', set_B), ('C_synth', set_C)):
        d = os.path.join(OUT, name); os.makedirs(d, exist_ok=True)
        hum, zap, strike, dip = fn()
        hum = loopfade(hum)
        for nm, x in (('neon_hum', hum), ('neon_zap', zap), ('neon_strike', strike), ('neon_dip', dip)):
            save(os.path.join(d, nm + '.wav'), norm(x, .8 if nm != 'neon_hum' else .5))
        save(os.path.join(OUT, 'demo_' + name + '.wav'), norm(demo(norm(hum, .5), norm(zap, .8), norm(strike, .8), norm(dip, .8)), .85))
        print(name)
