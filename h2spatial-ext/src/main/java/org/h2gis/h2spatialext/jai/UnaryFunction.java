/*
 * @(#) $Header: /cvs/baselib/src/main/ca/forklabs/baselib/util/UnaryFunction.java,v 1.2 2006/12/23 04:30:26 forklabs Exp $
 *
 * Copyright (C)  2006  Daniel Léonard
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

/**
 * An {@code UnaryFunction} is a function object that takes an object, does some
 * operation on it and then returns a result, which can be {@code null}.
 *
 * @param <R> the type of the returned object.
 * @param <A> the type of the argument object.
 *
 * @author <a href="mailto:forklabs at
 * gmail.com?subject=ca.forklabs.baselib.util.UnaryFunction">Daniel L�onard</a>
 * @version $Revision: 1.2 $
 */
public interface UnaryFunction<R, A> {

    /**
     * Performs an operation on the specified object.
     *
     * @param arg an object.
     * @return the result of the operation.
     */
    public R invoke(A arg);

}
