import java.time.*;
import java.util.*;
import javax.swing.*;

public class ReminderService {
    private Set<String> notifiedSet = new HashSet<>();

    public void checkReminders(OurCalendar calendar) {
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, List<ToDo>> entry : calendar.tasks.entrySet()) {
            for (ToDo task : entry.getValue()) {
                LocalDateTime taskTime = LocalDateTime.of(
                        task.getStartYear(),
                        task.getStartMonth() + 1,
                        task.getStartDay(),
                        task.getStartHour(),
                        task.getStartMinute()
                );

                String alarmType = task.getAlarm();
                LocalDateTime notifyTime = taskTime;

                // 알림 시간 계산
                switch (alarmType) {
                    case "10분 전": notifyTime = taskTime.minusMinutes(10); break;
                    case "30분 전": notifyTime = taskTime.minusMinutes(30); break;
                    case "1시간 전": notifyTime = taskTime.minusHours(1); break;
                    case "하루 전": notifyTime = taskTime.minusDays(1); break;
                    case "없음":
                    default: notifyTime = taskTime; break; // 일정 시작 시간에 알림
                }

                String key = entry.getKey() + "_" + task.getTaskName() + "_" + alarmType;

                // 알림 시각에 도달했고 아직 알림을 보내지 않은 경우
                long diff = Duration.between(now, notifyTime).toMinutes();
                if (Math.abs(diff) <= 1 && !notifiedSet.contains(key)) {
                    notifiedSet.add(key);

                    String message = String.format("🔔 알림 (%s): %s\n시작: %02d:%02d",
                            alarmType.equals("없음") ? "정시 알림" : alarmType,
                            task.getTaskName(),
                            task.getStartHour(),
                            task.getStartMinute());

                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null, message, "일정 알림", JOptionPane.INFORMATION_MESSAGE)
                    );
                }
            }
        }
    }
}
