
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
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;

//라벨을 상속 받은 클래스
public class OurDate extends JLabel {
	// UI 테마 색상
	private static final Color HOVER_COLOR = new Color(230, 240, 255);
	private static final Color SELECTED_COLOR = new Color(173, 216, 230);
	private static final Color NORMAL_COLOR = Color.WHITE;
	private static final Color BORDER_COLOR = new Color(233, 236, 239);
	private static final Color TASK_INDICATOR = new Color(52, 144, 220);

	// 멤버 변수 선언
	private int date;
	private int week;
	HashMap<Integer, ToDo> tasks = new HashMap<Integer, ToDo>();
	private OurCalendar baseCal;
	private boolean isHovered = false;

	// 생성자
	OurDate(int date, int week, OurCalendar baseCal) {
		super(String.valueOf(date), SwingConstants.CENTER);
		this.date = date;
		this.week = week;
		this.baseCal = baseCal;
		this.setOpaque(true);
		this.setBackground(NORMAL_COLOR);
		this.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setBorder(createDateBorder());
		this.setPreferredSize(new Dimension(80, 60));
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		setupMouseEvents();
	}

	// 모던한 테두리 생성
	private Border createDateBorder() {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	// 마우스 이벤트 설정
	private void setupMouseEvents() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 이전 선택된 날짜들 초기화
				for (int i = 1; i < 32; i++) {
					if (baseCal.calendar[i] != null) {
						baseCal.calendar[i].setBackground(NORMAL_COLOR);
					}
				}

				baseCal.setCurrent(OurDate.this.date, OurDate.this.week);
				baseCal.selectDate();
				OurDate.this.setBackground(SELECTED_COLOR);

				// 선택한 날짜에 일정이 있으면 목록 보여주기
				String key = OurCalendar.getDateKey(baseCal.getYear(), baseCal.getMonth(), baseCal.getCurrentDay());
				List<ToDo> list = baseCal.tasks.get(key);

				if (list != null && !list.isEmpty()) {
					ToDoForm.showDialogWithList(baseCal, key, list);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!getBackground().equals(SELECTED_COLOR)) {
					setBackground(HOVER_COLOR);
				}
				isHovered = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!getBackground().equals(SELECTED_COLOR)) {
					setBackground(NORMAL_COLOR);
				}
				isHovered = false;
			}
		});
	}

	// 일정이 있는 날짜 스타일 업데이트
	public void updateTaskStyle(boolean hasTask) {
		if (hasTask) {
			// 일정이 있는 경우 하단에 작은 점 표시를 위한 HTML 사용
			String currentText = getText();
			if (!currentText.contains("●")) {
				setText("<html><div style='text-align: center;'>" +
						date + "<br><span style='color: #3490dc; font-size: 8px;'>●</span></div></html>");
			}
		} else {
			setText(String.valueOf(date));
		}
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