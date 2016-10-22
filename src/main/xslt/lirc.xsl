<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:transform
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:exportformats="http://www.harctoolbox.org/exportformats"
    version="2.0">

    <xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

    <xsl:output method="xml" />

    <xsl:param name="eps" select="'30'"/>
    <xsl:param name="aeps" select= "'100'"/>

    <xsl:template match="/">

        <exportformats:exportformat>
            <xsl:attribute name="name">Lirc</xsl:attribute>
            <xsl:attribute name="extension">lircd.conf</xsl:attribute>
            <xsl:attribute name="multiSignal">true</xsl:attribute>
            <xsl:attribute name="simpleSequence">false</xsl:attribute>
            <xsl:attribute name="metadata">true</xsl:attribute>

            <axsl:stylesheet>
                <xsl:namespace name="girr" select="'http://www.harctoolbox.org/Girr'"/>
                <xsl:namespace name="exporterutils" select="'http://xml.apache.org/xalan/java/org.harctoolbox.irscrutinizer.exporter.ExporterUtils'"/>
                <xsl:attribute name="version">1.0</xsl:attribute>

                <axsl:output method="text" />

            <axsl:template>
                <xsl:attribute name="match">/girr:remotes</xsl:attribute>
                <axsl:text xml:space="preserve">
# </axsl:text>
                <axsl:value-of select="@title"/>
                <axsl:text>
#
# Creating tool: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingTool</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Creating user: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingUser</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Creating date: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingDate</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Encoding: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$encoding</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
#
</axsl:text>
                <axsl:apply-templates>
                    <xsl:attribute name="select">girr:remote</xsl:attribute>
                </axsl:apply-templates>
            </axsl:template>

            <axsl:template>
                <xsl:attribute name="match">girr:remote</xsl:attribute>
                <axsl:text># Manufacturer: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@manufacturer</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Model: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@model</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Displayname: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@displayName</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Remotename: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@remoteName</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
#
</axsl:text>
                <axsl:apply-templates>
                    <xsl:attribute name="select">girr:commandSet</xsl:attribute>
                </axsl:apply-templates>
            </axsl:template>

            <!-- General case, raw codes -->
            <xsl:text xml:space="preserve">&#10;&#10;</xsl:text>
            <xsl:comment> ################ Default protocol rule, raw codes ############## </xsl:comment>
            <xsl:text xml:space="preserve">&#10;</xsl:text>
            <axsl:template>
                <xsl:attribute name="match">girr:commandSet</xsl:attribute>
                <axsl:text>begin remote
&#9;name&#9;&#9;</axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">../@name</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
&#9;flags&#9;&#9;RAW_CODES
&#9;eps&#9;&#9;30
&#9;aeps&#9;&#9;100
&#9;frequency&#9;</axsl:text>
        <axsl:value-of>
            <xsl:attribute name="select">//girr:command[1]/girr:raw/@frequency</xsl:attribute>
        </axsl:value-of>
        <axsl:text>
&#9;gap&#9;&#9;</axsl:text>
        <axsl:value-of select="//girr:command[1]/girr:raw/girr:repeat/girr:gap[position()=last()]"/>
        <axsl:text>
&#9;begin raw_codes
</axsl:text>
        <axsl:apply-templates select="//girr:command"/>
        <axsl:text>&#9;end raw_codes
end remote
</axsl:text>
    </axsl:template>

    <axsl:template match="girr:command">
        <axsl:text>&#9;&#9;name </axsl:text>
        <axsl:value-of select="@name"/>
        <axsl:text xml:space="preserve">
</axsl:text>
        <axsl:apply-templates select="girr:raw[1]"/>
        <axsl:text xml:space="preserve">
</axsl:text>
    </axsl:template>

    <axsl:template match="girr:raw">
        <axsl:apply-templates select="girr:intro"/>
        <axsl:if test="not(girr:intro)">
            <axsl:apply-templates select="girr:repeat"/>
        </axsl:if>
    </axsl:template>

    <axsl:template match="girr:intro|girr:repeat">
        <axsl:text xml:space="preserve">&#9;&#9;&#9;</axsl:text>
        <axsl:apply-templates select="*"/>
    </axsl:template>

    <axsl:template match="girr:flash">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve"> </axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve"> </axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap[position() mod 4 = 0]">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve">
