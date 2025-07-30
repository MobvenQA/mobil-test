#!/bin/bash

ALLURE_RESULTS_DIR="allure-results"
PROJECT_ID="madduck"
ALLURE_API="http://localhost:8181"
ALLURE_UI="http://localhost:8282"
TARGET_RESULTS_DIR="projects/${PROJECT_ID}/results"

echo "🟨 Script başlatıldı..."
set -x  # debug mod

# 0. Allure sonuç klasörü kontrolü
if [ ! -d "$ALLURE_RESULTS_DIR" ]; then
  echo "❌ Klasör bulunamadı: $ALLURE_RESULTS_DIR"
  exit 1
fi

# 1. Hedef klasörü oluştur (silme yok, geçmişi koru)
mkdir -p "$TARGET_RESULTS_DIR"

# 2. Sonuçları kopyala
cp -R "$ALLURE_RESULTS_DIR"/* "$TARGET_RESULTS_DIR"/

# 3. Proje oluştur (varsa hata vermez)
curl -X POST "$ALLURE_API/allure-docker-service/projects" \
     -H "Content-Type: application/json" \
     -d "{\"id\":\"$PROJECT_ID\"}" -ik

# 4. Sonuçları yükle
echo "📤 Sonuçlar gönderiliyor..."
for file in "$TARGET_RESULTS_DIR"/*; do
  curl -X POST "$ALLURE_API/allure-docker-service/send-results?project_id=$PROJECT_ID" \
       -H "Content-Type: multipart/form-data" \
       -F "files[]=@$file" -ik
done

# 5. Her çalıştırmaya benzersiz bir execution adı ver
EXECUTION_NAME="execution_$(date +%Y%m%d_%H%M%S)"
EXECUTION_FROM="$ALLURE_UI"
EXECUTION_TYPE="manual"

# 6. Rapor oluştur
echo "------------------GENERATE-REPORT------------------"
RESPONSE=$(curl --max-time 60 -X GET \
  "$ALLURE_API/allure-docker-service/generate-report?project_id=$PROJECT_ID&execution_name=$EXECUTION_NAME&execution_from=$EXECUTION_FROM&execution_type=$EXECUTION_TYPE" -ik)

# 7. Report URL çıkar
ALLURE_REPORT=$(grep -o '"report_url":"[^"]*' <<< "$RESPONSE" | grep -o '[^\"]*$')
echo "📊 Allure UI URL: $ALLURE_UI"
echo "📊 Allure Report URL: $ALLURE_REPORT"
