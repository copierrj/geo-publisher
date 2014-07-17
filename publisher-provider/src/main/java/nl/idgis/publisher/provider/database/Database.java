package nl.idgis.publisher.provider.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import nl.idgis.publisher.protocol.database.DescribeTable;
import nl.idgis.publisher.protocol.database.FetchTable;
import nl.idgis.publisher.provider.database.messages.Query;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class Database extends UntypedActor {	
	
	private final String driver, url, user, password;
	
	private Connection connection;
	private ActorRef content;
	
	public Database(String driver, String url, String user, String password) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	public static Props props(String driver, String url, String user, String password) {
		return Props.create(Database.class, driver, url, user, password);
	}
	
	@Override
	public void preStart() throws SQLException, ClassNotFoundException {
		if(driver != null) {
			Class.forName(driver);
		}
		connection = DriverManager.getConnection(url, user, password);
		
		content = getContext().actorOf(DatabaseContent.props(connection), "content");
	}
	
	@Override
	public void postStop() throws SQLException {
		connection.close();
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if(msg instanceof FetchTable) {
			content.tell(new Query("select * from " + ((FetchTable)msg).getTableName()), getSender());
		} else if(msg instanceof DescribeTable) {
			String requestedTableName = ((DescribeTable) msg).getTableName();
			
			final String sql;
			int separatorIndex = requestedTableName.indexOf(".");
			if(separatorIndex == -1) {
				sql = "select column_name, data_type from user_tab_columns "
						+ "where table_name = '" + requestedTableName.toUpperCase() + "' "
						+ "order by column_id";
			} else {
				String owner = requestedTableName.substring(0, separatorIndex);
				String tableName = requestedTableName.substring(separatorIndex + 1);
				
				sql = "select column_name, data_type from all_tab_columns "
						+ "where owner = '" + owner.toUpperCase() + "' and table_name = '" + tableName.toUpperCase() 
						+ "' " + "order by column_id";
			}
			
			content.tell(new Query(sql), getSender());
		} else {
			unhandled(msg);
		}
	}	
}
