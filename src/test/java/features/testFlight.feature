@testflight @ios @cloud
Feature: TestFlight üzerinden uygulama kurulumu ve açılışı

  Scenario Outline: TestFlight ile bir uygulamayı indirip aç
    Given TestFlight açılır ve "<bundleId>" terminate & activate edilir
    When "<appKey>" uygulaması TestFlight'ta aranır ve bulunursa tıklanır
    And Hedef uygulama "<targetBundleId>" yüklü ise kaldırılır
    And Install butonuna basılır ve yükleme tamamlanır
    And OPEN butonuna basılarak uygulama açılır
    Then "<appKey>" uygulamasının başarıyla açıldığı doğrulanır

    Examples:
      | appKey     | bundleId              | targetBundleId                |
      | Sociable - Video Chat & Games   | com.apple.TestFlight  | com.forplay.app             |

