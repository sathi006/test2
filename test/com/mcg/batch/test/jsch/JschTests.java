package com.mcg.batch.test.jsch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JschTests {
    
    public static void sftpConnection() throws Exception {    
	    // Object Declaration.
	    JSch.setLogger(new TestLogger());
	    JSch jsch = new JSch();
	    Session session = null;
	    Channel channel = null;

	    // Variable Declaration.
	    String user = "ftpsuser";
	    String host = "mcanab01.eur.ad.sag";
	    Integer port = 4242;
	    String password = "ftpsuser";
	    String watchFolder = "\\localhost\textfiles";
	    String outputDir = "/remote/textFolder/";
	    String filemask = "*.txt";
	    ChannelSftp sftpChannel = null;

	   try {
	        session = jsch.getSession(user, host, port);

	        /*
	         * StrictHostKeyChecking Indicates what to do if the server's host 
	         * key changed or the server is unknown. One of yes (refuse connection), 
	         * ask (ask the user whether to add/change the key) and no 
	         * (always insert the new key).
	         */
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(password);
	        session.setTimeout(30000);
	        session.connect();

	        channel = session.openChannel("psexec");
	        channel.connect();
	        sftpChannel = (ChannelSftp)channel;

	        // Go through watch folder looking for files.
	        File[] files = findFile(watchFolder, filemask);
	        for(File file : files) {
	            // Upload file.
	            putFile(file, sftpChannel, outputDir);            
	        }                 
	    } finally {
	      //  sftpChannel.exit();
	        session.disconnect();
	    }
	}

	public static void putFile(File file, ChannelSftp sftpChannel, String outputDir) throws Exception {

	    FileInputStream fis = null;

	    try {
	        // Change to output directory.
	        sftpChannel.cd(outputDir);

	        // Upload file.

	        fis = new FileInputStream(file);
	        sftpChannel.put(fis, file.getName());
	        fis.close();

	    } finally {
		if (fis != null) {
		    fis.close();
		}
	    }
	}

	public static File[] findFile(String dirName, final String mask) {
	    File dir = new File(dirName);

	    return dir.listFiles(new FilenameFilter() {
	        public boolean accept(File dir, String filename)
	            { return filename.endsWith(mask); }
	    } );
	}
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	try {
	    JschTests.sftpConnection();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
