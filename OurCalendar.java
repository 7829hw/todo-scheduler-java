
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

	private static final Color PRIMARY_COLOR = new Color(64, 128, 255);
	private static final Color SECONDARY_COLOR = new Color(248, 249, 250);
	private static final Color ACCENT_COLOR = new Color(255, 99, 71);
	private static final Color TEXT_COLOR = new Color(33, 37, 41);
	private static final Color HOVER_COLOR = new Color(108, 117, 125);
	private static final Color WEEKEND_COLOR = new Color(220, 53, 69);
	private static final Color SELECTED_COLOR = new Color(173, 216, 230);
	private static final Color SHARED_COLOR = new Color(255, 193, 7);

	private int year;
	private int month;
	private int lastDay;
	private int currentDay;
	private int currentWeek;
	public OurDate calendar[] = new OurDate[32];
	public Calendar cal = Calendar.getInstance();
	HashMap<String, List<ToDo>> tasks = new HashMap<>();
	HashMap<String, List<SharedToDo>> sharedTasks = new HashMap<>();
	private ReminderService reminderService = new ReminderService();

	private CalendarClient client;
	private String nickname;
	private UserDataManager userDataManager;

	JPanel headPanel;
	JLabel monthLabel;
	JPanel calPanel;
	JButton prevButton;
	JButton nextButton;

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
		initializeNetworking();
	}

	private void initializeNetworking() {

		nickname = JOptionPane.showInputDialog(
				null,
				"캘린더에서 사용할 닉네임을 입력하세요:",
				"닉네임 설정",
				JOptionPane.QUESTION_MESSAGE);

		if (nickname == null || nickname.trim().isEmpty()) {
			nickname = "익명" + System.currentTimeMillis() % 1000;
		}

		userDataManager = new UserDataManager(nickname);

		client = new CalendarClient(this);
		if (client.connect(nickname)) {

			sharedTasks = new HashMap<>();
			System.out.println("서버 연결 성공 - 공유 일정은 서버에서 수신 예정");
		} else {

			sharedTasks = userDataManager.loadSharedCache();
			System.out.println("서버 연결 실패 - 로컬 캐시에서 공유 일정 로드");

			JOptionPane.showMessageDialog(
					null,
					"서버에 연결할 수 없습니다.\n로컬 모드로 실행됩니다.",
					"연결 실패",
					JOptionPane.WARNING_MESSAGE);
		}
	}

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

	void showCalendar() {
		openFile();

		setTitle("📅 Modern Calendar - " + nickname +
				(client.isConnected() ? " (온라인)" : " (오프라인)") +
				" | 데이터: " + (userDataManager != null ? userDataManager.getUserDataDir() : "기본"));
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setBackground(SECONDARY_COLOR);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveFile();
				if (client != null) {
					client.disconnect();
				}
			}
		});

		headPanel = new JPanel(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

		monthLabel = new JLabel();
		monthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		monthLabel.setForeground(TEXT_COLOR);
		monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

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

		JButton appendButton = createModernButton("+ 새 일정", ACCENT_COLOR);
		appendButton.addActionListener(e -> {
			this.showToDoList();
		});

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.setBackground(SECONDARY_COLOR);
		bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		bottomPanel.add(appendButton);

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

	void updateCal() {
		updateCal(false);
	}

	void updateCal(boolean loadFile) {
		calPanel.removeAll();
		currentDay = 0;
		currentWeek = 0;

		String[] days = { "일", "월", "화", "수", "목", "금", "토" };
		for (int i = 0; i < days.length; i++) {
			JLabel label = new JLabel(days[i], SwingConstants.CENTER);
			label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
			label.setOpaque(true);
			label.setBackground(PRIMARY_COLOR);
			label.setForeground(Color.WHITE);
			label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

			if (i == 0) {
				label.setBackground(WEEKEND_COLOR);
			} else if (i == 6) {
				label.setBackground(new Color(52, 144, 220));
			}

			calPanel.add(label);
		}

		year = cal.get(cal.YEAR);
		month = cal.get(cal.MONTH);

		monthLabel.setText(String.format("%d년 %d월", year, month + 1));

		if (loadFile) {
			openFile();
		}

		cal.set(year, month, 1);

		lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);

		int dayWeek = cal.get(cal.DAY_OF_WEEK);

		for (int i = 1; i < dayWeek; i++) {
			JLabel emptyLabel = new JLabel("");
			emptyLabel.setOpaque(true);
			emptyLabel.setBackground(Color.WHITE);
			emptyLabel.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
			calPanel.add(emptyLabel);
		}

		for (int day = 1; day <= lastDay; day++) {
			calendar[day].setDate(day, dayWeek);

			String key = getDateKey(year, month, day);
			List<ToDo> todos = tasks.get(key);
			List<SharedToDo> sharedTodos = sharedTasks.get(key);

			StringBuilder displayText = new StringBuilder("<html><div style='text-align: center;'>" + day);

			if ((todos != null && !todos.isEmpty()) || (sharedTodos != null && !sharedTodos.isEmpty())) {
				displayText.append("<br>");

				if (todos != null && !todos.isEmpty()) {
					displayText.append("<small style='color: #666;'>").append(todos.get(0).getTaskName())
							.append("</small>");
				}

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

	void setCurrent(int day, int week) {
		this.currentDay = day;
		this.currentWeek = week;
	}

	void selectDate() {
		System.out.printf("%d일 %d요일\n", this.currentDay, this.currentWeek);

		for (int i = 1; i < 32; i++) {
			calendar[i].setBackground(Color.WHITE);
		}
	}

	void showToDoList() {

		if (currentDay == 0 || currentWeek == 0)
			return;
		System.out.printf("현재: %d일 %d요일\n", this.currentDay, this.currentWeek);
		ToDoForm todoForm = new ToDoForm(year, month, currentDay, this);
		todoForm.showList();
	}

	public void addSharedTodo(SharedToDo sharedTodo) {
		addSharedTodo(sharedTodo, true);
	}

	public void addSharedTodo(SharedToDo sharedTodo, boolean showNotification) {
		SwingUtilities.invokeLater(() -> {
			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());

			List<SharedToDo> existingTodos = sharedTasks.get(key);
			if (existingTodos != null) {
				for (SharedToDo existing : existingTodos) {
					if (existing.getId().equals(sharedTodo.getId())) {
						System.out.println("이미 존재하는 공유 일정: " + sharedTodo.getId());
						return;
					}
				}
			}

			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			if (sharedTodo.getStartYear() == year && sharedTodo.getStartMonth() == month) {
				updateCal(false);
			}

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

	public void updateSharedTodo(SharedToDo updatedTodo) {
		SwingUtilities.invokeLater(() -> {
			System.out.println("공유 일정 업데이트 처리 - ID: " + updatedTodo.getId() + ", 제목: " + updatedTodo.getTaskName()
					+ ", 생성자: " + updatedTodo.getCreator());

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

						if (userDataManager != null) {
							userDataManager.saveSharedCache(sharedTasks);
						}

						updateCal(false);

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

	public void deleteSharedTodo(String todoId) {
		SwingUtilities.invokeLater(() -> {
			boolean found = false;
			String deletedTaskName = "";
			String deletedCreator = "";

			System.out.println("공유 일정 삭제 처리 - ID: " + todoId);

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

				if (userDataManager != null) {
					userDataManager.saveSharedCache(sharedTasks);
				}

				updateCal(false);

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

	public void shareTask(ToDo todo) {
		if (client != null && client.isConnected()) {

			SharedToDo sharedTodo = new SharedToDo(
					todo.getTaskName(), todo.getLocation(), todo.isAllDay(),
					todo.getStartYear(), todo.getStartMonth(), todo.getStartDay(),
					todo.getStartHour(), todo.getStartMinute(),
					todo.getEndYear(), todo.getEndMonth(), todo.getEndDay(),
					todo.getEndHour(), todo.getEndMinute(),
					todo.getAlarm(), todo.getRepeat(), todo.getMemo(), nickname);

			System.out.println("공유 일정 생성: " + sharedTodo.getId() + " - " + sharedTodo.getTaskName());

			String key = getDateKey(sharedTodo.getStartYear(), sharedTodo.getStartMonth(), sharedTodo.getStartDay());
			sharedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(sharedTodo);

			if (userDataManager != null) {
				userDataManager.saveSharedCache(sharedTasks);
			}

			updateCal(false);

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

	void saveFile() {
		if (userDataManager != null) {
			userDataManager.saveLocalTasks(year, month, tasks);
			userDataManager.saveSharedCache(sharedTasks);
			System.out.println("사용자 데이터 저장 완료: " + nickname + " - " + year + "-" + (month + 1));
		}
	}

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

	public void clearSharedCache() {
		SwingUtilities.invokeLater(() -> {
			sharedTasks.clear();
			System.out.println("공유 일정 캐시가 초기화되었습니다.");

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