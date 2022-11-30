# exploedView

Carto Mobile SDK를 이용하여 전개도 레이어 표출 및 편집 기능 구현

<img src="https://img.shields.io/badge/Kotlin-000?style=flat-square&logo=kotlin&logoColor=blue"/> <img src="https://img.shields.io/badge/Android-000?style=flat-square&logo=android&logoColor=green"/> <img src="https://img.shields.io/badge/carto-000?style=flat-square&logo=carto&logoColor=green"/>
<img src="https://img.shields.io/badge/GitHub-000?style=flat-square&logo=GitHub&logoColor=blue"/>


# 데모 앱 설명

### 좌표계   : EPSG:5179 -> EPSG:4326
> Sample : duson.json (해운대 두산위브더 제니스 공동주택 데이터)

# 💡 개요

------------

1. AssetManger를 통해 target ~.geojson Feature를 가져온다.

2. FeatureCount만큼 최초 전개도 레이어에 Element를 담아준다.

3. 사용자는 레이어 생성 및 편집을 할 수 있어야한다.

# 🖥 기능

------------

1. 선택 / 비선택 ( Android MVMM 디자인 패턴 적용 (ViewModel) )

2. 머터리얼 디자인 3 적용

3. 사용자는 층 추가 및 라인 추가를 진행 할 수 있다.

4. 그룹영역 내 포함된 Polygon을 알 수 있다
