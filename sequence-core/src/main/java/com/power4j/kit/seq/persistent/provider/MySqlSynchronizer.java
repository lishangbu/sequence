package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.utils.Pair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * MySql支持
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/1
 * @since 1.0
 */
@Slf4j
@AllArgsConstructor
public class MySqlSynchronizer implements SeqSynchronizer {

	private final static String MYSQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME("
			+ "seq_name VARCHAR(32) NOT NULL," + "seq_piece VARCHAR(255) NOT NULL," + "seq_next_value BIGINT NOT NULL,"
			+ "seq_create_time TIMESTAMP NOT NULL," + "seq_update_time TIMESTAMP NULL,"
			+ "PRIMARY KEY (`seq_name`,`seq_piece`)" + ")";

	private final static String MYSQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME";

	private final static String MYSQL_INSERT_IGNORE = "INSERT IGNORE INTO $TABLE_NAME"
			+ "(seq_name,seq_piece,seq_next_value,seq_create_time,seq_update_time)" + " VALUE(?,?,?,?,?)";

	private final static String MYSQL_UPDATE_VALUE = "UPDATE $TABLE_NAME SET seq_next_value=?,seq_update_time=? "
			+ "WHERE seq_name=? AND seq_piece=? AND seq_next_value=?";

	private final static String MYSQL_SELECT_VALUE = "SELECT seq_next_value FROM $TABLE_NAME WHERE seq_name=? AND seq_piece=?";

	private final String tableName;

	private final DataSource dataSource;

	public void createTable() {
		log.info("create table if not exists : {}", tableName);
		try (Connection connection = getConnection();
				PreparedStatement statement = connection
						.prepareStatement(MYSQL_CREATE_TABLE.replace("$TABLE_NAME", tableName))) {
			statement.executeUpdate();
		}
		catch (SQLException e) {
            log.warn(e.getMessage(),e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	public void dropTable() {
		log.warn("drop table if exists : {}", tableName);
		try (Connection connection = getConnection();
				PreparedStatement statement = connection
						.prepareStatement(MYSQL_DROP_TABLE.replace("$TABLE_NAME", tableName))) {
			statement.executeUpdate();
		}
		catch (SQLException e) {
            log.warn(e.getMessage(),e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public boolean tryCreate(String name, String piece, long nextValue) {
		try  (Connection connection = getConnection()) {
			return createIgnoreInternal(connection,name, piece, nextValue);
		}
		catch (SQLException e) {
            log.warn(e.getMessage(),e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public boolean tryUpdate(String name, String piece, long nextValueOld, long nextValueNew) {
		try  (Connection connection = getConnection()) {
			return updateInternal(connection,name, piece, nextValueOld, nextValueNew);
		}
		catch (SQLException e) {
            log.warn(e.getMessage(),e);
			throw new SeqException(e.getMessage(), e);
		}
	}

    @Override
    public Optional<Pair<Long,Long>> tryAddAndGet(String name, String piece, int addend, int retry) {
	    final boolean noLimit = retry < 0;
        try  (Connection connection = getConnection()) {
            do{
                long lastValue = selectInternal(connection,name, piece).get();
                final long target = lastValue + addend;
                if(updateInternal(connection,name, piece, lastValue, target)){
                    return Optional.of(Pair.of(lastValue,target));
                }
            }while (noLimit || retry-- > 0);
            return Optional.empty();
        }
        catch (SQLException e) {
            log.warn(e.getMessage(),e);
            throw new SeqException(e.getMessage(), e);
        }
    }

    @Override
	public Optional<Long> getNextValue(String name, String piece) {
		try (Connection connection = getConnection()) {
			return selectInternal(connection,name, piece);
		}
		catch (SQLException e) {
			throw new SeqException(e.getMessage(), e);
		}
	}

	protected Optional<Long> selectInternal(Connection connection, String name, String slice) throws SQLException {
		try (PreparedStatement statement = connection
						.prepareStatement(MYSQL_SELECT_VALUE.replace("$TABLE_NAME", tableName))) {
			statement.setString(1, name);
			statement.setString(2, slice);
			try (ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()) {
                    if (resultSet.getObject(1) == null) {
                        throw new IllegalStateException("bad seq value");
                    }
                    return Optional.of(resultSet.getLong(1));
                }
                return Optional.empty();
            }
		}
	}

	protected boolean createIgnoreInternal(Connection connection, String name, String slice, long nextValue) throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		try (PreparedStatement statement = connection
						.prepareStatement(MYSQL_INSERT_IGNORE.replace("$TABLE_NAME", tableName))) {
			statement.setString(1, name);
			statement.setString(2, slice);
			statement.setLong(3, nextValue);
			statement.setTimestamp(4, now);
			statement.setTimestamp(5, null);
			return statement.executeUpdate() > 0;
		}
	}

	protected boolean updateInternal(Connection connection, String name, String slice, long nextValueOld, long nextValueNew) throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		try (PreparedStatement statement = connection
						.prepareStatement(MYSQL_UPDATE_VALUE.replace("$TABLE_NAME", tableName))) {
			statement.setLong(1, nextValueNew);
			statement.setTimestamp(2, now);
			statement.setString(3, name);
			statement.setString(4, slice);
			statement.setLong(5, nextValueOld);
			return statement.executeUpdate() > 0;
		}
	}
	protected Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            log.warn(e.getMessage(),e);
            throw new SeqException(e.getMessage(),e);
        }
    }

    public static void closeQuietly(AutoCloseable autoCloseable){
	    if(autoCloseable == null){
	        return;
        }
	    try {
            autoCloseable.close();
        } catch (Exception e) {
            // Ignore
        }
    }
}
