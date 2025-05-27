package org.projectplatformer.lwjgl3;

import com.badlogic.gdx.utils.Json;
import java.io.*;
import java.util.*;

public class SaveManager {
    /** Папка для збережень (відносно user.dir) */
    public static final String SAVE_DIR = "saves";

    /** Завантажити дані зі слота (1–4). Якщо файлу нема — повернути новий SaveData */
    public static SaveData load(int slot) {
        File dir = new File(SAVE_DIR);
        File file = new File(dir, "slot" + slot + ".json");
        if (!file.exists()) return new SaveData();

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return new Json().fromJson(SaveData.class, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return new SaveData();
        }
    }

    /** Записати дані у вказаний слот, перезаписавши старий файл */
    public static void save(int slot, SaveData data) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Не вдалося створити папку для збережень: " + dir.getAbsolutePath());
            return;
        }
        File file = new File(dir, "slot" + slot + ".json");
        String json = new Json().toJson(data);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file, false))) {
            out.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Повернути список занятих слотів (1–4) */
    public static List<Integer> availableSlots() {
        File dir = new File(SAVE_DIR);
        List<Integer> res = new ArrayList<>();
        if (!dir.exists()) return res;
        for (int i = 1; i <= 4; i++) {
            File f = new File(dir, "slot" + i + ".json");
            if (f.exists()) res.add(i);
        }
        return res;
    }
}
