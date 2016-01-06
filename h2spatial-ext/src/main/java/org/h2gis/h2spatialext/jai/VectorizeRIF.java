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

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.jai.ROI;

/**
 * The image factory for the Vectorize operator.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class VectorizeRIF implements RenderedImageFactory {

    /** Constructor */
    public VectorizeRIF() {
    }

    /**
     * Creates a new instance of VectorizeOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     *        "roi", "band", "outsideValues" and "insideEdges"
     *
     * @param renderHints rendering hints (ignored)
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        ROI roi = (ROI) paramBlock.getObjectParameter(VectorizeDescriptor.ROI_ARG);
        int band = paramBlock.getIntParameter(VectorizeDescriptor.BAND_ARG);
        
        List<Double> outsideValues = null;
        Object obj = paramBlock.getObjectParameter(VectorizeDescriptor.OUTSIDE_VALUES_ARG);
        if (obj != null) {
            outsideValues = new ArrayList<Double>();
            Collection coll = (Collection) obj;
            for (Object val : coll) {
                outsideValues.add(((Number)val).doubleValue());
            }
        }
        
        Boolean insideEdges = (Boolean) paramBlock.getObjectParameter(VectorizeDescriptor.INSIDE_EDGES_ARG);
        Boolean removeCollinear = (Boolean) paramBlock.getObjectParameter(VectorizeDescriptor.REMOVE_COLLINEAR_ARG);
        
        double filterThreshold = paramBlock.getDoubleParameter(VectorizeDescriptor.FILTER_SMALL_POLYS_ARG);
        if (Double.isNaN(filterThreshold)) {
            filterThreshold = 0;
        }
        int filterMethod = paramBlock.getIntParameter(VectorizeDescriptor.FILTER_METHOD_ARG);

        return new VectorizeOpImage(paramBlock.getRenderedSource(0), roi, band, outsideValues, 
                insideEdges, removeCollinear, filterThreshold, filterMethod);
    }
}

