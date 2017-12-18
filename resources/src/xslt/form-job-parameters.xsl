<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<parameters>
			<xsl:for-each select="/batchDefinition/sources">
				<xsl:variable name="source-counter" select="position()" />
				<xsl:choose>
					<xsl:when test="/batchDefinition/sources[$source-counter]/type='file'">
						<parameter>
							<xsl:value-of select="concat('source-',$source-counter,'-file-name')" />
						</parameter>
						<xsl:for-each select="/batchDefinition/targets">
							<xsl:variable name="target-counter" select="position()" />
							<xsl:variable name="sourceLinked">
								<xsl:call-template name="check-array-existance">
									<xsl:with-param name="list" select="source-names"></xsl:with-param>
									<xsl:with-param name="value"
										select="number($source-counter) - 1"></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<xsl:if
								test="$sourceLinked='true' or source-names='ALL' or string(source-names)=''">
								<xsl:choose>
									<xsl:when
										test="/batchDefinition/targets[$target-counter]/type='file' and /batchDefinition/sources[$source-counter]/fileDetail/isSingleFile = 'true'">
										<parameter>
											<xsl:value-of
												select="concat('target-',$target-counter,'-file-name')" />
										</parameter>
									</xsl:when>
								</xsl:choose>
							</xsl:if>
						</xsl:for-each>
					</xsl:when>
					<xsl:when
						test="/batchDefinition/sources[$source-counter]/type='database'">
						<parameter>
							<xsl:value-of select="concat('source-',$source-counter,'-sql')" />
						</parameter>
						<xsl:for-each select="/batchDefinition/targets">
							<xsl:variable name="target-counter" select="position()" />
							<xsl:variable name="sourceLinked">
								<xsl:call-template name="check-array-existance">
									<xsl:with-param name="list" select="source-names"></xsl:with-param>
									<xsl:with-param name="value"
										select="number($source-counter) - 1"></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<xsl:if
								test="$sourceLinked='true' or source-names='ALL' or string(source-names)=''">
								<xsl:choose>
									<xsl:when
										test="/batchDefinition/targets[$target-counter]/type='file'">
										<parameter>
											<xsl:value-of
												select="concat('target-',$target-counter,'-file-name')" />
										</parameter>
									</xsl:when>
								</xsl:choose>
							</xsl:if>
						</xsl:for-each>
					</xsl:when>
					<xsl:when
						test="/batchDefinition/sources[$source-counter]/type='message'">
						<parameter>
							<xsl:value-of
								select="concat('source-',$source-counter,'-jms-destination')" />
						</parameter>
						<xsl:for-each select="/batchDefinition/targets">
							<xsl:variable name="target-counter" select="position()" />
							<xsl:variable name="sourceLinked">
								<xsl:call-template name="check-array-existance">
									<xsl:with-param name="list" select="source-names"></xsl:with-param>
									<xsl:with-param name="value"
										select="number($source-counter) - 1"></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<xsl:if
								test="$sourceLinked='true' or source-names='ALL' or string(source-names)=''">
								<xsl:choose>
									<xsl:when
										test="/batchDefinition/targets[$target-counter]/type='file'">
										<parameter>
											<xsl:value-of
												select="concat('target-',$target-counter,'-file-name')" />
										</parameter>
									</xsl:when>
								</xsl:choose>
							</xsl:if>
						</xsl:for-each>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:for-each select="/batchDefinition/targets">
				<xsl:variable name="target-counter" select="position()" />
				<xsl:choose>
					<xsl:when test="/batchDefinition/targets[$target-counter]/type='file'">
						<parameter>
							<xsl:value-of
								select="concat('target-',$target-counter,'-relative-path')" />
						</parameter>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</parameters>
	</xsl:template>
	<xsl:template name="check-array-existance">
		<xsl:param name="list" />
		<xsl:param name="value" />
		<xsl:variable name="exists">
			<xsl:for-each select="$list">
				<xsl:if test="string(.) = $value">
					<xsl:value-of select="true()" />
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:if test="$exists != ''">
			<xsl:value-of select="$exists"></xsl:value-of>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>