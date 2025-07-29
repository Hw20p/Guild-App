package com.hyeon.controller;

import com.hyeon.model.GuildMember;
import com.hyeon.service.DiscordMessageService;
import com.hyeon.service.GuildMemberService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GuildController {

    // 멤버 등록 관련 컨트롤들
    @FXML private TextField nameField;
    @FXML private TextField subsField;
    @FXML private TextField knightsField;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    // 멤버 목록 관련 컨트롤들
    @FXML private TableView<GuildMember> memberTable;
    @FXML private TableColumn<GuildMember, String> nameColumn;
    @FXML private TableColumn<GuildMember, String> subsColumn;
    @FXML private TableColumn<GuildMember, String> knightsColumn;

    // 검색 관련 컨트롤들
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;

    // 디스코드 메시지 관련 컨트롤들
    @FXML private TextArea discordTextArea;
    @FXML private Button parseDiscordButton;
    @FXML private TextArea discordResultArea;

    // 통계 및 기타 컨트롤들
    @FXML private Label totalMembersLabel;
    @FXML private Label subsCountLabel;
    @FXML private Label knightsCountLabel;
    @FXML private Button exportButton;
    @FXML private Button sampleDataButton;

    private final GuildMemberService service = GuildMemberService.getInstance();
    private final ObservableList<GuildMember> displayedMembers = FXCollections.observableArrayList();
    private GuildMember editingMember = null;

    @FXML
    public void initialize() {
        setupTableView();
        setupEventHandlers();
        refreshMemberList();
        updateStatistics();

        // 버튼 초기 상태 설정
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void setupTableView() {
        // 테이블 컬럼 설정
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        subsColumn.setCellValueFactory(cellData -> {
            List<String> subs = cellData.getValue().getSubCharacters();
            return new javafx.beans.property.SimpleStringProperty(
                    subs.isEmpty() ? "없음" : String.join(", ", subs));
        });

        knightsColumn.setCellValueFactory(cellData -> {
            List<String> knights = cellData.getValue().getKnightCharacters();
            return new javafx.beans.property.SimpleStringProperty(
                    knights.isEmpty() ? "없음" : String.join(", ", knights));
        });

        memberTable.setItems(displayedMembers);

        // 더블클릭으로 수정 모드 진입
        memberTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                GuildMember selectedMember = memberTable.getSelectionModel().getSelectedItem();
                if (selectedMember != null) {
                    startEditing(selectedMember);
                }
            }
        });

        // 우클릭 컨텍스트 메뉴
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("수정");
        MenuItem deleteItem = new MenuItem("삭제");

        editItem.setOnAction(e -> {
            GuildMember selectedMember = memberTable.getSelectionModel().getSelectedItem();
            if (selectedMember != null) {
                startEditing(selectedMember);
            }
        });

        deleteItem.setOnAction(e -> {
            GuildMember selectedMember = memberTable.getSelectionModel().getSelectedItem();
            if (selectedMember != null) {
                handleDeleteMember(selectedMember);
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        memberTable.setContextMenu(contextMenu);
    }

    private void setupEventHandlers() {
        // 검색 필드에서 엔터 키 처리
        searchField.setOnAction(e -> handleSearch());

        // 디스코드 텍스트 영역 프롬프트 설정
        discordTextArea.setPromptText("디스코드에서 복사한 메시지를 여기에 붙여넣으세요...");
    }

    @FXML
    public void handleAddMember() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("이름을 입력하세요.", Alert.AlertType.WARNING);
            return;
        }

        List<String> subs = parseCharacterNames(subsField.getText());
        List<String> knights = parseCharacterNames(knightsField.getText());

        GuildMember member = new GuildMember(name, subs, knights);
        service.addMember(member);

        clearInputFields();
        refreshMemberList();
        updateStatistics();

        showAlert("멤버가 성공적으로 추가되었습니다.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void handleUpdateMember() {
        if (editingMember == null) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("이름을 입력하세요.", Alert.AlertType.WARNING);
            return;
        }

        editingMember.setName(name);
        editingMember.setSubCharacters(parseCharacterNames(subsField.getText()));
        editingMember.setKnightCharacters(parseCharacterNames(knightsField.getText()));

        service.updateMember(editingMember);
        cancelEditing();
        refreshMemberList();
        updateStatistics();

        showAlert("멤버 정보가 성공적으로 수정되었습니다.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void handleCancelEdit() {
        cancelEditing();
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        List<GuildMember> searchResults = service.searchByKeyword(keyword);
        displayedMembers.setAll(searchResults);

        // 검색 결과 표시
        if (keyword.isEmpty()) {
            totalMembersLabel.setText("전체 멤버: " + searchResults.size() + "명");
        } else {
            totalMembersLabel.setText("검색 결과: " + searchResults.size() + "명 (키워드: " + keyword + ")");
        }
    }

    @FXML
    public void handleClearSearch() {
        searchField.clear();
        refreshMemberList();
        updateStatistics();
    }

    @FXML
    public void handleParseDiscord() {
        String discordText = discordTextArea.getText().trim();
        if (discordText.isEmpty()) {
            showAlert("디스코드 메시지를 입력하세요.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, List<String>> userMessages = DiscordMessageService.parseDiscordMessagesByUser(discordText);
            Map<String, Object> stats = DiscordMessageService.getMessageStatistics(userMessages);

            StringBuilder result = new StringBuilder();
            result.append(String.format("\n"));

            result.append(DiscordMessageService.formatMessagesAsText(userMessages));

            discordResultArea.setText(result.toString());
        } catch (Exception e) {
            showAlert("메시지 파싱 중 오류가 발생했습니다: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("데이터 내보내기");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("텍스트 파일", "*.txt")
        );
        fileChooser.setInitialFileName("길드멤버_목록_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt");

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                exportMembersToFile(file);
                showAlert("데이터가 성공적으로 내보내졌습니다.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void handleAddSampleData() {
        // 먼저 데이터가 있는지 확인
        if (!service.getAllMembers().isEmpty()) {
            showAlert("이미 데이터가 존재합니다. 샘플 데이터를 추가할 수 없습니다.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("샘플 데이터 추가");
        confirmAlert.setHeaderText("샘플 데이터를 추가하시겠습니까?");
        confirmAlert.setContentText("테스트용 길드 멤버 데이터가 추가됩니다.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.addSampleData();
            refreshMemberList();
            updateStatistics();
            showAlert("샘플 데이터가 추가되었습니다.", Alert.AlertType.INFORMATION);
        }
    }

    private void handleDeleteMember(GuildMember member) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("멤버 삭제");
        confirmAlert.setHeaderText("멤버를 삭제하시겠습니까?");
        confirmAlert.setContentText("'" + member.getName() + "' 멤버가 영구적으로 삭제됩니다.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.deleteMember(member.getId());
            refreshMemberList();
            updateStatistics();
            showAlert("멤버가 삭제되었습니다.", Alert.AlertType.INFORMATION);
        }
    }

    private void startEditing(GuildMember member) {
        editingMember = member;
        nameField.setText(member.getName());
        subsField.setText(String.join(", ", member.getSubCharacters()));
        knightsField.setText(String.join(", ", member.getKnightCharacters()));

        // 버튼 상태 변경
        addButton.setVisible(false);
        updateButton.setVisible(true);
        cancelButton.setVisible(true);

        nameField.requestFocus();
    }

    private void cancelEditing() {
        editingMember = null;
        clearInputFields();

        // 버튼 상태 복원
        addButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void clearInputFields() {
        nameField.clear();
        subsField.clear();
        knightsField.clear();
    }

    private List<String> parseCharacterNames(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void refreshMemberList() {
        displayedMembers.setAll(service.getAllMembers());
    }

    private void updateStatistics() {
        Map<String, Object> stats = service.getStatistics();
        totalMembersLabel.setText("전체 멤버: " + stats.get("totalMembers") + "명");
        subsCountLabel.setText("부캐 보유자: " + stats.get("membersWithSubs") + "명");
        knightsCountLabel.setText("기사 보유자: " + stats.get("membersWithKnights") + "명");
    }

    private void exportMembersToFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("=== 길드 멤버 목록 ===\n");
            writer.write("생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            Map<String, Object> stats = service.getStatistics();
            writer.write("=== 통계 정보 ===\n");
            writer.write("총 멤버 수: " + stats.get("totalMembers") + "명\n");
            writer.write("부캐 보유자: " + stats.get("membersWithSubs") + "명\n");
            writer.write("기사 보유자: " + stats.get("membersWithKnights") + "명\n");
            writer.write("총 부캐 수: " + stats.get("totalSubCharacters") + "개\n");
            writer.write("총 기사 수: " + stats.get("totalKnightCharacters") + "개\n\n");

            writer.write("=== 상세 목록 ===\n");
            int index = 1;
            for (GuildMember member : service.getAllMembers()) {
                writer.write(String.format("[%d] %s\n", index++, member.toString()));

                if (!member.getSubCharacters().isEmpty()) {
                    writer.write("    부캐: " + String.join(", ", member.getSubCharacters()) + "\n");
                }

                if (!member.getKnightCharacters().isEmpty()) {
                    writer.write("    기사: " + String.join(", ", member.getKnightCharacters()) + "\n");
                }

                writer.write("\n");
            }

            // 디스코드 메시지 분석 결과도 포함 (있는 경우)
            String discordResult = discordResultArea.getText();
            if (!discordResult.trim().isEmpty()) {
                writer.write("\n=== 디스코드 메시지 분석 결과 ===\n");
                writer.write(discordResult);
            }
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "오류" :
                type == Alert.AlertType.WARNING ? "경고" : "정보");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 추가 유틸리티 메서드들

    /**
     * 현재 선택된 멤버 정보를 클립보드에 복사
     */
    @FXML
    public void handleCopyMemberInfo() {
        GuildMember selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            String memberInfo = selectedMember.toString();

            // 클립보드에 복사
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(memberInfo);
            clipboard.setContent(content);

            showAlert("멤버 정보가 클립보드에 복사되었습니다.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("복사할 멤버를 선택해주세요.", Alert.AlertType.WARNING);
        }
    }

    /**
     * 전체 멤버 데이터 초기화
     */
    @FXML
    public void handleClearAllData() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("전체 데이터 삭제");
        confirmAlert.setHeaderText("모든 데이터를 삭제하시겠습니까?");
        confirmAlert.setContentText("이 작업은 되돌릴 수 없습니다. 정말로 모든 길드 멤버 데이터를 삭제하시겠습니까?");

        ButtonType deleteButton = new ButtonType("삭제", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            service.clearAllData();
            refreshMemberList();
            updateStatistics();
            discordResultArea.clear();
            showAlert("모든 데이터가 삭제되었습니다.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * 디스코드 메시지 결과를 파일로 저장
     */
    @FXML
    public void handleSaveDiscordResult() {
        String discordResult = discordResultArea.getText().trim();
        if (discordResult.isEmpty()) {
            showAlert("저장할 디스코드 분석 결과가 없습니다.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("디스코드 분석 결과 저장");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("텍스트 파일", "*.txt")
        );
        fileChooser.setInitialFileName("디스코드_분석결과_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt");

        Stage stage = (Stage) parseDiscordButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write("=== 디스코드 메시지 분석 결과 ===\n");
                writer.write("생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
                writer.write(discordResult);
                showAlert("디스코드 분석 결과가 저장되었습니다.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * 검색 결과를 별도 파일로 내보내기
     */
    @FXML
    public void handleExportSearchResults() {
        if (displayedMembers.isEmpty()) {
            showAlert("내보낼 검색 결과가 없습니다.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("검색 결과 내보내기");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("텍스트 파일", "*.txt")
        );

        String searchKeyword = searchField.getText().trim();
        String fileName = searchKeyword.isEmpty() ?
                "전체_멤버목록_" : "검색결과_" + searchKeyword + "_";
        fileName += LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt";
        fileChooser.setInitialFileName(fileName);

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write("=== 검색 결과 ===\n");
                writer.write("생성일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");

                if (!searchKeyword.isEmpty()) {
                    writer.write("검색 키워드: " + searchKeyword + "\n");
                }

                writer.write("결과 개수: " + displayedMembers.size() + "명\n\n");

                int index = 1;
                for (GuildMember member : displayedMembers) {
                    writer.write(String.format("[%d] %s\n", index++, member.toString()));

                    if (!member.getSubCharacters().isEmpty()) {
                        writer.write("    부캐: " + String.join(", ", member.getSubCharacters()) + "\n");
                    }

                    if (!member.getKnightCharacters().isEmpty()) {
                        writer.write("    기사: " + String.join(", ", member.getKnightCharacters()) + "\n");
                    }

                    writer.write("\n");
                }

                showAlert("검색 결과가 저장되었습니다.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * 테이블 선택 변경 시 호출되는 메서드
     */
    private void handleTableSelectionChanged() {
        GuildMember selectedMember = memberTable.getSelectionModel().getSelectedItem();
        // 선택된 멤버에 따른 추가 로직을 여기에 구현할 수 있습니다.
        // 예: 상세 정보 패널 업데이트, 버튼 활성화/비활성화 등
    }

    /**
     * 애플리케이션 종료 시 정리 작업
     */
    public void cleanup() {
        // 필요한 경우 정리 작업 수행
        // 예: 임시 파일 삭제, 연결 종료 등
    }
}