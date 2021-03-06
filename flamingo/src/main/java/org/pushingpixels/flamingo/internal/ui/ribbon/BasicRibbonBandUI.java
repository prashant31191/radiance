/*
 * Copyright (c) 2005-2018 Flamingo Kirill Grouchnikov. All Rights Reserved.
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
package org.pushingpixels.flamingo.internal.ui.ribbon;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.substance.ribbon.ui.SubstanceRibbonBandBorder;
import org.pushingpixels.substance.api.SubstanceCortex;
import org.pushingpixels.substance.api.SubstanceSlices;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.swing.SwingRepaintCallback;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Basic UI for ribbon band {@link JRibbonBand}.
 *
 * @author Kirill Grouchnikov
 * @author Matt Nathan
 */
public abstract class BasicRibbonBandUI extends RibbonBandUI {
    /**
     * The associated ribbon band.
     */
    protected AbstractRibbonBand ribbonBand;

    /**
     * The button for collapsed state.
     */
    protected JCommandButton collapsedButton;

    /**
     * The band expand button. Is visible when the {@link JRibbonBand#getExpandActionListener()} of
     * the associated ribbon band is not <code>null</code>.
     */
    protected AbstractCommandButton expandButton;

    protected float rolloverAmount;

    protected Timeline rolloverTimeline;

    /**
     * Mouse listener on the associated ribbon band.
     */
    protected MouseListener mouseListener;

    /**
     * Listens to property changes on the associated ribbon band.
     */
    protected PropertyChangeListener propertyChangeListener;

    /**
     * Action listener on the expand button.
     */
    protected ActionListener expandButtonActionListener;

    /**
     * Popup panel that shows the contents of the ribbon band when it is in a collapsed state.
     *
     * @author Kirill Grouchnikov
     */
    protected static class CollapsedButtonPopupPanel extends JPopupPanel {
        /**
         * The main component of <code>this</code> popup panel. Can be <code>null</code>.
         */
        protected Component component;

        /**
         * Creates popup gallery with the specified component.
         *
         * @param component    The main component of the popup gallery.
         * @param originalSize The original dimension of the main component.
         */
        public CollapsedButtonPopupPanel(Component component, Dimension originalSize) {
            this.component = component;
            this.setLayout(new BorderLayout());
            this.add(component, BorderLayout.CENTER);
            // System.out.println("Popup dim is " + originalSize);
            this.setPreferredSize(originalSize);
            this.setSize(originalSize);
        }

        /**
         * Removes the main component of <code>this</code> popup gallery.
         *
         * @return The removed main component.
         */
        public Component removeComponent() {
            this.remove(this.component);
            return this.component;
        }

