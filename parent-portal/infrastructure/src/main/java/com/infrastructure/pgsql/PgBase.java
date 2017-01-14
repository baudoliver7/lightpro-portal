package com.infrastructure.pgsql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.infrastructure.core.DomainMetadata;
import com.infrastructure.datasource.Base;
import com.infrastructure.datasource.DomainsStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PgBase implements Base {
	
	private transient Connection conn;
	private transient UUID author;
	private static transient DataSource origin;

	public PgBase(UUID author) {
        this.author = author;
    }
	
	public UUID author(){
		return author;
	}
		
	synchronized private static DataSource getDs() {

    	if(origin == null){
    		HikariConfig config = new HikariConfig();
    		config.setJdbcUrl("jdbc:postgresql://localhost:5432/lightpro_db");
    		config.setUsername("openpg");
    		config.setPassword("openpgpwd");
    		config.setDriverClassName("org.postgresql.Driver");
    		config.addDataSourceProperty("cachePrepStmts", "true");
    		config.addDataSourceProperty("prepStmtCacheSize", "250");
    		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            return origin = new HikariDataSource(config);
    	}else
    		return origin;        
    }

	@Override
	public List<Object> executeQuery(String query, List<Object> params) throws IOException {
		
		Connection conn1 = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		List<Object> values = new ArrayList<Object>();
		
		try {
			conn1 = getDs().getConnection();
			pstmt = conn1.prepareStatement(query);
			
			for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(i+1, params.get(i));
			}
						
            rs = pstmt.executeQuery();    
            
            while(rs.next()){
            	values.add(rs.getObject(1));
            }                                             
        } catch (final SQLException ex) {
            throw new IOException(ex);
        } finally{
        	DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pstmt);
            DbUtils.closeQuietly(conn1);
        }
		
		return values;	
	}

	@Override
	public void executeUpdate(String query, List<Object> params) throws IOException {
		
		PreparedStatement pstmt = null;
		
        try {        	
        	pstmt = connection().prepareStatement(query);
        	
        	for (int i = 0; i < params.size(); i++) {
				pstmt.setObject(i+1, params.get(i));
			}
        	
        	pstmt.executeUpdate();       	            
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }finally{
        	DbUtils.closeQuietly(pstmt);
        }
	}

	@Override
	public DomainsStore domainsStore(DomainMetadata dm) {
		return new PgDomainsStore(this, dm);
	}

	@Override
	public void beginTransaction() throws IOException {
		try {
			connection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new IOException(e);
		}		
	}

	@Override
	public void commit() throws IOException {
		try {
			connection().commit();			
		} catch (SQLException e) {								
			throw new IOException(e);
		}
	}

	@Override
	public void rollback() throws IOException {
		try {
			connection().rollback();

		} catch (SQLException e) {						
			throw new IOException(e);
		}
	}
	
	synchronized private Connection connection() throws IOException {
		try {
			if(conn == null || conn.isClosed())
				conn = getDs().getConnection();
			
			return conn;
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void terminate() throws IOException {
		DbUtils.closeQuietly(connection());
	}
}