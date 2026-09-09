FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev --ignore-scripts
COPY server.js pg-store.js realtime.js meta-client.js ./
ENV NODE_ENV=production
USER node
EXPOSE 8788
CMD ["node", "server.js"]
