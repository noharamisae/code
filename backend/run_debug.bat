@echo off
echo [INFO] Spring Boot 調試モードで起動します...
echo.

REM 切换到项目路径（必要なら変更）
cd /d D:\jtk_base\self\serverpj\code\backend

REM JVM にデバッグ引数を渡して起動
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:5005"

pause
