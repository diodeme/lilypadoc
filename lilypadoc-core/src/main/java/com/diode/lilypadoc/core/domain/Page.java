package com.diode.lilypadoc.core.domain;

import com.diode.lilypadoc.standard.common.Result;

import java.io.File;

public interface Page {

    Result<File> parseAndSync();
}