        /**
         * Returns the main component of <code>this</code> popup gallery.
         *
         * @return The main component.
         */
        public Component getComponent() {
            return this.component;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    @Override
    public void installUI(JComponent c) {
        this.ribbonBand = (AbstractRibbonBand) c;

        this.rolloverTimeline = new Timeline(this);
        this.rolloverTimeline.addPropertyToInterpolate("rolloverAmount", 0.0f, 1.0f);
        this.rolloverTimeline.addCallback(new SwingRepaintCallback(this.ribbonBand));
        this.rolloverTimeline.setDuration(250);

        installDefaults();
        installComponents();
        installListeners();
        c.setLayout(createLayoutManager());
        AWTRibbonEventListener.install();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
     */
    @Override
    public void uninstallUI(JComponent c) {
        c.setLayout(null);
        uninstallListeners();
        uninstallDefaults();
        uninstallComponents();
        if (!AWTRibbonEventListener.uninstall()) {
            // should remove other methods of tracking
        }
    }

    /**
     * Installs default parameters on the associated ribbon band.
     */
    protected void installDefaults() {
        Color bg = this.ribbonBand.getBackground();
        if (bg == null || bg instanceof UIResource) {
            this.ribbonBand.setBackground(new ColorUIResource(
                    SubstanceCortex.ComponentScope.getCurrentSkin(this.ribbonBand).
                            getActiveColorScheme(SubstanceSlices.DecorationAreaType.NONE).
                            getBackgroundFillColor()));
        }

        Border b = this.ribbonBand.getBorder();
        if (b == null || b instanceof UIResource) {
            this.ribbonBand.setBorder(new SubstanceRibbonBandBorder());
        }
    }

    /**
     * Installs subcomponents on the associated ribbon band.
     */
    protected void installComponents() {
        this.collapsedButton = new JCommandButton(this.ribbonBand.getTitle(),
                this.ribbonBand.getIcon());
        this.collapsedButton.setDisplayState(CommandButtonDisplayState.BIG);
        this.collapsedButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        this.collapsedButton.setPopupKeyTip(this.ribbonBand.getCollapsedStateKeyTip());
        this.ribbonBand.add(this.collapsedButton);

        if (this.ribbonBand.getExpandActionListener() != null) {
            this.expandButton = this.createExpandButton();
            this.ribbonBand.add(this.expandButton);
        }
    }

    /**
     * Creates the expand button for the associated ribbon band.
     *
     * @return Expand button for the associated ribbon band.
     */
    protected abstract JCommandButton createExpandButton();

    protected abstract void syncExpandButtonIcon();

    /**
     * Installs listeners on the associated ribbon band.
     */
    protected void installListeners() {
        // without this empty adapter, the global listener never
        // gets mouse entered events on the ribbon band
        this.mouseListener = new MouseAdapter() {
        };
        this.ribbonBand.addMouseListener(this.mouseListener);

        configureExpandButton();

        this.propertyChangeListener = (PropertyChangeEvent evt) -> {
            if ("title".equals(evt.getPropertyName()))
                ribbonBand.repaint();
            if ("expandButtonKeyTip".equals(evt.getPropertyName())) {
                if (expandButton != null) {
                    expandButton.setActionKeyTip((String) evt.getNewValue());
                }
            }
            if ("expandButtonRichTooltip".equals(evt.getPropertyName())) {
                if (expandButton != null) {
                    expandButton.setActionRichTooltip((RichTooltip) evt.getNewValue());
                }
            }
            if ("collapsedStateKeyTip".equals(evt.getPropertyName())) {
                if (collapsedButton != null) {
                    collapsedButton.setPopupKeyTip((String) evt.getNewValue());
                }
            }
            if ("expandActionListener".equals(evt.getPropertyName())) {
                ActionListener oldListener = (ActionListener) evt.getOldValue();
                ActionListener newListener = (ActionListener) evt.getNewValue();

                if ((oldListener != null) && (newListener == null)) {
                    // need to remove
                    unconfigureExpandButton();
                    ribbonBand.remove(expandButton);
                    expandButton = null;
                    ribbonBand.revalidate();
                }
                if ((oldListener == null) && (newListener != null)) {
                    // need to add
                    expandButton = createExpandButton();
                    configureExpandButton();
                    ribbonBand.add(expandButton);
                    ribbonBand.revalidate();
                }
                if ((oldListener != null) && (newListener != null)) {
                    // need to reconfigure
                    expandButton.removeActionListener(oldListener);
                    expandButton.addActionListener(newListener);
                }
            }
            if ("componentOrientation".equals(evt.getPropertyName())) {
                if (expandButton != null) {
                    syncExpandButtonIcon();
                }
            }
        };
        this.ribbonBand.addPropertyChangeListener(this.propertyChangeListener);
    }

    protected void configureExpandButton() {
        if (this.expandButton != null) {
            this.expandButton.addActionListener(this.ribbonBand.getExpandActionListener());

            this.expandButtonActionListener = (ActionEvent e) -> SwingUtilities
                    .invokeLater(() -> trackMouseCrossing(false));
            this.expandButton.addActionListener(this.expandButtonActionListener);
        }
    }

    /**
     * Uninstalls default parameters from the associated ribbon band.
     */
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(this.ribbonBand);
    }

    /**
     * Uninstalls components from the associated ribbon band.
     */
    protected void uninstallComponents() {
        if (this.collapsedButton.isVisible()) {
            // restore the control panel to the ribbon band.
            CollapsedButtonPopupPanel popupPanel = (collapsedButton.getPopupCallback() == null)
                    ? null
                    : (CollapsedButtonPopupPanel) collapsedButton.getPopupCallback()
                    .getPopupPanel(collapsedButton);
            if (popupPanel != null) {
                AbstractRibbonBand bandFromPopup = (AbstractRibbonBand) popupPanel
                        .removeComponent();
                ribbonBand.setControlPanel(bandFromPopup.getControlPanel());
                ribbonBand.setPopupRibbonBand(null);
                collapsedButton.setPopupCallback(null);
            }
        }

        this.ribbonBand.remove(this.collapsedButton);
        this.collapsedButton = null;

        if (this.expandButton != null)
            this.ribbonBand.remove(this.expandButton);

        this.expandButton = null;
        this.ribbonBand = null;
    }

    /**
     * Uninstalls listeners from the associated ribbon band.
     */
    protected void uninstallListeners() {
        this.ribbonBand.removePropertyChangeListener(this.propertyChangeListener);
        this.propertyChangeListener = null;

        this.ribbonBand.removeMouseListener(this.mouseListener);
        this.mouseListener = null;

        unconfigureExpandButton();
    }

    protected void unconfigureExpandButton() {
        if (this.expandButton != null) {
            this.expandButton.removeActionListener(this.expandButtonActionListener);
            this.expandButtonActionListener = null;
            this.expandButton.removeActionListener(this.ribbonBand.getExpandActionListener());
        }
    }

    /**
     * Invoked by <code>installUI</code> to create a layout manager object to manage the
     * {@link JCommandButtonStrip}.
     *
     * @return a layout manager object
     */
    protected LayoutManager createLayoutManager() {
        return new RibbonBandLayout();
    }

    /**
     * Layout for the ribbon band.
     *
     * @author Kirill Grouchnikov
     */
    private class RibbonBandLayout implements LayoutManager {

        /*
         * (non-Javadoc)
         *
         * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
         */
        public void addLayoutComponent(String name, Component c) {
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
         */
        public void removeLayoutComponent(Component c) {
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
         */
        public Dimension preferredLayoutSize(Container c) {
            Insets ins = c.getInsets();
            AbstractBandControlPanel controlPanel = ribbonBand.getControlPanel();
            boolean useCollapsedButton = (controlPanel == null) || !controlPanel.isVisible();
            int width = useCollapsedButton ? collapsedButton.getPreferredSize().width
                    : controlPanel.getPreferredSize().width;
            int height = (useCollapsedButton ? collapsedButton.getPreferredSize().height
                    : controlPanel.getPreferredSize().height) + getBandTitleHeight();

            // System.out.println(ribbonBand.getTitle() + ":" + height);

            // Preferred height of the ribbon band is:
            // 1. Insets on top and bottom
            // 2. Preferred height of the control panel
            // 3. Preferred height of the band title panel
            // System.out.println("Ribbon band pref = "
            // + (height + ins.top + ins.bottom));

            return new Dimension(width + 2 + ins.left + ins.right, height + ins.top + ins.bottom);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
         */
        public Dimension minimumLayoutSize(Container c) {
            Insets ins = c.getInsets();
            AbstractBandControlPanel controlPanel = ribbonBand.getControlPanel();
            boolean useCollapsedButton = (controlPanel == null) || (!controlPanel.isVisible());
            int width = useCollapsedButton ? collapsedButton.getMinimumSize().width
                    : controlPanel.getMinimumSize().width;
            int height = useCollapsedButton
                    ? collapsedButton.getMinimumSize().height + getBandTitleHeight()
                    : controlPanel.getMinimumSize().height + getBandTitleHeight();

            // System.out.println(useCollapsedButton + ":" + height);

            // Preferred height of the ribbon band is:
            // 1. Insets on top and bottom
            // 2. Preferred height of the control panel
            // 3. Preferred height of the band title panel
            return new Dimension(width + 2 + ins.left + ins.right, height + ins.top + ins.bottom);

        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(Container c) {
            // System.out.println("Ribbon band real = " + c.getHeight());
            if (!c.isVisible())
                return;
            Insets ins = c.getInsets();

            int availableHeight = c.getHeight() - ins.top - ins.bottom;
            RibbonBandResizePolicy resizePolicy = ((AbstractRibbonBand) c).getCurrentResizePolicy();

            if (resizePolicy instanceof IconRibbonBandResizePolicy) {
                collapsedButton.setVisible(true);
                int collapsedButtonWidth = c.getWidth() - ins.left - ins.right - 2;
                collapsedButton.setBounds((c.getWidth() - collapsedButtonWidth) / 2, ins.top,
                        collapsedButtonWidth, c.getHeight() - ins.top - ins.bottom);

                if (collapsedButton.getPopupCallback() == null) {
                    final AbstractRibbonBand popupBand = ribbonBand.cloneBand();
                    popupBand.setControlPanel(ribbonBand.getControlPanel());
                    List<RibbonBandResizePolicy> resizePolicies = ribbonBand.getResizePolicies();
                    popupBand.setResizePolicies(resizePolicies);
                    RibbonBandResizePolicy largest = resizePolicies.get(0);
                    popupBand.setCurrentResizePolicy(largest);
                    int gap = popupBand.getControlPanel().getUI().getLayoutGap();
                    final Dimension size = new Dimension(
                            ins.left + ins.right + gap
                                    + largest.getPreferredWidth(availableHeight, gap),
                            ins.top + ins.bottom
                                    + Math.max(c.getHeight(),
                                    ribbonBand.getControlPanel().getPreferredSize().height
                                            + getBandTitleHeight()));
                    collapsedButton.setPopupCallback(
                            (JCommandButton commandButton) -> new CollapsedButtonPopupPanel(
                                    popupBand, size));
                    ribbonBand.setControlPanel(null);
                    ribbonBand.setPopupRibbonBand(popupBand);
                }

                if (expandButton != null)
                    expandButton.setBounds(0, 0, 0, 0);

                return;
            }

            if (collapsedButton.isVisible()) {
                // was icon and now is normal band - have to restore the
                // control panel
                CollapsedButtonPopupPanel popupPanel = (collapsedButton.getPopupCallback() != null)
                        ? (CollapsedButtonPopupPanel) collapsedButton.getPopupCallback()
                        .getPopupPanel(collapsedButton)
                        : null;
                if (popupPanel != null) {
                    AbstractRibbonBand bandFromPopup = (AbstractRibbonBand) popupPanel
                            .removeComponent();
                    ribbonBand.setControlPanel(bandFromPopup.getControlPanel());
                    ribbonBand.setPopupRibbonBand(null);
                    collapsedButton.setPopupCallback(null);
                }
            }
            collapsedButton.setVisible(false);

            AbstractBandControlPanel controlPanel = ribbonBand.getControlPanel();
            controlPanel.setVisible(true);
            controlPanel.setBounds(ins.left, ins.top, c.getWidth() - ins.left - ins.right,
                    c.getHeight() - getBandTitleHeight() - ins.top - ins.bottom);
            controlPanel.doLayout();

            if (expandButton != null) {
                int ebpw = expandButton.getPreferredSize().width;
                int ebph = expandButton.getPreferredSize().height;
                int maxHeight = getBandTitleHeight() - 4;
                if (ebph > maxHeight)
                    ebph = maxHeight;

                int expandButtonBottomY = c.getHeight() - (getBandTitleHeight() - ebph) / 2;

                boolean ltr = ribbonBand.getComponentOrientation().isLeftToRight();

                if (ltr) {
                    expandButton.setBounds(c.getWidth() - ins.right - ebpw,
                            expandButtonBottomY - ebph, ebpw, ebph);
                } else {
                    expandButton.setBounds(ins.left, expandButtonBottomY - ebph, ebpw, ebph);
                }
            }
        }
    }

    /**
     * Event listener to handle global ribbon events. Currently handles:
     * <ul>
     * <li>Marking a ribbon band to be hovered when the mouse moves over it.</li>
     * <li>Mouse wheel events anywhere in the ribbon to rotate the selected task.</li>
     * </ul>
     */
    private static class AWTRibbonEventListener implements AWTEventListener {
        private static AWTRibbonEventListener instance;
        private int installCount = 0;
        private AbstractRibbonBand lastHovered;

        public static void install() {
            if (instance == null) {
                instance = new AWTRibbonEventListener();
                java.security.AccessController
                        .doPrivileged(new java.security.PrivilegedAction<Object>() {
                            public Object run() {
                                Toolkit.getDefaultToolkit().addAWTEventListener(instance,
                                        AWTEvent.MOUSE_EVENT_MASK
                                                | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
                                return null;
                            }
                        });
            }
            instance.installCount++;
        }

        public static boolean uninstall() {
            if (instance != null) {
                instance.installCount--;
                if (instance.installCount == 0) {
                    // really uninstall
                    Toolkit.getDefaultToolkit().removeAWTEventListener(instance);
                    instance = null;
                }
                return true;
            }
            return false;
        }

        public void eventDispatched(AWTEvent event) {
            MouseEvent mouseEvent = (MouseEvent) event;
            if (mouseEvent.getID() == MouseEvent.MOUSE_ENTERED) {
                Object object = event.getSource();
                if (!(object instanceof Component)) {
                    return;
                }
                Component component = (Component) object;
                AbstractRibbonBand band = (component instanceof AbstractRibbonBand)
                        ? ((AbstractRibbonBand) component)
                        : (AbstractRibbonBand) SwingUtilities
                        .getAncestorOfClass(AbstractRibbonBand.class, component);
                setHoveredBand(band);
            }

            if (mouseEvent.getID() == MouseEvent.MOUSE_WHEEL) {
                if (PopupPanelManager.defaultManager().getShownPath().size() > 0)
                    return;

                Object object = event.getSource();
                if (!(object instanceof Component)) {
                    return;
                }
                Component component = (Component) object;
                // get the deepest subcomponent at the event point
                MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) mouseEvent;
                Component deepest = SwingUtilities.getDeepestComponentAt(component,
                        mouseWheelEvent.getX(), mouseWheelEvent.getY());
                JRibbon ribbon = (JRibbon) SwingUtilities.getAncestorOfClass(JRibbon.class,
                        deepest);
                if (ribbon != null) {
                    // if the mouse wheel scroll has happened inside a ribbon,
                    // ask the UI delegate to handle it
                    ribbon.getUI().handleMouseWheelEvent((MouseWheelEvent) mouseEvent);
                }
            }
        }

        private void setHoveredBand(AbstractRibbonBand band) {
            if (lastHovered == band) {
                return; // nothing to do as we are already over
            }
            if (lastHovered != null) {
                // RibbonBandUI ui = lastHovered.getUI();
                lastHovered.getUI().trackMouseCrossing(false);
            }
            lastHovered = band;
            if (band != null) {
                band.getUI().trackMouseCrossing(true);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D graphics = (Graphics2D) g.create();

        Insets ins = ribbonBand.getInsets();

        this.paintBandBackground(graphics, new Rectangle(0, 0, c.getWidth(), c.getHeight()));

        if (!(ribbonBand.getCurrentResizePolicy() instanceof IconRibbonBandResizePolicy)) {
            String title = ribbonBand.getTitle();
            int titleHeight = getBandTitleHeight();

            int bandTitleTopY = c.getHeight() - titleHeight;

            this.paintBandTitleBackground(graphics,
                    new Rectangle(0, bandTitleTopY, c.getWidth(), titleHeight), title);
            boolean ltr = ribbonBand.getComponentOrientation().isLeftToRight();
            int titleWidth = c.getWidth() - 2 * ins.left - 2 * ins.right;
            int titleX = 2 * ins.left;
            if (expandButton != null) {
                if (ltr) {
                    titleWidth = expandButton.getX() - 2 * ins.right - 2 * ins.left;
                } else {
                    titleWidth = ribbonBand.getWidth() - expandButton.getX()
                            - expandButton.getWidth() - 2 * ins.right - 2 * ins.left;
                    titleX = expandButton.getX() + expandButton.getWidth() + 2 * ins.left;
                }
            }
            this.paintBandTitle(graphics,
                    new Rectangle(titleX, bandTitleTopY, titleWidth, titleHeight), title);
        }

        graphics.dispose();
    }

    /**
     * Paints band title pane.
     *
     * @param g              Graphics context.
     * @param titleRectangle Rectangle for the title pane.
     * @param title          Title string.
     */
    protected abstract void paintBandTitle(Graphics g, Rectangle titleRectangle, String title);

    /**
     * Paints band title pane.
     *
     * @param g              Graphics context.
     * @param titleRectangle Rectangle for the title pane.
     * @param title          Title string.
     */
    protected abstract void paintBandTitleBackground(Graphics g, Rectangle titleRectangle, String title);

    /**
     * Paints band background.
     *
     * @param graphics Graphics context.
     * @param toFill   Rectangle for the background.
     */
    protected abstract void paintBandBackground(Graphics graphics, Rectangle toFill);

    @Override
    public float getRolloverAmount() {
        return this.rolloverAmount;
    }

    // This is needed for running the rollover animation tracked by rolloverTimeline.
    public void setRolloverAmount(float rolloverAmount) {
        this.rolloverAmount = rolloverAmount;
    }

    @Override
    public int getPreferredCollapsedWidth() {
        // Don't let long ribbon band titles create collapsed buttons that are too wide
        Dimension collapsedPreferredSize = this.collapsedButton.getPreferredSize();
        return Math.min((int) (collapsedPreferredSize.height * 1.25),
                collapsedPreferredSize.width + 2);
    }

    @Override
    public void trackMouseCrossing(boolean isMouseIn) {
        if (isMouseIn) {
            this.rolloverTimeline.play();
        } else {
            this.rolloverTimeline.playReverse();
        }
        this.ribbonBand.repaint();
    }
}
