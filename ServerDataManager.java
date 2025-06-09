import java.io.*;
import java.util.*;

public class ServerDataManager {
    private static final String SERVER_DATA_DIR = "server_data";
    private static final String SHARED_TODOS_FILE = SERVER_DATA_DIR + "/shared_todos.txt";

    public static void saveSharedTodos(List<SharedToDo> sharedTodos) {

        File dataDir = new File(SERVER_DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(SHARED_TODOS_FILE))) {
            for (SharedToDo todo : sharedTodos) {
                writer.println(todo.toNetworkString());
            }
            System.out.println("공유 일정 " + sharedTodos.size() + "개 저장 완료");
        } catch (IOException e) {
            System.err.println("공유 일정 저장 실패: " + e.getMessage());
        }
    }

    public static List<SharedToDo> loadSharedTodos() {
        List<SharedToDo> sharedTodos = new ArrayList<>();
        File file = new File(SHARED_TODOS_FILE);

        if (!file.exists()) {
            System.out.println("공유 일정 파일이 없습니다. 새로 시작합니다.");
            return sharedTodos;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SharedToDo todo = SharedToDo.fromCompleteNetworkString(line);
                if (todo != null) {
                    sharedTodos.add(todo);
                }
            }
            System.out.println("공유 일정 " + sharedTodos.size() + "개 로드 완료");
        } catch (IOException e) {
            System.err.println("공유 일정 로드 실패: " + e.getMessage());
        }

        return sharedTodos;
    }
}