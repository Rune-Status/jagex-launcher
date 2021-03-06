#! /bin/sh
#
# Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#

# Generate exception classes

SPEC=$1
DST=$2

gen() {
  ID=$1
  WHAT=$2
  SVUID=$3 
  ARG_TYPE=$4
  ARG_ID=$5
  ARG_PROP=$6
  ARG_PHRASE=$7
  ARG_PARAM="$ARG_TYPE$ $ARG_ID"
  echo '-->' $DST/$ID.java
  out=$DST/${ID}.java

  $SH ./addNotices.sh "$COPYRIGHT_YEARS" > $out

cat >>$out <<__END__

// -- This file was mechanically generated: Do not edit! -- //

package $PACKAGE;


/**$WHAT
 *
 * @since $SINCE
 */

public `if [ ${ABSTRACT:-0} = 1 ];
        then echo 'abstract '; fi`class $ID
    extends ${SUPER}
{

    private static final long serialVersionUID = $SVUID;
__END__

  if [ $ARG_ID ]; then

    cat >>$out <<__END__

    private $ARG_TYPE $ARG_ID;

    /**
     * Constructs an instance of this class. </p>
     *
     * @param  $ARG_ID
     *         The $ARG_PHRASE
     */
    public $ID($ARG_TYPE $ARG_ID) {
        super(String.valueOf($ARG_ID));
	this.$ARG_ID = $ARG_ID;
    }

    /**
     * Retrieves the $ARG_PHRASE. </p>
     *
     * @return  The $ARG_PHRASE
     */
    public $ARG_TYPE get$ARG_PROP() {
        return $ARG_ID;
    }

}
__END__

  else

    cat >>$out <<__END__

    /**
     * Constructs an instance of this class.
     */
    public $ID() { }

}
__END__

  fi
}

. $SPEC
