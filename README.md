# 2026-ybict-system2

## 📌 DB 설치
'''
1. 설치하기 
brew install mariadb@11.8 (mac기준, 11.8 버전으로 설치)
brew install mariadb      (mac기준)
필요한 설치 버전 : mariadb from 12.3.2-MariaDB, client 15.2 for osx10.19 (arm64) using  EditLine wrapper
폐쇠망 설치 방법 : https://mariadb.org/download/?t=mariadb&p=mariadb&r=12.3.2&os=windows&cpu=x86_64&pkg=msi&mirror=ossplanet

1-1. 포트 사용여부 확인하기
lsof -i :3306                   (mac기준)
netstat -ano | findstr 3306     (window기준)

3. DB 실행 및 확인 
실행 : brew services start mariadb  
확인 : brew services list

3-1. PW를 입력하라고 하는데 모르느경우, PW 변경방법 
서비스  정지 : brew services stop mariadb
안전모드 수행 : mysqld_safe --skip-grant-tables --datadir=/opt/homebrew/var/mysql &
(접속1 또는 접속2 방식으로 접속)
접속 1 : mariadb -u root
TCP 방식 접속 2 : mariadb -u root -h 127.0.0.1 -P 3306 
PW 변경 : 원하는 PWD 입력 ('1234')
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('1234');
FLUSH PRIVILEGES;
EXIT;

4. DB 서비스 종료후 재시작 
pkill -9 mariadbd
rm -f /tmp/mysql.sock
brew services start mariadb

5. 접속
mariadb -u root -p1234

6. DB생성 및 확인
CREATE DATABASE ict_yb_db;
SHOW DATABASES;

7. 계정 생성 및 적용
CREATE USER 'icyybusr'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON ict_yb_db.* TO 'icyybusr'@'localhost';
FLUSH PRIVILEGES;

'''

## 📌 DBeaver 설치 및 접속
'''
1. DBeaver설치
https://dbeaver.io/download/
brew install --cask dbeaver-community

2. 접속 
- 좌측 상단 콘센트 아이콘 클릭 → New Database Connection
- 데이터베이스 종류 선택 (Mariadb)
3. 연결 정보 입력 (Main 탭)
항목(입력값)
Server Host(localhost)
Port(3306)
Database(ict_yb_db)
Username(icyybusr)
Password(1234)
'''


## 📌 폐쇄망 프론트쪽 환경구성(Jest me)
'''
1. node.js 설치
https://nodejs.org/dist/v24.2.0/
https://nodejs.org/dist/v24.2.0/node-v24.2.0-x64.msi          ← Windows용
https://nodejs.org/dist/v24.2.0/node-v24.2.0-linux-x64.tar.xz  ← Linux용


2. 개발 환경 준비 사항
Phase 1. Mac에서 준비 (지금, 인터넷 됨)

cd ictyb_front
1. 일반 설치 (정상 동작 확인용)
npm install
npm run dev   # localhost:5173 등에서 뜨는지 확인

2. 오프라인 캐시 생성 (Mac용 바이너리 포함)
npm install --cache ./offline-cache

3. Windows(win32/x64)용 바이너리 강제로 캐시에 추가
npm install --cache ./offline-cache --os=win32 --cpu=x64 --force

4. Linux(x64)용도 미리 추가 (나중에 서버에서 필요할 수도 있으니)
npm install --cache ./offline-cache --os=linux --cpu=x64 --force

4. 이관용 압축
cd ..
zip -r ictyb_front_transfer.zip \
  ictyb_front -x "ictyb_front/node_modules/*" \
  node-v24.2.0-x64.msi \
  node-v24.2.0-linux-x64.tar.xz


5. Phase 3. Windows(폐쇄망)에서 개발
1. Node.js 설치 (node-v24.2.0-x64.msi 실행)
node -v   # v24.2.0 정확히 나오는지 확인

2. zip 해제 후 폴더 진입
cd ictyb_front

3. npm 캐시를 이관받은 offline-cache로 지정
npm config set cache .\offline-cache

4. 오프라인 설치 (네트워크 전혀 안 씀)
npm install --offline

5. 개발 서버 실행
npm run dev


'''
