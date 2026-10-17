package liugrape.game.battle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class HelloController {
    private final Path profilePath = Paths.get(
            System.getProperty("user.home"),
            "liuGrapeDevelopmentData",
            "player.json");

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
                playerNameField.setText(loadPlayerName());
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
        alert.setContentText("游戏玩法正在开发中，敬请期待。");
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

    private String loadPlayerName() throws IOException {
        try {
            JsonObject profile = JsonParser.parseString(
                    new String(Files.readAllBytes(profilePath), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            if (!profile.has("username") || !profile.get("username").isJsonPrimitive()
                    || !profile.getAsJsonPrimitive("username").isString()) {
                throw new IOException("玩家档案格式无效，请重新登录以创建档案。");
            }
            return profile.get("username").getAsString();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("玩家档案格式无效，请重新登录以创建档案。", e);
        }
    }

    private void savePlayerName(String playerName) throws IOException {
        Path parent = profilePath.getParent();
        Files.createDirectories(parent);

        JsonObject profile = new JsonObject();
        profile.addProperty("username", playerName);
        Files.write(profilePath, profile.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }
}
