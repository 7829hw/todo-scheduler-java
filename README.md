## 자바 To-Do List 및 일정 관리 애플리케이션 프로젝트 구조

### 1. 프로젝트 목표

*   할 일(Task) 추가, 삭제, 수정, 조회
*   할 일의 카테고리(예: 업무, 개인) 구분
*   **날짜 기반 일정 확인 (특정 날짜, 특정 월 조회)**
*   일정 알림 (마감일 임박 시)
*   데이터 영구 저장/불러오기 (파일 입출력)
*   사용자 입력 예외 처리
*   여러 할 일 객체 관리 (제네릭, 컬렉션 활용)

### 2. 활용할 주요 자바 기능

*   객체 지향 프로그래밍 (클래스, 상속 - 필요시)
*   제네릭 (`ArrayList<Task>`)
*   컬렉션 프레임워크 (`List`, `Map`, `ArrayList`)
*   Stream API (데이터 처리 및 필터링)
*   파일 입출력 (`ObjectInputStream`, `ObjectOutputStream`, `Serializable`)
*   예외 처리 (`try-catch`, 사용자 정의 예외)
*   Thread 및 동시성 (`Runnable`, `ScheduledExecutorService`)
*   날짜 및 시간 API (`java.util.Date`, `java.util.Calendar`, `java.text.SimpleDateFormat`)

### 3. 패키지 구조

```
com.yourteamname.todolistapp  // 최상위 패키지 (팀 이름이나 프로젝트 이름으로 변경 가능)
├── main                      // 애플리케이션 실행 관련
│   └── Main.java
├── model                     // 데이터 모델 (Entity)
│   └── Task.java
├── repository                // 데이터 저장소 접근 로직
│   └── TaskRepository.java
├── service                   // 비즈니스 로직 및 서비스
│   ├── TaskService.java
│   ├── FileService.java
│   └── SchedulerService.java
├── ui                        // 사용자 인터페이스 (콘솔)
│   └── ConsoleUI.java
└── exception                 // 사용자 정의 예외
    └── InvalidInputException.java
```

### 4. 클래스별 주요 역할 및 메소드 인터페이스

| 클래스명                      | 주요 역할                                                                 | 주요 메소드 (시그니처)                                                                                                                                                                                             |
| :-------------------------- | :------------------------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`model.Task`**            | 할 일 정보(ID, 제목, 내용, 카테고리, 마감일, 완료여부)를 담는 데이터 객체. `Serializable`. | `Task(String, String, String, Date)`, `getId()`, `getTitle()`, `setTitle(String)`, `getDescription()`, `setDescription(String)`, `getCategory()`, `setCategory(String)`, `getDueDate()`, `setDueDate(Date)`, `isCompleted()`, `setCompleted(boolean)`, `updateNextId(int)`, `toString()` |
| **`repository.TaskRepository`** | `List<Task>` 형태로 할 일 데이터를 메모리에 저장하고 관리.                       | `TaskRepository()`, `addTask(Task)`, `findTaskById(int): Optional<Task>`, `findAllTasks(): List<Task>`, `updateTask(Task): boolean`, `deleteTask(int): boolean`, `setTasks(List<Task>)`                                  |
| **`service.TaskService`**     | 할 일 관련 핵심 비즈니스 로직 처리. `TaskRepository`를 사용.                      | `TaskService(TaskRepository)`, `createTask(String...)`, `getTaskById(int)`, `getAllTasks()`, `updateTask(int, String...)`, `deleteTask(int)`, `getTasksByCategory(String)`, `markTaskAsCompleted(int)`, `markTaskAsIncomplete(int)`, `getTasksByDate(Date): List<Task>`, `getTasksByMonth(int, int): Map<Integer, List<Task>>` |
| **`service.FileService`**     | 할 일 데이터를 파일에 영구 저장하고 불러오는 로직 담당. 객체 직렬화/역직렬화 사용.           | `FileService(String filePath)`, `saveTasksToFile(List<Task>) throws IOException`, `loadTasksFromFile(): List<Task> throws IOException, ClassNotFoundException`                                                        |
| **`service.SchedulerService`**| 백그라운드 스레드에서 주기적으로 마감일 임박 할 일 알림 기능 담당. `Runnable` 구현.       | `SchedulerService(TaskService)`, `startScheduler()`, `stopScheduler()`, `run()`                                                                                                                                |
| **`ui.ConsoleUI`**          | 콘솔 기반 사용자 인터페이스 제공. 사용자 입력 처리 및 결과 출력.                  | `ConsoleUI(TaskService, FileService, SchedulerService)`, `start()`, `displayMenu()`, `handleAddTask()`, `handleViewAllTasks()`, `handleViewTaskById()`, ... (각 메뉴 핸들러), `handleViewTasksBySpecificDate()`, `handleViewTasksByMonth()`, `getStringInput(String)`, `getIntInput(String)`, `getDateInput(String)`, `getDayOfWeekString(int)`, `loadTasks()`, `saveTasks()` |
| **`exception.InvalidInputException`** | 사용자 입력 값 오류 등 특정 예외 상황을 나타내는 사용자 정의 예외 클래스.           | `InvalidInputException(String message)`, `InvalidInputException(String message, Throwable cause)`                                                                                                                    |
| **`main.Main`**             | 애플리케이션의 시작점. 각 컴포넌트(서비스, UI) 객체를 생성하고 `ConsoleUI`를 실행. | `main(String[] args)`                                                                                                                                                                                                 |

