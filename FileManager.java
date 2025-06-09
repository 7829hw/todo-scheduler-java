import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String DATA_DIR = "data";

    public static void saveToFile(int year, int month, HashMap<String, List<ToDo>> tasks) {

        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        String fileName = String.format("%s/%d-%d.txt", DATA_DIR, year, month + 1);

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

    public static HashMap<String, List<ToDo>> loadFromFile(int year, int month) {

        String fileName = String.format("%s/%d-%d.txt", DATA_DIR, year, month + 1);
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
                        parts[0],
                        parts[1],
                        Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7]),
                        Integer.parseInt(parts[8]),
                        Integer.parseInt(parts[9]),
                        Integer.parseInt(parts[10]),
                        Integer.parseInt(parts[11]),
                        Integer.parseInt(parts[12]),
                        parts[13],
                        parts[14],
                        parts[15]);

                tasks.get(currentDateKey).add(todo);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}