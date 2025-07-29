package com.hyeon.controller;

import javafx.fxml.FXML;

public class NavigationBarController {

    private NavigationListener listener;

    // 메인 컨트롤러에서 리스너 세팅
    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    @FXML
    private void goHome() {
        if (listener != null) listener.onNavigate("home");
    }

    @FXML
    private void goSearch() {
        if (listener != null) listener.onNavigate("search");
    }

    @FXML
    private void goOther() {
        if (listener != null) listener.onNavigate("other");
    }

    // 네비게이션 액션 콜백용 인터페이스
    public interface NavigationListener {
        void onNavigate(String destination);
    }
}