### 5. 데이터 흐름 (예시: 할 일 추가)

1.  **`ConsoleUI`**: 사용자로부터 할 일 정보 입력 받음 (`handleAddTask()`).
2.  **`ConsoleUI`**: 입력 값 유효성 검사 (필요시 `InvalidInputException` 발생 및 처리).
3.  **`ConsoleUI`**: `TaskService.createTask()` 메소드 호출 (입력 값 전달).
4.  **`TaskService`**: 전달받은 정보로 `Task` 객체 생성.
5.  **`TaskService`**: `TaskRepository.addTask()` 메소드 호출 (`Task` 객체 전달).
6.  **`TaskRepository`**: `List<Task>`에 `Task` 객체 추가.
7.  (성공/실패 여부 또는 생성된 객체 정보가 `TaskService`를 거쳐 `ConsoleUI`로 반환됨)
8.  **`ConsoleUI`**: 사용자에게 처리 결과 메시지 출력.

### 6. 주요 기능별 구현 담당 (4명 역할 분담 요약)

1.  **데이터 모델 및 저장소 담당:** `Task.java`, `TaskRepository.java`
2.  **핵심 비즈니스 로직 및 날짜 기반 서비스 담당:** `TaskService.java`
3.  **사용자 인터페이스 및 예외 처리 담당:** `ConsoleUI.java`, `InvalidInputException.java`, `Main.java`
4.  **파일 입출력 및 백그라운드 스케줄링 담당:** `FileService.java`, `SchedulerService.java`

### 7. 추가 고려 사항

*   **날짜/시간 처리:** 현재 `java.util.Date` 및 `Calendar`를 주로 사용. Java 8 이상 환경이라면 `java.time` 패키지(LocalDate, LocalDateTime 등) 사용을 고려하면 더욱 편리하고 직관적인 코드 작성이 가능.
*   **코드 스타일 및 컨벤션:** 팀 내에서 일관된 코드 스타일과 네이밍 컨벤션을 정하고 따르는 것이 좋음.
*   **테스트:** 각 기능 단위 테스트(JUnit 등)를 작성하면 코드의 안정성을 높일 수 있음 (초보 단계에서는 필수는 아니지만 좋은 습관).
*   **문서화:** Javadoc 등을 활용하여 클래스 및 중요 메소드에 대한 설명을 추가하면 유지보수 및 협업에 도움.
*   **Git 활용:** 기능 개발 시 브랜치를 활용하고, Pull Request 및 코드 리뷰를 통해 협업.


---
### 8. 인터페이스

**1. `model.Task.java`**

```java
package model;

import java.io.Serializable;
import java.text.SimpleDateFormat; // toString을 위해 추가
import java.util.Date;

public class Task implements Serializable {
    // Fields
    // private int id;
    // private String title;
    // private String description;
    // private String category;
    // private Date dueDate;
    // private boolean isCompleted;
    // private static int nextId = 1;
    // private static final long serialVersionUID = 1L;

    // Constructor
    public Task(String title, String description, String category, Date dueDate);

    // Getters
    public int getId();
    public String getTitle();
    public String getDescription();
    public String getCategory();
    public Date getDueDate();
    public boolean isCompleted();

    // Setters
    public void setTitle(String title);
    public void setDescription(String description);
    public void setCategory(String category);
    public void setDueDate(Date dueDate);
    public void setCompleted(boolean completed);

    // Static ID Management
    public static void updateNextId(int maxId);

    // Override
    @Override
    public String toString(); // 사용자에게 보여줄 Task 정보 형식 (날짜 포맷 포함)
}
```

