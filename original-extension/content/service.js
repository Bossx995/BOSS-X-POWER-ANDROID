(() => {
  const EXT = globalThis.browser ?? globalThis.chrome;
  if (!EXT?.runtime || !EXT?.storage?.local) return;

  const HAS_PROMISE_API = typeof globalThis.browser !== 'undefined' && EXT === globalThis.browser;
  const DEFAULTS = {
    profileVersion: 11,
    enabled: true,
    noiseGateEnabled: true,
    noiseGateThresholdDb: -48,
    gainDb: 18,
    thresholdDb: -28,
    knee: 40,
    ratio: 6,
    attack: 0.0001,
    release: 0.03,
    lowShelfDb: 1,
    presenceDb: 5,
    highShelfDb: 3,
    limiterDb: -1.5,
    drive: 0.15,
    loudness: 1.5,
    maxBoost: 4,
    sustain: true,
    sustainTargetDb: 5,
    sustainMaxGain: 4,
    forceRawMic: false,
    reverbEnabled: false,
    reverbDelay: 0.045,
    reverbFeedback: 0.35,
    reverbWet: 0.18,
    keepAlive: false,
    keepAliveGain: 0.0012,
    senderRefreshMs: 250
  };
  const MSG_CFG = 'MIC_MAXIMIZER_CONFIG';
  let hookReady = false;

  function storageGet(key) {
    if (HAS_PROMISE_API) return EXT.storage.local.get(key);
    return new Promise((resolve) => {
      try {
        EXT.storage.local.get(key, (res) => {
          if (EXT.runtime?.lastError) resolve({});
          else resolve(res || {});
        });
      } catch (_) {
        resolve({});
      }
    });
  }

  function sendMessage(message) {
    if (HAS_PROMISE_API) return EXT.runtime.sendMessage(message);
    return new Promise((resolve) => {
      try {
        EXT.runtime.sendMessage(message, () => resolve(!EXT.runtime?.lastError));
      } catch (_) {
        resolve(false);
      }
    });
  }

  function pushConfig(config) {
    window.postMessage({ type: MSG_CFG, payload: config }, '*');
  }

  async function loadConfig() {
    try {
      const res = await storageGet('micMaximizerConfig');
      const stored = res.micMaximizerConfig || {};
      if (stored.profileVersion !== DEFAULTS.profileVersion) return { ...DEFAULTS };
      return { ...DEFAULTS, ...stored };
    } catch (_) {
      return { ...DEFAULTS };
    }
  }

  async function sync() {
    pushConfig(await loadConfig());
  }

  function heartbeat() {
    if (!hookReady) return;
    sendMessage({ type: 'MICMAX_HEARTBEAT' }).catch(() => {});
  }

  window.addEventListener('message', (event) => {
    if (event.source === window && event.data?.type === 'MIC_MAXIMIZER_READY') {
      hookReady = true;
      sync();
      heartbeat();
    }
  });

  EXT.storage.onChanged.addListener((changes, area) => {
    if (area === 'local' && changes.micMaximizerConfig) {
      sync();
    }
  });

  // Periodic sync and heartbeat
  setInterval(() => {
    if (hookReady) {
      sync();
      heartbeat();
    }
  }, 8000);

  // Initial sync attempt
  setTimeout(sync, 1500);

  console.log('[WhatsApp Loud Mic Pro] content service loaded');
})();
