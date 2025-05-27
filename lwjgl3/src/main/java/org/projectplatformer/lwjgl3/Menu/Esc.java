package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Esc {

    // --- Головне ігрове вікно з пауз-меню ---
    public static class GameWindow extends JFrame implements KeyListener, ComponentListener {
        private JPanel menuPanel;
        private boolean menuVisible = false;
        private GameMenu gameMenuInstance;
        private JButton musicButton, soundsButton;

        // Кольори
        private static final Color PAUSE_MENU_BG_COLOR = new Color(51, 34, 25);
        private static final Color PAUSE_MENU_TITLE_COLOR = new Color(153, 102, 51);
        private static final Color BUTTON_TEXT_COLOR = new Color(255, 255, 255);
        private static final Color BUTTON_BG_COLOR = new Color(102, 68, 34);
        private static final Color BUTTON_ON_COLOR = new Color(70, 120, 70);
        private static final Color BUTTON_OFF_COLOR = new Color(120, 70, 70);
        private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
        private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);
        private static final Color BUTTON_BORDER_OUTER = new Color(128, 85, 42);
        private static final Color BUTTON_BORDER_INNER = new Color(77, 51, 26);

        private static final int BASE_WIDTH = 800, BASE_HEIGHT = 600;
        private Font FONT_PAUSE_TITLE_BASE = new Font("SansSerif", Font.BOLD, 36);
        private Font FONT_BUTTON_PAUSE_BASE = new Font("SansSerif", Font.BOLD, 22);

        public GameWindow(GameMenu gameMenu) {
            this.gameMenuInstance = gameMenu;
            setTitle(LanguageManager.get("gameWindow_title"));
            setSize(BASE_WIDTH, BASE_HEIGHT);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            addComponentListener(this);

            JPanel mainContentPanel = new JPanel();
            mainContentPanel.setBackground(PAUSE_MENU_BG_COLOR);
            mainContentPanel.setLayout(new BorderLayout());
            add(mainContentPanel, BorderLayout.CENTER);

            initMenuPanel();
            updateMenuComponentSizes();

            setVisible(true);
        }

        private void initMenuPanel() {
            menuPanel = new JPanel();
            menuPanel.setBackground(PAUSE_MENU_BG_COLOR);
            menuPanel.setLayout(new GridBagLayout());
            menuPanel.setPreferredSize(new Dimension(400, 550));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 0, 15, 0);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;

            JLabel titleLabel = new JLabel("PAUSE");
            titleLabel.setForeground(PAUSE_MENU_TITLE_COLOR);

            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 50, 0);
            menuPanel.add(titleLabel, gbc);

            gbc.insets = new Insets(15, 0, 15, 0);

            JButton continueButton = createFantasyButton("CONTINUE", BUTTON_BG_COLOR);
            JButton guideButton = createFantasyButton("GUIDE", BUTTON_BG_COLOR);
            JButton goToMenuButton = createFantasyButton("GO TO MENU", BUTTON_BG_COLOR);

            musicButton = createFantasyButton(AudioManager.getMusicButtonText(),
                AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR);
            soundsButton = createFantasyButton(AudioManager.getSoundsButtonText(),
                AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR);

            // --- Кнопки ---
            continueButton.addActionListener(e -> {
                AudioManager.playClickSound();
                toggleMenuVisibility();
            });
            guideButton.addActionListener(e -> {
                AudioManager.playClickSound();
                GuideWindow guideWindow = new GuideWindow(this);
                guideWindow.setVisible(true);
            });
            goToMenuButton.addActionListener(e -> {
                AudioManager.playClickSound();
                dispose();
                if (gameMenuInstance != null) gameMenuInstance.showGameMenu();
            });
            musicButton.addActionListener(e -> {
                AudioManager.playClickSound();
                AudioManager.toggleMusic();
                musicButton.setText(AudioManager.getMusicButtonText());
                ((FantasyButton) musicButton).setCurrentBackgroundColor(
                    AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
                );
                revalidate(); repaint();
            });
            soundsButton.addActionListener(e -> {
                AudioManager.playClickSound();
                AudioManager.toggleSounds();
                soundsButton.setText(AudioManager.getSoundsButtonText());
                ((FantasyButton) soundsButton).setCurrentBackgroundColor(
                    AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
                );
                revalidate(); repaint();
            });

            gbc.gridy = 1; menuPanel.add(continueButton, gbc);
            gbc.gridy = 2; menuPanel.add(guideButton, gbc);
            gbc.gridy = 3; menuPanel.add(goToMenuButton, gbc);
            gbc.gridy = 4; menuPanel.add(musicButton, gbc);
            gbc.gridy = 5; menuPanel.add(soundsButton, gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            menuPanel.add(Box.createHorizontalGlue(), gbc);

            menuPanel.setVisible(false);
            getLayeredPane().add(menuPanel, JLayeredPane.PALETTE_LAYER);
        }

        // --- Кастомна кнопка ---
        private class FantasyButton extends JButton {
            private Color currentBackgroundColor;
            private boolean isHovered = false, isPressed = false;

            public FantasyButton(String text, Color defaultBgColor) {
                super(text);
                this.currentBackgroundColor = defaultBgColor;
                setForeground(BUTTON_TEXT_COLOR);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                    public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                    public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
                });
            }
            public void setCurrentBackgroundColor(Color color) { this.currentBackgroundColor = color; repaint(); }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = currentBackgroundColor;
                if (isPressed) bg = BUTTON_PRESSED_COLOR_DARK;
                else if (isHovered) bg = BUTTON_HOVER_COLOR_LIGHT;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(BUTTON_BORDER_OUTER);
                g2.setStroke(new BasicStroke(4));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2.setColor(BUTTON_BORDER_INNER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 10, 10);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {}
        }

        private JButton createFantasyButton(String text, Color bgColor) {
            FantasyButton btn = new FantasyButton(text, bgColor);
            btn.setFont(FONT_BUTTON_PAUSE_BASE);
            btn.setPreferredSize(new Dimension(200, 50));
            return btn;
        }

        // --- Масштабування пауз-меню ---
        private void updateMenuComponentSizes() {
            int w = getWidth(), h = getHeight();
            double scaleX = (double) w / BASE_WIDTH, scaleY = (double) h / BASE_HEIGHT;
            double scale = Math.min(scaleX, scaleY);

            JLabel titleLabel = (JLabel) menuPanel.getComponent(0);
            titleLabel.setFont(FONT_PAUSE_TITLE_BASE.deriveFont((float)(FONT_PAUSE_TITLE_BASE.getSize2D() * scale)));
            Font curBtnFont = FONT_BUTTON_PAUSE_BASE.deriveFont((float)(FONT_BUTTON_PAUSE_BASE.getSize2D() * scale));
            for (int i = 1; i < menuPanel.getComponentCount() - 1; i++) {
                Component comp = menuPanel.getComponent(i);
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    btn.setFont(curBtnFont);
                    Dimension base = new Dimension(200, 50);
                    Dimension d = new Dimension((int)(base.width * scale), (int)(base.height * scale));
                    btn.setPreferredSize(d); btn.setMinimumSize(d); btn.setMaximumSize(d);
                }
            }
            menuPanel.setPreferredSize(new Dimension((int)(400 * scale), (int)(550 * scale)));
            centerMenuPanel();
            revalidate(); repaint();
        }
        private void centerMenuPanel() {
            if (menuVisible) {
                int pw = menuPanel.getPreferredSize().width, ph = menuPanel.getPreferredSize().height;
                int fw = getContentPane().getWidth(), fh = getContentPane().getHeight();
                int x = (fw - pw) / 2, y = (fh - ph) / 2;
                menuPanel.setBounds(x, y, pw, ph);
                menuPanel.revalidate(); menuPanel.repaint();
            }
        }

        // --- ESC показ/сховати меню, пауза музики ---
        private void toggleMenuVisibility() {
            menuVisible = !menuVisible;
            menuPanel.setVisible(menuVisible);
            centerMenuPanel();
            if (menuVisible) {
                AudioManager.pauseLevelMusic();
            } else {
                AudioManager.resumeLevelMusic();
                setFocusable(true);
                requestFocusInWindow();
            }
        }

        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) toggleMenuVisibility();
        }
        @Override public void keyReleased(KeyEvent e) {}
        @Override public void componentResized(ComponentEvent e) { updateMenuComponentSizes(); }
        @Override public void componentMoved(ComponentEvent e) {}
        @Override public void componentShown(ComponentEvent e) {}
        @Override public void componentHidden(ComponentEvent e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception e) { e.printStackTrace(); }
            GameMenu mainMenu = new GameMenu();
            mainMenu.setVisible(true);
        });
    }
}
