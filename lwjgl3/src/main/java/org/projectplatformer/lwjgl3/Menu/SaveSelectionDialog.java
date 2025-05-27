package org.projectplatformer.lwjgl3.Menu;

import org.projectplatformer.lwjgl3.SaveData;
import org.projectplatformer.lwjgl3.StartupHelper;
import org.projectplatformer.lwjgl3.SaveManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class SaveSelectionDialog extends JDialog {

    private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
    private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
    private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
    private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
    private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

    private static final int DIALOG_BASE_WIDTH = 800;
    private static final int DIALOG_BASE_HEIGHT = 600;

    private static Font FONT_DIALOG_TITLE_BASE;
    private static Font FONT_SAVE_BUTTON_BASE;

    static {
        FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 48);
        FONT_SAVE_BUTTON_BASE = new Font("Arial", Font.BOLD, 24);
    }

    private GameMenu parentMenu;
    private JLabel dialogTitle;
    private JButton[] saveButtons;

    public SaveSelectionDialog(GameMenu parent) {
        super(parent, LanguageManager.get("loadGame_title"), true);
        this.parentMenu = parent;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setPreferredSize(new Dimension(DIALOG_BASE_WIDTH, DIALOG_BASE_HEIGHT));
        setLocationRelativeTo(parent);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));

        dialogTitle = new JLabel(LanguageManager.get("loadGame_title"), SwingConstants.CENTER);
        dialogTitle.setForeground(FANTASY_BRONZE);
        dialogTitle.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(dialogTitle, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonsPanel.setOpaque(false);

        saveButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            final int slotNumber = i + 1;
            String label = "Save " + slotNumber;
            JButton saveButton = createFantasyButton(
                String.format("<html><center>%s</center></html>", label),
                FANTASY_BROWN_LEATHER);
            saveButton.addActionListener(e -> {
                AudioManager.playClickSound();
                StartupHelper.setSelectedSlot(slotNumber);
                StartupHelper.setContinueGame(true);


                SaveData data = SaveManager.load(slotNumber);
                int currentLevel = data.getCurrentLevel();
                String levelName = "level" + currentLevel;
                AudioManager.playLevelMusic(levelName);

                dispose();
                parentMenu.setVisible(false);
                parentMenu.startGame(true);
            });
            saveButtons[i] = saveButton;
            buttonsPanel.add(saveButton);
        }

        mainPanel.add(buttonsPanel, BorderLayout.CENTER);

        JButton backButton = createFantasyButton(LanguageManager.get("backButton"), FANTASY_BRONZE.darker());
        backButton.addActionListener(e -> {
            AudioManager.playClickSound();
            dispose();
        });

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.add(backButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        updateComponentSizes();
    }

    private void updateComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / DIALOG_BASE_WIDTH;
        double scaleY = (double) currentHeight / DIALOG_BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        dialogTitle.setFont(FONT_DIALOG_TITLE_BASE.deriveFont((float) (FONT_DIALOG_TITLE_BASE.getSize2D() * overallScale)));
        Font btnFont = FONT_SAVE_BUTTON_BASE.deriveFont((float) (FONT_SAVE_BUTTON_BASE.getSize2D() * overallScale));

        for (JButton btn : saveButtons) {
            btn.setFont(btnFont);
            int size = (int) (200 * overallScale);
            Dimension dim = new Dimension(size, size);
            btn.setPreferredSize(dim);
            btn.setMinimumSize(dim);
            btn.setMaximumSize(dim);
        }
        revalidate();
        repaint();
    }

    private JButton createFantasyButton(String text, Color bgColor) {
        FantasyButton button = new FantasyButton(text, bgColor, FONT_SAVE_BUTTON_BASE);
        button.setPreferredSize(new Dimension(200, 200));
        return button;
    }

    private class FantasyButton extends JButton {
        private Color currentBackgroundColor;
        private Color defaultBaseColor;
        private boolean isHovered;
        private boolean isPressed;

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
                @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = isPressed ? BUTTON_PRESSED_COLOR_DARK : (isHovered ? BUTTON_HOVER_COLOR_LIGHT : defaultBaseColor);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
            g2.setColor(FANTASY_BORDER_COLOR);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
