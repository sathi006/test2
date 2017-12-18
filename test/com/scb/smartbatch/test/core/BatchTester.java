/**
 * 
 */
package com.mcg.batch.test.core;

import static com.mcg.batch.core.BatchConfiguration.BATCH_DEPENDENT_ADAPTERS_LABEL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.mcg.batch.adapter.AdapterDefinitionBean;
import com.mcg.batch.core.kernel.SmartBatchKernel;
import com.mcg.batch.utils.StringHelper;
import com.wm.util.event.Event;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class BatchTester {

	private static String batchXML = "sample-batch.xml";
	private static String batchName = "sample-batch";

	public static void main(String[] args) throws Exception {
		SmartBatchKernel kernel = SmartBatchKernel.getInstance();

		try {
			// System.out.println(kernel.invoke("getAllBatchesNames", Set.class,
			// "default"));
			// System.out.println(kernel.invoke("getAllAdapterDefinitions",
			// Set.class, "default"));
			// List<String> list = new ArrayList<String>(kernel.invoke(
			// "getAllBatchesNames", Set.class, "default"));
			//
			// list.add("fm.BF1.GLBL.APP1.Z97041");
			// list.add("fm.BF2.GLBL.APP2.F90361");
			// list.add("fm.test.test.test.F89458");
			// list.add("fm.BF3.GLBL.APP3.U39596");
			// list.add("fm.Batch182D-2D.India.Smartbatch.L32363");
			// list.add("fm.Batch6M-M.India.Smartbatch.G68222");
			// list.add("fm.Batch5D-F.India.Smartbatch.I14459");
			// kernel.invoke("exportBuild", String.class, "default", list,
			// "c:\\tmp\\export");
			// String fileName = "c:\\tmp\\export\\export_1420607677493.zip";
			// kernel.invoke("importBuild", null, "default", fileName);
			// dbToCsv(kernel);

//			Long id = kernel
//					.invoke("instantiateNewBatch",
//							Long.class,
//							"default",
//							"fm.BF2.GLBL.APP2.F90361",
//							"source-1-file-name=#{T(com.mcg.batch.test.core.TestParameter).getFileName()},test=123,current.TimeStamp="
//									+ System.currentTimeMillis());
			
//			System.out.println(id);
			 System.out.println(kernel.invoke("getAllBatchesNames", Set.class,
						 "default"));
		} finally {
//			kernel.shutdown();
		}

	}

	private static void startJob(SmartBatchKernel kernel) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		kernel.invoke("registerNewBatchfromXML", null, "default", batchName,

		StringHelper.getFile(batchXML), StringHelper.getFile(batchXML), map);
		for (int i = 0; i < 1; i++) {

			kernel.invoke("instantiateNewBatch", Event.class, "default",
					batchName,
					"current.TimeStamp=" + System.currentTimeMillis());
		}

	}

	public static void restartJob(String id) throws Exception {
		SmartBatchKernel kernel = SmartBatchKernel.getInstance();

		kernel.invoke("restartIfFailed", String.class, "default", id);

		kernel.shutdown();
	}

	public static void testLocalLocal(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "local-local-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"local-local-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-local-local-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"local-local-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-local-local-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "local-local-src-resource",
				"local-local-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-local-local-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-local-local-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void testFTPLocal(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "ftp-local-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"ftp-local-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-ftp-local-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"ftp-local-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-ftp-local-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "ftp-local-src-resource",
				"ftp-local-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-ftp-local-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-ftp-local-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void testFTPFTP(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "ftp-ftp-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"ftp-ftp-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-ftp-ftp-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"ftp-ftp-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-ftp-ftp-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "ftp-ftp-src-resource",
				"ftp-ftp-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-ftp-ftp-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-ftp-ftp-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void testSFTPLocal(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "sftp-local-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"sftp-local-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-sftp-local-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"sftp-local-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-sftp-local-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "sftp-local-src-resource",
				"sftp-local-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-sftp-local-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-sftp-local-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void testSFTPSFTP(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "sftp-sftp-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"sftp-sftp-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-sftp-sftp-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"sftp-sftp-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-sftp-sftp-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "sftp-sftp-src-resource",
				"sftp-sftp-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-sftp-sftp-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-sftp-sftp-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void testHTTPSFTP(SmartBatchKernel kernel) throws Exception {
		String f2fBatchName = "http-sftp-test-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"http-sftp-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-http-sftp-src.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"http-sftp-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\vfs-adapter-http-sftp-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);

		String[] dependedntAdapters = { "http-sftp-src-resource",
				"http-sftp-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-http-sftp-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\vfs-http-sftp-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void rsyncLocal2Local(SmartBatchKernel kernel)
			throws Exception {
		String f2fBatchName = "rsync-local-local-test";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"rsync-local-local-src-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\rsync-adapter-local-local-src.xml"),
				null, null);

		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"rsync-local-local-tgt-resource",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\rsync-adapter-local-local-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		System.out.println();

		String[] dependedntAdapters = { "rsync-local-local-src-resource",
				"rsync-local-local-tgt-resource" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				f2fBatchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\rsync-local-local-file.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\rsync-local-local-file.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Event.class, "default",
				f2fBatchName, "current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void csvToDb(SmartBatchKernel kernel) throws Exception {
		String batchName = "csv-db-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"csv-jdbc-src",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\csv-jdbc-src.xml"),
				null, null);

		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"csv-jdbc-tgt",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\csv-jdbc-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		System.out.println();

		String[] dependedntAdapters = { "csv-jdbc-src", "csv-jdbc-tgt" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				batchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\csv-db.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\csv-db.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Long.class, "default", batchName,
				"current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void dbToCsv(SmartBatchKernel kernel) throws Exception {
		String batchName = "db-csv";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"jdbc-csv-src",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\jdbc-csv-src.xml"),
				null, null);

		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"jdbc-csv-tgt",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\jdbc-csv-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		System.out.println();

		String[] dependedntAdapters = { "jdbc-csv-src", "jdbc-csv-tgt" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				batchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\db-csv.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\db-csv.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Long.class, "default", batchName,
				"current.TimeStamp=" + System.currentTimeMillis());
	}

	public static void xmlToDb(SmartBatchKernel kernel) throws Exception {
		String batchName = "xml-db-batch";
		AdapterDefinitionBean bean = new AdapterDefinitionBean(
				"xml-jdbc-src",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\xml-jdbc-src.xml"),
				null, null);

		kernel.invoke("upsertAdapterBean", null, "default", bean);
		bean = new AdapterDefinitionBean(
				"xml-jdbc-tgt",
				StringHelper
						.getFile(".\\resources\\test\\adapters\\xml-jdbc-tgt.xml"),
				null, null);
		kernel.invoke("upsertAdapterBean", null, "default", bean);
		System.out.println();

		String[] dependedntAdapters = { "xml-jdbc-src", "xml-jdbc-tgt" };

		HashMap<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("environment", "dev");
		map.put("domain", "sag");
		map.put(BATCH_DEPENDENT_ADAPTERS_LABEL, dependedntAdapters);
		kernel.invoke(
				"registerNewBatchfromXML",
				null,
				"default",
				batchName,
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\xml-db.xml"),
				StringHelper
						.getFile("C:\\Workspace\\eclipse\\scb-smart-batch-framework\\smart-batch-framework\\resources\\test\\batch\\xml-db.xml"),
				map);

		kernel.invoke("instantiateNewBatch", Long.class, "default", batchName,
				"current.TimeStamp=" + System.currentTimeMillis());
	}
}
