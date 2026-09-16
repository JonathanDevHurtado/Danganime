const SoundFX = (() => {
  let ctx = null;

  function getCtx() {
    if (!ctx) {
      ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (ctx.state === 'suspended') {
      ctx.resume();
    }
    return ctx;
  }

  function playTone(freq, duration, type, volume, detune) {
    try {
      const c = getCtx();
      const osc = c.createOscillator();
      const gain = c.createGain();
      osc.connect(gain);
      gain.connect(c.destination);
      osc.type = type || 'sine';
      osc.frequency.value = freq;
      if (detune) osc.detune.value = detune;
      gain.gain.setValueAtTime(volume || 0.15, c.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration);
      osc.start(c.currentTime);
      osc.stop(c.currentTime + duration);
    } catch (e) {}
  }

  function playNoise(duration, volume) {
    try {
      const c = getCtx();
      const bufferSize = c.sampleRate * duration;
      const buffer = c.createBuffer(1, bufferSize, c.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = (Math.random() * 2 - 1) * 0.3;
      }
      const source = c.createBufferSource();
      source.buffer = buffer;
      const gain = c.createGain();
      const filter = c.createBiquadFilter();
      filter.type = 'highpass';
      filter.frequency.value = 3000;
      source.connect(filter);
      filter.connect(gain);
      gain.connect(c.destination);
      gain.gain.setValueAtTime(volume || 0.05, c.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration);
      source.start(c.currentTime);
    } catch (e) {}
  }

  let welcomeAudio = null;

  function ensureContext() {
    if (!ctx) {
      ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (ctx.state === 'suspended') {
      ctx.resume();
    }
  }

  return {
    click() {
      playTone(800, 0.08, 'sine', 0.12);
      playNoise(0.04, 0.03);
    },

    tap() {
      playTone(600, 0.06, 'sine', 0.08);
    },

    bounce() {
      playTone(400, 0.1, 'sine', 0.1);
      setTimeout(() => playTone(600, 0.08, 'sine', 0.08), 50);
    },

    open() {
      playTone(500, 0.12, 'sine', 0.1);
      setTimeout(() => playTone(700, 0.1, 'sine', 0.08), 60);
      setTimeout(() => playTone(900, 0.08, 'sine', 0.06), 120);
    },

    close() {
      playTone(700, 0.1, 'sine', 0.1);
      setTimeout(() => playTone(500, 0.08, 'sine', 0.08), 60);
    },

    back() {
      playTone(600, 0.08, 'sine', 0.1);
      setTimeout(() => playTone(400, 0.1, 'sine', 0.08), 50);
    },

    splash() {
      playTone(300, 0.3, 'sine', 0.08);
      setTimeout(() => playTone(500, 0.25, 'sine', 0.06), 150);
      setTimeout(() => playTone(700, 0.2, 'sine', 0.05), 300);
      setTimeout(() => playTone(900, 0.15, 'sine', 0.04), 450);
    },

    welcome() {
      try {
        ensureContext();
        if (!welcomeAudio) {
          welcomeAudio = new Audio('sounds/welcome.m4a');
          welcomeAudio.volume = 0.8;
        }
        welcomeAudio.currentTime = 0;
        welcomeAudio.play().catch(() => {});
      } catch (e) {}
    },

    success() {
      playTone(523, 0.15, 'sine', 0.1);
      setTimeout(() => playTone(659, 0.15, 'sine', 0.1), 100);
      setTimeout(() => playTone(784, 0.2, 'sine', 0.1), 200);
    },

    error() {
      playTone(300, 0.15, 'square', 0.08);
      setTimeout(() => playTone(200, 0.2, 'square', 0.06), 100);
    },

    init() {
      getCtx();
    }
  };
})();
