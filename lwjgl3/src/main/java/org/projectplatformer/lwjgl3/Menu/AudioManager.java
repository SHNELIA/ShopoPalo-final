package org.projectplatformer.lwjgl3.Menu;

public class AudioManager {

        private static boolean musicEnabled = true;
        private static boolean soundsEnabled = true;

        public static boolean isMusicEnabled() {
            return musicEnabled;
        }

        public static void toggleMusic() {
            musicEnabled = !musicEnabled;
            System.out.println("Music is now: " + (musicEnabled ? "ON" : "OFF"));
        }

        public static boolean isSoundsEnabled() {
            return soundsEnabled;
        }

        public static void toggleSounds() {
            soundsEnabled = !soundsEnabled;
            System.out.println("Sounds are now: " + (soundsEnabled ? "ON" : "OFF"));

        }


        public static String getMusicButtonText() {
            return "MUSIC";
        }

        public static String getSoundsButtonText() { return "SOUNDS"; }
    }

