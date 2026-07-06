# 2026-ybict-system2

## 📌 DB 설치
'''
CREATE DATABASE ict_yb_db;
CREATE USER 'icyybusr'@'localhost' IDENTIFIED BY '비밀번호';
GRANT ALL PRIVILEGES ON ict_yb_db.* TO 'icyybusr'@'localhost';
FLUSH PRIVILEGES;
'''