# Movies API Performance Tests

Bu dizin Movies API'si için kapsamlı performans testlerini içerir.

## 🧪 Test Türleri

### 1. JMH (Java Microbenchmark Harness) Testleri
- **Dosyalar:** `MovieServicePerformanceTest.java`, `DatabasePerformanceTest.java`
- **Amaç:** Servis metodlarının mikro-benchmark'ları
- **Ölçümler:** Ortalama yanıt süresi, throughput

### 2. JUnit Performance Testleri
- **Dosyalar:** `ApiPerformanceTest.java`, `LoadTest.java`
- **Amaç:** HTTP endpoint'lerinin performans testleri
- **Ölçümler:** Yanıt süresi, eşzamanlı kullanıcı limitleri, bellek kullanımı

### 3. JMeter Load Testleri
- **Dosya:** `movies-performance-test.jmx`
- **Amaç:** Gerçek yük testleri ve web performans analizi
- **Ölçümler:** Concurrent users, response time, throughput

## 🚀 Testleri Çalıştırma

### Tüm Performans Testleri (Önerilen)
```bash
# Linux/macOS
./src/test/performance/run-performance-tests.sh

# Windows
src\test\performance\run-performance-tests.bat
```

### Ayrı Test Türleri

#### JMH Benchmarks
```bash
mvn clean compile test-compile
mvn exec:java -Dexec.mainClass="dev.hakan.movies.performance.MovieServicePerformanceTest"
```

#### JUnit Performance Tests
```bash
mvn test -Dtest="*PerformanceTest"
mvn test -Dtest="LoadTest"
```

#### JMeter Tests
```bash
# Uygulamayı başlat
mvn spring-boot:run

# Başka terminal'de JMeter testini çalıştır
jmeter -n -t src/test/jmeter/movies-performance-test.jmx -l results.jtl -e -o html-report
```

## 📊 Performans Kriterleri

### Yanıt Süreleri
- **GET /movies**: < 500ms
- **POST /auth/register**: < 1000ms
- **POST /auth/login**: < 800ms
- **POST /movies/searchAndSort**: < 2000ms

### Throughput
- **GET /movies**: > 200 req/sec
- **Auth endpoints**: > 100 req/sec
- **Search endpoints**: > 50 req/sec

### Kaynak Kullanımı
- **Bellek**: < 512MB (yük altında)
- **CPU**: < 80% (yük altında)
- **Hata Oranı**: < 1%

### Eşzamanlılık
- **Concurrent Users**: 100+ kullanıcı desteklenmeli
- **Connection Pool**: Stabil connection pool yönetimi

## 🔧 Test Konfigürasyonu

### Test Ortamı
- **Test Database**: `movies_test`
- **Test Port**: Random port (Spring Boot Test)
- **JMeter Threads**: 50 concurrent users (default)

### JVM Ayarları
```bash
-Xms2G -Xmx2G -XX:+UseG1GC
```

## 📈 Sonuçları Analiz Etme

### JMH Sonuçları
```json
{
  "benchmark": "findAllMovies",
  "mode": "avgt",
  "score": 245.67,
  "scoreUnit": "ms/op"
}
```

### JMeter HTML Raporu
- `jmeter-html-report/index.html` dosyasını tarayıcıda açın
- Response time grafikleri, throughput metrikleri
- Error rate analizi

### JUnit Test Sonuçları
Console output'da şu bilgileri kontrol edin:
- Average response time
- Success rate
- Memory usage
- Concurrent user limits

## 🐛 Sorun Giderme

### Yaygın Sorunlar

1. **OutOfMemoryError**
   ```bash
   export MAVEN_OPTS="-Xmx4G"
   mvn test
   ```

2. **JMeter bağlanamıyor**
   - Uygulamanın çalıştığından emin olun
   - Port'un doğru olduğunu kontrol edin

3. **TestTimeoutException**
   - Test timeout değerlerini artırın
   - Sistem kaynaklarını kontrol edin

### Debug Modu
```bash
mvn test -Dtest="*PerformanceTest" -Ddebug=true
```

## 📋 Test Senaryoları

### 1. Temel Yük Testi
- 50 concurrent user
- 10 iterasyon per user
- GET /movies endpoint

### 2. Auth Load Test
- 20 concurrent user
- Register/Login cycle
- Error handling test

### 3. Stress Test
- Artan yük seviyeleri (10, 25, 50, 100 users)
- Sistem limitlerini bulma
- Memory leak tespiti

### 4. Database Performance
- Complex search queries
- Bulk update operations
- Index performance

## 🎯 İyileştirme Önerileri

Test sonuçlarına göre:

1. **Yüksek Response Time**
   - Database index'leri ekleyin
   - Query optimizasyonu yapın
   - Caching implementasyonu

2. **Düşük Throughput**
   - Connection pool ayarları
   - Async processing
   - Database connection optimization

3. **Memory Issues**
   - Garbage collection tuning
   - Object pooling
   - Memory leak fixes

## 📚 Referanslar

- [JMH Documentation](https://github.com/openjdk/jmh)
- [JMeter User Manual](https://jmeter.apache.org/usermanual/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)

