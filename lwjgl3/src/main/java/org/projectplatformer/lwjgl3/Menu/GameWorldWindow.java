package org.projectplatformer.lwjgl3.Menu;

import org.projectplatformer.lwjgl3.SaveData;
import org.projectplatformer.lwjgl3.SaveManager;
import org.projectplatformer.lwjgl3.Shop.Shop;
import org.projectplatformer.lwjgl3.StartupHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class GameWorldWindow extends JFrame implements ActionListener {

    // Fantasy Colors
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

    // Main UI Components
    private JLabel titleLabel;
    private JButton shopButton;
    private JButton equipmentButton;
    private JButton goToLevelsButton;
    private JButton backToMenuButton;

    private GameMenu parentMenu;
    private String currentLevelNum;

    public GameWorldWindow(String levelNum, GameMenu parent) {
        this.parentMenu = parent;
        this.currentLevelNum = levelNum;

        setTitle(LanguageManager.get("title") + " - " + LanguageManager.get("gameWorld_title") + " " + levelNum);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        // --- UI Layout ---
        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));

        titleLabel = new JLabel(LanguageManager.get("gameWorld_title") + " " + levelNum, SwingConstants.CENTER);
        titleLabel.setForeground(FANTASY_BRONZE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonsContainerPanel = new JPanel(new GridBagLayout());
        buttonsContainerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        shopButton = createFantasyButton(LanguageManager.get("gameWorld_shop"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        equipmentButton = createFantasyButton(LanguageManager.get("gameWorld_equipment"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        goToLevelsButton = createFantasyButton(LanguageManager.get("gameWorld_levels"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        backToMenuButton = createFantasyButton(LanguageManager.get("backToMainMenu"), FANTASY_BRONZE.darker(), FONT_BUTTON_BASE.deriveFont(Font.PLAIN, 24f));

        shopButton.addActionListener(this);
        equipmentButton.addActionListener(this);
        goToLevelsButton.addActionListener(this);
        backToMenuButton.addActionListener(this);

        gbc.gridy = 0;
        buttonsContainerPanel.add(shopButton, gbc);
        gbc.gridy = 1;
        buttonsContainerPanel.add(equipmentButton, gbc);
        gbc.gridy = 2;
        buttonsContainerPanel.add(goToLevelsButton, gbc);
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        buttonsContainerPanel.add(Box.createVerticalGlue(), gbc);

        mainPanel.add(buttonsContainerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        southPanel.add(backToMenuButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        updateComponentSizes();
        setVisible(true);

        // Перестраховка — грає музику рівня
        AudioManager.playLevelMusic("level" + levelNum);
    }

    private void updateComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / BASE_WIDTH;
        double scaleY = (double) currentHeight / BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        titleLabel.setFont(FONT_TITLE_BASE.deriveFont((float) (FONT_TITLE_BASE.getSize2D() * overallScale)));

        Font currentButtonFont = FONT_BUTTON_BASE.deriveFont((float) (FONT_BUTTON_BASE.getSize2D() * overallScale));
        shopButton.setFont(currentButtonFont);
        equipmentButton.setFont(currentButtonFont);
        goToLevelsButton.setFont(currentButtonFont);

        Dimension baseBtnSize = new Dimension(400, 80);
        Dimension newBtnSize = new Dimension(
            (int) (baseBtnSize.width * overallScale),
            (int) (baseBtnSize.height * overallScale)
        );
        shopButton.setPreferredSize(newBtnSize);
        equipmentButton.setPreferredSize(newBtnSize);
        goToLevelsButton.setPreferredSize(newBtnSize);

        Font currentBackBtnFont = FONT_BUTTON_BASE.deriveFont(Font.PLAIN, (float) (FONT_BUTTON_BASE.getSize2D() * overallScale * 0.7));
        backToMenuButton.setFont(currentBackBtnFont);
        Dimension baseBackBtnSize = new Dimension(250, 60);
        Dimension newBackBtnSize = new Dimension(
            (int) (baseBackBtnSize.width * overallScale),
            (int) (baseBackBtnSize.height * overallScale)
        );
        backToMenuButton.setPreferredSize(newBackBtnSize);

        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AudioManager.playClickSound();

        if (e.getSource() == shopButton) {
            // 1) Завантажуємо дані з поточного ігрового слоту
            int slot = StartupHelper.getSelectedSlot();
            SaveData data = SaveManager.load(slot);

            // 2) Ховаємо це вікно
            this.setVisible(false);

            // 3) Відкриваємо вікно Shop
            Shop shopWindow = new Shop(data);
            shopWindow.setVisible(true);

            // 4) Після закриття Shop повертаємося сюди
            shopWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent we) {
                    GameWorldWindow.this.setVisible(true);
                }
            });

        } else if (e.getSource() == equipmentButton) {
            this.setVisible(false);
            new EquipmentWindow(this).setVisible(true);
            // Після закриття EquipmentWindow ми знову відобразимо це вікно:
            this.setVisible(true);
            this.revalidate();
            this.repaint();

        } else if (e.getSource() == goToLevelsButton) {
            this.setVisible(false);
            // Передаємо parentMenu, щоб LevelsWindow міг повернутися назад
            new LevelsWindow(parentMenu).setVisible(true);
            this.dispose();

        } else if (e.getSource() == backToMenuButton) {
            // Зупиняємо музику рівня, відтворюємо музику меню
            AudioManager.pauseLevelMusic();
            AudioManager.playMenuMusic();

            this.dispose();
            if (parentMenu != null) {
                parentMenu.showGameMenu();
            } else {
                System.exit(0);
            }
        }
    }


    // --- FantasyButton ---
    private JButton createFantasyButton(String text, Color bgColor, Font font) {
        FantasyButton button = new FantasyButton(text, bgColor, font);
        button.setPreferredSize(new Dimension(300, 70));
        button.setMinimumSize(new Dimension(200, 60));
        button.setMaximumSize(new Dimension(500, 100));
        return button;
    }

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
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
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
            if (isPressed) actualBgColor = BUTTON_PRESSED_COLOR_DARK;
            else if (isHovered) actualBgColor = BUTTON_HOVER_COLOR_LIGHT;

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
