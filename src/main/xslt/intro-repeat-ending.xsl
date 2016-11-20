<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0">
    <xsl:output method="xml" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="RepeatMarker"/>

    <xsl:template match="IrStream[@repeatMax='infinite']" mode="repeat">
        <Repeat>
            <xsl:apply-templates select="@*|node()"/>
        </Repeat>
    </xsl:template>

  <!-- General case: ( I R* E) -->
    <xsl:template match="BitspecIrstream/IrStream[IrStream[@repeatMax='infinite']]">
        <Intro>
            <xsl:attribute name="isRepeat" select="'false'"/>
            <xsl:attribute name="numberOfBareDurations">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/preceding-sibling::Flash)
                                    + count(IrStream[@repeatMax='infinite']/preceding-sibling::Gap)
                                    + count(IrStream[@repeatMax='infinite']/preceding-sibling::Extent)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBitSpecs">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/preceding-sibling::BitSpecs)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBits">
                <xsl:value-of select="sum(IrStream[@repeatMax='infinite']/preceding-sibling::FiniteBitField/Width/Number)"/>
            </xsl:attribute>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/preceding-sibling::*"/>
        </Intro>
        <xsl:apply-templates select="IrStream[@repeatMax='infinite']" mode="repeat"/>
        <Ending>
            <xsl:attribute name="isRepeat" select="'false'"/>
            <xsl:attribute name="numberOfBareDurations">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/following-sibling::Flash)
                                    + count(IrStream[@repeatMax='infinite']/following-sibling::Gap)
                                    + count(IrStream[@repeatMax='infinite']/following-sibling::Extent)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBitSpecs">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/following-sibling::BitSpecs)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBits">
                <xsl:value-of select="sum(IrStream[@repeatMax='infinite']/following-sibling::FiniteBitField/Width/Number)"/>
            </xsl:attribute>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/following-sibling::*"/>
        </Ending>
    </xsl:template>

    <!-- Special case: just one sequence, which repeats -->
    <xsl:template match="BitspecIrstream/IrStream[not(Variation) and @repeatMax='infinite']">
        <Intro/>
        <xsl:apply-templates select="." mode="repeat"/>
        <Ending/>
    </xsl:template>

    <!-- Special case: just one sequence, shot exactly once -->
    <xsl:template match="BitspecIrstream/IrStream[not(Variation) and not(@repeatMax='infinite') and not(IrStream[@repeatMax='infinite'])]">
        <Intro>
            <xsl:apply-templates select="@*|*"/>
        </Intro>
        <Repeat/>
        <Ending/>
    </xsl:template>

    <xsl:template match="BitspecIrstream/IrStream[Variation]">
        <Intro>
            <xsl:attribute name="isRepeat" select="'false'"/>
            <xsl:apply-templates select="Variation/preceding-sibling::*"/><!-- FIXME -->
            <xsl:apply-templates select="Variation/BareIrStream[1]/*"/>
            <xsl:if test="Variation/BareIrStream[1]/*">
                <xsl:apply-templates select="Variation/following-sibling::*"/>
            </xsl:if>


            <!--xsl:attribute name="numberOfBareDurations">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/preceding-sibling::Flash)
                                    + count(IrStream[@repeatMax='infinite']/preceding-sibling::Gap)
                                    + count(IrStream[@repeatMax='infinite']/preceding-sibling::Extent)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBitSpecs">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/preceding-sibling::BitSpecs)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBits">
                <xsl:value-of select="sum(IrStream[@repeatMax='infinite']/preceding-sibling::FiniteBitField/Width/Number)"/>
            </xsl:attribute>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/preceding-sibling::*"/-->
        </Intro>
        <Repeat>
            <xsl:attribute name="isRepeat" select="'true'"/>
            <xsl:apply-templates select="Variation/preceding-sibling::*"/><!-- FIXME -->
            <xsl:apply-templates select="Variation/BareIrStream[2]/*"/>
            <xsl:if test="Variation/BareIrStream[2]/*">
                <xsl:apply-templates select="Variation/following-sibling::*"/>
            </xsl:if>
        </Repeat>

        <Ending>
            <xsl:attribute name="isRepeat" select="'false'"/>
            <xsl:if test="Variation/BareIrStream[3]/*">
                <xsl:apply-templates select="Variation/preceding-sibling::*"/><!-- FIXME -->
                <xsl:apply-templates select="Variation/BareIrStream[3]/*"/>
                <xsl:apply-templates select="Variation/following-sibling::*"/>
            </xsl:if>
            <!--xsl:attribute name="numberOfBareDurations">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/following-sibling::Flash)
                                    + count(IrStream[@repeatMax='infinite']/following-sibling::Gap)
                                    + count(IrStream[@repeatMax='infinite']/following-sibling::Extent)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBitSpecs">
                <xsl:value-of select="count(IrStream[@repeatMax='infinite']/following-sibling::BitSpecs)"/>
            </xsl:attribute>
            <xsl:attribute name="numberOfBits">
                <xsl:value-of select="sum(IrStream[@repeatMax='infinite']/following-sibling::FiniteBitField/Width/Number)"/>
            </xsl:attribute>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/following-sibling::*"/-->
        </Ending>
    </xsl:template>


</xsl:stylesheet>