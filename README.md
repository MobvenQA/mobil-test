# Mobil Test Otomasyon Projesi

## Özellikler
- **Java + Cucumber + TestNG + Appium** tabanlı mobil test otomasyonu
- **iOS ve Android** desteği
- **Local ve Cloud** (MomentumSuite) ortam desteği
- **Tekli ve Paralel** test koşumu
- **Port çakışması olmadan** paralel test desteği
- **Allure raporlama** ve otomatik rapor gönderimi

---

## Klasör Yapısı

- `src/main/java/com/flick/`  
  - `drivers/`: ThreadLocal Appium driver yönetimi
  - `capabilities/`: Cloud ve local için capability ayarları
  - `config/`: Ortam, cihaz, app ve port yönetimi
  - `runners/`: TestNG + Cucumber runner
- `src/test/java/stepDefinitions/`: Hooks ve test adımları
- `TestSuiteXml/`: TestNG XML dosyaları (tekli/paralel, local/cloud)
- `src/main/resources/environment.json`: Cihaz, ortam ve port konfigürasyonu

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

### S: Paralel cloud testte port çakışması olur mu?
**Hayır.** Her cihaz için farklı `gw` ve offset ile portlar atanır, çakışma yaşanmaz.

### S: Android desteği var mı?
**Evet.** Android için de local ve cloud test desteği mevcuttur.

### S: Tekli ve paralel testler arasında fark var mı?
**Evet.** Paralel testlerde her test için farklı cihaz ve port atanır, tekli testte tek cihaz kullanılır.

---

## Geliştirici Notları

- Yeni cihaz eklemek için environment.json'a cihaz bilgisi ve gw portu eklenmelidir.
- Yeni app eklemek için ilgili platform altında apps objesine ekleme yapılmalıdır.
- Cloud ortamı için MomentumSuite kullanıcı ve token bilgisi güncellenmelidir.

---

## İletişim

Herhangi bir sorun veya katkı için [proje sahibi](mailto:hakan.tektas@mobven.com) ile iletişime geçebilirsiniz. 