package com.hyeon.controller;

import com.hyeon.model.CharacterInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class CharacterDetailController {

    @FXML
    private Label characterNameLabel;

    @FXML
    private Label serverLabel;

    @FXML
    private Label levelLabel;

    @FXML
    private Label jobLabel;

    @FXML
    private Label worldLabel;

    private CharacterInfo characterInfo;
    private MainController mainController; // 메인 컨트롤러 참조

    public void setCharacterInfo(CharacterInfo characterInfo) {
        this.characterInfo = characterInfo;
        updateUI();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void updateUI() {
        if (characterInfo != null) {
            characterNameLabel.setText(characterInfo.getName());
            serverLabel.setText(characterInfo.getWorldName() + " 서버");
            levelLabel.setText("Lv. " + characterInfo.getLevel());
            jobLabel.setText(characterInfo.getJob());
            worldLabel.setText(characterInfo.getWorldName());
        }
    }

    @FXML
    private void goBackToSearch() {
        if (mainController != null) {
            mainController.goBackToHome();
        }
    }

    @FXML
    private void showEquipment() {
        showComingSoonAlert("장비 정보", "캐릭터의 장비 정보를 조회하는 기능입니다.");
    }

    @FXML
    private void showStats() {
        showComingSoonAlert("스탯 정보", "캐릭터의 능력치 정보를 조회하는 기능입니다.");
    }

    @FXML
    private void showSkills() {
        showComingSoonAlert("스킬 정보", "캐릭터의 스킬 정보를 조회하는 기능입니다.");
    }

    private void showComingSoonAlert(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("기능 준비 중");
        alert.setContentText(description + "\n\n이 기능은 곧 추가될 예정입니다!");
        alert.showAndWait();
    }
}