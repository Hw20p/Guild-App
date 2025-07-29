package com.hyeon.controller;

import com.hyeon.model.CharacterInfo;
import com.hyeon.service.NexonApiService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController implements NavigationBarController.NavigationListener {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private TextField nicknameField;

    @FXML
    private Button searchButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label statusLabel;

    private NexonApiService apiService;
    private VBox originalCenter; // 원래 중앙 컨텐츠 저장

    @FXML
    public void initialize() {
        apiService = new NexonApiService();
        loadingIndicator.setVisible(false);
        statusLabel.setText("닉네임을 입력하고 검색해보세요!");

        // 원래 중앙 컨텐츠 저장
        originalCenter = (VBox) mainBorderPane.getCenter();

        // Enter 키로 검색 실행
        nicknameField.setOnAction(e -> onSearchClicked());

        // 네비게이션 바 설정
        setupNavigationBar();
    }

    private void setupNavigationBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NavigationBar.fxml"));
            HBox navBar = loader.load(); // VBox -> HBox로 수정
            NavigationBarController navController = loader.getController();
            navController.setNavigationListener(this);
            mainBorderPane.setTop(navBar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearchClicked() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            showAlert("오류", "닉네임을 입력하세요.", Alert.AlertType.WARNING);
            return;
        }

        // UI 상태 변경
        searchButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setText("캐릭터 정보를 검색중입니다...");

        // 백그라운드에서 API 호출
        Task<CharacterInfo> searchTask = new Task<CharacterInfo>() {
            @Override
            protected CharacterInfo call() throws Exception {
                String ocid = apiService.fetchCharacterId(nickname);
                return apiService.fetchCharacterInfo(nickname, ocid);
            }

            @Override
            protected void succeeded() {
                CharacterInfo character = getValue();
                searchButton.setDisable(false);
                loadingIndicator.setVisible(false);
                statusLabel.setText("검색 완료!");

                // 같은 창에서 캐릭터 정보 표시
                showCharacterDetailsInSameWindow(character);
            }

            @Override
            protected void failed() {
                searchButton.setDisable(false);
                loadingIndicator.setVisible(false);
                statusLabel.setText("검색에 실패했습니다.");

                Throwable exception = getException();
                String errorMsg = "캐릭터를 찾을 수 없습니다.";
                if (exception.getMessage().contains("404")) {
                    errorMsg = "존재하지 않는 캐릭터입니다.";
                } else if (exception.getMessage().contains("네트워크")) {
                    errorMsg = "네트워크 연결을 확인해주세요.";
                }

                showAlert("검색 실패", errorMsg, Alert.AlertType.ERROR);
            }
        };

        new Thread(searchTask).start();
    }

    private void showCharacterDetailsInSameWindow(CharacterInfo character) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/character_detail.fxml"));
            VBox characterDetailView = loader.load();

            CharacterDetailController controller = loader.getController();
            controller.setCharacterInfo(character);
            controller.setMainController(this); // 뒤로 가기를 위한 참조 설정

            // 중앙 영역을 캐릭터 상세 정보로 교체
            mainBorderPane.setCenter(characterDetailView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("오류", "캐릭터 정보를 표시할 수 없습니다.", Alert.AlertType.ERROR);
        }
    }

    // 홈 화면으로 돌아가기
    public void goBackToHome() {
        mainBorderPane.setCenter(originalCenter);
        // 검색 필드 초기화
        nicknameField.clear();
        statusLabel.setText("닉네임을 입력하고 검색해보세요!");
    }

    // NavigationListener 구현
    @Override
    public void onNavigate(String destination) {
        switch (destination) {
            case "home":
                goBackToHome();
                break;
            case "search":
                goBackToHome();
                nicknameField.requestFocus();
                break;
            case "other":
                showAlert("알림", "기타 기능은 준비 중입니다.", Alert.AlertType.INFORMATION);
                break;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}