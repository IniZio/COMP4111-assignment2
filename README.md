1. native way:
```sh
mvn exec:java
```

2. Docker way:
```
docker build . --tag=library-management
docker run -p 8080:8080 -t -i library-management
```
