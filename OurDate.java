
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

public class OurDate extends JLabel {

	private static final Color HOVER_COLOR = new Color(230, 240, 255);
	private static final Color SELECTED_COLOR = new Color(173, 216, 230);
	private static final Color NORMAL_COLOR = Color.WHITE;
	private static final Color BORDER_COLOR = new Color(233, 236, 239);
	private static final Color TASK_INDICATOR = new Color(52, 144, 220);

	private int date;
	private int week;
	HashMap<Integer, ToDo> tasks = new HashMap<Integer, ToDo>();
	private OurCalendar baseCal;
	private boolean isHovered = false;

	OurDate(int date, int week, OurCalendar baseCal) {
		super(String.valueOf(date), SwingConstants.CENTER);
		this.date = date;
		this.week = week;
		this.baseCal = baseCal;
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

	private Border createDateBorder() {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	private void setupMouseEvents() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				for (int i = 1; i < 32; i++) {
					if (baseCal.calendar[i] != null) {
						baseCal.calendar[i].setBackground(NORMAL_COLOR);
					}
				}

				baseCal.setCurrent(OurDate.this.date, OurDate.this.week);
				baseCal.selectDate();
				OurDate.this.setBackground(SELECTED_COLOR);

				String key = OurCalendar.getDateKey(baseCal.getYear(), baseCal.getMonth(), OurDate.this.date);
				List<ToDo> localList = baseCal.tasks.get(key);
				List<SharedToDo> sharedList = baseCal.sharedTasks.get(key);

				System.out.println("날짜 클릭: " + key + ", 로컬 일정: " +
						(localList != null ? localList.size() : 0) + "개, 공유 일정: " +
						(sharedList != null ? sharedList.size() : 0) + "개");

				if ((localList != null && !localList.isEmpty()) || (sharedList != null && !sharedList.isEmpty())) {
					ToDoForm.showDialogWithBothLists(baseCal, key, localList, sharedList);
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

	public void updateTaskStyle(boolean hasTask) {
		if (hasTask) {

			String currentText = getText();
			if (!currentText.contains("●")) {
				setText("<html><div style='text-align: center;'>" +
						date + "<br><span style='color: #3490dc; font-size: 8px;'>●</span></div></html>");
			}
		} else {
			setText(String.valueOf(date));
		}
	}

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