package view;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import action.model.domain.ActionResponseDto;
import action.model.domain.ActionType;
import common.FrontController;
import item.model.domain.InventoryItemDto;
import item.model.domain.ItemInfoDto;
import item.model.domain.ItemUseDto;
import quiz.model.dao.QuizDao;
import quiz.model.domain.QuizConfirmResponseDto;
import quiz.model.domain.QuizDto;
import quiz.model.domain.QuizType;
import state.model.domain.StateResponseDto;
import state.model.domain.StateUpdateInfoDto;
import state.model.domain.StateUpdateRequestDto;

public class TraineeView {
    private String playerName;
    private int playerId;
    private FrontController fc;

    public TraineeView() {
        fc = FrontController.getInstance();
    }

    public void exec () {

        initMenu();
	//
        while (true) {
            int userChoice = mainMenu();
            switch (userChoice) {
                case 1:
                    studyMenu();
                    break;	//반복문 종료
                case 2:
                    exerciseMenu();
                    break;
                case 3:
                    workMenu();
                    break;
                case 4:
                    quizMenu();
                    break;
                case 5:
                    restMenu();
                    break;
                case 7:
                    shopLoop();
                    break;
                case 8:
                    inventoryLoop();
                    break;
                case 0:
                    System.out.println("종료합니다.");
                    return;	// 함수 종료
            
                default:
                    break;
            }
        }
    }
	// 캐릭터 상태 출력, 생성할 캐릭터 ID, Name 출력 (Userchoice =0 일 경우,캐릭터 생성)
    public void initMenu() {
        while (true) {
            List<StateResponseDto> charList = fc.getPlayerList();	// getPlayerList -> State로 바꾸면 좋겠다 

            clear();
            printBanner("캐릭터를 선택하세요.");
    
            System.out.println("0. 새로운 캐릭터 생성");
            margin();
			// 캐릭터 상태 출력
            if(charList.size() > 0) {
                for (StateResponseDto c : charList) {
                    System.out.println(c.getPlayerId() +". "+ c.getPlayerName()+
                                        "    \t소지금:   "+c.getMoney()
                                        );
                }
            }
            int userChoice = inputInt();		//스캐너 객체 생성, 숫자값 반환
            if (userChoice == 0) {
                playerName = inputbox("캐릭터 생성", "이름을 입력하세요.");
                playerId = fc.createPlayer(playerName).getPlayerId();
                return;
            }
            else {
                StateResponseDto result = fc.getState(userChoice);

                // System.out.println(result);

                if (result.getPlayerId() == 0) {
                    continue;
                }
                else {
                    playerId = result.getPlayerId();
                    playerName = result.getPlayerName();
                    return;
                }
            }
        }
    }

    public void shopLoop() {

        while (true) {

            String title = "상점";

            clear();
            System.out.println(AsciiArts.artShop);
            margin();
            System.out.println("[ 아이템 목록 ]");
            margin();
            printShopItems();
            margin();
            System.out.println("  0. 뒤로");
			// 스캐너로 이동. 0일 경우 뒤로 가기. 0이 아니면, 캐릭터 아이디와 번호로 선택한 상품 가져오기
			// 알림과 결과값 가져오기
            int userChoice = inputInt();
            if (userChoice == 0) {
                return;
            }
            else {
                String result = fc.buyItem(playerId, userChoice);
                messagebox("알림", result);
            }
        }
    }

    public void inventoryLoop() {
        while (true) {

            String title = "소지품";

            clear();
            printBanner(title);
            margin();
            System.out.println("[ 아이템 목록 ]");
            margin();
            printInventoryItems();
            margin();
            System.out.println("  0. 뒤로");

            int userChoice = inputInt();
            if (userChoice == 0) {
                return;
            }
            else {
				// 아이템 사용
                ItemUseDto result = fc.useItem(userChoice, userChoice);
                messagebox(result.getResult(), statChangeString(result.getStatChange()));
            }
        }
    }

