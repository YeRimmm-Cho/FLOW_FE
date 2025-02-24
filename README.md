# 🍴 Multi-modal LLM 기반 조리 도우미 서비스 : Voice를 곁들인 방구석 미슐랭
2024년 SW개발자양성과정 산학연계프로젝트2 - FLOW  
<br>

## 📌 프로젝트 개요
본 서비스는 Multimodal(이미지 및 음성 기반) & Interactive(상호작용형) AI 조리 어플리케이션으로 MZ 세대를 위한 집밥 조리의 편의성을 높이며 요리 진입 장벽을 낮추는 것을 목표로 합니다.
사용자의 재료 이미지, 온라인 영수증, 유튜브 요리 영상 링크를 활용하여 재료 인식 → 레시피 추천 → 조리 과정 안내까지 한 번에 해결하는 All-in-One Solution을 제공합니다.
기존 요리 보조 앱들이 재료를 수동 입력해야 하고 일방향적인 정보 제공 방식이었다는 한계를 개선하여, 음성 기반 실시간 상호작용 AI 조리 보조 서비스를 통해 더 쉽고 편리한 요리를 할 수 있습니다.    
<br>

## 👥 팀구성
구분 | 성명 | 학번 | 소속학과 | 연계전공 | 역할
------|-------|-------|-------|-------|-------
팀장 | [황재연](https://github.com/khwwang) | 2019112487 | 산업시스템공학과 | 데이터사이언스 | 백엔드      
팀원 | [김동호](https://github.com/DecisionwitHdata) | 2019111437 | 경영학과 | 데이터사이언스 | 백엔드      
팀원 | [김유선](https://github.com/kimyusun) | 2020112473 | 산업시스템공학과 | 데이터사이언스 | 백엔드
팀원 | [조예림](https://github.com/YeRimmm-Cho) | 2020111500 | 회계학과 | 융합소프트웨어 | 프론트엔드    
<br>

## 🌟 주요 기능
- <b>유튜브 요리 영상 & 재료 인식 기반 레시피 추출</b>
  - YouTube URL을 업로드 할 경우 영상의 스크립트를 추출하여 조리 보조
  - 재료 사진 및 영수증 사진을 업로드 할 경우 GPT 4o 모델 기반으로 식재료 자동 인식 및 저장<br><br>
- <b>실시간 레시피 추천 & 요리 과정 지원</b>
  - 인식된 재료를 기반으로 GPT 모델이 3가지 레시피 추천
  - 추천된 레시피를 Recipe DB에 저장하여 조리 과정 단계별 안내<br><br>
- <b>음성 기반 AI 조리 보조 (STT/TTS + GPT)</b>
  - STT API를 활용한 음성 인식으로 사용자의 질문을 분석
  - GPT 4o 모델과 Few-Shot Prompting을 적용하여 조리 과정 관련 응답 생성
  - TTS API를 통해 음성 출력하고 음성만으로도 요리 진행 가능<br><br>
- <b>사용자와 AI 간의 상호작용을 통한 조리 편의성 극대화</b>
  - 조리 과정 중 사용자가 추가 질문 가능 → AI가 실시간 피드백 제공
  - 필요 시 조리 단계 반복 요청 및 세부 설명 가능
  - 음성 및 텍스트 입력을 통한 직관적인 조리 보조 시스템 구축
<br>

## 🚀 기대효과   
**✔️ 개인적 측면**    

- 멀티모달 AI 활용으로 번거로운 검색 없이 이미지(재료/영수증)나 유튜브 링크 입력만으로 필요한 레시피를 쉽게 찾을 수 있음
- 요리에 익숙하지 않은 사용자도 개인화된 레시피 추천과 실시간 음성 보조 기능을 통해 다양한 요리를 시도할 기회를 제공
- 음성 기반 AI 조리 보조로 요리 과정 중 핸즈프리(Hands-free) 조리 가능<br>
  
**✔️ 경제적 측면**    

- 보유한 식재료를 활용한 맞춤형 레시피 추천을 통해 불필요한 외식 및 추가 재료 구매 감소
- 1인 가구 및 가정에서 남은 식재료를 효과적으로 활용, 음식물 쓰레기 절감 및 식비 절약 가능
- 외식 물가 상승에 대응하여 MZ 세대가 직접 집밥을 해먹을 수 있도록 지원<br>
  
**✔️ 사회적 측면**    

- 텍스트 입력이 어려운 사용자 및 고령층도 멀티모달을 활용하여 쉽게 접근 가능
- AI 기반 레시피 추천과 음성 상호작용을 통해 조리 과정을 보다 쉽게 안내하여 요리 경험이 부족한 사용자도 편리하게 조리 가능
- 남은 재료를 활용한 요리 추천을 통해 음식물 쓰레기를 줄이고 환경 보호 및 지속 가능한 소비 문화 촉진

<br>

 ## 🖥️ 주요 화면 구성

<table>
  <tr>
    <th>레시피 추천 방식 선택 화면</th>
    <th>재료 사진 업로드 화면</th>
    <th>재료 인식 화면</th>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/cbfcc29a-9d11-4d27-98f9-4b1c38a76d00"  width="250"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/96527b8b-b994-4bf8-a6db-6c932a748004"  width="250"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/494b8f62-fe38-4a93-ae50-3606ca9bf887"  width="250"></td>
  </tr>
  <tr>
    <th>레시피 추천 화면</th>
    <th>추천 레시피 디테일 화면</th>
    <th>조리 어시스턴트 화면</th>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/32697126-529f-4023-8ddd-814379a85ad7"  width="250"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/f22da4d4-5f8b-40aa-a126-b516f8870391"  width="250"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/28a8cd33-af18-4fce-9a9d-48529f8ff905"  width="250"></td>
  </tr>
</table>
<br>

## 📂 시스템 아키텍쳐
![image](https://github.com/user-attachments/assets/7d45033b-6256-4bd5-b5db-6ceac87a5a4c)    
<br>

## 📈 사용 기술 및 성능 평가
<table>
  <tr>
    <th>GPT 4o</th>
    <th>BERT Score</th>
    <th>Few-shot Prompting</th>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/d4c33bb5-6549-4c73-a752-cb627a5bd9a3" alt="GPT" width="500"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/adb0c0eb-447a-41f5-a62e-06fcaba73487" alt="Bert" width="500"></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/768a63d5-01bf-4682-8876-bcf064496403" alt="Few-shot Prompting" width="500"></td>
  </tr>
</table>    
<br>

## 🛠️ 기술 스택
<div style="display:flex; flex-direction:column; align-items:flex-start;">
    <!-- Frontend -->
    <p><strong>Frontend</strong></p>
    <div>
      <img src="https://img.shields.io/badge/androidstudio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=black">
      <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
      <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=black">
      <img src="https://img.shields.io/badge/socket.io-010101?style=for-the-badge&logo=socket.io&logoColor=white">
      <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
    </div>
  <br>
    <!-- Backend -->
    <p><strong>Backend</strong></p>
    <div>
      <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white">
      <img src="https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white">
      <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
      <img src="https://img.shields.io/badge/sqlite-003B57?style=for-the-badge&logo=sqlite&logoColor=white">
      <img src="https://img.shields.io/badge/openai-412991?style=for-the-badge&logo=openai&logoColor=white">
      <img src="https://img.shields.io/badge/amazons3-569A31?style=for-the-badge&logoColor=white">
    </div>
</div>

