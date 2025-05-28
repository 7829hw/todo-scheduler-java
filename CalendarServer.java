import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 멀티스레드 캘린더 서버
 * 포트 12345에서 클라이언트 연결 대기
 */
public class CalendarServer {
    private static final int PORT = 12345;
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static List<SharedToDo> sharedTodos = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("📅 캘린더 서버 시작 - 포트: " + PORT);

        // 기존 공유 일정 로드
        sharedTodos = Collections.synchronizedList(ServerDataManager.loadSharedTodos());

        // 서버 종료 시 데이터 저장을 위한 셧다운 훅
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("서버 종료 중... 데이터 저장");
            ServerDataManager.saveSharedTodos(sharedTodos);
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
    }

    public static void addClient(String nickname, ClientHandler handler) {
        clients.put(nickname, handler);
        System.out.println("클라이언트 연결: " + nickname + " (총 " + clients.size() + "명)");
    }

    public static void removeClient(String nickname) {
        clients.remove(nickname);
        System.out.println("클라이언트 연결 해제: " + nickname + " (총 " + clients.size() + "명)");
    }

    public static void addSharedTodo(SharedToDo todo) {
        sharedTodos.add(todo);
        ServerDataManager.saveSharedTodos(sharedTodos); // 즉시 저장
        broadcastNewTodo(todo);
    }

    public static void broadcastNewTodo(SharedToDo todo) {
        String message = "NEW_TODO|" + todo.toNetworkString();
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 전송: " + todo.getTaskName() + " by " + todo.getCreator());
    }

    public static void updateSharedTodo(SharedToDo updatedTodo) {
        System.out.println("공유 일정 업데이트 요청: " + updatedTodo.getId() + " - " + updatedTodo.getTaskName());

        for (int i = 0; i < sharedTodos.size(); i++) {
            if (sharedTodos.get(i).getId().equals(updatedTodo.getId())) {
                System.out.println("기존 일정 찾음: " + sharedTodos.get(i).getId() + " -> " + updatedTodo.getId());
                sharedTodos.set(i, updatedTodo);
                ServerDataManager.saveSharedTodos(sharedTodos); // 즉시 저장
                broadcastUpdatedTodo(updatedTodo);
                System.out.println("공유 일정 업데이트 완료: " + updatedTodo.getTaskName() + " by " + updatedTodo.getCreator());
                return;
            }
        }

        System.err.println("업데이트할 공유 일정을 찾을 수 없음: " + updatedTodo.getId());
        System.out.println("현재 공유 일정 목록:");
        for (SharedToDo todo : sharedTodos) {
            System.out.println("  - ID: " + todo.getId() + ", 제목: " + todo.getTaskName());
        }
    }

    public static void broadcastUpdatedTodo(SharedToDo todo) {
        String message = "UPDATE_TODO|" + todo.toNetworkString();
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 업데이트 전송: " + todo.getTaskName() + " by " + todo.getCreator());
    }

    public static void deleteSharedTodo(String todoId, String requesterNickname) {
        for (int i = 0; i < sharedTodos.size(); i++) {
            SharedToDo todo = sharedTodos.get(i);
            if (todo.getId().equals(todoId) && todo.getCreator().equals(requesterNickname)) {
                sharedTodos.remove(i);
                ServerDataManager.saveSharedTodos(sharedTodos); // 즉시 저장
                broadcastDeletedTodo(todoId);
                System.out.println("공유 일정 삭제: " + todo.getTaskName() + " by " + todo.getCreator());
                return;
            }
        }
        System.out.println("삭제할 공유 일정을 찾을 수 없음: " + todoId + " by " + requesterNickname);
    }

    public static void broadcastDeletedTodo(String todoId) {
        String message = "DELETE_TODO|" + todoId;
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 삭제 전송: " + todoId);
    }

    public static List<SharedToDo> getSharedTodos() {
        return new ArrayList<>(sharedTodos);
    }
}

/**
 * 각 클라이언트 연결을 처리하는 핸들러
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("클라이언트 핸들러 생성 오류: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // 닉네임 받기
            nickname = in.readLine();
            if (nickname != null && !nickname.trim().isEmpty()) {
                CalendarServer.addClient(nickname, this);
                out.println("CONNECTED|" + nickname);

                // 기존 공유 일정들 전송
                for (SharedToDo todo : CalendarServer.getSharedTodos()) {
                    out.println("EXISTING_TODO|" + todo.toNetworkString());
                }
            }

            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("클라이언트 처리 오류: " + e.getMessage());
        } finally {
            if (nickname != null) {
                CalendarServer.removeClient(nickname);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("소켓 종료 오류: " + e.getMessage());
            }
        }
    }

    private void handleMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2)
            return;

        String command = parts[0];
        String data = parts[1];

        switch (command) {
            case "SHARE_TODO":
                // 공유 일정 생성 - ID 포함된 완전한 데이터로 파싱 시도
                SharedToDo todo = null;
                String[] dataParts = data.split("\\|");

                if (dataParts.length >= 18) {
                    // 완전한 SharedToDo 데이터 (ID 포함)
                    todo = SharedToDo.fromCompleteNetworkString(data);
                    System.out.println("완전한 공유 일정 수신: " + (todo != null ? todo.getId() : "null"));
                } else {
                    // 기존 ToDo 데이터 (ID 없음)
                    todo = SharedToDo.fromNetworkString(data, nickname);
                    System.out.println("기본 공유 일정 생성: " + (todo != null ? todo.getId() : "null"));
                }

                if (todo != null) {
                    CalendarServer.addSharedTodo(todo);
                }
                break;
            case "UPDATE_TODO":
                // 공유 일정 업데이트 - 완전한 데이터 (ID 포함)로 파싱
                SharedToDo updatedTodo = SharedToDo.fromCompleteNetworkString(data);
                if (updatedTodo != null && updatedTodo.getCreator().equals(nickname)) {
                    // 본인이 만든 일정만 수정 가능
                    System.out.println("공유 일정 업데이트 요청: " + updatedTodo.getId() + " by " + nickname);
                    CalendarServer.updateSharedTodo(updatedTodo);
                } else if (updatedTodo == null) {
                    System.err.println("공유 일정 업데이트 파싱 실패: " + data);
                } else {
                    System.err.println("권한 없음 - 요청자: " + nickname + ", 생성자: " + updatedTodo.getCreator());
                }
                break;
            case "DELETE_TODO":
                // 공유 일정 삭제
                String todoId = data;
                CalendarServer.deleteSharedTodo(todoId, nickname);
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}