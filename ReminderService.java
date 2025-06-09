
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 일정 알림 서비스 클래스 - 등록된 일정의 알림 시간을 체크하고 팝업 알림을 표시
 * 설정된 알림 옵션에 따라 지정된 시간 전에 알림을 띄워줌
 */

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import java.awt.*;

public class ReminderService {
    private Set<String> notifiedSet = new HashSet<>(); // 이미 알림을 보낸 일정들의 키를 저장

    /**
     * 모든 일정의 알림 시간을 체크하고 필요시 알림 표시
     * 
     * @param calendar 체크할 캘린더 객체
     */
    public void checkReminders(OurCalendar calendar) {
        LocalDateTime now = LocalDateTime.now(); // 현재 시간

        // 모든 일정을 순회하며 알림 체크
        for (Map.Entry<String, List<ToDo>> entry : calendar.tasks.entrySet()) {
            for (ToDo task : entry.getValue()) {
                // 일정의 시작 시간 계산
                LocalDateTime taskTime = LocalDateTime.of(
                        task.getStartYear(),
                        task.getStartMonth() + 1, // 월은 0부터 시작하므로 +1
                        task.getStartDay(),
                        task.getStartHour(),
                        task.getStartMinute());

                String alarmType = task.getAlarm();
                LocalDateTime notifyTime = taskTime; // 기본값은 시작 시간

                // 알림 설정에 따른 알림 시간 계산
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
                        notifyTime = taskTime; // 정시 알림
                        break;
                }

                // 중복 알림 방지를 위한 고유 키 생성
                String key = entry.getKey() + "_" + task.getTaskName()
                        + "_" + task.getStartHour()
                        + "_" + task.getStartMinute()
                        + "_" + alarmType;

                // 현재 시간과 알림 시간의 차이 계산 (분 단위)
                long diff = Duration.between(now, notifyTime).toMinutes();

                // 알림 시간이 되었고 아직 알림을 보내지 않은 경우
                if (Math.abs(diff) <= 1 && !notifiedSet.contains(key)) {
                    notifiedSet.add(key); // 알림 보냄 표시
                    showModernNotification(task, alarmType);
                }
            }
        }
    }

    /**
     * 모던 스타일의 알림 팝업 표시
     * 
     * @param task      알림을 표시할 일정
     * @param alarmType 알림 타입
     */
    private void showModernNotification(ToDo task, String alarmType) {
        SwingUtilities.invokeLater(() -> {
            // 알림 다이얼로그 생성
            JDialog notification = new JDialog();
            notification.setTitle("📅 일정 알림");
            notification.setSize(350, 200);
            notification.setLocationRelativeTo(null);
            notification.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            notification.getContentPane().setBackground(Color.WHITE);

            // 메인 패널 구성
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            mainPanel.setBackground(Color.WHITE);

            // 헤더 패널 (아이콘 + 제목)
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            headerPanel.setBackground(Color.WHITE);

            JLabel iconLabel = new JLabel("🔔");
            iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));

            JLabel titleLabel = new JLabel("일정 알림");
            titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            titleLabel.setForeground(new Color(33, 37, 41));

            headerPanel.add(iconLabel);
            headerPanel.add(Box.createHorizontalStrut(10));
            headerPanel.add(titleLabel);

            // 알림 타입 레이블
            JLabel typeLabel = new JLabel(alarmType.equals("없음") ? "정시 알림" : alarmType);
            typeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            typeLabel.setForeground(new Color(108, 117, 125));
            typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 일정명 레이블
            JLabel eventLabel = new JLabel(task.getTaskName());
            eventLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            eventLabel.setForeground(new Color(64, 128, 255));
            eventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 시간 정보 레이블
            String timeInfo = String.format("시작: %02d:%02d",
                    task.getStartHour(), task.getStartMinute());
            JLabel timeLabel = new JLabel(timeInfo);
            timeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            timeLabel.setForeground(new Color(108, 117, 125));
            timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 확인 버튼
            JButton okButton = new JButton("확인");
            okButton.setBackground(new Color(64, 128, 255));
            okButton.setForeground(Color.WHITE);
            okButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            okButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            okButton.setFocusPainted(false);
            okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 확인 버튼 클릭 시 다이얼로그 닫기
            okButton.addActionListener(e -> notification.dispose());

            // 컴포넌트들을 메인 패널에 추가
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

            // 3초 후 자동으로 알림 닫기
            javax.swing.Timer autoClose = new javax.swing.Timer(3000, e -> notification.dispose());
            autoClose.setRepeats(false);
            autoClose.start();
        });
    }
}