    public String statChangeString(StateUpdateInfoDto statDto) {

        StringBuilder sb = new StringBuilder();
            if (statDto.getHp() >= 0) {
                sb.append(" 체력: +").append(statDto.getHp()).append(" ");
            }
            if (statDto.getMp() >= 0) {
                sb.append(" 정신력: +").append(statDto.getMp()).append(" ");
            }
            if (statDto.getIntelligence() >= 0) {
                sb.append(" 지능: +").append(statDto.getIntelligence()).append(" ");
            }
            if (statDto.getStrength() >= 0) {
                sb.append(" 힘: +").append(statDto.getStrength()).append(" ");
            }
            return sb.toString().trim();
}   


    public void studyMenu() {
        int userChoice = ynMessagebox("공부", 
            "정신력과 체력을 사용하여 공부합니다. 지능이 상승합니다.\n"+
            "공부하시겠습니까? (Y/N)\r",
            1);

        if (userChoice == 1) {
            ActionResponseDto response = fc.doAction(playerId, ActionType.STUDY);
            if (response.getResult() == "Action Successful") {
                messagebox("성공", "능지 상승!");
                fc.updateTime(playerId);
            }
            else {
                messagebox("실패!", "정신력이 부족합니다.");
            }
        }
    }

    public void exerciseMenu() {
        int userChoice = ynMessagebox("운동", 
        "체력과 정신력을 사용하여 운동합니다. 힘이 증가합니다.\n"+
        "운동하시겠습니까? (Y/N)\r",
        2);
        if (userChoice == 1) {
            ActionResponseDto response = fc.doAction(playerId, ActionType.EXERCISE);
            if (response.getResult() == "Action Successful") {
                messagebox("성공", "힘 상승!");
                fc.updateTime(playerId);
            }
            else {
                messagebox("실패!", "체력이 부족합니다.");
            }
        }
    }

    public void restMenu() {
        int userChoice = ynMessagebox("휴식", 
        "체력과 정신력을 어느정도 회복합니다."+
        "휴식하시겠습니까? (Y/N)\r",
        3);
        if (userChoice == 1) {
            ActionResponseDto response = fc.doAction(playerId, ActionType.REST);
            if (response.getResult() == "Action Successful") {
                messagebox("성공", "몸과 마음이 회복되었다!");
                fc.updateTime(playerId);
            }
            else {
                messagebox("실패!", "뭣..?");
            }
        }
    }

    public void workMenu() {
        int userChoice = ynMessagebox("아르바이트",
        "정신력과 정신력을 사용하여 아르바이트를 뜁니다."+
        "힘이 소폭 증가하고 돈을 법니다.\n"+ 
        "알바하시겠습니까? (Y/N)\r");

        if (userChoice == 1) {
            ActionResponseDto response = fc.doAction(playerId, ActionType.PART_TIME_JOB);
            if (response.getResult() == "Action Successful") {
                messagebox("성공", "돈을 벌었다!");
                fc.updateTime(playerId);
            }
            else {
                messagebox("실패!", "넌 일할 준비가 되지 않았다!");
            }
        }
    }

    public void quizMenu() {
        clear();
        divider();
        System.out.println(AsciiArts.artQuiz);

        System.out.println("5개의 퀴즈가 주어지며 절반 이상 맞추면 통과합니다.");
        margin();
        System.out.println("[ 퀴즈 종류 ]");
        margin();
        System.out.println("  1. 파이썬 5문제 [보통]");
        margin();
        System.out.println("  2. 자바 5문제 [보통]");
        margin();
        System.out.println("  3. 종합 시험 8문제 [어려움]");
        margin();
        System.out.println("  0. 뒤로");
        int userChoice = inputInt();

        switch (userChoice) {
            case 1:
                quizLoop(2, QuizType.PYTHON, 5);
                break;
            case 2:
                quizLoop(2, QuizType.JAVA, 5);
                break;
            case 3:
                quizLoop(3, QuizType.ALL, 8);
                break;
            default:
                break;
        }
    }

