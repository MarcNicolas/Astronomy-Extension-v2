 /*******************************************************************************
 * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of SITools2.
 *
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.astro.vo.sia;

/**
 * Exception for graph package.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class SimpleImageAccessException extends Exception {
    /**
     * Empty constructor.
     */
    public SimpleImageAccessException() {
        super();
    }
    /**
     * Constructs an Exception with a message.
     * @param message message
     */
    public SimpleImageAccessException(final String message) {
        super(message);
    }
    /**
     * Constructs an Exception with a cause.
     * @param cause cause
     */
    public SimpleImageAccessException(final Throwable cause) {
        super(cause);
    }
    /**
     * Constructs an Exception with a message and a cause.
     * @param message message
     * @param cause cause
     */
    public SimpleImageAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
