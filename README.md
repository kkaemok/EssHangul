# EssHangul

Essential Mod의 Social 탭 채팅에서 한글 IME 입력을 안정적으로 사용할 수 있게 해주는 Fabric 클라이언트 모드입니다.

## 주요 기능

- Essential Social 탭 채팅 입력창 한글 조합 입력 개선
- 마지막 글자 반영 지연(예: Tab 눌러야 보이는 현상) 완화
- 한/영 전환 안정성 개선
- Mod Menu 설정 연동
  - `Essential Social Hangul: ON/OFF` 토글 제공

## 적용 범위

- 이 모드는 **Essential Social 탭 채팅 입력창**에만 동작하도록 설계되어 있습니다.

## 호환성

- Minecraft: `26.1.2`
- Fabric Loader: `0.19.2+`
- Fabric API: `0.148.0+26.1.2`
- 필수 모드: `Essential Mod`
- 선택 모드: `ModMenu` (설정 화면 사용 시)

## 설치 방법

1. `EssHangul-<version>.jar`를 `.minecraft/mods` 폴더에 넣습니다.
2. Fabric 프로필로 게임을 실행합니다.
3. Mod Menu를 사용하는 경우 EssHangul 설정에서 ON/OFF를 조절할 수 있습니다.

## 설정

- Mod Menu 경로: `EssHangul -> Config`
- 옵션:
  - `Essential Social Hangul: ON/OFF`
- 설정 파일:
  - `.minecraft/config/esshangul.properties`

## 소스 빌드

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

빌드 결과물:

- `build/libs/EssHangul-<version>.jar`

## 알려진 사항

- Essential 내부 UI/입력 구조가 업데이트되면 추가 대응이 필요할 수 있습니다.
- 문제가 발생하면 사용 중인 모드 목록과 로그를 함께 첨부해 주세요.

## 라이선스

이 프로젝트는 **GNU General Public License v3.0 이상**(`GPL-3.0-or-later`)을 따릅니다.  
자세한 내용은 [LICENSE](./LICENSE)를 참고하세요.
