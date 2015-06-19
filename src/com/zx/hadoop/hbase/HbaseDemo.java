package com.zx.hadoop.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

public class HbaseDemo {
	private Configuration conf = null;
	@Before
	public void init() {
		conf = HBaseConfiguration.create();
		//连接zk
		conf.set("hbase.zookeeper.quorum", "cent04:2181,cent05:2181,cent06:2181");
	}
	
	@Test
	public void testPut() throws Exception{
		HTable hTable = new HTable(conf, "peoples");
		Put put=new Put(Bytes.toBytes("kr00001"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes("Koke"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("age"), Bytes.toBytes("24000"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("money"), Bytes.toBytes(12));
		
		hTable.put(put);
		hTable.close();
	}

	//插入1000001条
	@Test
	public void testPutAll() throws Exception {
		HTable hTable = new HTable(conf, "peoples");
		//直接给出list大小
		List<Put> puts=new ArrayList<Put>(1000);
		for (int i = 1; i <= 100001; i++) {
			Put put=new Put(Bytes.toBytes("zx"+i));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("name"), Bytes.toBytes("Koke"+i));
			put.add(Bytes.toBytes("data"), Bytes.toBytes("monet"), Bytes.toBytes(""+i));
			puts.add(put);
			// 每1000条插入一次
			if (i%1000==0) {
				hTable.put(puts);
				puts = new ArrayList<Put>(1000);
			}
		}
		hTable.put(puts);
		hTable.close();
	}
	
	@Test
	public void testGet() throws IOException {
		HTable hTable = new HTable(conf, "peoples");
		Get get = new Get(Bytes.toBytes("zx100001"));
		Result result = hTable.get(get); 
		byte[] value = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"));
		System.out.println(Bytes.toString(value));
		hTable.close();
	}
	
	@Test
	public void testScanner() throws Exception {
		HTable hTable = new HTable(conf, "peoples");
		Scan scan = new Scan(Bytes.toBytes("zx29990"), Bytes.toBytes("zx30000"));
		ResultScanner scanner = hTable.getScanner(scan);
		for (Result result : scanner) {
			byte[] value = result.getValue(Bytes.toBytes("data"), Bytes.toBytes("monet"));
			System.out.println(Bytes.toString(value));
		}
		hTable.close();
	}
	@Test
	public void testDel() throws Exception {
		HTable hTable = new HTable(conf, "peoples");
		Delete delete = new Delete(Bytes.toBytes("zx100001"));
		hTable.delete(delete);
		hTable.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = HBaseConfiguration.create();
		//连接zk
		conf.set("hbase.zookeeper.quorum", "cent04:2181,cent05:2181,cent06:2181");
		HBaseAdmin admin = new HBaseAdmin(conf);
		
		HTableDescriptor htd = new HTableDescriptor(TableName.valueOf("peoples"));
		HColumnDescriptor hcd = new HColumnDescriptor("info");
		hcd.setMaxVersions(3);
		HColumnDescriptor hcd2 = new HColumnDescriptor("data");
		htd.addFamily(hcd);
		htd.addFamily(hcd2);
		admin.createTable(htd);
		admin.close();
	}

}