---

**2. `repository.TaskRepository.java`**

```java
package repository;

import model.Task;
import java.util.List;
import java.util.Optional;

public class TaskRepository {
    // Fields
    // private List<Task> tasks;

    // Constructor
    public TaskRepository();

    // CRUD & Data Management Methods
    public void addTask(Task task);
    public Optional<Task> findTaskById(int id);
    public List<Task> findAllTasks();
    public boolean updateTask(Task updatedTask);
    public boolean deleteTask(int id);
    public void setTasks(List<Task> newTasks); // 파일에서 로드 시 사용
}
```

---

**3. `service.TaskService.java`**

```java
package service;

import model.Task;
import repository.TaskRepository; // 의존성
import java.util.Date;
import java.util.List;
import java.util.Map; // getTasksByMonth 반환 타입
import java.util.Optional;

public class TaskService {
    // Fields
    // private TaskRepository taskRepository;

    // Constructor
    public TaskService(TaskRepository taskRepository);

    // Core Task Management Methods
    public Task createTask(String title, String description, String category, Date dueDate);
    public Optional<Task> getTaskById(int id);
    public List<Task> getAllTasks();
    public boolean updateTask(int id, String newTitle, String newDescription, String newCategory, Date newDueDate);
    public boolean deleteTask(int id);
    public List<Task> getTasksByCategory(String category);
    public boolean markTaskAsCompleted(int id);
    public boolean markTaskAsIncomplete(int id);

    // Date-based Task Retrieval Methods
    public List<Task> getTasksByDate(Date date); // 특정 날짜의 할 일 목록
    public Map<Integer, List<Task>> getTasksByMonth(int year, int month); // 특정 월의 할 일 목록 (일별 그룹화)
}
```

---

**4. `service.FileService.java`**

```java
package service;

import model.Task;
import java.io.IOException;
import java.util.List;

public class FileService {
    // Fields
    // private String filePath;

    // Constructor
    public FileService(String filePath);

    // File I/O Methods
    public void saveTasksToFile(List<Task> tasks) throws IOException;
    public List<Task> loadTasksFromFile() throws IOException, ClassNotFoundException;
}
```

---

**5. `service.SchedulerService.java`**

```java
package service;

// import model.Task; (run 메소드 내부에서 Task를 직접 다루지는 않고 TaskService를 통해 접근)
// import java.util.Date;
// import java.util.List;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

public class SchedulerService implements Runnable {
    // Fields
    // private TaskService taskService;
    // private boolean running = true;
    // private final ScheduledExecutorService scheduler;

    // Constructor
    public SchedulerService(TaskService taskService);

    // Scheduler Control Methods
    public void startScheduler();
    public void stopScheduler();

    // Runnable Interface Method (자동으로 주기적 실행됨)
    @Override
    public void run(); // 마감 임박 알림 로직
}
```

---

**6. `ui.ConsoleUI.java`**

```java
package ui;

import exception.InvalidInputException; // 사용자 정의 예외
// import model.Task; (핸들러 메소드 내에서 사용)
import service.FileService;     // 의존성
import service.SchedulerService; // 의존성
import service.TaskService;      // 의존성
// import java.io.IOException; (load/saveTasks 내부에서 처리)
// import java.text.ParseException; (getDateInput 내부에서 처리)
// import java.text.SimpleDateFormat;
// import java.util.*; (Date, Scanner, List, Map, Calendar 등)

public class ConsoleUI {
    // Fields
    // private TaskService taskService;
    // private FileService fileService;
    // private SchedulerService schedulerService;
    // private Scanner scanner;
    // private SimpleDateFormat dateFormat;

    // Constructor
    public ConsoleUI(TaskService taskService, FileService fileService, SchedulerService schedulerService);

    // Main Application Loop
    public void start();

    // Private Helper Methods for UI
    private void displayMenu();

    // --- Handler Methods for Menu Options ---
    private void handleAddTask() throws InvalidInputException;
    private void handleViewAllTasks();
    private void handleViewTaskById() throws InvalidInputException;
    private void handleUpdateTask() throws InvalidInputException;
    private void handleDeleteTask() throws InvalidInputException;
    private void handleViewTasksByCategory() throws InvalidInputException;
    private void handleMarkTaskComplete() throws InvalidInputException;
    private void handleMarkTaskIncomplete() throws InvalidInputException;
    private void handleViewTasksBySpecificDate() throws InvalidInputException; // 날짜별 조회 핸들러
    private void handleViewTasksByMonth() throws InvalidInputException;        // 월별 조회 핸들러

    // --- Input Helper Methods ---
    private String getStringInput(String prompt) throws InvalidInputException;
    private String getStringInput(String prompt, String defaultValue); // 수정 시 사용
    private int getIntInput(String prompt) throws InvalidInputException;
    private Date getDateInput(String prompt) throws InvalidInputException;
    private Date getDateInput(String prompt, Date defaultValue); // 수정 시 사용

    // --- Date/Time Helper Methods ---
    private String getDayOfWeekString(int dayOfWeekConstant); // Calendar.DAY_OF_WEEK -> "월", "화"

    // --- File Load/Save Helper Methods ---
    private void loadTasks();
    private void saveTasks();
}
```

