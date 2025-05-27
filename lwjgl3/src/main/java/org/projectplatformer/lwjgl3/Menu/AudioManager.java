package org.projectplatformer.lwjgl3.Menu;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioManager {
    private static boolean musicEnabled = true;
    private static boolean soundsEnabled = true;
    private static Clip menuMusicClip;

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        System.out.println("Music toggled. New state: " + musicEnabled);
        if (musicEnabled) {
            playMenuMusic();
        } else {
            stopMenuMusic();
        }
    }

    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public static void toggleSounds() {
        soundsEnabled = !soundsEnabled;
        System.out.println("Sounds toggled. New state: " + soundsEnabled);
    }

    public static String getMusicButtonText() {
        return "MUSIC";
    }

    public static String getSoundsButtonText() {
        return "SOUNDS";
    }

    // ===== MENU MUSIC =====
    public static void playMenuMusic() {
        if (!musicEnabled) {
            System.out.println("playMenuMusic: music disabled");
            return;
        }
        if (menuMusicClip != null && menuMusicClip.isActive()) {
            System.out.println("Menu music already playing, skip restart");
            return;
        }
        stopMenuMusic();
        try {
            System.out.println("Trying to play menu music...");
            InputStream audioSrc = AudioManager.class.getResourceAsStream("/audio/menu_music.wav");
            if (audioSrc == null) {
                System.err.println("Menu music not found!");
                return;
            }
            BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            menuMusicClip = AudioSystem.getClip();
            menuMusicClip.open(audioStream);
            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            menuMusicClip.start();
            System.out.println("Music started!");
        } catch (Exception e) {
            System.err.println("Failed to play menu music: " + e.getMessage());
        }
    }

    public static void stopMenuMusic() {
        if (menuMusicClip != null) {
            System.out.println("Stopping menu music...");
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }

    // ===== CLICK SOUND =====
    public static void playClickSound() {
        if (!isSoundsEnabled()) {
            System.out.println("Click sound: sounds disabled");
            return;
        }
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
                clip.start();
                // Clean up after playback
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                System.out.println("Click sound played");
            } catch (Exception e) {
                System.err.println("Failed to play click sound: " + e.getMessage());
            }
        }).start();
    }
}
