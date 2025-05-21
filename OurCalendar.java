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
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class OurCalendar extends JFrame {
	//멤버 변수 선언
	private int year;
	private int month;
	private int lastDay;
	private int currentDay;
	private int currentWeek;
	public OurDate calendar[] = new OurDate[32]; //현재 날짜의 정보는 claendar[currentDay]로 접근 가능
	public Calendar cal = Calendar.getInstance();
	HashMap<String, List<ToDo>> tasks = new HashMap<>(); // "년-월-일" : ToDo 연결
	private ReminderService reminderService = new ReminderService();

	
	//위젯 관련 멤버 변수
	JPanel headPanel; //달력 위쪽을 표현할 위젯
	JLabel monthLabel; //달력의 년-월을 표현할 위젯
	JPanel calPanel; //달력의 날짜를 표현할 위젯
	//<<수정부분>>
	JButton prevButton;
	JButton nextButton;
	
	//<멤버 메소드 선언 및 정의>
	//생성자
	OurCalendar() {
		this.year = cal.get(cal.YEAR);
		this.month = cal.get(cal.MONTH); //1월(0) ~ 12월(11)로 저장됨
		this.lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		this.currentDay = 0;
		this.currentWeek = 0;
		
		for(int day = 1; day < 32; day++) {
			calendar[day] = new OurDate(0, 0, this);
			calendar[day].setBackground(Color.white);
		}
		
		//<<수정 부분>>
		//달력의 크기 바뀔때마다 글자 크기 조정
		this.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e) {
				if(OurCalendar.this.calPanel != null)
					OurCalendar.this.updateCal();
			}
		});
		openFile();
	}
	
	//달력을 출력하는 메소드
	void showCalendar() {
		openFile();
		
		setTitle("Calendar"); 
		setSize(700, 500); //달력 panel 크기
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		// 위젯 닫힐 때 현재 달의 할 일 저장
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveFile();
			}
		});
		
		//위쪽의 달력을 넘기는 버튼과 년-월 추가
		headPanel = new JPanel();
		monthLabel = new JLabel();
		//<<수정 부분>>
		prevButton = new JButton("<");
		nextButton = new JButton(">");
		headPanel.add(prevButton);
		headPanel.add(monthLabel);
		headPanel.add(nextButton);
		
		//버튼 이벤트 설정
		prevButton.addActionListener(e -> {
			saveFile(); // 현재 달의 할 일 저장
			cal.add(cal.MONTH, -1);
			updateCal();
		});
		nextButton.addActionListener(e -> {
			saveFile(); // 현재 달의 할 일 저장
			cal.add(cal.MONTH, 1);
			updateCal();
		});
		
		JButton appendButton = new JButton("+");
		appendButton.addActionListener(e -> {
			this.showToDoList();
		});
		
		//달력의 요일 밑 날짜를 담을 열이 7개인 배열형 위젯
		calPanel = new JPanel(new GridLayout(0, 7));
		
		setLayout(new BorderLayout()); //달력의 레이아웃을 BorderLaydout()으로 설정
		add(headPanel, BorderLayout.NORTH);
		add(calPanel, BorderLayout.CENTER);
		add(appendButton, BorderLayout.SOUTH);
		
		updateCal();
		setVisible(true); //위젯들이 보이도록 설정
		
		startReminderTimer(); 
	}
	
	//달력을 업데이트 하는 함수
	void updateCal() {
		calPanel.removeAll(); //배열형 위젯 초기화
		calPanel.setBackground(Color.white);
		currentDay = 0;
		currentWeek = 0;
		
		//<<수정 부분>>
		this.prevButton.setPreferredSize(new Dimension(this.getWidth() / 17, this.getHeight() / 20));
		this.nextButton.setPreferredSize(new Dimension(this.getWidth() / 17, this.getHeight() / 20));
		//요일 설정
		String[] days = {"일", "월", "화", "수", "목", "금", "토"};
		for(String day : days) {
			JLabel label = new JLabel(day, SwingConstants.CENTER);
			label.setFont(label.getFont().deriveFont(this.getWidth() * this.getHeight() / 25000.0f));
			if(day == "일")
				label.setForeground(Color.red);
			else if(day == "토")
				label.setForeground(Color.blue);
			else
				label.setForeground(Color.black);
			calPanel.add(label);
		}
		
		//현재의 년, 월, 마지막 날을 가져옴
		year = cal.get(cal.YEAR);
		month = cal.get(cal.MONTH);
		
		//년-월 위젯을 추가
		monthLabel.setText(String.format("%d년 %d월", year, month + 1));
		monthLabel.setFont(monthLabel.getFont().deriveFont(this.getWidth() * this.getHeight() / 25000.0f));
		
		openFile();
		//현재 날짜를 현재 년도의 월의 1일로 변경
		cal.set(year, month, 1);
		//그 후 현재 날짜의 마지막 날짜를 가져옴
		lastDay = cal.getActualMaximum(cal.DAY_OF_MONTH);
		//맨 처음 날짜의 요일을 구함
		int dayWeek = cal.get(cal.DAY_OF_WEEK);
		
		//앞쪽의 빈칸을 채움
		for(int i = 1; i < dayWeek; i++) {
			calPanel.add(new JLabel(""));
		}
		
		//여기서 부터 날짜를 채움
		for(int day = 1; day <= lastDay; day++) {
			calendar[day].setDate(day, dayWeek);
			calendar[day].setBackground(Color.white);
			
			//  일정이 있으면 제목 표시
		    String key = getDateKey(year, month, day);
		    List<ToDo> todos = tasks.get(key);
		    if (todos != null && !todos.isEmpty()) {
		        calendar[day].setText(day + " " + todos.get(0).getTaskName()); // 여러 일정 중 첫 번째만
		    }
		    
		    //<<수정 부분>>
		    if(dayWeek == 1) {
		    	dayWeek++;
		    	calendar[day].setForeground(Color.red);
		    }
		    else if(dayWeek == 7) {
		    	dayWeek = 1;
		    	calendar[day].setForeground(Color.blue);
		    }
		    else {
		    	dayWeek++;
		    	calendar[day].setForeground(Color.black);
		    }
		    
		    calendar[day].setFont( calendar[day].getFont().deriveFont(this.getWidth() * this.getHeight() / 25000.0f));
			calPanel.add(calendar[day]);
		}
		
		calPanel.revalidate();
		calPanel.repaint();
	}
	
	//달력이 가리키는 현재 날자를 변경하는 함수
	void setCurrent(int day, int week) {
		this.currentDay = day;
		this.currentWeek = week;
	}
	
	//할 일을 추가하고 지우는 화면으로 넘어가는 함수
	void selectDate() {
		//현재 가리키는 날짜 출력(임시)
		System.out.printf("%d일 %d요일\n", this.currentDay, this.currentWeek);
		//날짜 JLabel 색을 전부 흰색으로 변경
		for(int i = 1; i < 32; i++) {
			calendar[i].setBackground(Color.white);
		}
	}
	
	//오늘 할 일을 추가, 삭제, 수정 하는 함수
	void showToDoList() {
		//날짜가 선택 안 된 상태면 return
		if(currentDay == 0 || currentWeek == 0)
			return;
		System.out.printf("현재: %d일 %d요일\n", this.currentDay, this.currentWeek);
		System.out.println("showToDoList 호출되었음!");
		ToDoForm todoForm = new ToDoForm(year, month, currentDay, this);
		todoForm.showList();
	}
	
	public static String getDateKey(int year, int month, int day) {
	    return String.format("%04d-%02d-%02d", year, month + 1, day); 
	}
	
	//현재 달의 할 일 리스트를 파일로 저장하는 함수
	void saveFile() {
		FileManager.saveToFile(year, month, tasks);
		System.out.println("할 일 데이터 저장 완료: " + year + "-" + (month + 1));
	}
	
	//달이 변경되었을 때, 그 달의 할 일 리스트를 가져오는 함수
	void openFile() {
		tasks = FileManager.loadFromFile(year, month);
		System.out.println("할 일 데이터 로드 완료: " + year + "-" + (month + 1));
	}
	
	void startReminderTimer() {
	    Timer timer = new Timer(20 * 1000, e -> {
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