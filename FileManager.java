
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 기본 파일 관리 클래스 - 일정 데이터를 파일로 저장하고 로드하는 기능을 제공
 * UserDataManager와 달리 공통 data 폴더를 사용하는 레거시 구현
 */

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String DATA_DIR = "data"; // 기본 데이터 디렉터리

    /**
     * 일정 데이터를 파일로 저장
     * 
     * @param year  연도
     * @param month 월 (0부터 시작)
     * @param tasks 저장할 일정 맵
     */
    public static void saveToFile(int year, int month, HashMap<String, List<ToDo>> tasks) {
        // 데이터 디렉터리 생성
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        String fileName = String.format("%s/%d-%d.txt", DATA_DIR, year, month + 1);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // 해당 월의 일정만 필터링하여 저장
            String targetMonth = String.format("%04d-%02d", year, month + 1);
            for (String dateKey : tasks.keySet()) {
                if (!dateKey.startsWith(targetMonth))
                    continue;
                List<ToDo> todoList = tasks.get(dateKey);
                if (todoList.isEmpty())
                    continue;

                writer.printf("Day %s:\n", dateKey);

                // 각 일정을 파이프(|)로 구분된 문자열로 저장
                for (ToDo todo : todoList) {
                    writer.printf("%s|%s|%b|%d|%d|%d|%d|%d|%d|%d|%d|%d|%d|%s|%s|%s\n",
                            todo.getTaskName(),
                            todo.getLocation(),
                            todo.isAllDay(),
                            todo.getStartYear(),
                            todo.getStartMonth(),
                            todo.getStartDay(),
                            todo.getStartHour(),
                            todo.getStartMinute(),
                            todo.getEndYear(),
                            todo.getEndMonth(),
                            todo.getEndDay(),
                            todo.getEndHour(),
                            todo.getEndMinute(),
                            todo.getAlarm(),
                            todo.getRepeat(),
                            todo.getMemo());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 파일에서 일정 데이터를 로드
     * 
     * @param year  연도
     * @param month 월 (0부터 시작)
     * @return 로드된 일정 맵
     */
    public static HashMap<String, List<ToDo>> loadFromFile(int year, int month) {
        // 파일명 생성
        String fileName = String.format("%s/%d-%d.txt", DATA_DIR, year, month + 1);
        File file = new File(fileName);
        HashMap<String, List<ToDo>> tasks = new HashMap<>();

        if (!file.exists()) {
            return tasks; // 파일이 없으면 빈 맵 반환
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentDateKey = null;

            while ((line = reader.readLine()) != null) {
                // 날짜 헤더 라인 처리
                if (line.startsWith("Day ")) {
                    currentDateKey = line.substring(4, line.length() - 1);
                    tasks.putIfAbsent(currentDateKey, new ArrayList<>());
                    continue;
                }

                if (currentDateKey == null)
                    continue;

                // 일정 데이터 파싱 (16개 필드)
                String[] parts = line.split("\\|");
                if (parts.length != 16)
                    continue;

                ToDo todo = new ToDo(
                        parts[0], // 일정명
                        parts[1], // 장소
                        Boolean.parseBoolean(parts[2]), // 하루종일 여부
                        Integer.parseInt(parts[3]), // 시작년도
                        Integer.parseInt(parts[4]), // 시작월
                        Integer.parseInt(parts[5]), // 시작일
                        Integer.parseInt(parts[6]), // 시작시간
                        Integer.parseInt(parts[7]), // 시작분
                        Integer.parseInt(parts[8]), // 종료년도
                        Integer.parseInt(parts[9]), // 종료월
                        Integer.parseInt(parts[10]), // 종료일
                        Integer.parseInt(parts[11]), // 종료시간
                        Integer.parseInt(parts[12]), // 종료분
                        parts[13], // 알림
                        parts[14], // 반복
                        parts[15]); // 메모

                tasks.get(currentDateKey).add(todo);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}