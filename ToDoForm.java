import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

public class ToDoForm extends JDialog {
	// JDialog -> 팝업창 만들 수 있는 스윙 컴포넌트

	private int year;
	private int month;
	private int day;
	private ToDo todo;
	private boolean editMode = false;
	OurCalendar baseCal;
	JTextField titleField = new JTextField();
	JTextField locationField = new JTextField("장소");
	JCheckBox checkBox = new JCheckBox();
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

	// 입력하기 전에는 회색 글자로 타이틀 -> 입력 시작하면 지워짐
	public void applyPlaceholder(JTextComponent field, String placeholderText) {
		field.setText(placeholderText);
		field.setForeground(Color.GRAY);

		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (field.getText().equals(placeholderText)) {
					field.setText("");
					field.setForeground(Color.BLACK);
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
		dropdown.setBackground(Color.LIGHT_GRAY);
		dropdown.setForeground(Color.BLACK);
		dropdown.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		dropdown.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
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
		});

		return dropdown;
	}

	// 새 일정 추가 UI
	void showList() {
		setTitle("Add Schedule");
		setSize(300, 450);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// 전체 레이아웃
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // 컴포넌트를 세로로 정렬
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // 테두리 부분에 여백을 줌

		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel();
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		titleLabel.setText("New");
		titlePanel.add(titleLabel);
		mainPanel.add(titlePanel);

		// 제목 입력
		titleField.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		titleField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		applyPlaceholder(titleField, "새로운 이벤트");
		mainPanel.add(titleField);

		// 더미 패널을 포커스 타깃으로 추가
		JPanel dummyFocus = new JPanel();
		dummyFocus.setFocusable(true); // 이게 없으면 requestFocusInWindow 안 먹힘
		mainPanel.add(dummyFocus);

		// 장소 입력
		locationField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		locationField.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		applyPlaceholder(locationField, "장소");
		mainPanel.add(locationField);

		// 빈 칸 + 구분선
		mainPanel.add(Box.createVerticalStrut(10)); // 10px 간격 확보
		JSeparator separator = new JSeparator();
		mainPanel.add(separator);

		// 하루종일 체크박스
		JPanel checkPanel = new JPanel(new BorderLayout());

		checkPanel.add(new JLabel("하루종일"), BorderLayout.WEST);
		checkPanel.add(checkBox, BorderLayout.EAST);
		mainPanel.add(checkPanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// 기간 - 시작
		JPanel startPanel = new JPanel(new BorderLayout());
		startPanel.add(new JLabel("시작"), BorderLayout.WEST);

		JPanel start = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
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
		mainPanel.add(startPanel);

		// 기간 - 종료
		JPanel finishPanel = new JPanel(new BorderLayout());
		finishPanel.add(new JLabel("종료"), BorderLayout.WEST);

		JPanel end = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
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
		mainPanel.add(finishPanel);

		mainPanel.add(new JPanel());
		JSeparator separator2 = new JSeparator();
		mainPanel.add(separator2);

		// 알림 설정
		alarmCombo = new JComboBox<>(new String[] {
				"없음", "10분 전", "30분 전", "1시간 전", "하루 전"
		});
		JPanel alarmPanel = new JPanel(new BorderLayout());
		alarmPanel.add(new JLabel("알림"), BorderLayout.WEST);
		alarmPanel.add(alarmCombo, BorderLayout.EAST);
		mainPanel.add(alarmPanel);

		// 반복 일정
		repeatCombo = new JComboBox<>(new String[] {
				"없음", "매일", "매주", "매달", "매년"
		});
		JPanel repeatPanel = new JPanel(new BorderLayout());
		repeatPanel.add(new JLabel("반복"), BorderLayout.WEST);
		repeatPanel.add(repeatCombo, BorderLayout.EAST);
		mainPanel.add(repeatPanel);

		mainPanel.add(new JPanel());

		// 메모
		memoArea = new JTextArea(5, 30);
		memoArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		applyPlaceholder(memoArea, " 메모");
		mainPanel.add(memoArea);

		// 확인/취소 버튼
		JPanel buttonPanel = new JPanel();
		JButton saveBtn = new JButton("확인");
		saveBtn.addActionListener(e -> {
			String date = OurCalendar.getDateKey(year, month, day);
			ToDo newData = getToDo();

			if (editMode && todo != null) {
				// 기존 객체 수정
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

				// 달력에 제목 다시 표시
				baseCal.calendar[day].setText(day + " " + todo.getTaskName());
			} else {
				// 새로 추가
				baseCal.tasks.computeIfAbsent(date, k -> new ArrayList<>()).add(newData);
				baseCal.calendar[day].setText(day + " " + newData.getTaskName());
			}
			dispose();
		});

		JButton cancelBtn = new JButton("취소");
		cancelBtn.addActionListener(e -> dispose());

		buttonPanel.add(saveBtn);
		buttonPanel.add(cancelBtn);
		mainPanel.add(buttonPanel);

		add(mainPanel);

		// 수정할 때 이미 입력된 필드 채움
		if (todo != null) {
			populateFields(todo);
		}

		setVisible(true);
		SwingUtilities.invokeLater(() -> dummyFocus.requestFocusInWindow());
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

		System.out.println(">>> [addToDo] 저장된 날짜 key: " + date);
		System.out.println(">>> [addToDo] 저장된 일정 제목: " + todo.getTaskName());

		// 해당 날짜 전체 출력
		System.out.println(">>> [addToDo] 해당 날짜의 전체 일정 목록:");
		for (ToDo t : baseCal.tasks.get(date)) {
			System.out.println(" - " + t.getTaskName());
		}
		// 달력에 일정 제목 표시 -> 더 수정해야함
		baseCal.calendar[day].setText(day + " " + todo.getTaskName());
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
		for (int i = 0; i < 31; i++) {
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

	// 수정용 생성자
	public ToDoForm(int year, int month, int day, OurCalendar baseCal, ToDo existingToDo) {
		this(year, month, day, baseCal);
		this.todo = existingToDo;
	}

	// 추가되어 있는 일정 목록 UI - 수정, 삭제 기능 포함
	public static void showDialogWithList(JFrame parent, String dateKey, List<ToDo> list) {
		JDialog dialog = new JDialog(parent);
		dialog.setTitle(dateKey + " 일정 목록");
		dialog.setSize(440, 350);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		JLabel dayLabel = new JLabel(dateKey);
		dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(dayLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new JSeparator());
		mainPanel.add(Box.createVerticalStrut(10));

		for (int i = 0; i < list.size(); i++) {
			ToDo todo = list.get(i);
			int index = i; // 인덱스 통해 접근 가능

			JPanel rowPanel = new JPanel();
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
			rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			rowPanel.setMaximumSize(new Dimension(420, 30));
			rowPanel.setOpaque(false);

			JLabel nameLabel = new JLabel(todo.getTaskName());
			nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
			nameLabel.setPreferredSize(new Dimension(130, 22));

			String start = String.format("%02d:%02d", todo.getStartHour(), todo.getStartMinute());
			JLabel startLabel = new JLabel("시작 " + start);
			startLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
			startLabel.setPreferredSize(new Dimension(90, 22));

			String end = String.format("%02d:%02d", todo.getEndHour(), todo.getEndMinute());
			JLabel endLabel = new JLabel("종료 " + end);
			endLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
			endLabel.setPreferredSize(new Dimension(90, 22));

			JButton editButton = new JButton("수정");
			editButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			editButton.setMargin(new Insets(2, 5, 2, 5));
			editButton.addActionListener(e -> {
				dialog.dispose(); // 기존 다이얼로그 닫기
				if (parent instanceof OurCalendar) {
					OurCalendar calendar = (OurCalendar) parent;

					ToDoForm editForm = new ToDoForm(
							todo.getStartYear(),
							todo.getStartMonth() - 1, // 내부에서 +1 하므로 -1
							todo.getStartDay(),
							calendar,
							todo);
					editForm.showList(); // 기존 일정 내용 자동 채움
				}
			});

			JButton deleteButton = new JButton("삭제");
			deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
			deleteButton.setMargin(new Insets(2, 5, 2, 5));
			deleteButton.addActionListener(e -> {
				int confirm = JOptionPane.showConfirmDialog(dialog, "삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					// 리스트에서 항목 제거
					list.remove(index);

					// OurCalendar 객체 가져오기
					OurCalendar calObj = (OurCalendar) parent;

					// 중요: 데이터 삭제 후 바로 파일에 저장
					calObj.saveFile();

					// 다이얼로그 닫기
					dialog.dispose();

					// 해당 날짜의 캘린더 표시 업데이트
					int day = Integer.parseInt(dateKey.split("-")[2]);
					if (list.isEmpty()) {
						// 모든 일정이 삭제된 경우 날짜만 표시
						calObj.calendar[day].setText(String.valueOf(day));
					} else {
						// 첫 번째 일정 표시
						calObj.calendar[day].setText(day + " " + list.get(0).getTaskName());
					}

					// UI 갱신 (파일 로드하지 않고 현재 메모리 상태로 표시)
					calObj.updateCal(false);

					// 성공 메시지 표시
					JOptionPane.showMessageDialog(parent,
							"일정이 삭제되었습니다.",
							"삭제 완료",
							JOptionPane.INFORMATION_MESSAGE);

					// 리스트가 남아있으면 갱신된 목록 다시 표시
					if (!list.isEmpty()) {
						showDialogWithList(parent, dateKey, list);
					}
				}
			});

			rowPanel.add(nameLabel);
			rowPanel.add(Box.createHorizontalStrut(10));
			rowPanel.add(startLabel);
			rowPanel.add(Box.createHorizontalStrut(10));
			rowPanel.add(endLabel);
			rowPanel.add(Box.createHorizontalStrut(10));
			rowPanel.add(editButton);
			rowPanel.add(Box.createHorizontalStrut(5));
			rowPanel.add(deleteButton);

			mainPanel.add(rowPanel);
			mainPanel.add(Box.createVerticalStrut(5));
		}

		mainPanel.add(Box.createVerticalGlue());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton closeButton = new JButton("닫기");
		closeButton.addActionListener(e -> dialog.dispose());
		buttonPanel.add(closeButton);

		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(buttonPanel);

		dialog.add(mainPanel);
		dialog.setVisible(true);
	}

	// 필드 채우가
	private void populateFields(ToDo todo) {
		titleField.setText(todo.getTaskName());
		locationField.setText(todo.getLocation());
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
	}
}
