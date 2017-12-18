package com.mcg.batch.test.jsch;

import com.jcraft.jsch.Logger;

public class TestLogger implements Logger {

    @Override
    public boolean isEnabled(int arg0) {
	// TODO Auto-generated method stub
	return true;
    }

    @Override
    public void log(int arg0, String arg1) {
	// TODO Auto-generated method stub
	System.out.println(arg0 + " ------ " + arg1);
    }

}
