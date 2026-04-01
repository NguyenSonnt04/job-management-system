@echo off
echo ========================================
echo Cap nhat tieu chi cham diem CV vao DB
echo ========================================
echo.
echo Dang khoi dong lai Docker MySQL container...
echo.

cd /d %~dp0
docker-compose restart mysql

echo.
echo Da khoi dong lai! Nhan Enter de tiep tuc...
pause > nul

echo.
echo Dang kiem tra du lieu...

docker-compose exec -T mysql mysql -u qltd_user -pqltd_pass123 qltd_db -e "SELECT id, name, max_score FROM cv_scoring_criteria ORDER BY display_order;"

echo.
echo ========================================
echo Neu thay 7 tieu chi ben tren = THANH CONG!
echo ========================================
echo.
pause
