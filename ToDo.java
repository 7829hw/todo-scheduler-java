/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 *        개인 일정을 나타내는 기본 클래스
 *        일정의 모든 속성(제목, 시간, 장소, 알림 등)을 저장하고 관리함
 */

public class ToDo {
	// 일정 기본 정보
	private String taskName; // 일정 제목
	private String location; // 장소
	private boolean allDay; // 하루종일 여부

	// 시작 시간
	private int startYear, startMonth, startDay, startHour, startMinute;

	// 종료 시간
	private int endYear, endMonth, endDay, endHour, endMinute;

	// 부가 정보
	private String alarm; // 알림 설정
	private String repeat; // 반복 설정
	private String memo; // 메모

	/**
	 * ToDo 생성자 - 모든 속성을 초기화
	 * 
	 * @param taskName    일정 제목
	 * @param location    장소
	 * @param allDay      하루종일 여부
	 * @param startYear   시작 연도
	 * @param startMonth  시작 월 (0부터 시작)
	 * @param startDay    시작 일
	 * @param startHour   시작 시간
	 * @param startMinute 시작 분
	 * @param endYear     종료 연도
	 * @param endMonth    종료 월 (0부터 시작)
	 * @param endDay      종료 일
	 * @param endHour     종료 시간
	 * @param endMinute   종료 분
	 * @param alarm       알림 설정
	 * @param repeat      반복 설정
	 * @param memo        메모
	 */
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

	// Getter 및 Setter 메서드들

	/**
	 * 일정 제목 반환
	 * 
	 * @return 일정 제목
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * 일정 제목 설정
	 * 
	 * @param taskName 일정 제목
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * 시작 일 반환
	 * 
	 * @return 시작 일
	 */
	public int getStartDay() {
		return startDay;
	}

	/**
	 * 시작 일 설정
	 * 
	 * @param startDay 시작 일
	 */
	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	/**
	 * 장소 반환
	 * 
	 * @return 장소
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * 장소 설정
	 * 
	 * @param location 장소
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * 시작 연도 반환
	 * 
	 * @return 시작 연도
	 */
	public int getStartYear() {
		return startYear;
	}

	/**
	 * 시작 연도 설정
	 * 
	 * @param startYear 시작 연도
	 */
	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	/**
	 * 하루종일 여부 반환
	 * 
	 * @return 하루종일 여부
	 */
	public boolean isAllDay() {
		return allDay;
	}

	/**
	 * 하루종일 여부 설정
	 * 
	 * @param allDay 하루종일 여부
	 */
	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	/**
	 * 시작 월 반환
	 * 
	 * @return 시작 월 (0부터 시작)
	 */
	public int getStartMonth() {
		return startMonth;
	}

	/**
	 * 시작 월 설정
	 * 
	 * @param startMonth 시작 월 (0부터 시작)
	 */
	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	/**
	 * 시작 시간 반환
	 * 
	 * @return 시작 시간
	 */
	public int getStartHour() {
		return startHour;
	}

	/**
	 * 시작 시간 설정
	 * 
	 * @param startHour 시작 시간
	 */
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	/**
	 * 시작 분 반환
	 * 
	 * @return 시작 분
	 */
	public int getStartMinute() {
		return startMinute;
	}

	/**
	 * 시작 분 설정
	 * 
	 * @param startMinute 시작 분
	 */
	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	/**
	 * 종료 연도 반환
	 * 
	 * @return 종료 연도
	 */
	public int getEndYear() {
		return endYear;
	}

	/**
	 * 종료 연도 설정
	 * 
	 * @param endYear 종료 연도
	 */
	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	/**
	 * 종료 월 반환
	 * 
	 * @return 종료 월 (0부터 시작)
	 */
	public int getEndMonth() {
		return endMonth;
	}

	/**
	 * 종료 월 설정
	 * 
	 * @param endMonth 종료 월 (0부터 시작)
	 */
	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	/**
	 * 종료 일 반환
	 * 
	 * @return 종료 일
	 */
	public int getEndDay() {
		return endDay;
	}

	/**
	 * 종료 일 설정
	 * 
	 * @param endDay 종료 일
	 */
	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}

	/**
	 * 종료 시간 반환
	 * 
	 * @return 종료 시간
	 */
	public int getEndHour() {
		return endHour;
	}

	/**
	 * 종료 시간 설정
	 * 
	 * @param endHour 종료 시간
	 */
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	/**
	 * 종료 분 반환
	 * 
	 * @return 종료 분
	 */
	public int getEndMinute() {
		return endMinute;
	}

	/**
	 * 종료 분 설정
	 * 
	 * @param endMinute 종료 분
	 */
	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}

	/**
	 * 알림 설정 반환
	 * 
	 * @return 알림 설정
	 */
	public String getAlarm() {
		return alarm;
	}

	/**
	 * 알림 설정
	 * 
	 * @param alarm 알림 설정
	 */
	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	/**
	 * 반복 설정 반환
	 * 
	 * @return 반복 설정
	 */
	public String getRepeat() {
		return repeat;
	}

	/**
	 * 반복 설정
	 * 
	 * @param repeat 반복 설정
	 */
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	/**
	 * 메모 반환
	 * 
	 * @return 메모
	 */
	public String getMemo() {
		return memo;
	}

	/**
	 * 메모 설정
	 * 
	 * @param memo 메모
	 */
	public void setMemo(String memo) {
		this.memo = memo;
	}
}