# 🎬 Movies Application

Modern, full-stack bir film takip ve değerlendirme uygulaması. React ve Spring Boot kullanılarak geliştirilmiş, MongoDB veritabanı ile desteklenen web uygulaması.

## 🚀 Özellikler

### 🎯 Ana Özellikler
- **Film Kataloğu**: Geniş film koleksiyonu ile arama ve filtreleme
- **Trailer Oynatma**: Netflix benzeri arayüz ile YouTube trailer entegrasyonu
- **Kullanıcı Sistemi**: Kayıt, giriş yapma, Google OAuth ve profil yönetimi
- **Değerlendirme Sistemi**: Detaylı film yorumları ve puanlama
- **İzleme Listesi**: Kişisel film listesi oluşturma ve yönetme
- **Responsive Tasarım**: Tüm cihazlarda uyumlu modern arayüz

### 🛠️ Teknik Özellikler
- **Frontend**: React 18, Material-UI, Bootstrap, React Router
- **Backend**: Spring Boot 3.2.3, Spring Security, Spring Data MongoDB
- **Veritabanı**: MongoDB (Atlas & Local)
- **Test**: Unit, Integration ve Performance Testleri (JMH, JMeter)
- **Containerization**: Docker & Docker Compose
- **CI/CD**: Maven, npm scripts

## 🏗️ Proje Mimarisi

```
movies/
├── src/
│   ├── main/
│   │   ├── java/                 # Spring Boot Backend
│   │   │   └── dev/hakan/movies/
│   │   │       ├── api/          # REST Controllers
│   │   │       ├── business/     # Service Layer
│   │   │       ├── data/         # Models, DTOs, Repositories
│   │   │       ├── config/       # Security & Configuration
│   │   │       └── exception/    # Error Handling
│   │   ├── client/               # React Frontend
│   │   │   └── src/
│   │   │       ├── components/   # React Components
│   │   │       ├── api/          # API Configuration
│   │   │       └── App.js        # Main App Component
│   │   └── resources/            # Application Properties
│   └── test/                     # Test Suite
│       ├── java/                 # Backend Tests
│       ├── performance/          # Performance Tests
│       └── jmeter/               # Load Tests
├── docker-compose.yml            # Container Orchestration
├── Dockerfile.backend            # Backend Container
├── Dockerfile.frontend           # Frontend Container
└── README.md                     # This file
```

## 🚀 Hızlı Başlangıç

### Docker ile (Önerilen)

1. **Repository'yi klonlayın**
   ```bash
   git clone <repository-url>
   cd movies
   ```

2. **Environment dosyasını oluşturun**
   ```bash
   cp env.example .env
   ```
   `.env` dosyasını MongoDB ve Google OAuth ayarlarınızla düzenleyin:
   ```env
   MONGO_DATABASE=moviesdb
   MONGO_USER=movieuser
   MONGO_PASSWORD=moviepass
   MONGO_CLUSTER=localhost
   GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   REACT_APP_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
   ```

3. **Uygulamayı başlatın**
   ```bash
   docker-compose up --build
   ```

4. **Uygulamaya erişin**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api/v1
   - MongoDB: localhost:27017

### Manuel Kurulum

#### Gereksinimler
- Java 17+
- Node.js 16+
- MongoDB (Local veya Atlas)
- Maven 3.6+

#### Backend Kurulumu
```bash
# Backend dependencies
mvn clean install

# Uygulamayı çalıştır
mvn spring-boot:run
```

#### Frontend Kurulumu
```bash
cd src/main/client

# Dependencies yükle
npm install

# Development server başlat
npm start
```

## 📚 API Dokümantasyonu

### 🔐 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Kullanıcı kaydı |
| POST | `/auth/login` | Kullanıcı girişi |
| POST | `/auth/google` | Google ile giriş |

**Register Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Login Request:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Google Login Request:**
```json
{
  "idToken": "google-id-token-from-client"
}
```

### 🎬 Movie Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/movies` | Tüm filmleri listele |
| POST | `/movies/searchAndSort` | Film arama ve filtreleme |
| PUT | `/movies/update/{imdbId}` | Film bilgilerini güncelle |

**Search Request:**
```json
{
  "movie": {
    "title": "Inception",
    "releaseDate": "2010",
    "genres": ["Action", "Sci-Fi"]
  }
}
```

### ⭐ Review Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reviews` | Yeni yorum oluştur |
| POST | `/reviews/detailed` | Detaylı yorum oluştur |
| PUT | `/reviews/{id}` | Yorumu güncelle |
| DELETE | `/reviews/{id}` | Yorumu sil |

### 📝 Watchlist Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/watchlist/{userId}` | Kullanıcı izleme listesi |
| POST | `/watchlist/add` | Filmi listeye ekle |
| DELETE | `/watchlist/remove` | Filmi listeden çıkar |

## 🧪 Test

### Unit & Integration Tests
```bash
# Tüm testleri çalıştır
mvn test

# Specific test sınıfı
mvn test -Dtest=MovieServiceTest
```

