// src/utils/remoteLogger.js


function sendLogToBackend(level, message) {
  try {
    const payload = {
      level,
      message,
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent,
    };

    const blob = new Blob([JSON.stringify(payload)], {
      type: 'application/json',
    });

    navigator.sendBeacon(`/log`, blob);
  } catch (err) {
    console.warn('Failed to send log to backend', err);
  }
}

export const remoteLogger = {
  error: (msg) => sendLogToBackend('ERROR', msg),
  warn: (msg) => sendLogToBackend('WARN', msg),
  info: (msg) => sendLogToBackend('INFO', msg),
  debug: (msg) => sendLogToBackend('DEBUG', msg),
};
