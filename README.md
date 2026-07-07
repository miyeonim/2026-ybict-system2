# 2026 YBICT System 환경 구성 가이드

> 폐쇄망 이관을 전제로 한 DB / DBeaver / 프론트엔드 개발 환경 세팅 문서입니다.

---

## 📌 1. DB 설치 (MariaDB)

### 1-1. 설치

| 환경 | 명령어 / 링크 |
|---|---|
| Mac (11.8 버전) | `brew install mariadb@11.8` |
| Mac (기본) | `brew install mariadb` |
| 요구 버전 | `mariadb from 12.3.2-MariaDB, client 15.2 for osx10.19 (arm64) using EditLine wrapper` |
| 폐쇄망 설치 | [MariaDB 12.3.2 / Windows / x86_64 / msi 다운로드](https://mariadb.org/download/?t=mariadb&p=mariadb&r=12.3.2&os=windows&cpu=x86_64&pkg=msi&mirror=ossplanet) |

### 1-2. 포트 사용 여부 확인

```bash
# Mac
lsof -i :3306

# Windows
netstat -ano | findstr 3306
```

### 1-3. DB 실행 및 확인

```bash
# 실행
brew services start mariadb

# 상태 확인
brew services list
```

### 1-4. 비밀번호를 모르는 경우 (PW 초기화)

```bash
# 1. 서비스 정지
brew services stop mariadb

# 2. 안전모드 수행
mysqld_safe --skip-grant-tables --datadir=/opt/homebrew/var/mysql &

# 3. 접속 (둘 중 하나)
mariadb -u root
# 또는
mariadb -u root -h 127.0.0.1 -P 3306
```

접속 후 비밀번호 변경 (예: `1234`):

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('1234');
FLUSH PRIVILEGES;
EXIT;
```

### 1-5. 서비스 재시작 (문제 발생 시)

```bash
pkill -9 mariadbd
rm -f /tmp/mysql.sock
brew services start mariadb
```

### 1-6. 접속

```bash
mariadb -u root -p1234
```

### 1-7. DB 생성 및 확인

```sql
CREATE DATABASE ict_yb_db;
SHOW DATABASES;
```

### 1-8. 계정 생성 및 권한 부여

```sql
CREATE USER 'icyybusr'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON ict_yb_db.* TO 'icyybusr'@'localhost';
FLUSH PRIVILEGES;
```

---

## 📌 2. DBeaver 설치 및 접속

### 2-1. 설치

- 다운로드: https://dbeaver.io/download/
- Mac (Homebrew):
  ```bash
  brew install --cask dbeaver-community
  ```

### 2-2. 접속 설정

1. 좌측 상단 콘센트 아이콘 클릭 → **New Database Connection**
2. 데이터베이스 종류 선택 → **MariaDB**
3. 연결 정보 입력 (Main 탭)

| 항목 | 값 |
|---|---|
| Server Host | `localhost` |
| Port | `3306` |
| Database | `ict_yb_db` |
| Username | `icyybusr` |
| Password | `1234` |

---

## 📌 3. 폐쇄망 Back 엔드 환경 구성

> 목표: back엔드 관련 환경 구성 
```bash
# 0) 개발 도구 옮기기 : vscode or STS 

# 1) 자바 버전 : 17
https://adoptium.net/temurin/releases/?version=17
```

# 2) Mac에서 오프라인 Maven 캐시 만들기 
```bash

```

### 3-1. Node.js 준비


## 📌 4. 폐쇄망 프론트엔드 환경 구성

> 목표: **Mac(인터넷 O) → Windows(폐쇄망 개발) → Linux/Unix(운영 서버)** 순으로 이관하며, node_modules는 직접 복사하지 않고 npm 오프라인 캐시를 이관하는 방식.

### 3-1. Node.js 준비

동일 버전(`v24.2.0`)으로 각 OS별 설치파일을 미리 받아둡니다.

| 대상 | 다운로드 |
|---|---|
| 버전 목록 | https://nodejs.org/dist/v24.2.0/ |
| Windows용 | [node-v24.2.0-x64.msi](https://nodejs.org/dist/v24.2.0/node-v24.2.0-x64.msi) |
| Linux용 | [node-v24.2.0-linux-x64.tar.xz](https://nodejs.org/dist/v24.2.0/node-v24.2.0-linux-x64.tar.xz) |

> ⚠️ 세 환경(Mac / Windows / Linux) 모두 **정확히 같은 버전**으로 통일해야 네이티브 모듈(esbuild 등) 호환 문제가 없습니다.

### 3-2. Phase 1 — Mac에서 준비 (인터넷 가능한 지금)

```bash
cd ictyb_front

# 1) 일반 설치 + 정상 동작 확인
npm install
npm run dev   # localhost:5173 등에서 확인

# 2) 오프라인 캐시 생성 (Mac용 바이너리 포함)
npm install --cache ./offline-cache

# 3) Windows(win32/x64)용 바이너리 강제로 캐시에 추가
npm install --cache ./offline-cache --os=win32 --cpu=x64 --force

# 4) Linux(x64)용도 미리 추가 (운영 서버 대비)
npm install --cache ./offline-cache --os=linux --cpu=x64 --force
```

### 3-3. 이관용 압축 (node_modules 제외)

```bash
cd ..
zip -r ictyb_front_transfer.zip \
  ictyb_front -x "ictyb_front/node_modules/*" \
  node-v24.2.0-x64.msi \
  node-v24.2.0-linux-x64.tar.xz
```

> 이 zip을 사내 매체반입 절차(USB 등)로 폐쇄망에 전달합니다.

### 3-4. Phase 2 — Windows(폐쇄망)에서 개발

```powershell
# 1) Node.js 설치 (node-v24.2.0-x64.msi 실행)
node -v   # v24.2.0 정확히 나오는지 확인

# 2) zip 해제 후 폴더 진입
cd ictyb_front

# 3) npm 캐시를 이관받은 offline-cache로 지정
npm config set cache .\offline-cache

# 4) 오프라인 설치 (네트워크 전혀 사용 안 함)
npm install --offline

# 5) 개발 서버 실행
npm run dev
```

> 특정 패키지가 캐시에 없다는 에러가 나면, Mac에서 해당 패키지만 `--os=win32 --cpu=x64 --force`로 다시 받아 재이관하세요.

### 3-5. Phase 3 — 최종 Linux/Unix 서버 배포

**옵션 A (권장, 정적 서빙)**

```bash
npm run build   # dist/ 폴더 생성
```

→ `dist` 폴더만 서버로 이관 → nginx 등으로 정적 서빙 (Node/npm 불필요)

```nginx
server {
  listen 80;
  root /var/www/ictyb_front/dist;
  location / { try_files $uri /index.html; }
  location /api/ { proxy_pass http://ictyb_back:8082/; }
}
```

**옵션 B (서버에서 직접 빌드/실행이 필요한 경우)**

- 미리 받아둔 `node-v24.2.0-linux-x64.tar.xz` + 이관된 `offline-cache` 사용
- `npm install --offline` → `npm run build` (또는 `pm2`로 실행)

---

## ✅ 체크리스트 요약

- [ ] MariaDB 설치 및 계정/DB 생성 완료
- [ ] DBeaver 접속 확인 완료
- [ ] Mac에서 Node.js v24.2.0 기준 오프라인 캐시(offline-cache) 생성 완료
- [ ] Windows/Linux용 Node 설치파일 확보 완료
- [ ] 이관용 zip 압축 완료 (node_modules 제외 확인)
- [ ] Windows 폐쇄망에서 `npm install --offline` 정상 동작 확인
- [ ] 최종 서버에 `dist` 정적 파일 서빙 또는 Node 런타임 배포 완료
