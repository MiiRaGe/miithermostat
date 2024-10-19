FROM node:hydrogen-alpine
RUN apk add --no-cache tzdata

ENV TZ=Europe/Stockholm

SHELL ["/bin/sh", "-c"]

COPY ./sensor-graph/.output/ /app/

WORKDIR /app

EXPOSE 3000

CMD ["node", "/app/server/index.mjs"]
