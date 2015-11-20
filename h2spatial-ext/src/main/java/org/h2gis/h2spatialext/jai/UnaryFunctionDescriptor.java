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

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Class {@code UnaryFunctionDescriptor} is an {@link OperationDescriptor}
 * describing the <em>unaryfunction</em> operation. This operation takes a
 * source image and creates a new image in which each pixel is the result of
 * applying an {@link UnaryFunction} to its corresponding pixel of the source
 * image. Only one source and one parameter are needed.
 *
 * <table border=1>
 * <caption>Resource List</caption>
 * <tr><th>Name</th>        <th>Value</th></tr>
 * <tr><td>GlobalName</td>  <td>UnaryFunction</td></tr>
 * <tr><td>LocalName</td>   <td>UnaryFunction</td></tr>
 * <tr><td>Vendor</td>      <td>ca.forklabs.media.jai.opimage</td></tr>
 * <tr><td>Description</td> <td>Application of an unary function on all the pixels</td></tr>
 * <tr><td>DocURL</td>      <td>n/a</td></tr>
 * <tr><td>Version</td>     <td>$Version$</td></tr>
 * <tr><td>Arg0Desct</td>   <td>The {@link UnaryFunction}</td></tr>
 * </table>
 *
 * <table border=1>
 * <caption>Parameter List</caption>
 * <tr><th>Name</th>       <th>Class Type</th>                            <th>Default Value</th></tr>
 * <tr><td>function</td>   <td>UnaryFunction&lt;Double, Double&gt;</td>   <td>NO_PARAMETER_DEFAULT</td>
 * </table>
 *
 * @author   <a href="mailto:forklabs at gmail.com?subject=ca.forklabs.media.jai.operator.UnaryFunctionDescriptor">Daniel Léonard</a>
 * @version $Revision$
 */
public class UnaryFunctionDescriptor extends OperationDescriptorImpl {

//---------------------------
// Class variables
//---------------------------

   /** <em>serialVersionUID</em>. */
   private static final long serialVersionUID = -3900844847894674507L;

   /** The name of this operator. */
   public static final String NAME = "UnaryFunction"; //$NON-NLS-1$

   /** The name of the unary function parameter. */
   public static final String FUNCTION_PARAMETER_NAME = "function"; //$NON-NLS-1$
   /** The index in the parameter block of the unary function parameter. */
   public static final int FUNCTION_PARAMETER_INDEX = 0;

   /**
    * The resource strings that provide the general documentation and specify
    * the parameter list for this operation.
    */
   private static final String[][] RESOURCES =
   {
      { "GlobalName",  UnaryFunctionDescriptor.NAME, }, //$NON-NLS-1$
      { "LocalName",   UnaryFunctionDescriptor.NAME, }, //$NON-NLS-1$
      { "Vendor",      "ca.forklabs.media.jai.", }, //$NON-NLS-1$ //$NON-NLS-2$
      { "Description", UnaryFunctionDescriptor.getDescription(), }, //$NON-NLS-1$
      { "DocURL",      "n/a", }, //$NON-NLS-1$ //$NON-NLS-2$
      { "Version",     "$Version$", }, //$NON-NLS-1$ //$NON-NLS-2$
      { "arg0Desc",    UnaryFunctionDescriptor.getArg0Description(), }, //$NON-NLS-1$
   };

   /** The supported modes. */
   private static final String[] SUPPORTED_MODES =
   {
      RenderedRegistryMode.MODE_NAME,
      RenderableRegistryMode.MODE_NAME,
   };

   /** The parameter class list for this operation. */
   private static final Class<?>[] PARAMETER_CLASSES = new Class<?>[]
   {
      UnaryFunction.class,
   };

   /** The parameter name list for this operation. */
   private static final String[] PARAMETER_NAMES = new String[]
   {
      UnaryFunctionDescriptor.FUNCTION_PARAMETER_NAME,
   };

// TODO : change for the identity function and update the documentation and manual
   /** The default parameters. */
   private static final Object[] PARAMETER_DEFAULTS = new Object[]
   {
      OperationDescriptor.NO_PARAMETER_DEFAULT,
   };

   /** Valid parameter values. */
   private static final Object[] VALID_PARAMETERS = new Object[]
   {
      null,
   };
    private static boolean registered = false;


//---------------------------
// Constructor
//---------------------------

   /**
    * Constructor.
    */
   public UnaryFunctionDescriptor() {
      super(RESOURCES, SUPPORTED_MODES, 1, PARAMETER_NAMES, PARAMETER_CLASSES, PARAMETER_DEFAULTS, VALID_PARAMETERS);
      }


//---------------------------
// Class methods
//---------------------------

   /**
    * Creates and fills a new parameter block.
    * @param   mode   the rendering mode.
    * @param   source   the source image.
    * @param   function   the unary function.
    * @return   a new parameter block.
    */
   public static ParameterBlockJAI createParameterBlock(String mode, Object source, UnaryFunction<Double, Double> function) {
      String name = UnaryFunctionDescriptor.NAME;
      ParameterBlockJAI pb = new ParameterBlockJAI(name, mode);
      if (null != source) {
         pb.addSource(source);
         }
      pb.setParameter(UnaryFunctionDescriptor.FUNCTION_PARAMETER_NAME, function);
      return pb;
      }

   /**
    * Creates a rendered image.
    * @param   source   the source image.
    * @param   function   the unary function.
    * @param   hints    the rendering hints, may be {@code null}.
    * @return   the new image.
    */
   public static RenderedOp create(RenderedImage source, UnaryFunction<Double, Double> function, RenderingHints hints) {
      String operation = UnaryFunctionDescriptor.NAME;
      String mode = RenderedRegistryMode.MODE_NAME;
      ParameterBlock pb = UnaryFunctionDescriptor.createParameterBlock(mode, source, function);
      RenderedOp image = JAI.create(operation, pb, hints);
      return image;
      }

   /**
    * Creates a renderable image.
    * @param   source   the source image.
    * @param   function   the unary function.
    * @param   hints    the rendering hints, may be {@code null}.
    * @return   the new image.
    */
   public static RenderableOp createRenderable(RenderableImage source, UnaryFunction<Double, Double> function, RenderingHints hints) {
      String operation = UnaryFunctionDescriptor.NAME;
      String mode = RenderableRegistryMode.MODE_NAME;
      ParameterBlock pb = UnaryFunctionDescriptor.createParameterBlock(mode, source, function);
      RenderableOp image = JAI.createRenderable(operation, pb, hints);
      return image;
      }


//---------------------------
// External resources methods
//---------------------------

   /**
    * Gets the description of this operation.
    * @return   the description message.
    */
   protected static String getDescription() {
      String key = "Description";
      String message = "Messages";
      return message;
      }

   /**
    * Gets the description of the first parameter.
    * @return   the description message.
    */
   protected static String getArg0Description() {
      String key = "Description";
      String message = "Messages";
      return message;
      }
   
   /**
     * A method to register this operator with the OperationRegistry and
     * RIFRegistry.
     */
    public static void register()
    {
        if (!registered)
        {
            // Get the OperationRegistry.
            OperationRegistry op = JAI.getDefaultInstance().getOperationRegistry();
            // Register the operator's descriptor.
            UnaryFunctionDescriptor desc =
                    new UnaryFunctionDescriptor();
            op.registerDescriptor(desc);
            // Register the operators's RIF.
            UnaryFunctionRIF rif = new UnaryFunctionRIF();
            RIFRegistry.register(op, "UnaryFunction", "h2gis", rif);
            registered = true;
        }

   }
}