    public void quizLoop(int difficulty, QuizType qt, int size) {
        ActionResponseDto response= ActionResponseDto.builder().build();
        switch (difficulty) {
            case 1:
            response = fc.doAction(playerId, ActionType.QUIZ1);
        if (!response.getResult().equals("Action Successful")) {
            messagebox("실패!", "퀴즈를 풀수 없습니다... "+response.getResult());
            return;
        }
            case 2:
            response = fc.doAction(playerId, ActionType.QUIZ2);
        if (!response.getResult().equals("Action Successful")) {
            messagebox("실패!", "퀴즈를 풀수 없습니다... "+response.getResult());
            return;
        }
            case 3:
            response = fc.doAction(playerId, ActionType.QUIZ3);
        if (!response.getResult().equals("Action Successful")) {
            messagebox("실패!", "퀴즈를 풀수 없습니다... "+response.getResult());
            return;
        }
                
                break;
        
            default:
                break;
        }
        if (!response.getResult().equals("Action Successful")) {
            messagebox("실패!", "퀴즈를 풀수 없습니다... "+response.getResult());
            return;
        }

        // Assume getQuizInfoList returns a list of quizzes
        List<QuizDto> quizzes = fc.getQuizInfoList(difficulty, qt);

        int correctAnswers = 0;
        int quizCount = 1;
        
        for (QuizDto quiz : quizzes) {
            clear();
            printBanner(quizCount + "번 문제");
            System.out.println(quiz.getContent()); // Display the question

            String userAnswer = inputString(); // Get user input

            QuizConfirmResponseDto result = fc.confirmQuiz(quiz.getQuizId(), userAnswer);
            if (result.getResult().equals("성공")) { // Check answer
                correctAnswers++;
                quizConfirmBox("정답!", result.getExplanation());
            } else {
                quizConfirmBox("오답!", result.getExplanation());
            }
            quizCount++;
        }

        // Calculate the pass/fail condition.
        if (correctAnswers >= size / 2) {
            messagebox("Quiz Result", "Congratulations! You passed the quiz with " + correctAnswers + " correct answers.");
            fc.submitQuizResult(playerId, difficulty, "성공");
        } else {
            messagebox("Quiz Result", "You failed the quiz with only " + correctAnswers + " correct answers.");
            fc.submitQuizResult(playerId, difficulty, "실패");
        }
        fc.updateTime(playerId);
    }

    public int mainMenu() {

        String title = getCurrentTime();

        clear();
        printBanner(title, 1);
        margin();
        printStats();
        margin();
        printActions();
        margin();
        System.out.println(AsciiArts.artChar);
        return inputInt();
    }



    public void clear() {
        // System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public void margin () {
        margin(1);
    }
    public void margin (int size) {
        for (int i=0; i < size; i++ ) {
            System.out.println();
        }
    }

    public int ynMessagebox(String title, String content, int ascii) {
        clear();
        divider();
        System.out.println("[ "+title+" ]");

        switch (ascii) {
            case 1:
                System.out.println(AsciiArts.artBook);
                break;
            case 2:
                System.out.println(AsciiArts.artRunning);
                break;
            case 3:
                System.out.println(AsciiArts.artSofa);
                break;
            default:
                margin();
                break;
        }

        System.out.println(content);
        String yn = inputString();
        if (yn.equals("y") || yn.equals("Y")) {
            return 1;
        }
        return 0;
    }

    public int ynMessagebox(String title, String content) {
        return ynMessagebox(title, content, 0);
    }

    public void printShopItems() {
        List<ItemInfoDto> items = fc.getShopItemList(playerId);
        if (items == null) {
            System.out.println("아무것도 없음!");
            return;
        }
        for (ItemInfoDto item : items) {
            printItem(item);
        }
    }
	// 아이탬 정보
    public void printInventoryItems() {
        List<InventoryItemDto> items = fc.getInventoryInfo(playerId);
        if (items == null) {
            System.out.println("아무것도 없음!");
            return;
        }
        for (InventoryItemDto item : items) {
            printItem(item);
        }
    }
	// 가독성을 위해 추가
    public void printItem(InventoryItemDto item) {
        System.out.println(
            item.getInvenId() +". "+
            item.getItemName() +"    "+
            item.getCount()
        ); 
    }
	// 가독성을 위해 추가
	// 아이템 상세정보
    public void printItem(ItemInfoDto item) {
        System.out.println(
            item.getId() +". "+
            item.getName() +"    "+
            item.getCategory() +"    "+
            item.getCost() +"    "+
            item.getDescription()
        ); 
    }

