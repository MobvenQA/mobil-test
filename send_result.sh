#!/bin/bash

ALLURE_RESULTS_DIR="allure-results"
PROJECT_ID="madduck"
ALLURE_API="http://localhost:8181"
ALLURE_UI="http://localhost:8282"
TARGET_RESULTS_DIR="projects/${PROJECT_ID}/results"

echo "🟨 Script başlatıldı..."

# 0. Allure sonuç klasörü mevcut mu?
if [ ! -d "$ALLURE_RESULTS_DIR" ]; then
  echo "❌ Klasör bulunamadı: $ALLURE_RESULTS_DIR"
  exit 1
fi

# 1. Hedef klasörü temizle ve yeniden oluştur
rm -rf "$TARGET_RESULTS_DIR"
mkdir -p "$TARGET_RESULTS_DIR"

# 2. Sonuçları kopyala
cp -R "$ALLURE_RESULTS_DIR"/* "$TARGET_RESULTS_DIR"/

# 3. Proje oluştur (zaten varsa yok sayılır)
curl -X POST "$ALLURE_API/allure-docker-service/projects" \
     -H "Content-Type: application/json" \
     -d "{\"id\":\"$PROJECT_ID\"}" -ik

# 4. Sonuçları multipart olarak gönder (eval ile)
echo "📤 Sonuçlar gönderiliyor..."

FILES=""
for file in "$TARGET_RESULTS_DIR"/*; do
  FILES+=" -F files[]=@\"$file\""
done

eval curl -X POST "$ALLURE_API/allure-docker-service/send-results?project_id=$PROJECT_ID" \
     -H "Content-Type: multipart/form-data" $FILES -ik

# 5. Raporu oluştur
EXECUTION_NAME="execution_from_script"
EXECUTION_FROM="http://localhost:8282"
EXECUTION_TYPE="manual"

echo "------------------GENERATE-REPORT------------------"
RESPONSE=$(curl -X GET "$ALLURE_API/allure-docker-service/generate-report?project_id=$PROJECT_ID&execution_name=$EXECUTION_NAME&execution_from=$EXECUTION_FROM&execution_type=$EXECUTION_TYPE" -ik)

# 6. Report URL'yi çıkar ve yazdır
ALLURE_REPORT=$(grep -o '"report_url":"[^"]*' <<< "$RESPONSE" | grep -o '[^\"]*$')
echo "📊 Allure UI URL: $ALLURE_UI"
echo "📊 Allure Report URL: $ALLURE_REPORT"
