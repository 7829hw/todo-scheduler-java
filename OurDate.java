
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 이 클래스는 달력의 cell의 정보를 담고, 다루는 클래스 입니다.
 * 이름이 OurDate인 이유는 이미 존재하는 Date 클래스와 구별하기 위해서 입니다.
 */
import java.util.HashMap;
import java.util.List;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;

//라벨을 상속 받은 클래스
public class OurDate extends JLabel {
	// 멤버 변수 선언
	private int date; // 날짜
	private int week; // (일 ~ 토, 1 ~ 7)
	HashMap<Integer, ToDo> tasks = new HashMap<Integer, ToDo>(); // 할 일을 저장하는 HashMap 자료구조
	private OurCalendar baseCal; // 베이스가 되는 OurCalendar 객체를 참조

	// 생성자
	OurDate(int date, int week, OurCalendar baseCal) {
		super(String.valueOf(date), SwingConstants.CENTER); // 부모인 JLabel의 생성자 호출
		this.date = date;
		this.week = week;
		this.baseCal = baseCal;
		this.setOpaque(true); // 라벨의 색이 변경될 수 있도록 허락하는 함수

		// 생성과 동시에 마우스 입력이벤트 추가
		this.addMouseListener(new MouseAdapter() {
			// @override
			// 마우스 클릭 시 인식
			public void mouseClicked(MouseEvent e) {
				baseCal.setCurrent(OurDate.this.date, OurDate.this.week); // 달력이 가리키는 현재 날짜 변경
				baseCal.selectDate(); // Calendar에서 할 일을 추가하는 함수 참조
				OurDate.this.setBackground(new Color(173, 216, 230)); // 색깔을 변경

				// 선택한 날짜에 일정이 있으면 목록 보여주기
				String key = OurCalendar.getDateKey(baseCal.getYear(), baseCal.getMonth(), baseCal.getCurrentDay());
				List<ToDo> list = baseCal.tasks.get(key);

				if (list != null && !list.isEmpty()) {
					ToDoForm.showDialogWithList(baseCal, key, list);
				}
			}
		});
	}

	// 날짜 객체의 날짜와 그 요일을 변경
	void setDate(int date, int week) {
		this.date = date;
		this.week = week;
		this.setText(String.valueOf(date));
	}

	int getDate() {
		return date;
	}

	int getWeek() {
		return week;
	}

}
