<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2017 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
-->
    <xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.harctoolbox.org/irp-protocols https://www.harctoolbox.org/schemas/irp-protocols.xsd"
                xmlns:irp="http://www.harctoolbox.org/irp-protocols">

    <xsl:output method="text"/>

    <!--xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template-->

    <xsl:template match="/"># List of Irp Protocols, version <xsl:value-of select="irp:protocols/@version"/>

*Note:* The descriptions of the protocols are mostly taken from DecodeIR.
In some cases, it describes DecodeIR's handling of the protocol rather than the protocol.
"I" in the text likely denotes John Fine, the original author of DecodeIR.

        <xsl:apply-templates select="irp:protocols"/>

# Glossary

### DecodeIR
Library for the decoding of
                        IrSequences. Originally written by John Fine, extended by others; used
                        by many widely spread programs as a shared library (IrScrutinizer, IrpMaster, IrScope, RemoteMaster), often using
                        Java Native Interface. The current version is 2.45. [Current official documentation](http://www.hifi-remote.com/wiki/index.php?title=DecodeIR).
                        License: public domain. [Binaries for Windows, Linux, and Mac](http://www.hifi-remote.com/forums/dload.php?action=file&amp;file_id=13104).
                        [source code](https://sourceforge.net/p/controlremote/code/HEAD/tree/trunk/decodeir/).
                            [Arduino port](https://github.com/bengtmartensson/Arduino-DecodeIR).


### Executor
An embedded "program" for the rendering and transmission of one
                    or several protocols. One executor can manage several protocols; also,
                    for one protocol there may be several alternative executors. An executer has its own parametrization,
                    more-or-less similar to the parametrization of the protocol. Used in UEI Remotes
                    and RemoteMaster.

### repeat (ditto)
Simple sequence, without information, that is repeated as repeat sequence. For an example, see NEC1.

### spurious decodes
An imperfectly measured signal that incorrectly matches a protocol.
    </xsl:template>

    <xsl:template match="irp:protocols">
## Protocols
         <xsl:apply-templates select="irp:protocol"/>
    </xsl:template>

    <xsl:template match="irp:protocol">
### <xsl:value-of select="@name"/>
        <xsl:apply-templates select="@c-name"/>
        <xsl:apply-templates select="irp:irp"/>
        <xsl:apply-templates select="irp:parameter"/>
        <xsl:apply-templates select="irp:documentation"/>
    </xsl:template>

    <xsl:template match="@c-name">
        <xsl:text> (C name: `</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>`)</xsl:text>
    </xsl:template>

    <xsl:template match="irp:irp">
        <xsl:text>
```C
</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>
```
</xsl:text>
    </xsl:template>

    <xsl:template match="irp:documentation">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="irp:parameter">
        <xsl:text>**</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>:** </xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>

 </xsl:text>
    </xsl:template>

    <xsl:template match="a|A">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>](</xsl:text>
        <xsl:value-of select="@href"/>
        <xsl:text>)</xsl:text>

    </xsl:template>
</xsl:stylesheet>
