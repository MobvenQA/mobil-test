@f02_onboarding @ios @cloud
Feature: Onboarding Sürecini Tamamlama

  Scenario: Kullanıcı onboarding akışını tamamlar ve uygulamayı açar
    And Onboarding akışı tamamlanır
    Then Open butonu görünürse tıklanır
