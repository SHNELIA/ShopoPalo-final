package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LevelsWindow extends JFrame implements ActionListener {

    // --- Fantasy Colors та базові налаштування ---
    private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
    private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
    private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
    private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
    private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;

    private static Font FONT_TITLE_BASE;
    private static Font FONT_BUTTON_BASE;

    static {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        FONT_TITLE_BASE = new Font("Arial", Font.BOLD, 60);
        FONT_BUTTON_BASE = new Font("Arial", Font.BOLD, 32);
    }

    private JLabel titleLabel;
    private JButton backButton;
    private JPanel levelsButtonsPanel;

    // --- Reference на головне меню ---
    private GameMenu mainMenu;

    public LevelsWindow(GameMenu mainMenu) {
        this.mainMenu = mainMenu; // <-- reference на головне меню

        setTitle(LanguageManager.get("title") + " - " + LanguageManager.get("levelsWindow_title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));

        titleLabel = new JLabel(LanguageManager.get("levelsWindow_chooseLevel"), SwingConstants.CENTER);
        titleLabel.setForeground(FANTASY_BRONZE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        levelsButtonsPanel = new JPanel(new GridBagLayout());
        levelsButtonsPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // --- Кнопки рівнів ---
        for (int i = 1; i <= 4; i++) {
            JButton levelButton = createFantasyButton(LanguageManager.get("levelsWindow_level") + " " + i, FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
            levelButton.setActionCommand("LEVEL_" + i);
            levelButton.addActionListener(this);
            gbc.gridy = i - 1;
            levelsButtonsPanel.add(levelButton, gbc);
        }

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        levelsButtonsPanel.add(Box.createVerticalGlue(), gbc);

        mainPanel.add(levelsButtonsPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        backButton = createFantasyButton(LanguageManager.get("backButton"), FANTASY_BRONZE.darker(), FONT_BUTTON_BASE.deriveFont(Font.PLAIN, 24f));
        backButton.addActionListener(this);
        southPanel.add(backButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        updateComponentSizes();
        setVisible(true);
    }

    private void updateComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / BASE_WIDTH;
        double scaleY = (double) currentHeight / BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        titleLabel.setFont(FONT_TITLE_BASE.deriveFont((float) (FONT_TITLE_BASE.getSize2D() * overallScale)));

        Font currentButtonFont = FONT_BUTTON_BASE.deriveFont((float) (FONT_BUTTON_BASE.getSize2D() * overallScale * 0.6));

        Dimension baseBtnSize = new Dimension(200, 40);
        Dimension newBtnSize = new Dimension(
            (int) (baseBtnSize.width * overallScale),
            (int) (baseBtnSize.height * overallScale)
        );
        newBtnSize.width = Math.max(newBtnSize.width, 120);
        newBtnSize.height = Math.max(newBtnSize.height, 30);
        newBtnSize.width = Math.min(newBtnSize.width, 300);
        newBtnSize.height = Math.min(newBtnSize.height, 60);

        for (Component comp : levelsButtonsPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setFont(currentButtonFont);
                button.setPreferredSize(newBtnSize);
                button.setMinimumSize(newBtnSize);
                button.setMaximumSize(newBtnSize);
            }
        }

        Font currentBackBtnFont = FONT_BUTTON_BASE.deriveFont(Font.PLAIN, (float) (FONT_BUTTON_BASE.getSize2D() * overallScale * 0.4));
        backButton.setFont(currentBackBtnFont);

        Dimension baseBackBtnSize = new Dimension(150, 40);
        Dimension newBackBtnSize = new Dimension(
            (int) (baseBackBtnSize.width * overallScale),
            (int) (baseBackBtnSize.height * overallScale)
        );
        newBackBtnSize.width = Math.max(newBackBtnSize.width, 90);
        newBackBtnSize.height = Math.max(newBackBtnSize.height, 25);
        newBackBtnSize.width = Math.min(newBackBtnSize.width, 220);
        newBackBtnSize.height = Math.min(newBackBtnSize.height, 50);

        backButton.setPreferredSize(newBackBtnSize);
        backButton.setMinimumSize(newBackBtnSize);
        backButton.setMaximumSize(newBackBtnSize);

        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.startsWith("LEVEL_")) {
            String levelNum = command.substring(6);
            AudioManager.playLevelMusic("level" + levelNum);
            new GameWorldWindow(levelNum, mainMenu); // ← ТЕПЕР ПРАВИЛЬНО! (mainMenu)
            this.dispose(); // Закриваємо це вікно (бо вже новий рівень)
        } else if (e.getSource() == backButton) {
            AudioManager.playMenuMusic();
            this.dispose();
            if (mainMenu != null) mainMenu.showGameMenu();
        }
    }

    private JButton createFantasyButton(String text, Color bgColor, Font font) {
        FantasyButton button = new FantasyButton(text, bgColor, font);
        return button;
    }

    // --- Клас кнопки ---
    private class FantasyButton extends JButton {
        private Color currentBackgroundColor;
        private Color defaultBaseColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public FantasyButton(String text, Color defaultBgColor, Font font) {
            super(text);
            this.defaultBaseColor = defaultBgColor;
            this.currentBackgroundColor = defaultBgColor;
            setForeground(FANTASY_TEXT_LIGHT);
            setFont(font);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setCurrentBackgroundColor(Color color) {
            this.currentBackgroundColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color actualBgColor = currentBackgroundColor;
            if (isPressed) {
                actualBgColor = BUTTON_PRESSED_COLOR_DARK;
            } else if (isHovered) {
                actualBgColor = BUTTON_HOVER_COLOR_LIGHT;
            }

            g2.setColor(actualBgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(FANTASY_BORDER_COLOR);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {}
    }
}
