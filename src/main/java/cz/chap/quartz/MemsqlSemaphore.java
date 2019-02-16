package cz.chap.quartz;

import org.quartz.impl.jdbcjobstore.DBSemaphore;
import org.quartz.impl.jdbcjobstore.LockException;
import org.quartz.impl.jdbcjobstore.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MemsqlSemaphore extends DBSemaphore {

    private static final int RETRY_COUNT = 1;
    private static final String COL_LOCK_TIME = "LOCK_TIME";


     private static final String INSERT_UPDATE =  "INSERT INTO " + TABLE_PREFIX_SUBST + TABLE_LOCKS
            + " ( " + COL_LOCK_NAME + "," + COL_SCHEDULER_NAME + ") "
            + "VALUES (? , " + SCHED_NAME_SUBST + ") ON DUPLICATE KEY UPDATE `" + COL_LOCK_TIME + "` = ?";

    public MemsqlSemaphore() {
        super(DEFAULT_TABLE_PREFIX, null, INSERT_UPDATE, "SELECT 1");
    }

    /**
     * Execute the SQL select for update that will lock the proper database row.
     */
    @Override
    protected void executeSQL(Connection conn, final String lockName, final String expandedSQL, final String expandedInsertSQL) throws LockException {
        SQLException lastFailure = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                lockViaInsert(conn, lockName, expandedSQL);
                return;
            } catch (SQLException e) {
                lastFailure = e;
                if ((i + 1) == RETRY_COUNT) {
                    getLog().debug("Lock '{}' was not obtained by: {}", lockName, Thread.currentThread().getName());
                } else {
                    getLog().debug("Lock '{}' was not obtained by: {} - will try again.", lockName, Thread.currentThread().getName());
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new LockException("Failure obtaining db row lock: " + lastFailure.getMessage(), lastFailure);
    }

    private void lockViaInsert(Connection conn, String lockName, String sql) throws SQLException {
        getLog().debug("Inserting new lock row for lock: '" + lockName + "' being obtained by thread: " + Thread.currentThread().getName());
        PreparedStatement ps = conn.prepareStatement(sql);
        try {
            ps.setString(1, lockName);
            ps.setLong(2, System.nanoTime());
            if(ps.executeUpdate() == 0) {
                throw new SQLException(Util.rtp(
                        "No row exists, and one could not be inserted in table " + TABLE_PREFIX_SUBST + TABLE_LOCKS +
                                " for lock named: " + lockName, getTablePrefix(), getSchedulerNameLiteral()));
            }
        } finally {
            ps.close();
        }
    }
}
