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
  
    <xsl:template match="IrStream[@repeatMax='infinite']"/>
  
    <xsl:template match="IrStream[@repeatMax='infinite']" mode="repeat">
        <repeat>
            <xsl:apply-templates select="@*|node()"/>
        </repeat>
    </xsl:template>
  
  <!-- General case: ( I R* E) -->
    <xsl:template match="BitspecIrstream/IrStream[IrStream[@repeatMax='infinite']]">
        <intro>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/preceding-sibling::*"/>
        </intro>
        <xsl:apply-templates select="IrStream[@repeatMax='infinite']" mode="repeat"/>
        <ending>
            <xsl:apply-templates select="IrStream[@repeatMax='infinite']/following-sibling::*"/>
        </ending>
    </xsl:template>
  
    <!-- Special case: just one sequence, which repeats -->
    <xsl:template match="BitspecIrstream/IrStream[@repeatMax='infinite']">
        <intro/>
        <xsl:apply-templates select="." mode="repeat"/>
        <ending/>
    </xsl:template>
  
    <!-- Special case: just one sequence, shot exactly once -->
    <xsl:template match="BitspecIrstream/IrStream[not(@repeatMax='infinity') and not(IrStream[@repeatMax='infinite'])]">
        <intro>
            <xsl:apply-templates select="*"/>
        </intro>
    </xsl:template>

</xsl:stylesheet>