&#9;&#9;&#9;</axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap[position()=last()]"/>


    <xsl:apply-templates select="//protocol"/>
            </axsl:stylesheet>
        </exportformats:exportformat>
    </xsl:template>

    <xsl:template match="NamedProtocols">
        <xsl:apply-templates select="NamedProtocol"/>
    </xsl:template>

    <xsl:template match="NamedProtocol">
        <xsl:-apply-templates match="Protocol"/>
    </xsl:template>

    <xsl:template match="Protocol">
        <xsl:text xml:space="preserve">&#10;&#10;</xsl:text>
        <xsl:comment> ################## Protocol <xsl:value-of select="@name"/> ################ </xsl:comment>
        <xsl:text xml:space="preserve">&#10;</xsl:text>
        <axsl:template>
            <xsl:attribute name="match">girr:commandSet[girr:command/girr:parameters/@protocol = '<xsl:value-of select="lower-case(replace(@name,'[^_0-9A-Za-z]',''))"/>']</xsl:attribute>
            <axsl:text xml:space="preserve">begin remote
&#9;name&#9;&#9;</axsl:text>
            <axsl:value-of select="../@name"/>
<axsl:text>
&#9;bits&#9;&#9;<xsl:value-of select="function[body/finiteBitField]/@numberOfBits"/>
&#9;flags&#9;&#9;<xsl:apply-templates select="@standardPwm"/><xsl:apply-templates select="@biphase"/>
            <xsl:apply-templates select="function[body/finiteBitField]/body/extent" mode="flags"/>
&#9;eps&#9;&#9;<xsl:value-of select="$eps"/>
&#9;aeps&#9;&#9;<xsl:value-of select="$aeps"/>
&#9;zero&#9;<xsl:apply-templates select="bitspec/bitspeccase[@nr='0']"/>
&#9;one&#9;<xsl:apply-templates select="bitspec/bitspeccase[@nr='1']"/>
<xsl:apply-templates select="function[body/finiteBitField]" mode="header"/>
<xsl:apply-templates select="function[body/finiteBitField]" mode="plead"/>
<xsl:apply-templates select="function[body/finiteBitField]" mode="ptrail"/>
<xsl:apply-templates select="function[@name='repeat']" mode="repeatFlag"/>
<xsl:apply-templates select="function[body/finiteBitField]" mode="gapFlag"/>
<xsl:apply-templates select="function[body/finiteBitField]" mode="toggle_bit"/> <!-- obsolete synonom: repeat_bit -->
&#9;frequency&#9;<xsl:value-of select="@frequency"/>
&#9;begin codes
</axsl:text>
        <axsl:apply-templates select="//girr:command"/>
        <axsl:text>&#9;end codes
end remote
</axsl:text>
        </axsl:template>

        <axsl:template>
            <xsl:attribute name="name">command-<xsl:value-of select="lower-case(replace(@name,'[^_0-9A-Za-z]',''))"/></xsl:attribute>
            <xsl:apply-templates select="function[body/finiteBitField]/parameters/parameter"/>
            <axsl:text xml:space="preserve">&#9;&#9;</axsl:text>
            <axsl:value-of select="@name"/>
            <axsl:text>&#9;0x</axsl:text>
            <axsl:value-of>
                <xsl:attribute name="select">
                    <xsl:text>exporterutils:processBitFields(</xsl:text>
                    <xsl:apply-templates select="function[body/finiteBitField]/body/finiteBitField" mode="inCode"/>
                    <xsl:text>)</xsl:text>
                </xsl:attribute>
            </axsl:value-of>
            <axsl:text xml:space="preserve">
