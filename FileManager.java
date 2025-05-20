import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 *        이 클래스는 OurCalendar의 할 일 데이터를 파일로 저장하고 불러오는 기능을 제공합니다. 파일 이름은 "년-월.txt"
 *        형식으로 생성되며, 각 날짜별 할 일을 관리합니다.
 */
public class FileManager {

    /**
     * 현재 달의 할 일 데이터를 파일에 저장합니다.
     * 
     * @param year     저장할 연도
     * @param month    저장할 월 (0-based, 0=1월)
     * @param tasks    할 일 데이터를 포함하는 HashMap (key: "YYYY-MM-DD" 형식)
     */
    public static void saveToFile(int year, int month, HashMap<String, List<ToDo>> tasks) {
        // 파일명 형식: "년-월.txt" (월은 1-based로 표시)
        String fileName = String.format("%d-%d.txt", year, month + 1);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // 모든 날짜 순회
            for (String dateKey : tasks.keySet()) {
                List<ToDo> todoList = tasks.get(dateKey);
                if (todoList.isEmpty()) continue;

                // 파일에 날짜 헤더 작성 (예: "Day 2025-05-20:")
                writer.printf("Day %s:\n", dateKey);

                // 각 할 일 저장
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
     * 파일에서 할 일 데이터를 읽어 HashMap에 로드합니다.
     * 
     * @param year     불러올 연도
     * @param month    불러올 월 (0-based, 0=1월)
     * @return         할 일 데이터를 포함하는 HashMap (key: "YYYY-MM-DD" 형식)
     */
    public static HashMap<String, List<ToDo>> loadFromFile(int year, int month) {
        String fileName = String.format("%d-%d.txt", year, month + 1);
        File file = new File(fileName);
        HashMap<String, List<ToDo>> tasks = new HashMap<>();

        // 파일이 존재하지 않으면 빈 HashMap 반환
        if (!file.exists()) {
            return tasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentDateKey = null;

            while ((line = reader.readLine()) != null) {
                // 날짜 헤더 감지 (예: "Day 2025-05-20:")
                if (line.startsWith("Day ")) {
                    currentDateKey = line.substring(4, line.length() - 1);
                    tasks.putIfAbsent(currentDateKey, new ArrayList<>());
                    continue;
                }

                // 유효하지 않은 날짜인 경우 건너뜀
                if (currentDateKey == null) continue;

                // 할 일 파싱
                String[] parts = line.split("\\|");
                if (parts.length != 16) continue; // 필드 개수 확인

                ToDo todo = new ToDo(
                    parts[0],  // taskName
                    parts[1],  // location
                    Boolean.parseBoolean(parts[2]),  // allDay
                    Integer.parseInt(parts[3]),  // startYear
                    Integer.parseInt(parts[4]),  // startMonth
                    Integer.parseInt(parts[5]),  // startDay
                    Integer.parseInt(parts[6]),  // startHour
                    Integer.parseInt(parts[7]),  // startMinute
                    Integer.parseInt(parts[8]),  // endYear
                    Integer.parseInt(parts[9]),  // endMonth
                    Integer.parseInt(parts[10]), // endDay
                    Integer.parseInt(parts[11]), // endHour
                    Integer.parseInt(parts[12]), // endMinute
                    parts[13],  // alarm
                    parts[14],  // repeat
                    parts[15]   // memo
                );

                tasks.get(currentDateKey).add(todo);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}