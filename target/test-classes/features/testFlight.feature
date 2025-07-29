Feature: TestFlight üzerinden uygulama indirme ve açma

  @testflight
  Scenario Outline: TestFlight ile bir uygulamayı indirip aç
    Given cihaz "<platform>" üzerinde TestFlight açılır
    When appKey "<appKey>" ile uygulama TestFlight üzerinden yüklenir
    Then uygulama başarıyla açılır ve doğrulanır

    Examples:
      | platform | appKey     |
      | ios      | sampleApp  |
