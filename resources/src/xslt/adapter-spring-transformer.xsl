<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Version 1.0 This xslt is used to transform the ui adapter defintion 
		bean to spring based xml configuration. -->
	<xsl:variable name="apos" select="string('&quot;')" />
	<xsl:variable name="smallcase" select="'true'"/>
	<xsl:variable name="uppercase" select="'TRUE'"/>
	<xsl:template match="/">
		<beans xmlns="http://www.springframework.org/schema/beans"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:batch="http://www.springframework.org/schema/batch"
			xmlns:context="http://www.springframework.org/schema/context"
			xmlns:jdbc="http://www.springframework.org/schema/jdbc"
			xsi:schemaLocation="http://www.springframework.org/schema/batch http://www.springframework.org/schema/batch/spring-batch-2.1.xsd 
			http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-3.0.xsd 
			http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
			http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">

			<xsl:if test="/adapterDefinition/name">
				<xsl:choose>
					<!-- The transformation for the rsync Adapter -->
					<xsl:when test="/adapterDefinition/rsyncAdapterDetail/remoteHostName">
						<bean>
							<xsl:attribute name="id">
							<xsl:value-of select="/adapterDefinition/name" />
						</xsl:attribute>
							<xsl:attribute name="class">com.mcg.batch.adapters.impl.support.rsync.RsyncAdapterResource</xsl:attribute>
							<property name="adapterName">
								<xsl:attribute name="value">
							<xsl:value-of select="/adapterDefinition/name"></xsl:value-of>
							</xsl:attribute>
							</property>
							<property name="host">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/rsyncAdapterDetail/remoteHostName" /></xsl:attribute>
							</property>
							<property name="userName">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/rsyncAdapterDetail/remoteHostUserName" /></xsl:attribute>
							</property>
							<xsl:if test="/adapterDefinition/rsyncAdapterDetail/sshKeyFilePath">
								<property name="sshKeyFilePath">
									<xsl:attribute name="value"><xsl:value-of
										select="/adapterDefinition/rsyncAdapterDetail/sshKeyFilePath" /></xsl:attribute>
								</property>
							</xsl:if>
							<property name="path">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/rsyncAdapterDetail/directoryPath" /></xsl:attribute>
							</property>

							<property name="parameters">
								<map>
								<xsl:if
										test="/adapterDefinition/rsyncAdapterDetail/additionalParameters[name='remedy.group']">
										<entry key="remedy.group">
										<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/rsyncAdapterDetail/additionalParameters[name='remedy.group']/value" /></xsl:attribute>
                                        </entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/rsyncAdapterDetail/additionalParameters[name='support.mail.id']">
										<entry key="support.mail.id">
										<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/rsyncAdapterDetail/additionalParameters[name='support.mail.id']/value" /></xsl:attribute>
                                        </entry>
									</xsl:if>
								</map>
							</property>

						</bean>
					</xsl:when>
					<!-- The transformation for the vfs Adapter -->
					<xsl:when test="/adapterDefinition/vfsAdapterDetail/protocol">
						<bean>
							<xsl:attribute name="id">
							<xsl:value-of select="/adapterDefinition/name" />
						</xsl:attribute>
							<xsl:attribute name="class">com.mcg.batch.adapters.impl.support.vfs.VFSResource</xsl:attribute>
							<property name="resourceAlias">
								<xsl:attribute name="value">
							<xsl:value-of select="/adapterDefinition/name"></xsl:value-of> </xsl:attribute>
							</property>
							<property name="fileName">
								<!-- Differentiate b/w local file and wired protocol file access -->
								<xsl:choose>
									<xsl:when
										test="/adapterDefinition/vfsAdapterDetail/protocol='file' or /adapterDefinition/vfsAdapterDetail/protocol='gz' or  /adapterDefinition/vfsAdapterDetail/protocol='bz2'">
										<bean
											class="com.mcg.batch.adapters.impl.support.vfs.LocalFileName">
											<constructor-arg name="scheme" value="file" />
											<constructor-arg name="rootFile" value="/" />
											<constructor-arg name="path">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/vfsAdapterDetail/directoryPath" /></xsl:attribute>
											</constructor-arg>
										</bean>
									</xsl:when>
									<xsl:otherwise>
										<bean
											class="com.mcg.batch.adapters.impl.support.vfs.GenericFileName">
											<constructor-arg name="scheme">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/vfsAdapterDetail/protocol" /></xsl:attribute>
											</constructor-arg>

											<constructor-arg name="hostName">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/vfsAdapterDetail/hostName" /></xsl:attribute>
											</constructor-arg>
											<constructor-arg name="port">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/vfsAdapterDetail/port" /></xsl:attribute>
											</constructor-arg>
											<xsl:if
												test="string-length(/adapterDefinition/vfsAdapterDetail/userName) !=0">
												<constructor-arg name="userName">
													<xsl:attribute name="value"><xsl:value-of
														select="/adapterDefinition/vfsAdapterDetail/userName" /></xsl:attribute>
												</constructor-arg>
											</xsl:if>
											<xsl:if
												test="string-length(/adapterDefinition/vfsAdapterDetail/password) !=0">
												<constructor-arg name="password">
													<xsl:attribute name="value"><xsl:value-of
														select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,/adapterDefinition/vfsAdapterDetail/password,$apos,')}')" /></xsl:attribute>
												</constructor-arg>
											</xsl:if>
											<constructor-arg name="path">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/vfsAdapterDetail/directoryPath" /></xsl:attribute>
											</constructor-arg>
										</bean>
									</xsl:otherwise>
								</xsl:choose>
							</property>
							<property name="parameters">
								<map>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ssh.host.key.file']">
										<entry key="ssh.host.key.file">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ssh.host.key.file']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.host']">
										<entry key="http.proxy.host">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.host']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.port']">
										<entry key="http.proxy.port">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.port']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.user']">
										<entry key="http.proxy.auth.user">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.user']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.password']">
										<entry key="http.proxy.auth.password">
											<xsl:attribute name="value"><xsl:value-of
												select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.password']/value,$apos,')}')" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.domain']">
										<entry key="http.proxy.auth.domain">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='http.proxy.auth.domain']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='domain']">
										<entry key="NT_DOMAIN">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='domain']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='vfs.target.fs.usage.threshold']">
										<entry key="vfs.target.fs.usage.threshold">
											<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='vfs.target.fs.usage.threshold']/value" /></xsl:attribute>
										</entry>
									</xsl:if>
									<!-- <xsl:if
										test="translate(/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.passive']/value, $smallcase, $uppercase) = 'true'">
										<entry key="ftps.passive">
										<xsl:attribute name="value">true</xsl:attribute>
                                        </entry>
									</xsl:if>
									<xsl:if
										test="translate(/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.implicit.enabled']/value, $smallcase, $uppercase) = 'true'">
										<entry key="ftps.type">
										<xsl:attribute name="value">implicit</xsl:attribute>
                                        </entry>
									</xsl:if> -->
									
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='remedy.group']">
										<entry key="remedy.group">
										<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='remedy.group']/value" /></xsl:attribute>
                                        </entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='support.mail.id']">
										<entry key="support.mail.id">
										<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='support.mail.id']/value" /></xsl:attribute>
                                        </entry>
									</xsl:if>
									<!-- <xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.keystore.path']">
										<entry key="ftps.keystore.path">
										<xsl:attribute name="value"><xsl:value-of
												select="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.keystore.path']/value" /></xsl:attribute>
                                        </entry>
									</xsl:if>
									<xsl:if
										test="/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.keystore.password']">
										<entry key="ftps.keystore.password">
										<xsl:attribute name="value"><xsl:value-of
												select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,/adapterDefinition/vfsAdapterDetail/additionalParameters[name='ftps.keystore.password']/value,$apos,')}')"  /></xsl:attribute>
                                        </entry>
									</xsl:if> -->
									
									
									
									
								</map>
							</property>
						</bean>
					</xsl:when>
					<xsl:when test="/adapterDefinition/messageAdapterDetail/url">
						<bean class="org.springframework.jms.core.JmsTemplate">
							<xsl:attribute name="id"><xsl:value-of
								select="/adapterDefinition/name" /></xsl:attribute>
							<property name="connectionFactory">
								<bean
									class="org.springframework.jms.connection.CachingConnectionFactory">
									<constructor-arg>
										<bean class="org.springframework.jndi.JndiObjectFactoryBean">
											<property name="jndiTemplate">
												<bean class="org.springframework.jndi.JndiTemplate"
													lazy-init="true">
													<property name="environment">
														<props>
															<xsl:for-each
																select="/adapterDefinition/messageAdapterDetail/additionalParameters">
																<prop>
																	<xsl:attribute name="key"><xsl:value-of
																		select="name" /></xsl:attribute>
																	<xsl:choose>
																		<xsl:when test="name='java.naming.security.credentials'">
																			<xsl:value-of
																				select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,value,$apos,')}')" />
																		</xsl:when>
																		<xsl:otherwise>
																			<xsl:value-of select="value" />
																		</xsl:otherwise>
																	</xsl:choose>
																</prop>
															</xsl:for-each>
															<prop key="java.naming.factory.initial">
																<xsl:value-of
																	select="/adapterDefinition/messageAdapterDetail/initialContextFactory" />
															</prop>
															<prop key="java.naming.provider.url">
																<xsl:value-of
																	select="/adapterDefinition/messageAdapterDetail/url" />
															</prop>

														</props>
													</property>
												</bean>
											</property>
											<property name="jndiName">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/messageAdapterDetail/connectionFactory" /></xsl:attribute>
											</property>
											<property name="cache">
												<xsl:attribute name="value"><xsl:value-of
													select="/adapterDefinition/messageAdapterDetail/cacheSessions" /></xsl:attribute>
											</property>
											<property name="proxyInterface" value="javax.jms.ConnectionFactory" />
										</bean>
									</constructor-arg>
								</bean>
							</property>
							<property name="destinationResolver">
								<bean
									class="org.springframework.jms.support.destination.JndiDestinationResolver">
									<property name="jndiEnvironment">
										<props>
											<xsl:for-each
												select="/adapterDefinition/messageAdapterDetail/additionalParameters">
												<prop>
													<xsl:attribute name="key"><xsl:value-of
														select="name" /></xsl:attribute>
													<xsl:choose>
														<xsl:when test="name='java.naming.security.credentials'">
															<xsl:value-of
																select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,value,$apos,')}')" />
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="value" />
														</xsl:otherwise>
													</xsl:choose>
												</prop>
											</xsl:for-each>
											<prop key="java.naming.factory.initial">
												<xsl:value-of
													select="/adapterDefinition/messageAdapterDetail/initialContextFactory" />
											</prop>
											<prop key="java.naming.provider.url">
												<xsl:value-of select="/adapterDefinition/messageAdapterDetail/url" />
											</prop>
										</props>
									</property>
								</bean>
							</property>
							<property name="receiveTimeout">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/messageAdapterDetail/defaultReceiveTimeout" /></xsl:attribute>
							</property>
							<property name="sessionTransacted">
								<xsl:attribute name="value">true</xsl:attribute>
							</property>
						</bean>
					</xsl:when>
					<xsl:when
						test="/adapterDefinition/databaseAdapterDetail/driverClassName">
						<bean class="org.apache.commons.dbcp.BasicDataSource">
							<xsl:attribute name="id"><xsl:value-of
								select="/adapterDefinition/name" /></xsl:attribute>
							<property name="driverClassName">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/databaseAdapterDetail/driverClassName" /></xsl:attribute>
							</property>
							<property name="url">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/databaseAdapterDetail/dbURL" /></xsl:attribute>
							</property>
							<property name="username">
								<xsl:attribute name="value"><xsl:value-of
									select="/adapterDefinition/databaseAdapterDetail/userName" /></xsl:attribute>
							</property>
							<property name="password">
								<xsl:attribute name="value"><xsl:value-of
									select="concat('#{T(com.mcg.batch.runtime.impl.batch.utils.EncryptorFactory).doTextDecryption(',$apos,/adapterDefinition/databaseAdapterDetail/password,$apos,')}')" /></xsl:attribute>
							</property>
							<xsl:for-each
								select="/adapterDefinition/databaseAdapterDetail/additionalParameters">
								<property>
									<xsl:attribute name="name"><xsl:value-of
										select="name" /></xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="value" /></xsl:attribute>
								</property>
							</xsl:for-each>
						</bean>
					</xsl:when>
				</xsl:choose>
				<bean class="com.mcg.batch.adapter.Retryer" scope="prototype">
					<xsl:attribute name="id"><xsl:value-of
						select="concat(/adapterDefinition/name,'-retryer')" /></xsl:attribute>
					<property name="retryLimit">
						<xsl:attribute name="value"><xsl:value-of
							select="/adapterDefinition/retryCount" /></xsl:attribute>
					</property>
					<property name="retryInterval">
						<xsl:attribute name="value"><xsl:value-of
							select="/adapterDefinition/retryInterval" /></xsl:attribute>
					</property>
				</bean>
			</xsl:if>
		</beans>
	</xsl:template>
</xsl:stylesheet>