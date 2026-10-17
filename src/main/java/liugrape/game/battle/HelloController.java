package liugrape.game.battle;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HelloController {
    private final Path profilePath = Paths.get(
            System.getProperty("user.home"),
            "liuGrapeDevelopmentData",
            "player.json");
    private final Gson gson = new Gson();

    @FXML
    private HBox loginView;

    @FXML
    private HBox homeView;

    @FXML
    private TextField playerNameField;

    @FXML
    private Label loginMessage;

    @FXML
    private Label welcomeText;

    @FXML
    private void initialize() {
        try {
            if (Files.exists(profilePath)) {
                PlayerData data = loadPlayerData();
                playerNameField.setText(data.username);
            } else {
                loginMessage.setText("首次游玩？请输入你的玩家昵称。");
            }
        } catch (IOException e) {
            loginMessage.setText("读取玩家档案失败：" + e.getMessage());
        }
    }

    @FXML
    private void login() {
        String playerName = playerNameField.getText() == null
                ? ""
                : playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            loginMessage.setText("请输入玩家昵称。");
            playerNameField.requestFocus();
            return;
        }
        if (playerName.length() > 20 || playerName.chars().anyMatch(Character::isISOControl)) {
            loginMessage.setText("昵称最多 20 个字符，且不能包含控制字符。");
            playerNameField.requestFocus();
            return;
        }

        try {
            savePlayerName(playerName);
            welcomeText.setText("欢迎，" + playerName + "！");
            loginMessage.setText("");
            showView(homeView);
        } catch (IOException e) {
            loginMessage.setText("保存玩家档案失败：" + e.getMessage());
        }
    }

    @FXML
    private void startGame() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("即将开放");
        alert.setHeaderText("欢迎来到游戏！");
        //alert.setContentText("游戏玩法正在开发中，敬请期待。");
        GameMain.openGame();
        alert.showAndWait();
    }

    @FXML
    private void changePlayer() {
        loginMessage.setText("请输入玩家昵称后登录。");
        showView(loginView);
        playerNameField.requestFocus();
        playerNameField.selectAll();
    }

    private void showView(Pane activeView) {
        boolean showLogin = activeView == loginView;
        loginView.setVisible(showLogin);
        loginView.setManaged(showLogin);
        homeView.setVisible(!showLogin);
        homeView.setManaged(!showLogin);
    }

    private PlayerData loadPlayerData() throws IOException {
        try {
            String json = Files.readString(profilePath, StandardCharsets.UTF_8);
            PlayerData data = gson.fromJson(json, PlayerData.class);
            if (data == null || data.username == null) {
                throw new IOException("玩家档案格式无效，请重新登录以创建档案。");
            }
            return data;
        } catch (JsonParseException e) {
            throw new IOException("玩家档案格式无效，请重新登录以创建档案。", e);
        }
    }

    private void savePlayerName(String playerName) throws IOException {
        Path parent = profilePath.getParent();
        Files.createDirectories(parent);
        PlayerData data = new PlayerData();
        data.username = playerName;
        String json = gson.toJson(data);
        try (FileWriter writer = new FileWriter(profilePath.toFile(), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }
}
