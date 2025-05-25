
public class ToDo {
	private String taskName;
	private String location;
	private boolean allDay;
	private int startYear, startMonth, startDay, startHour, startMinute;
	private int endYear, endMonth, endDay, endHour, endMinute;
	private String alarm;
	private String repeat;
	private String memo;

	public ToDo(String taskName, String location, boolean allDay, int startYear, int startMonth, int startDay,
			int startHour, int startMinute, int endYear, int endMonth, int endDay, int endHour, int endMinute,
			String alarm, String repeat, String memo) {
		this.setTaskName(taskName);
		this.setLocation(location);
		this.setAllDay(allDay);
		this.setStartYear(startYear);
		this.setStartMonth(startMonth);
		this.setStartDay(startDay);
		this.setStartHour(startHour);
		this.setStartMinute(startMinute);
		this.setEndYear(endYear);
		this.setEndMonth(endMonth);
		this.setEndDay(endDay);
		this.setEndHour(endHour);
		this.setEndMinute(endMinute);
		this.setAlarm(alarm);
		this.setRepeat(repeat);
		this.setMemo(memo);
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getStartDay() {
		return startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public int getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	public int getEndDay() {
		return endDay;
	}

	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getEndMinute() {
		return endMinute;
	}

	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}

	public String getAlarm() {
		return alarm;
	}

	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}