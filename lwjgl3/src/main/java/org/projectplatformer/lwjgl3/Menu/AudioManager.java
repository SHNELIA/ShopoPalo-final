package org.projectplatformer.lwjgl3.Menu;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Properties;

public class AudioManager {
    private static boolean musicEnabled = true;
    private static boolean soundsEnabled = true;
    private static float musicVolume = 0.5f;   // 0..1
    private static float effectsVolume = 0.5f; // 0..1
    private static Clip menuMusicClip;
    private static Clip levelMusicClip;
    private static String currentLevelMusic = null;

    private static final String SETTINGS_FILE = "settings.properties";

    static {
        loadSettings();
    }

    // --- Збереження та завантаження ---
    public static void saveSettings() {
        Properties prop = new Properties();
        prop.setProperty("musicEnabled", String.valueOf(musicEnabled));
        prop.setProperty("soundsEnabled", String.valueOf(soundsEnabled));
        prop.setProperty("musicVolume", String.valueOf(musicVolume));
        prop.setProperty("effectsVolume", String.valueOf(effectsVolume));
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            prop.store(fos, "Game Audio Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public static void loadSettings() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            prop.load(fis);
            musicEnabled = Boolean.parseBoolean(prop.getProperty("musicEnabled", "true"));
            soundsEnabled = Boolean.parseBoolean(prop.getProperty("soundsEnabled", "true"));
            musicVolume = Float.parseFloat(prop.getProperty("musicVolume", "0.5"));
            effectsVolume = Float.parseFloat(prop.getProperty("effectsVolume", "0.5"));
        } catch (IOException e) {
            // Якщо файлу немає — використовуються дефолтні значення
        }
    }

    // --- Гучність ---
    public static float getMusicVolume() { return musicVolume; }
    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        setVolume(menuMusicClip, musicVolume);
        setVolume(levelMusicClip, musicVolume);
        saveSettings();
    }
    public static float getEffectsVolume() { return effectsVolume; }
    public static void setEffectsVolume(float volume) {
        effectsVolume = Math.max(0f, Math.min(1f, volume));
        saveSettings();
        // Клік-звуки отримують гучність лише коли створюються — тут нічого більше не треба
    }

    // --- ОНОВЛЕНА ФУНКЦІЯ! ---
    private static void setVolume(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum(); // Наприклад, -80.0f
            float max = gainControl.getMaximum(); // Наприклад, 6.0f
            float dB = (volume == 0f) ? min : (float)(20.0 * Math.log10(volume));
            dB = Math.max(min, Math.min(max, dB));
            gainControl.setValue(dB);
        } catch (Exception ignored) {}
    }

    // --- Включення/виключення ---
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        saveSettings();
        if (musicEnabled) {
            if (currentLevelMusic != null)
                playLevelMusic(currentLevelMusic, true);
            else
                playMenuMusic();
        } else {
            stopMenuMusic();
            stopLevelMusic();
        }
    }
    public static void toggleMusic() {
        setMusicEnabled(!musicEnabled);
    }
    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }
    public static void setSoundsEnabled(boolean enabled) {
        soundsEnabled = enabled;
        saveSettings();
    }
    public static void toggleSounds() {
        setSoundsEnabled(!soundsEnabled);
    }

    // --- МУЗИКА МЕНЮ ---
    public static void playMenuMusic() {
        if (!musicEnabled) return;
        if (menuMusicClip != null && menuMusicClip.isActive()) return;
        stopLevelMusic();
        stopMenuMusic();
        try {
            InputStream audioSrc = AudioManager.class.getResourceAsStream("/audio/menu_music.wav");
            if (audioSrc == null) {
                System.err.println("Menu music not found!");
                return;
            }
            BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(audioStream);
            setVolume(menuMusicClip, musicVolume);
            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            menuMusicClip.start();
        } catch (Exception e) {
            System.err.println("Failed to play menu music: " + e.getMessage());
        }
    }
    public static void stopMenuMusic() {
        if (menuMusicClip != null) {
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }

    // --- МУЗИКА РІВНЯ ---
    public static void playLevelMusic(String levelName) {
        playLevelMusic(levelName, false);
    }
    private static void playLevelMusic(String levelName, boolean forToggle) {
        if (!musicEnabled) return;
        String filename = "/audio/" + levelName + "_music.wav";
        if (!forToggle) currentLevelMusic = levelName;
        if (levelMusicClip != null && levelMusicClip.isActive() && currentLevelMusic != null && currentLevelMusic.equals(levelName)) return;
        stopMenuMusic();
        stopLevelMusic();
        try {
            InputStream audioSrc = AudioManager.class.getResourceAsStream(filename);
            if (audioSrc == null) {
                System.err.println("Level music not found: " + filename);
                return;
            }
            BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            levelMusicClip = AudioSystem.getClip();
            levelMusicClip.open(audioStream);
            setVolume(levelMusicClip, musicVolume);
            levelMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            levelMusicClip.start();
        } catch (Exception e) {
            System.err.println("Failed to play level music: " + e.getMessage());
        }
    }
    public static void stopLevelMusic() {
        if (levelMusicClip != null) {
            levelMusicClip.stop();
            levelMusicClip.close();
            levelMusicClip = null;
        }
    }

    // --- КЛІК ЗВУК ---
    public static void playClickSound() {
        if (!isSoundsEnabled()) return;
        new Thread(() -> {
            try {
                InputStream audioSrc = AudioManager.class.getResourceAsStream("/audio/click.wav");
                if (audioSrc == null) {
                    System.err.println("Click sound not found!");
                    return;
                }
                BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                setVolume(clip, effectsVolume); // !!! Гучність ефектів
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
    // --- Пауза/Відновлення музики ---
    private static int levelMusicPosition = 0; // Кількість фреймів, на якій була пауза

    public static void pauseLevelMusic() {
        if (levelMusicClip != null && levelMusicClip.isActive()) {
            levelMusicPosition = levelMusicClip.getFramePosition();
            levelMusicClip.stop();
        }
    }

    public static void resumeLevelMusic() {
        if (levelMusicClip != null && !levelMusicClip.isActive()) {
            levelMusicClip.setFramePosition(levelMusicPosition);
            levelMusicClip.start();
        }
    }

    public static String getMusicButtonText() {
        // Якщо хочеш англійською чи українською — легко змінити тут
        return musicEnabled ? "MUSIC ON" : "MUSIC OFF";
    }

    public static String getSoundsButtonText() {
        return soundsEnabled ? "SOUNDS ON" : "SOUNDS OFF";
    }

}
