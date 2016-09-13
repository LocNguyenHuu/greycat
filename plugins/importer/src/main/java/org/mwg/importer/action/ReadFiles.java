package org.mwg.importer.action;

import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class ReadFiles extends AbstractTaskAction {

    private final String _pathOrTemplate;

    public ReadFiles(String _pathOrTemplate) {
        this._pathOrTemplate = _pathOrTemplate;
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult next = context.wrap(null);
        final String path = context.template(_pathOrTemplate);
        if (path == null) {
            throw new RuntimeException("Variable " + _pathOrTemplate + " does not exist in the context");
        }
        File file = null;
        try {
            file = new File(URLDecoder.decode(path, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            //should never append
        }
        if (!file.exists()) {
            URL url = this.getClass().getClassLoader().getResource(path);
            if (url == null) {
                throw new RuntimeException("File " + path + " does not exist and it is not present in resources directory.");
            }
            file = new File(url.getPath());
        }
        if (file.isDirectory()) {
            final File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    next.add(listFiles[i].getAbsolutePath());
                }
            }
        } else {
            next.add(file.getAbsolutePath());
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "readFiles(\'" + _pathOrTemplate + "\')";
    }


}