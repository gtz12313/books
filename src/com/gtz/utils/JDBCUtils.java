package com.gtz.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author 葛天助
 * @version 1.0
 */
public class JDBCUtils {
    private static ThreadLocal<Connection> conns;
    private static DataSource ds;

    static {
        Properties properties = new Properties();
        try {
            InputStream inputStream =
                    JDBCUtils.class.getClassLoader().
                            getResourceAsStream("druid.properties");
            properties.load(inputStream);
            ds = DruidDataSourceFactory.createDataSource(properties);
            conns = new ThreadLocal<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() {
        Connection conn = conns.get();
        if (conn == null) {
            //如果当前线程还没有Connection,从连接池取出一个与该线程绑定(关联)
            try {
                conn = ds.getConnection();
                //设置为手动提交事务
                conn.setAutoCommit(false);
                conns.set(conn);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        }
        return conn;
    }
//已过时
//    public static void close(Object... connects) {
//        for (Object connect : connects) {
//            if (connect == null)
//                continue;
//
//            try {
//                if (connect instanceof ResultSet) {
//                    ((ResultSet) connect).close();
//                    continue;
//                }
//                if (connect instanceof Statement) {
//                    ((Statement) connect).close();
//                    continue;
//                }
//                if (connect instanceof Connection) {
//                    ((Connection) connect).close();
//                }
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    /**
     * 提交事务,并关闭释放连接
     */
    public static void commitAndClose(){
        Connection conn = conns.get();
        if (conn != null){
            try {
                conn.commit(); //提交事务
            } catch (SQLException e) {
                throw new RuntimeException();
            }finally {
                try {
                    conn.close(); //关闭连接
                } catch (SQLException e) {

                }
            }
        }
        //必须执行remove操作,否则会出错
        conns.remove();
    }


    /**
     * 回滚事务,关闭流
     */
    public static void rollback(){
        Connection conn = conns.get();
        if (conn != null){
            try {
                conn.rollback(); //回滚事务
            } catch (SQLException e) {
                throw new RuntimeException();
            }finally {
                try {
                    conn.close(); //关闭连接
                } catch (SQLException e) {

                }
            }
        }
        //必须执行remove操作,否则会出错
        conns.remove();
        System.out.println();
        System.out.println("hello");
    }
}
