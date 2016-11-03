<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<xsl:stylesheet
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xalan/java"
    xmlns:math="http://exslt.org/math"
    extension-element-prefixes="math"
    version="2.0">
    <xsl:output method="text" />

    <!--xsl:param name="frequencyType" select="'Xunsigned int'"/>
    <xsl:param name="microsecondsType" select="'Xunsigned int'"/>
    <xsl:param name="parameterType" select="'Xunsigned int'"/>
    <xsl:param name="returnType" select="'Xvoid'"/>
    <xsl:param name="constAttribute" select="'cXonst'"/>
    <xsl:param name="localFunctionAttribute" select="'Xstatic'"/-->

    <xsl:param name="frequencyType" select="'XfrequencyType'"/>
    <xsl:param name="microsecondsType" select="'XmicrosecondsType'"/>
    <xsl:param name="parameterType" select="'XparameterType'"/>
    <xsl:param name="returnType" select="'XreturnType'"/>
    <xsl:param name="constAttribute" select="'XconstAttribute'"/>
    <xsl:param name="localFunctionAttribute" select="'XlocalFunctionAttribute'"/>
    <xsl:param name="staticAttribute" select="'XstaticAttribute'"/>
    <xsl:param name="instanceVariableAttribute" select="'XinstanceVariableAttribute'"/>
    <xsl:param name="intType" select="'XintType'"/>

    <!-- default template for elements is to just ignore -->
    <xsl:template match="*"/>

    <xsl:template match="function">
        <xsl:value-of select="$localFunctionAttribute"/>
        <xsl:text> void </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:apply-templates select="parameters"/>
        <xsl:apply-templates select="body"/>
        <xsl:text xml:space="preserve">&#10;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="body">
        <xsl:text> {&#10;</xsl:text>
        <xsl:apply-templates select="*" mode="statement"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="if">
        <xsl:text>if (</xsl:text>
        <xsl:value-of select="@condition"/>
        <xsl:text>) {&#10;</xsl:text>
        <xsl:apply-templates select="true-clause/*" mode="statement"/>
        <xsl:text>} else {&#10;</xsl:text>
        <xsl:apply-templates select="false-clause/*"  mode="statement"/>
        <xsl:text>}&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="if" mode="statement">
        <xsl:apply-templates select="."/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="for">
        <xsl:text>for ( </xsl:text>
        <xsl:apply-templates select="initial/*"/>
        <xsl:text>; </xsl:text>
        <xsl:apply-templates select="condition/*"/>
        <xsl:text>; </xsl:text>
        <xsl:apply-templates select="repeat/*"/>
        <xsl:text>) {&#10;</xsl:text>
        <xsl:apply-templates select="body/*" mode="statement"/>
        <xsl:text>}</xsl:text>
    </xsl:template>

    <xsl:template match="for" mode="statement">
        <xsl:apply-templates select="."/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="funccall">
        <xsl:value-of select="@name"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="param"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="funccall" mode="statement">
        <xsl:apply-templates select="."/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="param">
        <xsl:value-of select="."/>
        <xsl:if test="position() &lt; last()">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="local-variables">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="comparision">
        <xsl:value-of select="@left"/>
        <xsl:value-of select="@type"/>
        <xsl:value-of select="@right"/>
    </xsl:template>

    <xsl:template match="switch">
        <xsl:text>switch (</xsl:text>
        <xsl:apply-templates select="@variable"/>
        <xsl:text>) {&#10;</xsl:text>
        <xsl:apply-templates select="case"/>
        <xsl:text>}&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="case">
        <xsl:text>case </xsl:text>
        <xsl:value-of select="@arg"/>
        <xsl:text>:&#10;</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>break;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="increment">
        <xsl:value-of select="@name"/>
        <xsl:text> += </xsl:text>
        <xsl:value-of select="@value"/>
    </xsl:template>

    <xsl:template match="local-variable">
        <xsl:apply-templates select="@type"/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="@const"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:apply-templates select="@value"/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="@type[.='intType']">
        <xsl:value-of select="$intType"/>
    </xsl:template>

    <xsl:template match="@type[.='parameterType']">
        <xsl:value-of select="$parameterType"/>
    </xsl:template>

    <xsl:template match="@type[.='microsecondsType']">
        <xsl:value-of select="$microsecondsType"/>
    </xsl:template>

    <xsl:template match="@value">
        <xsl:text> = </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="@const[.='true']">
        <xsl:value-of select="$constAttribute"/>
    </xsl:template>

    <xsl:template match="/">
        <xsl:text>// This file was automatically generated by IrpTransmogrifier

typedef </xsl:text>
<xsl:value-of select="$parameterType"/>
<xsl:text> parameterType;&#10;typedef </xsl:text>
<xsl:value-of select="$microsecondsType"/>
<xsl:text> microsecondsType;&#10;&#10;</xsl:text>

        <xsl:apply-templates select=".//NamedProtocol"/>
    </xsl:template>

    <xsl:template match="NamedProtocol">
        <xsl:text>// Protocol: </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text xml:space="preserve">&#10;</xsl:text>
        <xsl:apply-templates select="Irp"/>
        <xsl:apply-templates select="Protocol"/>
    </xsl:template>

    <xsl:template match="Protocol">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="static-variables">
        <xsl:text>// Static variables&#10;</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="instance-variables">
        <xsl:text>// Instance variables&#10;</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <!--xsl:template match="BareIrStream" mode="function-def">
        <xsl:text>case </xsl:text>
        <xsl:value-of select="@arg"/>
        <xsl:text>:&#10;</xsl:text>
        <xsl:apply-templates select="*" mode="send"/>
        <xsl:text>break;&#10;</xsl:text>
    </xsl:template-->

    <xsl:template match="Irp">
        <xsl:text>// This code is a translation of the following IRP form:&#10;// </xsl:text>
        <xsl:value-of select="text()"/>
        <xsl:text>&#10;&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="instance-variable">
        <xsl:value-of select="$instanceVariableAttribute"/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="@type"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@value"/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>

    <!--xsl:template match="Protocol[@standardPwm='true']" mode="send">
        <xsl:apply-templates select="function[@name='Intro']"/>
    </xsl:template-->

    <!--xsl:template match="function[@name='Intro']">
        <xsl:value-of select="$returnType"/>
        <xsl:text> IRsend::send</xsl:text>
        <xsl:value-of select="upper-case(../@name)"/>
        <xsl:apply-templates select="parameters" mode="prototypeWithoutDefaults"/>
        <xsl:text> {&#10;</xsl:text>
        <xsl:apply-templates select="../@frequency" mode="enableIROut"/>
        <xsl:apply-templates select="body/*" mode="send"/>
        <xsl:text>}</xsl:text>
    </xsl:template-->

    <xsl:template match="funccall[@name='finiteBitField']" mode="send">
        <xsl:text>bitField</xsl:text>
        <xsl:if test="@reverse='true'">
            <xsl:text>Reverse</xsl:text>
        </xsl:if>
        <xsl:text>(</xsl:text>
        <xsl:if test="@complement='true'">
            <xsl:text>~</xsl:text>
        </xsl:if>
        <xsl:value-of select="@data"/>
        <xsl:text> &gt;&gt; </xsl:text>
        <xsl:value-of select="@chop"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@width"/>
        <xsl:text>);&#10;</xsl:text>
    </xsl:template>


    <xsl:template match="assignment">
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@value"/>
        <!--xsl:text>;&#10;</xsl:text-->
    </xsl:template>

    <xsl:template match="assignment" mode="statement">
        <xsl:apply-templates select="."/>
        <xsl:text>;&#10;</xsl:text>
    </xsl:template>


    <xsl:template match="BitspecIrstream">
        <xsl:apply-templates select="Intro[*]"/>
        <xsl:if test="not(Intro[*])">
            <xsl:apply-templates select="Repeat"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Intro|Repeat">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="@frequency">
        <xsl:text>setFrequency(</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>);&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="parameters" mode="prototypeWithoutDefaults">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="*" mode="prototypeWithoutDefaults"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="parameter" mode="prototypeWithoutDefaults">
        <xsl:value-of select="$parameterType"/>
        <xsl:text xml:space="preserve"> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameters">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="parameter"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="parameter">
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Number">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="NameOrNumber">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="NumberWithDecimals">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

</xsl:stylesheet>