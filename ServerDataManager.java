
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 서버측 공유 일정 데이터 관리 클래스
 * 서버의 공유 일정 데이터를 파일로 저장하고 로드하는 기능을 제공함
 */

import java.io.*;
import java.util.*;

public class ServerDataManager {
    private static final String SERVER_DATA_DIR = "server_data"; // 서버 데이터 디렉터리
    private static final String SHARED_TODOS_FILE = SERVER_DATA_DIR + "/shared_todos.txt"; // 공유 일정 파일

    /**
     * 공유 일정 목록을 파일로 저장
     * 
     * @param sharedTodos 저장할 공유 일정 목록
     */
    public static void saveSharedTodos(List<SharedToDo> sharedTodos) {
        // 서버 데이터 디렉터리 생성
        File dataDir = new File(SERVER_DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(SHARED_TODOS_FILE))) {
            // 각 공유 일정을 네트워크 문자열 형태로 저장
            for (SharedToDo todo : sharedTodos) {
                writer.println(todo.toNetworkString());
            }
            System.out.println("공유 일정 " + sharedTodos.size() + "개 저장 완료");
        } catch (IOException e) {
            System.err.println("공유 일정 저장 실패: " + e.getMessage());
        }
    }

    /**
     * 파일에서 공유 일정 목록을 로드
     * 
     * @return 로드된 공유 일정 목록
     */
    public static List<SharedToDo> loadSharedTodos() {
        List<SharedToDo> sharedTodos = new ArrayList<>();
        File file = new File(SHARED_TODOS_FILE);

        if (!file.exists()) {
            System.out.println("공유 일정 파일이 없습니다. 새로 시작합니다.");
            return sharedTodos; // 빈 리스트 반환
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // 파일의 각 라인을 SharedToDo 객체로 변환
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