    public String getCurrentTime() {
        int rawTime = fc.getTime(playerId);
        int day = (int)rawTime / 4 ;
        String time = "황혼";
        switch (rawTime % 4) {
            case 0:
                time = "아침"; 
                break;
            case 1:
                time = "점심";
                break;
            case 2:
                time = "저녁";
                break;
            case 3:
                time = "밤";
            default:
                break;
        }
        return "DAY : "+(day + 1)+" - "+time;
    }
    
    public void quizConfirmBox(String title, String content) {
        clear();
        divider();
        margin();
        System.out.println("[ "+title+" ]");
        margin();
        System.out.println(content);
        margin();
        inputEnter("엔터를 눌러 다음 문제로 이동");
    }

    public void messagebox(String title, String content) {
        clear();
        divider();
        margin();
        System.out.println("[ "+title+" ]");
        margin();
        System.out.println(content);
        margin();
        inputEnter();
    }

    public String inputbox(String title, String content) {
        clear();
        divider();
        margin();
        System.out.println("[ "+title+" ]");
        margin();
        System.out.println(content);
        margin();
        return inputString();
    }

    public void printBanner(String title) {
        printBanner(title, 0);	// 기본값
    }
    
    public void printBanner(String title, int style) {
        String bannerStyle = null;
        boolean underLine = true;
        switch (style) {
            case 1:
                bannerStyle = "=-._.-=-._.-=-._.-=-._.-=-._.-=-._.-=-._.-=-._.-=-._.-=-._.-";
                break;
            case 2:
                bannerStyle = "============================================================";
                break;
            case 3:
                underLine = false;
                break;
            default:
                bannerStyle = "------------------------------------------------------------";
                break;
        }
        System.out.println(bannerStyle);
        System.out.println();
        System.out.println("          "+title);
        System.out.println();
        if (underLine) {
            System.out.println(bannerStyle);
        }
    }

    public void printStats() {

        StateResponseDto state = fc.getState(playerId);
        System.out.println("[ "+playerName+" ]");
        margin();
        System.out.println("  체  력 : "+ state.getHp());
        System.out.println("  정신력 : "+ state.getMp());
        System.out.println("  지  능 : "+ state.getIntelligence());
        System.out.println("  힘     : "+ state.getStrength());
        System.out.println("  소지금 : "+ state.getMoney());

    }

    public void printActions() {
        System.out.println("[ 행동 선택 ]              [ 메뉴 ]");
        margin();
        System.out.println("  1. 공부                    7. 상점");
        System.out.println("  2. 운동                    8. 소지품");
        System.out.println("  3. 알바                    0. 종료");
        System.out.println("  4. 퀴즈");
        System.out.println("  5. 휴식");
        margin();
    }

    public void divider() {
        System.out.println("------------------------------------------------------------");
    }

    public void getState() {
        return;
    }
    public void inputEnter() {
        inputEnter("엔터를 누르세요.");
    }

    public void inputEnter(String text) {
        System.out.println("------------------------------------------------------------");
        System.out.print(text);
        Scanner s = new Scanner(System.in);
        s.nextLine();
    }

    public String inputString() {
        System.out.println("------------------------------------------------------------");
        System.out.print(">>>");
        Scanner s = new Scanner(System.in);
        return s.nextLine();
    }

    public int inputInt() {
        int result = 0;
        System.out.println("------------------------------------------------------------");
        while (true) {
            try {
                System.out.print(">>>");
                Scanner s = new Scanner(System.in);
                result = s.nextInt();
            } catch(InputMismatchException e) {
                System.err.println("숫자를 입력하세요.");
                continue;
            }
            return result;
        }
    }
}
    
