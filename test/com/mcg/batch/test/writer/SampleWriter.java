/**
 * 
 */
package com.mcg.batch.test.writer;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.mcg.batch.utils.IOHelper;
import com.mcg.batch.utils.ThreadContextUtils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
@Component
public class SampleWriter implements ItemWriter<String> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends String> stringList) throws Exception {
		File file = new File("c:\\test\\"
				+ ThreadContextUtils.getJobExecutionId() + ".log");
		RandomAccessFile fos = new RandomAccessFile(file, "rw");
//		System.out.println("Writer Called" + stringList);

		try {
			fos.seek(file.length());
			for (String str : stringList) {

				fos.write(str.getBytes());
				fos.write(("\n".getBytes()));

			}
		} finally {
			IOHelper.close(fos);
			fos = null;
		}

	}
}
