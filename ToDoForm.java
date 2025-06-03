import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class ToDoForm extends JDialog {
	// UI 테마 색상
	private static final Color PRIMARY_COLOR = new Color(64, 128, 255);
	private static final Color SECONDARY_COLOR = new Color(248, 249, 250);
	private static final Color ACCENT_COLOR = new Color(255, 99, 71);
	private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
	private static final Color TEXT_COLOR = new Color(33, 37, 41);
	private static final Color BORDER_COLOR = new Color(206, 212, 218);
	private static final Color SHARED_COLOR = new Color(255, 193, 7);

	private int year;
	private int month;
	private int day;
	private ToDo todo;
	private SharedToDo sharedTodo; // 공유 일정 정보 보관
	private boolean editMode = false;
	OurCalendar baseCal;
	JTextField titleField = new JTextField();
	JTextField locationField = new JTextField();
	JCheckBox checkBox = new JCheckBox();
	JCheckBox shareCheckBox = new JCheckBox(); // 공유 일정 체크박스 추가
	JComboBox<String> alarmCombo;
	JComboBox<String> repeatCombo;
	JTextArea memoArea;

	private JLabel startYearLabel, startMonthLabel, startDayLabel, startHourLabel, startMinuteLabel;
	private JLabel endYearLabel, endMonthLabel, endDayLabel, endHourLabel, endMinuteLabel;

	ToDoForm(int year, int month, int day, OurCalendar baseCal) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.baseCal = baseCal;
		this.editMode = true;
	}

	// 모던 버튼 생성
	private JButton createStyledButton(String text, Color bgColor) {
		JButton button = new JButton(text);
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
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

	// 스타일링된 텍스트 필드 생성
	private JTextField createStyledTextField(String placeholder) {
		JTextField field = new JTextField();
		field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		applyPlaceholder(field, placeholder);
		return field;
	}

	// 입력하기 전에는 회색 글자로 타이틀 -> 입력 시작하면 지워짐
	public void applyPlaceholder(JTextComponent field, String placeholderText) {
		field.setText(placeholderText);
		field.setForeground(Color.GRAY);

		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (field.getText().equals(placeholderText)) {
					field.setText("");
					field.setForeground(TEXT_COLOR);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (field.getText().isEmpty()) {
					field.setForeground(Color.GRAY);
					field.setText(placeholderText);
				}
			}
		});
	}

	// 드롭다운 설정
	public JLabel createCustomDropdown(String[] options, String initialValue) {
		JLabel dropdown = new JLabel(initialValue);
		dropdown.setOpaque(true);
		dropdown.setBackground(Color.WHITE);
		dropdown.setForeground(TEXT_COLOR);
		dropdown.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		dropdown.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(6, 10, 6, 10)));
		dropdown.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JList<String> list = new JList<>(options);
		list.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(120, Math.min(25 * options.length, 120)));

		JPopupMenu popup = new JPopupMenu();
		popup.setLayout(new BorderLayout());
		popup.add(scrollPane, BorderLayout.CENTER);

		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				String selected = list.getSelectedValue();
				if (selected != null) {
					dropdown.setText(selected);
					popup.setVisible(false);
				}
			}
		});

		dropdown.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				popup.show(dropdown, 0, dropdown.getHeight());
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				dropdown.setBackground(SECONDARY_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				dropdown.setBackground(Color.WHITE);
			}
		});

		return dropdown;
	}

	// 새 일정 추가 UI
	void showList() {
		setTitle("📅 일정 관리");
		setSize(400, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		// 전체 레이아웃
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
		mainPanel.setBackground(Color.WHITE);

		// 헤더
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(Color.WHITE);

		String titleText = "새 일정 추가";
		if (editMode && todo != null) {
			if (sharedTodo != null) {
				titleText = "공유 일정 수정";
			} else {
				titleText = "일정 수정";
			}
		}

		JLabel titleLabel = new JLabel(titleText);
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		titleLabel.setForeground(TEXT_COLOR);
		headerPanel.add(titleLabel, BorderLayout.WEST);
		mainPanel.add(headerPanel);
		mainPanel.add(Box.createVerticalStrut(20));

		// 제목 입력
		titleField = createStyledTextField("일정 제목을 입력하세요");
		titleField.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		mainPanel.add(titleField);
		mainPanel.add(Box.createVerticalStrut(15));

		// 장소 입력
		locationField = createStyledTextField("장소 (선택사항)");
		mainPanel.add(locationField);
		mainPanel.add(Box.createVerticalStrut(20));

		// 구분선
		JSeparator separator = new JSeparator();
		separator.setForeground(BORDER_COLOR);
		mainPanel.add(separator);
		mainPanel.add(Box.createVerticalStrut(20));

		// 하루종일 체크박스
		JPanel checkPanel = new JPanel(new BorderLayout());
		checkPanel.setBackground(Color.WHITE);
		JLabel allDayLabel = new JLabel("하루종일");
		allDayLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		checkBox.setBackground(Color.WHITE);
		checkPanel.add(allDayLabel, BorderLayout.WEST);
		checkPanel.add(checkBox, BorderLayout.EAST);
		mainPanel.add(checkPanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// 공유 일정 체크박스 추가
		JPanel sharePanel = new JPanel(new BorderLayout());
		sharePanel.setBackground(Color.WHITE);

		String shareLabelText = "🌐 공유 일정";
		if (editMode && todo != null && sharedTodo == null) {
			// 기존 로컬 일정 수정 시 변환 가능 표시
			shareLabelText = "🌐 공유 일정으로 변환";
		}

		JLabel shareLabel = new JLabel(shareLabelText);
		shareLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		shareLabel.setForeground(SHARED_COLOR.darker());
		shareCheckBox.setBackground(Color.WHITE);

		// 서버 연결 상태에 따라 활성화/비활성화
		if (baseCal.getClient() == null || !baseCal.getClient().isConnected()) {
			shareCheckBox.setEnabled(false);
			shareLabel.setText(shareLabelText + " (오프라인)");
			shareLabel.setForeground(Color.GRAY);
		}

		// 기존 공유 일정 수정 시에는 특별 처리
		if (sharedTodo != null) {
			shareCheckBox.setSelected(true);
			shareCheckBox.setEnabled(true); // 활성화하여 공유 해제 가능
			shareLabel.setText("🌐 공유 일정 (체크 해제 시 개인 일정으로 변환)");
		}

		sharePanel.add(shareLabel, BorderLayout.WEST);
		sharePanel.add(shareCheckBox, BorderLayout.EAST);
		mainPanel.add(sharePanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// 시간 설정 패널
		JPanel timePanel = createTimePanel();
		mainPanel.add(timePanel);
		mainPanel.add(Box.createVerticalStrut(20));

		// 구분선
		JSeparator separator2 = new JSeparator();
		separator2.setForeground(BORDER_COLOR);
		mainPanel.add(separator2);
		mainPanel.add(Box.createVerticalStrut(15));

		// 알림 및 반복 설정
		JPanel optionsPanel = createOptionsPanel();
		mainPanel.add(optionsPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// 메모
		JLabel memoLabel = new JLabel("메모");
		memoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		memoLabel.setForeground(TEXT_COLOR);
		mainPanel.add(memoLabel);
		mainPanel.add(Box.createVerticalStrut(8));

		memoArea = new JTextArea(4, 30);
		memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		memoArea.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		applyPlaceholder(memoArea, "메모를 입력하세요 (선택사항)");

		JScrollPane memoScroll = new JScrollPane(memoArea);
		memoScroll.setBorder(null);
		mainPanel.add(memoScroll);
		mainPanel.add(Box.createVerticalStrut(25));

		// 버튼 패널
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		JButton cancelBtn = createStyledButton("취소", new Color(108, 117, 125));
		JButton saveBtn = createStyledButton("저장", SUCCESS_COLOR);

		cancelBtn.addActionListener(e -> dispose());
		saveBtn.addActionListener(e -> {
			String date = OurCalendar.getDateKey(year, month, day);
			ToDo newData = getToDo();

			// 유효성 검사 추가 (종료시간이 시작시간보다 앞서는 경우)
			LocalDateTime startTime = LocalDateTime.of(
					newData.getStartYear(),
					newData.getStartMonth(),
					newData.getStartDay(),
					newData.getStartHour(),
					newData.getStartMinute());

			LocalDateTime endTime = LocalDateTime.of(
					newData.getEndYear(),
					newData.getEndMonth(),
					newData.getEndDay(),
					newData.getEndHour(),
					newData.getEndMinute());

			if (endTime.isBefore(startTime)) {
				JOptionPane.showMessageDialog(this,
						"시작 날짜는 종료 날짜 이전이어야 합니다.",
						"시간 오류",
						JOptionPane.ERROR_MESSAGE);
				return; // 저장 중단
			}

			if (editMode && todo != null) {
				// 기존 객체 수정
				if (sharedTodo != null) {
					// 공유 일정 수정
					if (shareCheckBox.isSelected()) {
						// 여전히 공유 일정으로 유지
						updateSharedTodo(newData);
					} else {
						// 공유 일정을 개인 일정으로 변환
						System.out.println("공유 일정을 개인 일정으로 변환 시작: " + newData.getTaskName());

						// 서버에서 공유 일정 삭제
						baseCal.deleteSharedTask(sharedTodo.getId());

						// 로컬 공유 일정에서도 삭제
						baseCal.deleteSharedTodo(sharedTodo.getId());

						// 개인 일정으로 추가
						baseCal.tasks.computeIfAbsent(date, k -> new ArrayList<>()).add(newData);

						JOptionPane.showMessageDialog(this,
								"공유 일정이 개인 일정으로 변환되었습니다.\n다른 사용자들에게는 삭제됩니다.",
								"개인 일정 변환",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					// 로컬 일정 수정
					updateExistingTodo(newData);

					// 공유 일정으로 변환 체크
					if (shareCheckBox.isSelected() && baseCal.getClient() != null
							&& baseCal.getClient().isConnected()) {
						System.out.println("로컬 일정을 공유 일정으로 변환 시작: " + newData.getTaskName());

						// 기존 로컬 일정을 리스트에서 제거
						List<ToDo> localTodos = baseCal.tasks.get(date);
						if (localTodos != null && localTodos.contains(todo)) {
							localTodos.remove(todo);
							System.out.println("로컬 일정 삭제 완료");
						}

						// 공유 일정으로 생성 (newData 사용)
						baseCal.shareTask(newData);
						System.out.println("공유 일정 생성 완료");

						JOptionPane.showMessageDialog(this,
								"로컬 일정이 공유 일정으로 변환되어 다른 사용자들과 공유되었습니다!",
								"공유 일정 변환",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} else {
				// 새로 추가
				if (shareCheckBox.isSelected() && baseCal.getClient() != null && baseCal.getClient().isConnected()) {
					// 공유 일정으로만 생성 (로컬 일정은 생성하지 않음)
					System.out.println("새 공유 일정 생성: " + newData.getTaskName());
					baseCal.shareTask(newData);

					JOptionPane.showMessageDialog(this,
							"일정이 다른 사용자들과 공유되었습니다!",
							"공유 완료",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					// 로컬 일정으로만 생성
					System.out.println("새 로컬 일정 생성: " + newData.getTaskName());
					baseCal.tasks.computeIfAbsent(date, k -> new ArrayList<>()).add(newData);

					if (shareCheckBox.isSelected()) {
						JOptionPane.showMessageDialog(this,
								"서버에 연결되지 않아 로컬 일정으로 저장되었습니다.",
								"오프라인 모드",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			// 달력 업데이트
			baseCal.updateCal(false);
			dispose();
		});

		buttonPanel.add(cancelBtn);
		buttonPanel.add(saveBtn);
		mainPanel.add(buttonPanel);

		add(mainPanel);

		// 수정할 때 이미 입력된 필드 채움
		if (todo != null) {
			populateFields(todo);
		}

		setVisible(true);
	}

	// 시간 설정 패널 생성
	private JPanel createTimePanel() {
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
		timePanel.setBackground(Color.WHITE);

		// 시작 시간
		JPanel startPanel = new JPanel(new BorderLayout());
		startPanel.setBackground(Color.WHITE);
		JLabel startLabel = new JLabel("시작");
		startLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		startPanel.add(startLabel, BorderLayout.WEST);

		JPanel start = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		start.setBackground(Color.WHITE);
		startYearLabel = createCustomDropdown(makeYearList(), year + "년");
		startMonthLabel = createCustomDropdown(makeMonthList(), (month + 1) + "월");
		startDayLabel = createCustomDropdown(makeDayList(), day + "일");
		startHourLabel = createCustomDropdown(makeHourList(), "0시");
		startMinuteLabel = createCustomDropdown(makeMinuteList(), "0분");

		start.add(startYearLabel);
		start.add(startMonthLabel);
		start.add(startDayLabel);
		start.add(startHourLabel);
		start.add(startMinuteLabel);

		startPanel.add(start, BorderLayout.EAST);
		timePanel.add(startPanel);
		timePanel.add(Box.createVerticalStrut(10));

		// 종료 시간
		JPanel finishPanel = new JPanel(new BorderLayout());
		finishPanel.setBackground(Color.WHITE);
		JLabel finishLabel = new JLabel("종료");
		finishLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		finishPanel.add(finishLabel, BorderLayout.WEST);

		JPanel end = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		end.setBackground(Color.WHITE);
		endYearLabel = createCustomDropdown(makeYearList(), year + "년");
		endMonthLabel = createCustomDropdown(makeMonthList(), (month + 1) + "월");
		endDayLabel = createCustomDropdown(makeDayList(), day + "일");
		endHourLabel = createCustomDropdown(makeHourList(), "1시");
		endMinuteLabel = createCustomDropdown(makeMinuteList(), "0분");

		end.add(endYearLabel);
		end.add(endMonthLabel);
		end.add(endDayLabel);
		end.add(endHourLabel);
		end.add(endMinuteLabel);

		finishPanel.add(end, BorderLayout.EAST);
		timePanel.add(finishPanel);

		return timePanel;
	}

	// 옵션 패널 생성 (알림, 반복)
	private JPanel createOptionsPanel() {
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.setBackground(Color.WHITE);

		// 알림 설정
		alarmCombo = new JComboBox<>(new String[] {
				"없음", "10분 전", "30분 전", "1시간 전", "하루 전"
		});
		alarmCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

		JPanel alarmPanel = new JPanel(new BorderLayout());
		alarmPanel.setBackground(Color.WHITE);
		JLabel alarmLabel = new JLabel("알림");
		alarmLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		alarmPanel.add(alarmLabel, BorderLayout.WEST);
		alarmPanel.add(alarmCombo, BorderLayout.EAST);
		optionsPanel.add(alarmPanel);
		optionsPanel.add(Box.createVerticalStrut(10));

		// 반복 일정
		repeatCombo = new JComboBox<>(new String[] {
				"없음", "매일", "매주", "매달", "매년"
		});
		repeatCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

		JPanel repeatPanel = new JPanel(new BorderLayout());
		repeatPanel.setBackground(Color.WHITE);
		JLabel repeatLabel = new JLabel("반복");
		repeatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		repeatPanel.add(repeatLabel, BorderLayout.WEST);
		repeatPanel.add(repeatCombo, BorderLayout.EAST);
		optionsPanel.add(repeatPanel);

		return optionsPanel;
	}

	// 기존 할일 업데이트
	private void updateExistingTodo(ToDo newData) {
		todo.setTaskName(newData.getTaskName());
		todo.setLocation(newData.getLocation());
		todo.setAllDay(newData.isAllDay());
		todo.setStartYear(newData.getStartYear());
		todo.setStartMonth(newData.getStartMonth());
		todo.setStartDay(newData.getStartDay());
		todo.setStartHour(newData.getStartHour());
		todo.setStartMinute(newData.getStartMinute());
		todo.setEndYear(newData.getEndYear());
		todo.setEndMonth(newData.getEndMonth());
		todo.setEndDay(newData.getEndDay());
		todo.setEndHour(newData.getEndHour());
		todo.setEndMinute(newData.getEndMinute());
		todo.setAlarm(newData.getAlarm());
		todo.setRepeat(newData.getRepeat());
		todo.setMemo(newData.getMemo());
	}

	// 공유 일정 업데이트
	private void updateSharedTodo(ToDo newData) {
		if (sharedTodo == null) {
			System.err.println("오류: sharedTodo가 null입니다!");
			return;
		}

		System.out.println("공유 일정 업데이트 시작 - ID: " + sharedTodo.getId() + ", 제목: " + newData.getTaskName());

		// 기존 SharedToDo 업데이트
		sharedTodo.setTaskName(newData.getTaskName());
		sharedTodo.setLocation(newData.getLocation());
		sharedTodo.setAllDay(newData.isAllDay());
		sharedTodo.setStartYear(newData.getStartYear());
		sharedTodo.setStartMonth(newData.getStartMonth());
		sharedTodo.setStartDay(newData.getStartDay());
		sharedTodo.setStartHour(newData.getStartHour());
		sharedTodo.setStartMinute(newData.getStartMinute());
		sharedTodo.setEndYear(newData.getEndYear());
		sharedTodo.setEndMonth(newData.getEndMonth());
		sharedTodo.setEndDay(newData.getEndDay());
		sharedTodo.setEndHour(newData.getEndHour());
		sharedTodo.setEndMinute(newData.getEndMinute());
		sharedTodo.setAlarm(newData.getAlarm());
		sharedTodo.setRepeat(newData.getRepeat());
		sharedTodo.setMemo(newData.getMemo());

		System.out.println("공유 일정 업데이트 완료 - 서버 전송: " + sharedTodo.toNetworkString());

		// 로컬에서 즉시 업데이트
		baseCal.updateSharedTodo(sharedTodo);

		// 서버로 업데이트 전송
		baseCal.updateSharedTask(sharedTodo);

		JOptionPane.showMessageDialog(this,
				"공유 일정이 수정되어 다른 사용자들에게 전송되었습니다!",
				"공유 일정 업데이트",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// 입력한 값들 받아옴
	public ToDo getToDo() {
		String title = titleField.getText().trim();
		String location = locationField.getText().trim();
		boolean isAllDay = checkBox.isSelected();
		int sy = Integer.parseInt(startYearLabel.getText().replace("년", ""));
		int sm = Integer.parseInt(startMonthLabel.getText().replace("월", "")) - 1;
		int sd = Integer.parseInt(startDayLabel.getText().replace("일", ""));
		int sh = Integer.parseInt(startHourLabel.getText().replace("시", ""));
		int smin = Integer.parseInt(startMinuteLabel.getText().replace("분", ""));
		int ey = Integer.parseInt(endYearLabel.getText().replace("년", ""));
		int em = Integer.parseInt(endMonthLabel.getText().replace("월", "")) - 1;
		int ed = Integer.parseInt(endDayLabel.getText().replace("일", ""));
		int eh = Integer.parseInt(endHourLabel.getText().replace("시", ""));
		int emin = Integer.parseInt(endMinuteLabel.getText().replace("분", ""));
		String alarm = alarmCombo.getSelectedItem().toString();
		String repeat = repeatCombo.getSelectedItem().toString();
		String memo = memoArea.getText().trim();

		return new ToDo(title, location, isAllDay, sy, sm, sd, sh, smin, ey, em, ed, eh, emin, alarm, repeat, memo);
	}

	// 해쉬맵에 일정 추가
	public void addToDo(String date, ToDo todo) {
		baseCal.tasks.computeIfAbsent(date, k -> new ArrayList<>()).add(todo);
		System.out.println(date + " 일정 추가됨");
	}

	// 드롭다운에 들어갈 리스트들
	private String[] makeYearList() {
		String[] years = new String[100];
		for (int i = 0; i < 100; i++) {
			years[i] = (i + 2000) + "년";
		}
		return years;
	}

	private String[] makeMonthList() {
		String[] months = new String[12];
		for (int i = 0; i < 12; i++) {
			months[i] = (i + 1) + "월";
		}
		return months;
	}

	private String[] makeDayList() {
		String[] days = new String[31];
		for (int i = 0; i < baseCal.getLastDay(); i++) {
			days[i] = (i + 1) + "일";
		}
		return days;
	}

	private String[] makeHourList() {
		String[] hours = new String[24];
		for (int i = 0; i < 24; i++) {
			hours[i] = i + "시";
		}
		return hours;
	}

	private String[] makeMinuteList() {
		String[] minutes = new String[60];
		for (int i = 0; i < 60; i++) {
			minutes[i] = i + "분";
		}
		return minutes;
	}

	public ToDo getTodo() {
		return todo;
	}

	public void setTodo(ToDo todo) {
		this.todo = todo;
	}

	public void setSharedTodo(SharedToDo sharedTodo) {
		this.sharedTodo = sharedTodo;
	}

	// 수정용 생성자
	public ToDoForm(int year, int month, int day, OurCalendar baseCal, ToDo existingToDo) {
		this(year, month, day, baseCal);
		this.todo = existingToDo;
	}

	// 추가되어 있는 일정 목록 UI - 수정, 삭제 기능 포함 (로컬 + 공유 일정)
	public static void showDialogWithList(JFrame parent, String dateKey, List<ToDo> localList) {
		// 공유 일정도 함께 표시
		OurCalendar calendar = (OurCalendar) parent;
		List<SharedToDo> sharedList = calendar.sharedTasks.get(dateKey);

		showDialogWithBothLists(parent, dateKey, localList, sharedList);
	}

	// 로컬 + 공유 일정을 모두 표시하는 다이얼로그
	public static void showDialogWithBothLists(JFrame parent, String dateKey, List<ToDo> localList,
			List<SharedToDo> sharedList) {
		JDialog dialog = new JDialog(parent);
		dialog.setTitle("📅 " + dateKey + " 일정");
		dialog.setSize(650, 500);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().setBackground(Color.WHITE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setBackground(Color.WHITE);

		// 헤더
		JLabel dayLabel = new JLabel(dateKey + " 일정 목록");
		dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
		dayLabel.setForeground(new Color(33, 37, 41));
		dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(dayLabel);
		mainPanel.add(Box.createVerticalStrut(15));

		JSeparator headerSep = new JSeparator();
		headerSep.setForeground(new Color(206, 212, 218));
		mainPanel.add(headerSep);
		mainPanel.add(Box.createVerticalStrut(15));

		// 로컬 일정 표시
		if (localList != null && !localList.isEmpty()) {
			JLabel localLabel = new JLabel("🏠 내 일정");
			localLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			localLabel.setForeground(PRIMARY_COLOR);
			mainPanel.add(localLabel);
			mainPanel.add(Box.createVerticalStrut(10));

			for (int i = 0; i < localList.size(); i++) {
				ToDo todo = localList.get(i);
				int index = i;
				mainPanel.add(createLocalTodoPanel(dialog, parent, todo, localList, index, dateKey));
				mainPanel.add(Box.createVerticalStrut(10));
			}
		}

		// 공유 일정 표시
		if (sharedList != null && !sharedList.isEmpty()) {
			if (localList != null && !localList.isEmpty()) {
				mainPanel.add(Box.createVerticalStrut(15));
			}

			JLabel sharedLabel = new JLabel("🌐 공유 일정");
			sharedLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			sharedLabel.setForeground(SHARED_COLOR.darker());
			mainPanel.add(sharedLabel);
			mainPanel.add(Box.createVerticalStrut(10));

			for (SharedToDo sharedTodo : sharedList) {
				mainPanel.add(createSharedTodoPanelWithEdit(dialog, parent, sharedTodo, dateKey));
				mainPanel.add(Box.createVerticalStrut(10));
			}
		}

		// 일정이 없는 경우
		if ((localList == null || localList.isEmpty()) && (sharedList == null || sharedList.isEmpty())) {
			JLabel emptyLabel = new JLabel("등록된 일정이 없습니다.");
			emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
			emptyLabel.setForeground(new Color(108, 117, 125));
			emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			mainPanel.add(emptyLabel);
		}

		mainPanel.add(Box.createVerticalGlue());

		// 닫기 버튼
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		closePanel.setBackground(Color.WHITE);
		JButton closeButton = new JButton("닫기");
		closeButton.setBackground(new Color(108, 117, 125));
		closeButton.setForeground(Color.WHITE);
		closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		closeButton.setBorder(new EmptyBorder(10, 20, 10, 20));
		closeButton.setFocusPainted(false);
		closeButton.addActionListener(e -> dialog.dispose());
		closePanel.add(closeButton);
		// 맥에서도 버튼 보이도록
		closeButton.setContentAreaFilled(true);
		closeButton.setOpaque(true);
		closeButton.setBorderPainted(true);

		mainPanel.add(closePanel);

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(null);
		dialog.add(scrollPane);
		dialog.setVisible(true);
	}

	// 로컬 일정 패널 생성
	private static JPanel createLocalTodoPanel(JDialog dialog, JFrame parent, ToDo todo, List<ToDo> list, int index,
			String dateKey) {
		JPanel rowPanel = new JPanel(new BorderLayout());
		rowPanel.setBackground(new Color(248, 249, 250));
		rowPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
				new EmptyBorder(15, 15, 15, 15)));
		rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rowPanel.setMaximumSize(new Dimension(460, 80));

		// 일정 정보
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setBackground(new Color(248, 249, 250));

		JLabel nameLabel = new JLabel(todo.getTaskName());
		nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		nameLabel.setForeground(new Color(33, 37, 41));

		String timeInfo = String.format("%02d:%02d - %02d:%02d",
				todo.getStartHour(), todo.getStartMinute(),
				todo.getEndHour(), todo.getEndMinute());
		JLabel timeLabel = new JLabel("🕐 " + timeInfo);
		timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		timeLabel.setForeground(new Color(108, 117, 125));

		if (!todo.getLocation().isEmpty()) {
			JLabel locationLabel = new JLabel("📍 " + todo.getLocation());
			locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			locationLabel.setForeground(new Color(108, 117, 125));
			infoPanel.add(locationLabel);
		}

		infoPanel.add(nameLabel);
		infoPanel.add(timeLabel);

		// 버튼 패널
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.setBackground(new Color(248, 249, 250));

		JButton editButton = new JButton("수정");
		styleSmallButton(editButton, new Color(64, 128, 255));
		// 맥에서도 버튼 보이도록
		editButton.setContentAreaFilled(true);
		editButton.setOpaque(true);
		editButton.setBorderPainted(true);

		JButton deleteButton = new JButton("삭제");
		styleSmallButton(deleteButton, new Color(220, 53, 69));
		// 맥에서도 버튼 보이도록
		deleteButton.setContentAreaFilled(true);
		deleteButton.setOpaque(true);
		deleteButton.setBorderPainted(true);

		// 버튼 이벤트
		editButton.addActionListener(e -> {
			dialog.dispose();
			if (parent instanceof OurCalendar) {
				OurCalendar calendar = (OurCalendar) parent;
				ToDoForm editForm = new ToDoForm(
						todo.getStartYear(),
						todo.getStartMonth(),
						todo.getStartDay(),
						calendar,
						todo);
				editForm.showList();
			}
		});

		deleteButton.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(dialog,
					"정말로 삭제하시겠습니까?", "삭제 확인",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION) {
				list.remove(index);
				OurCalendar calObj = (OurCalendar) parent;
				calObj.saveFile();
				dialog.dispose();
				calObj.updateCal(false);

				JOptionPane.showMessageDialog(parent,
						"일정이 삭제되었습니다.", "삭제 완료",
						JOptionPane.INFORMATION_MESSAGE);

				// 남은 일정이 있으면 다시 표시
				List<SharedToDo> remainingShared = calObj.sharedTasks.get(dateKey);
				if (!list.isEmpty() || (remainingShared != null && !remainingShared.isEmpty())) {
					showDialogWithBothLists(parent, dateKey, list, remainingShared);
				}
			}
		});

		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);

		rowPanel.add(infoPanel, BorderLayout.CENTER);
		rowPanel.add(buttonPanel, BorderLayout.EAST);

		return rowPanel;
	}

	// 공유 일정 패널 생성 (수정 가능)
	private static JPanel createSharedTodoPanel(SharedToDo sharedTodo) {
		JPanel rowPanel = new JPanel(new BorderLayout());
		rowPanel.setBackground(new Color(255, 248, 220)); // 공유 일정 배경색
		rowPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(SHARED_COLOR, 2),
				new EmptyBorder(15, 15, 15, 15)));
		rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rowPanel.setMaximumSize(new Dimension(460, 80));

		// 일정 정보
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setBackground(new Color(255, 248, 220));

		JLabel nameLabel = new JLabel(sharedTodo.getTaskName() + " (공유)");
		nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		nameLabel.setForeground(new Color(33, 37, 41));

		String timeInfo = String.format("%02d:%02d - %02d:%02d",
				sharedTodo.getStartHour(), sharedTodo.getStartMinute(),
				sharedTodo.getEndHour(), sharedTodo.getEndMinute());
		JLabel timeLabel = new JLabel("🕐 " + timeInfo);
		timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		timeLabel.setForeground(new Color(108, 117, 125));

		JLabel creatorLabel = new JLabel("👤 " + sharedTodo.getCreator() + "님이 공유");
		creatorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		creatorLabel.setForeground(SHARED_COLOR.darker());

		if (!sharedTodo.getLocation().isEmpty()) {
			JLabel locationLabel = new JLabel("📍 " + sharedTodo.getLocation());
			locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			locationLabel.setForeground(new Color(108, 117, 125));
			infoPanel.add(locationLabel);
		}

		infoPanel.add(nameLabel);
		infoPanel.add(timeLabel);
		infoPanel.add(creatorLabel);

		rowPanel.add(infoPanel, BorderLayout.CENTER);

		return rowPanel;
	}

	// 공유 일정 패널 생성 (수정 가능한 버전)
	private static JPanel createSharedTodoPanelWithEdit(JDialog dialog, JFrame parent, SharedToDo sharedTodo,
			String dateKey) {
		JPanel rowPanel = new JPanel(new BorderLayout());
		rowPanel.setBackground(new Color(255, 248, 220)); // 공유 일정 배경색
		rowPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(SHARED_COLOR, 2),
				new EmptyBorder(15, 15, 15, 15)));
		rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rowPanel.setMaximumSize(new Dimension(460, 80));

		// 일정 정보
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setBackground(new Color(255, 248, 220));

		JLabel nameLabel = new JLabel(sharedTodo.getTaskName() + " (공유)");
		nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		nameLabel.setForeground(new Color(33, 37, 41));

		String timeInfo = String.format("%02d:%02d - %02d:%02d",
				sharedTodo.getStartHour(), sharedTodo.getStartMinute(),
				sharedTodo.getEndHour(), sharedTodo.getEndMinute());
		JLabel timeLabel = new JLabel("🕐 " + timeInfo);
		timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		timeLabel.setForeground(new Color(108, 117, 125));

		JLabel creatorLabel = new JLabel("👤 " + sharedTodo.getCreator() + "님이 공유");
		creatorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		creatorLabel.setForeground(SHARED_COLOR.darker());

		if (!sharedTodo.getLocation().isEmpty()) {
			JLabel locationLabel = new JLabel("📍 " + sharedTodo.getLocation());
			locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			locationLabel.setForeground(new Color(108, 117, 125));
			infoPanel.add(locationLabel);
		}

		infoPanel.add(nameLabel);
		infoPanel.add(timeLabel);
		infoPanel.add(creatorLabel);

		// 버튼 패널 (본인이 만든 공유 일정만 수정/삭제 가능)
		if (parent instanceof OurCalendar) {
			OurCalendar calendar = (OurCalendar) parent;
			if (sharedTodo.getCreator().equals(calendar.getNickname())) {
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
				buttonPanel.setBackground(new Color(255, 248, 220));

				JButton editButton = new JButton("수정");
				styleSmallButton(editButton, SHARED_COLOR.darker());

				JButton deleteButton = new JButton("삭제");
				styleSmallButton(deleteButton, new Color(220, 53, 69));

				editButton.addActionListener(e -> {
					dialog.dispose();
					ToDoForm editForm = new ToDoForm(
							sharedTodo.getStartYear(),
							sharedTodo.getStartMonth(),
							sharedTodo.getStartDay(),
							calendar,
							sharedTodo); // SharedToDo를 ToDo로 전달
					editForm.setSharedTodo(sharedTodo); // 원본 SharedToDo 정보 보관
					editForm.showList();
				});

				deleteButton.addActionListener(e -> {
					int confirm = JOptionPane.showConfirmDialog(dialog,
							"정말로 삭제하시겠습니까?\n모든 사용자에게서 삭제됩니다.",
							"공유 일정 삭제 확인",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (confirm == JOptionPane.YES_OPTION) {
						// 서버로 삭제 요청
						calendar.deleteSharedTask(sharedTodo.getId());

						// 로컬에서도 즉시 삭제
						calendar.deleteSharedTodo(sharedTodo.getId());

						dialog.dispose();

						JOptionPane.showMessageDialog(calendar,
								"공유 일정이 삭제되었습니다.", "삭제 완료",
								JOptionPane.INFORMATION_MESSAGE);

						// 남은 일정 있으면 다시 표시
						List<ToDo> remainingLocal = calendar.tasks.get(dateKey);
						List<SharedToDo> remainingShared = calendar.sharedTasks.get(dateKey);
						if ((remainingLocal != null && !remainingLocal.isEmpty()) ||
								(remainingShared != null && !remainingShared.isEmpty())) {
							showDialogWithBothLists(calendar, dateKey, remainingLocal, remainingShared);
						}
					}
				});

				buttonPanel.add(editButton);
				buttonPanel.add(deleteButton);
				rowPanel.add(buttonPanel, BorderLayout.EAST);
			}
		}

		rowPanel.add(infoPanel, BorderLayout.CENTER);

		return rowPanel;
	}

	// 작은 버튼 스타일링
	private static void styleSmallButton(JButton button, Color bgColor) {
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		button.setBorder(new EmptyBorder(6, 12, 6, 12));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
	}

	// 필드 채우기
	private void populateFields(ToDo todo) {
		titleField.setText(todo.getTaskName());
		titleField.setForeground(Color.BLACK);

		locationField.setText(todo.getLocation());
		locationField.setForeground(Color.BLACK);

		checkBox.setSelected(todo.isAllDay());

		startYearLabel.setText(todo.getStartYear() + "년");
		startMonthLabel.setText((todo.getStartMonth() + 1) + "월");
		startDayLabel.setText(todo.getStartDay() + "일");
		startHourLabel.setText(todo.getStartHour() + "시");
		startMinuteLabel.setText(todo.getStartMinute() + "분");

		endYearLabel.setText(todo.getEndYear() + "년");
		endMonthLabel.setText((todo.getEndMonth() + 1) + "월");
		endDayLabel.setText(todo.getEndDay() + "일");
		endHourLabel.setText(todo.getEndHour() + "시");
		endMinuteLabel.setText(todo.getEndMinute() + "분");

		alarmCombo.setSelectedItem(todo.getAlarm());
		repeatCombo.setSelectedItem(todo.getRepeat());

		memoArea.setText(todo.getMemo());
		memoArea.setForeground(Color.BLACK);
	}
}