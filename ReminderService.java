import java.time.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
                        task.getStartMinute());

                String alarmType = task.getAlarm();
                LocalDateTime notifyTime = taskTime;

                // 알림 시간 계산
                switch (alarmType) {
                    case "10분 전":
                        notifyTime = taskTime.minusMinutes(10);
                        break;
                    case "30분 전":
                        notifyTime = taskTime.minusMinutes(30);
                        break;
                    case "1시간 전":
                        notifyTime = taskTime.minusHours(1);
                        break;
                    case "하루 전":
                        notifyTime = taskTime.minusDays(1);
                        break;
                    case "없음":
                    default:
                        notifyTime = taskTime;
                        break;
                }

                String key = entry.getKey() + "_" + task.getTaskName() + "_" + alarmType;

                // 알림 시각에 도달했고 아직 알림을 보내지 않은 경우
                long diff = Duration.between(now, notifyTime).toMinutes();
                if (Math.abs(diff) <= 1 && !notifiedSet.contains(key)) {
                    notifiedSet.add(key);
                    showModernNotification(task, alarmType);
                }
            }
        }
    }

    // 모던한 알림 다이얼로그 표시
    private void showModernNotification(ToDo task, String alarmType) {
        SwingUtilities.invokeLater(() -> {
            JDialog notification = new JDialog();
            notification.setTitle("📅 일정 알림");
            notification.setSize(350, 200);
            notification.setLocationRelativeTo(null);
            notification.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            notification.getContentPane().setBackground(Color.WHITE);

            // 메인 패널
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            mainPanel.setBackground(Color.WHITE);

            // 아이콘과 제목
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.setBackground(Color.WHITE);

            JLabel iconLabel = new JLabel("🔔");
            iconLabel.setFont(new Font("Apple Color Emoji", Font.PLAIN, 24));

            JLabel titleLabel = new JLabel("일정 알림");
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
            titleLabel.setForeground(new Color(33, 37, 41));

            headerPanel.add(iconLabel);
            headerPanel.add(Box.createHorizontalStrut(10));
            headerPanel.add(titleLabel);

            // 알림 유형
            JLabel typeLabel = new JLabel(alarmType.equals("없음") ? "정시 알림" : alarmType);
            typeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            typeLabel.setForeground(new Color(108, 117, 125));
            typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 일정 이름
            JLabel eventLabel = new JLabel(task.getTaskName());
            eventLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            eventLabel.setForeground(new Color(64, 128, 255));
            eventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 시간 정보
            String timeInfo = String.format("시작: %02d:%02d",
                    task.getStartHour(), task.getStartMinute());
            JLabel timeLabel = new JLabel(timeInfo);
            timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            timeLabel.setForeground(new Color(108, 117, 125));
            timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 확인 버튼
            JButton okButton = new JButton("확인");
            okButton.setBackground(new Color(64, 128, 255));
            okButton.setForeground(Color.WHITE);
            okButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            okButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            okButton.setFocusPainted(false);
            okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            okButton.addActionListener(e -> notification.dispose());

            // 컴포넌트 추가
            mainPanel.add(headerPanel);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(typeLabel);
            mainPanel.add(Box.createVerticalStrut(15));
            mainPanel.add(eventLabel);
            mainPanel.add(Box.createVerticalStrut(5));
            mainPanel.add(timeLabel);
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(okButton);

            notification.add(mainPanel);
            notification.setVisible(true);

            // 3초 후 자동 닫기
            javax.swing.Timer autoClose = new javax.swing.Timer(3000, e -> notification.dispose());
            autoClose.setRepeats(false);
            autoClose.start();
        });
    }
}