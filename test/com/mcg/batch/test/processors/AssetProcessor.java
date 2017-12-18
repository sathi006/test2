package com.mcg.batch.test.processors;

import com.mcg.batch.data.Asset;



public class AssetProcessor {
    public Object processAsset(Object input) throws Exception {
	if (input instanceof Asset) {
	    if (((Asset) input).getAssetId().equals("assetId-100")) {
		throw new Exception ("Asset Id 100 received.!!");
	    }
	}
	return input;
    }
}
