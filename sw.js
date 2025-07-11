const CACHE_NAME = 'hidden-player-v2';
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
    );
});

// 接收消息
self.addEventListener('message', event => {
    if (event.data.type === 'heartbeat') {
        // 处理心跳消息
        console.log('收到心跳:', event.data.time);
    }
});
