
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 사용자별 개인 일정과 공유 일정 캐시를 파일로 저장/로드하는 데이터 관리 클래스
 * 각 사용자마다 별도의 폴더를 생성하여 데이터를 분리 관리함
 */

import java.io.*;
import java.util.*;

public class UserDataManager {
    private String nickname; // 사용자 닉네임
    private String userDataDir; // 사용자별 데이터 디렉터리
    private String sharedCacheFile; // 공유 일정 캐시 파일 경로

    /**
     * UserDataManager 생성자
     * 
     * @param nickname 사용자 닉네임
     */
    public UserDataManager(String nickname) {
        this.nickname = nickname;
        // 특수문자를 언더스코어로 변환하여 안전한 폴더명 생성
        this.userDataDir = "data_" + nickname.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        this.sharedCacheFile = userDataDir + "/shared_cache.txt";

        // 사용자 데이터 폴더가 없으면 생성
        File dir = new File(userDataDir);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("사용자 데이터 폴더 생성: " + userDataDir);
        }
    }

    /**
     * 개인 일정을 파일로 저장
     * 
     * @param year  연도
     * @param month 월 (0부터 시작)
     * @param tasks 저장할 일정 데이터
     */
    public void saveLocalTasks(int year, int month, HashMap<String, List<ToDo>> tasks) {
        String fileName = String.format("%s/%d-%d.txt", userDataDir, year, month + 1);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            String targetMonth = String.format("%04d-%02d", year, month + 1);

            // 해당 월의 일정만 필터링하여 저장
            for (String dateKey : tasks.keySet()) {
                if (!dateKey.startsWith(targetMonth))
                    continue;

                List<ToDo> todoList = tasks.get(dateKey);
                if (todoList.isEmpty())
                    continue;

                writer.printf("Day %s:\n", dateKey);
                // 각 일정의 모든 속성을 파이프(|)로 구분하여 저장
                for (ToDo todo : todoList) {
                    writer.printf("%s|%s|%b|%d|%d|%d|%d|%d|%d|%d|%d|%d|%d|%s|%s|%s\n",
                            todo.getTaskName(), todo.getLocation(), todo.isAllDay(),
                            todo.getStartYear(), todo.getStartMonth(), todo.getStartDay(),
                            todo.getStartHour(), todo.getStartMinute(),
                            todo.getEndYear(), todo.getEndMonth(), todo.getEndDay(),
                            todo.getEndHour(), todo.getEndMinute(),
                            todo.getAlarm(), todo.getRepeat(), todo.getMemo());
                }
            }
        } catch (IOException e) {
            System.err.println("로컬 일정 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 개인 일정을 파일에서 로드
     * 
     * @param year  연도
     * @param month 월 (0부터 시작)
     * @return 로드된 일정 데이터
     */
    public HashMap<String, List<ToDo>> loadLocalTasks(int year, int month) {
        String fileName = String.format("%s/%d-%d.txt", userDataDir, year, month + 1);
        File file = new File(fileName);
        HashMap<String, List<ToDo>> tasks = new HashMap<>();

        if (!file.exists()) {
            return tasks;
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
                        parts[0], parts[1], Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]), Integer.parseInt(parts[8]),
                        Integer.parseInt(parts[9]), Integer.parseInt(parts[10]), Integer.parseInt(parts[11]),
                        Integer.parseInt(parts[12]), parts[13], parts[14], parts[15]);

                tasks.get(currentDateKey).add(todo);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("로컬 일정 로드 실패: " + e.getMessage());
        }

        return tasks;
    }

    /**
     * 공유 일정 캐시를 파일로 저장
     * 
     * @param sharedTasks 저장할 공유 일정 데이터
     */
    public void saveSharedCache(HashMap<String, List<SharedToDo>> sharedTasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(sharedCacheFile))) {
            for (String dateKey : sharedTasks.keySet()) {
                List<SharedToDo> todoList = sharedTasks.get(dateKey);
                if (todoList.isEmpty())
                    continue;

                writer.printf("SharedDay %s:\n", dateKey);
                // SharedToDo의 네트워크 문자열 형태로 저장
                for (SharedToDo todo : todoList) {
                    writer.println(todo.toNetworkString());
                }
            }
        } catch (IOException e) {
            System.err.println("공유 일정 캐시 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 공유 일정 캐시를 파일에서 로드
     * 
     * @return 로드된 공유 일정 데이터
     */
    public HashMap<String, List<SharedToDo>> loadSharedCache() {
        HashMap<String, List<SharedToDo>> sharedTasks = new HashMap<>();
        File file = new File(sharedCacheFile);

        if (!file.exists()) {
            return sharedTasks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentDateKey = null;

            while ((line = reader.readLine()) != null) {
                // 공유 일정 날짜 헤더 처리
                if (line.startsWith("SharedDay ")) {
                    currentDateKey = line.substring(10, line.length() - 1);
                    sharedTasks.putIfAbsent(currentDateKey, new ArrayList<>());
                    continue;
                }

                if (currentDateKey == null)
                    continue;

                // SharedToDo 객체로 변환
                SharedToDo todo = SharedToDo.fromCompleteNetworkString(line);
                if (todo != null) {
                    sharedTasks.get(currentDateKey).add(todo);
                }
            }
            System.out.println("공유 일정 캐시 로드 완료");
        } catch (IOException e) {
            System.err.println("공유 일정 캐시 로드 실패: " + e.getMessage());
        }

        return sharedTasks;
    }

    /**
     * 사용자 데이터 디렉터리 경로 반환
     * 
     * @return 데이터 디렉터리 경로
     */
    public String getUserDataDir() {
        return userDataDir;
    }
}