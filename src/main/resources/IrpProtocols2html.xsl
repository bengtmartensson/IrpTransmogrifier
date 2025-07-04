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
                    div.protocol {
                    padding-top: 1ex;
                    }
                    div.protocol-title {
                    font-size: 120%;
                    font-weight: bold;

                    }
                    div.alias {
                    padding-top: 1ex;
                    }
                    div.alias-title {
                    font-size: 120%;
                    font-weight: bold;

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
                <!--p>
                    <strong>Note: </strong>
                    The descriptions of the protocols are mostly taken from <a href="#DecodeIR">DecodeIR</a>.
                    In some cases, it describes DecodeIR's handling of the protocol rather than the protocol.
                    "I" in the text likely denotes John Fine, the original author of DecodeIR.
                </p-->

                <xsl:apply-templates select="irp:protocols" mode="toc"/>
                <xsl:apply-templates select="irp:protocols"/>
                <h2>
                    <xsl:attribute name="id">Aliases</xsl:attribute>
                    <xsl:text>Aliases (alternative names)</xsl:text>
                </h2>
                <xsl:text>
                <!-- Better to have them sorted all together. -->
                </xsl:text>
                <xsl:apply-templates select="irp:protocols/irp:protocol/irp:parameter[@name='alt_name']"/>
                <h2>
                    <xsl:attribute name="id">Glossary</xsl:attribute>
                    <xsl:text>Glossary</xsl:text>
                </h2>
                <dl>
                    <dt>
                        <xsl:attribute name="id">CRC</xsl:attribute>
                        <xsl:text>Cyclic Redundancy Check (CRC)</xsl:text>
                    </dt>
                    <dd>
                        An elaborate form of checksum, see <a href="https://en.wikipedia.org/wiki/Cyclic_redundancy_check">Wikipedia</a>.
                    </dd>
                    <dt>
                        <xsl:attribute name="id">DecodeIR</xsl:attribute>
                        <xsl:text>DecodeIR</xsl:text>
                    </dt>
                    <dd>Library for the decoding of
                        IrSequences. Originally written by John Fine, extended by others; used
                        by many widely spread programs as a shared library (IrScrutinizer version 1, IrpMaster, IrScope, RemoteMaster).
                        Written in C++ with Java Native Interface. The current (and most likely final) version is 2.45, released in January 2015.
                        <a href="http://www.hifi-remote.com/wiki/index.php?title=DecodeIR">Current official documentation</a>.
                        License: public domain. <a
                            href="http://www.hifi-remote.com/forums/dload.php?action=file&amp;file_id=13104">Binaries for Windows, Linux, and Mac</a>,
                        <a
                            href="https://sourceforge.net/p/controlremote/code/HEAD/tree/trunk/decodeir/">source
                            code</a>.
                        <a href="https://github.com/bengtmartensson/Arduino-DecodeIR">Arduino port</a>.
                    </dd>

                    <dt>
                        <xsl:attribute name="id">ditto</xsl:attribute>
                        <xsl:text>ditto</xsl:text>
                    </dt>
                    <dd>Simple sequence, without payload information, that is repeated as a <a href="#repeat">repeat sequence</a>'s.
                        For an example, see <a href="#NEC1">NEC1</a>.
                    </dd>

                    <dt>
                        <xsl:attribute name="id">ending</xsl:attribute>
                        <xsl:text>ending sequence</xsl:text>
                    </dt>
                    <dd>
                        IR sequence that is sent exactly once when a button is released, after the <a href="#repeat">repeats</a> have ended.
                        Only present if a few protocols, like <a href="#NRC17">NRC17</a> and <a href="#OrtekMCE">OrtekMCE</a>.
                    </dd>

                    <dt id="Executor">executor</dt>
                    <dd>An embedded "program" for the rendering and transmission of one
                        or several protocols on an embedded processor. One executor can manage several protocols; also,
                        for one protocol there may be several alternative executors. An executer has its own parameterization,
                        more-or-less similar to the parameterization of the protocol.
                        Used in UEI Remotes and RemoteMaster.
                    </dd>

                    <dt>
                        <xsl:attribute name="id">intro</xsl:attribute>
                        <xsl:text>intro sequence</xsl:text>
                    </dt>
                    <dd>
                    IR sequence that is sent exactly once when a button is held pressed.
                        (If more is sent if the button is held, then it is the <a href="#repeat">repeat-</a> and <a href="#ending">ending</a> sequence.)
                    </dd>

                    <dt>
                        <xsl:attribute name="id">relaxed</xsl:attribute>
                        <xsl:text>relaxed protocol</xsl:text>
                    </dt>
                    <dd>
                        A relaxed protocol (in general recognized by the suffix <code>_relaxed</code> in the name) is a protocol,
                        derived from another protocol, where the recognition is in some way "relaxed", for example by some checks being left out.
                        For example, a checksum may be just reported by its value, instead of being checked
                        (possibly leading to the decode being considered as invalid).
                        This can be useful for identifying imperfectly learned, or otherwise flawed IR signals.
                        It should normally be marked <code>decode-only</code>, since rendering is normally meaningless.
                    </dd>

                    <dt>
                        <xsl:attribute name="id">repeat</xsl:attribute>
                        <xsl:text>repeat sequence</xsl:text>
                    </dt>
                    <dd>
                        IR sequence that is sent repeatedly when a button is held down, after the <a href="#intro">intro sequence</a> has been sent.
                    </dd>
                    <dt>
                        <xsl:attribute name="id">spurious</xsl:attribute>
                        <xsl:text>spurious decodes</xsl:text>
                    </dt>
                    <dd>An imperfectly measured signal that incorrectly matches a protocol (false positive).</dd>
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
            <h3>Protocols</h3>
            <xsl:apply-templates select="irp:protocol" mode="toc">
                <xsl:sort select="@name"/>
            </xsl:apply-templates>
            <h3>Aliases</h3>
            <xsl:apply-templates select="irp:protocol/irp:parameter[@name='alt_name']" mode="aliastoc">
                <xsl:sort select="text()"/>
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
                    <xsl:value-of select="@name"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </a>
        </li>
    </xsl:template>

    <xsl:template match="irp:parameter[@name='alt_name']" mode="aliastoc">
        <li>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="../@name"/>
                </xsl:attribute>
                <xsl:value-of select="text()"/>
            </a>
        </li>
    </xsl:template>

    <xsl:template match="irp:protocol">
        <div>
            <xsl:attribute name="class">protocol</xsl:attribute>
            <xsl:attribute name="id">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <div>
                <xsl:attribute name="class">protocol-title</xsl:attribute>
                <xsl:value-of select="@name"/>
                <xsl:apply-templates select="@c-name"/>
            </div>
            <xsl:apply-templates select="irp:irp"/>
            <xsl:apply-templates select="irp:parameter" mode="protocol"/>
            <xsl:apply-templates select="irp:documentation"/>
        </div>
    </xsl:template>

    <xsl:template match="@c-name">
        <xsl:text> (</xsl:text>
        <code>
            <xsl:value-of select="."/>
        </code>
        <xsl:text>)</xsl:text>
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

    <xsl:template match="irp:parameter" mode="protocol">
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

    <xsl:template match="irp:parameter[@name='alt_name']">
        <div>
            <xsl:attribute name="class">alias</xsl:attribute>
            <xsl:attribute name="id">
                <xsl:value-of select="text()"/>
            </xsl:attribute>
            <div>
                <xsl:attribute name="class">alias-title</xsl:attribute>
                <xsl:value-of select="text()"/>
            </div>

            <xsl:text>Alternative name for </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:text>#</xsl:text>
                    <xsl:value-of select="../@name"/>
                </xsl:attribute>
                <xsl:value-of select="../@name"/>
            </a>
            <xsl:text>.</xsl:text>
        </div>
    </xsl:template>
</xsl:stylesheet>
