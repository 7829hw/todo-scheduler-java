import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

            nickname = in.readLine();
            if (nickname != null && !nickname.trim().isEmpty()) {
                CalendarServer.addClient(nickname, this);
                out.println("CONNECTED|" + nickname);

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

                SharedToDo todo = null;
                String[] dataParts = data.split("\\|");

                if (dataParts.length >= 18) {

                    todo = SharedToDo.fromCompleteNetworkString(data);
                    System.out.println("완전한 공유 일정 수신: " + (todo != null ? todo.getId() : "null"));
                } else {

                    todo = SharedToDo.fromNetworkString(data, nickname);
                    System.out.println("기본 공유 일정 생성: " + (todo != null ? todo.getId() : "null"));
                }

                if (todo != null) {
                    CalendarServer.addSharedTodo(todo);
                }
                break;
            case "UPDATE_TODO":

                SharedToDo updatedTodo = SharedToDo.fromCompleteNetworkString(data);
                if (updatedTodo != null && updatedTodo.getCreator().equals(nickname)) {

                    System.out.println("공유 일정 업데이트 요청: " + updatedTodo.getId() + " by " + nickname);
                    CalendarServer.updateSharedTodo(updatedTodo);
                } else if (updatedTodo == null) {
                    System.err.println("공유 일정 업데이트 파싱 실패: " + data);
                } else {
                    System.err.println("권한 없음 - 요청자: " + nickname + ", 생성자: " + updatedTodo.getCreator());
                }
                break;
            case "DELETE_TODO":

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