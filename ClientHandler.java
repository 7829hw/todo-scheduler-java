
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 서버에서 개별 클라이언트 연결을 처리하는 핸들러 클래스
 * 각 클라이언트의 요청을 처리하고 서버의 공유 일정 관리 기능을 호출함
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket socket; // 클라이언트 소켓
    private BufferedReader in; // 입력 스트림
    private PrintWriter out; // 출력 스트림
    private String nickname; // 클라이언트 닉네임

    /**
     * ClientHandler 생성자
     * 
     * @param socket 클라이언트 소켓
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            // 입출력 스트림 초기화
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("클라이언트 핸들러 생성 오류: " + e.getMessage());
        }
    }

    /**
     * 클라이언트 처리 메인 루프
     */
    @Override
    public void run() {
        try {
            // 첫 번째 메시지로 닉네임 수신
            nickname = in.readLine();
            if (nickname != null && !nickname.trim().isEmpty()) {
                // 서버에 클라이언트 등록
                CalendarServer.addClient(nickname, this);
                out.println("CONNECTED|" + nickname);

                // 기존 공유 일정을 새 클라이언트에게 전송
                for (SharedToDo todo : CalendarServer.getSharedTodos()) {
                    out.println("EXISTING_TODO|" + todo.toNetworkString());
                }
            }

            // 클라이언트 메시지 처리 루프
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("클라이언트 처리 오류: " + e.getMessage());
        } finally {
            // 연결 정리
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

    /**
     * 클라이언트로부터 받은 메시지 처리
     * 
     * @param message 클라이언트 메시지
     */
    private void handleMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2)
            return;

        String command = parts[0];
        String data = parts[1];

        switch (command) {
            case "SHARE_TODO":
                // 공유 일정 추가 처리
                SharedToDo todo = null;
                String[] dataParts = data.split("\\|");

                if (dataParts.length >= 18) {
                    // 완전한 SharedToDo 데이터 (ID 포함)
                    todo = SharedToDo.fromCompleteNetworkString(data);
                    System.out.println("완전한 공유 일정 수신: " + (todo != null ? todo.getId() : "null"));
                } else {
                    // 기본 ToDo 데이터를 SharedToDo로 변환
                    todo = SharedToDo.fromNetworkString(data, nickname);
                    System.out.println("기본 공유 일정 생성: " + (todo != null ? todo.getId() : "null"));
                }

                if (todo != null) {
                    CalendarServer.addSharedTodo(todo);
                }
                break;
            case "UPDATE_TODO":
                // 공유 일정 업데이트 처리
                SharedToDo updatedTodo = SharedToDo.fromCompleteNetworkString(data);
                if (updatedTodo != null && updatedTodo.getCreator().equals(nickname)) {
                    // 생성자 본인만 수정 가능
                    System.out.println("공유 일정 업데이트 요청: " + updatedTodo.getId() + " by " + nickname);
                    CalendarServer.updateSharedTodo(updatedTodo);
                } else if (updatedTodo == null) {
                    System.err.println("공유 일정 업데이트 파싱 실패: " + data);
                } else {
                    System.err.println("권한 없음 - 요청자: " + nickname + ", 생성자: " + updatedTodo.getCreator());
                }
                break;
            case "DELETE_TODO":
                // 공유 일정 삭제 처리
                String todoId = data;
                CalendarServer.deleteSharedTodo(todoId, nickname);
                break;
        }
    }

    /**
     * 클라이언트에게 메시지 전송
     * 
     * @param message 전송할 메시지
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}