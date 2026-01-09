package me.zhivaevda.interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interface extends JFrame {
    public static boolean statusUptime = true;
    public static boolean statusTimer = false;
    public static Integer procent = 0;
    public static Integer realProcent = 0;
    public static String nowChecking = "Проверяется код из буфера обмена...";

    public Interface() {
        setTitle("CordyChecker");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setExtendedState(JFrame.NORMAL);
        setLocationRelativeTo(null);

        SpinnerPanel spinnerPanel = new SpinnerPanel();
        spinnerPanel.setBounds(0, 0, 800, 500);
        add(spinnerPanel);

        setContentPane(spinnerPanel);
    }

    public static void setProcent(int newProcent) {
        procent = newProcent;
    }

    public static void setStatus(boolean Status) {
        statusUptime = Status;
        procent = 100;
        realProcent = 100;
        statusTimer = false;
    }

    public static void setDo(String Do) {
        nowChecking = Do;
    }

    public static void setTimer(boolean Status) {
        statusTimer = Status;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Interface app = new Interface();
            app.setVisible(true);
        });
    }

    private class SpinnerPanel extends JPanel {
        private Color textMiddleColor = new Color(239, 116, 116);
        private Color textMainColor = new Color(115, 107, 107);
        private Color textTitleColor = new Color(225, 61, 61);
        private Color readyColor = new Color(92, 159, 83);
        private Color readyMainColor = new Color(56, 179, 38);
        private Integer uptimeSeconds = 0;
        private Integer uptimeMinutes = 0;

        public SpinnerPanel() {
            setBackground(new Color(26, 23, 23));

            Timer textTimer = new Timer(750, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (statusUptime) {
                        if (realProcent < procent) {
                            realProcent = realProcent + 1;
                        }
                    } else {
                        realProcent = procent;
                    }
                    updateText();
                }
            });
            textTimer.start();

            Timer timeTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (statusUptime && statusTimer) {
                        if (uptimeSeconds >= 59) {
                            uptimeSeconds = 0;
                            uptimeMinutes = uptimeMinutes + 1;
                        } else {
                            uptimeSeconds = uptimeSeconds + 1;
                        }
                    }
                    updateText();
                }
            });
            timeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Используем шрифт Exo 2
            Font exoFont18 = FontUtil.loadExo2(18f).deriveFont(Font.BOLD, 18f);

            if (statusUptime) {
                drawCenteredText(g2d, nowChecking, exoFont18, textMiddleColor, 400, 215);
                drawCenteredText(g2d, "Прогресс проверки: " + realProcent + "%", exoFont18, textMiddleColor, 400, 239);
            } else {
                drawCenteredText(g2d, nowChecking, exoFont18, readyMainColor, 400, 215);
                drawCenteredText(g2d, "Статус проверки: " + realProcent, exoFont18, readyMainColor, 400, 239);
            }

            if (statusUptime) {
                drawCenteredText(g2d, "Проверка продолжается, дождитесь ее завершения", exoFont18, textTitleColor, 400, 20);
                drawCenteredText(g2d, "Выполняйте все указания проверяющего модератора", exoFont18, textTitleColor, 400, 42);
            } else {
                drawCenteredText(g2d, "Проверка успешно завершена, модератор получил результаты", exoFont18, readyColor, 400, 20);
                drawCenteredText(g2d, "Дождитесь, пока проверяющей сотрудник даст итоговый вердикт", exoFont18, readyColor, 400, 42);
            }

            if (statusUptime) {
                drawCenteredText(g2d, "Проверка длится", exoFont18, textTitleColor, 400, 419);
                if (uptimeMinutes > 0) {
                    drawCenteredText(g2d, uptimeMinutes + " " + getSuffix(uptimeMinutes, "минута", "минуты", "минут") + " · " +
                            uptimeSeconds + " " + getSuffix(uptimeSeconds, "секунда", "секунды", "секунд"), exoFont18, textTitleColor, 400, 441);
                } else {
                    drawCenteredText(g2d, uptimeSeconds + " " + getSuffix(uptimeSeconds, "секунда", "секунды", "секунд"), exoFont18, textTitleColor, 400, 441);
                }
            } else {
                if (uptimeMinutes > 0) {
                    drawCenteredText(g2d, uptimeMinutes + " " + getSuffix(uptimeMinutes, "минута", "минуты", "минут") + " · " +
                            uptimeSeconds + " " + getSuffix(uptimeSeconds, "секунда", "секунды", "секунд"), exoFont18, readyColor, 400, 441);
                } else {
                    drawCenteredText(g2d, uptimeSeconds + " " + getSuffix(uptimeSeconds, "секунда", "секунды", "секунд"), exoFont18, readyColor, 400, 441);
                }
                drawCenteredText(g2d, "Проверка длилась", exoFont18, readyColor, 400, 419);
            }

            drawText(g2d, "CordyChecker", exoFont18, textMainColor, 12, 428, 18);
            drawText(g2d, "t.me/zhivaevda", exoFont18, textMainColor, 12, 450, 18);
            drawText(g2d, "Ваш проект", exoFont18, textMainColor, 670, 428, 18);
            drawText(g2d, "vk.com/ваш_проект", exoFont18, textMainColor, 594, 450, 18);

            g2d.dispose();
        }

        private void drawCenteredText(Graphics2D g2d, String text, Font font, Color color, int centerX, int centerY) {
            g2d.setFont(font);
            g2d.setColor(color);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            int x = centerX - textWidth / 2;
            int y = centerY + textHeight / 4;

            g2d.drawString(text, x, y);
        }

        public void updateText() {
            repaint();
        }

        private void drawText(Graphics2D g2d, String text, Font font, Color color, int x, int y, int fontSize) {
            g2d.setFont(font.deriveFont((float) fontSize));
            g2d.setColor(color);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int drawX = x;

            if (x == (getWidth() - textWidth) / 2) {
                drawX = (getWidth() - textWidth) / 2;
            }

            g2d.drawString(text, drawX, y);
        }

        private String getSuffix(int value, String one, String two, String many) {
            if (value % 100 >= 11 && value % 100 <= 19) {
                return many;
            }
            int lastDigit = value % 10;
            if (lastDigit == 1) {
                return one;
            }
            if (lastDigit >= 2 && lastDigit <= 4) {
                return two;
            }
            return many;
        }
    }
}

