/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.h2gis.h2spatialext.jai;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Vector;

import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.ROI;

/**
 * Abstract base class for operators that generate non-image attributes from a
 * source image while passing source image pixels directly to destination pixels.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AttributeOpImage extends OpImage {

    /**
     * An optional ROI to define the region over which to derive attributes.
     * If an ROI is not provided this will be set to an ROIShape having the
     * bounds of the source image.
     */
    protected ROI roi;
    
    /**
     * Bounds of the source image as a convenience.
     */
    protected Rectangle srcBounds;


    /**
     * Constructor.
     *
     * @param source the source image from from which to derive attributes
     * @param roi an optional {@code ROI} to define sub-region(s) of the
     *        source image to examine.
     */
    public AttributeOpImage(RenderedImage source, ROI roi) {
        super(wrapSource(source),
              new ImageLayout(source),
              null,
              false);

        this.roi = roi;
        this.srcBounds = getSourceImage(0).getBounds();
    }
    
    /**
     * Helper function for constructor. Wraps the given image in a {@code Vector}
     * as required by {@link OpImage} constructor. Also checks for a null 
     * source image.
     * 
     * @param img the image to wrap
     * @return a new {@code Vector} containing the image
     */
    private static Vector wrapSource(RenderedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("Source image must not be null");
        }
        
        Vector<RenderedImage> v = new Vector<RenderedImage>();
        v.add(img);
        return v;
    }

    /**
     * Always returns false since source and destination tiles are the same.
     */
    @Override
    public boolean computesUniqueTiles() {
        return false;
    }

    /**
     * Gets the requested image tile (which will be from the source image).
     * 
     * @param tileX  tile X index
     * @param tileY  tile Y index
     *
     * @return the requested tile
     */
    @Override
    public Raster getTile(int tileX, int tileY) {
        return this.getSourceImage(0).getTile(tileX, tileY);
    }

    /**
     * Handles a request to compute image data for a given tile by
     * simply passing a getTile request to the source image.
     * 
     * @param tileX  tile X index
     * @param tileY  tile Y index
     *
     * @return the requested tile
     */
    @Override
    public Raster computeTile(int tileX, int tileY) {
        return getTile(tileX, tileY);
    }

    /**
     * Gets the requested image tiles (which will be from the source image).
     *
     * @param tileIndices tile X and Y indices as {@code Points}
     *
     * @throws IllegalArgumentException  If {@code tileIndices} is {@code null}.
     */
    @Override
    public Raster[] getTiles(Point[] tileIndices) {
        if ( tileIndices == null ) {
            throw new IllegalArgumentException("tileIndices must not be null");
        }

        return getSourceImage(0).getTiles(tileIndices);
    }

    /**
     * Maps the source rectangle into destination space (which will be identical).
     *
     * @param sourceRect the {@code Rectangle} in source image coordinates
     * 
     * @param sourceIndex the index of the source image (must be 0 since there
     *        is only one source image)
     *
     * @return A new {@code Rectangle} with identical bounds to {@code sourceRect}
     *
     * @throws IllegalArgumentException if {@code sourceRect} is {@code null}
     * @throws IllegalArgumentException if {@code sourceIndex} is not 0
     */
    public Rectangle mapSourceRect(Rectangle sourceRect, int sourceIndex) {
        if ( sourceRect == null ) {
            throw new IllegalArgumentException("sourceRect must not be null");
        }

        if (sourceIndex != 0) {
            throw new IllegalArgumentException("sourceIndex arg must be 0");
        }
        
        return new Rectangle(sourceRect);
    }
    
    /**
     * Maps the destination rectangle into source image space (which will be identical).
     *
     * @param destRect the {@code Rectangle} in destination image coordinates
     * 
     * @param sourceIndex the index of the source image (must be 0 since there
     *        is only one source image)
     *
     * @return A new {@code Rectangle} with identical bounds to {@code destRect}
     *
     * @throws IllegalArgumentException if {@code destRect} is {@code null}
     * @throws IllegalArgumentException if {@code sourceIndex} is not 0
     */
    public Rectangle mapDestRect(Rectangle destRect, int sourceIndex) {
        if ( destRect == null ) {
            throw new IllegalArgumentException("destRect must not be null");
        }

        if (sourceIndex != 0) {
            throw new IllegalArgumentException("sourceIndex arg must be 0");
        }
        
        return new Rectangle(destRect);
    }

    /**
     * Retrieves an attribute by name. Calling this method will fire a request 
     * for it to be generated if it has not already been so, or if the sub-class
     * does not cache the attribute.
     * 
     * @param name the attribute name
     * 
     * @return the requested attribute or {@code null} if the name does not
     *         match any of the available attributes
     *
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    protected abstract Object getAttribute(String name);
    
    /**
     * Returns the class of the specified attribute. This
     * default implementation always returns class Object.
     * 
     * @param name attribute name
     * @return the attribute class
     */
    protected Class<?> getAttributeClass(String name) {
        return Object.class;
    }

    /**
     * Returns the names of available attributes.
     * 
     * @return attribute names
     */
    protected abstract String[] getAttributeNames();
    
    private boolean isAttributeName(String name) {
        String[] attributeNames = getAttributeNames();
        for (int i = 0; i < attributeNames.length; i++) {
            if (name.equalsIgnoreCase(attributeNames[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getProperty(String name) {
        if (isAttributeName(name)) {
            return getAttribute(name);
        }
        
        return super.getProperty(name);
    }

    @Override
    public Class getPropertyClass(String name) {
        if (isAttributeName(name)) {
            return getAttributeClass(name);
        }
        
        return super.getPropertyClass(name);
    }

    @Override
    public String[] getPropertyNames() {
        String[] superNames = super.getPropertyNames();
        if (superNames == null) superNames = new String[0];
        
        String[] myNames = getAttributeNames();
        if (myNames == null) myNames = new String[0];
        
        String[] names = new String[superNames.length + myNames.length];
        
        int i, k = 0;
        for (i = 0; i < superNames.length; i++) names[k++] = superNames[i];
        for (i = 0; i < myNames.length; i++) names[k++] = myNames[i];
        
        return names;
    }
    
}