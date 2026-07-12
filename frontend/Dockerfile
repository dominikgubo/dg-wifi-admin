FROM node:22-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
# npm's lockfile includes platform-specific optional Rollup packages. npm 11
# can reject that lockfile on Alpine/Linux even though the package manifest is
# valid, so use the lockfile-aware installer for a portable image build.
RUN npm install --no-audit --no-fund
COPY . .

ARG VITE_API_BASE_URL=http://localhost:8081
ARG VITE_AUTH_ENABLED=false
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_AUTH_ENABLED=${VITE_AUTH_ENABLED}

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
