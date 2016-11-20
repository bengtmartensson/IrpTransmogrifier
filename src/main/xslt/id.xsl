<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:transform
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:exportformats="http://www.harctoolbox.org/exportformats"
    xmlns:harctoolbox="xxxxxxxxx"
    version="2.0">
    <xsl:output method="xml" />
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:transform>
