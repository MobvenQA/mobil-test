# Mobil Test Otomasyon Projesi

## Özellikler
- **Java 11 + Cucumber + TestNG + Appium** tabanlı mobil test otomasyonu
- **iOS ve Android** desteği
- **Local ve Cloud** (MomentumSuite) ortam desteği
- **Tekli ve Paralel** test koşumu
- **Port çakışması olmadan** paralel test desteği
- **Allure raporlama** ve otomatik rapor gönderimi
- **Docker** entegrasyonu ile Allure raporlama
- **Proje bazlı** test yönetimi
- **Hooks ve Session Yönetimi** - Otomatik driver init ve scenario hooks

---

## Klasör Yapısı

```
mobil-test/
├── projects/                    # Proje bazlı test yönetimi
│   ├── default/                # Varsayılan proje
│   └── madduck/               # Madduck projesi
├── src/main/java/com/flick/
│   ├── drivers/               # ThreadLocal Appium driver yönetimi
│   ├── capabilities/          # Cloud ve local için capability ayarları
│   ├── config/               # Ortam, cihaz, app ve port yönetimi
│   ├── pages/                # Page Object Model
│   ├── runners/              # TestNG + Cucumber runner
│   └── utils/                # Yardımcı sınıflar
├── src/test/java/
│   ├── stepDefinitions/      # Hooks ve test adımları
│   └── features/            # Cucumber feature dosyaları
├── TestSuiteXml/            # TestNG XML dosyaları
│   └── iOS/
│       ├── Local/           # Local test konfigürasyonları
│       └── Cloud/           # Cloud test konfigürasyonları
├── docker-compose.yml       # Docker Allure servisleri
└── send_result.sh          # Otomatik rapor gönderimi
```

---

## Kurulum ve Başlangıç

### 1. Gereksinimler
- Java 11+
- Maven 3.6+
- Appium Server
- iOS Simulator/Device (iOS testleri için)
- Android Emulator/Device (Android testleri için)

### 2. Konfigürasyon
1. `src/main/resources/enviroment.example.json` dosyasını `environment.json` olarak kopyalayın
2. Cihaz bilgilerinizi ve MomentumSuite token'ınızı güncelleyin
3. Docker Compose ile Allure servislerini başlatın:

```bash
docker-compose up -d
```

---

## Cucumber Feature Dosyaları ve Toplu/Paralel Koşum

- Tüm `.feature` dosyalarınızı `src/test/java/features/` klasörüne ekleyebilirsiniz.
- Varsayılan olarak, TestRunner ve TestNG XML dosyaları bu klasördeki tüm feature dosyalarını bulup çalıştırır.
- Birden fazla feature dosyasını **aynı anda** (paralel veya topluca) koşturmak için:
  - **Tekli Koşum:** Tek bir TestNG XML dosyası ile tüm feature dosyaları sırayla çalışır.
  - **Paralel Koşum:** `testngLocalParallel.xml` veya `testngCloudParallel.xml` gibi paralel yapılandırılmış XML dosyalarını kullanın. Her test bloğu (her cihaz/thread) aynı anda farklı feature dosyalarını çalıştırabilir.
- **Not:** Paralel koşumda, Cucumber senaryoları thread-safe olduğu ve driver/context yönetimi ThreadLocal ile yapıldığı için çakışma yaşanmaz.

### Örnek: Tüm Feature Dosyalarını Paralel Koşmak

```bash
mvn clean test -DsuiteXmlFile=TestSuiteXml/iOS/Cloud/testngCloudParallel.xml
```

- Bu komut, ilgili XML’de tanımlı her cihaz/thread için tüm feature dosyalarını paralel olarak çalıştırır.
- Eğer belirli feature dosyalarını çalıştırmak isterseniz, TestRunner veya XML’de `features` parametresini özelleştirebilirsiniz.

### Feature Dosyası Ekleme

1. Yeni bir `.feature` dosyasını `src/test/java/features/` klasörüne ekleyin.
2. Adım tanımlarını (`stepDefinitions`) yazın.
3. TestNG XML ile çalıştırın – ek bir ayar yapmanıza gerek yoktur, tüm feature dosyaları otomatik olarak dahil edilir.

### Hooks ve Session Yönetimi

Proje artık otomatik session yönetimi ile gelir:
- **SuiteHooks**: Her test suite başında driver init ve environment setup
- **Hooks**: Her scenario için before/after işlemleri
- **Otomatik Driver Init**: Driver null ise otomatik başlatma
- **Screenshot ve Logging**: Her adım sonrası otomatik screenshot

