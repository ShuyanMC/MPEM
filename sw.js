const CACHE_NAME = 'hidden-player-v1';
const CACHE_URLS = [
    '/',
    '/oiiaio.html',
    'https://www.bilibili.com/video/BV1btJHzGEUE'
];

// 安装时缓存资源
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(CACHE_URLS))
});

// 拦截请求
self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => response || fetch(event.request))
});

// 接收来自页面的消息
self.addEventListener('message', event => {
    if (event.data === 'restore') {
        event.waitUntil(
            clients.matchAll({type: 'window'})
                .then(windows => {
                    if (windows.length === 0) {
                        return clients.openWindow('/');
                    }
                })
        );
    }
});
