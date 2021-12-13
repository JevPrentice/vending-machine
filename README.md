# Vending machine

Basic vending machine example written in Java 17. The project was created with spring boot initializr (though springs
features are not really used) and IntelliJ and makes use of Lombok.

### Build locally:

```
    ./mvnw clean install
```

### Test locally:

```
    ./mvnw test
```

### Run locally:

```
    ./mvnw spring-boot:run
```

### Example API calls
```
curl "http://localhost:8080/product/0/quantity/5" -X POST
curl "http://localhost:8080/product/0/price/160" -X POST
curl "http://localhost:8080/coin/all/quantity/10" -X POST
curl "http://localhost:8080/" -X GET
curl "http://localhost:8080/coin" -X GET
curl "http://localhost:8080/product/0/purchase" -X POST -H "Content-Type: application/json" -d '[1.00, 1.00]'
```
