const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');

const root = path.join(__dirname, 'web-preview');
const server = http.createServer((req, res) => {
  const requested = req.url === '/' ? '/index.html' : req.url;
  const file = path.resolve(root, `.${requested}`);
  if (!file.startsWith(root + path.sep)) {
    res.writeHead(400, {'Content-Type': 'text/plain; charset=utf-8'});
    res.end('Bad request');
    return;
  }
  fs.readFile(file, (error, data) => {
    if (error) {
      res.writeHead(404, {'Content-Type': 'text/plain; charset=utf-8'});
      res.end('Not found');
      return;
    }
    res.writeHead(200, {'Content-Type': 'text/html; charset=utf-8', 'Cache-Control': 'no-store'});
    res.end(data);
  });
});

server.listen(8787, '127.0.0.1', () => {
  console.log('WAW preview listening on http://127.0.0.1:8787');
});
