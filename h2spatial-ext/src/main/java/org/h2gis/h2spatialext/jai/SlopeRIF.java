package org.h2gis.h2spatialext.jai;

import com.sun.media.jai.opimage.RIFUtil;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

/**
 * @author Nicolas Fortin
 */
public class SlopeRIF implements RenderedImageFactory {
    /**
     * Empty constructor required
     */
    public SlopeRIF()
    {
    }

    /**
     * The create method, that will be called to create a RenderedImage (or chain
     * of operators that represents one).
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints)
    {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);


        // Get BorderExtender from renderHints if any.
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);

        KernelJAI unRotatedKernel =
                (KernelJAI)paramBlock.getObjectParameter(0);
        KernelJAI kJAI = unRotatedKernel.getRotatedKernel();

        return new SlopeOpImage(paramBlock.getRenderedSource(0),
                extender,
                renderHints,
                layout,
                kJAI);
    }
}