---

**7. `exception.InvalidInputException.java`**

```java
package exception;

public class InvalidInputException extends Exception {
    // Constructors
    public InvalidInputException(String message);
    public InvalidInputException(String message, Throwable cause);
}
```

---

**8. `main.Main.java`**

```java
package main;

import repository.TaskRepository;
import service.FileService;
import service.SchedulerService;
import service.TaskService;
import ui.ConsoleUI;

public class Main {
    // Main Method
    public static void main(String[] args);
    // (내부적으로 각 서비스 및 UI 객체 생성 및 UI 시작)
}
```

---


### 9. 역할 분담

1.  **데이터 모델 및 저장소 담당 (Data Modeler & Repository Lead)**
    *   **주요 담당 클래스:**
        *   `model.Task.java`
        *   `repository.TaskRepository.java`
    *   **주요 책임:**
        *   `Task` 클래스의 모든 속성(필드) 및 메소드(생성자, Getter/Setter, `toString()`, `updateNextId()`) 완벽 구현.
        *   `TaskRepository` 클래스 구현: `ArrayList<Task>`를 활용한 할 일 객체들의 저장, 조회(ID기반, 전체), 추가, 수정, 삭제 로직 구현 (`setTasks` 포함).
        *   데이터 구조의 일관성 및 안정성 확보.
    *   **주요 학습/활용 기능:**
        *   **객체 지향 설계 (클래스, 캡슐화)**
        *   **제네릭 및 컬렉션 (`ArrayList<Task>`)**
        *   `Serializable` 인터페이스 이해 및 활용 (파일 서비스와의 연동 고려)
        *   `toString()` 메소드 오버라이딩
        *   `static` 멤버 이해 및 활용
    *   **협업:**
        *   `TaskService` 담당자와 `Task` 객체 및 `TaskRepository` 메소드 사용 방식 긴밀히 협의.
        *   `FileService` 담당자와 `Task` 객체의 직렬화/역직렬화 관련 논의.

2.  **핵심 비즈니스 로직 및 날짜 기반 서비스 담당 (Core Logic & Date Service Lead)**
    *   **주요 담당 클래스:**
        *   `service.TaskService.java`
    *   **주요 책임:**
        *   `TaskService`의 모든 핵심 기능 구현: 할 일 생성, ID/카테고리별 조회, 전체 조회, 수정, 삭제, 완료/미완료 처리.
        *   **날짜 기반 조회 기능 구현:** `getTasksByDate(Date date)` 및 `getTasksByMonth(int year, int month)` 메소드 로직 상세 구현 (날짜 비교, 필터링, 정렬, 그룹핑).
        *   `TaskRepository`를 효과적으로 활용하여 비즈니스 로직 구성.
    *   **주요 학습/활용 기능:**
        *   **서비스 계층의 역할 이해**
        *   **Stream API 활용 (필터링, 매핑, 정렬, 그룹핑 등)**
        *   `java.util.Date` 및 `java.util.Calendar` (또는 `java.time`) 활용한 날짜 처리
        *   `Optional` 클래스 활용
        *   메소드 설계 및 로직 흐름 제어
    *   **협업:**
        *   데이터 모델/저장소 담당자와 `TaskRepository` 인터페이스 활용.
        *   UI 담당자와 `TaskService`의 각 메소드 사용 방법 및 반환 값 형태 논의.
        *   스케줄러 담당자와 알림 대상 태스크 조회 로직 공유.

