// Debug utility for consistent logging and path analysis
const debug = {
    log: function(source, message, data) {
        console.log(`[${source}] ${message}`, data || '');
    },
    error: function(source, message, error) {
        console.error(`[${source}] ERROR: ${message}`, error || '');
    },
    warn: function(source, message, data) {
        console.warn(`[${source}] WARNING: ${message}`, data || '');
    },
    info: function(source, message, data) {
        console.info(`[${source}] INFO: ${message}`, data || '');
    },
    // Special utility to analyze paths and URL configuration
    analyzePaths: function() {
        const analysis = {
            protocol: window.location.protocol,
            host: window.location.host,
            pathname: window.location.pathname,
            origin: window.location.origin,
            contextPath: this.detectContextPath(),
            fullPath: window.location.href
        };

        console.group("Path Analysis");
        console.log("Protocol: " + analysis.protocol);
        console.log("Host: " + analysis.host);
        console.log("Path: " + analysis.pathname);
        console.log("Origin: " + analysis.origin);
        console.log("Detected context path: " + analysis.contextPath);
        console.log("Full URL: " + analysis.fullPath);
        console.groupEnd();

        return analysis;
    },
    // Try to detect the application's context path
    detectContextPath: function() {
        // Split the path by '/' and get the first segment
        const pathSegments = window.location.pathname.split('/');
        // The context path is the first non-empty segment (index 1 typically)
        return pathSegments.length > 1 && pathSegments[1] ? '/' + pathSegments[1] : '';
    },
    // Check if a URL is accessible
    checkUrl: function(url, method = 'GET') {
        this.info("URLChecker", `Testing URL: ${url} with method: ${method}`);

        return fetch(url, { method: method, credentials: 'same-origin' })
            .then(response => {
                this.info("URLChecker", `Response from ${url}: ${response.status} ${response.statusText}`);
                // Return the response object for further processing
                return {
                    url: url,
                    status: response.status,
                    statusText: response.statusText,
                    ok: response.ok,
                    headers: Array.from(response.headers.entries())
                };
            })
            .catch(error => {
                this.error("URLChecker", `Error with ${url}:`, error);
                return {
                    url: url,
                    error: error.message,
                    ok: false
                };
            });
    },
    // Test multiple URL variations to see which one works
    testUrlVariations: function(baseUrl, paths) {
        this.info("URLTester", `Testing ${paths.length} URL variations with base: ${baseUrl}`);

        const promises = paths.map(path => {
            const fullUrl = baseUrl + path;
            return this.checkUrl(fullUrl);
        });

        Promise.all(promises)
            .then(results => {
                console.group("URL Test Results");
                results.forEach(result => {
                    if (result.ok) {
                        console.log(`✅ ${result.url} - ${result.status} ${result.statusText}`);
                    } else {
                        console.log(`❌ ${result.url} - ${result.error || `${result.status} ${result.statusText}`}`);
                    }
                });
                console.groupEnd();
            });
    }
};

