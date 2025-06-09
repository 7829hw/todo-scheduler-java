/**
 * @author 자바 프로그래밍 5조
 * @version 1.0
 * @since 2025-05-07
 * 
 *        공유 일정을 나타내는 클래스 - ToDo 클래스를 상속받아 공유 기능을 추가
 *        생성자 정보와 고유 ID를 가지며, 네트워크 전송을 위한 직렬화 기능을 제공함
 */

public class SharedToDo extends ToDo {
    private String creator; // 일정 생성자 닉네임
    private String id; // 고유 식별자

    /**
     * SharedToDo 생성자 (새 ID 자동 생성)
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
     * @param creator     생성자 닉네임
     */
    public SharedToDo(String taskName, String location, boolean allDay,
            int startYear, int startMonth, int startDay, int startHour, int startMinute,
            int endYear, int endMonth, int endDay, int endHour, int endMinute,
            String alarm, String repeat, String memo, String creator) {
        super(taskName, location, allDay, startYear, startMonth, startDay, startHour, startMinute,
                endYear, endMonth, endDay, endHour, endMinute, alarm, repeat, memo);
        this.creator = creator;
        this.id = generateId(); // 고유 ID 자동 생성
    }

    /**
     * SharedToDo 생성자 (기존 ID 사용)
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
     * @param creator     생성자 닉네임
     * @param id          기존 ID
     */
    public SharedToDo(String taskName, String location, boolean allDay,
            int startYear, int startMonth, int startDay, int startHour, int startMinute,
            int endYear, int endMonth, int endDay, int endHour, int endMinute,
            String alarm, String repeat, String memo, String creator, String id) {
        super(taskName, location, allDay, startYear, startMonth, startDay, startHour, startMinute,
                endYear, endMonth, endDay, endHour, endMinute, alarm, repeat, memo);
        this.creator = creator;
        this.id = id;
    }

    /**
     * 고유 ID 생성 - 생성자명, 시간, 랜덤값 조합
     * 
     * @return 생성된 고유 ID
     */
    private String generateId() {
        return creator + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    /**
     * 생성자 닉네임 반환
     * 
     * @return 생성자 닉네임
     */
    public String getCreator() {
        return creator;
    }

    /**
     * 생성자 닉네임 설정
     * 
     * @param creator 생성자 닉네임
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 고유 ID 반환
     * 
     * @return 고유 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 고유 ID 설정
     * 
     * @param id 고유 ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 네트워크 전송용 문자열로 변환 (생성자와 ID 포함)
     * 
     * @return 파이프(|)로 구분된 모든 속성 문자열
     */
    public String toNetworkString() {
        return String.format("%s|%s|%b|%d|%d|%d|%d|%d|%d|%d|%d|%d|%d|%s|%s|%s|%s|%s",
                getTaskName(), getLocation(), isAllDay(),
                getStartYear(), getStartMonth(), getStartDay(), getStartHour(), getStartMinute(),
                getEndYear(), getEndMonth(), getEndDay(), getEndHour(), getEndMinute(),
                getAlarm(), getRepeat(), getMemo(), creator, id);
    }

    /**
     * 기본 ToDo 데이터로부터 SharedToDo 생성 (새 ID 생성)
     * 
     * @param data    파이프(|)로 구분된 ToDo 데이터 문자열
     * @param creator 생성자 닉네임
     * @return 생성된 SharedToDo 객체, 실패 시 null
     */
    public static SharedToDo fromNetworkString(String data, String creator) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 16)
                return null;

            SharedToDo todo = new SharedToDo(
                    parts[0], // 일정명
                    parts[1], // 장소
                    Boolean.parseBoolean(parts[2]), // 하루종일 여부
                    Integer.parseInt(parts[3]), // 시작년도
                    Integer.parseInt(parts[4]), // 시작월
                    Integer.parseInt(parts[5]), // 시작일
                    Integer.parseInt(parts[6]), // 시작시간
                    Integer.parseInt(parts[7]), // 시작분
                    Integer.parseInt(parts[8]), // 종료년도
                    Integer.parseInt(parts[9]), // 종료월
                    Integer.parseInt(parts[10]), // 종료일
                    Integer.parseInt(parts[11]), // 종료시간
                    Integer.parseInt(parts[12]), // 종료분
                    parts[13], // 알림
                    parts[14], // 반복
                    parts[15], // 메모
                    creator);

            System.out.println("SharedToDo 생성됨 - ID: " + todo.getId() + ", 제목: " + todo.getTaskName());
            return todo;
        } catch (Exception e) {
            System.err.println("SharedToDo 파싱 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 완전한 네트워크 문자열로부터 SharedToDo 생성 (생성자와 ID 포함)
     * 
     * @param data 파이프(|)로 구분된 완전한 SharedToDo 데이터 문자열
     * @return 생성된 SharedToDo 객체, 실패 시 null
     */
    public static SharedToDo fromCompleteNetworkString(String data) {
        try {
            String[] parts = data.split("\\|");
            System.out.println("SharedToDo 파싱 시도 - 파트 개수: " + parts.length + ", 데이터: " + data);

            if (parts.length < 17) {
                System.err.println("파트 개수 부족: " + parts.length + " < 17");
                return null;
            }

            String creator = parts[16];
            // ID가 없는 경우 레거시 ID 생성
            String id = parts.length >= 18 ? parts[17] : "legacy_" + System.currentTimeMillis();

            SharedToDo todo = new SharedToDo(
                    parts[0], parts[1], Boolean.parseBoolean(parts[2]),
                    Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]), Integer.parseInt(parts[7]), Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9]), Integer.parseInt(parts[10]), Integer.parseInt(parts[11]),
                    Integer.parseInt(parts[12]), parts[13], parts[14], parts[15], creator, id);

            System.out.println("SharedToDo 파싱 완료 - ID: " + todo.getId() + ", 제목: " + todo.getTaskName() + ", 생성자: "
                    + todo.getCreator());
            return todo;
        } catch (Exception e) {
            System.err.println("Complete SharedToDo 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 문자열 표현 반환 (일정명과 생성자 표시)
     * 
     * @return "일정명 (by 생성자)" 형태의 문자열
     */
    @Override
    public String toString() {
        return getTaskName() + " (by " + creator + ")";
    }
}