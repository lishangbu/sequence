package com.power4j.kit.seq.persistent.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

/**
 * Mysql 测试
 * <p>
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class MySqlSynchronizerTest {

	private final static String SEQ_TABLE = "tb_seq";

	private final static String JDBC_URL = "jdbc:mysql://localhost:3306/seq_test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	private MySqlSynchronizer mySqlSynchronizer;

	private static DataSource getDataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(JDBC_URL);
		config.setUsername("root");
		config.setPassword("root");
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		return new HikariDataSource(config);
	}

	@Before
	public void prepare() {
		mySqlSynchronizer = new MySqlSynchronizer(SEQ_TABLE, getDataSource());
		mySqlSynchronizer.createTable();
	}

	@After
	public void teardown() {
		mySqlSynchronizer.dropTable();
	}

	@Test
	public void simpleTest() {
		SynchronizerTestCase.simpleTest(mySqlSynchronizer);
	}

	@Test
	public void multipleThreadUpdateTest() {
		SynchronizerTestCase.multipleThreadUpdateTest(mySqlSynchronizer);
	}

	@Test
	public void multipleThreadAddTest() {
		SynchronizerTestCase.multipleThreadAddTest(mySqlSynchronizer);
	}

}