
/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 * 캘린더의 개별 날짜 셀을 나타내는 UI 컴포넌트 클래스
 * JLabel을 상속받아 날짜 표시와 마우스 이벤트 처리를 담당함
 */

import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

public class OurDate extends JLabel {

	// 날짜 셀 색상 상수 정의
	private static final Color HOVER_COLOR = new Color(230, 240, 255); // 마우스 오버 색상
	private static final Color SELECTED_COLOR = new Color(173, 216, 230); // 선택된 날짜 색상
	private static final Color NORMAL_COLOR = Color.WHITE; // 기본 배경색
	private static final Color BORDER_COLOR = new Color(233, 236, 239); // 테두리 색상

	// 날짜 정보
	private int date; // 날짜 (일)
	private int week; // 요일
	private OurCalendar baseCal; // 상위 캘린더 객체 참조

	/**
	 * OurDate 생성자 - 날짜 셀 초기화 및 이벤트 설정
	 * 
	 * @param date    날짜 (일)
	 * @param week    요일
	 * @param baseCal 상위 캘린더 객체
	 */
	OurDate(int date, int week, OurCalendar baseCal) {
		super(String.valueOf(date), SwingConstants.CENTER);
		this.date = date;
		this.week = week;
		this.baseCal = baseCal;

		// 기본 스타일 설정
		this.setOpaque(true);
		this.setBackground(NORMAL_COLOR);
		this.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setBorder(createDateBorder());
		this.setPreferredSize(new Dimension(80, 60));
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		setupMouseEvents();
	}

	/**
	 * 날짜 셀의 테두리 스타일 생성
	 * 
	 * @return 설정된 테두리
	 */
	private Border createDateBorder() {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * 마우스 이벤트 설정 - 클릭, 호버 효과 등
	 */
	private void setupMouseEvents() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 모든 날짜 셀의 배경색을 기본색으로 초기화
				for (int i = 1; i < 32; i++) {
					if (baseCal.calendar[i] != null) {
						baseCal.calendar[i].setBackground(NORMAL_COLOR);
					}
				}

				// 현재 선택된 날짜 설정
				baseCal.setCurrent(OurDate.this.date, OurDate.this.week);
				baseCal.selectDate();
				OurDate.this.setBackground(SELECTED_COLOR);

				// 해당 날짜의 일정 정보 조회
				String key = OurCalendar.getDateKey(baseCal.getYear(), baseCal.getMonth(), OurDate.this.date);
				List<ToDo> localList = baseCal.tasks.get(key);
				List<SharedToDo> sharedList = baseCal.sharedTasks.get(key);

				System.out.println("날짜 클릭: " + key + ", 로컬 일정: " +
						(localList != null ? localList.size() : 0) + "개, 공유 일정: " +
						(sharedList != null ? sharedList.size() : 0) + "개");

				// 일정이 있는 경우 일정 목록 다이얼로그 표시
				if ((localList != null && !localList.isEmpty()) || (sharedList != null && !sharedList.isEmpty())) {
					ToDoForm.showDialogWithBothLists(baseCal, key, localList, sharedList);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// 선택되지 않은 날짜인 경우 호버 색상 적용
				if (!getBackground().equals(SELECTED_COLOR)) {
					setBackground(HOVER_COLOR);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// 선택되지 않은 날짜인 경우 기본 색상으로 복원
				if (!getBackground().equals(SELECTED_COLOR)) {
					setBackground(NORMAL_COLOR);
				}
			}
		});
	}

	/**
	 * 날짜와 요일 정보 설정
	 * 
	 * @param date 날짜 (일)
	 * @param week 요일
	 */
	void setDate(int date, int week) {
		this.date = date;
		this.week = week;
		this.setText(String.valueOf(date));
	}

	/**
	 * 날짜 반환
	 * 
	 * @return 날짜 (일)
	 */
	int getDate() {
		return date;
	}

	/**
	 * 요일 반환
	 * 
	 * @return 요일
	 */
	int getWeek() {
		return week;
	}
}