</axsl:text>
        </axsl:template>

        <xsl:apply-templates select="function[body/finiteBitField and parameters/parameter/default]" mode="withDefaults"/>
        <xsl:apply-templates select="function[body/finiteBitField and parameters/parameter]" mode="withoutDefaults"/>
    </xsl:template>

    <xsl:template match="function" mode="withoutDefaults">
        <xsl:comment> Version without defaults </xsl:comment>
        <axsl:template>
            <xsl:attribute name="match" xml:space="skip">
                <xsl:text>girr:command[girr:parameters/@protocol='</xsl:text>
                <xsl:value-of select="lower-case(replace(../@name,'[^_0-9A-Za-z]',''))"/>
                <xsl:text>'</xsl:text>
            <xsl:apply-templates select="parameters/parameter[default]" mode="default-path"/>]</xsl:attribute>
            <axsl:call-template>
                <xsl:attribute xml:space="skip" name="name">
                    <xsl:text>command-</xsl:text>
                    <xsl:value-of xml:space="skip" select="lower-case(replace(../@name,'[^_0-9A-Za-z]',''))"/>
                </xsl:attribute>
                <xsl:apply-templates select="parameters/parameter" mode="inCodeWithoutDefaults"/>
            </axsl:call-template>
        </axsl:template>
    </xsl:template>

    <xsl:template match="function[parameters/parameter/default]" mode="withDefaults">
        <xsl:comment> Version with defaults </xsl:comment>
        <axsl:template>
            <xsl:attribute name="match">girr:command[girr:parameters/@protocol='<xsl:value-of select="lower-case(replace(../@name,'[^_0-9A-Za-z]',''))"/>']</xsl:attribute>
            <axsl:call-template>
                <xsl:attribute xml:space="skip" name="name">
                    <xsl:text>command-</xsl:text>
                    <xsl:value-of xml:space="skip" select="lower-case(replace(../@name,'[^_0-9A-Za-z]',''))"/>
                </xsl:attribute>
                <xsl:apply-templates select="parameters/parameter" mode="inCodeWithDefaults"/>
            </axsl:call-template>
        </axsl:template>
    </xsl:template>

    <xsl:template match="parameter" mode="default-path" xml:space="skip">
        <xsl:text> and girr:parameters/girr:parameter[@name='</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>']</xsl:text>
    </xsl:template>

    <xsl:template match="parameter">
        <axsl:param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
        </axsl:param>
    </xsl:template>

    <xsl:template match="parameter" mode="inCodeWithoutDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="parameter" mode="inCodeWithDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="parameter[./default]" mode="inCodeWithDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:apply-templates select="default"/>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="parameter" mode="inCode">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="default">
        <xsl:apply-templates select="*"/>
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

    <xsl:template match="Name">
        <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>']/@value)</xsl:text>
    </xsl:template>

    <xsl:template name="bool-attribute">
        <xsl:value-of select="."/>
        <xsl:text>()</xsl:text>
    </xsl:template>

    <xsl:template match="@complement">
        <xsl:call-template name="bool-attribute"/>
    </xsl:template>

    <xsl:template match="@reverse">
        <xsl:call-template name="bool-attribute"/>
    </xsl:template>

    <xsl:template match="@reverse[ancestor::protocol[@bitDirection='lsb']]">
        <xsl:if test=".='true'">
            <xsl:text>false()</xsl:text>
        </xsl:if>
        <xsl:if test="not(.='true')">
            <xsl:text>true()</xsl:text>
        </xsl:if>

    </xsl:template>

    <xsl:template match="finiteBitField" mode="namedTemplate">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="data"/>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="finiteBitField" mode="inCode">
        <xsl:apply-templates select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="@reverse"/>
        <xsl:text>, </xsl:text>

        <!--xsl:text>, number(girr:parameters/girr:parameter[@name='</xsl:text-->
        <xsl:apply-templates select="data" mode="inFiniteBitField"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="chop"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="data" mode="inFiniteBitField">
        <xsl:apply-templates select="node()" mode="inFiniteBitField"/>
    </xsl:template>

    <xsl:template match="Name" mode="inFiniteBitField">
        <xsl-text>$</xsl-text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="function" mode="toggle_bit"/>

    <xsl:template match="function[ancestor::protocol/@toggle='true' and body/finiteBitField/data[.='T']]" mode="toggle_bit">
        <xsl:text xml:space="preserve">
&#9;toggle_bit&#9;</xsl:text>
        <xsl:apply-templates select="body/finiteBitField[data[.='T']]" mode="toggle_bit_position"/>
    </xsl:template>

    <xsl:template match="finiteBitField" mode="toggle_bit_position">
        <xsl:value-of select="1 + sum(preceding-sibling::finiteBitField/width)"/>
    </xsl:template>

    <xsl:template match="function" mode="header"/>

    <xsl:template match="function[body/*[1][name()='flash']  and  body/*[2][name()='gap']]" mode="header">
        <xsl:text xml:space="preserve">
&#9;header&#9;</xsl:text>
        <xsl:apply-templates select="body/flash[1]"/>
        <!--xsl:text xml:space="preserve">&#9;</xsl:text-->
        <xsl:apply-templates select="body/gap[1]"/>
    </xsl:template>

    <xsl:template match="function" mode="plead"/>

    <xsl:template match="function[body/*[1][name()='flash']  and  body/*[2][name()='finiteBitField']]" mode="plead">
        <xsl:text xml:space="preserve">
&#9;plead&#9;</xsl:text>
        <xsl:apply-templates select="body/flash[1]"/>
    </xsl:template>

    <xsl:template match="function[body/*[1][name()='flash']  and  body/*[2][name()='gap'] and body/*[3][name()='flash'] ]" mode="plead">
        <xsl:text xml:space="preserve">
&#9;plead&#9;</xsl:text>
        <xsl:apply-templates select="body/flash[2]"/>
    </xsl:template>

    <xsl:template match="function" mode="repeatFlag"/>

    <xsl:template match="function[body/*[1][name()='flash']  and  body/*[2][name()='gap']]" mode="repeatFlag">
        <xsl:text xml:space="preserve">
&#9;repeat&#9;</xsl:text>
        <xsl:apply-templates select="body/flash[1]"/>
        <!--xsl:text xml:space="preserve">&#9;</xsl:text-->
        <xsl:apply-templates select="body/gap[1]"/>
    </xsl:template>

    <xsl:template match="function" mode="gapFlag"/>

    <xsl:template match="function[body/extent]" mode="gapFlag">
        <xsl:text xml:space="preserve">
&#9;gap&#9;</xsl:text>
        <xsl:apply-templates select="body/extent" mode="gap"/>
    </xsl:template>

    <xsl:template match="function[body/*[position()=last()][name()='gap']]" mode="gapFlag">
        <xsl:text xml:space="preserve">
&#9;gap&#9;</xsl:text>
        <xsl:apply-templates select="body/gap"/>
    </xsl:template>

    <xsl:template match="function" mode="ptrail"/>

    <xsl:template match="function[body/flash[preceding-sibling::finiteBitField]]" mode="ptrail">
        <xsl:text xml:space="preserve">
&#9;ptrail&#9;</xsl:text>
        <xsl:apply-templates select="body/flash[preceding-sibling::finiteBitField and position()=last()]"/>
    </xsl:template>

    <xsl:template match="bitspeccase">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="flash[@unit='']|gap[@unit='']">
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@time * number(ancestor::protocol/@timeUnit)"/>
    </xsl:template>

    <xsl:template match="flash[@unit='m']|gap[@unit='m']">
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="1000 * @time"/>
    </xsl:template>

    <xsl:template match="extent[@unit='m']" mode="gap">
        <xsl:text>&#9;</xsl:text>
        <xsl:value-of select="@time * 1000"/>
    </xsl:template>

    <xsl:template match="@standardPwm[.='true']">
        <xsl:text>SPACE_ENC</xsl:text>
    </xsl:template>

    <xsl:template match="@biphase[.='true']">
        <xsl:text>RC5</xsl:text>
    </xsl:template>

    <xsl:template match="@bitDirection[.='lsb']" mode="flags">
        <xsl:text>|REVERSE</xsl:text>
    </xsl:template>

    <xsl:template match="extent" mode="flags">
        <xsl:text>|CONST_LENGTH</xsl:text>
    </xsl:template>

</xsl:transform>