### Performance Tests
```bash
# Windows
src\test\performance\run-performance-tests.bat

# Linux/Mac
src/test/performance/run-performance-tests.sh
```

**Performance Test Türleri:**
- **JMH Benchmarks**: Micro-benchmark testleri
- **Load Tests**: Eşzamanlı kullanıcı testleri
- **API Tests**: Endpoint performans testleri
- **Database Tests**: Veritabanı performans testleri

## 🔧 Google OAuth Kurulumu

Google OAuth entegrasyonu için aşağıdaki adımları takip edin:

### 1. Google Cloud Console Ayarları

1. [Google Cloud Console](https://console.cloud.google.com/)'a gidin
2. Yeni proje oluşturun veya mevcut projeyi seçin
3. **APIs & Services > Credentials** bölümüne gidin
4. **Create Credentials > OAuth 2.0 Client IDs** seçin
5. Application type olarak **Web application** seçin
6. **Authorized JavaScript origins** kısmına ekleyin:
   - `http://localhost:3000` (development)
   - `https://yourdomain.com` (production)
7. **Authorized redirect URIs** kısmına ekleyin:
   - `http://localhost:3000` (development)
8. Client ID ve Client Secret'i kopyalayın

### 2. Environment Variables Ayarlama

`.env` dosyanızı aşağıdaki gibi düzenleyin:

```env
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
REACT_APP_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
```

### 3. Test Etme

1. Uygulamayı başlatın: `docker-compose up --build`
2. http://localhost:3000/login adresine gidin
3. "Google ile Giriş Yap" butonuna tıklayın
4. Google hesabınızla giriş yapın

## 🐳 Docker & Deployment

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGO_DATABASE` | moviesdb | MongoDB veritabanı adı |
| `MONGO_USER` | movieuser | MongoDB kullanıcı adı |
| `MONGO_PASSWORD` | moviepass | MongoDB şifresi |
| `MONGO_CLUSTER` | localhost | MongoDB cluster URL |
| `GOOGLE_CLIENT_ID` | - | Google OAuth Client ID |
| `GOOGLE_CLIENT_SECRET` | - | Google OAuth Client Secret |
| `REACT_APP_GOOGLE_CLIENT_ID` | - | Frontend Google Client ID |

### Docker Commands
```bash
# Servisleri başlat
docker-compose up -d

# Logları görüntüle
docker-compose logs -f [service-name]

# Servisleri durdur
docker-compose down

# Volume'ları da temizle
docker-compose down -v
```

## 🔧 Geliştirme

### Code Structure

**Backend (Spring Boot):**
- `@RestController` sınıfları API endpoint'lerini tanımlar
- `@Service` sınıfları iş mantığını içerir
- `@Repository` interface'leri veri erişim katmanını sağlar
- `@Entity` sınıfları MongoDB document'larını model'ler

**Frontend (React):**
- `components/` klasörü UI bileşenlerini içerir
- `api/` klasörü backend iletişimini yönetir
- `App.js` ana uygulama bileşenidir

### Database Schema

**Movies Collection:**
```json
{
  "_id": "ObjectId",
  "imdbId": "tt1234567",
  "title": "Movie Title",
  "releaseDate": "2023",
  "trailerLink": "https://youtube.com/watch?v=...",
  "poster": "poster_url",
  "backdrop": "backdrop_url",
  "genres": ["Action", "Drama"],
  "imdbRating": "8.5",
  "description": "Movie description...",
  "director": "Director Name",
  "cast": ["Actor1", "Actor2"],
  "duration": "120 min",
  "ageRating": "PG-13"
}
```

**Users Collection:**
```json
{
  "_id": "ObjectId",
  "name": "User Name",
  "email": "user@example.com",
  "password": "hashed_password",
  "createdAt": "2023-01-01T00:00:00Z"
}
```

**Reviews Collection:**
```json
{
  "_id": "ObjectId",
  "title": "Review Title",
  "body": "Review content...",
  "rating": 4.5,
  "movieId": "movie_imdb_id",
  "username": "reviewer_name",
  "createdAt": "2023-01-01T00:00:00Z",
  "tags": ["recommended"],
  "isRecommended": true
}
```

## 🤝 Katkıda Bulunma

1. Repository'yi fork edin
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📋 TODO

- [ ] Film verilerini harici API'den çekme
- [ ] Kullanıcı profil sayfası
- [ ] Sosyal özellikler (arkadaşlık sistemi)
- [ ] Mobil uygulama geliştirme
- [ ] Redis cache entegrasyonu
- [ ] WebSocket ile gerçek zamanlı bildirimler

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

## 👥 Geliştirici

**Hakan Özdemir**
- GitHub: [@ozdemirhakan346](https://github.com/ozdemirhakan346)

## 🆘 Destek

Herhangi bir sorun yaşarsanız:
1. `Issues` bölümünde arama yapın
2. Yeni issue oluşturun
3. Detaylı açıklama ve hata logları ekleyin

---

⭐ **Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!**
