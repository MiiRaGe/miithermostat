FROM amazoncorretto:21-alpine

RUN apk add --no-cache tzdata

ENV TZ=Europe/Stockholm

WORKDIR /app

ADD ./app/build/distributions/app.tar /app

EXPOSE 8080

CMD ["./app/bin/app"]