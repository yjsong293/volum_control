Volume Lock - Android Studio 프로젝트

포함 기능
- 미디어/통화/벨소리/알림/알람/시스템 음량 개별 고정 ON/OFF
- 각 음량의 목표 수준 0~100% 설정
- 전체 고정 스위치
- 포그라운드 서비스 기반 음량 변경 감시 및 복구 시도
- 재부팅 후 설정 복원 시도

빌드 방법
1. Android Studio를 설치합니다.
2. Android Studio에서 이 폴더(VolumeLock)를 Open 합니다.
3. Gradle Sync가 끝날 때까지 기다립니다.
4. 상단 메뉴 Build > Build Bundle(s) / APK(s) > Build APK(s)를 선택합니다.
5. 생성된 APK는 app/build/outputs/apk/debug/app-debug.apk 에서 확인합니다.

중요한 제한
- 이 프로젝트는 범용 Android API를 사용한 초기 구현입니다.
- 일반 앱은 Android의 모든 버전/제조사에서 볼륨 변경을 완벽히 가로채거나 차단할 수 없습니다.
- 볼륨 변경 감지는 Settings.System의 변경 관찰을 이용한 best-effort 방식이며 기기별 검증이 필요합니다.
- 앱을 설정에서 강제 종료(Force stop)하면 서비스가 중단됩니다. 재부팅 후 자동 실행도 제조사 절전 정책에 따라 제한될 수 있습니다.
- 일부 기기에서는 통화/시스템 등 특정 스트림이 다르게 동작하거나 사용할 수 없습니다.
- 정확한 동작 확인을 위해 삼성 갤럭시와 레노버 기기에서 각각 테스트해야 합니다.
- 이 앱은 음량 단계(0~최대 단계)를 백분율로 환산합니다. 실제 음압의 백분율은 아닙니다.


GitHub에서 Android Studio 없이 APK 만들기
1. GitHub 계정에 로그인하고 New repository를 선택합니다.
2. 저장소 이름을 VolumeLock 으로 정하고 Public 또는 Private 저장소를 만듭니다.
3. 이 프로젝트 ZIP의 압축을 풉니다.
4. 압축을 푼 폴더 안의 파일과 .github 폴더를 저장소에 업로드합니다.
   - .github 폴더가 숨김 폴더로 보이지 않으면 숨김 파일 표시를 켭니다.
   - 저장소 최상위에 app, .github, build.gradle.kts 등이 보여야 합니다.
5. GitHub 저장소의 Actions 탭에서 Build Android APK 워크플로를 선택합니다.
6. Run workflow를 누릅니다. main 브랜치에 파일을 올려도 자동 빌드됩니다.
7. 빌드가 성공하면 실행 기록 페이지 아래 Artifacts에서
   VolumeLock-debug-apk 를 다운로드합니다.
8. 다운로드한 ZIP을 풀면 app-debug.apk가 있습니다. 이를 Android 기기로 옮겨 설치합니다.

주의
- GitHub Actions는 클라우드에서 빌드만 수행합니다. GitHub 계정이 필요합니다.
- APK는 현재 디버그 서명 APK입니다. 개인 기기 테스트용이며 Play 스토어 배포용 릴리스 서명 APK가 아닙니다.
- 저장소 업로드 시 .github/workflows/build-apk.yml 파일이 반드시 포함되어야 합니다.
