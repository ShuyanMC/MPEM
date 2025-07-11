const CACHE_NAME = 'hidden-player-v1';
const CACHE_URLS = [
    '/',
    'https://www.bilibili.com/video/BV1btJHzGEUE'
];

// 安装Service Worker
self.addEventListener('install', function(event) {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(function(cache) {
                return cache.addAll(CACHE_URLS);
            })
    );
});

// 拦截请求
self.addEventListener('fetch', function(event) {
    event.respondWith(
        caches.match(event.request)
            .then(function(response) {
                return response || fetch(event.request);
            })
    );
});
