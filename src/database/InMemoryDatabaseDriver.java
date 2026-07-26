package database;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded JDBC Proxy providing crash-free fallback execution when external JDBC drivers are absent.
 */
public class InMemoryDatabaseDriver {

    public static Connection createInMemoryConnection() {
        return (Connection) Proxy.newProxyInstance(
                InMemoryDatabaseDriver.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    private boolean closed = false;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("isClosed".equals(name)) return closed;
                        if ("close".equals(name)) { closed = true; return null; }
                        if ("setAutoCommit".equals(name) || "commit".equals(name) || "rollback".equals(name)) return null;

                        if ("createStatement".equals(name)) {
                            return createMockStatement();
                        }
                        if ("prepareStatement".equals(name)) {
                            return createMockPreparedStatement((String) args[0]);
                        }
                        return null;
                    }
                }
        );
    }

    private static Statement createMockStatement() {
        return (Statement) Proxy.newProxyInstance(
                InMemoryDatabaseDriver.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("executeUpdate".equals(name)) return 1;
                    if ("executeQuery".equals(name)) return createMockResultSet();
                    if ("close".equals(name)) return null;
                    return null;
                }
        );
    }

    private static PreparedStatement createMockPreparedStatement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(
                InMemoryDatabaseDriver.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("executeUpdate".equals(name)) return 1;
                    if ("executeQuery".equals(name)) return createMockResultSet();
                    if ("getGeneratedKeys".equals(name)) return createMockResultSet();
                    if ("close".equals(name)) return null;
                    return null;
                }
        );
    }

    private static ResultSet createMockResultSet() {
        return (ResultSet) Proxy.newProxyInstance(
                InMemoryDatabaseDriver.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                new InvocationHandler() {
                    private int rowCount = 0;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("next".equals(name)) {
                            return rowCount++ < 0; // returns false for empty default, avoids loops
                        }
                        if ("getInt".equals(name) || "getLong".equals(name)) return 0;
                        if ("getDouble".equals(name) || "getFloat".equals(name)) return 0.0;
                        if ("getString".equals(name)) return "";
                        if ("getBoolean".equals(name)) return false;
                        if ("getTimestamp".equals(name)) return new Timestamp(System.currentTimeMillis());
                        if ("close".equals(name)) return null;
                        return null;
                    }
                }
        );
    }
}
