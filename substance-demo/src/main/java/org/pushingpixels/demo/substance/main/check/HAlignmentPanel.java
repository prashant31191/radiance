/*
 * Copyright (c) 2005-2018 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.demo.substance.main.check;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceCortex;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.BusinessBlackSteelSkin;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

public class HAlignmentPanel extends ControllablePanel implements Deferrable {
    private boolean isInitialized;
    private JCheckBox toPaintGuiderLines;
    private JCheckBox toPaintBounds;

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    public HAlignmentPanel() {
        this.setLayout(new BorderLayout());
    }

    @Override
    public synchronized void initialize() {
        FormLayout lm = new FormLayout("left:pref:grow", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(lm, new ScrollablePanel())
                .border(Borders.DIALOG);

        for (int fontSize = 11; fontSize < 25; fontSize++) {
            builder.append(getSubPanel(fontSize));
        }

        this.add(new JScrollPane(builder.getPanel()));

        FormLayout controlPanelLayoutManager = new FormLayout("fill:pref:grow", "");
        DefaultFormBuilder controlPanelBuilder = new DefaultFormBuilder(controlPanelLayoutManager,
                new ScrollablePanel());

        toPaintGuiderLines = new JCheckBox("guider lines");
        toPaintGuiderLines.addActionListener((ActionEvent e) -> repaint());
        controlPanelBuilder.append(toPaintGuiderLines);

        toPaintBounds = new JCheckBox("bounds");
        toPaintBounds.addActionListener((ActionEvent e) -> repaint());
        controlPanelBuilder.append(toPaintBounds);
        this.controlPanel = controlPanelBuilder.getPanel();

        this.isInitialized = true;
    }

    private JPanel getSubPanel(int size) {
        final JButton button = new JButton("sample");
        final JTextField tf = new JTextField("sample");

        final JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                Graphics2D g2d = (Graphics2D) g.create();
                if (toPaintGuiderLines.isSelected()) {
                    Rectangle buttonBounds = button.getBounds();
                    int textFieldBaseline = tf.getBaseline(buttonBounds.width, buttonBounds.height);
                    g2d.setColor(new Color(255, 0, 0, 196));
                    g2d.drawLine(0, buttonBounds.y, getWidth(), buttonBounds.y);
                    g2d.drawLine(0, buttonBounds.y + buttonBounds.height - 1, getWidth(),
                            buttonBounds.y + buttonBounds.height - 1);
                    g2d.setColor(new Color(0, 128, 0, 196));
                    g2d.drawLine(0, buttonBounds.y + textFieldBaseline, getWidth(),
                            buttonBounds.y + textFieldBaseline);
                }

                if (toPaintBounds.isSelected()) {
                    for (int i = 0; i < getComponentCount(); i++) {
                        Component child = getComponent(i);
                        Rectangle bounds = child.getBounds();
                        g2d.setColor(new Color(128, 0, 255, 128));
                        g2d.fill(bounds);

                        // g2d.setFont(child.getFont());
                        // String size = bounds.getWidth() + "*"
                        // + bounds.getHeight();
                        // int sizeWidth =
                        // g2d.getFontMetrics().stringWidth(size);
                        // int sizeHeight = g2d.getFontMetrics().getHeight();
                        // g2d.setColor(new Color(64, 0, 128));
                        // Rectangle buttonBounds = button.getBounds();
                        // int textFieldBaseline = tf.getBaseline(
                        // buttonBounds.width, buttonBounds.height);
                        // g2d.drawString(size, bounds.x
                        // + (bounds.width - sizeWidth) / 2, bounds.y
                        // + textFieldBaseline);
                    }
                }
                g2d.dispose();
            }
        };

        String fontName = "Tahoma";
        Font font = new Font(fontName, Font.PLAIN, size);
        if (UIManager.getLookAndFeel() instanceof SubstanceLookAndFeel) {
            Font base = SubstanceCortex.GlobalScope.getFontPolicy().getFontSet(null)
                    .getControlFont();
            fontName = base.getFamily();
            font = base.deriveFont((float) size);
        }

        JLabel label = new JLabel(fontName + " " + size);
        label.setFont(font);
        result.add(label);

        tf.setFont(font);
        result.add(tf);

        JPasswordField pf = new JPasswordField("sample");
        pf.setFont(font);
        result.add(pf);

        JComboBox ecb = new JComboBox(new Object[] { "sample" }) {
            @Override
            public void updateUI() {
                super.updateUI();
                ((JTextField) getEditor().getEditorComponent()).setColumns(5);
            }
        };
        ecb.setFont(font);
        ecb.setEditable(true);
        // the next line is to make the combobox not too wide
        ecb.setPrototypeDisplayValue("sample");
        result.add(ecb);

        JSpinner s = new JSpinner(
                new SpinnerListModel(new Object[] { "sample0", "sample", "sample2" }));
        s.getModel().setValue("sample");
        s.setFont(font);
        result.add(s);

        JComboBox cb = new JComboBox(new Object[] { "sample" });
        cb.setFont(font);
        result.add(cb);

        button.setFont(font);
        result.add(button);

        JRadioButton radio = new JRadioButton("sample");
        radio.setFont(font);
        result.add(radio);

        JCheckBox check = new JCheckBox("sample");
        check.setFont(font);
        result.add(check);

        SwingUtilities.invokeLater(result::revalidate);

        return result;
    }

    public static void main(String[] args) throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> {
            SubstanceCortex.GlobalScope.setSkin(new BusinessBlackSteelSkin());
            JFrame frame = new JFrame("Alignment");
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            HAlignmentPanel panel = new HAlignmentPanel();
            panel.initialize();
            frame.add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
