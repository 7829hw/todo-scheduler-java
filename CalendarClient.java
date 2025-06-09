
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 캘린더 서버와 통신하는 클라이언트 클래스
 * 공유 일정의 생성, 수정, 삭제를 서버와 실시간 동기화함
 */

import java.io.*;
import java.net.*;

public class CalendarClient {
    private static final String SERVER_HOST = "localhost"; // 서버 호스트
    private static final int SERVER_PORT = 12345; // 서버 포트

    private Socket socket; // 서버 연결 소켓
    private BufferedReader in; // 입력 스트림
    private PrintWriter out; // 출력 스트림
    private String nickname; // 사용자 닉네임
    private OurCalendar calendar; // 캘린더 객체 참조
    private boolean connected = false; // 연결 상태

    /**
     * CalendarClient 생성자
     * 
     * @param calendar 연결할 캘린더 객체
     */
    public CalendarClient(OurCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * 서버에 연결 시도
     * 
     * @param nickname 사용자 닉네임
     * @return 연결 성공 여부
     */
    public boolean connect(String nickname) {
        try {
            // 서버에 소켓 연결
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            this.nickname = nickname;
            // 서버에 닉네임 전송
            out.println(nickname);

            // 서버 응답 확인
            String response = in.readLine();
            if (response != null && response.startsWith("CONNECTED")) {
                connected = true;
                startMessageListener(); // 메시지 수신 스레드 시작
                System.out.println("서버 연결 성공: " + nickname);
                return true;
            }
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
            connected = false;
        }
        return false;
    }

    /**
     * 서버로부터 메시지를 수신하는 백그라운드 스레드 시작
     */
    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                String message;
                // 연결이 유지되는 동안 메시지 수신
                while (connected && (message = in.readLine()) != null) {
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("서버 연결 끊어짐: " + e.getMessage());
                    connected = false;
                }
            }
        });
        listener.setDaemon(true); // 데몬 스레드로 설정
        listener.start();
    }

    /**
     * 서버로부터 받은 메시지 처리
     * 
     * @param message 서버 메시지
     */
    private void handleServerMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2)
            return;

        String command = parts[0];
        String data = parts[1];

        switch (command) {
            case "CLEAR_SHARED_CACHE":
                // 공유 캐시 초기화 명령
                calendar.clearSharedCache();
                System.out.println("공유 일정 캐시 초기화됨");
                break;
            case "NEW_TODO":
                // 새 공유 일정 수신
                SharedToDo newTodo = SharedToDo.fromCompleteNetworkString(data);
                if (newTodo != null && !newTodo.getCreator().equals(nickname)) {
                    calendar.addSharedTodo(newTodo, true);
                }
                break;
            case "EXISTING_TODO":
                // 기존 공유 일정 수신 (연결 시 서버에서 전송)
                SharedToDo existingTodo = SharedToDo.fromCompleteNetworkString(data);
                if (existingTodo != null) {
                    calendar.addSharedTodo(existingTodo, false);
                    System.out
                            .println("기존 공유 일정 로드: " + existingTodo.getTaskName() + " by " + existingTodo.getCreator());
                }
                break;
            case "UPDATE_TODO":
                // 공유 일정 업데이트 수신
                SharedToDo updatedTodo = SharedToDo.fromCompleteNetworkString(data);
                if (updatedTodo != null) {
                    calendar.updateSharedTodo(updatedTodo);
                    System.out.println("공유 일정 업데이트 수신: " + updatedTodo.getTaskName() + " by " + updatedTodo.getCreator()
                            + " (ID: " + updatedTodo.getId() + ")");
                }
                break;
            case "DELETE_TODO":
                // 공유 일정 삭제 수신
                String todoId = data;
                calendar.deleteSharedTodo(todoId);
                System.out.println("공유 일정 삭제 수신: " + todoId);
                break;
        }
    }

    /**
     * SharedToDo 객체를 서버에 공유
     * 
     * @param sharedTodo 공유할 일정 객체
     */
    public void shareTask(SharedToDo sharedTodo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "SHARE_TODO|" + sharedTodo.toNetworkString();
        out.println(message);
        System.out.println("공유 일정 전송 (ID 포함): " + sharedTodo.getTaskName() + " (ID: " + sharedTodo.getId() + ")");
    }

    /**
     * ToDo 객체를 서버에 공유 (SharedToDo로 변환)
     * 
     * @param todo 공유할 일정 객체
     */
    public void shareTask(ToDo todo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        // ToDo 객체의 속성을 문자열로 직렬화
        String message = "SHARE_TODO|" + String.format("%s|%s|%b|%d|%d|%d|%d|%d|%d|%d|%d|%d|%d|%s|%s|%s",
                todo.getTaskName(), todo.getLocation(), todo.isAllDay(),
                todo.getStartYear(), todo.getStartMonth(), todo.getStartDay(),
                todo.getStartHour(), todo.getStartMinute(),
                todo.getEndYear(), todo.getEndMonth(), todo.getEndDay(),
                todo.getEndHour(), todo.getEndMinute(),
                todo.getAlarm(), todo.getRepeat(), todo.getMemo());

        out.println(message);
        System.out.println("공유 일정 전송: " + todo.getTaskName());
    }

    /**
     * 공유 일정 업데이트를 서버에 전송
     * 
     * @param sharedTodo 업데이트할 공유 일정
     */
    public void updateSharedTask(SharedToDo sharedTodo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "UPDATE_TODO|" + sharedTodo.toNetworkString();
        out.println(message);
        System.out.println("공유 일정 업데이트 전송: " + sharedTodo.getTaskName());
    }

    /**
     * 공유 일정 삭제를 서버에 전송
     * 
     * @param todoId 삭제할 일정 ID
     */
    public void deleteSharedTask(String todoId) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "DELETE_TODO|" + todoId;
        out.println(message);
        System.out.println("공유 일정 삭제 전송: " + todoId);
    }

    /**
     * 서버 연결 해제
     */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("연결 해제 오류: " + e.getMessage());
        }
    }

    /**
     * 연결 상태 확인
     * 
     * @return 연결 여부
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 닉네임 반환
     * 
     * @return 사용자 닉네임
     */
    public String getNickname() {
        return nickname;
    }
}