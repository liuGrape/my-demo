package liugrape.game.battle;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FindPlayerData {
    public static void main(String[] args) {
        Gson gson = new Gson();
        Path profilePath = Paths.get(
                System.getProperty("user.home"),
                "liuGrapeDevelopmentData",
                "player.json");
        String filePath = profilePath.toAbsolutePath().toString();

        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            PlayerData playerData = gson.fromJson(reader, PlayerData.class);
            String playerName;
            boolean flag;
            if (playerData.username == null || playerData.username.isBlank()) {
                playerName = "你好，新玩家！";
                flag = false;
            } else {
                playerName = "欢迎回来，" + playerData.username + "！";
                flag = true;
            }
            System.out.println(playerName);
        } catch (IOException e) {
            System.out.println("玩家档案不存在或读取失败");
            e.printStackTrace();
        }
    }
}
//hahaha
