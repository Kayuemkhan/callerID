package com.chromatics.caller_id;

import java.io.IOException;
import java.io.InputStream;

public interface Storage {

    String getDataDirPath();

    String getCacheDirPath();

    InputStream openFile(String fileName, boolean internal) throws IOException;

}
