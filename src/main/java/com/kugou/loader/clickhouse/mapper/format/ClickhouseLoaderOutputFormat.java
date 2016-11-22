package com.kugou.loader.clickhouse.mapper.format;

import com.kugou.loader.clickhouse.mapper.ClickhouseJDBCConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;

/**
 * Created by jaykelin on 2016/11/15.
 */
public class ClickhouseLoaderOutputFormat extends OutputFormat<NullWritable, Text> {


    @Override
    public RecordWriter<NullWritable, Text> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new RecordWriter<NullWritable, Text>() {
            public void write(NullWritable key, Text value) {
            }

            public void close(TaskAttemptContext context) {
            }
        };
    }

    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return null;
    }

    class ClickhouseLoaderOutputCommitter extends  OutputCommitter{

        private final Log log = LogFactory.getLog(ClickhouseLoaderOutputCommitter.class);
        private String tempTable;

        @Override
        public void setupJob(JobContext jobContext) throws IOException {

        }

        @Override
        public void setupTask(TaskAttemptContext taskAttemptContext) throws IOException {

        }

        @Override
        public boolean needsTaskCommit(TaskAttemptContext taskAttemptContext) throws IOException {
            return true;
        }

        @Override
        public void commitTask(TaskAttemptContext taskAttemptContext) throws IOException {

        }

        @Override
        public void abortTask(TaskAttemptContext taskAttemptContext) throws IOException {

        }

        /**
         * 创建临时表
         * @param configuration
         * @param statement
         * @param ddl
         * @param tries
         * @param cause
         * @throws IOException
         */
        private void createTempTable(ClickhouseJDBCConfiguration configuration, Statement statement,
                                     String ddl, int tries, Throwable cause) throws IOException{
            log.info("Clickhouse JDBC : create temp table["+ddl+"]");
            try {
                if(null == ddl){
                    throw new IllegalArgumentException("Clickhouse JDBC : create table dll cannot be null.");
                }
                if(tries <= configuration.getMaxTries()){
                    statement.executeUpdate(ddl);
                }else{
                    throw new IOException("Clickhouse JDBC : create temp table[temp."+this.tempTable+"] failed.", cause);
                }
            } catch (SQLException e) {
                log.warn("Clickhouse JDBC : Create temp table failed. tries : "+tries+" : "+e.getMessage(), e);
                try {
                    Thread.sleep((tries+1) * 1000l);
                } catch (InterruptedException e1) {
                }
                createTempTable(configuration, statement, ddl, tries + 1, e.getCause());
            }
        }
    }

}