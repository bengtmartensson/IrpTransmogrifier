<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xalan/java"
    version="2.0">
    <xsl:output method="xml" />

    <!-- Default rule: just copy -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/">
        <xsl:comment>
            Parameters:
            intType: data type for integer counteract
            parameterType: data type for parameters in protocols
            voidType
            microsecondsType:
        </xsl:comment>
        <abstract-code>

            <!--xsl:value-of select="$protocolName"/-->
            <xsl:apply-templates select="NamedProtocols/NamedProtocol"/>
        </abstract-code>
    </xsl:template>

    <xsl:template match="NamedProtocol">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="Irp"/>
            <xsl:apply-templates select="Documentation"/>
            <xsl:apply-templates select="Protocol"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Irp|Documentation">
        <xsl:copy>
            <xsl:value-of select="."/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Protocol">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="GeneralSpec/@*"/>
            <xsl:attribute name="name" select="../@name"/>
            <xsl:apply-templates select="ParameterSpecs" mode="declare-statics"/>
            <instance-variables>
                <xsl:apply-templates select="." mode="declare-variables"/>
                <xsl:apply-templates select="Definitions/Definition" mode="declare-variables"/> <!-- ??? -->
            </instance-variables>
            <xsl:apply-templates select="BitspecIrstream/BitSpec" mode="declare_funcs"/>
            <xsl:apply-templates select="BitspecIrstream/(Intro|Repeat|Ending)" mode="define_setup"/>
            <!--xsl:apply-templates select="ParameterSpecs" mode="generate_assign_initial_value"/-->
            <xsl:apply-templates select="Definitions"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Intro|Repeat|Ending" mode="define_setup">
        <function>
            <xsl:copy-of select="@*"/>
            <!--xsl:attribute name="returnType" select="'void'"/-->
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="ancestor::Protocol/ParameterSpecs" mode="function-prototype"/>
            <body>
                <xsl:apply-templates select=".//Extent" mode="zero-sum-variable"/>
                <xsl:apply-templates select="*" mode="in-function"/>
            </body>
        </function>

    </xsl:template>

    <xsl:template match="Extent" mode="zero-sum-variable">
        <assignment name="sumOfDurations" value="0"/>
    </xsl:template>

    <xsl:template match="BitSpec" mode="declare_funcs">
        <xsl:call-template name="BitSpecLsb"/>
        <xsl:call-template name="BitSpecMsb"/>
        <xsl:call-template name="BitSpec5args"/>
    </xsl:template>

    <xsl:template name="BitSpec5args">
        <function>
            <xsl:attribute name="name" select="'finiteBitField'"/>
            <xsl:copy-of select="@*"/>
            <parameters>
                <parameter type="parameterType" name="data"/>
                <parameter type="parameterType" name="width"/>
                <parameter type="parameterType" name="chop"/>
                <parameter type="booleanType" name="complement"/>
                <parameter type="booleanType" name="reverse"/>
            </parameters>
            <body>
                <local-variable type="parameterType" name="d"/>
                <assignment name="d" value="(complement ? ~data : data) &gt;&gt; chop"/>
                <if>
                    <xsl:attribute name="condition">
                        <xsl:if test="ancestor::Protocol/GeneralSpec/@bitDirection='msb'">
                            <xsl:text>!</xsl:text>
                        </xsl:if>
                        <xsl:text>reverse</xsl:text>
                    </xsl:attribute>
                    <true-clause>
                        <funccall name="bitSpecMsb">
                            <param name="data">
                                <xsl:text>d</xsl:text>
                            </param>
                            <param name="width">
                                <xsl:text>width</xsl:text>
                            </param>
                        </funccall>
                    </true-clause>
                    <false-clause>
                        <funccall name="bitSpecLsb">
                            <param name="data">
                                <xsl:text>d</xsl:text>
                            </param>
                            <param name="width">
                                <xsl:text>width</xsl:text>
                            </param>
                        </funccall>
                    </false-clause>
                </if>
            </body>
        </function>
    </xsl:template>

    <xsl:template name="BitSpecLsb">
        <function>
            <xsl:attribute name="name" select="'bitSpecLsb'"/>

            <xsl:copy-of select="@*"/>
            <parameters>
                <parameter type="parameterType" name="data"/>
                <parameter type="parameterType" name="width"/>
            </parameters>
            <body>
                <local-variables>
                    <local-variable name="index" type="intType"/>
                    <local-variable name="bitmask" type="parameterType">
                        <xsl:attribute name="const" select="'true'"/>
                        <xsl:attribute name="value" select="@bitMask"/>
                    </local-variable>
                </local-variables>
                <for>
                    <initial>
                        <assignment name="index" value="0"/>
                    </initial>
                    <condition>
                        <comparision type="&lt;" left="index" right="width"/>
                    </condition>
                    <repeat>
                        <assignment name="value">
                            <xsl:attribute name="value">
                                <xsl:text>value + </xsl:text>
                                <xsl:value-of select="@chunkSize"/>
                            </xsl:attribute>
                        </assignment>
                    </repeat>
                    <body>
                        <switch variable="data &amp; bitMask">
                            <xsl:apply-templates select="BareIrStream" mode="in-bitspec"/>
                        </switch>
                    </body>
                </for>
            </body>
        </function>
    </xsl:template>

    <xsl:template name="BitSpecMsb">
         <function>
            <xsl:attribute name="name" select="'bitSpecMsb'"/>
            <xsl:copy-of select="@*"/>
            <parameters>
                <parameter type="parameterType" name="data"/>
                <parameter type="parameterType" name="width"/>
            </parameters>
            <body>
                <local-variables>
                    <local-variable name="index" type="intType"/>
                    <local-variable name="bitmask" type="parameterType">
                        <xsl:attribute name="const" select="'true'"/>
                        <xsl:attribute name="value" select="@bitMask"/>
                    </local-variable>
                </local-variables>
                <for>
                    <initial>
                        <assignment name="index" value="0"/>
                    </initial>
                    <condition>
                        <comparision type="&lt;" left="index" right="width"/>
                    </condition>
                    <repeat>
                        <assignment name="index" select="index">
                            <xsl:attribute name="value">
                                <xsl:text>index + </xsl:text>
                                <xsl:value-of select="@chunkSize"/>
                            </xsl:attribute>
                        </assignment>
                    </repeat>
                    <body>
                        <switch>
                            <xsl:attribute name="variable">
                                <xsl:text>(data >> (width - (index + </xsl:text>
                                <xsl:value-of select="@chunkSize"/>
                                <xsl:text>))) &amp; bitMask</xsl:text>
                            </xsl:attribute>
                            <xsl:apply-templates select="BareIrStream" mode="in-bitspec"/>
                        </switch>
                    </body>
                </for>
            </body>
        </function>
    </xsl:template>

    <xsl:template match="BareIrStream" mode="in-bitspec">
        <case>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="arg">
                <xsl:value-of select="position()-1"/>
            </xsl:attribute>
            <xsl:apply-templates select="*" mode="in-function"/>
        </case>
    </xsl:template>

    <xsl:template match="Definition" mode="declare-variables">
        <instance-variable>
            <xsl:attribute name="name" select="Name"/>
            <xsl:attribute name="type" select="'parameterType'"/>
        </instance-variable>
    </xsl:template>

    <xsl:template match="Protocol[.//Extent]" mode="declare-variables">
        <instance-variable name="sumOfDurations" value="0" type="microsecondsType"/>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="declare-statics">
        <static-variables>
            <xsl:apply-templates select="ParameterSpec[@memory='true']" mode="declare-statics"/>
        </static-variables>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="function-prototype">
        <parameters>
            <xsl:apply-templates select="ParameterSpec" mode="function-prototype"/>
        </parameters>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="declare-statics">
        <static-variable>
            <xsl:attribute name="name" select="@name"/>
            <xsl:attribute name="max" select="@max"/>
            <xsl:attribute name="min" select="@min"/>
            <xsl:attribute name="type" select="'parameterType'"/>
            <xsl:apply-templates select="Default/Expression/*"/>
        </static-variable>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="function-prototype">
        <parameter>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="Default"/>
        </parameter>
    </xsl:template>

    <xsl:template match="Flash[@unit='']|Gap[@unit='']|Extent[@unit='']" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="lower-case(name())"/>
            <param name="us">
                <xsl:value-of select="number(*) * number(ancestor::Protocol/GeneralSpec/@unit)"/>
            </param>
        </funccall>
    </xsl:template>

    <xsl:template match="Flash[@unit='m']|Gap[@unit='m']|Extent[@unit='m']" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="lower-case(name())"/>
            <param name="us">
                <xsl:value-of select="number(*) * 1000"/>
            </param>
        </funccall>
    </xsl:template>

    <!--xsl:template match="Flash|Gap|Extent" mode="in-function">
        <xsl:element name="{lower-case(name(.))}">
            <xsl:attribute name="time" select="."/>
            <xsl:attribute name="unit" select="@unit"/>
        </xsl:element>
    </xsl:template-->

    <xsl:template match="FiniteBitField" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="'finiteBitField'"/>
            <param name="data">
                <xsl:apply-templates select="Data"/>
            </param>
            <param name="width">
                <xsl:apply-templates select="Width"/>
            </param>
            <param name="chop">
                <xsl:apply-templates select="Chop"/>
            </param>
            <param name="complement">
                <xsl:value-of select="@complement"/>
            </param>
            <param name="reverse">
                <xsl:value-of select="@reverse"/>
            </param>
        </funccall>
    </xsl:template>

    <!-- BitField in numeric context -->
    <xsl:template match="FiniteBitField[..[name(.)='Expression']]">
        <xsl:text>finiteBitField(</xsl:text>
        <xsl:apply-templates select="Data"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Chop"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@reverse"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="Data|Width|Chop">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Expression">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="BinaryOperator">
        <xsl:apply-templates select="*[1]"/>
        <xsl:value-of select="@type"/>
        <xsl:apply-templates select="*[2]"/>
    </xsl:template>

    <xsl:template match="Number">
        <xsl:apply-templates select="text()"/>
    </xsl:template>

    <xsl:template match="Name[ancestor::Protocol/Definitions/Definition/Name=current()]">
        <xsl:apply-templates select="ancestor::Protocol/Definitions/Definition[Name=current()]/Expression/*"/>
    </xsl:template>

    <xsl:template match="Name[ancestor::Protocol/ParameterSpecs/ParameterSpec/@name=current()]">
        <xsl:apply-templates select="text()"/>
    </xsl:template>

    <xsl:template match="UnaryOperator[@type='#']">
        <xsl:text>bitCount(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="Definition">
        <definition>
            <xsl:attribute name="name" select="Name"/>
            <xsl:apply-templates select="Expression/*"/>
        </definition>
    </xsl:template>

    <xsl:template match="Assignment" mode="in-function">
        <xsl:copy>
            <xsl:attribute name="name" select="Name"/>
            <xsl:apply-templates select="Expression/*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>