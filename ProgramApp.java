/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 *        캘린더 애플리케이션의 메인 실행 클래스
 *        프로그램의 진입점으로 OurCalendar 객체를 생성하고 실행함
 */

public class ProgramApp {
	/**
	 * 프로그램 메인 실행 메서드
	 * 
	 * @param args 명령행 인자 (사용하지 않음)
	 */
	static public void main(String args[]) {
		// 캘린더 객체 생성 및 화면 표시
		OurCalendar calendar = new OurCalendar();
		calendar.showCalendar();
	}
}