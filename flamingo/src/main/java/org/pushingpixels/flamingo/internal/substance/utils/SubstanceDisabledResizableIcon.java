/*
 * Copyright (c) 2005-2018 Flamingo / Substance Kirill Grouchnikov. All Rights Reserved.
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
package org.pushingpixels.flamingo.internal.substance.utils;

import org.pushingpixels.neon.AsynchronousLoading;
import org.pushingpixels.neon.NeonCortex;
import org.pushingpixels.neon.icon.ResizableIcon;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.colorscheme.SubstanceColorScheme;
import org.pushingpixels.substance.internal.utils.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Implementation of a resizable icon of disabled controls based on the current Substance skin.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceDisabledResizableIcon implements ResizableIcon {
    /**
     * Image cache to speed up rendering.
     */
    protected LazyResettableHashMap<BufferedImage> cachedImages;

    /**
     * The main (pre-filtered) icon.
     */
    protected ResizableIcon delegate;

    /**
     * Creates a new filtered icon.
     * 
     * @param delegate
     *            The main (pre-filtered) icon.
     */
    public SubstanceDisabledResizableIcon(ResizableIcon delegate) {
        super();
        this.delegate = delegate;
        this.cachedImages = new LazyResettableHashMap<BufferedImage>(
                "FlamingoSubstanceDisabledIcons");
    }

    @Override
    public int getIconHeight() {
        return delegate.getIconHeight();
    }

    @Override
    public int getIconWidth() {
        return delegate.getIconWidth();
    }

    @Override
    public void setDimension(Dimension newDimension) {
        delegate.setDimension(newDimension);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // check if loading
        if (this.delegate instanceof AsynchronousLoading) {
            AsynchronousLoading asyncDelegate = (AsynchronousLoading) this.delegate;
            // if the delegate is still loading - do nothing
            if (asyncDelegate.isLoading())
                return;
        }

        SubstanceColorScheme scheme = SubstanceColorSchemeUtilities.getColorScheme(c,
                ComponentState.DISABLED_UNSELECTED);
        HashMapKey key = SubstanceCoreUtilities.getHashKey(this.getIconWidth(),
                this.getIconHeight(), scheme.getDisplayName());

        BufferedImage filtered = this.cachedImages.get(key);
        if (filtered == null) {
            BufferedImage offscreen = SubstanceCoreUtilities.getBlankImage(this.getIconWidth(),
                    this.getIconHeight());
            Graphics2D g2d = offscreen.createGraphics();
            this.delegate.paintIcon(c, g2d, 0, 0);
            g2d.dispose();
            filtered = SubstanceImageCreator.getColorSchemeImage(offscreen, scheme, 0.5f);
            this.cachedImages.put(key, filtered);
        }
        Graphics2D g2d = (Graphics2D) g.create();
        NeonCortex.drawImage(g2d, filtered, 0, 0);
        g2d.dispose();
    }
}