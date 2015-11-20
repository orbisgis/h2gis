/*
 * @(#) $Header$
 *
 * Copyright (C) 2007  Forklabs Daniel Léonard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.h2gis.h2spatialext.jai;

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ContextualRenderedImageFactory;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.ImageLayout;

/**
 * Class {@code UnaryFunctionRIF} is a {@link ContextualRenderedImageFactory}
 * supporting the <em>unaryfunction</em> operation in the rendered and
 * renderable image layers.
 *
 * @author <a href="mailto:forklabs at
 * gmail.com?subject=ca.forklabs.media.jai.opimage.UnaryFunctionCRIF">Daniel
 * Léonard</a>
 * @version $Revision$
 * @see UnaryFunctionOpImage
 * @author Erwan Bocher
 */
public class UnaryFunctionRIF implements RenderedImageFactory {

    /**
     * Empty constructor required
     */
    public UnaryFunctionRIF() {
    }

    /**
     * Gets the function.
     *
     * @param pb the parameter block.
     * @return the function.
     */
    @SuppressWarnings("unchecked")
    protected UnaryFunction<Double, Double> getFunction(ParameterBlock pb) {
        int index = UnaryFunctionDescriptor.FUNCTION_PARAMETER_INDEX;
        UnaryFunction<Double, Double> function = (UnaryFunction<Double, Double>) pb.getObjectParameter(index);
        return function;
    }

    /**
     * Creates a new instance of {@link UnaryFunctionOpImage} in the rendered
     * layer.
     *
     * @param pb the parameter block.
     * @param hints optional rendering hints.
     */
    @Override
    public RenderedImage create(ParameterBlock pb, RenderingHints hints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(hints);
        RenderedImage source = pb.getRenderedSource(0);
        UnaryFunction<Double, Double> function = this.getFunction(pb);
        return new UnaryFunctionOpImage(source, function, layout, hints);
    }

}