3.  **사용자 인터페이스 및 예외 처리 담당 (UI & Exception Handling Lead)**
    *   **주요 담당 클래스:**
        *   `ui.ConsoleUI.java`
        *   `exception.InvalidInputException.java`
        *   `main.Main.java` (애플리케이션 시작 및 의존성 설정)
    *   **주요 책임:**
        *   `ConsoleUI`의 전체적인 흐름(메뉴 표시, 사용자 입력, 결과 출력) 구현.
        *   각 메뉴 옵션(`handle...` 메소드)에 대한 상세 로직 구현 (`TaskService` 호출 포함).
        *   날짜 기반 조회 결과를 사용자에게 명확하게 보여주는 로직 구현 (`handleViewTasksBySpecificDate`, `handleViewTasksByMonth`).
        *   사용자 입력 유효성 검사 및 `InvalidInputException`을 포함한 각종 예외 처리 로직 구현.
        *   `getDayOfWeekString` 등 UI 표현을 위한 헬퍼 메소드 구현.
        *   `Main` 클래스에서 각 컴포넌트 객체 생성 및 `ConsoleUI` 시작 로직 구현.
    *   **주요 학습/활용 기능:**
        *   **콘솔 입출력 (`Scanner`)**
        *   **예외 처리 (`try-catch`, 사용자 정의 예외 클래스 작성 및 활용)**
        *   애플리케이션 제어 흐름 (루프, 조건문)
        *   `SimpleDateFormat`을 활용한 날짜 포맷팅 및 파싱
        *   메소드 호출 및 결과 처리
    *   **협업:**
        *   `TaskService` 담당자와 서비스 메소드 호출 방식, 파라미터, 반환 값에 대해 긴밀히 소통.
        *   `FileService` 담당자와 파일 로드/저장 시점 (`loadTasks`, `saveTasks`) 논의.

4.  **파일 입출력 및 백그라운드 스케줄링 담당 (Persistence & Scheduling Lead)**
    *   **주요 담당 클래스:**
        *   `service.FileService.java`
        *   `service.SchedulerService.java`
    *   **주요 책임:**
        *   `FileService` 구현: `Task` 객체 리스트를 파일에 저장(직렬화)하고, 파일로부터 읽어오는(역직렬화) 로직 구현 (`saveTasksToFile`, `loadTasksFromFile`).
        *   파일 관련 `IOException` 등 예외 처리.
        *   `SchedulerService` 구현: `Runnable` 인터페이스를 활용하여 백그라운드 스레드에서 주기적으로 마감일 임박 알림 기능 구현 (`run` 메소드 로직).
        *   스케줄러 시작(`startScheduler`) 및 중지(`stopScheduler`) 로직 구현.
        *   (필요시) 스레드 안전성 고려.
    *   **주요 학습/활용 기능:**
        *   **파일 입출력 (`ObjectOutputStream`, `ObjectInputStream`, `FileOutputStream`, `FileInputStream`)**
        *   **객체 직렬화/역직렬화 (`Serializable`)**
        *   **Thread 및 동시성 프로그래밍 기초 (`Runnable`, `ScheduledExecutorService`)**
        *   `IOException` 처리
    *   **협업:**
        *   데이터 모델/저장소 담당자와 `Task` 객체의 직렬화 방식 논의.
        *   UI 담당자와 파일 로드/저장 시점 (`ConsoleUI`의 `loadTasks`, `saveTasks`에서 `FileService` 메소드 호출) 및 스케줄러 시작/중지 시점 논의.
        *   `TaskService` 담당자와 알림 기능 구현 시 필요한 할 일 목록 조회 방식 논의.

**공통 책임 및 협업 방식은 이전과 동일합니다:**

*   **코드 리뷰 생활화**
*   **Git을 활용한 버전 관리 및 협업**
*   **정기적인 팀 미팅 (진행 상황 공유, 문제 해결)**
*   **문서화 (주석 등)**
*   **상속:** 만약 `Task`를 확장하는 경우가 생긴다면 (예: `RecurringTask extends Task`), 이는 `Task`를 주로 다루는 **데이터 모델 및 저장소 담당**과 **핵심 비즈니스 로직 담당**이 함께 논의하여 설계하고 구현하는 것이 좋습니다.
