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

    <xsl:variable name="protocolName" select="replace(//NamedProtocol/@name,'[^_0-9A-Za-z]','')"/>
    <xsl:variable name="unit"          select="number(//NamedProtocol/Protocol/GeneralSpec/@unit)"/>
    <xsl:variable name="frequency"     select="number(//NamedProtocol/Protocol/GeneralSpec/@frequency)"/>
    <xsl:variable name="dutycycle"            select="//NamedProtocol/Protocol/GeneralSpec/@dutycycle"/>
    <xsl:variable name="bitdirection"         select="//NamedProtocol/Protocol/GeneralSpec/@bitDirection"/>

    <xsl:template match="/NamedProtocol">
        <xsl:text>// NOTE: This code is intended to be put through a code beautifier, like indent.

            // Blurbl...

            package org.harctoolbox.irpprotocoltest;

            import java.util.HashMap;

            public final class </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer extends IrpRenderer {
        </xsl:text>

        <xsl:apply-templates select="Irp"/>
        <xsl:apply-templates select="Protocol"/>

        <xsl:text>}
        </xsl:text>
    </xsl:template>


    <xsl:template match="Irp">
        <xsl:text>public final static String irpString = "</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>";
        </xsl:text>
    </xsl:template>

    <xsl:template match="Protocol">
        <xsl:apply-templates select="ParameterSpecs/ParameterSpec" mode="define_fields"/>
        <xsl:apply-templates select=".//Assignment" mode="define_fields"/>
        <xsl:apply-templates select="GeneralSpec/@frequency" mode="define_getter"/>
        <xsl:apply-templates select="GeneralSpec/@dutycycle" mode="define_getter"/>
        <xsl:if test="not(GeneralSpec/@dutycycle)">
            <xsl:text>
                @Override
                public double getDutycycle() {
                return -1f;
                }
            </xsl:text>
        </xsl:if>


        <xsl:apply-templates select="BitspecIrstream/BitSpec" mode="declare_funcs"/>
        <xsl:apply-templates select="BitspecIrstream/IrStream/(Intro|Repeat|Ending)[*]" mode="define_setup"/>
        <xsl:apply-templates select="ParameterSpecs" mode="generate_assign_initial_value"/>
        <xsl:apply-templates select="Definitions"/>
        <xsl:apply-templates select="ParameterSpecs" mode="generate_standard_call"/>
        <xsl:apply-templates select="ParameterSpecs[./ParameterSpec/Default]" mode="generate_defaulted_call"/>
        <xsl:apply-templates select="ParameterSpecs" mode="generate_map_call"/>

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

    <xsl:template match="@dutyCycle" mode="define_getter">
        <xsl:text>
            @Override
            public double getDutycycle() {
            return </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>f;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="Intro|Repeat|Ending">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Ending" mode="define_setup">
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

    <xsl:template match="Repeat" mode="define_setup">
        <xsl:text>@Override
            protected </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList setupRepeat() {
            </xsl:text>
            <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList list = new </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList();
            bodyRepeat(list);
            return list;
            }

            private void bodyRepeat(</xsl:text>
            <xsl:value-of select="$protocolName"/>
    <xsl:text>IrList list) {
        </xsl:text>

        <xsl:apply-templates select="*"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="Intro" mode="define_setup">
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

        <xsl:if test="../repeat//repeat_marker/@min">
            <xsl:text>for (</xsl:text>
            <xsl:value-of select="$unsignedType"/>
            <xsl:text> repeatCounter = 0; repeatCounter &lt; </xsl:text>
            <xsl:value-of select="../repeat//repeat_marker/@min"/>
            <xsl:text>; repeatCounter++ )
                bodyRepeat(list);
            </xsl:text>
        </xsl:if>

        <xsl:text>return list;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="BitSpec" mode="declare_funcs">
        <xsl:text>private static class </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>IrList extends IrpRenderer.IrList {
          private final static </xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> mask = </xsl:text>
        <xsl:value-of select="count(bare_irstream)-1"/> <!-- almost right -->
        <xsl:text>;
        </xsl:text>
        <xsl:if test="number(@chunkSize) &gt; 1">
            <xsl:text>private </xsl:text>
            <xsl:value-of select="$parameterType"/>
            <xsl:text> pendingData = 0;
                private </xsl:text>
            <xsl:value-of select="$parameterType"/>
            <xsl:text> pendingBits = 0;
            </xsl:text>
        </xsl:if>
        <xsl:text>
            @Override
            void finiteBitField(</xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> data, </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> width) {
        </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> realData = data;
        </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> realWidth = width;
        </xsl:text>
        <xsl:if test="number(@chunkSize) &gt; 1">
            <xsl:text>
            if (pendingBits &gt; 0) {
                realData |= pendingData &lt;&lt; width;
                realWidth += pendingBits;
                pendingBits = 0;
            }
            if (realWidth % 2 != 0) {
                pendingData = realData;
                pendingBits = width;
                realWidth = 0;
            }
            </xsl:text>
        </xsl:if>
        <xsl:text>for (</xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> i = 0; i &lt; realWidth; i += </xsl:text>
        <xsl:value-of select="@chunkSize"/>
        <xsl:text>) {
        </xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> shift = </xsl:text>
        <xsl:if test="$bitdirection='msb'">
            <xsl:text>(</xsl:text>
            <xsl:value-of select="$unsignedType"/>
            <xsl:text>) realWidth - </xsl:text>
            <xsl:value-of select="@chunkSize"/>
            <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:text>i;
        </xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text> chunk = (((</xsl:text>
        <xsl:value-of select="$unsignedType"/>
        <xsl:text>)realData) >> shift) &amp; mask;
        </xsl:text>
        <xsl:apply-templates select="."/>
        <xsl:text>}
            }
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="@chunkSize[.='1']">
        <xsl:text>1</xsl:text>
    </xsl:template>

    <xsl:template match="@chunkSize[.='2']">
        <xsl:text>3</xsl:text>
    </xsl:template>

    <xsl:template match="@chunkSize[.='3']">
        <xsl:text>7</xsl:text>
    </xsl:template>


    <xsl:template match="BitSpec">
        <xsl:text>switch (chunk) {
        </xsl:text>
        <xsl:apply-templates select="./BareIrStream" mode="case"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="BitSpec[count(./*)=2]">
        <xsl:text>if (chunk == 0) {
        </xsl:text>
        <xsl:apply-templates select="BareIrStream[1]"/>
        <xsl:text>} else {
        </xsl:text>
        <xsl:apply-templates select="BareIrStream[2]"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="BareIrStream" mode="case">
        <xsl:text>case </xsl:text>
        <xsl:value-of select="position()-1"/>
        <xsl:text>:
        </xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>break;
        </xsl:text>
    </xsl:template>

    <xsl:template match="Definitions">
        <xsl:apply-templates select="Definition"/>
    </xsl:template>

    <xsl:template match="Definition">
        <xsl:text>private </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="Name"/>
        <xsl:text>() {
            return </xsl:text>
        <xsl:apply-templates select="Expression"/>
        <xsl:text>;
            }
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpecs">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="ParameterSpec"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="generate_assign_initial_value">
        <xsl:text>@Override
        protected void assignInitialValues() {
        </xsl:text>
        <xsl:apply-templates select="ParameterSpec" mode="generate_assign_initial_value"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="generate_standard_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(</xsl:text>
        <xsl:apply-templates select="ParameterSpec" mode="signature"/>
        <xsl:text>) {
            super();
        </xsl:text>
        <xsl:apply-templates select="ParameterSpec" mode="assign_fields"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="generate_defaulted_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(</xsl:text>
        <xsl:apply-templates select="ParameterSpec[not(./Default)]" mode="signature"/>
        <xsl:text>) {
            super();
        </xsl:text>
        <xsl:apply-templates select="ParameterSpec" mode="local_definition_use_defaults"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpecs" mode="generate_map_call">
        <xsl:text>public </xsl:text>
        <xsl:value-of select="$protocolName"/>
        <xsl:text>Renderer(HashMap &lt;String, Long&gt; ParameterSpecs) {
            super();
        </xsl:text>
        <xsl:apply-templates select="ParameterSpec" mode="fields_assignment"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="generate_assign_initial_value">
        <xsl:text>if (</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init &gt;= 0)
        </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[not(Default)]" mode="generate_assign_initial_value">
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[@memory='true']" mode="generate_assign_initial_value">
        <xsl:text>if (</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init &gt;= 0)
        </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[not(@memory='true') and Default]" mode="generate_assign_initial_value">
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init &gt;= 0 ? </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init : (</xsl:text>
        <xsl:apply-templates select="Default"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="local_definition_use_defaults">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[not(@memory='true') and ./Default]" mode="local_definition_use_defaults">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = -1;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[@memory='true']" mode="local_definition_use_defaults">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = -1L;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="fields_assignment">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>");
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[./Default and not(@memory='true')]" mode="fields_assignment">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = parameters.containsKey("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") ? parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") : -1;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec[@memory='true']" mode="fields_assignment">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = parameters.containsKey("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") ? parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>") : -1;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="arg">
        <xsl:value-of select="@name"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="define_fields">
        <xsl:text>private </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init;
        </xsl:text>
        <xsl:text>private </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="Assignment|Definition" mode="define_fields">
        <xsl:if test="not(ancestor::Protocol/ParameterSpecs/ParameterSpec[@name=current()/name/.])">
            <xsl:text>private </xsl:text>
            <xsl:value-of select="$parameterType"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="name/."/>
            <xsl:text>;
            </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ParameterSpec[@memory='true']" mode="define_fields">
        <xsl:text>private static </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> = </xsl:text>
        <xsl:apply-templates select="default"/>
        <xsl:text>;
        </xsl:text>
        <xsl:text>private </xsl:text>
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>_init;
        </xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="generate_parameter_this">
        <xsl:text>parameters.get("</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>")</xsl:text>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="generate_call">
        <xsl:text></xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text></xsl:text>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="signature">
        <xsl:value-of select="$parameterType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="assign_fields">
        <xsl:value-of select="@name"/>
        <xsl:text>_init = </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="BitSpecIrStream">
        <xsl:text> {
        </xsl:text>
        <xsl:apply-templates select="IrStream"/>
        <xsl:text>}
        </xsl:text>
    </xsl:template>

    <xsl:template match="IrStream">
        <xsl:apply-templates select="Intro[*]"/>
        <xsl:apply-templates select="Repeat[*]"/>
        <xsl:apply-templates select="Ending[*]"/>
    </xsl:template>

    <xsl:template match="Flash|Gap|Extent">
        <xsl:call-template name="Duration">
            <xsl:with-param name="funcName" select="lower-case(name(.))"/>
            <xsl:with-param name="multiplier">
                <xsl:apply-templates select="@unit"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@unit[.='1' or .='']">
        <xsl:text>*</xsl:text>
        <xsl:value-of select="$unit"/>
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

    <xsl:template match="Extent[@unit='1']">
        <xsl:text>extent(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>*</xsl:text>
        <xsl:value-of select="$unit"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template name="Duration">
        <xsl:param name="funcName"/>
        <xsl:param name="multiplier"/>
        <xsl:param name="object"/>
        <xsl:value-of select="$object"/>
        <xsl:if test="../..[not(name(.)='BitSpec')]">
            <xsl:text>list.</xsl:text>
        </xsl:if>
        <xsl:value-of select="$funcName"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:value-of select="$multiplier"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="Assignment">
        <xsl:value-of select="name"/>
        <xsl:text> = </xsl:text>
        <xsl:apply-templates select="Expression"/>
        <xsl:text>;
        </xsl:text>
    </xsl:template>

    <xsl:template match="NameOrNumber">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="NumberWithDecimals">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="FiniteBitField">
        <xsl:text>list.finiteBitField(</xsl:text>
        <xsl:apply-templates select="Data"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Chop"/>
        <xsl:if test="not(Chop)">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@reverse"/>
        <xsl:text>);
        </xsl:text>
    </xsl:template>

    <xsl:template match="FiniteBitField[..[name(.)='Expression']]">
        <xsl:text>finiteBitField(</xsl:text>
        <xsl:apply-templates select="Data"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Chop"/>
        <xsl:if test="not(Chop)">
            <xsl:text>0</xsl:text>
        </xsl:if>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@reverse"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="Data">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Name">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="Name[ancestor::implementation/ParameterSpecs/ParameterSpec/@name=current()/@name]|name[ancestor::Protocol//Assignment/name/@name=current()/@name]">
        <xsl:value-of select="@name"/>
    </xsl:template>

    <xsl:template match="Width|Chop">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Number">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="Expression">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="BinaryOperator">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="Expression[1]"/>
        <xsl:text>)</xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="Expression[2]"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="UnaryOperator">
        <xsl:text>(</xsl:text>
        <xsl:value-of select="@type"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="Expression"/>
        <xsl:text>))</xsl:text>
    </xsl:template>

    <xsl:template match="UnaryOperator[@type='#']">
        <xsl:text>Long.bitCount(</xsl:text>
        <xsl:apply-templates select="Expression"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

</xsl:stylesheet>