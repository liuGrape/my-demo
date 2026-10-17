package liugrape.game.battle;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameMain extends Application {

    static final int COLS = 9, ROWS = 5, CELL = 90, OX = 120, OY = 100;
    static final double[] PATH_X = {980, -60};
    static final int W = 900, H = 600;

    Image texSunflower, texPea, texNut, texZombie, texSun, texBullet, texShovel;
    GraphicsContext g;
    Canvas canvas;

    List<Plant> plants = new ArrayList<>();
    List<Zombie> zombies = new ArrayList<>();
    List<Bullet> bullets = new ArrayList<>();
    List<SunDrop> suns = new ArrayList<>();

    int sunCount = 50, lives = 10;
    double spawnTimer = 3, sunTimer = 5, time = 0;
    boolean gameOver = false;
    String selected = null;
    boolean shovelMode = false;

    static class Plant { String type; int col, row; double hp, cd; }
    static class Zombie { int row; double x, hp = 200, speed, hurt; boolean eating; }
    static class Bullet { double x, y; }
    static class SunDrop { double x, y, vy; }

    int cost(String t) { return t.equals("sunflower") ? 50 : t.equals("peashooter") ? 100 : 50; }
    double hp(String t) { return t.equals("wallnut") ? 600 : 120; }
    Image tex(String t) {
        return switch (t) {
            case "sunflower" -> texSunflower;
            case "peashooter" -> texPea;
            case "wallnut" -> texNut;
            default -> null;
        };
    }

    public static void openGame() {
        new GameMain().start(new Stage());
    }

    @Override
    public void start(Stage stage) {
        texSunflower = load("/image/sunflower.png");
        texPea = load("/image/peashooter.png");
        texNut = load("/image/wallnut.png");
        texZombie = load("/image/zombie.png");
        texSun = load("/image/sun.png");
        texBullet = load("/image/bullet.png");
        texShovel = load("/image/shovel.png");

        canvas = new Canvas(W, H);
        g = canvas.getGraphicsContext2D();

        Button btnSunflower = new Button("笔袋(50)");
        btnSunflower.setOnAction(e -> {
            selected = "sunflower";
            shovelMode = false;
        });
        Button btnPea = new Button("找蓬成(100)");
        btnPea.setOnAction(e -> {
            selected = "peashooter";
            shovelMode = false;
        });
        Button btnNut = new Button("桌子(50)");
        btnNut.setOnAction(e -> {
            selected = "wallnut";
            shovelMode = false;
        });
        Button btnShovel = new Button("铲子");
        btnShovel.setOnAction(e -> {
            shovelMode = true;
            selected = null;
        });
        HBox topBar = new HBox(10, btnSunflower, btnPea, btnNut, btnShovel);

        BorderPane root = new BorderPane(canvas, topBar, null, null, null);
        root.setStyle("-fx-background-color:#3b2f1e; -fx-padding:8; -fx-alignment:center;");
        Scene scene = new Scene(root);
        canvas.setOnMousePressed(e -> click(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> {
            if (selected != null) {
                if(tex(selected) != null){
                    g.drawImage(tex(selected), e.getX() - 30, e.getY() - 30, 60, 60);
                }
            }
        });
        stage.setScene(scene);
        stage.setTitle("Plants vs Zombies");
        stage.show();

        new AnimationTimer() {
            long last = 0;
            public void handle(long now) {
                if (last == 0) { last = now; return; }
                double dt = Math.min(0.05, (now - last) / 1e9);
                last = now;
                update(dt);
                draw();
            }
        }.start();
    }

    Image load(String path) {
        var is = getClass().getResourceAsStream(path);
        if(is == null){
            return null;
        }
        return new Image(is);
    }

    void click(double x, double y) {
        if (gameOver) return;
        for(int i = suns.size()-1; i >=0; i--){
            SunDrop s = suns.get(i);
            double dx = x - s.x;
            double dy = y - s.y;
            if(Math.sqrt(dx*dx + dy*dy) < 22){
                sunCount +=25;
                suns.remove(i);
                return;
            }
        }
        if (shovelMode) {
            int c = (int) ((x - OX) / CELL), r = (int) ((y - OY) / CELL);
            if (c >= 0 && c < COLS && r >=0 && r < ROWS) {
                plants.removeIf(p -> p.col == c && p.row == r);
            }
            return;
        }
        int c = (int) ((x - OX) / CELL), r = (int) ((y - OY) / CELL);
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return;
        if (selected == null) return;
        if (sunCount < cost(selected)) return;
        for (Plant p : plants) if (p.col == c && p.row == r) return;
        Plant p = new Plant();
        p.type = selected;
        p.col = c;
        p.row = r;
        p.hp = hp(selected);
        p.cd = 0;
        plants.add(p);
        sunCount -= cost(selected);
        selected = null;
    }

    void update(double dt) {
        if (gameOver) return;
        time += dt;

        sunTimer -= dt;
        if (sunTimer <= 0) {
            sunTimer = 8 + Math.random() * 4;
            SunDrop s = new SunDrop();
            s.x = OX + Math.random() * COLS * CELL;
            s.y = -20;
            s.vy = 60;
            suns.add(s);
        }
        for (SunDrop s : suns) {
            if (s.y < OY + ROWS * CELL - 40) s.y += s.vy * dt;
        }

        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnTimer = Math.max(1.5, 20 - time * 0.02);
            Zombie z = new Zombie();
            z.row = (int) (Math.random() * ROWS);
            z.x = PATH_X[0];
            z.speed = 22 + Math.random() * 10;
            zombies.add(z);
        }

        for (Plant p : plants) {
            p.cd -= dt;
            if (p.type.equals("sunflower") && p.cd <= 0) {
                p.cd = 10;
                SunDrop s = new SunDrop();
                s.x = OX + p.col * CELL + 30;
                s.y = OY + p.row * CELL + 20;
                s.vy = 0;
                suns.add(s);
            }
            if (p.type.equals("peashooter") && p.cd <= 0) {
                boolean has = false;
                for (Zombie z : zombies)
                    if (z.row == p.row && z.x > OX + p.col * CELL - 20) { has = true; break; }
                if (has) {
                    p.cd = 1.4;
                    Bullet b = new Bullet();
                    b.x = OX + p.col * CELL + 40;
                    b.y = OY + p.row * CELL + 30;
                    bullets.add(b);
                }
            }
        }

        for (Bullet b : bullets) b.x += 300 * dt;
        for (Bullet b : bullets) {
            for (Zombie z : zombies) {
                double zy = OY + z.row * CELL + 40;
                if (Math.abs(b.x - z.x - 15) < 25 && Math.abs(b.y - zy) < 40) {
                    b.y = -9999;
                    z.hp -= 25;
                    z.hurt = 0.15;
                    break;
                }
            }
        }
        bullets.removeIf(b -> b.y < -100 || b.x > 980);

        for (Zombie z : zombies) {
            z.hurt -= dt;
            Plant target = null;
            for (Plant p : plants)
                if (p.row == z.row && Math.abs(OX + p.col * CELL + 40 - z.x) < 30) { target = p; break; }
            if (target != null) {
                z.eating = true;
                target.hp -= 30 * dt;
                if (target.hp <= 0) plants.remove(target);
            } else {
                z.eating = false;
                z.x -= z.speed * dt;
            }
            if (z.x < OX - 40) {
                z.hp = -1;
                lives--;
            }
        }
        zombies.removeIf(z -> z.hp <= 0);
        if (lives <= 0) gameOver = true;
    }

    void draw() {
        g.clearRect(0, 0, W, H);
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                g.setFill((r + c) % 2 == 0 ? Color.web("#78c850") : Color.web("#6ab04c"));
                g.fillRect(OX + c * CELL, OY + r * CELL, CELL, CELL);
            }

        g.setFill(shovelMode ? Color.web("#ffd54a") : Color.rgb(0, 0, 0, 0.25));
        g.fillRect(10, 10, 50, 50);
        if (texShovel != null) g.drawImage(texShovel, 15, 12, 40, 46);
        if(texSun != null) g.drawImage(texSun, 70, 12, 36, 36);
        g.setFill(Color.WHITE);
        g.setFont(Font.font(20));
        g.fillText(String.valueOf(sunCount), 112, 38);

        for (Plant p : plants) {
            Image img = tex(p.type);
            if(img != null){
                g.drawImage(img, OX + p.col * CELL + 15, OY + p.row * CELL + 15, 60, 60);
            }
        }

        for (Zombie z : zombies) {
            if(texZombie == null) continue;
            double zx = z.x - 10, zy = OY + z.row * CELL + 5;
            if (z.hurt > 0) g.setGlobalAlpha(0.5);
            g.drawImage(texZombie, zx, zy, 55, 75);
            g.setGlobalAlpha(1);
            g.setFill(Color.web("#c0392b"));
            g.fillRect(z.x - 5, OY + z.row * CELL, 50, 4);
            g.setFill(Color.web("#2ecc71"));
            g.fillRect(z.x - 5, OY + z.row * CELL, 50 * Math.max(0, z.hp) / 200, 4);
        }

        for (Bullet b : bullets) {
            if(texBullet != null){
                g.drawImage(texBullet, b.x, b.y, 20, 20);
            }
        }
        for (SunDrop s : suns) {
            if(texSun != null){
                g.drawImage(texSun, s.x - 18, s.y - 18, 36, 36);
            }
        }

        if (gameOver) {
            g.setFill(Color.rgb(0, 0, 0, 0.7));
            g.fillRect(0, 0, W, H);
            g.setFill(Color.web("#e74c3c"));
            g.setFont(Font.font(48));
            g.fillText("僵尸吃掉了你的脑子", 250, 300);
            g.setFill(Color.WHITE);
            g.setFont(Font.font(20));
            g.fillText("坚持了 " + (int) time + " 秒", 380, 350);
        }
    }

    public static void main(String[] args) { launch(args); }
}
//liuGrapeDevelopment
