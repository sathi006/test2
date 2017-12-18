<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:bean="http://www.springframework.org/schema/beans">

	<!-- Version 1.0 This xslt is used to transform the ui batch defintion bean 
		to spring based xml configuration. -->

	<xsl:template match="/">
		<beans xmlns="http://www.springframework.org/schema/beans"
			xmlns:aop="http://www.springframework.org/schema/aop" xmlns:batch="http://www.springframework.org/schema/batch"
			xmlns:task="http://www.springframework.org/schema/task" xmlns:int="http://www.springframework.org/schema/integration"
			xmlns:jms="http://www.springframework.org/schema/jms" xmlns:int-jms="http://www.springframework.org/schema/integration/jms"
			xmlns:util="http://www.springframework.org/schema/util" xmlns:p="http://www.springframework.org/schema/p"
			xmlns:context="http://www.springframework.org/schema/context"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:file="http://www.springframework.org/schema/integration/file"
			xsi:schemaLocation="http://www.springframework.org/schema/batch
     http://www.springframework.org/schema/batch/spring-batch.xsd
     http://www.springframework.org/schema/aop
     http://www.springframework.org/schema/aop/spring-aop.xsd
     http://www.springframework.org/schema/context
     http://www.springframework.org/schema/context/spring-context.xsd
     http://www.springframework.org/schema/beans
     http://www.springframework.org/schema/beans/spring-beans.xsd
     http://www.springframework.org/schema/task
     http://www.springframework.org/schema/task/spring-task.xsd
     http://www.springframework.org/schema/util
     http://www.springframework.org/schema/util/spring-util.xsd
     http://www.springframework.org/schema/integration
     http://www.springframework.org/schema/integration/spring-integration.xsd">

			<xsl:for-each select="/beans/bean">
				<xsl:choose>
					<xsl:when test="starts-with(@id,'chunk-reference-')">
						<!-- do nothing -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="current()" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<batch:job job-repository="smartBatchRepository">
				<xsl:attribute name="id"><xsl:value-of
					select="/beans/batchDefinition/basicDetails/name" /></xsl:attribute>
				<xsl:for-each
					select="/beans/bean[starts-with(@id,'tasklet-reference-') or starts-with(@id,'chunk-reference-')]">
					<xsl:variable name="bean-counter" select="position()" />
					<batch:step>
						<xsl:attribute name="id">
							<xsl:value-of	select="concat('step-',position())" />
						</xsl:attribute>
						<xsl:if	test="count(/beans/bean[starts-with(@id,'tasklet-reference-') or starts-with(@id,'chunk-reference-')]) &gt; 1">
					<!-- Sandeep Start -->		
							<xsl:variable name="id-value" select="@id" />
							<xsl:variable name="substr-value" select="'-sourceDependent'" />
							<xsl:if	test="substring($id-value, (string-length($id-value) - string-length($substr-value)) + 1) != $substr-value">
								<xsl:if	test="count(/beans/bean[starts-with(@id,'tasklet-reference-') or starts-with(@id,'chunk-reference-')]) != $bean-counter">
									<xsl:attribute name="next">
										<xsl:value-of select="concat('step-',position()+1)" />
									</xsl:attribute>
								</xsl:if>
							</xsl:if>
					<!-- Sandeep End -->
						</xsl:if>
						<batch:tasklet>
						<xsl:attribute name="allow-start-if-complete"><xsl:value-of
							select="/beans/batchDefinition/basicDetails/restartStepFromScratch" /></xsl:attribute>
							<xsl:choose>
								<xsl:when test="starts-with(./@id,'tasklet-reference-') ">
									<xsl:attribute name="ref"><xsl:value-of
										select="./@id" /></xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<!-- Since the copy-of would have an incorrect name space the chunk 
										element is regenerated... -->
									<batch:chunk>
										<xsl:attribute name="reader"><xsl:value-of
											select="chunk/@reader" /></xsl:attribute>
										<xsl:attribute name="writer"><xsl:value-of
											select="chunk/@writer" /></xsl:attribute>
										<xsl:attribute name="commit-interval"><xsl:value-of
											select="chunk/@commit-interval" /></xsl:attribute>
										<xsl:if test="chunk/@skip-policy">
											<xsl:attribute name="skip-policy"><xsl:value-of
												select="chunk/@skip-policy" /></xsl:attribute>
										</xsl:if>
										<xsl:if test="chunk/@processor">
											<xsl:attribute name="processor"><xsl:value-of
												select="chunk/@processor" /></xsl:attribute>
										</xsl:if>
									</batch:chunk>
								</xsl:otherwise>
							</xsl:choose>
							<batch:listeners>
								<batch:listener ref="batchAuditEventEmitter" />
								<batch:listener ref="batchLifeCycleListener" />
							</batch:listeners>
						</batch:tasklet>
						<!-- Sandeep Start -->
							<xsl:variable name="id-value" select="@id" />
							<xsl:variable name="substr-value" select="'-sourceDependent'" />
							<xsl:if	test="substring($id-value, (string-length($id-value) - string-length($substr-value)) + 1) = $substr-value">
								<xsl:if	test="count(/beans/bean[starts-with(@id,'tasklet-reference-') or starts-with(@id,'chunk-reference-')]) != $bean-counter">
									 <batch:next on="*"  >
										 <xsl:attribute name="to">
										 	<xsl:value-of	select="concat('step-',position()+1)" />
										 </xsl:attribute>
									 </batch:next>
								</xsl:if>
							</xsl:if>
						<!-- Sandeep End -->
					</batch:step>
				</xsl:for-each>
				<batch:listeners>
					<batch:listener ref="batchAuditEventEmitter" />
					<batch:listener ref="batchLifeCycleListener" />
				</batch:listeners>
			</batch:job>
		</beans>
	</xsl:template>
</xsl:stylesheet>