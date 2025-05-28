#!/bin/bash
echo "📅 캘린더 클라이언트 컴파일 중..."
javac -encoding UTF-8 *.java
if [ $? -ne 0 ]; then
    echo "컴파일 실패!"
    exit 1
fi
echo "클라이언트 시작..."
java ProgramApp