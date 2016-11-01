<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:java="http://xml.apache.org/xalan/java"
    version="2.0">
    <xsl:output method="xml" />

    <!-- Default: just copy -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/">
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
            <xsl:attribute name="returnType" select="'void'"/>
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="ancestor::Protocol/ParameterSpecs" mode="function-prototype"/>
            <body>
                <xsl:apply-templates select=".//Extent" mode="zero-sum-variable"/>
                <xsl:apply-templates select="*" mode="in-function"/>
            </body>
        </function>

    </xsl:template>

    <xsl:template match="Extent" mode="zero-sum-variable">
        <assignment name="'sumOfDurations'" value="0"/>
    </xsl:template>

    <xsl:template match="BitSpec" mode="declare_funcs">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
                <xsl:apply-templates select="BareIrStream" mode="in-bitspec"/>
        </xsl:copy>
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

    <xsl:template match="BareIrStream" mode="in-bitspec">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="arg">
                <xsl:value-of select="position()-1"/>
            </xsl:attribute>
            <xsl:apply-templates select="*" mode="in-function"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Definition" mode="declare-variables">
        <instance-variable>
            <xsl:attribute name="name" select="Name"/>
            <xsl:attribute name="type" select="'parameterType'"/>
        </instance-variable>
    </xsl:template>

    <xsl:template match="Protocol[.//Extent]" mode="declare-variables">
        <instance-variable name="sumOfDurations" value="0" type="'microsecondsType'"/>
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

    <xsl:template match="Default">
        <xsl:copy>
            <xsl:apply-templates select="*"/>
        </xsl:copy>
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

    <xsl:template name="multiply">
        <xsl:param name="x"/>
        <xsl:param name="y"/>
        <xsl:value-of select="round(number($x)*number($y))"/>
    </xsl:template>

    <xsl:template match="Flash[@unit='']|Gap[@unit='']|Extent[@unit='']" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="name()"/>
            <xsl:attribute name="us" select="number(*) * number(ancestor::Protocol/GeneralSpec/@unit)"/>
        </funccall>
    </xsl:template>

    <xsl:template match="Flash[@unit='m']|Gap[@unit='m']|Extent[@unit='m']" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="name()"/>
            <xsl:attribute name="us">
                <xsl:value-of select="number(*) * 1000"/>
            </xsl:attribute>
        </funccall>
    </xsl:template>

    <xsl:template match="Flash|Gap|Extent" mode="in-function">
        <xsl:element name="{lower-case(name(.))}">
            <xsl:attribute name="time" select="."/>
            <xsl:attribute name="unit" select="@unit"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@unit[.='1' or .='']">
        <xsl:text>*</xsl:text>
        <xsl:value-of select="ancestor::Protocol/GeneralSpec/@unit"/>
    </xsl:template>

    <xsl:template match="@unit[.='m']">
        <xsl:text>*</xsl:text>
        <xsl:value-of select="1000"/>
    </xsl:template>

    <xsl:template match="@unit[.='u']">

    </xsl:template>

    <xsl:template match="@unit[.='p']">
        <xsl:text>*1000000/</xsl:text>
        <xsl:value-of select="ancestor::Protocol/GeneralSpec/@frequency"/>
    </xsl:template>

    <xsl:template match="Extent[@unit='1']">
        <xsl:text>extent(</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>*</xsl:text>
        <xsl:value-of select="ancestor::Protocol/GeneralSpec/@unit"/>
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

    <!--xsl:template match="NameOrNumber">
        <xsl:apply-templates select="*"/>
    </xsl:template-->

    <!--xsl:template match="NumberWithDecimals">
        <xsl:value-of select="."/>
    </xsl:template-->

    <xsl:template match="FiniteBitField" mode="in-function">
        <funccall>
            <xsl:attribute name="name" select="'finiteBitField'"/>
            <xsl:attribute name="complement">
                <xsl:value-of select="@complement"/>
            </xsl:attribute>
            <xsl:attribute name="reverse">
                <xsl:value-of select="@reverse"/>
            </xsl:attribute>
            <xsl:attribute name="data">
                <xsl:apply-templates select="Data"/>
            </xsl:attribute>
            <xsl:attribute name="width">
                <xsl:apply-templates select="Width"/>
            </xsl:attribute>
            <xsl:attribute name="chop">
                <xsl:apply-templates select="Chop"/>
                <xsl:if test="not(Chop)">
                    <xsl:text>0</xsl:text>
                </xsl:if>
            </xsl:attribute>
        </funccall>
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

    <!--xsl:template match="Name">
        <xsl:copy>
            <xsl:value-of select="."/>
        </xsl:copy>
    </xsl:template-->

    <xsl:template match="Name[ancestor::implementation/ParameterSpecs/ParameterSpec/@name=current()/@name]|name[ancestor::Protocol//Assignment/name/@name=current()/@name]">
        <xsl:value-of select="@name"/>
    </xsl:template>

    <xsl:template match="Width|Chop">
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