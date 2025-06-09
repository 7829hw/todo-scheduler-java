import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CalendarClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private OurCalendar calendar;
    private boolean connected = false;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public CalendarClient(OurCalendar calendar) {
        this.calendar = calendar;
    }

    public boolean connect(String nickname) {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            this.nickname = nickname;
            out.println(nickname);

            String response = in.readLine();
            if (response != null && response.startsWith("CONNECTED")) {
                connected = true;
                startMessageListener();
                System.out.println("서버 연결 성공: " + nickname);
                return true;
            }
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
            connected = false;
        }
        return false;
    }

    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                String message;
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
        listener.setDaemon(true);
        listener.start();
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2)
            return;

        String command = parts[0];
        String data = parts[1];

        switch (command) {
            case "CLEAR_SHARED_CACHE":

                calendar.clearSharedCache();
                System.out.println("공유 일정 캐시 초기화됨");
                break;
            case "NEW_TODO":

                SharedToDo newTodo = SharedToDo.fromCompleteNetworkString(data);
                if (newTodo != null && !newTodo.getCreator().equals(nickname)) {
                    calendar.addSharedTodo(newTodo, true);
                }
                break;
            case "EXISTING_TODO":

                SharedToDo existingTodo = SharedToDo.fromCompleteNetworkString(data);
                if (existingTodo != null) {
                    calendar.addSharedTodo(existingTodo, false);
                    System.out
                            .println("기존 공유 일정 로드: " + existingTodo.getTaskName() + " by " + existingTodo.getCreator());
                }
                break;
            case "UPDATE_TODO":

                SharedToDo updatedTodo = SharedToDo.fromCompleteNetworkString(data);
                if (updatedTodo != null) {
                    calendar.updateSharedTodo(updatedTodo);
                    System.out.println("공유 일정 업데이트 수신: " + updatedTodo.getTaskName() + " by " + updatedTodo.getCreator()
                            + " (ID: " + updatedTodo.getId() + ")");
                }
                break;
            case "DELETE_TODO":

                String todoId = data;
                calendar.deleteSharedTodo(todoId);
                System.out.println("공유 일정 삭제 수신: " + todoId);
                break;
        }
    }

    public void shareTask(SharedToDo sharedTodo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "SHARE_TODO|" + sharedTodo.toNetworkString();
        out.println(message);
        System.out.println("공유 일정 전송 (ID 포함): " + sharedTodo.getTaskName() + " (ID: " + sharedTodo.getId() + ")");
    }

    public void shareTask(ToDo todo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

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

    public void updateSharedTask(SharedToDo sharedTodo) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "UPDATE_TODO|" + sharedTodo.toNetworkString();
        out.println(message);
        System.out.println("공유 일정 업데이트 전송: " + sharedTodo.getTaskName());
    }

    public void deleteSharedTask(String todoId) {
        if (!connected || out == null) {
            System.err.println("서버에 연결되지 않음");
            return;
        }

        String message = "DELETE_TODO|" + todoId;
        out.println(message);
        System.out.println("공유 일정 삭제 전송: " + todoId);
    }

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

    public boolean isConnected() {
        return connected;
    }

    public String getNickname() {
        return nickname;
    }
}