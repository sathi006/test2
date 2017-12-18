/**
 * 
 */
package com.mcg.batch.test.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

import com.mcg.batch.core.kernel.SmartBatchKernel;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public class RedisFailoverTest {
	/**
	 * Logger to be used by this class.
	 */

	public static void main(String[] args) throws Exception {
		SmartBatchKernel kernel = SmartBatchKernel.getInstance();
		try {
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader bfr = new BufferedReader(isr);

			while (!"quit".equalsIgnoreCase(bfr.readLine())) {
				try {
					System.out.println(kernel.invoke("getAllBatchesNames",
							Set.class, "default"));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} finally {
			kernel.shutdown();
		}
	}
}
