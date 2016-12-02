<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2016 Bengt Martensson.

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
                xsi:schemaLocation="http://www.harctoolbox.org/irp-protocols http://www.harctoolbox.org/schemas/irp-protocols.xsd"
                xmlns:irp="http://www.harctoolbox.org/irp-protocols">

    <xsl:output method="html"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/">
        <html>
            <head>
                <title>
                    <xsl:text>IRP protocols version </xsl:text>
                    <xsl:value-of select="irp:protocols/@version"/>
                </title>
                <style>
                    <xsl:attribute name="type">text/css</xsl:attribute>
                    div.documentation { }
                    div.irp { font-family:'Lucida Console', monospace;
                              background: LightGray;
                            }
                    span.parametername { font-family:'Lucida Console', monospace;
                                         font-weight: bold;
                                       }
                </style>
            </head>
            <body>
                <h1>
                    <xsl:text>List of Irp Protocols, version </xsl:text>
                    <xsl:value-of select="irp:protocols/@version"/>
                </h1>
                <p>
                    <strong>Note: </strong>
                    The descriptions of the protocols are mostly taken from <a href="#DecodeIR">DecodeIR</a>.
                    In some cases, it describes DecodeIR's handling of the protocol rather than the protocol.
                    "I" in the text likely denotes John Fine, the original author of DecodeIR.
                </p>

                <xsl:apply-templates select="irp:protocols" mode="toc"/>
                <xsl:apply-templates select="irp:protocols"/>
                <h1>
                    <xsl:attribute name="id">Glossary</xsl:attribute>
                    <xsl:text>Glossary</xsl:text>
                </h1>
                <dl>
                    <dt>
                        <xsl:attribute name="id">DecodeIR</xsl:attribute>
                        <xsl:text>DecodeIR</xsl:text>
                    </dt>
                    <dd>Library for the decoding of
                        IrSequences. Originally written by John Fine, extended by others; used
                        by many widely spread programs as a shared library (IrScrutinizer, IrpMaster, IrScope, RemoteMaster), often using
                        Java Native Interface. The current version is 2.45. <a href="http://www.hifi-remote.com/wiki/index.php?title=DecodeIR">Current official documentation</a>.
                        License: public domain. <a
                            href="http://www.hifi-remote.com/forums/dload.php?action=file&amp;file_id=13104">Binaries for Windows, Linux, and Mac</a>,
                        <a
                            href="https://sourceforge.net/p/controlremote/code/HEAD/tree/trunk/decodeir/">source
                            code at SourceForge</a>.</dd>
                </dl>

                <dt id="Executor">Executor</dt>
                <dd>An embedded "program" for the rendering and transmission of one
                    or several protocols. One executor can manage several protocols; also,
                    for one protocol there may be several alternative executors. An executer has its own parametrization,
                    more-or-less similar to the parametrization of the protocol. Used in UEI Remotes
                    and RemoteMaster.
                </dd>
                <dl>
                    <dt>
                        <xsl:attribute name="id">repeat</xsl:attribute>
                        <xsl:text>repeat (ditto)</xsl:text>
                    </dt>
                    <dd>Simple sequence, without information, that is repeated as repeat sequence. For an example, see <a href="#NEC1">NEC1</a>.</dd>
                </dl>
                <dl>
                    <dt>
                        <xsl:attribute name="id">spurious</xsl:attribute>
                        <xsl:text>spurious decodes</xsl:text>
                    </dt>
                    <dd>An imperfectly measured signal that incorrectly matches a known protocol.</dd>
                </dl>

            </body>
        </html>
    </xsl:template>


    <xsl:template match="irp:protocols" mode="toc">
        <h2>Contents:</h2>
        <ul>
            <li>
                <a>
                    <xsl:attribute name="href">#Glossary</xsl:attribute>
                    <xsl:text>Glossary</xsl:text>
                </a>
            </li>

            <xsl:apply-templates select="irp:protocol" mode="toc">
                <xsl:sort select="@name"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="irp:protocols">
         <h2>Protocols</h2>
         <xsl:apply-templates select="irp:protocol"/>
    </xsl:template>

    <xsl:template match="irp:protocol" mode="toc">
        <li>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="@c-name"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </a>
        </li>
    </xsl:template>

    <xsl:template match="irp:protocol">
        <h2>
            <xsl:attribute name="id">
                <xsl:value-of select="@c-name"/>
            </xsl:attribute>
            <xsl:value-of select="@name"/>
            <xsl:text> (</xsl:text>
            <code>
            <xsl:value-of select="@c-name"/>
            </code>
            <xsl:text>)</xsl:text>
        </h2>
        <xsl:apply-templates select="irp:irp"/>
        <xsl:apply-templates select="irp:parameter"/>
        <xsl:apply-templates select="irp:documentation"/>
    </xsl:template>

    <xsl:template match="irp:irp">
        <!--xsl:text>IRP: </xsl:text-->
        <div>
            <xsl:attribute name="class">irp</xsl:attribute>
            <xsl:value-of select="."/>
        </div>
    </xsl:template>

    <xsl:template match="irp:documentation">
        <div>
            <xsl:attribute name="class">documentation</xsl:attribute>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template match="irp:parameter">
        <div>
            <xsl:attribute name="class">parameter</xsl:attribute>
            <span>
                <xsl:attribute name="class">parametername</xsl:attribute>
                <xsl:value-of select="@name"/>
                <xsl:text>: </xsl:text>
            </span>
            <xsl:value-of select="text()"/>
        </div>
    </xsl:template>
</xsl:stylesheet>
