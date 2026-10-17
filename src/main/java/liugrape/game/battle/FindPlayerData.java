package liugrape.game.battle;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FindPlayerData {
    public String text;
    public boolean found;
    public static void main(String[] args) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(System.getProperty("user.home") + "/liuGrapeDevelopmentData/player.json", StandardCharsets.UTF_8)) {
            PlayerData playerData = gson.fromJson(reader, PlayerData.class);
            System.out.println(playerData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PlayerData {
    String username;
}
