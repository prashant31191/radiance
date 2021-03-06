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
package org.pushingpixels.flamingo.internal.ui.ribbon.appmenu;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * Basic UI for ribbon application menu button
 * {@link JRibbonApplicationMenuButton}.
 * 
 * @author Kirill Grouchnikov
 */
public abstract class BasicRibbonApplicationMenuButtonUI extends BasicCommandButtonUI {
	/**
	 * The associated application menu button.
	 */
	protected JRibbonApplicationMenuButton applicationMenuButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicButtonUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.applicationMenuButton = (JRibbonApplicationMenuButton) c;
		super.installUI(c);
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();

		Border border = this.commandButton.getBorder();
		if (border == null || border instanceof UIResource) {
			Border toInstall = UIManager.getBorder("RibbonApplicationMenuButton.border");
			if (toInstall == null)
				toInstall = new BorderUIResource.EmptyBorderUIResource(4, 4, 4, 4);
			this.commandButton.setBorder(toInstall);
		}

		this.commandButton.setOpaque(false);
	}

	@Override
	protected void installComponents() {
		super.installComponents();

		final JRibbonApplicationMenuButton appMenuButton = 
				(JRibbonApplicationMenuButton) this.commandButton;
		appMenuButton.setPopupCallback((JCommandButton commandButton) -> {
			JRibbonFrame ribbonFrame = (JRibbonFrame) SwingUtilities
					.getWindowAncestor(commandButton);
			final JRibbon ribbon = ribbonFrame.getRibbon();
			RibbonApplicationMenu ribbonMenu = ribbon.getApplicationMenu();
			final JRibbonApplicationMenuPopupPanel menuPopupPanel = 
					new JRibbonApplicationMenuPopupPanel(appMenuButton, ribbonMenu);
			menuPopupPanel.applyComponentOrientation(appMenuButton.getComponentOrientation());
			menuPopupPanel.setCustomizer(() -> {
				boolean ltr = commandButton.getComponentOrientation().isLeftToRight();

				int pw = menuPopupPanel.getPreferredSize().width;
				int x = ltr ? ribbon.getLocationOnScreen().x
						: ribbon.getLocationOnScreen().x + ribbon.getWidth() - pw;
				int y = commandButton.getLocationOnScreen().y + commandButton.getSize().height / 2
						+ 2;

				// make sure that the menu popup stays in bounds
				Rectangle scrBounds = commandButton.getGraphicsConfiguration().getBounds();
				if ((x + pw) > (scrBounds.x + scrBounds.width)) {
					x = scrBounds.x + scrBounds.width - pw;
				}
				int ph = menuPopupPanel.getPreferredSize().height;
				if ((y + ph) > (scrBounds.y + scrBounds.height)) {
					y = scrBounds.y + scrBounds.height - ph;
				}

				return new Rectangle(x, y, menuPopupPanel.getPreferredSize().width,
						menuPopupPanel.getPreferredSize().height);
			});
			return menuPopupPanel;
		});
	}
}
