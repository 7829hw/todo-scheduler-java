
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 이 클래스는 달력 전체를 관리하는 클래스 입니다.
 * 이름이 OurCalendar인 이유는 이미 존재하는 Calendar 클래스와 구별하기 위해서 입니다.
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OurCalendar extends JFrame {
	// UI 테마 색상 상수
	private static final Color PRIMARY_COLOR = new Color(64, 128, 255);
	private static final Color SECONDARY_COLOR = new Color(248, 249, 250);
	private static final Color ACCENT_COLOR = new Color(255, 99, 71);
	private static final Color TEXT_COLOR = new Color(33, 37, 41);
	private static final Color HOVER_COLOR = new Color(108, 117, 125);
	private static final Color WEEKEND_COLOR = new Color(220, 53, 69);
	private static final Color SELECTED_COLOR = new Color(173, 216, 230);

	// 멤버 변수 선언
	private int year;
	private int month;
	private int lastDay;
	private int currentDay;
	private int currentWeek;
	public OurDate calendar[] = new OurDate[32];
	public Calendar cal = Calendar.getInstance();
	HashMap<String, List<ToDo>> tasks = new HashMap<>();
	private ReminderService reminderService = new ReminderService();

	// 위젯 관련 멤버 변수
	JPanel headPanel;
	JLabel monthLabel;
	JPanel calPanel;
	JButton prevButton;
	JButton nextButton;

	// <멤버 메소드 선언 및 정의>
	// 생성자
	OurCalendar() {
		this.year = cal.get(cal.YEAR);
		this.month = cal.get(cal.MONTH);
		this.lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		this.currentDay = 0;
		this.currentWeek = 0;

		for (int day = 1; day < 32; day++) {
			calendar[day] = new OurDate(day, 0, this);
		}

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (OurCalendar.this.calPanel != null)
					OurCalendar.this.updateCal();
			}
		});
		openFile();
	}

	// 모던 버튼 생성 메소드
	private JButton createModernButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// 호버 효과
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(bgColor.darker());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(bgColor);
			}
		});

		return button;
	}

	// 달력을 출력하는 메소드
	void showCalendar() {
		openFile();

		setTitle("📅 Modern Calendar");
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setBackground(SECONDARY_COLOR);

		// 위젯 닫힐 때 현재 달의 할 일 저장
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveFile();
			}
		});

		// 헤더 패널 스타일링
		headPanel = new JPanel(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

		monthLabel = new JLabel();
		monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
		monthLabel.setForeground(TEXT_COLOR);
		monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// 네비게이션 버튼들
		prevButton = createModernButton("◀", PRIMARY_COLOR);
		nextButton = createModernButton("▶", PRIMARY_COLOR);

		JPanel navPanel = new JPanel(new FlowLayout());
		navPanel.setBackground(Color.WHITE);
		navPanel.add(prevButton);
		navPanel.add(Box.createHorizontalStrut(20));
		navPanel.add(monthLabel);
		navPanel.add(Box.createHorizontalStrut(20));
		navPanel.add(nextButton);

		headPanel.add(navPanel, BorderLayout.CENTER);

		// 버튼 이벤트 설정
		prevButton.addActionListener(e -> {
			saveFile();
			cal.add(cal.MONTH, -1);
			updateCal(true);
		});

		nextButton.addActionListener(e -> {
			saveFile();
			cal.add(cal.MONTH, 1);
			updateCal(true);
		});

		// 일정 추가 버튼
		JButton appendButton = createModernButton("+ 새 일정", ACCENT_COLOR);
		appendButton.addActionListener(e -> {
			this.showToDoList();
		});

		// 하단 패널
		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBackground(SECONDARY_COLOR);
		bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		bottomPanel.add(appendButton);

		// 달력 패널 스타일링
		calPanel = new JPanel(new GridLayout(0, 7, 2, 2));
		calPanel.setBackground(SECONDARY_COLOR);
		calPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

		setLayout(new BorderLayout());
		add(headPanel, BorderLayout.NORTH);
		add(calPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		updateCal(true);
		setVisible(true);

		startReminderTimer();
	}

	// 달력을 업데이트 하는 함수
	void updateCal() {
		updateCal(false);
	}

	void updateCal(boolean loadFile) {
		calPanel.removeAll();
		currentDay = 0;
		currentWeek = 0;

		// 요일 헤더 설정
		String[] days = { "일", "월", "화", "수", "목", "금", "토" };
		for (int i = 0; i < days.length; i++) {
			JLabel label = new JLabel(days[i], SwingConstants.CENTER);
			label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			label.setOpaque(true);
			label.setBackground(PRIMARY_COLOR);
			label.setForeground(Color.WHITE);
			label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

			if (i == 0) { // 일요일
				label.setBackground(WEEKEND_COLOR);
			} else if (i == 6) { // 토요일
				label.setBackground(new Color(52, 144, 220));
			}

			calPanel.add(label);
		}

		// 현재의 년, 월, 마지막 날을 가져옴
		year = cal.get(cal.YEAR);
		month = cal.get(cal.MONTH);

		// 년-월 위젯을 추가
		monthLabel.setText(String.format("%d년 %d월", year, month + 1));

		if (loadFile) {
			openFile();
		}

		// 현재 날짜를 현재 년도의 월의 1일로 변경
		cal.set(year, month, 1);
		// 그 후 현재 날짜의 마지막 날짜를 가져옴
		lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		// 맨 처음 날짜의 요일을 구함
		int dayWeek = cal.get(cal.DAY_OF_WEEK);

		// 앞쪽의 빈칸을 채움
		for (int i = 1; i < dayWeek; i++) {
			JLabel emptyLabel = new JLabel("");
			emptyLabel.setOpaque(true);
			emptyLabel.setBackground(Color.WHITE);
			emptyLabel.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
			calPanel.add(emptyLabel);
		}

		// 여기서 부터 날짜를 채움
		for (int day = 1; day <= lastDay; day++) {
			calendar[day].setDate(day, dayWeek);

			// 일정이 있으면 제목 표시
			String key = getDateKey(year, month, day);
			List<ToDo> todos = tasks.get(key);
			if (todos != null && !todos.isEmpty()) {
				calendar[day].setText("<html><div style='text-align: center;'>" +
						day + "<br><small style='color: #666;'>" +
						todos.get(0).getTaskName() + "</small></div></html>");
			} else {
				calendar[day].setText(String.valueOf(day));
			}

			// 날짜 스타일링
			if (dayWeek == 1) { // 일요일
				calendar[day].setForeground(WEEKEND_COLOR);
				dayWeek++;
			} else if (dayWeek == 7) { // 토요일
				calendar[day].setForeground(new Color(52, 144, 220));
				dayWeek = 1;
			} else {
				calendar[day].setForeground(TEXT_COLOR);
				dayWeek++;
			}

			calPanel.add(calendar[day]);
		}

		calPanel.revalidate();
		calPanel.repaint();
	}

	// 달력이 가리키는 현재 날자를 변경하는 함수
	void setCurrent(int day, int week) {
		this.currentDay = day;
		this.currentWeek = week;
	}

	// 할 일을 추가하고 지우는 화면으로 넘어가는 함수
	void selectDate() {
		System.out.printf("%d일 %d요일\n", this.currentDay, this.currentWeek);
		// 날짜 JLabel 색을 전부 흰색으로 변경
		for (int i = 1; i < 32; i++) {
			calendar[i].setBackground(Color.WHITE);
		}
	}

	// 오늘 할 일을 추가, 삭제, 수정 하는 함수
	void showToDoList() {
		// 날짜가 선택 안 된 상태면 return
		if (currentDay == 0 || currentWeek == 0)
			return;
		System.out.printf("현재: %d일 %d요일\n", this.currentDay, this.currentWeek);
		ToDoForm todoForm = new ToDoForm(year, month, currentDay, this);
		todoForm.showList();
	}

	public static String getDateKey(int year, int month, int day) {
		return String.format("%04d-%02d-%02d", year, month + 1, day);
	}

	// 현재 달의 할 일 리스트를 파일로 저장하는 함수
	void saveFile() {
		FileManager.saveToFile(year, month, tasks);
		System.out.println("할 일 데이터 저장 완료: " + year + "-" + (month + 1));
	}

	// 달이 변경되었을 때, 그 달의 할 일 리스트를 가져오는 함수
	void openFile() {
		tasks = FileManager.loadFromFile(year, month);
		System.out.println("할 일 데이터 로드 완료: " + year + "-" + (month + 1));
	}

	void startReminderTimer() {
		javax.swing.Timer timer = new javax.swing.Timer(20 * 1000, e -> {
			reminderService.checkReminders(this);
		});
		timer.start();
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getCurrentDay() {
		return currentDay;
	}
}