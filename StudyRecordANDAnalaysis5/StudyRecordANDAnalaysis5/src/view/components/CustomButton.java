// view/components/CustomButton.java
package view.components;

import javax.swing.*;
import java.awt.*;

public class CustomButton extends JButton {
    public CustomButton(String text, Color backgroundColor, Color textColor) {
        super(text);
        setBackground(backgroundColor);
        setForeground(textColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 添加悬停效果
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(backgroundColor);
            }
        });
    }
}