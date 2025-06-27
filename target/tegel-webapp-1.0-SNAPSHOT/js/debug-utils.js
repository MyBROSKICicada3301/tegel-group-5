// Debug utility for consistent logging and path analysis
const debug = {
    log: function (source, message, data) {
        console.log(`[${source}] ${message}`, data || '');
    },
    error: function (source, message, error) {
        console.error(`[${source}] ERROR: ${message}`, error || '');
    },
    warn: function (source, message, data) {
        console.warn(`[${source}] WARNING: ${message}`, data || '');
    },
    info: function (source, message, data) {
        console.info(`[${source}] INFO: ${message}`, data || '');
    }
};

