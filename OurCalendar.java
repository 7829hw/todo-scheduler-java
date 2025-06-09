
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 메인 캘린더 GUI 클래스 - Swing을 사용한 캘린더 화면 구성 및 일정 관리
 * 개인 일정과 공유 일정을 통합 관리하며, 서버와의 네트워킹을 담당함
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

	// UI 색상 상수 정의
	private static final Color PRIMARY_COLOR = new Color(64, 128, 255); // 기본 색상
	private static final Color SECONDARY_COLOR = new Color(248, 249, 250); // 보조 색상
	private static final Color ACCENT_COLOR = new Color(255, 99, 71); // 강조 색상
	private static final Color TEXT_COLOR = new Color(33, 37, 41); // 텍스트 색상
	private static final Color WEEKEND_COLOR = new Color(220, 53, 69); // 주말 색상

	// 캘린더 상태 변수
	private int year; // 현재 표시 연도
	private int month; // 현재 표시 월
	private int lastDay; // 해당 월의 마지막 날
	private int currentDay; // 현재 선택된 날
	private int currentWeek; // 현재 선택된 요일

	// 캘린더 날짜 객체 배열
	public OurDate calendar[] = new OurDate[32];
	public Calendar cal = Calendar.getInstance();

	// 일정 저장소
	HashMap<String, List<ToDo>> tasks = new HashMap<>(); // 개인 일정
	HashMap<String, List<SharedToDo>> sharedTasks = new HashMap<>(); // 공유 일정

	private ReminderService reminderService = new ReminderService(); // 알림 서비스

	// 네트워킹 관련
	private CalendarClient client; // 서버 클라이언트
	private String nickname; // 사용자 닉네임
	private UserDataManager userDataManager; // 사용자 데이터 관리자

	// GUI 컴포넌트
	JPanel headPanel; // 헤더 패널
	JLabel monthLabel; // 월 표시 레이블
	JPanel calPanel; // 캘린더 패널
	JButton prevButton; // 이전 월 버튼
	JButton nextButton; // 다음 월 버튼

	/**
	 * OurCalendar 생성자 - 캘린더 초기화
	 */
	OurCalendar() {
		// 현재 날짜로 초기화
		this.year = cal.get(cal.YEAR);
		this.month = cal.get(cal.MONTH);
		this.lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		this.currentDay = 0;
		this.currentWeek = 0;

		// 날짜 객체 배열 초기화
		for (int day = 1; day < 32; day++) {
			calendar[day] = new OurDate(day, 0, this);
		}

		// 창 크기 변경 시 캘린더 업데이트
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (OurCalendar.this.calPanel != null)
					OurCalendar.this.updateCal();
			}
		});

		openFile();
		initializeNetworking();
	}

	/**
	 * 네트워킹 초기화 - 서버 연결 및 사용자 설정
	 */
	private void initializeNetworking() {
		// 닉네임 입력 받기
		nickname = JOptionPane.showInputDialog(
				null,
				"캘린더에서 사용할 닉네임을 입력하세요:",
				"닉네임 설정",
				JOptionPane.QUESTION_MESSAGE);

		if (nickname == null || nickname.trim().isEmpty()) {
			nickname = "익명" + System.currentTimeMillis() % 1000;
		}

		// 사용자 데이터 관리자 초기화
		userDataManager = new UserDataManager(nickname);

		// 서버 연결 시도
		client = new CalendarClient(this);
		if (client.connect(nickname)) {
			// 서버 연결 성공 - 서버에서 공유 일정 수신 예정
			sharedTasks = new HashMap<>();
			System.out.println("서버 연결 성공 - 공유 일정은 서버에서 수신 예정");
		} else {
			// 서버 연결 실패 - 로컬 캐시에서 공유 일정 로드
			sharedTasks = userDataManager.loadSharedCache();
			System.out.println("서버 연결 실패 - 로컬 캐시에서 공유 일정 로드");

			JOptionPane.showMessageDialog(
					null,
					"서버에 연결할 수 없습니다.\n로컬 모드로 실행됩니다.",
					"연결 실패",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * 모던 스타일 버튼 생성
	 * 
	 * @param text    버튼 텍스트
	 * @param bgColor 배경 색상
	 * @return 스타일이 적용된 버튼
	 */
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

		// 마우스 호버 효과
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

	/**
	 * 캘린더 메인 화면 표시
	 */
	void showCalendar() {
		openFile();

		// 윈도우 제목 설정 (연결 상태 표시)
		setTitle("📅 Modern Calendar - " + nickname +
				(client.isConnected() ? " (온라인)" : " (오프라인)") +
				" | 데이터: " + (userDataManager != null ? userDataManager.getUserDataDir() : "기본"));
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setBackground(SECONDARY_COLOR);

		// 윈도우 종료 시 데이터 저장
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveFile();
				if (client != null) {
					client.disconnect();
				}
			}
		});

		// 헤더 패널 구성
		headPanel = new JPanel(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

		monthLabel = new JLabel();
		monthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		monthLabel.setForeground(TEXT_COLOR);
		monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

		prevButton = createModernButton("◀", PRIMARY_COLOR);
		nextButton = createModernButton("▶", PRIMARY_COLOR);

		// 네비게이션 패널
		JPanel navPanel = new JPanel(new FlowLayout());
		navPanel.setBackground(Color.WHITE);
		navPanel.add(prevButton);
		navPanel.add(Box.createHorizontalStrut(20));
		navPanel.add(monthLabel);
		navPanel.add(Box.createHorizontalStrut(20));
		navPanel.add(nextButton);

		headPanel.add(navPanel, BorderLayout.CENTER);

		// 이전/다음 월 버튼 이벤트
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

		// 새 일정 추가 버튼
		JButton appendButton = createModernButton("+ 새 일정", ACCENT_COLOR);
		appendButton.addActionListener(e -> {
			this.showToDoList();
		});

		// 하단 패널
		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBackground(SECONDARY_COLOR);
		bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		bottomPanel.add(appendButton);

		// 캘린더 그리드 패널
		calPanel = new JPanel(new GridLayout(0, 7, 2, 2));
		calPanel.setBackground(SECONDARY_COLOR);
		calPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

		// 레이아웃 설정
		setLayout(new BorderLayout());
		add(headPanel, BorderLayout.NORTH);
		add(calPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		updateCal(true);
		setVisible(true);

		startReminderTimer();
	}

	/**
	 * 캘린더 업데이트 (overload)
	 */
	void updateCal() {
		updateCal(false);
	}

	/**
	 * 캘린더 화면 업데이트
	 * 
	 * @param loadFile 파일 로드 여부
	 */
	void updateCal(boolean loadFile) {
		calPanel.removeAll();
		currentDay = 0;
		currentWeek = 0;

		// 요일 헤더 추가
		String[] days = { "일", "월", "화", "수", "목", "금", "토" };
		for (int i = 0; i < days.length; i++) {
			JLabel label = new JLabel(days[i], SwingConstants.CENTER);
			label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
			label.setOpaque(true);
			label.setBackground(PRIMARY_COLOR);
			label.setForeground(Color.WHITE);
			label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

			// 일요일과 토요일 구분
			if (i == 0) {
				label.setBackground(WEEKEND_COLOR);
			} else if (i == 6) {
				label.setBackground(new Color(52, 144, 220));
			}

			calPanel.add(label);
		}

		// 현재 월 정보 업데이트
		year = cal.get(cal.YEAR);
		month = cal.get(cal.MONTH);

		monthLabel.setText(String.format("%d년 %d월", year, month + 1));

		if (loadFile) {
			openFile();
		}

		cal.set(year, month, 1);
		lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		int dayWeek = cal.get(cal.DAY_OF_WEEK);

		// 월 시작 전 빈 칸 추가
		for (int i = 1; i < dayWeek; i++) {
			JLabel emptyLabel = new JLabel("");
			emptyLabel.setOpaque(true);
			emptyLabel.setBackground(Color.WHITE);
			emptyLabel.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
			calPanel.add(emptyLabel);
		}

		// 날짜 셀 생성 및 일정 표시
		for (int day = 1; day <= lastDay; day++) {
			calendar[day].setDate(day, dayWeek);

			String key = getDateKey(year, month, day);
			List<ToDo> todos = tasks.get(key);
			List<SharedToDo> sharedTodos = sharedTasks.get(key);

			// 날짜와 일정 정보를 HTML로 구성
			StringBuilder displayText = new StringBuilder("<html><div style='text-align: center;'>" + day);

			if ((todos != null && !todos.isEmpty()) || (sharedTodos != null && !sharedTodos.isEmpty())) {
				displayText.append("<br>");

				// 개인 일정 표시
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

			// 요일별 색상 설정
			if (dayWeek == 1) {
				calendar[day].setForeground(WEEKEND_COLOR);
				dayWeek++;
			} else if (dayWeek == 7) {
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

	/**
	 * 현재 선택된 날짜 설정
	 * 
	 * @param day  날짜
	 * @param week 요일
	 */
	void setCurrent(int day, int week) {
		this.currentDay = day;
		this.currentWeek = week;
	}

	/**
	 * 날짜 선택 처리
	 */
	void selectDate() {
		System.out.printf("%d일 %d요일\n", this.currentDay, this.currentWeek);

		// 모든 날짜의 배경색 초기화
		for (int i = 1; i < 32; i++) {
			calendar[i].setBackground(Color.WHITE);
		}
	}

	/**
	 * 일정 관리 폼 표시
	 */
	void showToDoList() {
		if (currentDay == 0 || currentWeek == 0)
			return;
		System.out.printf("현재: %d일 %d요일\n", this.currentDay, this.currentWeek);
		ToDoForm todoForm = new ToDoForm(year, month, currentDay, this);
		todoForm.showList();
	}

	/**
	 * 공유 일정 추가 (overload)
	 * 
	 * @param sharedTodo 추가할 공유 일정
	 */
	public void addSharedTodo(SharedToDo sharedTodo) {
		addSharedTodo(sharedTodo, true);
	}

	/**
	 * 공유 일정 추가
	 * 
	 * @param sharedTodo       추가할 공유 일정
	 * @param showNotification 알림 표시 여부
	 */
	public void addSharedTodo(SharedToDo sharedTodo, boolean showNotification) {
		SwingUtilities.invokeLater(() -> {
			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());

			// 중복 체크
			List<SharedToDo> existingTodos = sharedTasks.get(key);
			if (existingTodos != null) {
				for (SharedToDo existing : existingTodos) {
					if (existing.getId().equals(sharedTodo.getId())) {
						System.out.println("이미 존재하는 공유 일정: " + sharedTodo.getId());
						return;
					}
				}
			}

			// 공유 일정 추가
			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			// 캐시 저장
			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			// 화면 업데이트
			if (sharedTodo.getStartYear() == year && sharedTodo.getStartMonth() == month) {
				updateCal(false);
			}

			// 알림 표시 (다른 사용자의 일정인 경우)
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
	 * 공유 일정 업데이트
	 * 
	 * @param updatedTodo 업데이트된 공유 일정
	 */
	public void updateSharedTodo(SharedToDo updatedTodo) {
		SwingUtilities.invokeLater(() -> {
			System.out.println("공유 일정 업데이트 처리 - ID: " + updatedTodo.getId() + ", 제목: " + updatedTodo.getTaskName()
					+ ", 생성자: " + updatedTodo.getCreator());

			boolean found = false;
			// 모든 날짜에서 해당 ID 찾기
			for (String dateKey : sharedTasks.keySet()) {
				List<SharedToDo> todos = sharedTasks.get(dateKey);
				for (int i = 0; i < todos.size(); i++) {
					SharedToDo existingTodo = todos.get(i);

					if (existingTodo.getId().equals(updatedTodo.getId())) {
						System.out.println("일치하는 일정 찾음! 업데이트 진행: " + existingTodo.getTaskName() + " -> "
								+ updatedTodo.getTaskName());
						todos.set(i, updatedTodo);
						found = true;

						// 캐시 저장 및 화면 업데이트
						if (userDataManager != null) {
							userDataManager.saveSharedCache(sharedTasks);
						}

						updateCal(false);

						// 다른 사용자 업데이트 알림
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

			// 업데이트할 일정을 찾지 못한 경우
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
	 * 공유 일정 삭제 처리
	 * 
	 * @param todoId 삭제할 일정 ID
	 */
	public void deleteSharedTodo(String todoId) {
		SwingUtilities.invokeLater(() -> {
			boolean found = false;
			String deletedTaskName = "";
			String deletedCreator = "";

			System.out.println("공유 일정 삭제 처리 - ID: " + todoId);

			// 모든 날짜에서 해당 ID 찾아 삭제
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
				// 캐시 저장 및 화면 업데이트
				if (userDataManager != null) {
					userDataManager.saveSharedCache(sharedTasks);
				}

				updateCal(false);

				// 다른 사용자 삭제 알림
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
	 * 공유 일정 삭제 요청을 서버에 전송
	 * 
	 * @param todoId 삭제할 일정 ID
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
	 * 공유 일정 업데이트를 서버에 전송
	 * 
	 * @param sharedTodo 업데이트할 공유 일정
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
	 * 개인 일정을 공유 일정으로 변환하여 서버에 전송
	 * 
	 * @param todo 공유할 개인 일정
	 */
	public void shareTask(ToDo todo) {
		if (client != null && client.isConnected()) {
			// ToDo를 SharedToDo로 변환
			SharedToDo sharedTodo = new SharedToDo(
					todo.getTaskName(), todo.getLocation(), todo.isAllDay(),
					todo.getStartYear(), todo.getStartMonth(), todo.getStartDay(),
					todo.getStartHour(), todo.getStartMinute(),
					todo.getEndYear(), todo.getEndMonth(), todo.getEndDay(),
					todo.getEndHour(), todo.getEndMinute(),
					todo.getAlarm(), todo.getRepeat(), todo.getMemo(), nickname);

			System.out.println("공유 일정 생성: " + sharedTodo.getId() + " - " + sharedTodo.getTaskName());

			// 로컬에 추가
			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());
			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			// 캐시 저장 및 화면 업데이트
			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			updateCal(false);

			// 서버에 전송
			client.shareTask(sharedTodo);

		} else {
			JOptionPane.showMessageDialog(
					this,
					"서버에 연결되지 않아 일정을 공유할 수 없습니다.",
					"연결 오류",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 날짜를 키 문자열로 변환 (YYYY-MM-DD 형식)
	 * 
	 * @param year  연도
	 * @param month 월 (0부터 시작)
	 * @param day   일
	 * @return 날짜 키 문자열
	 */
	public static String getDateKey(int year, int month, int day) {
		return String.format("%04d-%02d-%02d", year, month + 1, day);
	}

	/**
	 * 현재 일정 데이터를 파일로 저장
	 */
	void saveFile() {
		if (userDataManager != null) {
			userDataManager.saveLocalTasks(year, month, tasks);
			userDataManager.saveSharedCache(sharedTasks);
			System.out.println("사용자 데이터 저장 완료: " + nickname + " - " + year + "-" + (month + 1));
		}
	}

	/**
	 * 파일에서 일정 데이터 로드
	 */
	void openFile() {
		if (userDataManager != null) {
			tasks = userDataManager.loadLocalTasks(year, month);
			System.out.println("사용자 데이터 로드 완료: " + nickname + " - " + year + "-" + (month + 1));
		}
	}

	/**
	 * 알림 타이머 시작 (20초마다 체크)
	 */
	void startReminderTimer() {
		javax.swing.Timer timer = new javax.swing.Timer(20 * 1000, e -> {
			reminderService.checkReminders(this);
		});
		timer.start();
	}

	// Getter 메서드들
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

	public String getNickname() {
		return nickname;
	}

	public HashMap<String, List<SharedToDo>> getSharedTasks() {
		return sharedTasks;
	}

	/**
	 * 공유 일정 캐시 초기화
	 */
	public void clearSharedCache() {
		SwingUtilities.invokeLater(() -> {
			sharedTasks.clear();
			System.out.println("공유 일정 캐시가 초기화되었습니다.");
			updateCal(false);
		});
	}
}