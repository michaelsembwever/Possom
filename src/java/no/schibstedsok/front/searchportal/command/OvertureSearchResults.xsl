<?xml version="1.0" encoding="UTF-8" ?>
<!-- XSL Stylesheet for Overture 2.8.3 
     copyright 1998-2003, Overture Services Inc.
     
     Input:  An Overture XML 2.8.3 or 2.8.2  sample 
     Output:  A sample sponsored search listings page, in HTML.  

     Required:  XML parser and transformer
-->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" encoding="UTF-8" omit-xml-declaration="no" />

<!-- ***********************  -->
<xsl:template match="/">
 <html><body>
  <xsl:apply-templates select="Results" />
 </body></html>
</xsl:template>

<!-- ***********************  -->

<xsl:template match="Results">
 <table align="center" border="0" width="95%" cellpadding="0">
   <xsl:apply-templates select="KeywordsRatingSet" />
   <xsl:apply-templates select="ResultSet" />
 </table>
</xsl:template>

<!-- ***********************  -->

<xsl:template match="KeywordsRatingSet">
 <tr>
 <xsl:apply-templates select="Commercial|Market" />
 <xsl:apply-templates select="Label" />
 <xsl:choose>
  <xsl:when test="@keywords=''">
   <td>
   <xsl:comment>empty search</xsl:comment>
   </td>
  </xsl:when>
  <xsl:otherwise>
   <td><em>Search Results for<xsl:text> [ </xsl:text>
   <xsl:value-of select="@keywords"/><xsl:text> ]</xsl:text></em>
   </td>
  </xsl:otherwise>
 </xsl:choose>
 </tr>
</xsl:template>



<!-- ***********************  -->

<xsl:template match="Commercial|Market">
 <xsl:comment>
  <xsl:value-of select="name(.)"/>
  <xsl:text>=</xsl:text>
  <xsl:value-of select="current()" />
 </xsl:comment>
</xsl:template>


<!-- ***********************  -->

<xsl:template match="Label">
 <xsl:comment>
  <xsl:value-of select="@type"/>
  <xsl:text>=</xsl:text>
  <xsl:value-of select="current()" />
 </xsl:comment>
</xsl:template>


<!-- ***********************  -->

<xsl:template match="ResultSet">
 <tr><td>
 <xsl:comment>
  result set data:
  <xsl:if test="@id">
   id=<xsl:value-of select="@id"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="@numResults">
   numResults=<xsl:value-of select="@numResults"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="@trackURL">
   trackURL=<xsl:value-of select="@trackURL"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="@adultMatch">
   adultMatch=<xsl:value-of select="@adultMatch"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="@adultRating">
   adultRating=<xsl:value-of select="@adultRating"/><xsl:text> </xsl:text>
  </xsl:if>
  <xsl:if test="@comm">
   commercial=<xsl:value-of select="@comm"/><xsl:text> </xsl:text>
  </xsl:if>
 </xsl:comment>
 </td></tr>
 <tr><td>
  <xsl:apply-templates select="Listing"/>
 </td></tr>
 <tr><td align="center">
  <xsl:apply-templates select="PrevArgs"/>
  <xsl:if test="PrevArgs and NextArgs">
    <xsl:text> _ </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="NextArgs"/>
 </td></tr>
</xsl:template>



<!-- ***********************  -->

<xsl:template match="Listing">
 <xsl:variable name="ClickUrl1" select="ClickUrl[position() = 1]" />
 <font face="verdana,sans-serif" size="2">
  <b>
  <xsl:element name="a">
   <xsl:attribute name="href">
    <xsl:value-of select="$ClickUrl1" />
   </xsl:attribute>
   <xsl:value-of select="@title" />
  </xsl:element>
  </b>
  <br></br>
  <xsl:value-of select="@description"/>
  </font> <xsl:text> </xsl:text>
  <br></br>
  <em> <xsl:value-of select="@siteHost"/> </em> 
  <xsl:text> </xsl:text> 
  <xsl:if test="@biddedListing='true'">
   <font size="-2"><a href="http://www.overture.com/">Sponsored Search</a></font>
  </xsl:if>
        <br></br>
  <!-- only show if using Multi-url features. -->
  <xsl:if test="count(ClickUrl) &gt; 1">
   <xsl:for-each select="ClickUrl[position() = 1]">
    <a href="{current()}">
      <xsl:value-of select="@type" /></a> 
   </xsl:for-each>
   <xsl:for-each select="ClickUrl[position() &gt; 1]">
    <xsl:text> _ </xsl:text>
    <a href="{current()}">
      <xsl:value-of select="@type" /></a> 
   </xsl:for-each>
   <br></br>
  </xsl:if>
  <xsl:comment>
    rank=<xsl:value-of select="@rank" />
   <xsl:if test="@adultRating">
    adultRating=<xsl:value-of select="@adultRating"/><xsl:text> </xsl:text>
   </xsl:if>
   <xsl:if test="@bid">
    bid=<xsl:value-of select="@bid"/><xsl:text> </xsl:text>
   </xsl:if>
   <!-- want to disable bidText output escaping, but not supported here -->
   <!-- Disable the escape of < and > so the href tag does not -->
   <!-- get lost in transformation to &lt; &gt;   -->  
   <!-- xsl:value-of disable-output-escaping="yes"  -->  
   <!-- would be nice but not supported to output a comment.  -->  
   <xsl:if test="@bidText">
    bidText=<xsl:value-of select="@bidText"/><xsl:text> </xsl:text>
   </xsl:if>
   <xsl:if test="@currency">
    currency=<xsl:value-of select="@currency"/><xsl:text> </xsl:text>
   </xsl:if>
  </xsl:comment>
  <p></p>
</xsl:template>


<!-- ***********************  -->

<xsl:template match="NextArgs">
 <a href="transform.php?{current()}">Next Page&gt;&gt;</a>
</xsl:template>


<!-- ***********************  -->

<xsl:template match="PrevArgs">
 <a href="transform.php?{current()}">&lt;&lt;Last Page</a>
</xsl:template>

<!-- ***********************  -->



</xsl:stylesheet>
