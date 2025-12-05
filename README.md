# ⏰ WakeUp! - 미션 수행 스마트 알람 앱

[![Android](https://img.shields.io/badge/Android-Kotlin-green?logo=android)](https://developer.android.com)
[![Award](https://img.shields.io/badge/Award-2025%20DMU%20스마트앱_경진대회_장려상-gold)](https://github.com/Minseo033/WakeUp-)

> **"더 이상 알람을 끄고 다시 잠들지 마세요. 확실하게 깨워드립니다."**
> 
> **🏆 2025 DMU 스마트앱 경진대회 장려상 수상작**

## 📱 프로젝트 소개
**WakeUp**은 사용자가 수학 문제, 흔들기, 타자 입력 등 능동적인 미션을 수행해야만 알람이 꺼지는 안드로이드 앱입니다.
단순 기상을 넘어, 수면 패턴을 분석하고 AI 코멘트를 제공하여 건강한 수면 습관을 돕습니다.

## ✨ 핵심 기능
* **다이내믹 기상 미션 4종:** 수학 문제, 폰 흔들기, 연타하기, **나만의 명언 타자 치기**
* **스마트 알람 시스템:** Android **Doze Mode(절전)**를 우회하여 100% 정시 알람 보장
* **수면 분석 AI:** 평균 수면 시간 및 표준편차 알고리즘을 활용한 **수면 규칙성 분석** 및 조언 제공
* **사용자 친화적 UI:** 최신 트렌드의 카드형 UI 및 직관적인 설정 화면

## 🛠 기술 스택 (Tech Stack)
* **Language:** Kotlin
* **Architecture:** MVC / Repository Pattern
* **Local DB:** Room Database (v4)
* **System:** AlarmManager, BroadcastReceiver, Service, Notification
* **Sensor:** Accelerometer (가속도 센서)
* **UI:** XML Layout, Custom Views (막대그래프 직접 구현)

## 📂 발표 자료
[📄 발표 자료 보러가기](./presentation/WakeUp발표자료.pdf)

## 📸 스크린샷

### 메인 및 미션 화면
| 메인 화면 | 알람 화면 (미션X) |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/8414d39f-6c77-4114-9ab0-6b331f2a9256" width="250"/> | <img src="https://github.com/user-attachments/assets/77b2fbd8-f8b1-4150-92f3-4bf1df57ba18" width="250"/> |

| 🧮 수학 미션 | 📱 흔들기 미션 | ⌨️ 타자 미션 | 👆 연타 미션 |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/fe839186-0095-4cc6-abda-ec70b4dc370b" width="200"/> | <img src="https://github.com/user-attachments/assets/403ef525-1530-4e7a-b0be-5e18b17bc686" width="200"/> | <img src="https://github.com/user-attachments/assets/dd0a433b-a615-4dfb-8032-e16a3be07b2c" width="200"/> | <img src="https://github.com/user-attachments/assets/b199c1bb-236c-464c-b6dc-0d8c19d85a99" width="200"/> |

### 수면 분석 및 설정
| 📊 분석 화면 1 | 📊 분석 화면 2 |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/41661838-253a-4a64-9494-6c672418528b" width="250"/> | <img src="https://github.com/user-attachments/assets/b9882f7c-cbde-4dcd-bb57-2ef6c2037418" width="250"/> |

| ⚙️ 설정 화면 1 | ⚙️ 설정 화면 2 | ⚙️ 설정 (미션/탭) |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/8631b104-6a2c-478c-9c73-c8019364e256" width="200"/> | <img src="https://github.com/user-attachments/assets/13eff4b4-863c-4310-8220-459e8d5cf8bd" width="200"/> | <img src="https://github.com/user-attachments/assets/dc5362c8-6f96-4ee8-84cf-b23a491fe962" width="200"/> |
