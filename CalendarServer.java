
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 캘린더 서버 메인 클래스 - 여러 클라이언트의 공유 일정을 관리하고 실시간 동기화
 * 공유 일정의 생성, 수정, 삭제를 모든 연결된 클라이언트에게 브로드캐스트함
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CalendarServer {
    private static final int PORT = 12345; // 서버 포트
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>(); // 연결된 클라이언트들
    private static List<SharedToDo> sharedTodos = Collections.synchronizedList(new ArrayList<>()); // 공유 일정 목록

    /**
     * 서버 메인 실행 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        System.out.println("📅 캘린더 서버 시작 - 포트: " + PORT);

        // 서버 데이터 로드
        sharedTodos = Collections.synchronizedList(ServerDataManager.loadSharedTodos());

        // 종료 훅 등록 - 서버 종료 시 데이터 저장
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("서버 종료 중... 데이터 저장");
            ServerDataManager.saveSharedTodos(sharedTodos);
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // 클라이언트 연결 대기 루프
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start(); // 각 클라이언트를 별도 스레드에서 처리
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
    }

    /**
     * 새 클라이언트를 서버에 등록
     * 
     * @param nickname 클라이언트 닉네임
     * @param handler  클라이언트 핸들러
     */
    public static void addClient(String nickname, ClientHandler handler) {
        clients.put(nickname, handler);
        System.out.println("클라이언트 연결: " + nickname + " (총 " + clients.size() + "명)");
    }

    /**
     * 클라이언트를 서버에서 제거
     * 
     * @param nickname 제거할 클라이언트 닉네임
     */
    public static void removeClient(String nickname) {
        clients.remove(nickname);
        System.out.println("클라이언트 연결 해제: " + nickname + " (총 " + clients.size() + "명)");
    }

    /**
     * 새 공유 일정을 추가하고 모든 클라이언트에게 브로드캐스트
     * 
     * @param todo 추가할 공유 일정
     */
    public static void addSharedTodo(SharedToDo todo) {
        sharedTodos.add(todo);
        ServerDataManager.saveSharedTodos(sharedTodos); // 즉시 저장
        broadcastNewTodo(todo);
    }

    /**
     * 새 공유 일정을 모든 클라이언트에게 브로드캐스트
     * 
     * @param todo 브로드캐스트할 일정
     */
    public static void broadcastNewTodo(SharedToDo todo) {
        String message = "NEW_TODO|" + todo.toNetworkString();
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 전송: " + todo.getTaskName() + " by " + todo.getCreator());
    }

    /**
     * 공유 일정을 업데이트하고 모든 클라이언트에게 브로드캐스트
     * 
     * @param updatedTodo 업데이트된 일정
     */
    public static void updateSharedTodo(SharedToDo updatedTodo) {
        System.out.println("공유 일정 업데이트 요청: " + updatedTodo.getId() + " - " + updatedTodo.getTaskName());

        // ID로 기존 일정 찾아서 교체
        for (int i = 0; i < sharedTodos.size(); i++) {
            if (sharedTodos.get(i).getId().equals(updatedTodo.getId())) {
                System.out.println("기존 일정 찾음: " + sharedTodos.get(i).getId() + " -> " + updatedTodo.getId());
                sharedTodos.set(i, updatedTodo);
                ServerDataManager.saveSharedTodos(sharedTodos);
                broadcastUpdatedTodo(updatedTodo);
                System.out.println("공유 일정 업데이트 완료: " + updatedTodo.getTaskName() + " by " + updatedTodo.getCreator());
                return;
            }
        }

        // 업데이트할 일정을 찾지 못한 경우 디버그 정보 출력
        System.err.println("업데이트할 공유 일정을 찾을 수 없음: " + updatedTodo.getId());
        System.out.println("현재 공유 일정 목록:");
        for (SharedToDo todo : sharedTodos) {
            System.out.println("  - ID: " + todo.getId() + ", 제목: " + todo.getTaskName());
        }
    }

    /**
     * 업데이트된 공유 일정을 모든 클라이언트에게 브로드캐스트
     * 
     * @param todo 업데이트된 일정
     */
    public static void broadcastUpdatedTodo(SharedToDo todo) {
        String message = "UPDATE_TODO|" + todo.toNetworkString();
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 업데이트 전송: " + todo.getTaskName() + " by " + todo.getCreator());
    }

    /**
     * 공유 일정을 삭제하고 모든 클라이언트에게 브로드캐스트
     * 
     * @param todoId            삭제할 일정 ID
     * @param requesterNickname 삭제 요청자 닉네임 (권한 확인용)
     */
    public static void deleteSharedTodo(String todoId, String requesterNickname) {
        for (int i = 0; i < sharedTodos.size(); i++) {
            SharedToDo todo = sharedTodos.get(i);
            // ID와 생성자가 일치하는 경우에만 삭제 (권한 확인)
            if (todo.getId().equals(todoId) && todo.getCreator().equals(requesterNickname)) {
                sharedTodos.remove(i);
                ServerDataManager.saveSharedTodos(sharedTodos);
                broadcastDeletedTodo(todoId);
                System.out.println("공유 일정 삭제: " + todo.getTaskName() + " by " + todo.getCreator());
                return;
            }
        }
        System.out.println("삭제할 공유 일정을 찾을 수 없음: " + todoId + " by " + requesterNickname);
    }

    /**
     * 삭제된 공유 일정을 모든 클라이언트에게 브로드캐스트
     * 
     * @param todoId 삭제된 일정 ID
     */
    public static void broadcastDeletedTodo(String todoId) {
        String message = "DELETE_TODO|" + todoId;
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        System.out.println("공유 일정 삭제 전송: " + todoId);
    }

    /**
     * 현재 서버의 모든 공유 일정 목록 반환
     * 
     * @return 공유 일정 목록 복사본
     */
    public static List<SharedToDo> getSharedTodos() {
        return new ArrayList<>(sharedTodos);
    }
}