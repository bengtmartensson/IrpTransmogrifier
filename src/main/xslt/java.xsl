<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<xsl:stylesheet
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xalan/java"
    version="2.0">
    <xsl:output method="text" />

    <!-- Parameters specific for the target -->
    <xsl:param name="frequencyType" select="'double'" />
    <xsl:param name="microsecondsType" select="'double'" />
    <xsl:param name="parameterType" select="'long'" />
    <xsl:param name="unsignedType" select="'int'" />

    <xsl:variable name="protocolName" select="replace(/protocol/@name,'[^_0-9A-Za-z]','')"/>
    <xsl:variable name="timeUnit" select="number(/protocol/implementation/generalspec/@timeunit)"/>
    <xsl:variable name="frequency" select="number(/protocol/implementation/generalspec/@frequency)"/>
    <xsl:variable name="dutycycle" select="/protocol/implementation/generalspec/@dutycycle"/>
    <xsl:variable name="bitdirection" select="/protocol/implementation/generalspec/@bitdirection"/>

    <xsl:template match="/protocol">
        <xsl:text>// NOTE: This code is intened to be put through a code beautifier, like indent.

// Blurbl...

            package org.harctoolbox.irpprotocoltest;

            import java.util.HashMap;

            public final class </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer extends IrpRenderer {
        </xsl:text>

        <xsl:apply-templates select="irp"/>
        <xsl:apply-templates select="implementation"/>

        <xsl:text>}
        </xsl:text>
    </xsl:template>


    <xsl:template match="irp">
        <xsl:text>public final static String irpString = "</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>";
        </xsl:text>
    </xsl:template>

    <xsl:template match="implementation">
        <xsl:apply-templates select="parameters/parameter" mode="define_fields"/>
        <xsl:apply-templates select="generalspec/@frequency" mode="define_getter"/>
        <xsl:apply-templates select="generalspec/@dutycycle" mode="define_getter"/>
        <xsl:if test="not(generalspec/@dutycycle)">
            <xsl:text>
                @Override
                public double getDutycycle() {
                return -1f;
                }
            </xsl:text>
        </xsl:if>


        <xsl:apply-templates select="bitspec_irstream/bitspec" mode="declare_funcs"/>
        <xsl:apply-templates select="bitspec_irstream/irstream/(intro|repeat|ending)[*]" mode="define_setup"/>
        <xsl:apply-templates select="parameters" mode="generate_standard_call"/>
        <xsl:apply-templates select="parameters[./parameter/default]" mode="generate_defaulted_call"/>
        <xsl:apply-templates select="parameters" mode="generate_map_call"/>

    </xsl:template>

    <xsl:template match="@frequency" mode="define_getter">
        <xsl:text>
            @Override
            public double getFrequency() {
            return </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>f;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="@dutycycle" mode="define_getter">
        <xsl:text>
            @Override
            public double getDutycycle() {
            return </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>f;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="intro|repeat|ending">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="intro|repeat|ending" mode="define_setup">
        <xsl:text>@Override
            protected </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList setup</xsl:text>
        <xsl:value-of select="upper-case(substring(name(.),1,1))"/>
        <xsl:value-of select="substring(name(.),2)"/>
        <xsl:text>() {
        </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList list = new </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList();
        </xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>return list;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="bitspec" mode="declare_funcs">
        <xsl:text>private static class </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList extends IrpRenderer.IrList {

            @Override
            void finiteBitField(</xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> data, </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> width) {
        </xsl:text>
        <xsl:text>for (</xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> i = 0; i &lt; width; i += </xsl:text>
        <xsl:value-of select="@chunksize"/>
        <xsl:text>) {
        </xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> mask = </xsl:text>
        <xsl:apply-templates select="@chunksize"/>
        <xsl:text> &lt;&lt; </xsl:text>
        <xsl:if test="$bitdirection='msb'">
            <xsl:text>width - 1 -</xsl:text>
        </xsl:if>
        <xsl:text>i*</xsl:text>
        <xsl:value-of select="@chunksize"/>
        <xsl:text>;
        </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> chunk = data &amp; mask;
        </xsl:text>
        <xsl:apply-templates select="."/>
        <xsl:text>}
            }
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="@chunksize[.='1']">
        <xsl:text>1</xsl:text>
    </xsl:template>

    <xsl:template match="@chunksize[.='2']">
        <xsl:text>3</xsl:text>
    </xsl:template>

    <xsl:template match="@chunksize[.='3']">
        <xsl:text>7</xsl:text>
    </xsl:template>


    <xsl:template match="bitspec[count(./*)=2]">
        <xsl:text>if (chunk == 0) {
</xsl:text>
        <xsl:apply-templates select="bare_irstream[1]"/>
        <xsl:text>} else {
</xsl:text>
        <xsl:apply-templates select="bare_irstream[2]"/>
        <xsl:text>}
</xsl:text>
    </xsl:template>

    <xsl:template match="parameters">
        <xsl:text>(</xsl:text>
    <xsl:apply-templates select="parameter"/>
    <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="parameters" mode="generate_standard_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(</xsl:text>
        <xsl:apply-templates select="parameter" mode="signature"/>
        <xsl:text>) {
            super();
        </xsl:text>
        <xsl:apply-templates select="parameter" mode="assign_fields"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameters" mode="generate_defaulted_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(</xsl:text>
        <xsl:apply-templates select="parameter[not(./default)]" mode="signature"/>
        <xsl:text>) {
            super();
        </xsl:text>
        <xsl:apply-templates select="parameter[not(@memory='true')]" mode="local_definition_use_defaults"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameters" mode="generate_map_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(HashMap &lt;String, Long&gt; parameters) {
            super();
        </xsl:text>
        <xsl:apply-templates select="parameter" mode="fields_assignment"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter" mode="local_definition_use_defaults">
        <xsl:text>this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter[./default]" mode="local_definition_use_defaults">
        <xsl:text>this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:apply-templates select="default/expression"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter" mode="fields_assignment">
        <xsl:text>this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>");
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter[./default and not(@memory='true')]" mode="fields_assignment">
        <xsl:text>this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = parameters.containsKey("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") ? parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") : (</xsl:text>
        <xsl:apply-templates select="./default/expression"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter[@memory='true']" mode="fields_assignment">
        <xsl:text>if (parameters.containsKey("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>"))
        this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>");
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter" mode="arg">
        <xsl:value-of select="@name"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameter" mode="define_fields">
        <xsl:text>private final </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter[@memory='true']" mode="define_fields">
        <xsl:text>private static </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:apply-templates select="default"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="parameter" mode="generate_parameter_this">
        <xsl:text>parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>")</xsl:text>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameter" mode="generate_call">
        <xsl:text></xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text></xsl:text>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameter" mode="signature">
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameter" mode="assign_fields">
        <xsl:text>this.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="bitspec_irstream">
        <xsl:text> {
        </xsl:text>
        <xsl:apply-templates select="irstream"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="irstream">
        <xsl:apply-templates select="intro[*]"/>
        <xsl:apply-templates select="repeat[*]"/>
        <xsl:apply-templates select="ending[*]"/>
    </xsl:template>

    <xsl:template match="intro|repeat|ending" mode="definition">
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer::do</xsl:text>
        <xsl:value-of select="name(.)"/>
        <xsl:text>() {
        </xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>};
        </xsl:text>
    </xsl:template>

    <xsl:template match="flash|gap|extent">
        <xsl:call-template name="duration">
            <xsl:with-param name="funcName" select="name(.)"/>
            <xsl:with-param name="multiplier">
                <xsl:apply-templates select="@unit"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@unit[.='1']">
        <xsl:text>*</xsl:text>
        <xsl:value-of select="$timeUnit"/>
    </xsl:template>

    <xsl:template match="@unit[.='m']">
        <xsl:text>*</xsl:text>
        <xsl:value-of select="1000"/>
    </xsl:template>

    <xsl:template match="@unit[.='u']">

    </xsl:template>

    <xsl:template match="@unit[.='p']">
        <xsl:text>*1000000/</xsl:text>
        <xsl:value-of select="$frequency"/>
    </xsl:template>

    <xsl:template match="extent[@unit='1']">
        <xsl:text>extent(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>*</xsl:text>
        <xsl:value-of select="$timeUnit"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template name="duration">
        <xsl:param name="funcName"/>
        <xsl:param name="multiplier"/>
        <xsl:param name="object"/>
        <xsl:value-of select="$object"/>
        <xsl:if test="../..[not(name(.)='bitspec')]">
            <xsl:text>list.</xsl:text>
        </xsl:if>
        <xsl:value-of select="$funcName"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:value-of select="$multiplier"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="assignment">
        <xsl:value-of select="name/@name"/>
        <xsl:text> = </xsl:text>
        <xsl:apply-templates select="expression"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="name_or_number">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="number_with_decimals">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="finite_bitfield">
        <xsl:text>list.finiteBitField(</xsl:text>
        <xsl:apply-templates select="data"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="chop"/>
        <xsl:if test="not(chop)">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@reverse"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="finite_bitfield[..[name(.)='expression']]">
        <xsl:text>finiteBitField(</xsl:text>
        <xsl:apply-templates select="data"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="chop"/>
        <xsl:if test="not(chop)">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@reverse"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="data">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="name">
        <xsl:value-of select="@name"/>
    </xsl:template>

    <xsl:template match="width|chop">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="number">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="expression">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="binary_operator">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="expression[1]"/>
        <xsl:text>)</xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="expression[2]"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

</xsl:stylesheet>