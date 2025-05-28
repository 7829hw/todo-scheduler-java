#!/bin/bash
echo "📅 캘린더 서버 컴파일 중..."
javac -encoding UTF-8 *.java
if [ $? -ne 0 ]; then
    echo "컴파일 실패!"
    exit 1
fi
echo "서버 시작..."
java CalendarServer