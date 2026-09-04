export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === '/health') return Response.json({ ok: true, service: 'waw-chat' });
    if (url.pathname === '/ws') {
      if (request.headers.get('Upgrade') !== 'websocket') return new Response('WebSocket required', { status: 426 });
      const pair = new WebSocketPair();
      const [client, server] = Object.values(pair);
      server.accept();
      server.addEventListener('message', event => {
        try {
          const message = JSON.parse(event.data);
          if (!message.type || !message.conversationId) return;
          server.send(JSON.stringify({ type: 'message', ...message, status: 'SENT' }));
        } catch (_) {}
      });
      return new Response(null, { status: 101, webSocket: client });
    }
    return new Response('WAW Chat Server');
  }
};
