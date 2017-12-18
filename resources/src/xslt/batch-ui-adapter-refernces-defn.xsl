<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:batch="http://www.springframework.org/schema/batch">

	<!-- Version 1.0 This xslt is used to transform the ui batch defintion bean 
		to spring based xml configuration. -->
	<xsl:variable name="apos" select="string('&quot;')" />
	<xsl:template match="/">
		<beans>
			<xsl:choose>
				<xsl:when
					test="/batchDefinition/sources/fileDetail/transportVariant='rsync'">
					<xsl:call-template name="rsync-template" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="/batchDefinition/sources">
						<xsl:variable name="source-counter" select="position()" />
						<!-- Select the conditions based on the source type... -->
						<xsl:for-each select="/batchDefinition/targets">
							<xsl:variable name="target-counter" select="position()" />
							<xsl:variable name="sourceLinked">
								<xsl:call-template name="check-array-existance">
									<xsl:with-param name="list" select="source-names"></xsl:with-param>
									<xsl:with-param name="value"
										select="number($source-counter) - 1"></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<!-- Sandeep Start -->
							<xsl:variable name="sourceDependent">
								<xsl:call-template name="check-array-existance">
									<xsl:with-param name="list" select="dependent-source-step"></xsl:with-param>
									<xsl:with-param name="value"
										select="number($source-counter) - 1"></xsl:with-param>
								</xsl:call-template>
							</xsl:variable>
							<!-- Sandeep End -->
							<xsl:if
								test="$sourceLinked='true' or source-names='ALL' or string(source-names)=''">
								<xsl:choose>
									<xsl:when
										test="/batchDefinition/sources[$source-counter]/type='file'">
										<xsl:choose>
											<!-- Condition for target type file -->
											<xsl:when test="type='file'">
												<xsl:choose>
													<xsl:when test="transformationOptions/transformationOn='true'">
														<!-- Prepare Local Resource -->
														<xsl:call-template name="local-temp-file-template">
															<xsl:with-param name="source-counter"
																select="$source-counter" />
															<xsl:with-param name="target-counter"
																select="$target-counter" />
														</xsl:call-template>
														<!-- Transfer File to local -->
														<xsl:call-template name="vfs-template">
															<xsl:with-param name="source-counter"
																select="$source-counter" />
															<xsl:with-param name="sourceDependent"
																select="$sourceDependent" />
															<xsl:with-param name="target-counter"
																select="$target-counter" />
															<xsl:with-param name="source-resource"
																select="/batchDefinition/sources[$source-counter]/fileDetail/fsAdapterName" />
															<xsl:with-param name="target-resource"
																select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
															<xsl:with-param name="type" select="string('vfs-local')" />
														</xsl:call-template>
														<!-- Create the request transformer -->
														<bean
															class="com.mcg.batch.runtime.impl.tasklet.FileTransformationReqTasklet"
															scope="step">
															<!-- Sandeep Start -->
															<xsl:if test="$sourceDependent='true'">

																<xsl:attribute name="id">
																		<xsl:value-of
																	select="concat('tasklet-reference-transalation-request-',$source-counter,'-',$target-counter ,'-','sourceDependent')" />
																	</xsl:attribute>

															</xsl:if>
															<xsl:if test="$sourceDependent != 'true'">

																<xsl:attribute name="id">
																		<xsl:value-of
																	select="concat('tasklet-reference-transalation-request-',$source-counter,'-',$target-counter)" />
																	</xsl:attribute>

															</xsl:if>
															<!-- Sandeep End -->
															<property name="requestAdapter">
																<bean class="com.mcg.batch.adapter.impl.JMSAdapter">
																	<property name="resource">
																		<xsl:attribute name="ref"><xsl:value-of
																			select="transformationOptions/transformJMSBean" /></xsl:attribute>
																	</property>
																	<property name="retryerId">
																		<xsl:attribute name="value"><xsl:value-of
																			select="concat(transformationOptions/transformJMSBean,'-retryer')" /></xsl:attribute>
																	</property>
																</bean>
															</property>
															<property name="requestDestination">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/transformJMSDestination" /></xsl:attribute>
															</property>
															<property name="requestDestinationType">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/transformJMSDestinationType" /></xsl:attribute>
															</property>
															<property name="functionName">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/remoteServiceName" /></xsl:attribute>
															</property>
															<property name="params">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/eventTransformationParams" /></xsl:attribute>
															</property>
														</bean>
														<!-- Wait for the response event and transfer to target -->
														<bean
															class="com.mcg.batch.runtime.impl.tasklet.FileTransformationResTasklet"
															scope="step">
															<!-- <xsl:attribute name="id"><xsl:value-of select="concat('tasklet-reference-transalation-response-',$source-counter,'-',$target-counter)" 
																/></xsl:attribute> -->
															<!-- Sandeep Start -->
															<xsl:if test="$sourceDependent='true'">

																<xsl:attribute name="id">
																		<xsl:value-of
																	select="concat('tasklet-reference-transalation-response-',$source-counter,'-',$target-counter ,'-','sourceDependent')" />
																	</xsl:attribute>

															</xsl:if>
															<xsl:if test="$sourceDependent != 'true'">

																<xsl:attribute name="id">
																		<xsl:value-of
																	select="concat('tasklet-reference-transalation-response-',$source-counter,'-',$target-counter)" />
																	</xsl:attribute>

															</xsl:if>
															<!-- Sandeep End -->
															<property name="responseAdapter">
																<bean class="com.mcg.batch.adapter.impl.JMSAdapter">
																	<property name="resource">
																		<xsl:attribute name="ref"><xsl:value-of
																			select="transformationOptions/transformResponseJMSBean" /></xsl:attribute>
																	</property>
																	<property name="retryerId">
																		<xsl:attribute name="value"><xsl:value-of
																			select="concat(transformationOptions/transformResponseJMSBean,'-retryer')" /></xsl:attribute>
																	</property>
																</bean>
															</property>
															<property name="responseDestination">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/transformResponseJMSDestination" /></xsl:attribute>
															</property>
															<property name="responseDestinationType">
																<xsl:attribute name="value"><xsl:value-of
																	select="transformationOptions/transformResponseJMSDestinationType" /></xsl:attribute>
															</property>
															<xsl:if
																test="transformationOptions/transformResponseJMSDestinationType = 'Topic' and string-length(transformationOptions/transformResponseJMSDSName) != 0">
																<property name="responseDestinationDS">
																	<xsl:attribute name="value"><xsl:value-of
																		select="transformationOptions/transformResponseJMSDSName" /></xsl:attribute>
																</property>
															</xsl:if>
															<xsl:if
																test="string-length(transformationOptions/eventResponseTimeOut) !=0">
																<property name="timeout">
																	<xsl:attribute name="value"><xsl:value-of
																		select="transformationOptions/eventResponseTimeOut" /></xsl:attribute>
																</property>
															</xsl:if>
															<property name="vfsTasklet">
																<xsl:call-template name="vfs-template">
																	<xsl:with-param name="source-counter"
																		select="$source-counter" />
																	<xsl:with-param name="sourceDependent"
																		select="$sourceDependent" />
																	<xsl:with-param name="target-counter"
																		select="$target-counter" />
																	<xsl:with-param name="source-resource"
																		select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
																	<xsl:with-param name="target-resource"
																		select="fileDetail/fsAdapterName" />
																	<xsl:with-param name="type"
																		select="string('local-vfs')" />
																</xsl:call-template>
															</property>
														</bean>
														<xsl:call-template name="temp-file-cleanup">
															<xsl:with-param name="source-counter"
																select="$source-counter" />
															<xsl:with-param name="sourceDependent"
																select="$sourceDependent" />
															<xsl:with-param name="target-counter"
																select="$target-counter" />
														</xsl:call-template>
													</xsl:when>
													<xsl:otherwise>
														<xsl:call-template name="vfs-template">
															<xsl:with-param name="source-counter"
																select="$source-counter" />
															<xsl:with-param name="sourceDependent"
																select="$sourceDependent" />
															<xsl:with-param name="target-counter"
																select="$target-counter" />
															<xsl:with-param name="source-resource"
																select="/batchDefinition/sources[$source-counter]/fileDetail/fsAdapterName" />
															<xsl:with-param name="target-resource"
																select="fileDetail/fsAdapterName" />
															<xsl:with-param name="type" select="string('vfs-vfs')" />
														</xsl:call-template>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<!-- Condition for target type database -->
											<xsl:when test="type='database'">
												<xsl:call-template name="local-temp-file-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="vfs-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="source-resource"
														select="/batchDefinition/sources[$source-counter]/fileDetail/fsAdapterName" />
													<xsl:with-param name="target-resource"
														select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="type" select="string('vfs-vfs')" />
												</xsl:call-template>

												<xsl:call-template name="db-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="flat-file-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('file-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('db-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('atomic')" />
												</xsl:call-template>
												<xsl:call-template name="temp-file-cleanup">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>
											</xsl:when>
											<!-- Condition for target type message -->
											<xsl:when test="type='message'">
												<xsl:call-template name="local-temp-file-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="vfs-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="source-resource"
														select="/batchDefinition/sources[$source-counter]/fileDetail/fsAdapterName" />
													<xsl:with-param name="target-resource"
														select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="type" select="string('vfs-vfs')" />
												</xsl:call-template>

												<xsl:call-template name="jms-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="flat-file-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('file-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('jms-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('atomic')" />
												</xsl:call-template>
												<xsl:call-template name="temp-file-cleanup">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>
											</xsl:when>
										</xsl:choose>
									</xsl:when>
									<xsl:when
										test="/batchDefinition/sources[$source-counter]/type='database'">
										<xsl:choose>
											<xsl:when test="type='file'">

												<xsl:call-template name="db-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="flat-file-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('db-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('file-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>

												<xsl:call-template name="local-temp-file-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="vfs-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="source-resource"
														select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="target-resource"
														select="/batchDefinition/targets[$target-counter]/fileDetail/fsAdapterName" />
													<xsl:with-param name="type" select="string('db-vfs')" />
												</xsl:call-template>
												<xsl:call-template name="temp-file-cleanup">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>
											</xsl:when>
											<xsl:when test="type='message'">
												<xsl:call-template name="db-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="jms-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('db-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('jms-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>

											</xsl:when>
											<xsl:when test="type='database'">
												<xsl:call-template name="db-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="db-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('db-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('db-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>

											</xsl:when>
										</xsl:choose>
									</xsl:when>
									<xsl:when
										test="/batchDefinition/sources[$source-counter]/type='message'">
										<xsl:choose>
											<xsl:when test="type='database'">
												<xsl:call-template name="jms-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="db-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('jms-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('db-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>
											</xsl:when>
											<xsl:when test="type='message'">
												<xsl:call-template name="jms-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="jms-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('jms-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('jms-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>
											</xsl:when>
											<xsl:when test="type='file'">

												<xsl:call-template name="jms-reader">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="flat-file-writer">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="chunk-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="reader-value"
														select="concat('jms-reader-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="writer-value"
														select="concat('file-writer-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="chunk-type" select="string('composite')" />
												</xsl:call-template>

												<xsl:call-template name="local-temp-file-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>

												<xsl:call-template name="vfs-template">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
													<xsl:with-param name="source-resource"
														select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" />
													<xsl:with-param name="target-resource"
														select="/batchDefinition/targets[$target-counter]/fileDetail/fsAdapterName" />
													<xsl:with-param name="type" select="string('message-vfs')" />
												</xsl:call-template>
												<xsl:call-template name="temp-file-cleanup">
													<xsl:with-param name="source-counter"
														select="$source-counter" />
													<xsl:with-param name="sourceDependent"
														select="$sourceDependent" />
													<xsl:with-param name="target-counter"
														select="$target-counter" />
												</xsl:call-template>
											</xsl:when>
										</xsl:choose>
									</xsl:when>
								</xsl:choose>
							</xsl:if>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:copy-of select="/" />
		</beans>
	</xsl:template>

	<!-- Reusable template for jms reader -->
	<xsl:template name="jms-reader">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<bean class="com.mcg.batch.runtime.impl.item.readers.JMSItemReader"
			scope="step">
			<xsl:attribute name="id"><xsl:value-of
				select="concat('jms-reader-',$source-counter,'-',$target-counter)" /></xsl:attribute>

			<property name="adapter">
				<bean class="com.mcg.batch.adapter.impl.JMSAdapter">
					<property name="resource">
						<xsl:attribute name="ref"><xsl:value-of
							select="/batchDefinition/sources[$source-counter]/messageDetail/adapterName" /></xsl:attribute>
					</property>
					<property name="retryerId">
						<xsl:attribute name="value"><xsl:value-of
							select="concat(/batchDefinition/sources[$source-counter]/messageDetail/adapterName,'-retryer')" /></xsl:attribute>
					</property>
				</bean>
			</property>
			<property name="destination">
				<xsl:attribute name="value"><xsl:value-of
					select="concat('#{jobParameters[','source',$source-counter,'jmsdestination','] != null ? jobParameters[','source',$source-counter,'jmsdestination','] : ',$apos,/batchDefinition/sources[$source-counter]/messageDetail/destinationName,$apos,'}')" /></xsl:attribute>
				<!-- <xsl:attribute name="value"><xsl:value-of -->
				<!-- select="/batchDefinition/sources[$source-counter]/messageDetail/destinationName" 
					/></xsl:attribute> -->
			</property>
			<xsl:if
				test="string-length(/batchDefinition/sources[$source-counter]/messageDetail/durableSubscriberName) != 0">
				<property name="durableSubscriberName">
					<xsl:attribute name="value"><xsl:value-of
						select="/batchDefinition/sources[$source-counter]/messageDetail/durableSubscriberName" /></xsl:attribute>
				</property>
			</xsl:if>
			<property name="destinationType">
				<xsl:attribute name="value"><xsl:value-of
					select="/batchDefinition/sources[$source-counter]/messageDetail/destinationType" /></xsl:attribute>
			</property>

			<xsl:if
				test="string-length(/batchDefinition/sources[$source-counter]/messageDetail/messageFilterExpression) != 0">
				<property name="filter">
					<xsl:attribute name="value"><xsl:value-of
						select="concat('#{jobParameters[','source',$source-counter,'jmsdestination','] != null ? jobParameters[','source',$source-counter,'jmsfilter','] : ',$apos,/batchDefinition/sources[$source-counter]/messageDetail/messageFilterExpression,$apos,'}')" /></xsl:attribute>
					<!-- <xsl:attribute name="value"><xsl:value-of -->
					<!-- select="/batchDefinition/sources[$source-counter]/messageDetail/messageFilterExpression" 
						/></xsl:attribute> -->
				</property>
			</xsl:if>
			<xsl:if
				test="string-length(/batchDefinition/sources[$source-counter]/messageDetail/receiveTimeOut) !=0">
				<property name="receiveTimeout">
					<xsl:attribute name="value"><xsl:value-of
						select="/batchDefinition/sources[$source-counter]/messageDetail/receiveTimeOut" /></xsl:attribute>
				</property>
			</xsl:if>
			<xsl:if
				test="string-length(/batchDefinition/sources[$source-counter]/messageDetail/messageAckMode) !=0">
				<property name="ackMode">
					<xsl:attribute name="value"><xsl:value-of
						select="concat('#{T(javax.jms.Session).',/batchDefinition/sources[$source-counter]/messageDetail/messageAckMode,'}')" /></xsl:attribute>
				</property>
			</xsl:if>
			<xsl:if
				test="string-length(/batchDefinition/sources[$source-counter]/messageDetail/maxMessageCount) !=0">
				<property name="maxMessageCount">
					<xsl:attribute name="value"><xsl:value-of
						select="/batchDefinition/sources[$source-counter]/messageDetail/maxMessageCount" /></xsl:attribute>
				</property>
			</xsl:if>
		</bean>

	</xsl:template>

	<!-- Reusable template for jms-writer -->
	<xsl:template name="jms-writer">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<bean class="com.mcg.batch.runtime.impl.item.writers.JMSItemWriter"
			scope="step">
			<xsl:attribute name="id"><xsl:value-of
				select="concat('jms-writer-',$source-counter,'-',$target-counter)" /></xsl:attribute>
			<property name="destination">
				<xsl:attribute name="value"><xsl:value-of
					select="/batchDefinition/targets[$target-counter]/messageDetail/destinationName" /></xsl:attribute>
			</property>
			<property name="adapter">
				<bean class="com.mcg.batch.adapter.impl.JMSAdapter">
					<property name="resource">
						<xsl:attribute name="ref"><xsl:value-of
							select="/batchDefinition/targets[$target-counter]/messageDetail/adapterName" /></xsl:attribute>
					</property>
					<property name="retryerId">
						<xsl:attribute name="value"><xsl:value-of
							select="concat(/batchDefinition/targets[$target-counter]/messageDetail/adapterName,'-retryer')" /></xsl:attribute>
					</property>
				</bean>
			</property>
		</bean>
	</xsl:template>


	<!-- Reusable template for db-writer .. -->
	<xsl:template name="db-writer">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />

		<bean class="com.mcg.batch.runtime.impl.item.writers.JdbcItemWriter"
			scope="step">

			<xsl:attribute name="id"><xsl:value-of
				select="concat('db-writer-',$source-counter,'-',$target-counter)" /></xsl:attribute>
			<property name="adapter">
				<bean class="com.mcg.batch.adapter.impl.JdbcAdapter">
					<property name="writer">
						<bean
							class="org.springframework.batch.item.database.JdbcBatchItemWriter">
							<property name="dataSource">
								<xsl:attribute name="ref"><xsl:value-of
									select="/batchDefinition/targets[$target-counter]/databaseDetail/adapterName" /></xsl:attribute>
							</property>
							<property name="sql">
								<xsl:attribute name="value"><xsl:value-of
									select="/batchDefinition/targets[$target-counter]/databaseDetail/SQL" /></xsl:attribute>
							</property>
							<!-- It will take care matching between object property and sql name 
								parameter -->
							<property name="itemSqlParameterSourceProvider">
								<bean
									class="org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider" />
							</property>
						</bean>
					</property>
					<property name="retryerId">
						<xsl:attribute name="value"><xsl:value-of
							select="concat(/batchDefinition/targets[$target-counter]/databaseDetail/adapterName,'-retryer')" /></xsl:attribute>
					</property>
				</bean>
			</property>
		</bean>
	</xsl:template>
	<!-- Reusable template for db reader -->
	<xsl:template name="db-reader">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<bean class="com.mcg.batch.runtime.impl.item.readers.JdbcItemReader"
			scope="step">
			<xsl:attribute name="id"><xsl:value-of
				select="concat('db-reader-',$source-counter,'-',$target-counter)" /></xsl:attribute>
			<property name="adapter">
				<bean class="com.mcg.batch.adapter.impl.JdbcAdapter">
					<property name="reader">
						<bean
							class="org.springframework.batch.item.database.JdbcCursorItemReader">
							<property name="dataSource">
								<xsl:attribute name="ref"><xsl:value-of
									select="/batchDefinition/sources[$source-counter]/databaseDetail/adapterName" /></xsl:attribute>
							</property>
							<property name="sql">
								<xsl:attribute name="value"><xsl:value-of
									select="concat('#{jobParameters[','source',$source-counter,'sql','] != null ? jobParameters[','source',$source-counter,'sql','] : ',$apos,/batchDefinition/sources[$source-counter]/databaseDetail/SQL,$apos,'}')" /></xsl:attribute>
								<!-- <xsl:attribute name="value"><xsl:value-of -->
								<!-- select="/batchDefinition/sources[$source-counter]/databaseDetail/SQL" 
									/></xsl:attribute> -->
							</property>
							<property name="rowMapper">
								<bean>
									<xsl:attribute name="class"><xsl:value-of
										select="/batchDefinition/sources[$source-counter]/databaseDetail/rowMapperClassName" /></xsl:attribute>
								</bean>
							</property>
						</bean>
					</property>
					<property name="retryerId">
						<xsl:attribute name="value"><xsl:value-of
							select="concat(/batchDefinition/sources[$source-counter]/databaseDetail/adapterName,'-retryer')" /></xsl:attribute>
					</property>
				</bean>
			</property>
		</bean>
	</xsl:template>
	<!-- Reusable template for VFS BatchFileParameters properties -->
	<xsl:template name="batch-file-common">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<xsl:param name="type" />

		<xsl:choose>
			<xsl:when test="$type='vfs-vfs'">
				<property name="batchFileParameters">
					<bean
						class="com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters">
						<constructor-arg>
							<map>
								<entry key="vfs.target.relative.folder.path">
									<xsl:attribute name="value"><xsl:value-of
										select="concat('#{jobParameters[','target',$target-counter,'relativepath','] != null ? jobParameters[','target',$target-counter,'relativepath','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/folderPath,$apos,'}')" /></xsl:attribute>
								</entry>
								<entry key="vfs.transfer.file.name">
									<xsl:attribute name="value"><xsl:value-of
										select="concat('#{jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
									<!-- <xsl:attribute name="value"><xsl:value-of -->
									<!-- select="/batchDefinition/sources[$source-counter]/fileDetail/fileName" 
										/></xsl:attribute> -->
								</entry>
								<entry key="vfs.transfer.check.source">
									<xsl:attribute name="value"><xsl:value-of
										select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/sourceFileCheck" /></xsl:attribute>
								</entry>
								<entry key="vfs.is.single.file">
									<xsl:attribute name="value"><xsl:value-of
										select="/batchDefinition/sources[$source-counter]/fileDetail/isSingleFile" /></xsl:attribute>
								</entry>
								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/isSingleFile = 'true'">
									<entry key="vfs.target.file.name">
										<xsl:attribute name="value"><xsl:value-of
											select="concat('#{jobParameters[','target',$target-counter,'filename','] != null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/numberOfFiles)!=0">
									<entry key="vfs.transfer.max.file.count">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/numberOfFiles" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/maximumFileSize)!=0">
									<entry key="vfs.transfer.max.file.size">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/maximumFileSize" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortOrder)!=0">
									<entry key="vfs.transfer.filter.sort.order">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortOrder" /></xsl:attribute>
									</entry>

								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAge)!=0">
									<entry key="vfs.min.file.age">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAge" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAgeAction)!=0">
									<entry key="vfs.min.file.age.action">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAgeAction" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortBy)!=0">
									<entry key="vfs.transfer.filter.sort.by">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortBy" /></xsl:attribute>
									</entry>

								</xsl:if>

								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingOn = 'true' ">
									<xsl:choose>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptExecutionCriteria='BEFORE_FIRST_TARGET' and $target-counter=1">
											<entry key="vfs.source.pre.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptExecutionCriteria='BEFORE_EACH_TARGET'">
											<entry key="vfs.source.pre.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
									</xsl:choose>
								</xsl:if>
								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingOn = 'true'">
									<xsl:choose>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptExecutionCriteria='AFTER_LAST_TARGET' and $target-counter = count(/batchDefinition/targets)">
											<entry key="vfs.source.post.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptExecutionCriteria='AFTER_EACH_TARGET'">
											<entry key="vfs.source.post.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
									</xsl:choose>
								</xsl:if>
								<xsl:if
									test="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/preProcessingOn = 'true'  ">
									<entry key="vfs.target.pre.processing.script">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/postProcessingOn = 'true'">
									<entry key="vfs.target.post.processing.script">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression)!=0 or string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/compression)!=0">
									<entry key="sftp.compression">
										<xsl:choose>
											<xsl:when
												test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression)!=0">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression" /></xsl:attribute>
											</xsl:when>
											<xsl:otherwise>
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/compression" /></xsl:attribute>
											</xsl:otherwise>
										</xsl:choose>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode)!=0">
									<entry key="vfs.transfer.type">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode" /></xsl:attribute>
									</entry>
								</xsl:if>





							</map>
						</constructor-arg>
					</bean>
				</property>
				<property name="operation">
					<xsl:choose>
						<xsl:when
							test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/deleteSource='true' and $target-counter=last()">
							<value type="com.mcg.batch.utils.FileOperation">MOVE</value>
						</xsl:when>
						<xsl:otherwise>
							<value type="com.mcg.batch.utils.FileOperation">COPY</value>
						</xsl:otherwise>
					</xsl:choose>
				</property>
			</xsl:when>
			<xsl:when test="$type='vfs-local'">
				<property name="batchFileParameters">
					<bean
						class="com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters">
						<constructor-arg>
							<map>

								<entry key="vfs.transfer.file.name">
									<xsl:attribute name="value"><xsl:value-of
										select="concat('#{jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
									<!-- <xsl:attribute name="value"><xsl:value-of -->
									<!-- select="/batchDefinition/sources[$source-counter]/fileDetail/fileName" 
										/></xsl:attribute> -->
								</entry>
								<entry key="vfs.transfer.check.source">
									<xsl:attribute name="value"><xsl:value-of
										select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/sourceFileCheck" /></xsl:attribute>
								</entry>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/numberOfFiles)!=0">
									<entry key="vfs.transfer.max.file.count">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/numberOfFiles" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/maximumFileSize)!=0">
									<entry key="vfs.transfer.max.file.size">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/maximumFileSize" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortOrder)!=0">
									<entry key="vfs.transfer.filter.sort.order">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortOrder" /></xsl:attribute>
									</entry>

								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAge)!=0">
									<entry key="vfs.min.file.age">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAge" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAgeAction)!=0">
									<entry key="vfs.min.file.age.action">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/minFileAgeAction" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortBy)!=0">
									<entry key="vfs.transfer.filter.sort.by">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/fileSortBy" /></xsl:attribute>
									</entry>

								</xsl:if>

								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingOn = 'true' ">
									<xsl:choose>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptExecutionCriteria='BEFORE_FIRST_TARGET' and $target-counter=1">
											<entry key="vfs.source.pre.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptExecutionCriteria='BEFORE_EACH_TARGET'">
											<entry key="vfs.source.pre.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
									</xsl:choose>
								</xsl:if>

								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingOn = 'true'">
									<xsl:choose>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptExecutionCriteria='AFTER_LAST_TARGET' and $target-counter = count(/batchDefinition/targets)">
											<entry key="vfs.source.post.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptExecutionCriteria='AFTER_EACH_TARGET'">
											<entry key="vfs.source.post.processing.script">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
											</entry>
										</xsl:when>
									</xsl:choose>
								</xsl:if>

								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression)!=0 or string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/compression)!=0">
									<entry key="sftp.compression">
										<xsl:choose>
											<xsl:when
												test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression)!=0">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression" /></xsl:attribute>
											</xsl:when>
											<xsl:otherwise>
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/compression" /></xsl:attribute>
											</xsl:otherwise>
										</xsl:choose>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode)!=0">
									<entry key="vfs.transfer.type">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode" /></xsl:attribute>
									</entry>
								</xsl:if>

							</map>
						</constructor-arg>
					</bean>
				</property>
				<property name="operation">
					<xsl:choose>
						<xsl:when
							test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/deleteSource='true' and $target-counter=last()">
							<value type="com.mcg.batch.utils.FileOperation">MOVE</value>
						</xsl:when>
						<xsl:otherwise>
							<value type="com.mcg.batch.utils.FileOperation">COPY</value>
						</xsl:otherwise>
					</xsl:choose>
				</property>

			</xsl:when>
			<xsl:otherwise>

				<property name="batchFileParameters">
					<bean
						class="com.mcg.batch.adapters.impl.support.vfs.BatchVFSParameters">
						<constructor-arg>
							<map>
								<entry key="vfs.target.relative.folder.path">
									<xsl:attribute name="value"><xsl:value-of
										select="concat('#{jobParameters[','target',$target-counter,'relativepath','] != null ? jobParameters[','target',$target-counter,'relativepath','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/folderPath,$apos,'}')" /></xsl:attribute>
								</entry>
								<entry key="vfs.transfer.file.name">
									<xsl:attribute name="value"><xsl:value-of
										select="concat('#{jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
									<!-- <xsl:attribute name="value"><xsl:value-of -->
									<!-- select="/batchDefinition/sources[$source-counter]/fileDetail/fileName" 
										/></xsl:attribute> -->
								</entry>
								<entry key="vfs.is.single.file">
									<xsl:attribute name="value"><xsl:value-of
										select="/batchDefinition/sources[$source-counter]/fileDetail/isSingleFile" /></xsl:attribute>
								</entry>
								<xsl:if
									test="/batchDefinition/sources[$source-counter]/fileDetail/isSingleFile = 'true'">
									<entry key="vfs.target.file.name">
										<xsl:attribute name="value"><xsl:value-of
											select="concat('#{jobParameters[','target',$target-counter,'filename','] != null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/preProcessingOn = 'true'  ">
									<entry key="vfs.target.pre.processing.script">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/preProcessingScriptPath" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/postProcessingOn = 'true'">
									<entry key="vfs.target.post.processing.script">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/postProcessingScriptPath" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression)!=0">
									<entry key="sftp.compression">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/compression" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/maximumFileSize)!=0">
									<entry key="vfs.transfer.max.file.size">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/maximumFileSize" /></xsl:attribute>
									</entry>
								</xsl:if>
								<xsl:if
									test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode)!=0">
									<entry key="vfs.transfer.type">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/advancedOptions/fileWriteMode" /></xsl:attribute>
									</entry>
								</xsl:if>







							</map>
						</constructor-arg>
					</bean>
				</property>
				<property name="operation">
					<value type="com.mcg.batch.utils.FileOperation">COPY</value>
				</property>
			</xsl:otherwise>
		</xsl:choose>


	</xsl:template>
	<!-- Template to be called only when rsync is to be used -->
	<xsl:template name="rsync-template">
		<xsl:for-each select="/batchDefinition/sources">
			<xsl:variable name="source-counter" select="position()" />
			<xsl:variable name="apos">
				'
			</xsl:variable>
			<xsl:for-each select="/batchDefinition/targets">
				<xsl:variable name="target-counter" select="position()" />
				<bean class="com.mcg.batch.runtime.impl.tasklet.RsyncTasklet"
					scope="step">
					<xsl:attribute name="id"><xsl:value-of
						select="concat('tasklet-reference-',$source-counter,'-',$target-counter)" /></xsl:attribute>
					<property name="rsyncAdapter">
						<bean class="com.mcg.batch.adapter.impl.RsyncAdapter">
							<property name="resource">
								<list>
									<ref>
										<xsl:attribute name="bean"><xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/rsyncAdapterName" /></xsl:attribute>
									</ref>
									<ref>
										<xsl:attribute name="bean"><xsl:value-of
											select="fileDetail/rsyncAdapterName" /></xsl:attribute>
									</ref>
								</list>
							</property>
							<property name="retryerId">
								<xsl:attribute name="value"><xsl:value-of
									select="concat(/batchDefinition/sources[$source-counter]/fileDetail/rsyncAdapterName,'-retryer')" /></xsl:attribute>
							</property>
						</bean>
					</property>
					<property name="batchRSyncParameters">
						<bean
							class="com.mcg.batch.adapters.impl.support.rsync.BatchRsyncParameters">
							<property name="parameters">
								<map>
									<entry key="rsync.transfer.file.name">
										<xsl:attribute name="value"><xsl:value-of
											select="concat('#{jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" /></xsl:attribute>
										<!-- <xsl:attribute name="value"><xsl:value-of -->
										<!-- select="/batchDefinition/sources[$source-counter]/fileDetail/fileName" 
											/></xsl:attribute> -->
									</entry>
									<xsl:if
										test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/excludeFileFilter) != 0">
										<entry key="rsync.transfer.filter.exclude">
											<xsl:attribute name="value"><xsl:value-of
												select="/batchDefinition/sources[$source-counter]/advancedOptions/excludeFileFilter" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/includeFileFilter) != 0">
										<entry key="rsync.transfer.filter.include">
											<xsl:attribute name="value"><xsl:value-of
												select="/batchDefinition/sources[$source-counter]/advancedOptions/includeFileFilter" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:choose>
										<xsl:when
											test="/batchDefinition/sources[$source-counter]/fileDetail/advancedOptions/deleteSource='true'">
											<entry key="rsync.transfer.type" value="MOVE" />
										</xsl:when>
										<xsl:otherwise>
											<entry key="rsync.transfer.type" value="COPY" />
										</xsl:otherwise>
									</xsl:choose>
								</map>
							</property>
						</bean>
					</property>
				</bean>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	<!-- Reusable template for copying from VFS to local fs -->
	<xsl:template name='local-temp-file-template'>
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<!-- Target bean to copy the file to local directory for -->
		<bean class="com.mcg.batch.adapters.impl.support.vfs.VFSResource"
			scope="prototype">
			<xsl:attribute name="id"><xsl:value-of
				select="concat('temp-file-resource-',$source-counter,'-',$target-counter)" /></xsl:attribute>
			<property name="fileName">
				<bean class="com.mcg.batch.adapters.impl.support.vfs.LocalFileName">
					<constructor-arg name="scheme" value="file" />
					<constructor-arg name="rootFile" value="/" />
					<constructor-arg name="path">
						<xsl:attribute name="value"><xsl:value-of
							select="concat('','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}')" /></xsl:attribute>
					</constructor-arg>
				</bean>
			</property>
			<property name="parameters">
				<map />
			</property>
		</bean>
	</xsl:template>
	<!-- Resuable template for VFS -->
	<xsl:template name="vfs-template">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<xsl:param name="source-resource" />
		<xsl:param name="target-resource" />
		<xsl:param name="sourceDependent" />
		<xsl:param name="type" />
		<bean scope="step" lazy-init="true">
			<!-- Generate the adapter name... -->
			<!-- <xsl:attribute name="id"><xsl:value-of select="concat('tasklet-reference-',$source-counter,'-',$target-counter)" 
				/></xsl:attribute> -->
			<!-- Sandeep Start -->
			<xsl:if test="$sourceDependent='true'">

				<xsl:attribute name="id">
							<xsl:value-of
					select="concat('tasklet-reference-',$source-counter,'-',$target-counter ,'-','sourceDependent')" />
						</xsl:attribute>

			</xsl:if>
			<xsl:if test="$sourceDependent != 'true'">

				<xsl:attribute name="id">
							<xsl:value-of
					select="concat('tasklet-reference-',$source-counter,'-',$target-counter)" />
						</xsl:attribute>

			</xsl:if>
			<!-- Sandeep End -->
			<xsl:attribute name="class">com.mcg.batch.runtime.impl.tasklet.VFSTasklet</xsl:attribute>
			<property name="adapter">
				<bean>
					<xsl:attribute name="class">com.mcg.batch.adapter.impl.VFSAdapter</xsl:attribute>
					<property name="resource">
						<list>
							<ref>
								<xsl:attribute name="bean"><xsl:value-of
									select="$source-resource" /></xsl:attribute>
							</ref>
							<ref>
								<xsl:attribute name="bean"><xsl:value-of
									select="$target-resource" /></xsl:attribute>
							</ref>
						</list>
					</property>
					<property name="retryerId">
						<xsl:attribute name="value"><xsl:value-of
							select="concat($source-resource,'-retryer')" /></xsl:attribute>
					</property>
				</bean>
			</property>
			<xsl:call-template name="batch-file-common">
				<xsl:with-param name="source-counter" select="$source-counter" />
				<xsl:with-param name="target-counter" select="$target-counter" />
				<xsl:with-param name="type" select="$type" />
			</xsl:call-template>
		</bean>
	</xsl:template>
	<!-- Reusable template for flat file writer -->
	<xsl:template name="flat-file-writer">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<xsl:choose>
			<xsl:when
				test="/batchDefinition/targets[$target-counter]/fileDetail/fileFormat='flatfile'">
				<bean class="org.springframework.batch.item.file.FlatFileItemWriter"
					scope="step">
					<xsl:attribute name="id"><xsl:value-of
						select="concat('file-writer-',$source-counter,'-',$target-counter)" /></xsl:attribute>
					<xsl:variable name="correctedResource">
						<xsl:value-of
							select="concat('jobParameters[','target',$target-counter,'filename','] != null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos)" />
					</xsl:variable>

					<property name="resource">
						<xsl:attribute name="value"><xsl:value-of
							select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{T(com.mcg.batch.utils.FileNameUtils).retrieveFileName(',$correctedResource,')}')" /></xsl:attribute>
					</property>

					<!-- <property name="resource"> <xsl:attribute name="value"><xsl:value-of 
						select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{jobParameters[','target',$target-counter,'filename','] 
						!= null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos,'}')" 
						/></xsl:attribute> </property> -->
					<property name="lineAggregator">
						<!-- An Aggregator which converts an object into delimited list of 
							strings -->
						<xsl:choose>
							<xsl:when
								test="string-length(/batchDefinition/targets[$target-counter]/fileDetail/flatFileDelimiter)!=0">
								<bean
									class="org.springframework.batch.item.file.transform.DelimitedLineAggregator">

									<property name="delimiter">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/flatFileDelimiter" /></xsl:attribute>
									</property>

									<property name="fieldExtractor">
										<!-- Extractor which returns the value of beans property through 
											reflection -->
										<bean
											class="org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor">
											<property name="names">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/targets[$target-counter]/fileDetail/flatFileFieldNames" /></xsl:attribute>
											</property>
										</bean>
									</property>
								</bean>
							</xsl:when>
							<xsl:otherwise>
								<bean
									class="org.springframework.batch.item.file.transform.FormatterLineAggregator">

									<property name="format">
										<xsl:attribute name="value"><xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/flatFileRecordFormat" /></xsl:attribute>
									</property>
									<property name="fieldExtractor">
										<bean
											class="org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor">
											<property name="names">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/target[$target-counter]/fileDetail/flatFileFieldNames" /></xsl:attribute>
											</property>
										</bean>
									</property>
								</bean>
							</xsl:otherwise>
						</xsl:choose>
					</property>
				</bean>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>
					The value of the target counter is
				</xsl:message>
				<bean class="org.springframework.batch.item.xml.StaxEventItemWriter"
					scope="step">
					<xsl:attribute name="id"><xsl:value-of
						select="concat('file-writer-',$source-counter,'-',$target-counter)" /></xsl:attribute>
					<!-- <property name="resource"> <xsl:attribute name="value"><xsl:value-of 
						select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{jobParameters[','target',$target-counter,'filename','] 
						!= null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos,'}')" 
						/></xsl:attribute> </property> -->


					<xsl:variable name="correctedResource">
						<xsl:value-of
							select="concat('jobParameters[','target',$target-counter,'filename','] != null ? jobParameters[','target',$target-counter,'filename','] : ',$apos,/batchDefinition/targets[$target-counter]/fileDetail/fileName,$apos)" />
					</xsl:variable>

					<property name="resource">
						<xsl:attribute name="value"><xsl:value-of
							select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{T(com.mcg.batch.utils.FileNameUtils).retrieveFileName(',$correctedResource,')}')" /></xsl:attribute>
					</property>

					<property name="rootTagName">
						<xsl:attribute name="value"><xsl:value-of
							select="/batchDefinition/targets[$target-counter]/fileDetail/xmlRootElementName" /></xsl:attribute>
					</property>
					<property name="marshaller">
						<bean class="org.springframework.oxm.jaxb.Jaxb2Marshaller">
							<property name="classesToBeBound">
								<list>
									<value>
										<xsl:value-of
											select="/batchDefinition/targets[$target-counter]/fileDetail/xmlClassName" />
									</value>
								</list>
							</property>
						</bean>
					</property>
				</bean>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Resuable template for flat file reader -->
	<xsl:template name="flat-file-reader">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />

		<xsl:choose>
			<xsl:when
				test="/batchDefinition/sources[$source-counter]/fileDetail/fileFormat='flatfile'">
				<bean class="org.springframework.batch.item.file.FlatFileItemReader"
					scope="step">
					<xsl:attribute name="id"><xsl:value-of
						select="concat('file-reader-',$source-counter,'-',$target-counter)" /></xsl:attribute>
					<!-- Read a delimited file -->
					<xsl:variable name="correctedResource">
						<xsl:value-of
							select="concat('jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos)" />
					</xsl:variable>

					<property name="resource">
						<xsl:attribute name="value"><xsl:value-of
							select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{T(com.mcg.batch.utils.FileNameUtils).retrieveFileName(',$correctedResource,')}')" /></xsl:attribute>
					</property>
					<!-- <xsl:attribute name="value"><xsl:value-of select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{jobParameters[','source',$source-counter,'filename','] 
						!= null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" 
						/></xsl:attribute> -->

					<property name="recordSeparatorPolicy">
						<bean
							class="com.mcg.batch.runtime.impl.item.readers.support.BlankLineRecordSeparatorPolicy" />
					</property>
					<xsl:if
						test="/batchDefinition/sources[$source-counter]/fileDetail/flatFileSkipHeaderRecord='true'">
						<property name="linesToSkip">
							<xsl:attribute name="value"><xsl:value-of
								select="1" /></xsl:attribute>
						</property>
						<property name="skippedLinesCallback">
							<bean
								class="com.mcg.batch.runtime.impl.item.readers.support.SkippedLineCallbackHandler" />
						</property>
					</xsl:if>
					<property name="lineMapper">
						<bean
							class="org.springframework.batch.item.file.mapping.DefaultLineMapper">
							<!-- split it -->
							<property name="lineTokenizer">
								<xsl:choose>
									<xsl:when
										test="string-length(/batchDefinition/sources[$source-counter]/fileDetail/flatFileDelimiter)!=0">
										<bean
											class="org.springframework.batch.item.file.transform.DelimitedLineTokenizer">
											<property name="names">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/flatFileFieldNames" /></xsl:attribute>
											</property>
											<property name="delimiter">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/flatFileDelimiter" /></xsl:attribute>
											</property>
										</bean>
									</xsl:when>
									<xsl:otherwise>
										<bean
											class="org.springframework.batch.item.file.transform.FixedLengthTokenizer">
											<property name="names">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/flatFileFieldNames" /></xsl:attribute>
											</property>
											<property name="columns">
												<xsl:attribute name="value"><xsl:value-of
													select="/batchDefinition/sources[$source-counter]/fileDetail/flatFileRecordFormat" /></xsl:attribute>
											</property>
										</bean>

									</xsl:otherwise>
								</xsl:choose>
							</property>
							<property name="fieldSetMapper">
								<!-- map to an object -->
								<bean
									class="org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper">
									<property name="prototypeBeanName">
										<xsl:attribute name="value"><xsl:value-of
											select="concat('prototype-bean-',$source-counter,'-',$target-counter)" /></xsl:attribute>
									</property>
								</bean>
							</property>
						</bean>
					</property>
				</bean>

				<bean scope="prototype">
					<xsl:attribute name="class"><xsl:value-of
						select="/batchDefinition/sources[$source-counter]/fileDetail/flatFileClassName" /></xsl:attribute>
					<xsl:attribute name="id"><xsl:value-of
						select="concat('prototype-bean-',$source-counter,'-',$target-counter)" /></xsl:attribute>
				</bean>
			</xsl:when>
			<xsl:otherwise>
				<bean class="org.springframework.batch.item.xml.StaxEventItemReader"
					scope="step">
					<xsl:attribute name="id"><xsl:value-of
						select="concat('file-reader-',$source-counter,'-',$target-counter)" /></xsl:attribute>
					<!-- <property name="resource"> <xsl:attribute name="value"><xsl:value-of 
						select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{jobParameters[','source',$source-counter,'filename','] 
						!= null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos,'}')" 
						/></xsl:attribute> </property> -->

					<xsl:variable name="correctedResource">
						<xsl:value-of
							select="concat('jobParameters[','source',$source-counter,'filename','] != null ? jobParameters[','source',$source-counter,'filename','] : ',$apos,/batchDefinition/sources[$source-counter]/fileDetail/fileName,$apos)" />
					</xsl:variable>

					<property name="resource">
						<xsl:attribute name="value"><xsl:value-of
							select="concat('file:','#{T(com.mcg.batch.core.BatchConfiguration).BATCH_VFS_LOCAL_TEMP_LOCATION}/#{T(com.mcg.batch.utils.ThreadContextUtils).getJobInstanceIdAsString()}','/','#{T(com.mcg.batch.utils.FileNameUtils).retrieveFileName(',$correctedResource,')}')" /></xsl:attribute>
					</property>
					<property name="fragmentRootElementName">
						<xsl:attribute name="value"><xsl:value-of
							select="/batchDefinition/sources[$source-counter]/fileDetail/xmlrootElementName" /></xsl:attribute>
					</property>
					<property name="unmarshaller">
						<bean class="org.springframework.oxm.jaxb.Jaxb2Marshaller">
							<property name="classesToBeBound">
								<list>
									<value>
										<xsl:value-of
											select="/batchDefinition/sources[$source-counter]/fileDetail/xmlclassName" />
									</value>
								</list>
							</property>
						</bean>
					</property>
				</bean>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Template to generate chunk -->
	<xsl:template name="chunk-template">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<xsl:param name="reader-value" />
		<xsl:param name="writer-value" />
		<xsl:param name="chunk-type" />
		<xsl:if
			test="/batchDefinition/targets[$target-counter]/databaseDetail/skipDuplicates='true'">
			<bean class="com.mcg.batch.core.support.ExceptionSkipPolicy">
				<xsl:attribute name="id"><xsl:value-of
					select="concat('skip-policy-',$source-counter,'-',$target-counter)" /></xsl:attribute>
				<constructor-arg>
					<xsl:attribute name="value"><xsl:value-of
						select="string('org.springframework.dao.DuplicateKeyException')" /></xsl:attribute>
				</constructor-arg>
			</bean>
		</xsl:if>
		<xsl:if
			test="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationOn='true'">
			<bean
				class="com.mcg.batch.runtime.impl.item.processors.MethodItemProcessor">
				<xsl:attribute name="id"><xsl:value-of
					select="concat('item-processor-',$source-counter,'-',$target-counter)" /></xsl:attribute>
				<property name="transformerClass">
					<xsl:attribute name="value"><xsl:value-of
						select="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationClass" /></xsl:attribute>
				</property>

				<property name="methodName">
					<xsl:attribute name="value"><xsl:value-of
						select="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationMethod" /></xsl:attribute>
				</property>
				<xsl:if
					test="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationParams">
					<property name="parameters">
						<xsl:attribute name="value"><xsl:value-of
							select="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationParams" /></xsl:attribute>
					</property>
				</xsl:if>
			</bean>
		</xsl:if>
		<bean>
			<xsl:attribute name="id"><xsl:value-of
				select="concat('chunk-reference-',$chunk-type,'-',$source-counter,'-',$target-counter)" /></xsl:attribute>
			<chunk>
				<xsl:attribute name="reader"><xsl:value-of select="$reader-value" /></xsl:attribute>
				<xsl:attribute name="writer"><xsl:value-of select="$writer-value" /></xsl:attribute>
				<xsl:attribute name="commit-interval"><xsl:value-of
					select="/batchDefinition/sources[$source-counter]/commitInterval" /></xsl:attribute>
				<xsl:if
					test="/batchDefinition/targets[$source-counter]/databaseDetail/skipDuplicates='true'">
					<xsl:attribute name="skip-policy"><xsl:value-of
						select="concat('skip-policy-',$source-counter,'-',$target-counter)" /></xsl:attribute>
				</xsl:if>
				<xsl:if
					test="/batchDefinition/targets[$source-counter]/transformationOptions[$source-counter]/itemTransformationOn='true'">
					<xsl:attribute name="processor"><xsl:value-of
						select="concat('item-processor-',$source-counter,'-',$target-counter)" /></xsl:attribute>
				</xsl:if>
			</chunk>
		</bean>
	</xsl:template>
	<xsl:template name="temp-file-cleanup">
		<xsl:param name="source-counter" />
		<xsl:param name="target-counter" />
		<xsl:param name="sourceDependent" />
		<bean scope="step" lazy-init="true">
			<!-- Generate the adapter name... -->
			<!-- <xsl:attribute name="id"><xsl:value-of select="concat('tasklet-reference-','temp-file-cleanup-',$source-counter,'-',$target-counter)" 
				/></xsl:attribute> -->
			<!-- Sandeep Start -->
			<xsl:if test="$sourceDependent='true'">

				<xsl:attribute name="id">
							<xsl:value-of
					select="concat('tasklet-reference-','temp-file-cleanup-',$source-counter,'-',$target-counter ,'-','sourceDependent')" />
						</xsl:attribute>

			</xsl:if>
			<xsl:if test="$sourceDependent != 'true'">

				<xsl:attribute name="id">
							<xsl:value-of
					select="concat('tasklet-reference-','temp-file-cleanup-',$source-counter,'-',$target-counter)" />
						</xsl:attribute>

			</xsl:if>
			<!-- Sandeep End -->
			<xsl:attribute name="class">com.mcg.batch.runtime.impl.tasklet.TempFileCleanUp</xsl:attribute>

		</bean>

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