### IntelliJ'de Feature Dosyası Çalıştırma

Feature dosyalarını IntelliJ'de doğrudan çalıştırabilirsiniz:
1. Feature dosyasına sağ tık → "Run 'Feature:01_testFlight'"
2. Veya `FeatureTestRunner` sınıfını çalıştırın

---

## Çalıştırma Senaryoları

### 1. Local Tekli Test
```bash
mvn clean test -DsuiteXmlFile=TestSuiteXml/iOS/Local/testngLocal.xml
```

### 2. Local Paralel Test
```bash
mvn clean test -DsuiteXmlFile=TestSuiteXml/iOS/Local/testngLocalParallel.xml
```

### 3. Cloud Tekli Test
```bash
mvn clean test -DsuiteXmlFile=TestSuiteXml/iOS/Cloud/testngCloud.xml
```

### 4. Cloud Paralel Test
```bash
mvn clean test -DsuiteXmlFile=TestSuiteXml/iOS/Cloud/testngCloudParallel.xml
```

> Android için de benzer şekilde ilgili XML dosyaları kullanılabilir.

---

## Paralel Test ve Port Yönetimi

- Her paralel test thread'i için farklı cihaz (`deviceIndex`) ve port atanır.
- Portlar, environment.json'daki `gw` değeri ve offset ile hesaplanır:
  - `wdaLocalPort = gw + 4000 + 41`
  - `webkitDebugProxyPort = gw + 4000 + 42`
- Cloud ortamında MomentumSuite ile cihaz ve port yönetimi otomatik yapılır.
- **Port çakışması yaşanmaz.**

---

## Cloud Entegrasyonu

- Cloud ortamı için environment.json'da MomentumSuite kullanıcı ve token bilgisi girilir.
- Cloud testlerinde cihazlar ve portlar otomatik atanır.
- Cloud capabilities içinde MomentumSuite opsiyonları (`momentum:options`) set edilir.

---

## Raporlama

- Testler sonunda Allure raporu otomatik oluşturulur ve `send_result.sh` ile gönderilir.
- Raporlar `allure-results/` ve `target/` klasörlerinde tutulur.

---

## Sıkça Sorulanlar

### S: Feature dosyalarımı çalıştırdığımda session başlatmıyor, hooks çalışmıyor?
**Çözüldü!** Artık `glue = {"stepDefinitions", "hooks"}` ile hooks otomatik çalışır ve session başlatılır.

### S: Paralel cloud testte port çakışması olur mu?
**Hayır.** Her cihaz için farklı `gw` ve offset ile portlar atanır, çakışma yaşanmaz.

### S: Android desteği var mı?
**Evet.** Android için de local ve cloud test desteği mevcuttur.

### S: Tekli ve paralel testler arasında fark var mı?
**Evet.** Paralel testlerde her test için farklı cihaz ve port atanır, tekli testte tek cihaz kullanılır.

### S: IntelliJ'de feature dosyasını doğrudan çalıştırabilir miyim?
**Evet.** Feature dosyasına sağ tık → "Run" ile doğrudan çalıştırabilirsiniz.

---

## Geliştirici Notları

- Yeni cihaz eklemek için environment.json'a cihaz bilgisi ve gw portu eklenmelidir.
- Yeni app eklemek için ilgili platform altında apps objesine ekleme yapılmalıdır.
- Cloud ortamı için MomentumSuite kullanıcı ve token bilgisi güncellenmelidir.
- Hooks eklemek için `src/test/java/hooks/` klasörüne yeni hook sınıfları eklenebilir.
- Feature dosyaları `src/test/java/features/` klasörüne eklenmelidir.
- TestNG XML dosyaları `TestSuiteXml/` klasöründe organize edilmiştir.

## Son Güncellemeler

### v1.1 - Hooks ve Session Yönetimi Düzeltmesi
- ✅ Hooks çalışmama sorunu çözüldü
- ✅ Otomatik session başlatma eklendi
- ✅ Driver init güvenlik kontrolleri eklendi
- ✅ IntelliJ'de feature dosyası çalıştırma desteği
- ✅ Cucumber properties düzeltmesi
- ✅ TestNG XML dosyaları güncellendi

---

## İletişim

Herhangi bir sorun veya katkı için [proje sahibi](mailto:hakan.tektas@mobven.com) ile iletişime geçebilirsiniz. 