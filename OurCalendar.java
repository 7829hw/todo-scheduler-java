
/**
 * @author 자바 프로그래밍 5조
 * @version 1.1 - 클라이언트-서버 기능 추가
 * @since 2025-05-07
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
	private static final Color SHARED_COLOR = new Color(255, 193, 7); // 공유 일정 색상

	// 멤버 변수 선언
	private int year;
	private int month;
	private int lastDay;
	private int currentDay;
	private int currentWeek;
	public OurDate calendar[] = new OurDate[32];
	public Calendar cal = Calendar.getInstance();
	HashMap<String, List<ToDo>> tasks = new HashMap<>();
	HashMap<String, List<SharedToDo>> sharedTasks = new HashMap<>(); // 공유 일정
	private ReminderService reminderService = new ReminderService();

	// 네트워크 관련
	private CalendarClient client;
	private String nickname;
	private UserDataManager userDataManager;

	// 위젯 관련 멤버 변수
	JPanel headPanel;
	JLabel monthLabel;
	JPanel calPanel;
	JButton prevButton;
	JButton nextButton;

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
		initializeNetworking(); // 네트워킹 초기화
	}

	/**
	 * 네트워킹 초기화 - 닉네임 입력 및 서버 연결
	 */
	private void initializeNetworking() {
		// 닉네임 입력 다이얼로그
		nickname = JOptionPane.showInputDialog(
				null,
				"캘린더에서 사용할 닉네임을 입력하세요:",
				"닉네임 설정",
				JOptionPane.QUESTION_MESSAGE);

		if (nickname == null || nickname.trim().isEmpty()) {
			nickname = "익명" + System.currentTimeMillis() % 1000;
		}

		// 사용자별 데이터 관리자 초기화
		userDataManager = new UserDataManager(nickname);

		// 서버 연결 시도
		client = new CalendarClient(this);
		if (client.connect(nickname)) {
			// 서버 연결 성공 시 공유 일정은 서버에서 받음 (캐시 로드 안함)
			sharedTasks = new HashMap<>();
			System.out.println("서버 연결 성공 - 공유 일정은 서버에서 수신 예정");
		} else {
			// 서버 연결 실패 시에만 캐시에서 로드
			sharedTasks = userDataManager.loadSharedCache();
			System.out.println("서버 연결 실패 - 로컬 캐시에서 공유 일정 로드");

			JOptionPane.showMessageDialog(
					null,
					"서버에 연결할 수 없습니다.\n로컬 모드로 실행됩니다.",
					"연결 실패",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// 모던 버튼 생성 메소드
	private JButton createModernButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
		button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setOpaque(true);
		button.setContentAreaFilled(true);

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

		setTitle("📅 Modern Calendar - " + nickname +
				(client.isConnected() ? " (온라인)" : " (오프라인)") +
				" | 데이터: " + (userDataManager != null ? userDataManager.getUserDataDir() : "기본"));
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setBackground(SECONDARY_COLOR);

		// 위젯 닫힐 때 현재 달의 할 일 저장 및 연결 해제
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveFile(); // 로컬 + 공유 캐시 모두 저장
				if (client != null) {
					client.disconnect();
				}
			}
		});

		// 헤더 패널 스타일링
		headPanel = new JPanel(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

		monthLabel = new JLabel();
		monthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
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
			label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
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

			// 일정이 있으면 제목 표시 (로컬 + 공유)
			String key = getDateKey(year, month, day);
			List<ToDo> todos = tasks.get(key);
			List<SharedToDo> sharedTodos = sharedTasks.get(key);

			StringBuilder displayText = new StringBuilder("<html><div style='text-align: center;'>" + day);

			if ((todos != null && !todos.isEmpty()) || (sharedTodos != null && !sharedTodos.isEmpty())) {
				displayText.append("<br>");

				// 로컬 일정 표시
				if (todos != null && !todos.isEmpty()) {
					displayText.append("<small style='color: #666;'>").append(todos.get(0).getTaskName())
							.append("</small>");
				}

				// 공유 일정 표시
				if (sharedTodos != null && !sharedTodos.isEmpty()) {
					if (todos != null && !todos.isEmpty()) {
						displayText.append("<br>");
					}
					SharedToDo firstShared = sharedTodos.get(0);
					displayText.append("<small style='color: #ffc107;'>")
							.append(firstShared.getTaskName())
							.append("<br>by ").append(firstShared.getCreator()).append("</small>");
				}
			}

			displayText.append("</div></html>");
			calendar[day].setText(displayText.toString());

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

	/**
	 * 공유 일정을 달력에 추가하는 메소드
	 * 서버에서 받은 공유 일정을 UI에 반영
	 */
	public void addSharedTodo(SharedToDo sharedTodo) {
		addSharedTodo(sharedTodo, true); // 기본적으로 알림 표시
	}

	/**
	 * 공유 일정을 달력에 추가하는 메소드 (알림 옵션 포함)
	 */
	public void addSharedTodo(SharedToDo sharedTodo, boolean showNotification) {
		SwingUtilities.invokeLater(() -> {
			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());

			// 중복 방지 - 같은 ID의 일정이 이미 있는지 확인
			List<SharedToDo> existingTodos = sharedTasks.get(key);
			if (existingTodos != null) {
				for (SharedToDo existing : existingTodos) {
					if (existing.getId().equals(sharedTodo.getId())) {
						System.out.println("이미 존재하는 공유 일정: " + sharedTodo.getId());
						return; // 중복이므로 추가하지 않음
					}
				}
			}

			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			// 공유 일정 캐시 저장
			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			// 현재 보고 있는 월이면 달력 업데이트
			if (sharedTodo.getStartYear() == year && sharedTodo.getStartMonth() == month) {
				updateCal(false);
			}

			// 알림 표시 (본인이 만든 것이 아니고, 알림 옵션이 true인 경우만)
			if (showNotification && !sharedTodo.getCreator().equals(nickname)) {
				JOptionPane.showMessageDialog(
						this,
						sharedTodo.getCreator() + "님이 새 공유 일정을 추가했습니다:\n" + sharedTodo.getTaskName(),
						"새 공유 일정",
						JOptionPane.INFORMATION_MESSAGE);
			}

			System.out.println("공유 일정 추가됨: " + sharedTodo.getTaskName() + " by " + sharedTodo.getCreator() + " (ID: "
					+ sharedTodo.getId() + ")");
		});
	}

	/**
	 * 공유 일정 업데이트 처리 (자신의 업데이트도 포함)
	 */
	public void updateSharedTodo(SharedToDo updatedTodo) {
		SwingUtilities.invokeLater(() -> {
			System.out.println("공유 일정 업데이트 처리 - ID: " + updatedTodo.getId() + ", 제목: " + updatedTodo.getTaskName()
					+ ", 생성자: " + updatedTodo.getCreator());

			// 기존 일정 찾아서 업데이트
			boolean found = false;
			for (String dateKey : sharedTasks.keySet()) {
				List<SharedToDo> todos = sharedTasks.get(dateKey);
				for (int i = 0; i < todos.size(); i++) {
					SharedToDo existingTodo = todos.get(i);

					if (existingTodo.getId().equals(updatedTodo.getId())) {
						System.out.println("일치하는 일정 찾음! 업데이트 진행: " + existingTodo.getTaskName() + " -> "
								+ updatedTodo.getTaskName());
						todos.set(i, updatedTodo);
						found = true;

						// 공유 일정 캐시 저장
						if (userDataManager != null) {
							userDataManager.saveSharedCache(sharedTasks);
						}

						// 현재 보고 있는 월이면 달력 업데이트
						updateCal(false);

						// 본인이 수정한 게 아닌 경우만 알림
						if (!updatedTodo.getCreator().equals(nickname)) {
							JOptionPane.showMessageDialog(
									this,
									updatedTodo.getCreator() + "님이 공유 일정을 수정했습니다:\n" + updatedTodo.getTaskName(),
									"공유 일정 업데이트",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							System.out.println("본인이 수정한 공유 일정 로컬 동기화 완료");
						}
						return;
					}
				}
			}

			if (!found) {
				System.err.println("업데이트할 공유 일정을 찾을 수 없음: " + updatedTodo.getId());
				System.out.println("현재 공유 일정 목록:");
				for (String dateKey : sharedTasks.keySet()) {
					for (SharedToDo todo : sharedTasks.get(dateKey)) {
						System.out.println("  - 날짜: " + dateKey + ", ID: " + todo.getId() + ", 제목: "
								+ todo.getTaskName() + ", 생성자: " + todo.getCreator());
					}
				}
			}
		});
	}

	/**
	 * 공유 일정 삭제 처리 (자신의 삭제도 포함)
	 */
	public void deleteSharedTodo(String todoId) {
		SwingUtilities.invokeLater(() -> {
			boolean found = false;
			String deletedTaskName = "";
			String deletedCreator = "";

			System.out.println("공유 일정 삭제 처리 - ID: " + todoId);

			// 모든 날짜에서 해당 ID의 일정 찾아서 삭제
			for (String dateKey : sharedTasks.keySet()) {
				List<SharedToDo> todos = sharedTasks.get(dateKey);
				for (int i = 0; i < todos.size(); i++) {
					SharedToDo todo = todos.get(i);
					System.out.println("삭제 대상 확인 - ID: " + todo.getId() + " vs " + todoId);

					if (todo.getId().equals(todoId)) {
						SharedToDo deletedTodo = todos.remove(i);
						deletedTaskName = deletedTodo.getTaskName();
						deletedCreator = deletedTodo.getCreator();
						found = true;
						System.out.println("공유 일정 삭제됨: " + deletedTaskName + " (ID: " + todoId + ")");
						break;
					}
				}
				if (found)
					break;
			}

			if (found) {
				// 공유 일정 캐시 저장
				if (userDataManager != null) {
					userDataManager.saveSharedCache(sharedTasks);
				}

				// 달력 업데이트
				updateCal(false);

				// 본인이 삭제한 게 아닌 경우만 알림
				if (!deletedCreator.equals(nickname)) {
					JOptionPane.showMessageDialog(
							this,
							deletedCreator + "님이 공유 일정을 삭제했습니다:\n" + deletedTaskName,
							"공유 일정 삭제",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					System.out.println("본인이 삭제한 공유 일정 로컬 동기화 완료");
				}
			} else {
				System.err.println("삭제할 공유 일정을 찾을 수 없음: " + todoId);
				System.out.println("현재 공유 일정 목록:");
				for (String dateKey : sharedTasks.keySet()) {
					for (SharedToDo todo : sharedTasks.get(dateKey)) {
						System.out.println(
								"  - 날짜: " + dateKey + ", ID: " + todo.getId() + ", 제목: " + todo.getTaskName());
					}
				}
			}
		});
	}

	/**
	 * 공유 일정 삭제를 서버로 전송
	 */
	public void deleteSharedTask(String todoId) {
		if (client != null && client.isConnected()) {
			client.deleteSharedTask(todoId);
		} else {
			JOptionPane.showMessageDialog(
					this,
					"서버에 연결되지 않아 공유 일정을 삭제할 수 없습니다.",
					"연결 오류",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 공유 일정 업데이트를 서버로 전송
	 */
	public void updateSharedTask(SharedToDo sharedTodo) {
		if (client != null && client.isConnected()) {
			client.updateSharedTask(sharedTodo);
		} else {
			JOptionPane.showMessageDialog(
					this,
					"서버에 연결되지 않아 공유 일정을 업데이트할 수 없습니다.",
					"연결 오류",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 일정을 서버로 공유
	 */
	public void shareTask(ToDo todo) {
		if (client != null && client.isConnected()) {
			// 공유 일정 객체 생성
			SharedToDo sharedTodo = new SharedToDo(
					todo.getTaskName(), todo.getLocation(), todo.isAllDay(),
					todo.getStartYear(), todo.getStartMonth(), todo.getStartDay(),
					todo.getStartHour(), todo.getStartMinute(),
					todo.getEndYear(), todo.getEndMonth(), todo.getEndDay(),
					todo.getEndHour(), todo.getEndMinute(),
					todo.getAlarm(), todo.getRepeat(), todo.getMemo(), nickname);

			System.out.println("공유 일정 생성: " + sharedTodo.getId() + " - " + sharedTodo.getTaskName());

			// 로컬에 즉시 추가 (자신의 화면에 바로 반영)
			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());
			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			// 공유 일정 캐시 저장
			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			// 달력 업데이트
			updateCal(false);

			// 서버로 전송 (ID 포함)
			client.shareTask(sharedTodo);

		} else {
			JOptionPane.showMessageDialog(
					this,
					"서버에 연결되지 않아 일정을 공유할 수 없습니다.",
					"연결 오류",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static String getDateKey(int year, int month, int day) {
		return String.format("%04d-%02d-%02d", year, month + 1, day);
	}

	// 현재 달의 할 일 리스트를 파일로 저장하는 함수
	void saveFile() {
		if (userDataManager != null) {
			userDataManager.saveLocalTasks(year, month, tasks);
			userDataManager.saveSharedCache(sharedTasks);
			System.out.println("사용자 데이터 저장 완료: " + nickname + " - " + year + "-" + (month + 1));
		}
	}

	// 달이 변경되었을 때, 그 달의 할 일 리스트를 가져오는 함수
	void openFile() {
		if (userDataManager != null) {
			tasks = userDataManager.loadLocalTasks(year, month);
			System.out.println("사용자 데이터 로드 완료: " + nickname + " - " + year + "-" + (month + 1));
		}
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

	public int getLastDay() {
		return lastDay;
	}

	public CalendarClient getClient() {
		return client;
	}

	/**
	 * 공유 일정 캐시 초기화 (서버 재연결 시)
	 */
	public void clearSharedCache() {
		SwingUtilities.invokeLater(() -> {
			sharedTasks.clear();
			System.out.println("공유 일정 캐시가 초기화되었습니다.");

			// 현재 달력 업데이트
			updateCal(false);
		});
	}

	public String getNickname() {
		return nickname;
	}

	public HashMap<String, List<SharedToDo>> getSharedTasks() {
		return sharedTasks;
	}
}