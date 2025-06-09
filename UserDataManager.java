import java.io.*;
import java.util.*;

public class UserDataManager {
    private String nickname;
    private String userDataDir;
    private String sharedCacheFile;

    public UserDataManager(String nickname) {
        this.nickname = nickname;
        this.userDataDir = "data_" + nickname.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        this.sharedCacheFile = userDataDir + "/shared_cache.txt";

        File dir = new File(userDataDir);
        if (!dir.exists()) {
            dir.mkdir();
            System.out.println("사용자 데이터 폴더 생성: " + userDataDir);
        }
    }

    public void saveLocalTasks(int year, int month, HashMap<String, List<ToDo>> tasks) {
        String fileName = String.format("%s/%d-%d.txt", userDataDir, year, month + 1);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            String targetMonth = String.format("%04d-%02d", year, month + 1);
            for (String dateKey : tasks.keySet()) {
                if (!dateKey.startsWith(targetMonth))
                    continue;

                List<ToDo> todoList = tasks.get(dateKey);
                if (todoList.isEmpty())
                    continue;

                writer.printf("Day %s:\n", dateKey);
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
                if (line.startsWith("Day ")) {
                    currentDateKey = line.substring(4, line.length() - 1);
                    tasks.putIfAbsent(currentDateKey, new ArrayList<>());
                    continue;
                }

                if (currentDateKey == null)
                    continue;

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

    public void saveSharedCache(HashMap<String, List<SharedToDo>> sharedTasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(sharedCacheFile))) {
            for (String dateKey : sharedTasks.keySet()) {
                List<SharedToDo> todoList = sharedTasks.get(dateKey);
                if (todoList.isEmpty())
                    continue;

                writer.printf("SharedDay %s:\n", dateKey);
                for (SharedToDo todo : todoList) {
                    writer.println(todo.toNetworkString());
                }
            }
        } catch (IOException e) {
            System.err.println("공유 일정 캐시 저장 실패: " + e.getMessage());
        }
    }

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
                if (line.startsWith("SharedDay ")) {
                    currentDateKey = line.substring(10, line.length() - 1);
                    sharedTasks.putIfAbsent(currentDateKey, new ArrayList<>());
                    continue;
                }

                if (currentDateKey == null)
                    continue;

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

    public String getUserDataDir() {
        return userDataDir;
    }
}