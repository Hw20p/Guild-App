package com.hyeon.controller;

import com.hyeon.model.GuildMember;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;

public class GuildController {

    @FXML private TextField nameField;
    @FXML private TextField subsField;
    @FXML private TextField knightsField;
    @FXML private ListView<String> memberListView;

    private final ObservableList<String> memberDisplayList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        memberListView.setItems(memberDisplayList);
    }

    @FXML
    public void handleAddMember() {
        String name = nameField.getText().trim();
        List<String> subs = Arrays.stream(subsField.getText().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        List<String> knights = Arrays.stream(knightsField.getText().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        if (name.isEmpty()) {
            showAlert("이름을 입력하세요.");
            return;
        }

        GuildMember member = new GuildMember(name, subs, knights);
        memberDisplayList.add(String.format("이름: %s | 부캐: %s | 기사: %s",
                member.getName(), subs, knights));

        // 입력 필드 초기화
        nameField.clear();
        subsField.clear();
        knightsField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("입력 오류");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}